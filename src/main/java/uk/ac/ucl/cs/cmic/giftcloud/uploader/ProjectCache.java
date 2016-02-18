package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.Project;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.HashMap;
import java.util.Map;

public class ProjectCache {
    private Map<String, Project> projectMap = new HashMap<String, Project>();
    private RestServer restServer;

    public ProjectCache(final RestServer restServer) {
        this.restServer = restServer;
    }

    public Project getProject(final String projectName, GiftCloudReporter reporter) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new Project(projectName, restServer, reporter));
        }
        return projectMap.get(projectName);
    }
}
