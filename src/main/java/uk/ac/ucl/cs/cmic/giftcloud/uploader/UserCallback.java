/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;

import java.io.IOException;
import java.net.PasswordAuthentication;

/**
 * Callback interface for requesting information that might require user interaction when run in an interactive mode, and will need to be handled appropriately if run in a non-interactive mode
 */
public interface UserCallback {
    /**
     * Requests the name of a project to which data will be uploaded
     * @param server the {@link GiftCloudServer} object to which data will be uploaded
     * @return the name of the project to which data will be uploaded
     */
    String getProjectName(final GiftCloudServer server) throws IOException;

    /**
     * Prompts user for a username and password, with a supplemental message
     * @param supplementalMessage
     * @return a {@link PasswordAuthentication} object or {@link null}
     */
    PasswordAuthentication getPasswordAuthentication(final String supplementalMessage);
}