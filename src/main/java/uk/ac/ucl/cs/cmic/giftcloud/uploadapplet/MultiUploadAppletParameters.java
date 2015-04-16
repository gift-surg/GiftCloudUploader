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
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.applet.Applet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MultiUploadAppletParameters extends MultiUploadParameters {

    private final Applet applet;
    private MultiUploadReporter reporter;

    public MultiUploadAppletParameters(final Applet applet, final MultiUploadReporter reporter) {
        this.applet = applet;
        this.reporter = reporter;

        Properties properties = new Properties(System.getProperties());
        loadAppletProperties(properties);
        loadCustomProperties(properties);
        System.setProperties(properties);
    }

    public String getParameter(final String key) {
        return applet.getParameter(key);
    }


    @Override
    public boolean getDateFromSession() {
        if (UIUtils.getConfirmSessionDatePage()) {
            return true;
        }

        return "no_session_date".equals(getParameter(MultiUploadParameters.XNAT_SCAN_DATE));
    }

    /**
     * Loads applet properties from the <b>applet.properties</b>properties file. This
     * can be overridden by placing a custom version of the properties file ahead of
     * the applet jar on the classpath.
     *
     * @param properties The system properties for the applet.
     */
    private void loadAppletProperties(Properties properties) {
        InputStream appletProperties = null;

        try {
            appletProperties = applet.getClass().getResourceAsStream("/uk/ac/ucl/cs/cmic/giftcloud/uploadapplet/applet.properties");
            if (appletProperties != null) {
                properties.load(appletProperties);
            }
        } catch (IOException exception) {
            reporter.info("Unable to find the applet.properties resource for initialization", exception);
        } finally {
            if (appletProperties != null) {
                try {
                    appletProperties.close();
                } catch (IOException e) {
                    // Whatever.
                }
            }
        }
    }

    /**
     * Loads properties from custom properties specified in the CUSTOM_PROPS_URL
     * applet parameter. If this parameter is not specified, no properties are loaded.
     *
     * @param properties The system properties for the applet.
     */
    private void loadCustomProperties(Properties properties) {
        final String customProps = getParameter(MultiUploadParameters.CUSTOM_PROPS_URL);
        if (StringUtils.isNotBlank(customProps)) {
            InputStream customProperties = null;
            try {
                customProperties = applet.getClass().getResourceAsStream(customProps);
                properties.load(customProperties);
            } catch (IOException exception) {
                reporter.info("Unable to find the custom properties resource " + customProps + " for initialization", exception);
            } finally {
                if (customProperties != null) {
                    try {
                        customProperties.close();
                    } catch (IOException ignored) {
                        // Once again, whatever.
                    }
                }
            }
        }
    }


}
