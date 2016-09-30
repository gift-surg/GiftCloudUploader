/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.httpconnection;

import uk.ac.ucl.cs.cmic.giftcloud.request.ConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpMockConnectionFactory implements ConnectionFactory {

    public HttpMockConnectionFactory() {
        HttpURLConnection.setFollowRedirects(false);
    }

    @Override
    public HttpConnection createConnection(final String fullUrl, final HttpConnectionBuilder connectionBuilder) throws IOException {
        return connectionBuilder.buildHttpURLConnection(new FakeHttpConnectionWrapper(fullUrl));
    }
}
