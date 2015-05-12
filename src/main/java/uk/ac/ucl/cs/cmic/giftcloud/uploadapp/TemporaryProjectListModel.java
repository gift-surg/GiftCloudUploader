package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;

import java.util.Optional;

public class TemporaryProjectListModel extends DropDownListModel {

    private GiftCloudProperties giftCloudProperties;

    public TemporaryProjectListModel(final GiftCloudProperties giftCloudProperties) {
        this.giftCloudProperties = giftCloudProperties;
    }

    @Override
    void setLastUsedValue(String newValue) {
        giftCloudProperties.setLastProject(newValue);
    }

    @Override
    Optional<String> getLastUsedValue() {
        return giftCloudProperties.getLastProject();
    }
}
