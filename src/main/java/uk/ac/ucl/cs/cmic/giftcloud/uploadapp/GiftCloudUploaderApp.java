/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/



package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestClientFactory;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestClientFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderController;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GiftCloudUploaderApp {

	public GiftCloudUploaderApp(final RestClientFactory restClientFactory, List<File> fileList) {
	    Optional<GiftCloudLogger> logger = Optional.empty();
        try {
            // Create and configure the application. This must be done before the main frame is created
            final GiftCloudUploaderAppConfiguration applicationConfiguration = new GiftCloudUploaderAppConfiguration();
            logger = Optional.of(applicationConfiguration.getLogger());

            // Create the main gui component
            final MainFrame mainFrame = new MainFrame(applicationConfiguration);

            // Tell the applicationConfiguration about the main frame. This will allow the singleton processing to show and bring the window to the front if a second attempt is made to instantiate the application
            applicationConfiguration.registerMainFrame(mainFrame);

            // Create the object for displaying user messages and error dialogs
            GiftCloudDialogs dialogs = new GiftCloudDialogs(applicationConfiguration, mainFrame);

            // Create the general error, warning and progress callback class
            GiftCloudReporterFromApplication reporter = new GiftCloudReporterFromApplication(applicationConfiguration.getLogger(), mainFrame.getContainer(), dialogs);

            // Create a callback object for the UploaderController
            UploaderControllerCallback uploaderControllerCallback = new UploaderControllerCallback(applicationConfiguration, dialogs, mainFrame.getContainer());

            // Create the GUI-less UploaderController. This will use the UploaderControllerCallback and Reporting objects for any required output or user interaction
            final UploaderController uploaderController = new UploaderController(restClientFactory, applicationConfiguration.getProperties(), uploaderControllerCallback, reporter);

            // Create the GUI and related GUI controller code
            final UploaderGuiController uploaderGuiController = new UploaderGuiController(applicationConfiguration, uploaderController, mainFrame, dialogs, reporter);

            // Give the MainFrame a controller for the exit callback
            mainFrame.registerCloseOperationController(uploaderGuiController);

            // Start the Dicom listener and report errors to the user if encountered
            uploaderGuiController.startDicomNodeAndCheckProperties(fileList);
        }
        catch (Throwable t) {
            t.printStackTrace(System.err);
            if (logger.isPresent()) {
                logger.get().silentLogException(t, "GIFT-Cloud Uploader encountered a problem when starting.");
            }
        }
    }

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
        final List<File> fileList = new ArrayList<File>();
        if (arg.length==2) {
            fileList.add(new File(arg[1]));
        }

        new GiftCloudUploaderApp(new GiftCloudUploaderRestClientFactory(), fileList);
	}
}
