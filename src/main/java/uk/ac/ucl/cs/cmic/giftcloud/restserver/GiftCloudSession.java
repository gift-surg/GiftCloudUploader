/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;

class GiftCloudSession {

    private final GiftCloudAuthentication giftCloudAuthentication;
    private String baseUrlString;


    GiftCloudSession(final String baseUrlString, final GiftCloudProperties giftCloudProperties, final ConnectionFactory connectionFactory, final GiftCloudReporter reporter) throws MalformedURLException {
        this.baseUrlString = baseUrlString;
        giftCloudAuthentication = new GiftCloudAuthentication(baseUrlString, connectionFactory, giftCloudProperties, new GiftCloudLoginAuthenticator(reporter.getContainer(), giftCloudProperties), reporter);
    }

    <T> T request(final HttpRequest<T> request) throws IOException {
        // First, set up an authenticated session if one has not already been established.
        // This will attempt to connect using the existing cookieWrapper and user credentials.
        // If these do not exist or fail, then the user will be prompted for a user name and password, up to the number of times set in MAX_NUM_LOGIN_ATTEMPTS
        giftCloudAuthentication.tryAuthentication();

        try {
            return request.getResponse(baseUrlString, giftCloudAuthentication.getAuthenticatedConnectionFactory());

        } catch (AuthorisationFailureException exception) {

            // In the event of an authorisation failure, give the user another opportunity to enter a username and password (multiple times) to establish a new session
            giftCloudAuthentication.forceAuthentication();

            // Then try and connect again. We allow any further AuthorisationFailureException to fall through
            return request.getResponse(baseUrlString, giftCloudAuthentication.getAuthenticatedConnectionFactory());

        }
    }

    Optional<String> requestOptionalString(final HttpRequest<String> request) throws IOException {
        try {
            final String result = request(request);
            return Optional.of(result);
        } catch (GiftCloudHttpException exception) {

            // 404 indicates the requested resource doesn't exist
            if (exception.getResponseCode() == 404) {
                final Optional<String> returnValue = Optional.empty();
                return returnValue;
            } else {
                throw exception;
            }
        }
    }

    Optional<Map<String, String>> requestOptionalMap(final HttpRequest<Map<String, String>> request) throws IOException {
        try {
            final Map<String, String> result = request(request);
            return Optional.of(result);
        } catch (GiftCloudHttpException exception) {

            // 404 indicates the requested resource doesn't exist
            if (exception.getResponseCode() == 404) {
                final Optional<Map<String, String>> returnValue = Optional.empty();
                return returnValue;
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
