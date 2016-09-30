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

import org.json.JSONObject;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

import java.io.IOException;
import java.io.InputStream;

public class HttpJsonPpidResponseProcessor extends HttpResponseProcessor<String> {
    private String key;

    public HttpJsonPpidResponseProcessor(final String key) {
        this.key = key;
    }

    protected final String streamFromConnection(final InputStream inputStream) throws IOException {
        final JSONObject entries2 = GiftCloudUtils.extractJSONEntity(inputStream);

        final String output = (entries2.getJSONArray("items").getJSONObject(0).getJSONObject("data_fields")).get(key).toString();
        return output;
    }
}
