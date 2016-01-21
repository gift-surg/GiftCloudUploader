/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Used to provide login details to the server in response to an authentication failure, most likely resulting from a session timeout
 */
class GiftCloudLoginAuthenticator extends Authenticator {
    private PasswordAuthenticationWrapper passwordAuthenticationWrapper;

    GiftCloudLoginAuthenticator(PasswordAuthenticationWrapper passwordAuthenticationWrapper) {
        this.passwordAuthenticationWrapper = passwordAuthenticationWrapper;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        Optional<PasswordAuthentication> passwordAuthentication = passwordAuthenticationWrapper.get();
        if (passwordAuthentication.isPresent()) {
            return passwordAuthentication.get();
        } else {
            return null;
        }
    }
}
