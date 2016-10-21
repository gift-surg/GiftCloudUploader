/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.net.MalformedURLException;

public interface RestClientFactory {
    RestClient create(String giftCloudServerUrlString, GiftCloudProperties giftCloudProperties, final UserCallback userCallback, GiftCloudReporter reporter) throws MalformedURLException;
}
