/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

public class ProjectListModel extends DropDownListModel {

    private GiftCloudProperties giftCloudProperties;

    public ProjectListModel(final GiftCloudProperties giftCloudProperties) {
        this.giftCloudProperties = giftCloudProperties;
    }

    @Override
    void setLastUsedValue(String newValue) {
        giftCloudProperties.setLastProject(newValue);
        giftCloudProperties.save();
    }

    @Override
    Optional<String> getLastUsedValue() {
        return giftCloudProperties.getLastProject();
    }
}
