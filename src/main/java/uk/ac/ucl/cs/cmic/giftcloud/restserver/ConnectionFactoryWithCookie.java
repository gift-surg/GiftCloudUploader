/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.request.ConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnection;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionBuilder;

import java.io.IOException;

class ConnectionFactoryWithCookie implements ConnectionFactory {

    private final JSessionIdCookieWrapper cookie;
    private final ConnectionFactory connectionFactory;

    ConnectionFactoryWithCookie(final ConnectionFactory connectionFactory, final JSessionIdCookieWrapper cookie) {
        this.connectionFactory = connectionFactory;
        this.cookie = cookie;
    }

    public HttpConnection createConnection(final String fullUrl, final HttpConnectionBuilder connectionBuilder) throws IOException {

        connectionBuilder.setCookie(cookie.getFormattedCookieString());

        return connectionFactory.createConnection(fullUrl, connectionBuilder);
    }
}
