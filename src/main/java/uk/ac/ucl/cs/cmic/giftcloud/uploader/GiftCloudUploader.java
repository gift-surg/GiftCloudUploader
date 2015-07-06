package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.dicom.DicomException;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudAutoUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudDialogs;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.LocalWaitingForUploadDatabase;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.ProjectListModel;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.MultiUploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.MultiUploadWizard;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import javax.security.sasl.AuthenticationException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CancellationException;

public class GiftCloudUploader implements BackgroundUploader.BackgroundUploadOutcomeCallback {
    private final LocalWaitingForUploadDatabase uploadDatabase;
    private final GiftCloudProperties giftCloudProperties;
    private final UploaderStatusModel uploaderStatusModel;
    private final Container container;
    private final PendingUploadTaskList pendingUploadList;
    private final GiftCloudReporter reporter;
    private final ProjectListModel projectListModel;
    private final GiftCloudServerFactory serverFactory;
    private final BackgroundAddToUploaderService backgroundAddToUploaderService;
    private final GiftCloudAutoUploader autoUploader;
    private final BackgroundUploader backgroundUploader;

    public GiftCloudUploader(final RestServerFactory restServerFactory, final LocalWaitingForUploadDatabase uploadDatabase, final File pendingUploadFolder, final GiftCloudProperties giftCloudProperties, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporter reporter) {
        this.uploadDatabase = uploadDatabase;
        this.giftCloudProperties = giftCloudProperties;
        this.uploaderStatusModel = uploaderStatusModel;
        this.container = reporter.getContainer();
        this.reporter = reporter;
        projectListModel = new ProjectListModel(giftCloudProperties);
        serverFactory = new GiftCloudServerFactory(restServerFactory, giftCloudProperties, projectListModel, reporter);
        pendingUploadList = new PendingUploadTaskList(giftCloudProperties, pendingUploadFolder, reporter);

        final int numThreads = 1;
        backgroundUploader = new BackgroundUploader(new BackgroundCompletionServiceTaskList<CallableWithParameter<Set<String>, FileCollection>, FileCollection>(numThreads), this, uploaderStatusModel, reporter);
        autoUploader = new GiftCloudAutoUploader(backgroundUploader, giftCloudProperties, reporter);
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
     * Launches the interactive wizard for file uploading
     * This method does not throw any exceptions
     *
     * @param multiUploadParameters
     * @return true if the wizard was launched successfully
     */
    public boolean runWizard(final MultiUploadParameters multiUploadParameters) {
        String giftCloudServerUrl = "";

        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();
            giftCloudServerUrl = giftCloudServer.getGiftCloudServerUrl();

            final Dimension windowSize = new Dimension(300, 300);
            new MultiUploadWizard(giftCloudServer, giftCloudServer.getRestServer(), windowSize, multiUploadParameters, giftCloudServer.getGiftCloudServerUrl(), reporter);
            return true;

        } catch (CancellationException e) {
            reporter.silentLogException(e, "The upload wizard was cancelled. Server:" + giftCloudServerUrl + ", error:" + e.getMessage());
            // Do not report anything to user, since the user initiated the cancellation
            return false;

        } catch (AuthenticationException e) {
            reporter.silentLogException(e, "The GIFT-Cloud username or password was not recognised. Server:" + giftCloudServerUrl + ", error:" + e.getMessage());
            JOptionPane.showMessageDialog(container, "The GIFT-Cloud username or password was not recognised.", "Error", JOptionPane.DEFAULT_OPTION);
            return false;

        } catch (Exception e) {
            reporter.silentLogException(e, "An error occurred while executing the upload wizard using the GIFT-Cloud server at " + giftCloudServerUrl + ": " + e.getMessage());
            JOptionPane.showMessageDialog(container, "Could not launch the wizard due to the following error: " + e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
            return false;
        }
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

    public boolean uploadToGiftCloud(Vector<String> paths) throws IOException {

        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudServer.resetCancellation();

            final String projectName = getProjectName(giftCloudServer);

            return autoUploader.uploadToGiftCloud(giftCloudServer, paths, projectName);

        } catch (Throwable throwable) {

            return false;
        }
    }

    String getProjectName(final GiftCloudServer giftCloudServer) throws IOException {
        final Optional<String> lastProjectName = giftCloudProperties.getLastProject();
        if (lastProjectName.isPresent() && StringUtils.isNotBlank(lastProjectName.get())) {
            return lastProjectName.get();
        } else {
            try {
                final String selectedProject = GiftCloudDialogs.showInputDialogToSelectProject(giftCloudServer.getListOfProjects(), container, lastProjectName);
                giftCloudProperties.setLastProject(selectedProject);
                return selectedProject;
            } catch (IOException e) {
                throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
            }
        }
    }

    public void importFile(String dicomFileName, String fileReferenceType) throws IOException, DicomException {
        uploadDatabase.importFileIntoDatabase(dicomFileName, fileReferenceType);

        if (fileReferenceType.equals(DatabaseInformationModel.FILE_COPIED)) {
            addFileInstance(dicomFileName);
        } else if (fileReferenceType.equals(DatabaseInformationModel.FILE_REFERENCED)) {
            addFileReference(dicomFileName);
        } else {
            throw new RuntimeException("Unexpected file reference type");
        }
    }

    private void addFileReference(final String mediaFileName) {
        try {
            final Optional<String> projectName = giftCloudProperties.getLastProject();
            pendingUploadList.addFileReference(mediaFileName, projectName);

        } catch (Throwable throwable) {
            reporter.silentLogException(throwable, "Error when attempting to import a file reference to " + mediaFileName);
        }
    }

    private void addFileInstance(final String dicomFileName) {
        try {
            final Optional<String> projectName = giftCloudProperties.getLastProject();
            pendingUploadList.addFileInstance(dicomFileName, projectName);

        } catch (Throwable throwable) {
            reporter.silentLogException(throwable, "Error when attempting to import a file copy of " + dicomFileName);
        }
    }

    @Override
    public void fileUploadSuccess(final FileCollection fileCollection) {
        pendingUploadList.fileUploadSuccess(fileCollection);
        for (final File file : fileCollection.getFiles()) {
            uploadDatabase.deleteFileFromDatabase(file);
        }
    }

    @Override
    public void fileUploadFailure(final FileCollection fileCollection) {
        pendingUploadList.fileUploadFailure(fileCollection);
    }

    public void addExistingFilesToUploadQueue() {
        pendingUploadList.addExistingFiles();
    }

    public boolean requestProjectNameIfNotSet() throws IOException {

        final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();

        final Optional<String> lastProjectName = giftCloudProperties.getLastProject();
        if (lastProjectName.isPresent() && StringUtils.isNotBlank(lastProjectName.get())) {
            return true;
        } else {
            try {
                final String selectedProject = GiftCloudDialogs.showInputDialogToSelectProject(giftCloudServer.getListOfProjects(), container, lastProjectName);
                giftCloudProperties.setLastProject(selectedProject);
                return true;
            } catch (IOException e) {
                reporter.silentLogException(e, "Unable to retrieve project list due to following error: " + e.getMessage());
                return false;
            }
        }
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
}
