/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.request;

import java.io.IOException;
import java.io.InputStream;


public class HttpEmptyResponseProcessor  extends HttpResponseProcessor<Void> {

    final protected Void streamFromConnection(final InputStream inputStream) throws IOException {
        return null;
    }
}
