package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderController;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The main controller class for the GIFT-Cloud Uploader gui
 *
 * @author  Tom Doel
 */
public class UploaderGuiController {

    private final ResourceBundle resourceBundle;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final MainFrame mainFrame;
    private final GiftCloudDialogs giftCloudDialogs;
    private final UploaderPanel uploaderPanel;
    private ConfigurationDialogController configurationDialogController = null;
    private PixelDataTemplateDialog pixelDataDialog = null;
    private final GiftCloudReporterFromApplication reporter;
    private final QueryRetrieveController queryRetrieveController;
    private final MenuController menuController;
    private final UploaderController uploaderController;

    public UploaderGuiController(final GiftCloudUploaderAppConfiguration appConfiguration, final UploaderController uploaderController, final MainFrame mainFrame, final GiftCloudDialogs dialogs, final GiftCloudReporterFromApplication reporter) throws DicomException, IOException {
        this.resourceBundle = appConfiguration.getResourceBundle();
        this.uploaderController = uploaderController;
        this.configurationDialogController = new ConfigurationDialogController(appConfiguration, mainFrame, this, uploaderController.getProjectListModel(), dialogs, reporter);
        this.mainFrame = mainFrame;
        this.giftCloudDialogs = dialogs;
        this.giftCloudProperties = appConfiguration.getProperties();
        this.reporter = reporter;
        this.uploaderPanel = new UploaderPanel(mainFrame.getParent(), UploaderGuiController.this, uploaderController.getTableModel(), giftCloudProperties, resourceBundle, uploaderController.getUploaderStatusModel(), reporter);
        this.queryRetrieveController = new QueryRetrieveController(uploaderPanel.getQueryRetrieveRemoteView(), giftCloudProperties, this.uploaderController.getUploaderStatusModel(), reporter);
        this.mainFrame.addMainPanel(uploaderPanel);
        this.menuController = new MenuController(mainFrame.getParent(), UploaderGuiController.this, resourceBundle, reporter);

        mainFrame.addListener(menuController.new MainWindowVisibilityListener());
        uploaderController.addBackgroundAddToUploaderServiceListener(menuController.new BackgroundAddToUploaderServiceListener());

        // We hide the main window only if specified in the preferences, AND if the system tray or menu is supported
        final Optional<Boolean> hideWindowOnStartupProperty = giftCloudProperties.getHideWindowOnStartup();
        final boolean hideMainWindow = hideWindowOnStartupProperty.isPresent() && hideWindowOnStartupProperty.get() && menuController.isPresent();

        if (hideMainWindow) {
            hide();
        } else {
            show();
        }
    }

    public void startDicomNodeAndCheckProperties(final boolean wait, final List<File> filesToImport) {
        Optional<Throwable> dicomNodeFailureException = Optional.empty();
        try {
            uploaderController.startDicomListener();
        } catch (Throwable e) {
            dicomNodeFailureException = Optional.of(e);
            reporter.silentLogException(e, resourceBundle.getString("dicomNodeFailureMessageWithDetails") + e.getLocalizedMessage());
        }

        propertiesCheckAndImportLoop(dicomNodeFailureException, wait, filesToImport);
    }

    private void propertiesCheckAndImportLoop(final Optional<Throwable> dicomNodeFailureException, final boolean wait, final List<File> filesToImport) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // We check whether the main properties have been set. If not, we warn the user and bring up the configuration dialog. We suppress the Dicom node start failure in this case, as we assume the lack of properties is responsible
                final Optional<String> propertiesNotConfigured = checkProperties();
                if (propertiesNotConfigured.isPresent()) {
                    reporter.showMessageToUser(propertiesNotConfigured.get());
                    showConfigureDialog(wait);

                } else {
                    // If the properties have been set but the Dicom node still fails to start, then we report this to the user.
                    if (dicomNodeFailureException.isPresent()) {
                        reporter.reportErrorToUser(resourceBundle.getString("dicomNodeFailureMessage"), dicomNodeFailureException.get());
                        showConfigureDialog(wait);
                    }
                }
            }
        }).start();

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

    public void showConfigureDialog(final boolean wait) {
        configurationDialogController.showConfigureDialog(wait);
    }

    public void showAboutDialog() {
        mainFrame.show();
        giftCloudDialogs.showMessage(resourceBundle.getString("giftCloudAboutBoxText"));
    }

    public void hide() {
        mainFrame.hide();
    }

    public void show() {
        mainFrame.show();
    }

    public void startUploading() {
        uploaderController.startUploading();
    }

    public void pauseUploading() {
        uploaderController.pauseUploading();
    }

    public void retrieve(List<QuerySelection> currentRemoteQuerySelectionList) {
        try {
            queryRetrieveController.retrieve(currentRemoteQuerySelectionList);
        } catch (Exception e) {
            reporter.reportErrorToUser(resourceBundle.getString("dicomRetrieveFailureMessage"), e);
        }
    }

    public void query(final QueryParams queryParams) {
        try {
            queryRetrieveController.query(queryParams);
        } catch (Exception e) {
            reporter.reportErrorToUser(resourceBundle.getString("dicomQueryFailureMessage"), e);
        }
    }

    public void runImport(List<File> fileList, final boolean importAsReference, final Progress progress) {
        uploaderController.runImport(fileList, importAsReference, progress);
    }

    public void selectAndImport() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    reporter.setWaitCursor();

                    Optional<GiftCloudDialogs.SelectedPathAndFiles> selectFileOrDirectory = giftCloudDialogs.selectMultipleFilesOrDirectors(giftCloudProperties.getLastImportDirectory());

                    if (selectFileOrDirectory.isPresent()) {
                        giftCloudProperties.setLastImportDirectory(selectFileOrDirectory.get().getParentPath());
                        giftCloudProperties.save();
                        runImport(selectFileOrDirectory.get().getSelectedFiles(), true, reporter);
                    }
                } catch (Exception e) {
                    reporter.reportErrorToUser(resourceBundle.getString("fileImportFailureMessage") + e.getLocalizedMessage(), e);
                } finally {
                    reporter.restoreCursor();
                }
            }
        });
    }

    public void restartDicomService() {
        try {
            uploaderController.stopDicomListener();
        } catch (Throwable e) {
            reporter.silentLogException(e, "Failed to shutdown the dicom node service");
        }
        try {
            uploaderController.startDicomListener();
        } catch (Throwable e) {
            reporter.silentLogException(e, resourceBundle.getString("dicomNodeFailureMessageWithDetails") + e.getLocalizedMessage());
            reporter.reportErrorToUser(resourceBundle.getString("dicomNodeFailureMessage"), e);
        }
    }

    public void invalidateServerAndRestartUploader() {
        uploaderController.invalidateServerAndRestartUploader();
    }

    public void importFromPacs() {
        uploaderPanel.showQueryRetrieveDialog();
    }

    public void exportPatientList() {
        uploaderController.exportPatientList();
    }

    public void showPixelDataTemplateDialog() {
        if (pixelDataDialog == null || !pixelDataDialog.isVisible()) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pixelDataDialog = new PixelDataTemplateDialog(mainFrame.getContainer(), resourceBundle.getString("pixelDataDialogTitle"), uploaderController.getPixelDataAnonymiserFilterCache(), giftCloudProperties, giftCloudDialogs, reporter);
                }
            });
        }
    }
}
