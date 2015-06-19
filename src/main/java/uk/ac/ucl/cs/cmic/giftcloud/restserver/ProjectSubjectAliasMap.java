package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientListStore;

import java.util.Map;
import java.util.Optional;

/**
 * Stores a map of AliasMap objects, one for each XNAT project
 */
public class ProjectSubjectAliasMap {
    private final Map<String, AliasMap> projectMap;
    private final PatientListStore patientListStore;

    /**
     * Constructs a new ProjectSubjectAliasMap
     *
     * @param patientListStore used to save and load the patient list
     */
    public ProjectSubjectAliasMap(final PatientListStore patientListStore) {
        this.patientListStore = patientListStore;
        projectMap = patientListStore.load();
    }

    /**
     * Returns the XNAT alias for a given hashed patient ID and project
     *
     * @param projectName the XNAT project to which the hashed patient ID and subject is associated
     * @param hashedPatientId the pseudonymised patient ID (PPID) - a one-way hash of the patient ID
     * @return the XNAT alias string
     */
    public Optional<String> getSubjectAlias(final String projectName, final String hashedPatientId) {
        return getAliasMapForProject(projectName).getAlias(hashedPatientId);
    }

    /**
     * Adds alias and other subject information to the local database
     *
     * @param projectName the XNAT project to which this alias will be added
     * @param hashedPatientId a one-way hash of the patient ID
     * @param alias the XNAT alias string for this subject
     * @param patientId the original patient ID (this is only stored locally)
     * @param patientName the original patient name (this is only stored locally)
     */
    public void addAlias(final String projectName, final String hashedPatientId, final String alias, final String patientId, final String patientName) {
        // Get the map for this project
        final AliasMap aliasMapForProject = getAliasMapForProject(projectName);

        // Add the alias
        aliasMapForProject.addAlias(hashedPatientId, alias, patientId, patientName);

        patientListStore.save(projectMap);
    }

    /**
     * Force saving of the patient list
     */
    public void exportPatientList() {
        patientListStore.save(projectMap);
    }

    private AliasMap getAliasMapForProject(final String projectName) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new AliasMap());
        }
        return projectMap.get(projectName);
    }

}
