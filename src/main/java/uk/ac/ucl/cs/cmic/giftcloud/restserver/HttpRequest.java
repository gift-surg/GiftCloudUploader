/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import static java.net.HttpURLConnection.*;


/**
 * Base class for all HTTP requests to the GIFT-Cloud REST server
 *
 * All communication with the GIFT-Cloud server should be using objects derived from this type. This ensures proper
 * error handling and automated login after a connection has been lost.
 *
 * @param <T> type of the response that will be returned to the caller if the request succeeds. This is the type
 *           returned by the response processor after processing the server's reply
 */
abstract class HttpRequest<T> {

    private final HttpConnectionWrapper.ConnectionType connectionType;
    protected final String relativeUrlString;

    // The response value could be null. As you can't store a null value in an Optional, we use an Optional of Optional.
    // The outer Optional determines if a response has been set. The inner Optional determines whether this response is null or a value
    private Optional<Optional<T>> response = Optional.empty();
    private final HttpResponseProcessor<T> responseProcessor;
    private final GiftCloudReporter reporter;
    private final String userAgentString;
    private final int shortTimeout;
    private final int longTimeout;

    /**
     * Create a new request object that will connect to the given URL, and whose server reply will be interpreted by the response processor
     *
     * @param connectionType whether this request call is GET, POST, PUT
     * @param relativeUrlString the relative URL of the resource being referred to by the request call (i.e. excluding the server URL)
     * @param responseProcessor the object that will process the server's reply and produce an output of the parameterised type T
     * @param httpProperties contains configurable settings for the Http connection
     * @param reporter an object for reporting errors and warnings back to the user and/or program logs
     */
    HttpRequest(final HttpConnectionWrapper.ConnectionType connectionType, final String relativeUrlString, final HttpResponseProcessor<T> responseProcessor, HttpProperties httpProperties, final GiftCloudReporter reporter) {
        this.connectionType = connectionType;
        this.relativeUrlString = relativeUrlString;
        this.responseProcessor = responseProcessor;
        this.reporter = reporter;
        userAgentString = httpProperties.getUserAgentString();
        shortTimeout = httpProperties.getShortTimeout();
        longTimeout = httpProperties.getLongTimeout();
    }

    /**
     * Executes the request and processes the server response to produce an output of the parameterised type T
     *
     * @param connectionFactory used to construct the HTTP connection object
     * @return the result object computed by the response processor based on the response from the server
     * @throws IOException if an error occurs during the server communication
     */
    final T getResponse(final String baseUrlString, final ConnectionFactory connectionFactory, final boolean rapidTimeout) throws IOException {
        if (!response.isPresent()) {
            doRequest(baseUrlString, connectionFactory, rapidTimeout);
        }
        // The value of the response should now be set to an Optional - if this inner optional is not set, that indicates a null value
        final Optional<T> responseResult = response.get();
        final T returnValue = responseResult.orElse(null);
        return returnValue;
    }

    /**
     * Set the parameters for the connection. A subclass may wish to override this, but should call the base class
     *
     * @param connectionBuilder a builder object used to set the connection parameters in advance of creating the connection
     * @throws IOException not thrown by the base class but might be thrown by subclasses
     */
    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException
    {
        // Add version
        connectionBuilder.setUserAgent(userAgentString);

        // Accept all media types
        connectionBuilder.setAccept("*/*");

        // Set the type of request
        connectionBuilder.setConnectionType(connectionType);
    }

    /**
     * Subclasses use this method to write to the connection's output stream (if it exists) before performing the request
     *
     * @param connectionWrapper the connection interface to be used to write to the output stream
     * @throws IOException may be thrown by the implementing methods during writing to the output stream
     */
    abstract protected void processOutputStream(final HttpConnection connectionWrapper) throws IOException;

