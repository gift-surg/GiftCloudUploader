package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.json.JSONObject;

interface JSONDecoder<ResultType> {
    void decode(JSONObject o) throws JSONException;
    ResultType getResult();
}
