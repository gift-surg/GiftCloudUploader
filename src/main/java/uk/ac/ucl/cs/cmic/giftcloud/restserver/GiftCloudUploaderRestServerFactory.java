package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.net.MalformedURLException;

public class GiftCloudUploaderRestServerFactory implements RestServerFactory {

    @Override
    public GiftCloudUploaderRestServer create(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) throws MalformedURLException {
        return new GiftCloudUploaderRestServer(giftCloudServerUrlString, giftCloudProperties, new HttpConnectionFactory(), reporter);
    }
}
