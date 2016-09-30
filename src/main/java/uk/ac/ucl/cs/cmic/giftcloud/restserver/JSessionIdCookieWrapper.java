/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

final class JSessionIdCookieWrapper {
    private Optional<String> jSessionId;

    JSessionIdCookieWrapper(final Optional<String> jSessionId) {
        if (jSessionId.isPresent() && !StringUtils.isBlank(jSessionId.get())) {
            this.jSessionId = Optional.of(jSessionId.get());
        } else {
            this.jSessionId = Optional.empty();
        }
    }

    String getFormattedCookieString() {
        if (jSessionId.isPresent()) {
            return String.format("JSESSIONID=%s", jSessionId.get());
        } else {
            return "";
        }
    }

    boolean isValid() {
        return jSessionId.isPresent();
    }

    public String toString() {
        return getFormattedCookieString();
    }

    void replaceCookie(final String cookie) {
        jSessionId = Optional.of(cookie);
    }
}
