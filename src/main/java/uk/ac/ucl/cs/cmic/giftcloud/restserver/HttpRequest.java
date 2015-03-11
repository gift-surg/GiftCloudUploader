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

import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    HttpRequest(final HttpConnectionWrapper.ConnectionType connectionType, final String urlString, final HttpResponseProcessor<T> responseProcessor) {
        this.connectionType = connectionType;
        this.urlString = urlString;
        this.responseProcessor = responseProcessor;
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
                // ToDo: Handle error
//                e.printStackTrace();

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
            switch (responseCode) {
                case HTTP_ACCEPTED:
                case HTTP_NOT_AUTHORITATIVE:
                case HTTP_NO_CONTENT:
                case HTTP_RESET:
                case HTTP_PARTIAL:
                case HTTP_MOVED_PERM:
                    logger.trace(connection.getRequestMethod() + " to {} returned " + responseCode + " ({})", urlString, connection.getResponseMessage());
                    return;

                case HTTP_OK:
                case HTTP_CREATED:
                    return;

                // Handle 302, at least temporarily: Spring auth redirects to login page,
                // so assume that's what's happened when we see a redirect at this point.
                case HTTP_MOVED_TEMP:
                case HTTP_UNAUTHORIZED:
                    if (logger.isDebugEnabled()) {
                        logger.debug("Received status code " + (responseCode == HTTP_MOVED_TEMP ? "302 (Redirect)" : "401 (Unauthorized)"));
                        for (final Map.Entry<String, List<String>> me : connection.getHeaderFields().entrySet()) {
                            logger.trace("Header {} : {}", me.getKey(), me.getValue());
                        }
                        logger.debug("Will request credentials for {}", urlString);
                    }
                    throw new AuthorisationFailureException(responseCode, new URL(urlString));

                case HTTP_CONFLICT:
                    StringBuilder sb = new StringBuilder("<h3>Session data conflict</h3>");
                    sb.append("<p>The server ");
                    appendServer(sb, url);
                    sb.append(" reports a conflict between the uploaded data and a session in the archive.</p>");
                    sb.append("<p>All or part of this session was previously uploaded. Go to the prearchive page ");
                    sb.append("to archive the data just uploaded as a new session, or to merge it into an existing session.");
                    throw new GiftCloudHttpException(responseCode, "Conflict", MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), sb.toString());

                case HTTP_INTERNAL_ERROR:
                    sb = new StringBuilder("<h3>Internal Server Error (500)</h3>");
                    sb.append("<p>The server ");
                    appendServer(sb, url);
                    sb.append(" is accessible but was unable to commit the requested session");
                    sb.append(" due to an internal error.</p>");
                    sb.append("<p>Please contact the administrator for help.");
                    sb.append(" A detailed description of the problem should be available");
                    sb.append(" in the DICOM receiver log or the XNAT logs.</p>");
                    throw new GiftCloudHttpException(responseCode, "Internal Error", MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), sb.toString());

                case HTTP_NOT_FOUND:
                    sb = new StringBuilder("<h3>Resource not found (404)</h3>");
                    sb.append("<p>The server ");
                    appendServer(sb, url);
                    sb.append(" is accessible but reports that the session resource ");
                    sb.append(urlString);
                    sb.append(" does not exist.</p>");
                    sb.append("<p>Contact the administrator for help.</p>");
                    throw new GiftCloudHttpException(responseCode, "Resource not found", MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), sb.toString());

            default:
                    sb = new StringBuilder("<h3>Unexpected error ");
                    sb.append(responseCode).append(": ");
                    sb.append(connection.getResponseMessage()).append("</h3>");
                    sb.append("<p>Unable to commit uploaded session</p>");
                    sb.append("<p>Please contact the administrator for help.</p>");

                    final StringBuilder message = new StringBuilder();
                    message.append(connection.getRequestMethod());
                    message.append(" to ").append(urlString).append(" failed ");
                    try {
                        final String entity = MultiUploaderUtils.getErrorEntity(connection.getErrorStream());
                        if (null != entity) {
                            message.append(": ").append(entity);
                        }
                    } catch (Throwable t) {
                        message.append(" - ").append(t.getMessage());
                    }
                    throw new GiftCloudHttpException(responseCode, "Unexpected error", MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), sb.toString());
            }
    }

    private static StringBuilder appendServer(final StringBuilder sb, final URL url) {
        sb.append(url.getProtocol()).append("://");
        sb.append(url.getHost());
        final int httpPort = url.getPort();
        if (-1 != httpPort && httpPort != url.getDefaultPort()) {
            sb.append(":").append(httpPort);
        }
        return sb;
    }
}


