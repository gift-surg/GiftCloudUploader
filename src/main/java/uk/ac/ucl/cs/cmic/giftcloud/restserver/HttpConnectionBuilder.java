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
import java.util.Optional;

public class HttpConnectionBuilder {

    static final String CONTENT_TYPE_ZIP = "application/zip";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String COOKIE_HEADER = "Cookie";
    static final String USER_AGENT = "User-Agent";
    static final String ACCEPT = "Accept";

    private Optional<Boolean> doInput = Optional.empty();
    private Optional<Boolean> doOutput = Optional.empty();
    private Optional<Long> fixedLengthStreamingMode = Optional.empty();
    private Optional<String> contentType = Optional.empty();
    private Optional<Integer> chunkedStreamingMode = Optional.empty();
    private Optional<HttpConnectionWrapper.ConnectionType> connectionType = Optional.empty();
    private Optional<String> authorisationHeader = Optional.empty();
    private Optional<String> cookieString = Optional.empty();
    private Optional<String> userAgent = Optional.empty();
    private Optional<String> accept = Optional.empty();
    private Optional<Integer> connectTimeout = Optional.empty();

    private final String urlString;

    HttpConnectionBuilder(final String urlString) {
        this.urlString = urlString;
    }

    HttpConnection buildHttpURLConnection(final HttpConnection connection) throws IOException {
        if (connectionType.isPresent()) {
            connection.setRequestMethod(connectionType.get().getMethodString());
        } else {
            throw new RuntimeException("The connection type for this request has not been set");
        }

        if (doInput.isPresent()) {
            connection.setDoInput(doInput.get());
        }

        if (doOutput.isPresent()) {
            connection.setDoOutput(doOutput.get());
        }

        if (fixedLengthStreamingMode.isPresent()) {
            connection.setFixedLengthStreamingMode(fixedLengthStreamingMode.get());
        }

        if (contentType.isPresent()) {
            connection.setRequestProperty(CONTENT_TYPE_HEADER, contentType.get());
        }

        if (chunkedStreamingMode.isPresent()) {
            connection.setChunkedStreamingMode(chunkedStreamingMode.get());
        }

        if (userAgent.isPresent()) {
            connection.setRequestProperty(USER_AGENT, userAgent.get());
        }

        if (accept.isPresent()) {
            connection.setRequestProperty(ACCEPT, accept.get());
        }

        if (authorisationHeader.isPresent()) {
            connection.addRequestProperty(AUTHORIZATION_HEADER, authorisationHeader.get());
        }

        if (cookieString.isPresent()) {
            connection.setRequestProperty(COOKIE_HEADER, cookieString.get());
        }

        if (connectTimeout.isPresent()) {
            connection.setConnectTimeout(connectTimeout.get());
        }

        connection.setUseCaches(false);

        return connection;
    }

    public void setDoInput(final boolean doInput) {
        this.doInput = Optional.of(doInput);
    }

    public void setDoOutput(final boolean doOutput) {
        this.doOutput = Optional.of(doOutput);
    }

    public void setConnectionType(final HttpConnectionWrapper.ConnectionType connectionType) {
        this.connectionType = Optional.of(connectionType);
    }

    public void setFixedLengthStreamingMode(long contentLength) {
        fixedLengthStreamingMode = Optional.of(contentLength);
    }

    public void setContentType(final String contentType) {
        this.contentType = Optional.of(contentType);
    }

    public void setChunkedStreamingMode(final int chunklen) {
        chunkedStreamingMode = Optional.of(chunklen);
    }

    public void setAuthorisationHeader(final String authorisationHeader) {
        this.authorisationHeader = Optional.of(authorisationHeader);
    }

    public void setCookie(final String cookie) {
        this.cookieString = Optional.of(cookie);
    }

    public void setUserAgent(final String userAgent) {
        this.userAgent = Optional.of(userAgent);
    }

    public void setAccept(final String accept) {
        this.accept = Optional.of(accept);
    }

    public String getUrl() {
        return urlString;
    }

    public void setConnectTimeout(final int timeout) {
        this.connectTimeout = Optional.of(timeout);
    }
}
