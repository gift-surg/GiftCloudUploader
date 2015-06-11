package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientListStore;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.Map;
import java.util.Optional;

public class ProjectSubjectAliasMap {
    private final Map<String, AliasMap> projectMap;
    private final PatientListStore patientListStore;

    public ProjectSubjectAliasMap(final GiftCloudReporter reporter) {
        patientListStore = new PatientListStore(reporter);
        projectMap = patientListStore.load();
    }

    public Optional<String> getSubjectAlias(final String projectName, final String hashedPatientId) {
        return getAliasMapForProject(projectName).getAlias(hashedPatientId);
    }

    public void addAlias(final String projectName, final String hashedPatientId, final String alias, final String patientId, final String patientName) {
        // Get the map for this project
        final AliasMap aliasMapForProject = getAliasMapForProject(projectName);

        // Add the alias
        aliasMapForProject.addAlias(hashedPatientId, alias, patientId, patientName);

        patientListStore.update(projectMap);
    }

    private AliasMap getAliasMapForProject(final String projectName) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new AliasMap());
        }
        return projectMap.get(projectName);
    }

}
