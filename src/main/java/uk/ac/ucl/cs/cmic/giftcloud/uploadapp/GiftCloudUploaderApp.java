package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderController;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GiftCloudUploaderApp {

	public GiftCloudUploaderApp(final RestServerFactory restServerFactory, List<File> fileList) {
	    Optional<GiftCloudLogger> logger = Optional.empty();
        try {
            // Create and configure the application. This must be done before the main frame is created
            final GiftCloudUploaderAppConfiguration applicationConfiguration = new GiftCloudUploaderAppConfiguration();
            logger = Optional.of(applicationConfiguration.getLogger());

            // Create the main gui component
            final GiftCloudMainFrame mainFrame = new GiftCloudMainFrame(applicationConfiguration);

            // Tell the applicationConfiguration about the main frame. This will allow the singleton processing to show and bring the window to the front if a second attempt is made to instantiate the application
            applicationConfiguration.registerMainFrame(mainFrame);

            // Create the object for displaying user messages and error dialogs
            GiftCloudDialogs dialogs = new GiftCloudDialogs(applicationConfiguration, mainFrame);

            // Create the general error, warning and progress callback class
            GiftCloudReporterFromApplication reporter = new GiftCloudReporterFromApplication(applicationConfiguration.getLogger(), mainFrame.getContainer(), dialogs);

            // Create a callback object for the UploaderController
            UploaderControllerCallback uploaderControllerCallback = new UploaderControllerCallback(applicationConfiguration, dialogs, mainFrame.getContainer());

            // Create the GUI-less UploaderController. This will use the UploaderControllerCallback and Reporting objects for any required output or user interaction
            final UploaderController uploaderController = new UploaderController(restServerFactory, applicationConfiguration.getProperties(), uploaderControllerCallback, reporter);

            //
            final UploaderGuiController uploaderMain = new UploaderGuiController(applicationConfiguration, uploaderController, mainFrame, dialogs, reporter);

            // Add any leftover files from the last session to the upload queue
            uploaderController.importPendingFiles();

            // Add any specified files to the upload queue
            if (!fileList.isEmpty()) {
                uploaderController.runImport(fileList, true, reporter);
            }

            // Start the uploading thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploaderController.startUploading();
                }
            }).start();

            // Start the Dicom listener and report errors to the user if found
            uploaderMain.startDicomNodeAndCheckProperties(false, fileList);
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

        new GiftCloudUploaderApp(new GiftCloudUploaderRestServerFactory(), fileList);
	}
}
