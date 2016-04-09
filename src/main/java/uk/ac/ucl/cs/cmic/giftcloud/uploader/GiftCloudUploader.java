package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.DicomException;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.security.sasl.AuthenticationException;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CancellationException;

public class GiftCloudUploader implements BackgroundUploader.BackgroundUploadOutcomeCallback {
    private final WaitingForUploadDatabase uploadDatabase;
    private final GiftCloudProperties giftCloudProperties;
    private GiftCloudDialogs dialogs;
    private final Container container;
    private final PendingUploadTaskList pendingUploadList;
    private final GiftCloudReporter reporter;
    private final ProjectListModel projectListModel;
    private final GiftCloudServerFactory serverFactory;
    private final BackgroundAddToUploaderService backgroundAddToUploaderService;
    private final AutoUploader autoUploader;
    private final BackgroundUploader backgroundUploader;

    private final int DELAY_BETWEEN_UPDATES = 500;

    public GiftCloudUploader(final PixelDataAnonymiserFilterCache filters, final RestServerFactory restServerFactory, final GiftCloudProperties giftCloudProperties, final UploaderStatusModel uploaderStatusModel, final GiftCloudDialogs dialogs, final GiftCloudReporter reporter) {
        this.uploadDatabase =  new WaitingForUploadDatabase(DELAY_BETWEEN_UPDATES);
        this.giftCloudProperties = giftCloudProperties;
        this.dialogs = dialogs;
        this.container = reporter.getContainer();
        this.reporter = reporter;
        projectListModel = new ProjectListModel(giftCloudProperties);
        serverFactory = new GiftCloudServerFactory(filters, restServerFactory, giftCloudProperties, projectListModel, reporter);
        pendingUploadList = new PendingUploadTaskList(reporter);

        final int numThreads = 1;
        backgroundUploader = new BackgroundUploader(new BackgroundCompletionServiceTaskList<CallableWithParameter<Set<String>, FileCollection>, FileCollection>(numThreads), this, uploaderStatusModel, reporter);
        autoUploader = new AutoUploader(backgroundUploader, giftCloudProperties, reporter);
        backgroundAddToUploaderService = new BackgroundAddToUploaderService(pendingUploadList, serverFactory, this, autoUploader, uploaderStatusModel, reporter);

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

    String getProjectName(final GiftCloudServer giftCloudServer) throws IOException {
        final Optional<String> lastProjectName = giftCloudProperties.getLastProject();
        if (lastProjectName.isPresent() && StringUtils.isNotBlank(lastProjectName.get())) {
            return lastProjectName.get();
        } else {
            try {
                final String selectedProject = dialogs.showInputDialogToSelectProject(giftCloudServer.getListOfProjects(), container, lastProjectName);
                giftCloudProperties.setLastProject(selectedProject);
                giftCloudProperties.save();
                return selectedProject;
            } catch (IOException e) {
                throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
            }
        }
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
}
