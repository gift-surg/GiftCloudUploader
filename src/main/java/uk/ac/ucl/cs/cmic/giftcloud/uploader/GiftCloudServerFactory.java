/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestClientFactory;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.ProjectListModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;
import java.net.MalformedURLException;

public class GiftCloudServerFactory {

    private Optional<GiftCloudServer> giftCloudServer = Optional.empty();
    private final RestClientFactory restClientFactory;
    private final GiftCloudProperties properties;
    private final ProjectListModel projectListModel;
    private final UserCallback userCallback;
    private final GiftCloudReporter reporter;
    private final PixelDataAnonymiserFilterCache filters;

    public GiftCloudServerFactory(final PixelDataAnonymiserFilterCache filters, final RestClientFactory restClientFactory, final GiftCloudProperties properties, final ProjectListModel projectListModel, final UserCallback userCallback, final GiftCloudReporter reporter) {
        this.filters = filters;
        this.restClientFactory = restClientFactory;
        this.properties = properties;
        this.projectListModel = projectListModel;
        this.userCallback = userCallback;
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

            // The project list is no longer valid. We will update it after creating a new AutoUploader, but if that throws an exception, we want to leave the project list model in an invalid state
            projectListModel.invalidate();

            giftCloudServer = Optional.of(new GiftCloudServer(filters, restClientFactory, giftCloudUrl, properties, userCallback, reporter));

            // Now update the project list
            projectListModel.setItems(giftCloudServer.get().getListOfProjects());
        }

        return giftCloudServer.get();
    }

    public void invalidate() {
        giftCloudServer = Optional.empty();
    }
}
