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

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.UIUtils;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

public class MultiUploadParameters {

    private MultiUploadAppletParameters multiUploadAppletParameters;

    public MultiUploadParameters(final MultiUploadAppletParameters multiUploadAppletParameters) {
        this.multiUploadAppletParameters = multiUploadAppletParameters;
    }

    public String getParameter(final String key) {
        return multiUploadAppletParameters.getParameter(key);
    }

    public boolean getDateFromSession() {
        if (UIUtils.getConfirmSessionDatePage()) {
            return true;
        }

        return "no_session_date".equals(multiUploadAppletParameters.getParameter(MultiUploadParameters.XNAT_SCAN_DATE));
    }


    public String getParameter(final String key, final String defaultValue) {
        final String v = multiUploadAppletParameters.getParameter(key);
        return null == v ? defaultValue : v;
    }

    public Optional<String> getOptionalParameter(String key) {
        final String value = multiUploadAppletParameters.getParameter(key);
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }

    public Optional<String> getProjectName() {
        return getOptionalParameter(XNAT_PROJECT);
    }

    public Optional<String> getSubjectName() {
        return getOptionalParameter(XNAT_SUBJECT);
    }

    public Optional<String> getScanDate() {
        return getOptionalParameter(XNAT_SCAN_DATE);
    }

    public Optional<String> getXnatUrl() {
        return getOptionalParameter(XNAT_URL);
    }

    public Optional<String> getStrippedXnatUrl() {
        final Optional<String> url = getXnatUrl();
        if (url.isPresent()) {
            return Optional.of(StringUtils.stripEnd(url.get(), "/"));
        } else{
            return url;
        }
    }

    public Optional<String> getJsessionId() {
        return getOptionalParameter(MultiUploadParameters.JSESSIONID);
    }


    public static final String EXPECTED_MODALITY_LABEL = "*expected-modality*";
    public static final String XNAT_PROJECT = "xnat-project";
    public static final String XNAT_SUBJECT = "xnat-subject";
    public static final String XNAT_SCAN_DATE = "xnat-scan-date";
    public static final String XNAT_VISIT = "xnat-visit";
    public static final String XNAT_PROTOCOL = "xnat-protocol";
    public static final String EXPECTED_MODALITY = "expected-modality";
    public static final String XNAT_SCAN_TYPE = "xnat-scan-type";
    public static final String XNAT_SESSION = "xnat-session-label";
    public static final String XNAT_URL = "xnat-url";
    public static final String XNAT_DESCRIPTION = "xnat-description";
    public static final String XNAT_ADMIN_EMAIL = "xnat-admin-email";
    public static final String LOG4J_PROPS_URL = "log4j-properties-url";
    public static final String CUSTOM_PROPS_URL = "custom-properties-url";
    public static final String ENABLE_REMOTE_LOGGING = "enable-remote-logging";
    public static final String REMOTE_LOGGING_PATH = "remote-logging-path";
    public static final String USE_FIXED_SIZE_STREAMING = "fixed-size-streaming";
    public static final String N_UPLOAD_THREADS = "n-upload-threads";
    public static final String WINDOW_NAME = "window-name";
    public static final String JSESSIONID = "jsessionid";


}
