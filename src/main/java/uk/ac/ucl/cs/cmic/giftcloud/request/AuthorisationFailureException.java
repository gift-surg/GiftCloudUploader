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

package uk.ac.ucl.cs.cmic.giftcloud.request;

import java.io.IOException;
import java.net.URL;

public class AuthorisationFailureException extends IOException {
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