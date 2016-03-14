package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.Project;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.HashMap;
import java.util.Map;

public class ProjectCache {
    private Map<String, Project> projectMap = new HashMap<String, Project>();
    private RestServer restServer;
    private PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache;

    public ProjectCache(final RestServer restServer, PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache) {
        this.restServer = restServer;
        this.pixelDataAnonymiserFilterCache = pixelDataAnonymiserFilterCache;
    }

    public Project getProject(final String projectName, GiftCloudProperties properties, GiftCloudReporter reporter) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new Project(projectName, restServer, pixelDataAnonymiserFilterCache, properties, reporter));
        }
        return projectMap.get(projectName);
    }
}
