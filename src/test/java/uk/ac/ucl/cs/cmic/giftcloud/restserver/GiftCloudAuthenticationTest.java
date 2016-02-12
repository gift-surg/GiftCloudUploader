package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.ConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionBuilder;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionWrapper;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.ByteArrayInputStream;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CancellationException;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class GiftCloudAuthenticationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testTryAuthentication() throws Exception {

        // This test ensures that tryAuthentication() will authenticate the XNAT connection once and only once

        final String urlString = "http://UrlOne";
        final String cookieString = "CookieOne";

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(Optional.of("WrongUserName"));
        when(giftCloudProperties.getLastPassword()).thenReturn(Optional.of("WrongPassword".toCharArray()));

        final GiftCloudLoginDialog loginDialog = mock(GiftCloudLoginDialog.class);
        final String userName = "WrongUserName";
        final String password = "WrongPassword";
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.FIRST_LOGIN_MESSAGE);
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.ERROR_LOGIN_MESSAGE);

        final GiftCloudReporterFromApplication reporter = mock(GiftCloudReporterFromApplication.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(urlString, connectionFactory, loginDialog, giftCloudProperties, reporter);

        {
            // An authorisation failure should throw an exception
            when(connectionWrapper.getResponseCode()).thenReturn(HTTP_UNAUTHORIZED);
            try {
                authentication.tryAuthentication();
                Assert.fail();
            } catch (AuthorisationFailureException e) {        }
        }

        {
            // This should be a successful login
            when(connectionWrapper.getResponseCode()).thenReturn(HTTP_OK);
            authentication.tryAuthentication();
        }

        {
            // Another authorisation failure should not cause a problem as we are already authorised
            when(connectionWrapper.getResponseCode()).thenReturn(HTTP_UNAUTHORIZED);
            authentication.tryAuthentication();
        }
    }

    @Test
    public void testCookieAuthentication() throws Exception {

        // This test tries out the different authentication methods

        final String urlString = "http://UrlOne.org";
        final String cookieString = "CookieOne";
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastPassword()).thenReturn(emptyOptionalArray);

        final GiftCloudLoginDialog loginDialog = mock(GiftCloudLoginDialog.class);
        final String userName = "WrongUserName";
        final String password = "WrongPassword";
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.FIRST_LOGIN_MESSAGE);
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.ERROR_LOGIN_MESSAGE);

        final GiftCloudReporterFromApplication reporter = mock(GiftCloudReporterFromApplication.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(urlString, connectionFactory, loginDialog, giftCloudProperties, reporter);

        {
            // Login with cookie string
            when(connectionWrapper.getResponseCode()).thenReturn(HTTP_OK);
            authentication.forceAuthentication(false);
        }

    }

    @Test
    public void testAuthenticationFailure() throws Exception {

        // This test tries out the different authentication methods

        final String urlString = "http://UrlOne.org";
        final String cookieString = "CookieOne";

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getLastUserName()).thenReturn(Optional.of("WrongUserName"));
        when(giftCloudProperties.getLastPassword()).thenReturn(Optional.of("WrongPassword".toCharArray()));
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));

        final GiftCloudLoginDialog loginDialog = mock(GiftCloudLoginDialog.class);
        final String userName = "WrongUserName";
        final String password = "WrongPassword";
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.FIRST_LOGIN_MESSAGE);
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.ERROR_LOGIN_MESSAGE);


        final GiftCloudReporterFromApplication reporter = mock(GiftCloudReporterFromApplication.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(urlString, connectionFactory, loginDialog, giftCloudProperties, reporter);

        {
            // An authorisation failure should throw an exception
            when(connectionWrapper.getResponseCode()).thenReturn(HTTP_UNAUTHORIZED);
            try {
                authentication.forceAuthentication(false);
                Assert.fail();
            } catch (AuthorisationFailureException e) {        }
        }
    }

    @Test
    public void testUsernamePasswordAuthentication() throws Exception {

        // Test that forceAuthentication() works when using a username and password specified in the preferences

        final String urlString = "http://UrlOne.org";
        final String cookieString = "CookieOne";
        final Optional<String> emptyOptional = Optional.empty();
        final String userName = "UserName";
        final String password = "Password";

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastUserName()).thenReturn(Optional.of(userName));
        when(giftCloudProperties.getLastPassword()).thenReturn(Optional.of(password.toCharArray()));

        final GiftCloudLoginDialog loginDialog = mock(GiftCloudLoginDialog.class);
        final String userNameEntry = "WrongUserName";
        final String passwordEntry = "WrongPassword";
        doReturn(new PasswordAuthentication(userNameEntry, passwordEntry.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.FIRST_LOGIN_MESSAGE);
        doReturn(new PasswordAuthentication(userNameEntry, passwordEntry.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.ERROR_LOGIN_MESSAGE);

        final GiftCloudReporterFromApplication reporter = mock(GiftCloudReporterFromApplication.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(urlString, connectionFactory, loginDialog, giftCloudProperties, reporter);

        // Login with username and password
        when(connectionWrapper.getResponseCode()).thenReturn(HTTP_OK);
        authentication.forceAuthentication(false);
    }

    @Test
    public void testResetCancellation() throws Exception {

        final String urlString = "http://UrlOne";
        final String cookieString = "CookieOne";

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(Optional.of("WrongUserName"));
        when(giftCloudProperties.getLastPassword()).thenReturn(Optional.of("WrongUserName".toCharArray()));

        final GiftCloudReporterFromApplication reporter = mock(GiftCloudReporterFromApplication.class);

        final GiftCloudLoginDialog loginDialog = mock(GiftCloudLoginDialog.class);
        final String userName = "WrongUserName";
        final String password = "WrongPassword";
        doReturn(null).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.FIRST_LOGIN_MESSAGE);
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.ERROR_LOGIN_MESSAGE);


        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(urlString, connectionFactory, loginDialog, giftCloudProperties, reporter);

        // The authenticator has been set to cancel, and no cookie is valid, so an authorisation failure exception should be thrown
        when(connectionWrapper.getResponseCode()).thenReturn(HTTP_UNAUTHORIZED);
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }

        // User is asked for login details, and if they cancel they are not asked again
        verify(loginDialog, times(1)).getPasswordAuthentication("Please enter your GIFT-Cloud login details.");

        // Try authentication again, without resetting the user cancellation
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }
        // User is not asked for login details again if they cancelled last time
        verify(loginDialog, times(1)).getPasswordAuthentication("Please enter your GIFT-Cloud login details.");

        // Reset the cancellation and try login again. This should increase the authentication count
        authentication.resetCancellation();
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }
        // User asked for login again after cancellation was reset
        verify(loginDialog, times(2)).getPasswordAuthentication("Please enter your GIFT-Cloud login details.");

        // Try authentication again, without resetting the user cancellation
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }

        // User is not asked for login details again if they cancelled last time
        verify(loginDialog, times(2)).getPasswordAuthentication("Please enter your GIFT-Cloud login details.");
    }

    @Test
    public void testGetAuthenticatedConnectionFactory() throws Exception {

        // This test is about ensuring that the connection factory we get back has the cookie set correctly

        final String urlString = "http://UrlOne";
        final String cookieString = "CookieOne";
        final String jSessionIDString = "JSESSIONID=" + cookieString;
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastPassword()).thenReturn(emptyOptionalArray);

        final GiftCloudLoginDialog loginDialog = mock(GiftCloudLoginDialog.class);
        final String userName = "WrongUserName";
        final String password = "WrongPassword";
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.FIRST_LOGIN_MESSAGE);
        doReturn(new PasswordAuthentication(userName, password.toCharArray())).when(loginDialog).getPasswordAuthentication(PasswordAuthenticationWrapper.ERROR_LOGIN_MESSAGE);

        final GiftCloudReporterFromApplication reporter = mock(GiftCloudReporterFromApplication.class);
        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(urlString, connectionFactory, loginDialog, giftCloudProperties, reporter);
        final HttpConnectionBuilder connectionBuilder = mock(HttpConnectionBuilder.class);

        final ConnectionFactory authenticatedConnectionFactory = authentication.getAuthenticatedConnectionFactory();
        authenticatedConnectionFactory.createConnection(urlString, connectionBuilder);

        // Test that the cookie string has been set in the connection builder
        verify(connectionBuilder, times(1)).setCookie(jSessionIDString);
    }
}