/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

class FakeLoginAuthenticator extends Authenticator {
    private String userName;
    private char[] password;
    private boolean userCancel;
    private int getPasswordAuthenticationCount = 0;

    FakeLoginAuthenticator(final String userName, final char[] password, final boolean userCancel) {
        this.userName = userName;
        this.password = password;
        this.userCancel = userCancel;
    }


    protected PasswordAuthentication getPasswordAuthentication() {
        getPasswordAuthenticationCount++;
        if (userCancel) {
            return null;
        } else {
            return new PasswordAuthentication(userName, password);
        }
    }

    int getAuthenticationCount() {
        return getPasswordAuthenticationCount;
    }
}
