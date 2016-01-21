package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.CancellationException;

/**
 * This class manages authentication with the XNAT server, which is performed automatically where necessary before a rest server call.
 * Authentication details can be provided in several ways:
 *   - a JSessionID from the properties (for example, this will be provided to the uploading applet via parameters)
 *   - a username and password from the properties (for example, the previous login on this computer)
 *   - a username and password specified as part of the URL of the GIFT-Cloud server
 *   - a dialog displayed to the user to request username and password should the other authentication methods be unavailable or fail
 *
 * This class is typically instantiated once for a given server URL.
 * This class should be threadsafe, provided that the instances provided to the constructor are themselves threadsafe
 */
class GiftCloudAuthentication {
    private static final int MAX_NUM_LOGIN_ATTEMPTS = 3;
    private String baseUrlString;
    private final ConnectionFactory connectionFactory;
    private GiftCloudLoginDialog loginDialog;
    private GiftCloudProperties giftCloudProperties;
    private GiftCloudReporter reporter;
    private final JSessionIdCookieWrapper cookieWrapper;
    private final URL baseUrl;
    private boolean successfulAuthentication = false;
    private final PasswordAuthenticationWrapper passwordAuthenticationWrapper = new PasswordAuthenticationWrapper();
    private boolean userCancelled = false;

    /**
     * Creates a new instance of this class bound to the provided HttpConnectionFactory (and thus URL)
     *
     * @param connectionFactory used to create HttpConnectionWrapper objects for making server rest calls
     * @param giftCloudProperties used to get the session cookie and to get and set the username and password for the last successful login
     * @param reporter used to get the container for the user login dialog
     */
    GiftCloudAuthentication(final String baseUrlString, final ConnectionFactory connectionFactory, final GiftCloudLoginDialog loginDialog, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) throws MalformedURLException {
        this.baseUrlString = baseUrlString;
        this.connectionFactory = connectionFactory;
        this.loginDialog = loginDialog;
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;
        this.cookieWrapper = new JSessionIdCookieWrapper(giftCloudProperties.getSessionCookie());
        this.baseUrl = new URL(baseUrlString);
        Optional<GiftCloudLoginAuthenticator> defaultAuthenticator= Optional.empty();

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
                defaultAuthenticator = Optional.of(new GiftCloudLoginAuthenticator(passwordAuthenticationWrapper));
            }
        }

        // If a password exists then we use this to supply the default authenticator; otherwise we disable default authentication which will force a failure, allowing other authentication methods to be attempted
        Authenticator.setDefault(defaultAuthenticator.orElse(null));
    }

    /**
     * Authenticates with the GIFT-Cloud server, unless authentication has successfully been performed
     *
     * @throws IOException if a communications error occurred, or if the user exceeded the maximum number of incorrect login attempts
     */
    synchronized void tryAuthentication() throws IOException {
        if (!successfulAuthentication) {
            forceAuthentication(true);
            successfulAuthentication = true;
        }
    }

    /**
     * Authenticates with the GIFT-Cloud server, unless authentication has successfully been performed
     *
     * @throws IOException if a communications error occurred, or if the user exceeded the maximum number of incorrect login attempts
     */
    synchronized void forceAuthentication(final boolean rapidTimeout) throws IOException {

        Optional<String> cookieString = Optional.empty();

        // First we attempt to log in using the existing cookie
        if (cookieWrapper.isValid()) {
            cookieString = tryAuthenticatedLogin(new ConnectionFactoryWithCookie(connectionFactory, cookieWrapper), 0, rapidTimeout);
        }

        // If this fails, then attempt to log in using a specified username and password
        if (!cookieString.isPresent() && passwordAuthenticationWrapper.isValid()) {
            cookieString = tryAuthenticatedLogin(new ConnectionFactoryWithPasswordAuthentication(connectionFactory, passwordAuthenticationWrapper.get().get()), 0, rapidTimeout);
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

            final String prompt = number_of_login_attempts > 1 ? PasswordAuthenticationWrapper.ERROR_LOGIN_MESSAGE : PasswordAuthenticationWrapper.FIRST_LOGIN_MESSAGE;
            Optional<PasswordAuthentication> passwordAuthentication = Optional.ofNullable(loginDialog.getPasswordAuthentication(prompt));

            // If the user cancels the login, we suspend all future login dialogs until resetCancellation() is called
            if (!passwordAuthentication.isPresent()) {
                userCancelled = true;
                throw new CancellationException("User cancelled login to GIFT-Cloud");
            }

            cookieString = tryAuthenticatedLogin(new ConnectionFactoryWithPasswordAuthentication(connectionFactory, passwordAuthentication.get()), number_of_login_attempts, rapidTimeout);

            // If this succeeds then store the password authentication for future use
            if (cookieString.isPresent()) {
                passwordAuthenticationWrapper.set(passwordAuthentication.get());
            }
        }

        // If we have arrived here, we now have a valid cookie
        cookieWrapper.replaceCookie(cookieString.get());
    }

    /**
     * Disable automatic cancellation of user login dialogs.
     * When a user cancels a login attempt, future login dialogs are cancelled automatically. If this were not the case,
     * the user would be presented with a login dialog for every thread.
     * We need to reset this automatic cancellation when the user initiates a new action
     */
    synchronized void resetCancellation() {
        userCancelled = false;
    }

    /**
     * Returns a connection factory that will automatically add the session cookie to the connection it creates
     * @return ConnectionFactoryWithCookie
     */
    synchronized ConnectionFactory getAuthenticatedConnectionFactory() {
        return new ConnectionFactoryWithCookie(connectionFactory, cookieWrapper);
    }

    private Optional<String> tryAuthenticatedLogin(final ConnectionFactory connectionFactory, final int attemptNumber, final boolean rapidTimeout) throws IOException {
        try {
            return Optional.of(new HttpRequestWithoutOutput<String>(HttpConnection.ConnectionType.POST, "/data/JSESSION", new HttpStringResponseProcessor(), giftCloudProperties, reporter).getResponse(baseUrlString, connectionFactory, rapidTimeout));
        } catch (AuthorisationFailureException e) {
            if (attemptNumber >= MAX_NUM_LOGIN_ATTEMPTS) {
                throw e;
            } else {
                return Optional.empty();
            }
        }
    }
}