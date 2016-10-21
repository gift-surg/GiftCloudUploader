/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.net.MalformedURLException;

public class GiftCloudUploaderRestClientFactory implements RestClientFactory {

    @Override
    public RestClient create(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final UserCallback userCallback, final GiftCloudReporter reporter) throws MalformedURLException {
        return new GiftCloudUploaderRestClient(giftCloudServerUrlString, giftCloudProperties, new HttpConnectionFactory(), userCallback, reporter);
    }
}
