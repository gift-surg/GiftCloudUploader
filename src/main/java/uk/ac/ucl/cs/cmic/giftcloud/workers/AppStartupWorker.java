package uk.ac.ucl.cs.cmic.giftcloud.workers;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderAppConfiguration;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploaderGuiController;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderController;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A worker for checking the app properties, alerting the user to potential issues, and starting the Dicom listener and uploading services. These should be carried out in the correct order to avoid multiple failures being presented to the user
 */
public class AppStartupWorker implements Runnable {
    private GiftCloudUploaderAppConfiguration appConfiguration;
    private UploaderGuiController guiController;
    private UploaderController uploaderController;
    private boolean wait;
    private List<File> filesToImport;
    private final GiftCloudReporter reporter;

    /**
     * @param appConfiguration
     * @param guiController
     * @param uploaderController
     * @param filesToImport
     * @param reporter
     */
    public AppStartupWorker(final GiftCloudUploaderAppConfiguration appConfiguration, final UploaderGuiController guiController, final UploaderController uploaderController, final List<File> filesToImport, final GiftCloudReporter reporter) {
        this.appConfiguration = appConfiguration;
        this.guiController = guiController;
        this.uploaderController = uploaderController;
        this.filesToImport = filesToImport;
        this.reporter = reporter;
    }

    public void run() {
        Optional<Throwable> dicomNodeFailureException = Optional.empty();
        try {
            uploaderController.startDicomListener();
        } catch (Throwable e) {
            dicomNodeFailureException = Optional.of(e);
            reporter.silentLogException(e, appConfiguration.getResourceBundle().getString("dicomNodeFailureMessageWithDetails") + e.getLocalizedMessage());
        }

        // Add any leftover files from the last session to the upload queue
        uploaderController.importPendingFiles();

        // Add any files specified in the startup parameters to the upload queue
        if (!filesToImport.isEmpty()) {
            uploaderController.runImport(filesToImport, true, reporter);
        }

        // We check whether the main properties have been set. If not, we warn the user and bring up the configuration dialog. We suppress the Dicom node start failure in this case, as we assume the lack of properties is responsible
        final Optional<String> propertiesNotConfigured = checkProperties();
        if (propertiesNotConfigured.isPresent()) {
            reporter.showMessageToUser(propertiesNotConfigured.get());

            // This call will block until the user has had a chance to correct errors; otherwise it is likely the startUploading() call would fail
            guiController.showConfigureDialog(true);

        } else {
            // If the properties have been set but the Dicom node still fails to start, then we report this to the user.
            if (dicomNodeFailureException.isPresent()) {
                reporter.reportErrorToUser(appConfiguration.getResourceBundle().getString("dicomNodeFailureMessage"), dicomNodeFailureException.get());

                // Do not block here; while there has been a failure in the DicomListener, the Uploader might still be able to import and upload files
                guiController.showConfigureDialog(false);
            }
        }

        // Initiate the process that moves files from the uploading queue to the uploading process
        uploaderController.startUploading();
    }

    private Optional<String> checkProperties() {
        final List<String> toBeSet = new ArrayList<String>();
        final GiftCloudProperties giftCloudProperties = appConfiguration.getProperties();
        if (!giftCloudProperties.getGiftCloudUrl().isPresent()) {
            toBeSet.add("server URL");
        }
        if (!giftCloudProperties.getLastUserName().isPresent()) {
            toBeSet.add("username");
        }
        if (!giftCloudProperties.getLastPassword().isPresent()) {
            toBeSet.add("password");
        }

        final int numMessages = toBeSet.size();

        if (numMessages > 0) {
            String message = "Please set the GIFT-Cloud " + toBeSet.get(0);

            if (numMessages > 1) {
                for (int index = 1; index < toBeSet.size() - 1; index++) {
                    message = message + ", " + toBeSet.get(index);
                }
                message = message + " and " + toBeSet.get(numMessages - 1);
            }
            message = message + " in the Settings dialog";
            return Optional.of(message);
        } else {
            return Optional.empty();
        }
    }

}
