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

import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadResult;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.util.*;

public interface Session {
	String getID();
	String getAccession();
	Date getDateTime();
	String getDescription();
	int getScanCount();
	int getFileCount();
	long getSize();
	Set<String> getModalities();
	String getFormat();
	List<SessionVariable> getVariables(final Project project, final Session session);
    String getPatientId();
    String getStudyUid();
    String getSeriesUid();

	/**
	 * Unify the provided SessionVariables into the script-defined variables.
	 * @param vars variables originating from outside scripts
	 * @param project
     * @return List of unified SessionVariables, in dependency order
	 */
	/*
	List<SessionVariable> unify(Iterable<? extends SessionVariable> vars);
	*/
	UploadResult uploadTo(final boolean append, final String projectLabel, final String subjectLabel, final GiftCloudServer server, final SessionParameters sessionParameters, final Project project, final UploadFailureHandler failureHandler, final GiftCloudReporter reporter) throws IOException;

	TimeZone getTimeZone();
}
