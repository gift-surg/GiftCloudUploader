package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientListStore;

import java.io.IOException;
import java.util.Map;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

/**
 * Stores a map of PatientAliasMap objects, one for each XNAT project
 */
public class ProjectSubjectAliasMap {
    private final Map<String, PatientAliasMap> projectMap;
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
    public Optional<GiftCloudLabel.SubjectLabel> getSubjectAlias(final String projectName, final String hashedPatientId) {
        return getAliasMapForProject(projectName).getSubjectLabel(hashedPatientId);
    }

    /**
     * Adds subjectLabel and other subject information to the local database
     *
     * @param projectName the XNAT project to which this subjectLabel will be added
     * @param hashedPatientId a one-way hash of the patient ID
     * @param subjectLabel the XNAT subject label string for this subject
     * @param patientId the original patient ID (this is only stored locally)
     * @param patientName the original patient name (this is only stored locally)
     */
    public void addAlias(final String projectName, final String hashedPatientId, final GiftCloudLabel.SubjectLabel subjectLabel, final String patientId, final String patientName) {
        // Get the map for this project
        final PatientAliasMap patientAliasMapForProject = getAliasMapForProject(projectName);

        // Add the subjectLabel
        patientAliasMapForProject.addSubjectAlias(hashedPatientId, subjectLabel, patientId, patientName);

        patientListStore.save(projectMap);
    }

    /**
     * Force saving of the patient list
     */
    public void exportPatientList() {
        patientListStore.save(projectMap);
    }

    /**
     * Gets the GIFT-Cloud label for the experiment with a specific pseudonymised UID
     *
     * @param projectLabel the GIFT-Cloud project label
     * @param subjectLabel the GIFT-Cloud subject label
     * @param hashedStudyInstanceUid the pseudonymised experiment UID
     * @return An Optional which contains the experiment label if the pseudonymised UID exists; otherwise returns an Optional.Empty
     */
    public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid) {
        final PatientAliasMap patientAliasMap = getAliasMapForProject(projectLabel);
        return patientAliasMap.getExperimentLabel(subjectLabel, hashedStudyInstanceUid);
    }

    /**
     * Gets the GIFT-Cloud label for the scan with a specific pseudonymised UID
     *
     * @param projectLabel the GIFT-Cloud project label
     * @param subjectLabel the GIFT-Cloud subject label
     * @param experimentLabel the GIFT-Cloud experiment label
     * @param hashedSeriesInstanceUid the pseudonymised scan UID
     * @return An Optional which contains the scan label if the pseudonymised UID exists; otherwise returns an Optional.Empty
     */
    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid) {
        final PatientAliasMap patientAliasMap = getAliasMapForProject(projectLabel);
        return patientAliasMap.getScanLabel(subjectLabel, experimentLabel, hashedSeriesInstanceUid);
    }

    /**
     * Adds a new alias (label and UID) for an experiment
     *
     * @param projectLabel the GIFT-Cloud project label
     * @param subjectLabel the GIFT-Cloud subject label
     * @param hashedStudyInstanceUid the pseudonymised experiment UID
     * @param experimentLabel the experiment label
     * @throws IOException if the project label is unknown
     */
    public void addExperimentAlias(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid, final GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException {
        final PatientAliasMap patientAliasMap = getAliasMapForProject(projectLabel);
        patientAliasMap.addExperimentAlias(subjectLabel, hashedStudyInstanceUid, experimentLabel);
    }

    /**
     * Adds a new alias (label and UID) for a scan
     *
     * @param projectLabel the GIFT-Cloud project label
     * @param subjectLabel the GIFT-Cloud subject label
     * @param experimentLabel the GIFT-Cloud experiment label
     * @param hashedSeriesInstanceUid the pseudonymised scan UID
     * @param experimentLabel the experiment label
     * @throws IOException if the project label is unknown
     */
    public void addScanAlias(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid, final GiftCloudLabel.ScanLabel scanLabel)  throws IOException {
        final PatientAliasMap patientAliasMap = getAliasMapForProject(projectLabel);
        patientAliasMap.addScanAlias(subjectLabel, experimentLabel, hashedSeriesInstanceUid, scanLabel);
    }

    private PatientAliasMap getAliasMapForProject(final String projectName) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new PatientAliasMap());
        }
        return projectMap.get(projectName);
    }
}
