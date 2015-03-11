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

package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.google.common.base.Joiner;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariableNames;

import java.util.Calendar;

public class MultiUploadAppletParameterInfo {
    private static final String[][] PARAMETER_INFO = {
            {MultiUploadParameters.XNAT_URL, "URL", "Base URL for XNAT instance"},
            {MultiUploadParameters.XNAT_DESCRIPTION, "string", "Human-readable name of the XNAT instance"},
            {MultiUploadParameters.XNAT_ADMIN_EMAIL, "email address", "Email address for the XNAT administrator"},
            {SessionVariableNames.WARN_ON_DUPE_SESSION_LABELS, "boolean", "Indicates whether the applet should warn when session labels are specified that are duplicates of existing session labels in the target project; defaults to true."},
            {SessionVariableNames.ALLOW_OVERWRITE_ON_DUPE_SESSION_LABELS, "boolean", "Indicates whether the applet should allow the user to select the overwrite option when session labels are specified that are duplicates of existing session labels in the target project; defaults to false."},
            {MultiUploadParameters.USE_FIXED_SIZE_STREAMING, "boolean", "Should the applet use fixed-size streaming for data upload?"},
            {MultiUploadParameters.N_UPLOAD_THREADS, "integer", "Number of threads to use for uploading"},
            {MultiUploadParameters.LOG4J_PROPS_URL, "URL", "URL for log4j properties file"},
            {MultiUploadParameters.CUSTOM_PROPS_URL, "URL", "URL for custom properties file"},
            {MultiUploadParameters.ENABLE_REMOTE_LOGGING, "boolean", "Indicates whether the applet should use remote logging"},
            {MultiUploadParameters.REMOTE_LOGGING_PATH, "string", "Path to the REST service on the remote server to handle logging"},
            {MultiUploadParameters.XNAT_PROJECT, "string", "Indicates the project to which the uploaded resource should be added"},
            {MultiUploadParameters.XNAT_SUBJECT, "string", "Indicates the subject to which the uploaded resource should be added"},
            {MultiUploadParameters.XNAT_SCAN_DATE, "string", "Indicates the date the scan to be uploaded was acquired"},
            {MultiUploadParameters.XNAT_VISIT, "string", "The visit label for an existing pVisitData to which you would like to add these images"},
            {MultiUploadParameters.XNAT_PROTOCOL, "string", "The protocol label for an experiment to differentiate identical XSI types within the same pVisitData"},
            {MultiUploadParameters.EXPECTED_MODALITY, "string", "The modality you expect to upload. A warning message will appear if the image's modality doesn't match this string"},
            {MultiUploadParameters.XNAT_SCAN_TYPE, "string", "The type of scan to be uploaded"},
            {MultiUploadParameters.WINDOW_NAME, "string", "The name of the window running the applet. Defaults to applet."},
            {MultiUploadParameters.JSESSIONID, "string", "The JSESSIONID to authenticate the REST calls"},
    };

    private static final String AUTHORS = "Author: Kevin A. Archie <karchie@wustl.edu>, Rick Herrick <rick.herrick@wustl.edu>";
    private static final String COPYRIGHT = String.format("Copyright (c) 2014-%d University College London", Calendar.getInstance().get(Calendar.YEAR));
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String APPLET_INFO = Joiner.on(LINE_SEPARATOR).join(AUTHORS, COPYRIGHT);


    public static final String[][] getParameterInfo() {
        return PARAMETER_INFO;
    }

    public static final String getAppletInfo() {
        return APPLET_INFO;
    }
}
