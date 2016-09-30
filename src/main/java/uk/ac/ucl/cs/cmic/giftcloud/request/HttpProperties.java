/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.request;

/**
 * Stores configurable properties required for creating an Http connection
 */
public class HttpProperties {
    private final String userAgentString;
    private final int shortTimeout;
    private final int longTimeout;

    public HttpProperties(final String userAgentString, final int shortTimeout, final int longTimeout) {
        this.userAgentString = userAgentString;
        this.shortTimeout = shortTimeout;
        this.longTimeout = longTimeout;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public int getShortTimeout() {
        return shortTimeout;
    }

    public int getLongTimeout() {
        return longTimeout;
    }
}
