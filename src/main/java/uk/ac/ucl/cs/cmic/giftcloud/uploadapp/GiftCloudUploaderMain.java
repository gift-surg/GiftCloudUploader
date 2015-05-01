package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.DicomNetworkException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ExportWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.GiftCloudAppendUploadWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.GiftCloudUploadWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ImportWorker;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GiftCloudUploaderMain implements GiftCloudUploaderController {

	private static String propertiesFileName  = ".com.pixelmed.display.GiftCloudUploader.properties";
    private final ResourceBundle resourceBundle;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final GiftCloudMainFrame giftCloudMainFrame;
    private final GiftCloudDialogs giftCloudDialogs;
    private final DicomNode dicomNode;
    private final GiftCloudUploader giftCloudUploader;
    private final GiftCloudUploaderPanel giftCloudUploaderPanel;
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
        giftCloudUploader = new GiftCloudUploader(restServerFactory, giftCloudProperties, reporter);
        giftCloudUploader.addExistingFilesToUploadQueue();

        dicomNode = new DicomNode(giftCloudProperties, resourceBundle.getString("DatabaseRootTitleForOriginal"), giftCloudUploader, reporter);
        dicomNode.addObserver(new DicomNodeListener());
        try {
            dicomNode.activateStorageSCP();
        } catch (DicomNode.DicomNodeStartException e) {
            System.out.println("Failed to initialise the Dicom node:" + e.getMessage());
        }


        // Attempt to authenticate
        giftCloudUploader.tryAuthentication();

        giftCloudUploaderPanel = new GiftCloudUploaderPanel(this, giftCloudUploader.getProjectListModel(), dicomNode.getSrcDatabase(), giftCloudProperties, resourceBundle, reporter);
        queryRetrieveController = new QueryRetrieveController(giftCloudUploaderPanel.getQueryRetrievePanel(), giftCloudProperties, dicomNode, reporter);

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
    }

    @Override
    public void showConfigureDialog() throws IOException, DicomNode.DicomNodeStartException {
        dicomNode.shutdownStorageSCP();
        try {
            new NetworkApplicationConfigurationDialog(giftCloudMainFrame.getContainer(), dicomNode.getNetworkApplicationInformation(), giftCloudProperties, giftCloudDialogs);
        } catch (DicomNetworkException e) {
            throw new IOException("Failed to create configuration dialog due to error: " + e.getCause());
        }
        // should now save properties to file
        giftCloudProperties.updatePropertiesWithNetworkProperties();
        giftCloudProperties.storeProperties("Edited and saved from user interface");
        dicomNode.activateStorageSCP();
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
    public void runImport(String filePath, final Progress progress) {
        new Thread(new ImportWorker(dicomNode, filePath, progress, giftCloudProperties.acceptAnyTransferSyntax(), giftCloudUploader, reporter)).start();
    }

    @Override
    public void selectAndImport() {
        try {
            reporter.showMesageLogger();

            Optional<GiftCloudDialogs.SelectedPathAndFile> selectFileOrDirectory = giftCloudDialogs.selectFileOrDirectory(giftCloudProperties.getLastImportDirectory());

            if (selectFileOrDirectory.isPresent()) {
                giftCloudProperties.setLastImportDirectory(selectFileOrDirectory.get().getSelectedPath());
                String filePath = selectFileOrDirectory.get().getSelectedFile();
                runImport(filePath, reporter);
            }
        } catch (Exception e) {
            reporter.updateStatusText("Importing failed due to the following error: " + e);
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void quit() {
        try {
            dicomNode.shutdownStorageSCP();
            final long maxWaitTimeMs = 60000;
            queryRetrieveController.waitForCompletion(maxWaitTimeMs);
            giftCloudUploader.waitForCompletion(maxWaitTimeMs);
            systemTrayController.remove();
        } finally {
            System.exit(0);
        }
    }

    private class DicomNodeListener implements Observer {

        private GiftCloudAppendUploadActionListener appendListener = new GiftCloudAppendUploadActionListener();

        @Override
        public void update(Observable o, Object arg) {
            final String newFileName = (String)arg;
            giftCloudUploaderPanel.rebuildFileList(dicomNode.getSrcDatabase());
            appendListener.filesChanged(newFileName);
        }
    }


    protected class GiftCloudAppendUploadActionListener implements FileUploadSuccessCallback {

        private Vector<String> filesAlreadyUploaded = new Vector<String>();
        private Vector<String> failedUploads = new Vector<String>();


        @Override
        public void addFailedUpload(final String fileName) {
            failedUploads.add(fileName);
        }

        public void filesChanged(final String fileName) {
            try {
                if (filesAlreadyUploaded.contains(fileName) && !failedUploads.contains(fileName)) {
                    // Do not upload, because file has already been uploaded
                } else {
                    Vector<String> filesToUpload = new Vector<String>();
                    filesToUpload.add(fileName);

                    // ToDo: this is not threadsafe!
                    Thread activeThread = new Thread(new GiftCloudAppendUploadWorker(filesToUpload, giftCloudUploader, this, reporter));
                    activeThread.start();
                    filesAlreadyUploaded.add(fileName);
                }
            }
            catch (Exception e) {
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("GIFT-Cloud upload failed: "+e));
                e.printStackTrace(System.err);
            }
        }
    }

}
