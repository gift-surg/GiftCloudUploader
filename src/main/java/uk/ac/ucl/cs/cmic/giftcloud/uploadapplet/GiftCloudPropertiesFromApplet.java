/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.File;
import java.util.Optional;
import java.util.ResourceBundle;

public class GiftCloudPropertiesFromApplet implements GiftCloudProperties {

    private static final String DEV_USER = "dev-user";
    private static final String DEV_PASS = "dev-pass";

    private MultiUploadParameters multiUploadParameters;
    private final String userAgentString;

    public GiftCloudPropertiesFromApplet(final MultiUploadParameters multiUploadParameters, final ResourceBundle resourceBundle) {
        this.multiUploadParameters = multiUploadParameters;

        // Set the user agent string for the applet
        final String nameString = resourceBundle.getString("userAgentNameApplet");
        final String versionString = resourceBundle.getString("mavenVersion");
        userAgentString = (nameString != null ? nameString : "") + (versionString != null ? versionString : "");
    }

    @Override
    public Optional<String> getGiftCloudUrl() {
        return multiUploadParameters.getStrippedXnatUrl();
    }

    @Override
    public Optional<String> getLastUserName() {
        String lastUserName = multiUploadParameters.getParameter(DEV_USER);
        if (StringUtils.isNotBlank(lastUserName)) {
            return Optional.of(lastUserName);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getUserAgentString() {
        return userAgentString;
    }

    @Override
    public void setLastUserName(final String lastUserName) {
        // We do not store the last username when using the applet, since the applet is typically launched after already logging into the server
    }

    @Override
    public Optional<char[]> getLastPassword() {
        String password = multiUploadParameters.getParameter(DEV_PASS);
        if (StringUtils.isNotBlank(password)) {
            return Optional.of(password.toCharArray());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setLastPassword(char[] text) {
        // We do not store the last password when using the applet, since the applet is typically launched after already logging into the server
    }

    @Override
    public Optional<String> getSessionCookie() {
        return multiUploadParameters.getJsessionId();
    }

    @Override
    public Optional<String> getLastProject() {

        // ToDo: decide what we should return here
        return Optional.empty();
    }

    @Override
    public void setLastProject(String lastProjectName) {
        // ToDo
    }

    @Override
    public File getUploadFolder(GiftCloudReporter reporter) {
        // ToDo: the uploader folder is not currently an applet parameter, so we always use the default
        return MultiUploaderUtils.createOrGetLocalUploadCacheDirectory(reporter);
    }

}
