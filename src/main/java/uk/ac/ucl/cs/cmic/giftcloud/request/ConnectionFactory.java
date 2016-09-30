/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.request;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnection;
import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionBuilder;

import java.io.IOException;

public interface ConnectionFactory {
    HttpConnection createConnection(final String fullUrl, final HttpConnectionBuilder connectionBuilder) throws IOException;
}
