package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.net.MalformedURLException;

public class MockRestServerFactory implements RestServerFactory {

    @Override
    public RestServer create(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) throws MalformedURLException {
        return new MockRestServer(giftCloudServerUrlString, giftCloudProperties, new HttpMockConnectionFactory(), reporter);
    }
}
