package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ExportWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.GiftCloudUploadWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ImportWorker;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The main controller class for the uploader
 */
public class GiftCloudUploaderMain implements GiftCloudUploaderController {

	private static String propertiesFileName  = "GiftCloudUploader.properties";
    private final ResourceBundle resourceBundle;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final GiftCloudMainFrame giftCloudMainFrame;
    private final GiftCloudDialogs giftCloudDialogs;
    private final DicomNode dicomNode;
    private final LocalWaitingForUploadDatabase uploadDatabase;
    private final GiftCloudUploader giftCloudUploader;
    private final GiftCloudUploaderPanel giftCloudUploaderPanel;
    private GiftCloudConfigurationDialog configurationDialog = null;
    private final GiftCloudReporterFromApplication reporter;
    private final QueryRetrieveController queryRetrieveController;
    private final SystemTrayController systemTrayController;
    private final UploaderStatusModel uploaderStatusModel = new UploaderStatusModel();

    public GiftCloudUploaderMain(final RestServerFactory restServerFactory, final ResourceBundle resourceBundle) throws DicomException, IOException {
        this.resourceBundle = resourceBundle;
        final GiftCloudUploaderApplicationBase applicationBase = new GiftCloudUploaderApplicationBase(propertiesFileName);

        giftCloudMainFrame = new GiftCloudMainFrame(resourceBundle.getString("applicationTitle"), this);
        giftCloudDialogs = new GiftCloudDialogs(giftCloudMainFrame);
        reporter = new GiftCloudReporterFromApplication(giftCloudMainFrame.getContainer(), giftCloudDialogs);

        // Initialise application properties
        giftCloudProperties = new GiftCloudPropertiesFromApplication(applicationBase, resourceBundle);


        // Initialise the main GIFT-Cloud class
        final File pendingUploadFolder = giftCloudProperties.getUploadFolder(reporter);

        uploadDatabase = new LocalWaitingForUploadDatabase(resourceBundle.getString("DatabaseRootTitleForOriginal"), uploaderStatusModel, reporter);
        giftCloudUploader = new GiftCloudUploader(restServerFactory, uploadDatabase, pendingUploadFolder, giftCloudProperties, uploaderStatusModel, reporter);
        uploadDatabase.addObserver(new DatabaseListener());
        dicomNode = new DicomNode(giftCloudUploader, giftCloudProperties, uploadDatabase, uploaderStatusModel, reporter);

        try {
            dicomNode.activateStorageSCP();
        } catch (DicomNode.DicomNodeStartException e) {
            reporter.silentLogException(e, "The DICOM listening node failed to start due to the following error: " + e.getLocalizedMessage());
            reporter.showError("The DICOM listening node failed to start. Please check the listener settings and restart the GIFT-Cloud Uploader.");
        } catch (DicomNetworkException e) {
            reporter.silentLogException(e, "The DICOM listening node failed to start due to the following error: " + e.getLocalizedMessage());
            reporter.showError("The DICOM listening node failed to start. Please check the listener settings and restart the GIFT-Cloud Uploader");
        }



        giftCloudUploaderPanel = new GiftCloudUploaderPanel(giftCloudMainFrame.getDialog(), this, uploadDatabase.getSrcDatabase(), giftCloudProperties, resourceBundle, uploaderStatusModel, reporter);
        queryRetrieveController = new QueryRetrieveController(giftCloudUploaderPanel.getQueryRetrieveRemoteView(), giftCloudProperties, dicomNode, uploaderStatusModel, reporter);

        giftCloudMainFrame.addMainPanel(giftCloudUploaderPanel);

        systemTrayController = new SystemTrayController(this, resourceBundle, reporter);
        giftCloudMainFrame.addListener(systemTrayController.new MainWindowVisibilityListener());
        giftCloudUploader.getBackgroundAddToUploaderService().addListener(systemTrayController.new BackgroundAddToUploaderServiceListener());

        final Optional<Boolean> hideWindowOnStartupProperty = giftCloudProperties.getHideWindowOnStartup();

        // We hide the main window only if specified in the preferences, AND if the system tray is supported
        final boolean hideMainWindow = hideWindowOnStartupProperty.isPresent() && hideWindowOnStartupProperty.get() && systemTrayController.isPresent();
        if (hideMainWindow) {
            hide();
        } else {
            show();
        }

        addExistingFilesToUploadQueue(pendingUploadFolder);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Attempt to authenticate
//                giftCloudUploader.tryAuthentication();
                startUploading();
            }
        }).start();

    }

    @Override
    public void showConfigureDialog() {
        if (configurationDialog == null || !configurationDialog.isVisible()) {
            configurationDialog = new GiftCloudConfigurationDialog(giftCloudMainFrame.getDialog(), this, giftCloudProperties, giftCloudUploader.getProjectListModel(), resourceBundle, giftCloudDialogs, reporter);
        }
    }

    @Override
    public void showAboutDialog() {
        giftCloudMainFrame.show();
        giftCloudDialogs.showMessage(resourceBundle.getString("giftCloudProductName"));
    }

    @Override
    public void hide() {
        giftCloudMainFrame.hide();
    }

    @Override
    public void show() {
        giftCloudMainFrame.show();
    }

    @Override
    public void startUploading() {
        giftCloudUploader.setUploadServiceRunningState(true);
    }

    @Override
    public void pauseUploading() {
        giftCloudUploader.setUploadServiceRunningState(false);
    }

    @Override
    public void upload(Vector<String> filePaths) {
        try {
            Thread activeThread = new Thread(new GiftCloudUploadWorker(filePaths, giftCloudUploader, reporter));
            activeThread.start();
        } catch (Exception e) {
            reporter.reportErrorToUser("Uploading to GIFT-Cloud failed due to the following error:", e);
        }
    }

    @Override
    public void retrieve(List<QuerySelection> currentRemoteQuerySelectionList) {
        try {
            queryRetrieveController.retrieve(currentRemoteQuerySelectionList);
        } catch (Exception e) {
            reporter.reportErrorToUser("The DICOM retrieve operation failed due to the following error:", e);
        }
    }

    @Override
    public void query(final QueryParams queryParams) {
        try {
            queryRetrieveController.query(queryParams);
        } catch (Exception e) {
            reporter.reportErrorToUser("The DICOM query operation failed due to the following error:", e);
        }
    }

    @Override
    public void export(String exportDirectory, Vector<String> filesToExport) {
        File exportDirectoryFile = new File(exportDirectory);
        new Thread(new ExportWorker(filesToExport, exportDirectoryFile, giftCloudProperties.hierarchicalExport(), giftCloudProperties.zipExport(), reporter)).start();
    }

    @Override
    public void selectAndExport(final Vector<String> filesToExport) {
        try {
            reporter.showMesageLogger();

            Optional<String> selectDirectory = giftCloudDialogs.selectDirectory(giftCloudProperties.getLastExportDirectory());

            if (selectDirectory.isPresent()) {
                giftCloudProperties.setLastExportDirectory(selectDirectory.get());
                export(selectDirectory.get(), filesToExport);
            }
        } catch (Exception e) {
            reporter.reportErrorToUser("Exporting failed due to the following error: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void runImport(String filePath, final boolean importAsReference, final Progress progress) {
        new Thread(new ImportWorker(uploadDatabase, filePath, progress, giftCloudProperties.acceptAnyTransferSyntax(), giftCloudUploader, importAsReference, uploaderStatusModel, reporter)).start();
    }

    @Override
    public void selectAndImport() {
        try {
            reporter.setWaitCursor();
            reporter.showMesageLogger();

            Optional<GiftCloudDialogs.SelectedPathAndFile> selectFileOrDirectory = giftCloudDialogs.selectFileOrDirectory(giftCloudProperties.getLastImportDirectory());

            if (selectFileOrDirectory.isPresent()) {
                giftCloudProperties.setLastImportDirectory(selectFileOrDirectory.get().getSelectedPath());
                String filePath = selectFileOrDirectory.get().getSelectedFile();
                runImport(filePath, true, reporter);
            }
        } catch (Exception e) {
            reporter.reportErrorToUser("Exporting failed due to the following error: " + e.getLocalizedMessage(), e);
        } finally {
            reporter.restoreCursor();
        }
    }

    @Override
    public void restartDicomService() {
        try {
            dicomNode.shutdownStorageSCPAndWait(giftCloudProperties.getShutdownTimeoutMs());
        } catch (Exception e) {
            reporter.silentLogException(e, "Failed to shutdown the dicom node service");
        }
        try {
            dicomNode.activateStorageSCP();
        } catch (Exception e) {
            reporter.silentLogException(e, "Failed to startup the dicom node service");
        }
    }

    @Override
    public void invalidateServerAndRestartUploader() {
        pauseUploading();
        giftCloudUploader.invalidateServer();
        startUploading();
    }

    @Override
    public void importFromPacs() {
        giftCloudUploaderPanel.showQueryRetrieveDialog();
    }

    @Override
    public void refreshFileList() {
        giftCloudUploaderPanel.rebuildFileList(uploadDatabase.getSrcDatabase());
    }

    @Override
    public void exportPatientList() {
        giftCloudUploader.exportPatientList();
    }

    private void addExistingFilesToUploadQueue(final File pendingUploadFolder) {
        runImport(pendingUploadFolder.getAbsolutePath(), false, reporter);
    }

    private class DatabaseListener implements Observer, Runnable {
        private boolean updateIsPending = false;
        private Thread thread = null;

        public DatabaseListener() {
            // ShutdownHook will run regardless of whether Command-Q (on Mac) or window closed ...
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    cancelThread();
                }
            });

        }

        private synchronized void cancelThread() {
            if (thread != null) {
                try {
                    thread.interrupt();
                } catch (Throwable t) {
                }
            }
        }

        @Override
        public void update(Observable o, Object arg) {
            signalUpdateRequired();
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                resetUpdateStatus();
                doUpdate();
            } catch (InterruptedException e) {
                resetUpdateStatus();
            }
        }

        private void doUpdate() {
            giftCloudUploaderPanel.rebuildFileList(uploadDatabase.getSrcDatabase());
        }

        private synchronized void signalUpdateRequired() {
            if (!updateIsPending) {
                updateIsPending = true;
                thread = new Thread(this);
                thread.start();
            }
        }

        private synchronized void resetUpdateStatus() {
            updateIsPending = false;
        }
    }
}
