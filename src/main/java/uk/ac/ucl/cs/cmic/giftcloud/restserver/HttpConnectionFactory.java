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

class HttpConnectionFactory implements ConnectionFactory {

    private String baseUrl;

    HttpConnectionFactory(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public HttpConnectionWrapper createConnection(final String relativeUrlString, final HttpConnectionBuilder connectionBuilder) throws IOException {

        final String urlString = getFullUrl(relativeUrlString);
        return connectionBuilder.buildHttpURLConnection(new HttpConnectionWrapper(urlString));
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private String getFullUrl(final String relativePath) {
        final StringBuilder sb = new StringBuilder(baseUrl.toString());
        if ('/' != relativePath.charAt(0)) {
            sb.append('/');
        }
        sb.append(relativePath);
        return sb.toString();
    }
}
