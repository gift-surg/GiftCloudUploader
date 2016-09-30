/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.request;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnection;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;

import java.io.IOException;
import java.io.InputStream;

abstract public class HttpResponseProcessor<T> {

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