/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONObject;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

import java.io.IOException;
import java.io.InputStream;

class HttpJsonPpidResponseProcessor extends HttpResponseProcessor<String> {
    private String key;

    HttpJsonPpidResponseProcessor(final String key) {
        this.key = key;
    }

    protected final String streamFromConnection(final InputStream inputStream) throws IOException {
        final JSONObject entries2 = GiftCloudUtils.extractJSONEntity(inputStream);

        final String output = (entries2.getJSONArray("items").getJSONObject(0).getJSONObject("data_fields")).get(key).toString();
        return output;
    }
}
