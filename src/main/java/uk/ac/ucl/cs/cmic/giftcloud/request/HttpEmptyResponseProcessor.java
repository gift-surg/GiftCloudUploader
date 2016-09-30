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

import java.io.IOException;
import java.io.InputStream;


public class HttpEmptyResponseProcessor  extends HttpResponseProcessor<Void> {

    final protected Void streamFromConnection(final InputStream inputStream) throws IOException {
        return null;
    }
}
