/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.IOException;
import java.util.Optional;

class GiftCloudSession {

    private final GiftCloudAuthentication giftCloudAuthentication;


    GiftCloudSession(final GiftCloudProperties giftCloudProperties, final HttpConnectionFactory connectionFactory, final MultiUploadReporter reporter) {
        giftCloudAuthentication = new GiftCloudAuthentication(connectionFactory, giftCloudProperties, new GiftCloudLoginAuthenticator(reporter.getContainer(), giftCloudProperties), reporter);
    }

    <T> T request(final HttpRequest request) throws IOException {
        // First, set up an authenticated session if one has not already been established.
        // This will attempt to connect using the existing cookieWrapper and user credentials.
        // If these do not exist or fail, then the user will be prompted for a user name and password, up to the number of times set in MAX_NUM_LOGIN_ATTEMPTS
        giftCloudAuthentication.tryAuthentication();

        try {
            return (T) request.getResponse(giftCloudAuthentication.getAuthenticatedConnectionFactory());

        } catch (AuthorisationFailureException exception) {

            // In the event of an authorisation failure, give the user another opportunity to enter a username and password (multiple times) to establish a new session
            giftCloudAuthentication.forceAuthentication();

            // Then try and connect again. We allow any further AuthorisationFailureException to fall through
            return (T) request.getResponse(giftCloudAuthentication.getAuthenticatedConnectionFactory());

        }
    }

    <T> Optional<T> requestOptional(final HttpRequest request) throws IOException {
        try {
            final T result = request(request);
            return Optional.of(result);
        } catch (GiftCloudHttpException exception) {

            // 404 indicates the requested resource doesn't exist
            if (exception.getResponseCode() == 404) {
                return Optional.empty();
            } else {
                throw exception;
            }
        }
    }

    void tryAuthentication() throws IOException {
        giftCloudAuthentication.tryAuthentication();
    }

    // In the event that the user cancels authentication
    void resetCancellation() {
        giftCloudAuthentication.resetCancellation();
    }
}
