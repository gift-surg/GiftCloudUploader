package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.net.MalformedURLException;

public interface RestServerFactory {
    RestClient create(String giftCloudServerUrlString, GiftCloudProperties giftCloudProperties, final UserCallback userCallback, GiftCloudReporter reporter) throws MalformedURLException;
}
