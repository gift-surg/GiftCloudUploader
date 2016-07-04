/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.request.AuthorisationFailureException;
import uk.ac.ucl.cs.cmic.giftcloud.request.ConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.request.HttpRequest;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Processes HTTP request messages, adding the required server and login information
 */
class GiftCloudSession {

    private final GiftCloudAuthentication giftCloudAuthentication;
    private String baseUrlString;


    GiftCloudSession(final String baseUrlString, final GiftCloudProperties giftCloudProperties, final ConnectionFactory connectionFactory, final UserCallback userCallback, final GiftCloudReporter reporter) throws MalformedURLException {
        this.baseUrlString = baseUrlString;

        // Get the GIFT-Cloud icon - this will return null if not found
        ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftCloud.png"));
        giftCloudAuthentication = new GiftCloudAuthentication(baseUrlString, connectionFactory, userCallback, giftCloudProperties, reporter);
    }

    /**
     * Performs the HTTP request
     *
     * @param request the HTTP request building and response processing object to perform the request
     * @param <T> the expected type of the response after processing
     * @return the response after processing
     * @throws IOException if the request failed for any reason
     */
    <T> T request(final HttpRequest<T> request) throws IOException {
        // First, set up an authenticated session if one has not already been established.
        // This will attempt to connect using the existing cookieWrapper and user credentials.
        // If these do not exist or fail, then the user will be prompted for a user name and password, up to the number of times set in MAX_NUM_LOGIN_ATTEMPTS
        giftCloudAuthentication.tryAuthentication();

        try {
            return request.getResponse(baseUrlString, giftCloudAuthentication.getAuthenticatedConnectionFactory(), false);

        } catch (AuthorisationFailureException exception) {

            // In the event of an authorisation failure, give the user another opportunity to enter a username and password (multiple times) to establish a new session
            giftCloudAuthentication.forceAuthentication(false);

            // Then try and connect again. We allow any further AuthorisationFailureException to fall through
            return request.getResponse(baseUrlString, giftCloudAuthentication.getAuthenticatedConnectionFactory(), false);

        }
    }

    /**
     * Performs a request for which 404 (not found) is a valid outcome. The result is wrapped in an {@link Optional}
     * type reflecting whether the resource was found or not. Assuming the return value from the request is of type T,
     * the return type from this method is {@code Optional<T>}
     *
     * @param request the HTTP request building and response processing object to perform the request
     * @param <T> the expected type to be returned from the server, if the resource exists
     * @return An {@link Optional} containing the response, or {@code Optional.empty()} if the request returned error 404 (not found)
     * @throws IOException if any error occurred other than 404
     */
    <T> Optional<T> requestOptional(final HttpRequest<T> request) throws IOException {
        try {
            final T result = request(request);
            return Optional.of(result);
        } catch (GiftCloudHttpException exception) {

            // 404 indicates the requested resource doesn't exist
            if (exception.getResponseCode() == 404) {
                final Optional<T> returnValue = Optional.empty();
                return returnValue;
            } else {
                throw exception;
            }
        }
    }

    /**
     * Performs a request for which 404 (not found) is a valid outcome, and where the request itself returns an {@Optional} type

     * The difference between this method and requestOptional() is that requestOptional() assumes a return type from
     * the request of type T, whereas requestOptionalFromOptional() assumes the return type is of already of type {@code Optional<T>}.
     *
     * Unlike {@code requestOptional()}, this method does not wrap the result in another Optional It just returns the
     * same {@link Optional}, or an {@code Optional.empty()} if the resource was not found.
     *
     * @param request the HTTP request building and response processing object to perform the request
     * @param <T> the expected type to be returned from the server, if the resource exists
     * @return An {@link Optional} containing the response, or {@link }Optional.empty()} if the request returned error 404 (not found)
     * @throws IOException if any error occurred other than 404
     */
    <T> Optional<T> requestOptionalFromOptional(final HttpRequest<Optional<T>> request) throws IOException {
        try {
            final Optional<T> result = request(request);
            return result;
        } catch (GiftCloudHttpException exception) {

            // 404 indicates the requested resource doesn't exist
            if (exception.getResponseCode() == 404) {
                final Optional<T> returnValue = Optional.empty();
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
