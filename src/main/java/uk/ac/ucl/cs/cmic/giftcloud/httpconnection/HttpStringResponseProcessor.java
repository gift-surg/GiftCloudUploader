/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.httpconnection;

import java.io.IOException;
import java.io.InputStream;


public class HttpStringResponseProcessor extends HttpResponseProcessor<String> {

    protected final String streamFromConnection(final InputStream inputStream) throws IOException {
        return getString(inputStream);
    }

    static String getString(final InputStream inputStream) throws IOException {
        final int BUF_SIZE = 512;
        final StringBuilder sb = new StringBuilder();
        final byte[] buf = new byte[BUF_SIZE];
        for (int n = inputStream.read(buf); n > 0; n = inputStream.read(buf)) {
            sb.append(new String(buf, 0, n));
        }
        return sb.toString();
    }

}
