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
import uk.ac.ucl.cs.cmic.giftcloud.workers.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GiftCloudUploaderMain implements GiftCloudUploaderController {

	protected static String propertiesFileName  = ".com.pixelmed.display.GiftCloudUploader.properties";
	protected ResourceBundle resourceBundle;
    final GiftCloudPropertiesFromBridge giftCloudProperties;
    final GiftCloudMainFrame giftCloudMainFrame;
    final GiftCloudDialogs giftCloudDialogs;
    final private DicomNode dicomNode;
    GiftCloudBridge giftCloudBridge = null;
    final GiftCloudUploaderPanel giftCloudUploaderPanel;
    final GiftCloudReporter reporter;
    final GiftCloudSystemTray giftCloudSystemTray;


    private QueryInformationModel currentRemoteQueryInformationModel;


    public GiftCloudUploaderMain(ResourceBundle resourceBundle) throws DicomException, IOException {
        this.resourceBundle = resourceBundle;

        final GiftCloudUploaderApplicationBase applicationBase = new GiftCloudUploaderApplicationBase(propertiesFileName);

        giftCloudMainFrame = new GiftCloudMainFrame(resourceBundle.getString("applicationTitle"));
        giftCloudDialogs = new GiftCloudDialogs(giftCloudMainFrame);

        final String buildDate = applicationBase.getBuildDateFromApplicationBase();
        JLabel statusBar = applicationBase.getStatusBarFromApplicationBase();

        giftCloudProperties = new GiftCloudPropertiesFromBridge(applicationBase);

        dicomNode = new DicomNode(giftCloudProperties, resourceBundle.getString("DatabaseRootTitleForOriginal"));
        dicomNode.addObserver(new DicomNodeListener());
        try {
            dicomNode.activateStorageSCP();
        } catch (DicomNode.DicomNodeStartException e) {
            System.out.println("Failed to initialise the Dicom node:" + e.getMessage());
        }

        reporter = new GiftCloudReporter(giftCloudMainFrame.getContainer(), giftCloudDialogs);

        // Initialise GIFT-Cloud
        try {
            giftCloudBridge = new GiftCloudBridge(reporter, giftCloudMainFrame.getContainer(), giftCloudProperties);

        } catch (Throwable t) {
            System.out.println("Failed to initialise the GIFT-Cloud component:" + t.getMessage());
        }

        // ToDo: if giftCloudBridge creation failed we need to deal with this
        giftCloudUploaderPanel = new GiftCloudUploaderPanel(this, giftCloudBridge.getProjectListModel(), dicomNode.getSrcDatabase(), giftCloudProperties, resourceBundle, giftCloudDialogs, buildDate, statusBar, reporter);

        giftCloudMainFrame.addMainPanel(giftCloudUploaderPanel);
        giftCloudMainFrame.show();

        giftCloudSystemTray = new GiftCloudSystemTray(this, true);
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
        giftCloudSystemTray.updateMenu(false);
    }

    @Override
    public void show() {
        giftCloudMainFrame.show();
        giftCloudSystemTray.updateMenu(true);
    }

    @Override
    public void upload(Vector<String> filePaths) {
        try {
            Thread activeThread = new Thread(new GiftCloudUploadWorker(filePaths, giftCloudBridge, reporter));
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
    public void runImport(String filePath, JProgressBar progressBar) {
        new Thread(new ImportWorker(dicomNode, filePath, progressBar, giftCloudProperties.acceptAnyTransferSyntax(), reporter)).start();
    }

    @Override
    public void selectAndImport(JProgressBar progressBar) {
        try {
            reporter.showMesageLogger();

            Optional<GiftCloudDialogs.SelectedPathAndFile> selectFileOrDirectory = giftCloudDialogs.selectFileOrDirectory(giftCloudProperties.getLastImportDirectory());

            if (selectFileOrDirectory.isPresent()) {
                giftCloudProperties.setLastImportDirectory(selectFileOrDirectory.get().getSelectedPath());
                String filePath = selectFileOrDirectory.get().getSelectedFile();
                runImport(filePath, progressBar);
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
            final String newFileName = (String)arg;
            giftCloudUploaderPanel.rebuildFileList(dicomNode.getSrcDatabase());
            appendListener.filesChanged(newFileName);
        }
    }


    protected class GiftCloudAppendUploadActionListener {

        private Vector<String> filesAlreadyUploaded = new Vector<String>();


        public void filesChanged(final String fileName) {
            try {
                if (filesAlreadyUploaded.contains(fileName)) {
                    System.out.println("File " + fileName + " has already been uploaded.");
                } else {
                    System.out.println("File " + fileName + " not uploaded. Adding to list");
                    Vector<String> filesToUpload = new Vector<String>();
                    filesToUpload.add(fileName);

                    // ToDo: this is not threadsafe!
                    Thread activeThread = new Thread(new GiftCloudAppendUploadWorker(filesToUpload, giftCloudBridge, reporter));
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
