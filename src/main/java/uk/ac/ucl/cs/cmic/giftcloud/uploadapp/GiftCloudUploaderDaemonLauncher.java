/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/



package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestClientFactory;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderController;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;
import uk.ac.ucl.cs.cmic.giftcloud.workers.DaemonStartupWorker;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.List;

public class GiftCloudUploaderDaemonLauncher implements UserCallback {

	public GiftCloudUploaderDaemonLauncher(final RestClientFactory restClientFactory, List<File> fileList) {
	    Optional<GiftCloudLogger> logger = Optional.empty();
        try {
            // Create and configure the application. This must be done before the main frame is created
            final GiftCloudUploaderConfiguration applicationConfiguration = new GiftCloudUploaderConfiguration();
            logger = Optional.of(applicationConfiguration.getLogger());

            // Create the general error, warning and progress callback class
            GiftCloudReporterFromDaemon reporter = new GiftCloudReporterFromDaemon(applicationConfiguration.getLogger());

            // Create the GUI-less UploaderController. This will use the UploaderControllerCallback and Reporting objects for any required output or user interaction
            final UploaderController uploaderController = new UploaderController(restClientFactory, applicationConfiguration.getProperties(), this, reporter);

            new Thread(new DaemonStartupWorker(applicationConfiguration, uploaderController, reporter)).start();
        }
        catch (Throwable t) {
            System.out.println("Error in app constructor:" + t.toString());
            t.printStackTrace(System.err);
            if (logger.isPresent()) {
                logger.get().silentLogException(t, "GIFT-Cloud Uploader encountered a problem when starting.");
            }
            System.out.println("Error in app constructor");
        }
    }

    @Override
    public String getProjectName(GiftCloudServer server) throws IOException {
        return null;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(String supplementalMessage) {
        return null;
    }

    public void quit() {
        System.exit(0);
    }
}
