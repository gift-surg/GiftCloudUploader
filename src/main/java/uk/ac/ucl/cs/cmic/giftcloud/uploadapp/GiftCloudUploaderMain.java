package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ExportWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.GiftCloudUploadWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ImportWorker;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import java.awt.*;
import java.awt.List;
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
    private Optional<SingleInstanceService> singleInstanceService;

    public GiftCloudUploaderMain(final RestServerFactory restServerFactory, final ResourceBundle resourceBundle) throws DicomException, IOException {
        this.resourceBundle = resourceBundle;

        try {
            singleInstanceService = Optional.of((SingleInstanceService)ServiceManager.lookup("javax.jnlp.SingleInstanceService"));
            GiftCloudUploaderSingleInstanceListener singleInstanceListener = new GiftCloudUploaderSingleInstanceListener();
            singleInstanceService.get().addSingleInstanceListener(singleInstanceListener);
        } catch (UnavailableServiceException e) {
            singleInstanceService = Optional.empty();
        }


        setSystemLookAndFeel();

        giftCloudMainFrame = new GiftCloudMainFrame(resourceBundle.getString("applicationTitle"), this);
        giftCloudDialogs = new GiftCloudDialogs(giftCloudMainFrame);
        reporter = new GiftCloudReporterFromApplication(giftCloudMainFrame.getContainer(), giftCloudDialogs);

        // Initialise application properties
        giftCloudProperties = new GiftCloudPropertiesFromApplication(new PropertyStoreFromApplication(propertiesFileName, reporter), resourceBundle, reporter);


        // Initialise the main GIFT-Cloud class
        final File pendingUploadFolder = giftCloudProperties.getUploadFolder(reporter);

        uploadDatabase = new LocalWaitingForUploadDatabase(resourceBundle.getString("DatabaseRootTitleForOriginal"), uploaderStatusModel, reporter);
        giftCloudUploader = new GiftCloudUploader(restServerFactory, uploadDatabase, pendingUploadFolder, giftCloudProperties, uploaderStatusModel, reporter);
        uploadDatabase.addObserver(new DatabaseListener());
        dicomNode = new DicomNode(giftCloudUploader, giftCloudProperties, uploadDatabase, uploaderStatusModel, reporter);

        giftCloudUploaderPanel = new GiftCloudUploaderPanel(giftCloudMainFrame.getDialog(), this, uploadDatabase.getSrcDatabase(), giftCloudProperties, resourceBundle, uploaderStatusModel, reporter);
        queryRetrieveController = new QueryRetrieveController(giftCloudUploaderPanel.getQueryRetrieveRemoteView(), giftCloudProperties, dicomNode, uploaderStatusModel, reporter);

        giftCloudMainFrame.addMainPanel(giftCloudUploaderPanel);

        systemTrayController = new SystemTrayController(this, resourceBundle, reporter);
        giftCloudMainFrame.addListener(systemTrayController.new MainWindowVisibilityListener());
        giftCloudUploader.getBackgroundAddToUploaderService().addListener(systemTrayController.new BackgroundAddToUploaderServiceListener());


        Optional<Throwable> dicomNodeFailureException = Optional.empty();
        try {
            dicomNode.activateStorageSCP();
        } catch (Throwable e) {
            dicomNodeFailureException = Optional.of(e);
            reporter.silentLogException(e, "The DICOM listening node failed to start due to the following error: " + e.getLocalizedMessage());
        }




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

        // We check whether the main properties have been set. If not, we warn the user and bring up the configuration dialog. We suppress the Dicom node start failure in this case, as we assume the lack of properties is responsible
        final Optional<String> propertiesNotConfigured = checkProperties();
        if (propertiesNotConfigured.isPresent()) {
            reporter.showMessageToUser(propertiesNotConfigured.get());
            showConfigureDialog();

        } else {
            // If the properties have been set but the Dicom node still fails to start, then we report this to the user.
            if (dicomNodeFailureException.isPresent()) {
                reporter.reportErrorToUser("The DICOM listening node failed to start. Please check the listener settings and restart the listener.", dicomNodeFailureException.get());
                showConfigureDialog();
            }
        }
    }

    private Optional<String> checkProperties() {
        final List<String> toBeSet = new ArrayList<String>();

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

    private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background", Color.white);
            UIManager.put("CheckBox.background", Color.lightGray);
            UIManager.put("SplitPane.background", Color.white);
            UIManager.put("OptionPane.background", Color.white);
            UIManager.put("Panel.background", Color.white);

            Font font = new Font("Arial Unicode MS",Font.PLAIN,12);
            if (font != null) {
                UIManager.put("Tree.font", font);
                UIManager.put("Table.font", font);
            }
        } catch (Throwable t) {
            reporter.silentLogException(t, "Error when setting the system look and feel");
        }

    }

    @Override
    public void showConfigureDialog() {
        if (configurationDialog == null || !configurationDialog.isVisible()) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    configurationDialog = new GiftCloudConfigurationDialog(giftCloudMainFrame.getDialog(), GiftCloudUploaderMain.this, giftCloudProperties, giftCloudUploader.getProjectListModel(), resourceBundle, giftCloudDialogs, reporter);
                }
            });
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
                giftCloudProperties.save();
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
                giftCloudProperties.save();
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
        } catch (Throwable e) {
            reporter.silentLogException(e, "Failed to shutdown the dicom node service");
        }
        try {
            dicomNode.activateStorageSCP();
        } catch (Throwable e) {
            reporter.silentLogException(e, "The DICOM listening node failed to start due to the following error: " + e.getLocalizedMessage());
            reporter.showError("The DICOM listening node failed to start. Please check the listener settings and restart the listener.");
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

    private class GiftCloudUploaderSingleInstanceListener implements SingleInstanceListener {

        public GiftCloudUploaderSingleInstanceListener() {

            // Add a shutdown hook to unregister the single instance
            // ShutdownHook will run regardless of whether Command-Q (on Mac) or window closed ...
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (singleInstanceService.isPresent()) {
                        singleInstanceService.get().removeSingleInstanceListener(GiftCloudUploaderSingleInstanceListener.this);
                    }
                }
            });

        }
        @Override
        public void newActivation(String[] strings) {
            show();
        }
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
