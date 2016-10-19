/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.httpconnection;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;

public interface HttpConnection {

    enum ConnectionType {
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

    void disconnect();

    InputStream getErrorStream();

    OutputStream getOutputStream() throws IOException;

    void setRequestMethod(String method) throws ProtocolException;

    void addRequestProperty(String key, String value);

    String getRequestMethod();

    void setDoInput(boolean doinput);

    long getDate();

    String getResponseMessage() throws IOException;

    InputStream getInputStream() throws IOException;

    int getResponseCode() throws IOException;

    void setRequestProperty(String key, String value);

    void setFixedLengthStreamingMode(long contentLength) throws GiftCloudException;

    void connect() throws IOException;

    void setUseCaches(boolean usecaches);

    void setDoOutput(boolean dooutput);

    void setChunkedStreamingMode(int chunklen);

    URL getURL();

    String getUrlString();

    void setConnectTimeout(int timeout);

}
