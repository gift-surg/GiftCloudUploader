package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedHashSet;

final class JSONValuesExtractor implements JSONDecoder<Collection<String>> {
    private final Collection<String> decodedStrings = new LinkedHashSet<String>();
    private final String key;

    JSONValuesExtractor(final String key) {
        this.key = key;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.net.RestOpManager.JSONDecoder#decode(org.json.JSONObject)
     */
    public void decode(final JSONObject object) throws JSONException {
        if (object.has(key)) {
            decodedStrings.add((String) (object.get(key)));
        }
    }

    public Collection<String> getResult() {
        return decodedStrings;
    }
}
