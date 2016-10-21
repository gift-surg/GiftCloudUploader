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
import uk.ac.ucl.cs.cmic.giftcloud.dicom.SeriesZipper;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.Project;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.UploadParameters;

import java.io.IOException;
import java.util.List;

public interface Study {
    String getPatientId();
	String getPatientName();
    String getStudyUid();
    String getSeriesUid();
	List<FileCollection> getFiles();
	XnatModalityParams getXnatModalityParams();

    SeriesZipper getSeriesZipper(final Project project, final UploadParameters uploadParameters) throws IOException;

    boolean isAnonymised();
}
