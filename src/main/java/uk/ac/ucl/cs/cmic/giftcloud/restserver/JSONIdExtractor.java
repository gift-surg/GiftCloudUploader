/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

final class JSONIdExtractor implements JSONDecoder<Map<String, String>> {
    private final Logger logger = LoggerFactory.getLogger(JSONIdExtractor.class);
    private final Map<String, String> m = new LinkedHashMap<String, String>();
    private final String idKey;

    JSONIdExtractor(final String idKey) {
        this.idKey = idKey;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.net.RestOpManager.JSONDecoder#decode(org.json.JSONObject)
     */
    public void decode(final JSONObject o) throws JSONException {
        final String id = o.has(idKey) ? o.getString(idKey) : null;
        m.put(id, id);
    }

    public Map<String, String> getResult() {
        return m;
    }
}
