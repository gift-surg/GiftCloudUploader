package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientListStore;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.OneWayHash;

import java.io.IOException;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * This class is used to get the XNAT subject identifier for a given patient ID. The patient ID is hashed and the hashed
 * identifier used to obtain the XNAT subject from the server. A local cache map is maintained consisting of patient
 * identifiers already used in the current session.
 *
 * This class is threadsafe
 */
public class SubjectAliasStore {

    private final ProjectSubjectAliasMap projectMap;

    // Ensure synchronisation between adding and retrieving hashed patient IDs
    private Object synchronizationLock = new Object();

    /**
     * Creates a SubjectAliasStore
     *
     * @param patientListStore used to load and save the patient list
     * @param reporter for error and progress reporting
     */
    public SubjectAliasStore(final PatientListStore patientListStore, final GiftCloudReporter reporter) {
        projectMap = new ProjectSubjectAliasMap(patientListStore);

    }

    /** Returns the pseudonymised XNAT subject name for the given patient ID
     * @param patientId a string containing the unique patient identifier, typically the DICOM patient ID. Must not be blank.
     * @return An optional containing the XNAT subject ID if it already exists. Returns an empty Optional if a subject
     * ID does not already exist, or if it does not exist locally and the XNAT server does not support pseudonymisations
     * @throws IOException if communication with the server failed
     */
    public Optional<GiftCloudLabel.SubjectLabel> getSubjectAlias(final GiftCloudServer server, final String projectName, final String patientId, final String patientName) throws IOException {


        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        if (StringUtils.isBlank(patientId)) {
            return Optional.empty();
        }

        // First hash the patient ID
        final String hashedPatientId = OneWayHash.hashUid(patientId);

        // Ensure that the local and server caches of the hashed patient ID are not being updated before we check them
        synchronized (synchronizationLock) {

            // If the hashed ID is in our local session cache then return the subject identifier
            final Optional<GiftCloudLabel.SubjectLabel> subjectAlias = projectMap.getSubjectAlias(projectName, hashedPatientId);
            if (subjectAlias.isPresent()) {
                return subjectAlias;
            }

            try {
                // Check if a mapping already exists on the XNAT server
                final Optional<GiftCloudLabel.SubjectLabel> subjectLabelFromServer = server.getSubjectLabel(projectName, hashedPatientId);
                if (subjectLabelFromServer.isPresent()) {

                    // Cache the new alias
                    projectMap.addAlias(projectName, hashedPatientId, subjectLabelFromServer.get(), patientId, patientName);

                    return Optional.of(subjectLabelFromServer.get());
                }

            } catch (GiftCloudHttpException exception) {
                // 400 indicates the hashed patient ID request is not supported by the server.
                if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                    return Optional.empty();
                } else {
                    throw exception;
                }
            }

            return Optional.empty();
        }
    }

    /** Create a new mapping between patient ID and XNAT subject name
     * @param patientId the DICOM patient ID. Must not be blank or null.
     * @param subjectLabel the new XNAT subject identifier to be mapped to this patient ID
     * @throws IOException if communication with the XNAT server failed
     */
    public void addSubjectAlias(final GiftCloudServer server, final String projectName, final String patientId, final GiftCloudLabel.SubjectLabel subjectLabel, final String patientName) throws IOException {

        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        if (StringUtils.isBlank(patientId)) {
            throw new IllegalArgumentException("A patient ID must be specified.");
        }

        if (GiftCloudLabel.isBlank(subjectLabel)) {
            throw new IllegalArgumentException("A subject name must be specified.");
        }

        // Hash the patient ID
        final String hashedPatientId = OneWayHash.hashUid(patientId);

        // Ensure that the local and server caches of the hashed patient ID are not being queried or updated before we check them
        synchronized (synchronizationLock) {

            // Add the hashed patient ID to the XNAT subject
            try {
                server.createSubjectAliasIfNotExisting(projectName, subjectLabel, hashedPatientId);

            } catch (GiftCloudHttpException exception) {
                // 400 indicates the hashed patient ID request is not supported by the server.
                if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                    // Do nothing; this operation is not supported
                } else {
                    throw exception;
                }
            }

            // Cache the new alias
            projectMap.addAlias(projectName, hashedPatientId, subjectLabel, patientId, patientName);
        }
    }

    /**
     * Force saving of the patient list
     */
    public void exportPatientList() {
        synchronized (synchronizationLock) {
            projectMap.exportPatientList();
        }
    }

    public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final GiftCloudServer server, final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final String studyInstanceUid) throws IOException {

        if (StringUtils.isBlank(projectLabel)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        if (GiftCloudLabel.isBlank(subjectLabel)) {
            throw new IllegalArgumentException("A subject label must be specified.");
        }

        if (StringUtils.isBlank(studyInstanceUid)) {
            return Optional.empty();
        }

        // First hash the study instance UID
        final String hashedStudyInstanceUid = OneWayHash.hashUid(studyInstanceUid);

        // Ensure that the local and server caches of the hashed patient ID are not being updated before we check them
        synchronized (synchronizationLock) {

            // If the hashed ID is in our local session cache then return the subject identifier
            final Optional<GiftCloudLabel.ExperimentLabel> experimentLabel = projectMap.getExperimentLabel(projectLabel, subjectLabel, hashedStudyInstanceUid);
            if (experimentLabel.isPresent()) {
                return experimentLabel;
            }

            try {
                // Check if a mapping already exists on the XNAT server
                final Optional<GiftCloudLabel.ExperimentLabel> experimentLabelFromServer = server.getExperimentLabel(projectLabel, subjectLabel, hashedStudyInstanceUid);
                if (experimentLabelFromServer.isPresent()) {

                    // Cache the new label
                    projectMap.addExperimentAlias(projectLabel, subjectLabel, hashedStudyInstanceUid, experimentLabelFromServer.get());

                    return Optional.of(experimentLabelFromServer.get());
                }

            } catch (GiftCloudHttpException exception) {
                // 400 indicates the hashed experiment request is not supported by the server.
                if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                    return Optional.empty();
                } else {
                    throw exception;
                }
            }

            return Optional.empty();
        }
    }

    public void addExperimentAlias(final GiftCloudServer server, final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String studyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {

        if (StringUtils.isBlank(projectLabel)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        if (GiftCloudLabel.isBlank(subjectLabel)) {
            throw new IllegalArgumentException("A subject alias must be specified.");
        }

        if (GiftCloudLabel.isBlank(experimentLabel)) {
            throw new IllegalArgumentException("An experiment alias name must be specified.");
        }

        // Hash the study instance UID
        final String hashedStudyInstanceUid = OneWayHash.hashUid(studyInstanceUid);

        // Ensure that the local and server caches of the hashed patient ID are not being queried or updated before we check them
        synchronized (synchronizationLock) {

            // Add the hashed patient ID to the XNAT subject
            try {
                server.createExperimentAliasIfNotExisting(projectLabel, subjectLabel, experimentLabel, hashedStudyInstanceUid, xnatModalityParams);

            } catch (GiftCloudHttpException exception) {
                // 400 indicates the hashed patient ID request is not supported by the server.
                if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                    // Do nothing; this operation is not supported
                } else {
                    throw exception;
                }
            }

            // Cache the new alias
            projectMap.addExperimentAlias(projectLabel, subjectLabel, hashedStudyInstanceUid, experimentLabel);
        }
    }

    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String seriesInstanceUid) throws IOException {

        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        if (GiftCloudLabel.isBlank(subjectLabel)) {
            throw new IllegalArgumentException("A subject alias must be specified.");
        }

        if (GiftCloudLabel.isBlank(experimentLabel)) {
            throw new IllegalArgumentException("An experiment alias must be specified.");
        }

        if (StringUtils.isBlank(seriesInstanceUid)) {
            return Optional.empty();
        }

        // First hash the series instance UID
        final String hashedSeriesInstanceUid = OneWayHash.hashUid(seriesInstanceUid);

        // Ensure that the local and server caches of the hashed patient ID are not being updated before we check them
        synchronized (synchronizationLock) {

            // If the hashed ID is in our local session cache then return the subject identifier
            final Optional<GiftCloudLabel.ScanLabel> scanLabel = projectMap.getScanLabel(projectName, subjectLabel, experimentLabel, hashedSeriesInstanceUid);
            if (scanLabel.isPresent()) {
                return scanLabel;
            }

            try {
                // Check if a mapping already exists on the XNAT server
                final Optional<GiftCloudLabel.ScanLabel> scanLabelFromServer = server.getScanLabel(projectName, subjectLabel, experimentLabel, hashedSeriesInstanceUid);
                if (scanLabelFromServer.isPresent()) {

                    // Cache the new alias
                    projectMap.addScanAlias(projectName, subjectLabel, experimentLabel, hashedSeriesInstanceUid, scanLabelFromServer.get());

                    return Optional.of(scanLabelFromServer.get());
                }

            } catch (GiftCloudHttpException exception) {
                // 400 indicates the hashed scan request is not supported by the server.
                if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                    return Optional.empty();
                } else {
                    throw exception;
                }
            }

            return Optional.empty();
        }
    }


    public void addScanAlias(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final String seriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {

        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        if (GiftCloudLabel.isBlank(subjectLabel)) {
            throw new IllegalArgumentException("A subject alias must be specified.");
        }

        if (GiftCloudLabel.isBlank(experimentLabel)) {
            throw new IllegalArgumentException("A session alias name must be specified.");
        }

        if (GiftCloudLabel.isBlank(scanLabel)) {
            throw new IllegalArgumentException("A series alias name must be specified.");
        }

        if (StringUtils.isBlank(seriesInstanceUid)) {
            throw new IllegalArgumentException("A series instance UID must be specified.");
        }

        // Hash the series instance UID
        final String hashedSeriesInstanceUid = OneWayHash.hashUid(seriesInstanceUid);

        // Ensure that the local and server caches of the hashed patient ID are not being queried or updated before we check them
        synchronized (synchronizationLock) {

            // Add the hashed patient ID to the XNAT subject
            try {
                server.createScanAliasIfNotExisting(projectName, subjectLabel, experimentLabel, scanLabel, hashedSeriesInstanceUid, xnatModalityParams);

            } catch (GiftCloudHttpException exception) {
                // 400 indicates the hashed patient ID request is not supported by the server.
                if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                    // Do nothing; this operation is not supported
                } else {
                    throw exception;
                }
            }

            // Cache the new alias
            projectMap.addScanAlias(projectName, subjectLabel, experimentLabel, hashedSeriesInstanceUid, scanLabel);
        }
    }

}
