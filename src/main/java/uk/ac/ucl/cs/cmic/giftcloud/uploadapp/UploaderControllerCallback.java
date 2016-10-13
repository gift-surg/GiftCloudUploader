/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.PasswordAuthentication;

/**
 * Class to handle callback requests an in interactive user environment
 */
public class UploaderControllerCallback implements UserCallback {

    private final GiftCloudPropertiesFromApplication properties;
    private final GiftCloudDialogs dialogs;
    private final Component container;

    public UploaderControllerCallback(final GiftCloudUploaderAppConfiguration appConfiguration, final GiftCloudDialogs dialogs, final Component container) throws InvocationTargetException, InterruptedException {
        this.properties = appConfiguration.getProperties();
        this.container = container;
        this.dialogs = dialogs;
    }

    @Override
    public String getProjectName(GiftCloudServer server) throws IOException {
        final Optional<String> lastProjectName = properties.getLastProject();
        if (lastProjectName.isPresent() && StringUtils.isNotBlank(lastProjectName.get())) {
            return lastProjectName.get();
        } else {
            try {
                final String selectedProject = dialogs.showInputDialogToSelectProject(server.getListOfProjects(), container, lastProjectName);
                properties.setLastProject(selectedProject);
                properties.save();
                return selectedProject;
            } catch (IOException e) {
                throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(final String supplementalMessage) {
        return dialogs.getPasswordAuthentication(supplementalMessage);
    }
}
