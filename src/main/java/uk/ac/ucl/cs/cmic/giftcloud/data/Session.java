/*
 * Session
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;

import java.util.List;

public interface Session {
    String getPatientId();
	String getPatientName();
    String getStudyUid();
    String getSeriesUid();
	List<FileCollection> getFiles();
	XnatModalityParams getXnatModalityParams();

}
