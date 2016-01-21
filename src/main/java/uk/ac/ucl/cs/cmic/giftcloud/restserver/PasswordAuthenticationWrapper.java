/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PasswordAuthenticationWrapper {
    private Optional<PasswordAuthentication> passwordAuthentication = Optional.empty();
    private static final Pattern userInfoPattern = Pattern.compile("([^:@/]*):([^:@]*)");

    static final String FIRST_LOGIN_MESSAGE = "Please enter your GIFT-Cloud login details.";
    static final String ERROR_LOGIN_MESSAGE = "Incorrect username or password. Please try again.";

    void set(final PasswordAuthentication authenticator) {
        passwordAuthentication = Optional.of(authenticator);
    }

    Optional<PasswordAuthentication> get() {
            return passwordAuthentication;
    }

    boolean isValid() {
        return passwordAuthentication.isPresent();
    }

    static Optional<PasswordAuthentication> getPasswordAuthenticationFromURL(final URL url) {
        final String userInfo = url.getUserInfo();
        if (null != userInfo) {
            final Matcher m = userInfoPattern.matcher(userInfo);
            if (m.matches()) {
                final PasswordAuthentication authenticator = new PasswordAuthentication(m.group(1), m.group(2).toCharArray());
                return Optional.of(authenticator);
            }
        }
        return Optional.empty();
    }

    static Optional<PasswordAuthentication> getPasswordAuthenticationFromUsernamePassword(final Optional<String> userName, final Optional<char[]> password) {
        if (userName.isPresent() && password.isPresent() && StringUtils.isNotBlank(userName.get()) && ArrayUtils.isNotEmpty(password.get())) {
            return Optional.of(new PasswordAuthentication(userName.get(), password.get()));
        } else {
            return Optional.empty();
        }
    }

}