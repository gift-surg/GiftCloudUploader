/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

public final class ProjectSessionLister implements Callable<Map<String,String>> {
	private final RestClient restServer;
	private final String projectName;
	
	public ProjectSessionLister(final RestClient restClient, final String projectName) {
		this.restServer = restClient;
		this.projectName = projectName;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Map<String,String> call()
            throws IOException, JSONException {
		return restServer.getListOfSessions(projectName);
	}
}
