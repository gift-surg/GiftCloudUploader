package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;

final class JSONValuesExtractor implements JSONDecoder<Collection<Object>> {
    private final Logger logger = LoggerFactory.getLogger(JSONValuesExtractor.class);
    private final Collection<Object> c = new LinkedHashSet<Object>();
    private final String key;

    JSONValuesExtractor(final String key) {
        this.key = key;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.net.RestOpManager.JSONDecoder#decode(org.json.JSONObject)
     */
    public void decode(final JSONObject o) throws JSONException {
        logger.trace("decoding {} from {}", key, o);
        if (o.has(key)) {
            c.add(o.get(key));
        }
    }

    public Collection<Object> getResult() {
        return c;
    }
}
