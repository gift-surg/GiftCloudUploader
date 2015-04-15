package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.NetworkApplicationProperties;
import com.pixelmed.network.PresentationAddress;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.StudyRootQueryInformationModel;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.workers.*;

import javax.swing.*;
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
    private GiftCloudUploader giftCloudUploader = null;
    private final GiftCloudUploaderPanel giftCloudUploaderPanel;
    private final GiftCloudReporter reporter;
    private final Optional<GiftCloudSystemTray> giftCloudSystemTray;
    private QueryInformationModel currentRemoteQueryInformationModel;

    public GiftCloudUploaderMain(ResourceBundle resourceBundle) throws DicomException, IOException {
        this.resourceBundle = resourceBundle;

        final GiftCloudUploaderApplicationBase applicationBase = new GiftCloudUploaderApplicationBase(propertiesFileName);

        giftCloudMainFrame = new GiftCloudMainFrame(resourceBundle.getString("applicationTitle"), this);
        giftCloudDialogs = new GiftCloudDialogs(giftCloudMainFrame);

        final String buildDate = applicationBase.getBuildDateFromApplicationBase();
        JLabel statusBar = applicationBase.getStatusBarFromApplicationBase();

        giftCloudProperties = new GiftCloudPropertiesFromApplication(applicationBase);

        dicomNode = new DicomNode(giftCloudProperties, resourceBundle.getString("DatabaseRootTitleForOriginal"));
        dicomNode.addObserver(new DicomNodeListener());
        try {
            dicomNode.activateStorageSCP();
        } catch (DicomNode.DicomNodeStartException e) {
            System.out.println("Failed to initialise the Dicom node:" + e.getMessage());
        }

        reporter = new GiftCloudReporter(giftCloudMainFrame.getContainer(), giftCloudDialogs);

        // Initialise the main GIFT-Cloud class
        giftCloudUploader = new GiftCloudUploader(giftCloudProperties, giftCloudMainFrame.getContainer(), reporter);

        // Attempt to authenticate
        giftCloudUploader.tryAuthentication();

        giftCloudUploaderPanel = new GiftCloudUploaderPanel(this, giftCloudUploader.getProjectListModel(), dicomNode.getSrcDatabase(), giftCloudProperties, resourceBundle, giftCloudDialogs, buildDate, statusBar, reporter);

        giftCloudMainFrame.addMainPanel(giftCloudUploaderPanel);

        // Try to create a system tray icon. If this fails, then we warn the user and make the main dialog visible
        giftCloudSystemTray = GiftCloudSystemTray.safeCreateSystemTray(this, resourceBundle, reporter);
        if (giftCloudSystemTray.isPresent()) {
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
        if (giftCloudSystemTray.isPresent()) {
            giftCloudSystemTray.get().updateMenu(GiftCloudMainFrame.MainWindowVisibility.HIDDEN);
        }
    }

    @Override
    public void show() {
        giftCloudMainFrame.show();
        if (giftCloudSystemTray.isPresent()) {
            giftCloudSystemTray.get().updateMenu(GiftCloudMainFrame.MainWindowVisibility.VISIBLE);
        }
    }

    @Override
    public void upload(Vector<String> filePaths) {
        try {
            Thread activeThread = new Thread(new GiftCloudUploadWorker(filePaths, giftCloudUploader, reporter));
            activeThread.start();
        } catch (Exception e) {
            reporter.updateProgress("GIFT-Cloud upload failed: " + e);
            reporter.error("GIFT-Cloud upload failed: " + e);
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void retrieve(List<QuerySelection> currentRemoteQuerySelectionList) {
        Thread activeThread = new Thread(new RetrieveWorker(currentRemoteQuerySelectionList, currentRemoteQueryInformationModel, dicomNode, reporter));
        activeThread.start();
        // ToDo: Cache active thread so we can provide a cancel option
    }

    @Override
    public void query(QueryParams queryParams) {
        //new QueryRetrieveDialog("GiftCloudUploaderPanel Query",400,512);
        String ae = giftCloudProperties.getCurrentlySelectedQueryTargetAE();
        if (ae != null) {
            setCurrentRemoteQueryInformationModel(ae);
            if (currentRemoteQueryInformationModel == null) {
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Cannot query "+ae));
            }
            else {
                try {
                    AttributeList filter = queryParams.build();
                    Thread activeThread = new Thread(new QueryWorker(giftCloudUploaderPanel, currentRemoteQueryInformationModel, filter, dicomNode, reporter));
                    activeThread.start();
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                    reporter.updateProgress("Query to " + ae + " failed");
//                        ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Query to "+ae+" failed"));
                }
            }
        }


    }

    @Override
    public void export(String exportDirectory, Vector<String> filesToExport) {
        File exportDirectoryFile = new File(exportDirectory);
        new Thread(new ExportWorker(filesToExport, exportDirectoryFile, giftCloudProperties.hierarchicalExport(), giftCloudProperties.zipExport(), reporter)).start();
    }

    @Override
    public void runImport(String filePath, final Progress progress) {
        new Thread(new ImportWorker(dicomNode, filePath, progress, giftCloudProperties.acceptAnyTransferSyntax(), reporter)).start();
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
            reporter.updateProgress("Importing failed due to the following error: " + e);
            e.printStackTrace(System.err);
        }
    }

    protected void setCurrentRemoteQueryInformationModel(String remoteAEForQuery) {
        currentRemoteQueryInformationModel=null;
        String stringForTitle="";
        if (remoteAEForQuery != null && remoteAEForQuery.length() > 0 && giftCloudProperties.areNetworkPropertiesValid() && dicomNode.isNetworkApplicationInformationValid()) {
            try {
                String              queryCallingAETitle = giftCloudProperties.getCallingAETitle();
                String               queryCalledAETitle = dicomNode.getApplicationEntityTitleFromLocalName(remoteAEForQuery);
                PresentationAddress presentationAddress = dicomNode.getPresentationAddress(queryCalledAETitle);

                if (presentationAddress == null) {
                    throw new Exception("For remote query AE <"+remoteAEForQuery+">, presentationAddress cannot be determined");
                }

                String                        queryHost = presentationAddress.getHostname();
                int			      queryPort = presentationAddress.getPort();
                String                       queryModel = dicomNode.getQueryModel(queryCalledAETitle); //    networkApplicationInformation.getApplicationEntityMap().getQueryModel(queryCalledAETitle);
                int                     queryDebugLevel = giftCloudProperties.getQueryDebugLevel();

                if (NetworkApplicationProperties.isStudyRootQueryModel(queryModel) || queryModel == null) {
                    currentRemoteQueryInformationModel=new StudyRootQueryInformationModel(queryHost,queryPort,queryCalledAETitle,queryCallingAETitle,queryDebugLevel);
                    stringForTitle=":"+remoteAEForQuery;
                }
                else {
                    throw new Exception("For remote query AE <"+remoteAEForQuery+">, query model "+queryModel+" not supported");
                }
            }
            catch (Exception e) {		// if an AE's property has no value, or model not supported
                e.printStackTrace(System.err);
            }
        }
    }



    private class DicomNodeListener implements Observer {

        private GiftCloudAppendUploadActionListener appendListener = new GiftCloudAppendUploadActionListener();

        @Override
        public void update(Observable o, Object arg) {

            // ToDo: if user hasn't logged in yet, there is no panel...


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
                    // ToDo: giftCloudUploader might be null!
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
