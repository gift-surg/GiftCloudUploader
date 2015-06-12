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
    public Optional<String> getSubjectAlias(final GiftCloudServer server, final String projectName, final String patientId, final String patientName) throws IOException {


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
            final Optional<String> subjectAlias = projectMap.getSubjectAlias(projectName, hashedPatientId);
            if (subjectAlias.isPresent()) {
                return subjectAlias;
            }

            try {
                // Check if a mapping already exists on the XNAT server
                final Optional<String> subjectAliasFromServer = server.getSubjectPseudonym(projectName, hashedPatientId);
                if (subjectAliasFromServer.isPresent()) {

                    // Cache the new alias
                    projectMap.addAlias(projectName, hashedPatientId, subjectAliasFromServer.get(), patientId, patientName);

                    return Optional.of(subjectAliasFromServer.get());
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
     * @param subjectAlias the new XNAT subject identifier to be mapped to this patient ID
     * @throws IOException if communication with the XNAT server failed
     */
    public void addSubjectAlias(final GiftCloudServer server, final String projectName, final String patientId, final String subjectAlias, final String patientName) throws IOException {

        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        if (StringUtils.isBlank(patientId)) {
            throw new IllegalArgumentException("A patient ID must be specified.");
        }

        if (StringUtils.isBlank(subjectAlias)) {
            throw new IllegalArgumentException("A subject name must be specified.");
        }

        // Hash the patient ID
        final String hashedPatientId = OneWayHash.hashUid(patientId);

        // Ensure that the local and server caches of the hashed patient ID are not being queried or updated before we check them
        synchronized (synchronizationLock) {

            // Add the hashed patient ID to the XNAT subject
            try {
                server.createPseudonymIfNotExisting(projectName, subjectAlias, hashedPatientId);

            } catch (GiftCloudHttpException exception) {
                // 400 indicates the hashed patient ID request is not supported by the server.
                if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                    // Do nothing; this operation is not supported
                } else {
                    throw exception;
                }
            }

            // Cache the new alias
            projectMap.addAlias(projectName, hashedPatientId, subjectAlias, patientId, patientName);
        }
    }

}
