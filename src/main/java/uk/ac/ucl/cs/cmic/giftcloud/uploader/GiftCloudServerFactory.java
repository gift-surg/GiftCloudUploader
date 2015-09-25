package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.ProjectListModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

public class GiftCloudServerFactory {

    private Optional<GiftCloudServer> giftCloudServer = Optional.empty();
    private RestServerFactory restServerFactory;
    private GiftCloudProperties properties;
    private ProjectListModel projectListModel;
    private GiftCloudReporter reporter;

    public GiftCloudServerFactory(final RestServerFactory restServerFactory, final GiftCloudProperties properties, final ProjectListModel projectListModel, final GiftCloudReporter reporter) {
        this.restServerFactory = restServerFactory;
        this.properties = properties;
        this.projectListModel = projectListModel;
        this.reporter = reporter;
    }

    public GiftCloudServer getGiftCloudServer() throws IOException {

        final Optional<String> optionalGiftCloudUrl = properties.getGiftCloudUrl();

        // Check for an URL which is either not present or empty
        if (!optionalGiftCloudUrl.isPresent() || StringUtils.isBlank(optionalGiftCloudUrl.get())) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        final String giftCloudUrl = optionalGiftCloudUrl.get();

        // We need to create new GiftCloudServer if one does not exist, or if the URL has changed
        if (!(giftCloudServer.isPresent() && giftCloudServer.get().matchesServer(giftCloudUrl))) {

            // The project list is no longer valid. We will update it after creating a new GiftCloudAutoUploader, but if that throws an exception, we want to leave the project list model in an invalid state
            projectListModel.invalidate();

            giftCloudServer = Optional.of(new GiftCloudServer(restServerFactory, giftCloudUrl, properties, reporter));

            // Now update the project list
            projectListModel.setItems(giftCloudServer.get().getListOfProjects());
        }

        return giftCloudServer.get();
    }

    public void invalidate() {
        giftCloudServer = Optional.empty();
    }
}
