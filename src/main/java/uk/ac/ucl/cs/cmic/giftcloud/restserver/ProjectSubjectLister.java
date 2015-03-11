/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.json.JSONException;

public final class ProjectSubjectLister implements Callable<Map<String,String>> {
	private final RestServerHelper restServerHelper;
	private final String projectName;

	public ProjectSubjectLister(final RestServerHelper restServerHelper, final String projectName) {
		this.restServerHelper = restServerHelper;
		this.projectName = projectName;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Map<String,String> call() throws IOException, JSONException {
		return restServerHelper.getListOfSubjects(projectName);
	}
}
