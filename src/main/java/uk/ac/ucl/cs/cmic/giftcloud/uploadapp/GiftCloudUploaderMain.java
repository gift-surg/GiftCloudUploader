package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.DicomNetworkException;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudHttpException;

import javax.swing.*;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Vector;

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


        giftCloudUploaderPanel = new GiftCloudUploaderPanel(this, dicomNode, giftCloudBridge, giftCloudProperties, resourceBundle, giftCloudDialogs, giftCloudMainFrame, buildDate, statusBar, reporter);

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


    private class DicomNodeListener implements Observer {

        private GiftCloudAppendUploadActionListener appendListener = new GiftCloudAppendUploadActionListener();

        @Override
        public void update(Observable o, Object arg) {
            final String newFileName = (String)arg;
            giftCloudUploaderPanel.addFile(newFileName);

            Vector names = new Vector();
            names.add(newFileName);

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
                    Thread activeThread = new Thread(new GiftCloudAppendUploadWorker(filesToUpload));
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

    protected class GiftCloudAppendUploadWorker implements Runnable {
        private final Vector<String> sourceFilePathSelections;

        GiftCloudAppendUploadWorker(Vector<String> sourceFilePathSelections) {
            this.sourceFilePathSelections = sourceFilePathSelections;
        }

        public void run() {
            if (giftCloudBridge == null) {
                giftCloudDialogs.showError("An error occurred which prevents the uploader from connecting to the server. Please restart GIFT-Cloud uploader.");
                return;
            }

            reporter.setWaitCursor();

            if (sourceFilePathSelections == null) {
                reporter.updateProgress("GIFT-Cloud upload: no files were selected for upload");
                giftCloudDialogs.showError("No files were selected for uploading.");
            } else {
                reporter.sendLn("GIFT-Cloud upload started");
                reporter.startProgressBar();

                for (final String fileName : sourceFilePathSelections) {
                    try {
                        Vector singleFile = new Vector();
                        singleFile.add(fileName);

                        System.out.println("Uploading single file: " + fileName);
                        if (giftCloudBridge.appendToGiftCloud(singleFile)) {
                            reporter.updateProgress("GIFT-Cloud upload complete");
                        } else {
                            reporter.updateProgress("Partial failure in GIFT-Cloud upload");
                        }
                    } catch (GiftCloudHttpException e) {
                        reporter.updateProgress("Partial failure in GIFT-Cloud upload, due to the following error: " + e.getHtmlText());
                        e.printStackTrace(System.err);
                    } catch (Exception e) {
                        reporter.updateProgress("Failure in GIFT-Cloud upload, due to the following error: " + e.toString());
                        e.printStackTrace(System.err);
                    }
                }

                reporter.endProgress();
            }
            reporter.restoreCursor();
        }
    }

}
