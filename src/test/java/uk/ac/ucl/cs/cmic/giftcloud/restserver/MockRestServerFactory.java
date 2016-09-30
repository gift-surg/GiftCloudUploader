/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/



package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpMockConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.net.MalformedURLException;

public class MockRestServerFactory implements RestServerFactory {

    @Override
    public RestClient create(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final UserCallback userCallback, final GiftCloudReporter reporter) throws MalformedURLException {
        final MockRestClient mockRestServer = new MockRestClient(giftCloudServerUrlString, giftCloudProperties, new HttpMockConnectionFactory(), reporter);
        mockRestServer.addTestProject("sandbox");
        mockRestServer.addTestProject("testproject1");
        mockRestServer.addTestProject("testproject2");
        return mockRestServer;
    }
}
