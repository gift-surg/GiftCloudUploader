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
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionBuilder;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.io.OutputStream;

abstract class HttpRequestWithOutput<T> extends HttpRequest<T> {

    HttpRequestWithOutput(final HttpConnection.ConnectionType connectionType, final String urlString, final HttpResponseProcessor<T> responseProcessor, final HttpProperties httpProperties, final GiftCloudReporter reporter) {
        super(connectionType, urlString, responseProcessor, httpProperties, reporter);
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