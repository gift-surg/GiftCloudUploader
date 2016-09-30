/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

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
