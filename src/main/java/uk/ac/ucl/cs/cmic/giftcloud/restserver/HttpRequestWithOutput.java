/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.io.OutputStream;

abstract class HttpRequestWithOutput<T> extends HttpRequest<T> {

    HttpRequestWithOutput(final HttpConnectionWrapper.ConnectionType connectionType, final String urlString, final HttpResponseProcessor<T> responseProcessor, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) {
        super(connectionType, urlString, responseProcessor, giftCloudProperties, reporter);
    }

    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException {
        super.prepareConnection(connectionBuilder);
        connectionBuilder.setDoOutput(true);
        connectionBuilder.setDoInput(true);
    }

    final protected void processOutputStream(HttpConnection connection) throws IOException {
        final OutputStream outputStream = connection.getOutputStream();
        try {
            streamToConnection(outputStream);
            outputStream.flush();
        } finally {
            outputStream.close();
        }
    }

    abstract protected void streamToConnection(final OutputStream outputStream) throws IOException;
}