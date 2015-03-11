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

class HttpRequestWithoutOutput<T> extends HttpRequest<T> {

    HttpRequestWithoutOutput(final HttpConnectionWrapper.ConnectionType connectionType, final String urlString, final HttpResponseProcessor<T> responseProcessor) {
        super(connectionType, urlString, responseProcessor);
    }

    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException {
        super.prepareConnection(connectionBuilder);
        connectionBuilder.setDoOutput(false);
        connectionBuilder.setDoInput(true);
    }

    final protected void processOutputStream(HttpConnectionWrapper connection) throws IOException {
    }

}