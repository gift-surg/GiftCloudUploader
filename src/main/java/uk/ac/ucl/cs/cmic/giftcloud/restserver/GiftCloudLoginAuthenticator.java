/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

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
