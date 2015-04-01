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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.List;
import java.util.Map;

class HttpConnectionWrapper {
    private HttpURLConnection connection;
    private final String urlString;


    HttpConnectionWrapper(final String urlString) throws IOException {
        this.urlString = urlString;
        final URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
    }

    public void disconnect() {
        connection.disconnect();
    }

    public InputStream getErrorStream() {
        return connection.getErrorStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return connection.getOutputStream();
    }

    public void setRequestMethod(String method) throws ProtocolException {
        connection.setRequestMethod(method);
    }

    public void addRequestProperty(String key, String value) {
        connection.addRequestProperty(key, value);
    }

    public String getRequestMethod() {
        return connection.getRequestMethod();
    }

    public void setDoInput(boolean doinput) {
        connection.setDoInput(doinput);
    }

    public long getDate() {
        return connection.getDate();
    }

    public String getResponseMessage() throws IOException {
        return connection.getResponseMessage();
    }

    public Map<String, List<String>> getHeaderFields() {
        return connection.getHeaderFields();
    }

    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }

    public Object getContent() throws IOException {
        return connection.getContent();
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public void setRequestProperty(String key, String value) {
        connection.setRequestProperty(key, value);
    }

    public void setFixedLengthStreamingMode(int contentLength) {
        connection.setFixedLengthStreamingMode(contentLength);
    }

    public void connect() throws IOException {
        connection.connect();
    }

    public void setUseCaches(boolean usecaches) {
        connection.setUseCaches(usecaches);
    }

    public void setDoOutput(boolean dooutput) {
        connection.setDoOutput(dooutput);
    }

    public void setChunkedStreamingMode(int chunklen) {
        connection.setChunkedStreamingMode(chunklen);
    }

    public URL getURL() {
        return connection.getURL();
    }

    public String getUrlString() {
        return urlString;
    }

    public enum ConnectionType {
        GET("GET"),
        POST("POST"),
        PUT("PUT");

        private final String methodString;

        ConnectionType(final String methodString) {
            this.methodString = methodString;
        }

        String getMethodString() {
            return methodString;
        }
    }
}
