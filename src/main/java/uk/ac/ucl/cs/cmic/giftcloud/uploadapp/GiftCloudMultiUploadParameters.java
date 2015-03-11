package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.MultiUploadParameters;

import java.util.Hashtable;

public class GiftCloudMultiUploadParameters extends MultiUploadParameters{


    private Hashtable<String, String> parameters = new Hashtable<String, String>();

    @Override
    public String getParameter(String key) {
        if (parameters.containsKey(key)) {
            return parameters.get(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean getDateFromSession() {
        return false;
    }

    public void setParameter(final String parameter, final String value) {
        parameters.put(parameter, value);
    }
}
