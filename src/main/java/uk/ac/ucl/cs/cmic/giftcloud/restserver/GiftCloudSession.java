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
import java.net.*;
import java.util.Optional;
import java.util.concurrent.CancellationException;

class GiftCloudSession {

    private final JSessionIdCookieWrapper cookieWrapper;
    private final GiftCloudProperties giftCloudProperties;
    private final URL baseUrl;
    private final HttpConnectionFactory connectionFactory;
    private final MultiUploadReporter reporter;
    private boolean successfulAuthentication = false;
    private final int MAX_NUM_LOGIN_ATTEMPTS = 3;
    private final PasswordAuthenticationWrapper passwordAuthenticationWrapper = new PasswordAuthenticationWrapper();
    private boolean userCancelled = false;

    private Object synchronizationLock = new Object();


    GiftCloudSession(final GiftCloudProperties giftCloudProperties, final HttpConnectionFactory connectionFactory, final MultiUploadReporter reporter) throws MalformedURLException {
        this.giftCloudProperties = giftCloudProperties;
        this.connectionFactory = connectionFactory;
        baseUrl = new URL(connectionFactory.getBaseUrl());
        this.reporter = reporter;
        this.cookieWrapper = new JSessionIdCookieWrapper(giftCloudProperties.getSessionCookie());

        HttpURLConnection.setFollowRedirects(false);

        Optional<PasswordAuthentication> passwordAuthenticationFromUrl = PasswordAuthenticationWrapper.getPasswordAuthenticationFromURL(baseUrl);

        // Check the URL for a username and password. If it is present then set this as the default authentication
        if (passwordAuthenticationFromUrl.isPresent()) {
            passwordAuthenticationWrapper.set(passwordAuthenticationFromUrl.get());

        } else {
            // Otherwise check if a username/password has been specified through the properties
            final Optional<PasswordAuthentication> passwordAuthenticationFromUserPassword = PasswordAuthenticationWrapper.getPasswordAuthenticationFromUsernamePassword(giftCloudProperties.getLastUserName(), giftCloudProperties.getLastPassword());
            if (passwordAuthenticationFromUserPassword.isPresent()) {
                // If both a username and password are available, then construct an authenticator using these
                passwordAuthenticationWrapper.set(passwordAuthenticationFromUserPassword.get());
            }
        }

        // We set the authenticator that will be used to request login to a dialog
        Authenticator.setDefault(new GiftCloudLoginAuthenticator(reporter.getContainer(), giftCloudProperties));
    }

    <T> T request(final HttpRequest request) throws IOException {

        synchronized (synchronizationLock) {

            // First, set up an authenticated session if one has not already been established.
            // This will attempt to connect using the existing cookieWrapper and user credentials.
            // If these do not exist or fail, then the user will be prompted for a user name and password, up to the number of times set in MAX_NUM_LOGIN_ATTEMPTS
            tryAuthentication();

            try {
                return (T) request.getResponse(new ConnectionFactoryWithCookie(connectionFactory, cookieWrapper));

            } catch (AuthorisationFailureException exception) {

                // In the event of an authorisation failure, give the user another opportunity to enter a username and password (multiple times) to establish a new session
                forceAuthentication();

                // Then try and connect again. We allow any further AuthorisationFailureException to fall through
                return (T) request.getResponse(new ConnectionFactoryWithCookie(connectionFactory, cookieWrapper));

            }
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
        synchronized (synchronizationLock) {
            if (!successfulAuthentication) {
                forceAuthentication();
                successfulAuthentication = true;
            }
        }
    }

    // In the event that the user cancels authentication
    void resetCancellation() {
        synchronized (synchronizationLock) {
            userCancelled = false;
        }
    }


    private void forceAuthentication() throws IOException {

        Optional<String> cookieString = Optional.empty();

        // First we attempt to log in using the existing cookie
        if (cookieWrapper.isValid()) {
            cookieString = tryAuthenticatedLogin(new ConnectionFactoryWithCookie(connectionFactory, cookieWrapper), 0);
        }

        // If this fails, then attempt to log in using a specified username and password
        if (!cookieString.isPresent() && passwordAuthenticationWrapper.isValid()) {
            cookieString = tryAuthenticatedLogin(new ConnectionFactoryWithPasswordAuthentication(connectionFactory, passwordAuthenticationWrapper.get().get()), 0);
        }

        // Otherwise we ask for a username and password
        int number_of_login_attempts = 0;
        while (!cookieString.isPresent()) {
            number_of_login_attempts++;

            // If the user has already cancelled a login dialog then we automatically cancel this one. This is because, due to the use of futures,
            // some threads making rest calls may already have started before the user cancelled the thread
            if (userCancelled) {
                throw new CancellationException("User cancelled login to GIFT-Cloud");
            }

            Optional<PasswordAuthentication> passwordAuthentication = PasswordAuthenticationWrapper.askPasswordAuthenticationFromUser(baseUrl, number_of_login_attempts > 1);

            // If the user cancels the login, we suspend all future login dialogs until resetCancellation() is called
            if (!passwordAuthentication.isPresent()) {
                userCancelled = true;
                throw new CancellationException("User cancelled login to GIFT-Cloud");
            }

            cookieString = tryAuthenticatedLogin(new ConnectionFactoryWithPasswordAuthentication(connectionFactory, passwordAuthentication.get()), number_of_login_attempts);

            // If this succeeds then store the password authentication for future use
            if (cookieString.isPresent()) {
                passwordAuthenticationWrapper.set(passwordAuthentication.get());
            }
        }

        // If we have arrived here, we now have a valid cookie
        cookieWrapper.replaceCookie(cookieString.get());
    }

    private Optional<String> tryAuthenticatedLogin(final ConnectionFactory connectionFactory, final int attemptNumber) throws IOException {
        try {
            return Optional.of(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.POST, "/data/JSESSION", new HttpStringResponseProcessor()).getResponse(connectionFactory));
        } catch (AuthorisationFailureException e) {
            if (attemptNumber >= MAX_NUM_LOGIN_ATTEMPTS) {
                throw e;
            } else {
                return Optional.empty();
            }
        }
    }
}
