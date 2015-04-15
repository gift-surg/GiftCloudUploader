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

import java.util.Optional;

public class GiftCloudPropertiesFromApplet implements GiftCloudProperties {

    private static final String DEV_USER = "dev-user";
    private static final String DEV_PASS = "dev-pass";

    private MultiUploadParameters multiUploadParameters;

    public GiftCloudPropertiesFromApplet(MultiUploadParameters multiUploadParameters) {

        this.multiUploadParameters = multiUploadParameters;
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

}
