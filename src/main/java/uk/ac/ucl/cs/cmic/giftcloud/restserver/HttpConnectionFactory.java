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
import java.net.MalformedURLException;
import java.net.URL;

class HttpConnectionFactory implements ConnectionFactory {

    private final String baseUrlString;
    private final URL baseUrl;

    HttpConnectionFactory(final String baseUrlString) throws MalformedURLException {
        this.baseUrlString = baseUrlString;
        this.baseUrl = new URL(baseUrlString);

        HttpURLConnection.setFollowRedirects(false);
    }

    @Override
    public HttpConnectionWrapper createConnection(final String relativeUrlString, final HttpConnectionBuilder connectionBuilder) throws IOException {

        final String urlString = getFullUrl(relativeUrlString);
        return connectionBuilder.buildHttpURLConnection(new HttpConnectionWrapper(urlString));
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    /** Combine the base URL with a relative path, while ensuring there is exactly one / character between them
     * @param relativePath the location relative to the base URI
     * @return the full URI string
     */
    private String getFullUrl(final String relativePath) {
        final StringBuilder sb = new StringBuilder(baseUrlString);

        // Remove any trailing '/' characters from the base URL
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '/') {
            sb.deleteCharAt(sb.length() - 1);
        }

        // Now add a single '/' only if one isn't already present in the appended relative path
        if ('/' != relativePath.charAt(0)) {
            sb.append('/');
        }
        sb.append(relativePath);

        return sb.toString();
    }
}
