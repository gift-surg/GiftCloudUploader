/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.io.IOException;
import java.net.URL;

class AuthorisationFailureException extends IOException {
    private static final long serialVersionUID = 1L;
    private final int responseCode;
    private URL url;

    AuthorisationFailureException(final int responseCode, final URL url) {
        super("Could not log into the GIFT-Cloud server. Please check your username and password are correct.");
        this.responseCode = responseCode;
        this.url = url;
    }

    int getResponseCode() {
        return responseCode;
    }

    URL getUrl() {
        return url;
    }
}