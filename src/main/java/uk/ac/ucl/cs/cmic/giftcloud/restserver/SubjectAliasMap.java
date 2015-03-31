package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.OneWayHash;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * This class is used to get the XNAT subject identifier for a given patient ID. The patient ID is hashed and the hashed
 * identifier used to obtain the XNAT subject from the server. A local cache map is maintained consisting of patient
 * identifiers already used in the current session.
 */
public class SubjectAliasMap {

    private final RestServerHelper restServerHelper;
    private final Map<String, Map<String, String>> projectMap = new HashMap<String, Map<String, String>>();

    public SubjectAliasMap(final RestServerHelper restServerHelper) {
        this.restServerHelper = restServerHelper;
    }

    /** Returns the pseudonymised XNAT subject name for the given patient ID
     * @param patientId a string containing the unique patient identifier, typically the DICOM patient ID. Must not be blank.
     * @return An optional containing the XNAT subject ID if it already exists. Returns an empty Optional if a subject
     * ID does not already exist, or if it does not exist locally and the XNAT server does not support pseudonymisations
     * @throws IOException if communication with the server failed
     */
    public Optional<String> getSubjectAlias(final String projectName, final String patientId) throws IOException {

        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        // Get the map for this project
        final Map<String, String> hashedIdToSubjectMap = getMapForProject(projectName);

        // First hash the patient ID
        final String hashedPatientId = OneWayHash.hashUid(patientId);

        // If the hashed ID is in our local session cache then return the subject identifier
        if (hashedIdToSubjectMap.containsKey(hashedPatientId)) {
            return Optional.of(hashedIdToSubjectMap.get(hashedPatientId));
        }


        try {
            // Check if a mapping already exists on the XNAT server
            final Optional<String> subjectAliasFromServer = restServerHelper.getSubjectPseudonym(projectName, hashedPatientId);
            if (subjectAliasFromServer.isPresent()) {
                hashedIdToSubjectMap.put(hashedPatientId, subjectAliasFromServer.get());
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

    /** Create a new mapping between patient ID and XNAT subject name
     * @param patientId the DICOM patient ID. Must not be blank or null.
     * @param subjectName the new XNAT subject identifier to be mapped to this patient ID
     * @throws IOException if communication with the XNAT server failed
     */
    public void addSubjectAlias(final String projectName, final String patientId, final String subjectName) throws IOException {

        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("A project name must be specified.");
        }

        // Get the map for this project
        final Map<String, String> hashedIdToSubjectMap = getMapForProject(projectName);

        // Hash the patient ID
        final String hashedPatientId = OneWayHash.hashUid(patientId);

        // Add the hashed patient ID to the XNAT subject
        try {
            restServerHelper.createPseudonymIfNotExisting(projectName, subjectName, hashedPatientId);

        } catch (GiftCloudHttpException exception) {
            // 400 indicates the hashed patient ID request is not supported by the server.
            if (exception.getResponseCode() == HTTP_BAD_REQUEST) {
                // Do nothing; this operation is not supported
            } else {
                throw exception;
            }
        }

        // Cache in our local map
        hashedIdToSubjectMap.put(hashedPatientId, subjectName);
    }

    private Map<String, String> getMapForProject(final String projectName) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new HashMap<String, String>());
        }
        return projectMap.get(projectName);
    }
}
