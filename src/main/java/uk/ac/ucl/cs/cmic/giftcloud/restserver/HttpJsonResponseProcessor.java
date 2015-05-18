/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONArray;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.IOException;
import java.io.InputStream;

class HttpJsonResponseProcessor<T> extends HttpResponseProcessor<T> {
    private final JSONDecoder<T> decoder;

    HttpJsonResponseProcessor(final JSONDecoder<T> decoder) {
        this.decoder = decoder;
    }

    protected final T streamFromConnection(final InputStream inputStream) throws IOException {
        final JSONArray entries = MultiUploaderUtils.extractResultFromEntity(MultiUploaderUtils.extractJSONEntity(inputStream));
        for (int i = 0; i < entries.length(); i++) {
            decoder.decode(entries.getJSONObject(i));
        }
        return decoder.getResult();
    }

}
