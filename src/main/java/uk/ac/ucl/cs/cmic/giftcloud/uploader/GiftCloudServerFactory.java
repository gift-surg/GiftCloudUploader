package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.ProjectListModel;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

public class GiftCloudServerFactory {

    private Optional<GiftCloudServer> giftCloudServer = Optional.empty();
    private GiftCloudProperties properties;
    private ProjectListModel projectListModel;
    private Container container;
    private PendingUploadTaskList pendingUploadTaskList;
    private MultiUploadReporter reporter;

    public GiftCloudServerFactory(final GiftCloudProperties properties, final ProjectListModel projectListModel, final Container container, final PendingUploadTaskList pendingUploadTaskList, final MultiUploadReporter reporter) {
        this.properties = properties;
        this.projectListModel = projectListModel;
        this.container = container;
        this.pendingUploadTaskList = pendingUploadTaskList;
        this.reporter = reporter;
    }

    public GiftCloudServer getGiftCloudServer() throws IOException {

        final Optional<String> optionalGiftCloudUrl = properties.getGiftCloudUrl();

        // Check for an URL which is either not present or empty
        if (!optionalGiftCloudUrl.isPresent() || StringUtils.isBlank(optionalGiftCloudUrl.get())) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        final String giftCloudUrl = optionalGiftCloudUrl.get();

        // We need to create new GiftCloudAutoUploader if one does not exist, or if the URL has changed
        if (!(giftCloudServer.isPresent() && giftCloudServer.get().matchesServer(giftCloudUrl))) {

            // The project list is no longer valid. We will update it after creating a new GiftCloudAutoUploader, but if that throws an exception, we want to leave the project list model in an invalid state
            projectListModel.invalidate();

            giftCloudServer = Optional.of(new GiftCloudServer(giftCloudUrl, properties, reporter));

            // Now update the project list
            projectListModel.setItems(giftCloudServer.get().getListOfProjects());
        }

        return giftCloudServer.get();
    }
}
