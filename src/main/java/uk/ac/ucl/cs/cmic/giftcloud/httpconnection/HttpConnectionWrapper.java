/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.httpconnection;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpConnectionWrapper implements HttpConnection {
    private HttpURLConnection connection;
    private final String urlString;

    private static final Method SET_FIXED_LENGTH_STREAMING_MODE_METHOD;
    private static final boolean SET_FIXED_LENGTH_STREAMING_MODE_LONG_EXISTS;
    static {
        Method method;
        boolean long_method_exists = false;
        try {
            method = HttpURLConnection.class.getMethod("setFixedLengthStreamingMode", long.class);
            long_method_exists = true;
        } catch (NoSuchMethodException exception) {
            try {
                method = HttpURLConnection.class.getMethod("setFixedLengthStreamingMode", int.class);
            } catch (NoSuchMethodException exception2) {
                throw new RuntimeException(exception2);
            }
        }
        SET_FIXED_LENGTH_STREAMING_MODE_LONG_EXISTS = long_method_exists;
        SET_FIXED_LENGTH_STREAMING_MODE_METHOD = method;
    }

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
    public void setFixedLengthStreamingMode(long contentLength) throws GiftCloudException {
        if (!SET_FIXED_LENGTH_STREAMING_MODE_LONG_EXISTS) {
            if ((int)contentLength != contentLength) {
                throw new GiftCloudException(GiftCloudUploaderError.FILE_TOO_LARGE);
            }
        }

        try {
            SET_FIXED_LENGTH_STREAMING_MODE_METHOD.invoke(connection, contentLength);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Java reflection failed for setFixedLengthStreamingMode", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Java reflection failed for setFixedLengthStreamingMode", e);
        }
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
