/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import static java.net.HttpURLConnection.*;


abstract class HttpRequest<T> {

    private HttpConnectionWrapper.ConnectionType connectionType;
    protected final String urlString;

    // The response value could be null. As you can't store a null value in an Optional, we use an Optional of Optional.
    // The outer Optional determines if a response has been set. The inner Optional determines whether this response is null or a value
    private Optional<Optional<T>> response = Optional.empty();

    static final String PROPERTIES_FILE = "META-INF/application.properties";
    final static String GIFTCLOUD_VERSION;

    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private final HttpResponseProcessor<T> responseProcessor;
    private MultiUploadReporter reporter;


    static {
        final Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE));
        } catch (Throwable throwable) {
            LoggerFactory.getLogger(HttpConnectionBuilder.class).error("The properties file " + PROPERTIES_FILE + " could not be loaded", throwable);
        }
        String version = properties.getProperty("application.version");
        if (null != version) {
            GIFTCLOUD_VERSION = version;
        } else {
            GIFTCLOUD_VERSION = "";
        }
    }

    HttpRequest(final HttpConnectionWrapper.ConnectionType connectionType, final String urlString, final HttpResponseProcessor<T> responseProcessor, final MultiUploadReporter reporter) {
        this.connectionType = connectionType;
        this.urlString = urlString;
        this.responseProcessor = responseProcessor;
        this.reporter = reporter;
    }

    final T getResponse(final ConnectionFactory connectionFactory) throws IOException {
        if (!response.isPresent()) {
            doRequest(connectionFactory);
        }
        // The value of the response should now be set to an Optional - if this inner opttional is not set, that indicates a null value
        return response.get().orElse(null);
    }

    // Get the connection parameters. These may be altered by subclasses
    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException
    {
        // Add version
        connectionBuilder.setUserAgent("GiftCloudUploader/" + GIFTCLOUD_VERSION);

        // Accept all media types
        connectionBuilder.setAccept("*/*");

        // Set the type of request
        connectionBuilder.setConnectionType(connectionType);
    }

    abstract protected void processOutputStream(HttpConnectionWrapper connection) throws IOException;

    private void doRequest(final ConnectionFactory connectionFactory) throws IOException {

        try {
            final HttpConnectionBuilder connectionBuilder = new HttpConnectionBuilder(urlString);

            prepareConnection(connectionBuilder);

            // Build the connection
            final HttpConnectionWrapper connection = connectionFactory.createConnection(urlString, connectionBuilder);

            // Send data to the connection if required
            processOutputStream(connection);

            try {
                // Explicitly initiate the connection
                connection.connect();

                try {
                    throwIfBadResponse(connection);

                    // Get data from the connection and process
                    response = Optional.of(Optional.ofNullable(responseProcessor.processInputStream(connection)));

                    // ToDo: we need to process the error stream at some point           InputStream errorStream = connection.getErrorStream();


                } finally {
                    connection.disconnect();
                }

            } catch (IOException e) {
                reporter.silentLogException(e, "An error occurred while processing request " + connection.getUrlString());
                throwIfBadResponse(connection);
                throw e;
            }
        } finally {
            cleanup();
        }
    }

    protected void cleanup() {
    }

    private void throwIfBadResponse(final HttpConnectionWrapper connection) throws IOException {

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
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_CONFLICT:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_INTERNAL_ERROR:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_NOT_FOUND:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            default:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());
        }
    }

}


