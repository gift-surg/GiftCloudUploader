package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.MultiUploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PropertyStore;

import java.io.IOException;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

public class PropertyStoreFromApplet implements PropertyStore {

    private MultiUploadParameters multiUploadParameters;

    public PropertyStoreFromApplet(final MultiUploadParameters multiUploadParameters) {
        this.multiUploadParameters = multiUploadParameters;
    }

    @Override
    public String getProperty(final String propertyName) {
        if (propertyName.equals("GiftCloud_ServerUrl")) {
            return multiUploadParameters.getXnatUrl().orElse("");
        } else {
            return multiUploadParameters.getParameter(propertyName);
        }
    }

    @Override
    public void setProperty(final String propertyName, final String propertyValue) {
    }

    @Override
    public Optional<char[]> getPassword(final String passwordKey) {
        return Optional.empty();
    }

    @Override
    public void setPassword(final String passwordKey, char[] lastPassword) {
    }

    @Override
    public void save(String comment) throws IOException {
    }
}
