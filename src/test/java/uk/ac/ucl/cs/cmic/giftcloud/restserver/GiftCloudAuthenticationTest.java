package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporter;

import java.io.ByteArrayInputStream;
import java.net.Authenticator;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.mockito.Mockito.*;

public class GiftCloudAuthenticationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testTryAuthentication() throws Exception {

        // This test ensures that tryAuthentication() will authenticate the XNAT connection once and only once

        final String urlString = "http://UrlOne";
        final URL url = new URL(urlString);
        final String cookieString = "CookieOne";
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.getBaseUrl()).thenReturn(url);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastPassword()).thenReturn(emptyOptionalArray);

        final String wrongUserName = "WrongUserName";
        final String wrongPassword = "WrongPassword";
        final Authenticator authenticator = new FakeLoginAuthenticator(wrongUserName, wrongPassword.toCharArray(), false);

        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(connectionFactory, giftCloudProperties, authenticator, reporter);

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
        final URL url = new URL(urlString);
        final String cookieString = "CookieOne";
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.getBaseUrl()).thenReturn(url);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastPassword()).thenReturn(emptyOptionalArray);

        final String userName = "UserName";
        final String password = "Password";

        final String wrongUserName = "WrongUserName";
        final String wrongPassword = "WrongPassword";
        final Authenticator authenticator = new FakeLoginAuthenticator(wrongUserName, wrongPassword.toCharArray(), false);

        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(connectionFactory, giftCloudProperties, authenticator, reporter);

        {
            // Login with cookie string
            when(connectionWrapper.getResponseCode()).thenReturn(HTTP_OK);
            authentication.forceAuthentication();
        }

    }

    @Test
    public void testAuthenticationFailure() throws Exception {

        // This test tries out the different authentication methods

        final String urlString = "http://UrlOne.org";
        final URL url = new URL(urlString);
        final String cookieString = "CookieOne";
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.getBaseUrl()).thenReturn(url);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getLastUserName()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastPassword()).thenReturn(emptyOptionalArray);
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));

        final String wrongUserName = "WrongUserName";
        final String wrongPassword = "WrongPassword";
        final Authenticator authenticator = new FakeLoginAuthenticator(wrongUserName, wrongPassword.toCharArray(), false);

        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(connectionFactory, giftCloudProperties, authenticator, reporter);

        {
            // An authorisation failure should throw an exception
            when(connectionWrapper.getResponseCode()).thenReturn(HTTP_UNAUTHORIZED);
            try {
                authentication.forceAuthentication();
                Assert.fail();
            } catch (AuthorisationFailureException e) {        }
        }
    }

    @Test
    public void testUsernamePasswordAuthentication() throws Exception {

        // Test that forceAuthentication() works when using a username and password specified in the preferences

        final String urlString = "http://UrlOne.org";
        final URL url = new URL(urlString);
        final String cookieString = "CookieOne";
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();
        final String userName = "UserName";
        final String password = "Password";

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.getBaseUrl()).thenReturn(url);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastUserName()).thenReturn(Optional.of(userName));
        when(giftCloudProperties.getLastPassword()).thenReturn(Optional.of(password.toCharArray()));

        // In this test we are testing the username and password set in the properties, not those typed in by the user
        final Authenticator authenticator = new FakeLoginAuthenticator(null, null, true);

        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(connectionFactory, giftCloudProperties, authenticator, reporter);

        // Login with username and password
        when(connectionWrapper.getResponseCode()).thenReturn(HTTP_OK);
        authentication.forceAuthentication();
    }

    @Test
    public void testResetCancellation() throws Exception {

        final String urlString = "http://UrlOne";
        final URL url = new URL(urlString);
        final String cookieString = "CookieOne";
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();

        final HttpConnectionWrapper connectionWrapper = mock(HttpConnectionWrapper.class);
        when(connectionWrapper.getInputStream()).thenReturn(new ByteArrayInputStream(cookieString.getBytes(StandardCharsets.UTF_8)));

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.getBaseUrl()).thenReturn(url);
        when(connectionFactory.createConnection(anyString(), any(HttpConnectionBuilder.class))).thenReturn(connectionWrapper);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastPassword()).thenReturn(emptyOptionalArray);

        // Set the authentication to cancel
        final String wrongUserName = "WrongUserName";
        final String wrongPassword = "WrongPassword";
        final FakeLoginAuthenticator authenticator = new FakeLoginAuthenticator(wrongUserName, wrongPassword.toCharArray(), true);

        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);

        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(connectionFactory, giftCloudProperties, authenticator, reporter);

        // The authenticator has been set to cancel, and no cookie is valid, so an authorisation failure exception should be thrown
        when(connectionWrapper.getResponseCode()).thenReturn(HTTP_UNAUTHORIZED);
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }

        Assert.assertEquals(authenticator.getAuthenticationCount(), 1);

        // Try authentication again, without resetting the user cancellation
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }
        Assert.assertEquals(authenticator.getAuthenticationCount(), 1);

        // Reset the cancellation and try login again. This should increase the authentication count
        authentication.resetCancellation();
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }
        Assert.assertEquals(authenticator.getAuthenticationCount(), 2);

        // Try authentication again, without resetting the user cancellation
        try {
            authentication.tryAuthentication();
            Assert.fail();
        } catch (CancellationException e) {        }
        Assert.assertEquals(authenticator.getAuthenticationCount(), 2);
    }

    @Test
    public void testGetAuthenticatedConnectionFactory() throws Exception {

        // This test is about ensuring that the connection factory we get back has the cookie set correctly

        final String urlString = "http://UrlOne";
        final URL url = new URL(urlString);
        final String cookieString = "CookieOne";
        final String jSessionIDString = "JSESSIONID=" + cookieString;
        final Optional<String> emptyOptional = Optional.empty();
        final Optional<char[]> emptyOptionalArray = Optional.empty();

        final HttpConnectionFactory connectionFactory = mock(HttpConnectionFactory.class);
        when(connectionFactory.getBaseUrl()).thenReturn(url);

        final GiftCloudProperties giftCloudProperties = mock(GiftCloudProperties.class);
        when(giftCloudProperties.getUserAgentString()).thenReturn("TestUserAgent");
        when(giftCloudProperties.getSessionCookie()).thenReturn(Optional.of(cookieString));
        when(giftCloudProperties.getLastUserName()).thenReturn(emptyOptional);
        when(giftCloudProperties.getLastPassword()).thenReturn(emptyOptionalArray);

        final Authenticator authenticator = mock(Authenticator.class);

        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);
        final GiftCloudAuthentication authentication = new GiftCloudAuthentication(connectionFactory, giftCloudProperties, authenticator, reporter);
        final HttpConnectionBuilder connectionBuilder = mock(HttpConnectionBuilder.class);

        final ConnectionFactory authenticatedConnectionFactory = authentication.getAuthenticatedConnectionFactory();
        authenticatedConnectionFactory.createConnection(urlString, connectionBuilder);

        // Test that the cookie string has been set in the connection builder
        verify(connectionBuilder, times(1)).setCookie(jSessionIDString);
    }
}