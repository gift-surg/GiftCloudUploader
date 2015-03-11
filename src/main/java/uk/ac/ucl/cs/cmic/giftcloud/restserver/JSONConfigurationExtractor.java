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
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Turns a JSON object from the XNAT configuration service into a map.
 */
final class JSONConfigurationExtractor implements JSONDecoder<Optional<Object>> {

    private final HashMap<String, Object> map = new HashMap<String, Object>();

    @Override
    public void decode(final JSONObject json) throws JSONException {
        final Iterator keys = json.keys();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            map.put(key, json.get(key));
        }
        if (map.containsKey("status") && !map.get("status").equals("enabled")) {
            map.put("status", "disabled");
            return;
        }
        if (map.containsKey("contents")) {
            final String contents = (String) map.get("contents");
            try {
                JSONObject translated = new JSONObject(new JSONTokener(contents));
                Map<String, Object> decoded = new HashMap<String, Object>();
                final Iterator contentKeys = translated.keys();
                while(contentKeys.hasNext()) {
                    final String key = (String) contentKeys.next();
                    decoded.put(key, translated.has(key) ? translated.get(key) : "");
                }
                map.put("contents", decoded);
            } catch (JSONException ignored) {
                // If we get a JSONException, then the contents isn't something we know how to work with.
                // Leave the contents alone.
            }
        }
    }

    public Optional<Object> getResult() {
        if (map.containsKey("status") && map.get("status").equals("enabled") && map.containsKey("contents")) {
            return Optional.of(map.get("contents"));
        } else {
            return Optional.empty();
        }
    }
}
