/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

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
