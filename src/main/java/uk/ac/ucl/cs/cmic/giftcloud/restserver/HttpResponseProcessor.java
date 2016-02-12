/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnection;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;

import java.io.IOException;
import java.io.InputStream;

abstract class HttpResponseProcessor<T> {

    abstract protected T streamFromConnection(final InputStream inputStream) throws IOException;

    final T processInputStream(final HttpConnection connection) throws IOException {

        return new CloseableResource<T, InputStream>() {
            @Override
            public T run() throws IOException {
                resource = connection.getInputStream();
                return streamFromConnection(resource);
            }
        }.tryWithResource();
    }
}