    private void doRequest(final String baseUrlString, final ConnectionFactory connectionFactory, final boolean rapidTimeout) throws IOException {

        final HttpConnectionBuilder connectionBuilder = new HttpConnectionBuilder(relativeUrlString);

        prepareConnection(connectionBuilder);

        connectionBuilder.setConnectTimeout(rapidTimeout ? shortTimeout : longTimeout);

        // Build the connection
        final String fullUrl = HttpRequest.getFullUrl(baseUrlString, relativeUrlString);
        final HttpConnection connection = connectionFactory.createConnection(fullUrl, connectionBuilder);

        // Send data to the connection if required
        processOutputStream(connection);

        try {
            // Explicitly initiate the connection
            connection.connect();

            try {
                throwIfBadResponse(connection);

                // Get data from the connection and process. In the case of an error, this will process the error stream
                response = Optional.of(Optional.ofNullable(responseProcessor.processInputStream(connection)));

            } finally {
                connection.disconnect();
            }

        } catch (IOException e) {
            if (e instanceof ConnectException) {
                throw new GiftCloudException(GiftCloudUploaderError.SERVER_INVALID);
            }
            if (e instanceof SocketTimeoutException) {
                throw new GiftCloudException(GiftCloudUploaderError.SERVER_INVALID);
            }
            if (e instanceof UnknownHostException) {
                throw new GiftCloudException(GiftCloudUploaderError.SERVER_INVALID);
            }
            if (e.getCause() instanceof sun.security.validator.ValidatorException) {
                throw new GiftCloudException(GiftCloudUploaderError.SERVER_CERTIFICATE_FAILURE);
            }
            if (!(e instanceof GiftCloudHttpException && ((GiftCloudHttpException)e).getResponseCode() == HTTP_NOT_FOUND)) {
                reporter.silentLogException(e, "An error occurred while processing request " + connection.getUrlString());
            }
            throwIfBadResponse(connection);
            throw e;
        }
    }

    private void throwIfBadResponse(final HttpConnection connection) throws IOException {

        final String urlString = connection.getUrlString();
        final URL url = connection.getURL();
        final int responseCode = connection.getResponseCode();
        final ErrorDetails errorDetails = HttpRequestErrorMessages.getResponseMessage(responseCode, urlString, url);

        switch (responseCode) {
            case HTTP_ACCEPTED:
            case HTTP_NOT_AUTHORITATIVE:
            case HTTP_NO_CONTENT:
            case HTTP_RESET:
            case HTTP_PARTIAL:
            case HTTP_MOVED_PERM:
                reporter.silentWarning(errorDetails.getTitle() + ":" + errorDetails.getHtmlText() + " Details: request method " + connection.getRequestMethod() + " to URL " + urlString + " returned " + responseCode + " with message " + connection.getResponseMessage());
                return;

            case HTTP_OK:
            case HTTP_CREATED:
                return;

            // Handle 302, at least temporarily: Spring auth redirects to login page,
            // so assume that's what's happened when we see a redirect at this point.
            case HTTP_MOVED_TEMP:
            case HTTP_UNAUTHORIZED:
                reporter.silentWarning(errorDetails.getTitle() + ":" + errorDetails.getHtmlText() + " Details: request method " + connection.getRequestMethod() + " to URL " + urlString + " returned " + responseCode + " with message " + connection.getResponseMessage());
                throw new AuthorisationFailureException(responseCode, url);

            case HTTP_BAD_REQUEST:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), GiftCloudUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_CONFLICT:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), GiftCloudUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_INTERNAL_ERROR:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), GiftCloudUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_NOT_FOUND:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), GiftCloudUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            default:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), GiftCloudUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());
        }
    }

    /** Combine the base URL with a relative path, while ensuring there is exactly one / character between them
     * @param baseUrlString the URI of the server
     * @param relativePath the location relative to the base URI
     * @return the full URI string
     */
    private static final String getFullUrl(final String baseUrlString, final String relativePath) {
        final StringBuilder sb = new StringBuilder(baseUrlString);

        // Remove any trailing '/' characters from the base URL
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '/') {
            sb.deleteCharAt(sb.length() - 1);
        }

        // Now add a single '/' only if one isn't already present in the appended relative path
        if ('/' != relativePath.charAt(0)) {
            sb.append('/');
        }
        sb.append(relativePath);

        return sb.toString();
    }

}


