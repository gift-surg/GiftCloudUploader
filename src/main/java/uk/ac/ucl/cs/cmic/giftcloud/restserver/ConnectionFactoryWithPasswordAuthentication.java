/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.nrg.util.Base64;
import uk.ac.ucl.cs.cmic.giftcloud.request.ConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnection;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionBuilder;

import java.io.IOException;
import java.net.PasswordAuthentication;

class ConnectionFactoryWithPasswordAuthentication implements ConnectionFactory {

    private final PasswordAuthentication passwordAuthentication;
    private ConnectionFactory connectionFactory;

    ConnectionFactoryWithPasswordAuthentication(final ConnectionFactory connectionFactory, final PasswordAuthentication passwordAuthentication) {
        this.connectionFactory = connectionFactory;
        this.passwordAuthentication = passwordAuthentication;
    }

    public HttpConnection createConnection(final String fullUrl, final HttpConnectionBuilder connectionBuilder) throws IOException {
        connectionBuilder.setAuthorisationHeader(makeBasicAuthorization(passwordAuthentication));
        return connectionFactory.createConnection(fullUrl, connectionBuilder);
    }

    private static String makeBasicAuthorization(final PasswordAuthentication auth) {
        return "Basic " + Base64.encode(auth.getUserName() + ":" +  new String(auth.getPassword()));
    }
}
