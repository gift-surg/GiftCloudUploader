package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CancellationException;

public class GiftCloudAuthentication {
    private HttpConnectionFactory connectionFactory;
    final JSessionIdCookieWrapper cookieWrapper;
    private final URL baseUrl;
    boolean successfulAuthentication = false;
    static final int MAX_NUM_LOGIN_ATTEMPTS = 3;
    final PasswordAuthenticationWrapper passwordAuthenticationWrapper = new PasswordAuthenticationWrapper();
    boolean userCancelled = false;
    Object synchronizationLock = new Object();

    public GiftCloudAuthentication(final HttpConnectionFactory connectionFactory, final GiftCloudProperties giftCloudProperties, final MultiUploadReporter reporter) throws MalformedURLException {
        this.connectionFactory = connectionFactory;
        this.cookieWrapper = new JSessionIdCookieWrapper(giftCloudProperties.getSessionCookie());
        baseUrl = new URL(connectionFactory.getBaseUrl());

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

    void tryAuthentication() throws IOException {
        synchronized (synchronizationLock) {
            if (!successfulAuthentication) {
                forceAuthentication();
                successfulAuthentication = true;
            }
        }
    }

    void forceAuthentication() throws IOException {

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

    static Optional<String> tryAuthenticatedLogin(final ConnectionFactory connectionFactory, final int attemptNumber) throws IOException {
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

    // In the event that the user cancels authentication
    void resetCancellation() {
        synchronized (synchronizationLock) {
            userCancelled = false;
        }
    }

    ConnectionFactoryWithCookie getAuthenticatedConnectionFactory() {
        return new ConnectionFactoryWithCookie(connectionFactory, cookieWrapper);
    }
}