package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.ProjectListModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.security.sasl.AuthenticationException;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CancellationException;

public class GiftCloudUploader implements BackgroundUploader.BackgroundUploadOutcomeCallback {
    private final WaitingForUploadDatabase uploadDatabase;
    private final GiftCloudProperties giftCloudProperties;
    private final PendingUploadTaskList pendingUploadList;
    private final GiftCloudReporter reporter;
    private final ProjectListModel projectListModel;
    private final GiftCloudServerFactory serverFactory;
    private final BackgroundAddToUploaderService backgroundAddToUploaderService;
    private final AutoUploader autoUploader;
    private final BackgroundUploader backgroundUploader;
    private final PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache;

    private final int DELAY_BETWEEN_UPDATES = 500;

    public GiftCloudUploader(final RestServerFactory restServerFactory, final GiftCloudProperties giftCloudProperties, final UploaderStatusModel uploaderStatusModel, final UserCallback userCallback, final GiftCloudReporter reporter) {
        this.uploadDatabase =  new WaitingForUploadDatabase(DELAY_BETWEEN_UPDATES);
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;
        pixelDataAnonymiserFilterCache = new PixelDataAnonymiserFilterCache(giftCloudProperties, reporter);
        projectListModel = new ProjectListModel(giftCloudProperties);
        serverFactory = new GiftCloudServerFactory(pixelDataAnonymiserFilterCache, restServerFactory, giftCloudProperties, projectListModel, userCallback, reporter);
        pendingUploadList = new PendingUploadTaskList(reporter);

        final int numThreads = 1;
        backgroundUploader = new BackgroundUploader(new BackgroundCompletionServiceTaskList<CallableWithParameter<Set<String>, FileCollection>, FileCollection>(numThreads), this, uploaderStatusModel, reporter);
        autoUploader = new AutoUploader(serverFactory, backgroundUploader, giftCloudProperties, userCallback, reporter);
        backgroundAddToUploaderService = new BackgroundAddToUploaderService(pendingUploadList, autoUploader, uploaderStatusModel, reporter);

        // Add a shutdown hook for graceful exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                cleanup(giftCloudProperties.getShutdownTimeoutMs());
            }
        });

        backgroundUploader.start();
    }

    public void setUploadServiceRunningState(final boolean start) {
        if (start) {
            backgroundAddToUploaderService.start();
        } else {
            backgroundAddToUploaderService.stop();
        }
    }

    public BackgroundAddToUploaderService getBackgroundAddToUploaderService() {
        return backgroundAddToUploaderService;
    }

    /**
     * Attempt to connect to the GIFT-Cloud server and initiate a session
     * This method does not throw any exceptions
     *
     * @return true if the authentication was successful
     */
    public boolean tryAuthentication() {
        // We can get the URL form the GiftCloudServer object when it has been created, but if the creation fails we still want to report the URL to the user, so fetch it from the properties
        final Optional<String> optionalGiftCloudUrlForDebugging = giftCloudProperties.getGiftCloudUrl();
        String giftCloudServerUrlForDebugging = optionalGiftCloudUrlForDebugging.orElse("");

        // We attempt to connect to the GIFT-Cloud server, in order to authenticate and to set the project list, but we allow the connection to fail gracefully
        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();
            giftCloudServerUrlForDebugging = giftCloudServer.getGiftCloudServerUrl();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudServer.resetCancellation();

            serverFactory.getGiftCloudServer().tryAuthentication();
            return true;

        } catch (CancellationException e) {
            reporter.silentLogException(e, "Authentication was cancelled. Server:" + giftCloudServerUrlForDebugging + ", error:" + e.getMessage());
            // Do not report anything to user, since the user initiated the cancellation
            return false;

        } catch (AuthenticationException e) {
            reporter.silentLogException(e, "The GIFT-Cloud username or password was not recognised. Server:" + giftCloudServerUrlForDebugging + ", error:" + e.getMessage());
            reporter.reportErrorToUser("The GIFT-Cloud username or password was not recognised.", e);
            return false;

        } catch (Exception e) {
            reporter.silentLogException(e, "An error occurred when attempting to connect to the GIFT-Cloud server at " + giftCloudServerUrlForDebugging + ": " + e.getMessage());
            reporter.reportErrorToUser("Could not connect to the GIFT-Cloud server due to the following error: <br>" + e.getMessage(), e);
            return false;
        }
    }

    public ProjectListModel getProjectListModel() {
        return projectListModel;
    }


    public void importFiles(final FileImportRecord fileImportRecord) throws IOException, DicomException {
        try {
            uploadDatabase.addFiles(fileImportRecord);
            pendingUploadList.addFiles(giftCloudProperties.getLastProject(), fileImportRecord);

        } catch (Throwable throwable) {
            reporter.silentLogException(throwable, "Error when attempting to import files " + fileImportRecord.getFilenames());
        }
    }

    @Override
    public void fileUploadSuccess(final FileCollection fileCollection) {
        pendingUploadList.fileUploadSuccess(fileCollection);
        for (final File file : fileCollection.getFiles()) {
            uploadDatabase.removeAndDeleteCopies(file.getPath());
        }
    }

    @Override
    public void fileUploadFailure(final FileCollection fileCollection) {
        pendingUploadList.fileUploadFailure(fileCollection);
    }

    /**
     * Force saving of the patient list
     */
    public void exportPatientList() {
        autoUploader.exportPatientList();
    }

    private void cleanup(final long maxWaitTimeMs) {
        backgroundAddToUploaderService.stop();
        backgroundAddToUploaderService.waitForThreadCompletion(maxWaitTimeMs);
        backgroundUploader.stop();
        backgroundUploader.waitForThreadCompletion(maxWaitTimeMs);
    }

    public void invalidateServer() {
        serverFactory.invalidate();
    }

    public TableModel getTableModel() {
        return uploadDatabase.getTableModel();
    }

    /**
     * @return the object containing pixel data anonymisation filters
     */
    public PixelDataAnonymiserFilterCache getPixelDataAnonymiserFilterCache() {
        return pixelDataAnonymiserFilterCache;
    }
}

