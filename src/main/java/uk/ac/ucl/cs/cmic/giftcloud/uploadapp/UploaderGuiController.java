package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderController;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;
import uk.ac.ucl.cs.cmic.giftcloud.util.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.workers.AppStartupWorker;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
    private  final QueryRetrieveDialogController queryRetrieveDialogController;
    private final ConfigurationDialogController configurationDialogController;
    private final PixelDataTemplateDialogController pixelDataDialogController;
    private final GiftCloudReporterFromApplication reporter;
    private final QueryRetrieveController queryRetrieveController;
    private final MenuController menuController;
    private GiftCloudUploaderAppConfiguration appConfiguration;
    private final UploaderController uploaderController;

    public UploaderGuiController(final GiftCloudUploaderAppConfiguration appConfiguration, final UploaderController uploaderController, final MainFrame mainFrame, final GiftCloudDialogs dialogs, final GiftCloudReporterFromApplication reporter) throws DicomException, IOException, InvocationTargetException, InterruptedException {
        this.resourceBundle = appConfiguration.getResourceBundle();
        this.appConfiguration = appConfiguration;
        this.uploaderController = uploaderController;
        this.mainFrame = mainFrame;
        this.giftCloudDialogs = dialogs;
        this.giftCloudProperties = appConfiguration.getProperties();
        this.reporter = reporter;

        // Create GUI components - each controller must ensure Swing creation is performed on the EDT
        this.queryRetrieveDialogController = new QueryRetrieveDialogController(appConfiguration, mainFrame, this, reporter);
        this.pixelDataDialogController = new PixelDataTemplateDialogController(appConfiguration, uploaderController.getPixelDataAnonymiserFilterCache(), mainFrame, dialogs, reporter);
        this.configurationDialogController = new ConfigurationDialogController(appConfiguration, mainFrame, this, uploaderController.getProjectListModel(), dialogs, reporter);
        this.uploaderPanel = new UploaderPanel(mainFrame, UploaderGuiController.this, uploaderController.getTableModel(), resourceBundle, uploaderController.getUploaderStatusModel(), reporter);
        this.menuController = new MenuController(mainFrame.getParent(), UploaderGuiController.this, resourceBundle, reporter);

        // Create the controller for query-retrieve operations
        this.queryRetrieveController = new QueryRetrieveController(queryRetrieveDialogController, giftCloudProperties, this.uploaderController.getUploaderStatusModel(), reporter);

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

    public void startDicomNodeAndCheckProperties(final List<File> filesToImport) {
        new Thread(new AppStartupWorker(appConfiguration, this, uploaderController, filesToImport, reporter)).start();
    }

    public void showConfigureDialog(final boolean wait) {
        configurationDialogController.showConfigureDialog(wait);
    }

    public void showAboutDialog() {
        try {
            mainFrame.show();
            giftCloudDialogs.showMessage(resourceBundle.getString("giftCloudAboutBoxText"));
        } catch (Throwable e) {
            reporter.reportErrorToUser("The about box could not be displayed", e);
        }
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
        } catch (Throwable e) {
            reporter.reportErrorToUser(resourceBundle.getString("dicomRetrieveFailureMessage"), e);
        }
    }

    public void query(final QueryParams queryParams) {
        try {
            queryRetrieveController.query(queryParams);
        } catch (Throwable e) {
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
        try {
            queryRetrieveDialogController.showQueryRetrieveDialog();
        } catch (Throwable e) {
            reporter.reportErrorToUser("The query-retrieve dialog could not be displayed", e);
        }
    }

    public void exportPatientList() {
        uploaderController.exportPatientList();
    }

    public void showPixelDataTemplateDialog() {
        try {
            pixelDataDialogController.showPixelDataTemplateDialog();
        } catch (Throwable e) {
            reporter.reportErrorToUser("The pixel data template crearion dialog could not be displayed", e);
        }
    }
}
