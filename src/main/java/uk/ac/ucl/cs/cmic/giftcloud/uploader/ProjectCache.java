/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.Project;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestClient;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.HashMap;
import java.util.Map;

public class ProjectCache {
    private Map<String, Project> projectMap = new HashMap<String, Project>();
    private RestClient restClient;
    private PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache;

    public ProjectCache(final RestClient restClient, PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache) {
        this.restClient = restClient;
        this.pixelDataAnonymiserFilterCache = pixelDataAnonymiserFilterCache;
    }

    public Project getProject(final String projectName, GiftCloudProperties properties, GiftCloudReporter reporter) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new Project(projectName, restClient, pixelDataAnonymiserFilterCache, properties, reporter));
        }
        return projectMap.get(projectName);
    }
}
