/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

class FakeHttpConnectionWrapper implements HttpConnection {

    private String fullUrlString;
    private final URL fullUrl;

    public FakeHttpConnectionWrapper(final String fullUrlString) throws MalformedURLException {
        this.fullUrlString = fullUrlString;
        this.fullUrl = new URL(fullUrlString);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public InputStream getErrorStream() {

        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {

    }

    @Override
    public void addRequestProperty(String key, String value) {

    }

    @Override
    public String getRequestMethod() {
        return "FAKE_REQUEST";
    }

    @Override
    public void setDoInput(boolean doinput) {
    }

    @Override
    public long getDate() {
        return 0;
    }

    @Override
    public String getResponseMessage() throws IOException {
        return "FAKE_RESPONSE";
    }

    @Override
    public InputStream getInputStream() throws IOException {

        // ToDo
        return null;
    }

    @Override
    public int getResponseCode() throws IOException {

        return HttpURLConnection.HTTP_OK;
    }

    @Override
    public void setRequestProperty(String key, String value) {

    }

    @Override
    public void setFixedLengthStreamingMode(long contentLength) {

    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public void setUseCaches(boolean usecaches) {

    }

    @Override
    public void setDoOutput(boolean dooutput) {

    }

    @Override
    public void setChunkedStreamingMode(int chunklen) {

    }

    @Override
    public URL getURL() {
        return fullUrl;
    }

    @Override
    public String getUrlString() {
        return fullUrlString;
    }

    @Override
    public void setConnectTimeout(int timeout) {
    }
}
