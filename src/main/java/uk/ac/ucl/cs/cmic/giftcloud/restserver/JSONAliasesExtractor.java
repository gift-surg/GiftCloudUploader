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

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

final class JSONAliasesExtractor implements JSONDecoder<Map<String, String>> {
    private final Logger logger = LoggerFactory.getLogger(JSONAliasesExtractor.class);
    private final Map<String, String> m = new LinkedHashMap<String, String>();
    private final String aliasKey, idKey;

    JSONAliasesExtractor(final String aliasKey, final String idKey) {
        this.aliasKey = aliasKey;
        this.idKey = idKey;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.net.RestOpManager.JSONDecoder#decode(org.json.JSONObject)
     */
    public void decode(final JSONObject o) throws JSONException {
        logger.trace("decoding {} using {} -> {}", new Object[]{o, aliasKey, idKey});
        final String alias = o.has(aliasKey) ? o.getString(aliasKey) : null;
        final String id = o.has(idKey) ? o.getString(idKey) : null;
        if (!isNullOrEmpty(alias)) {
            m.put(alias, isNullOrEmpty(id) ? alias : id);
        } else if (!isNullOrEmpty(id)) {
            m.put(id, id);
        }
    }

    public Map<String, String> getResult() {
        return m;
    }

    private static boolean isNullOrEmpty(final String s) {
        return null == s || "".equals(s);
    }
}
