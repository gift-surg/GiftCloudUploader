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