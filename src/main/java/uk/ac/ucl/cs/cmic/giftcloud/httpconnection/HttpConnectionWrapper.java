/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.httpconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class HttpConnectionWrapper implements HttpConnection {
    private HttpURLConnection connection;
    private final String urlString;


    public HttpConnectionWrapper(final String urlString) throws IOException {
        this.urlString = urlString;
        final URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    @Override
    public InputStream getErrorStream() {
        return connection.getErrorStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return connection.getOutputStream();
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        connection.setRequestMethod(method);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        connection.addRequestProperty(key, value);
    }

    @Override
    public String getRequestMethod() {
        return connection.getRequestMethod();
    }

    @Override
    public void setDoInput(boolean doinput) {
        connection.setDoInput(doinput);
    }

    @Override
    public long getDate() {
        return connection.getDate();
    }

    @Override
    public String getResponseMessage() throws IOException {
        return connection.getResponseMessage();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }

    @Override
    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    @Override
    public void setRequestProperty(String key, String value) {
        connection.setRequestProperty(key, value);
    }

    @Override
    public void setFixedLengthStreamingMode(long contentLength) {
        connection.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public void connect() throws IOException {
        connection.connect();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        connection.setUseCaches(usecaches);
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        connection.setDoOutput(dooutput);
    }

    @Override
    public void setChunkedStreamingMode(int chunklen) {
        connection.setChunkedStreamingMode(chunklen);
    }

    @Override
    public URL getURL() {
        return connection.getURL();
    }

    @Override
    public String getUrlString() {
        return urlString;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        connection.setConnectTimeout(timeout);
    }

}
