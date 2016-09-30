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



package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONArray;
import uk.ac.ucl.cs.cmic.giftcloud.request.HttpResponseProcessor;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

import java.io.IOException;
import java.io.InputStream;

class HttpJsonResponseProcessor<T> extends HttpResponseProcessor<T> {
    private final JSONDecoder<T> decoder;

    HttpJsonResponseProcessor(final JSONDecoder<T> decoder) {
        this.decoder = decoder;
    }

    protected final T streamFromConnection(final InputStream inputStream) throws IOException {
        final JSONArray entries = GiftCloudUtils.extractResultFromEntity(GiftCloudUtils.extractJSONEntity(inputStream));
        for (int i = 0; i < entries.length(); i++) {
            decoder.decode(entries.getJSONObject(i));
        }
        return decoder.getResult();
    }

}
