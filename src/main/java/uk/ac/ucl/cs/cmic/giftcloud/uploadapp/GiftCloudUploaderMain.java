package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ExportWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.GiftCloudUploadWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ImportWorker;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GiftCloudUploaderMain implements GiftCloudUploaderController {

	private static String propertiesFileName  = "GiftCloudUploader.properties";
    private final ResourceBundle resourceBundle;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final GiftCloudMainFrame giftCloudMainFrame;
    private final GiftCloudDialogs giftCloudDialogs;
    private final DicomNode dicomNode;
    private final GiftCloudUploader giftCloudUploader;
    private final GiftCloudUploaderPanel giftCloudUploaderPanel;
    private GiftCloudConfigurationDialog configurationDialog = null;
    private final GiftCloudReporterFromApplication reporter;
    private final QueryRetrieveController queryRetrieveController;
    private final SystemTrayController systemTrayController;

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
        giftCloudUploader = new GiftCloudUploader(restServerFactory, pendingUploadFolder, giftCloudProperties, reporter);

        dicomNode = new DicomNode(giftCloudProperties, resourceBundle.getString("DatabaseRootTitleForOriginal"), giftCloudUploader, reporter);
        dicomNode.addObserver(new DicomNodeListener());
        try {
            dicomNode.activateStorageSCP();
        } catch (DicomNode.DicomNodeStartException e) {
            System.out.println("Failed to initialise the Dicom node:" + e.getMessage());
        } catch (DicomNetworkException e) {
            System.out.println("Failed to initialise the Dicom node:" + e.getMessage());
        }


        giftCloudUploaderPanel = new GiftCloudUploaderPanel(giftCloudMainFrame.getDialog(), this, dicomNode.getSrcDatabase(), giftCloudProperties, resourceBundle, reporter);
        queryRetrieveController = new QueryRetrieveController(giftCloudUploaderPanel.getQueryRetrieveRemoteView(), giftCloudProperties, dicomNode, reporter);

        giftCloudMainFrame.addMainPanel(giftCloudUploaderPanel);

        systemTrayController = new SystemTrayController(this, resourceBundle, reporter);
        giftCloudMainFrame.addListener(systemTrayController.new MainWindowVisibilityListener());
        giftCloudUploader.getBackgroundAddToUploaderService().addListener(systemTrayController.new BackgroundAddToUploaderServiceListener());

        if (systemTrayController.isPresent()) {
            hide();
        } else {
            reporter.warnUser("A system tray icon could not be created. The GIFT-Cloud uploader will start in visible mode.");
            show();
        }

        addExistingFilesToUploadQueue(pendingUploadFolder);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Attempt to authenticate
                giftCloudUploader.tryAuthentication();
            }
        }).start();

    }

    @Override
    public void showConfigureDialog() throws IOException, DicomNode.DicomNodeStartException {
        if (configurationDialog == null || !configurationDialog.isVisible()) {
            configurationDialog = new GiftCloudConfigurationDialog(giftCloudMainFrame.getDialog(), this, giftCloudProperties, giftCloudUploader.getProjectListModel(), dicomNode, resourceBundle, giftCloudDialogs);
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
            reporter.updateStatusText("Exporting failed due to the following error: " + e);
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void runImport(String filePath, final boolean importAsReference, final Progress progress) {
        new Thread(new ImportWorker(dicomNode, filePath, progress, giftCloudProperties.acceptAnyTransferSyntax(), giftCloudUploader, importAsReference, reporter)).start();
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
            reporter.updateStatusText("Importing failed due to the following error: " + e);
            e.printStackTrace(System.err);
        } finally {
            reporter.restoreCursor();
        }
    }

    @Override
    public void tryAuthentication() {
        new Thread() {
            public void run() {

                // Attempt to authenticate
                giftCloudUploader.tryAuthentication();
            }
        }.run();



    }

    @Override
    public void restartDicomService() {
        try {
            dicomNode.shutdownStorageSCP();
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
    public void restartUploader() {
        // ToDo: this doesn't really deal with changes to the server etc
        pauseUploading();
        startUploading();
    }

    @Override
    public void importFromPacs() {
        giftCloudUploaderPanel.showQueryRetrieveDialog();
    }

    private void addExistingFilesToUploadQueue(final File pendingUploadFolder) {
        runImport(pendingUploadFolder.getAbsolutePath(), false, reporter);
    }

    private class DicomNodeListener implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            giftCloudUploaderPanel.rebuildFileList(dicomNode.getSrcDatabase());
        }
    }
}
