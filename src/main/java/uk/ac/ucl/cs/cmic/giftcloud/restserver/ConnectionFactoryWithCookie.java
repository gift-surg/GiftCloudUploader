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

class ConnectionFactoryWithCookie implements ConnectionFactory {

    private final JSessionIdCookieWrapper cookie;
    private final HttpConnectionFactory connectionFactory;

    ConnectionFactoryWithCookie(final HttpConnectionFactory connectionFactory, final JSessionIdCookieWrapper cookie) {
        this.connectionFactory = connectionFactory;
        this.cookie = cookie;
    }

    public HttpConnectionWrapper createConnection(final String relativeUrlString, final HttpConnectionBuilder connectionBuilder) throws IOException {

        connectionBuilder.setCookie(cookie.getFormattedCookieString());

        return connectionFactory.createConnection(relativeUrlString, connectionBuilder);
    }
}
