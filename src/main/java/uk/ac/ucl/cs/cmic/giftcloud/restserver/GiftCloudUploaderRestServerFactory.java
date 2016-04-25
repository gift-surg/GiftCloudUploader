package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.httpconnection.HttpConnectionFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.net.MalformedURLException;

public class GiftCloudUploaderRestServerFactory implements RestServerFactory {

    @Override
    public RestServer create(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final UserCallback userCallback, final GiftCloudReporter reporter) throws MalformedURLException {
        return new GiftCloudUploaderRestServer(giftCloudServerUrlString, giftCloudProperties, new HttpConnectionFactory(), userCallback, reporter);
    }
}
