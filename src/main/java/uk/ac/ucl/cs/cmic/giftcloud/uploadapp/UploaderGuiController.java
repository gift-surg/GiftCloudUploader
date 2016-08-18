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

    /**
     * Creates a new GuiController
     *
     * @param appConfiguration
     * @param uploaderController
     * @param mainFrame
     * @param dialogs
     * @param reporter
     * @throws DicomException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public UploaderGuiController(final GiftCloudUploaderAppConfiguration appConfiguration, final UploaderController uploaderController, final MainFrame mainFrame, final GiftCloudDialogs dialogs, final GiftCloudReporterFromApplication reporter) throws InvocationTargetException, InterruptedException {
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
            hide(false);
        } else {
            show(false);
        }
    }

    /**
     * Runs startup tasks, including importing leftover files and importing any additional files specified
     * @param filesToImport additiional files to import
     */
    public void startDicomNodeAndCheckProperties(final List<File> filesToImport) {
        try {
            new Thread(new AppStartupWorker(appConfiguration, this, uploaderController, filesToImport, reporter)).start();
        } catch (Throwable e) {
            reportError("An error occurred while initialising the application", e, false);
        }
    }

    /**
     * @param wait if true then block the thread until the dialog has closed
     * @param showErrorIfFailed if true then report an error to the user if the dialog creation fails
     */
    public void showConfigureDialog(final boolean wait, final boolean showErrorIfFailed) {
        try {
            configurationDialogController.showConfigureDialog(wait);
        } catch (Throwable e) {
            reportError("An error occurred while trying to create the settings dialog", e, showErrorIfFailed);
        }
    }

    /**
     * Shows the about box
     */
    public void showAboutDialog() {
        try {
            mainFrame.show();
            giftCloudDialogs.showMessage(resourceBundle.getString("giftCloudAboutBoxText"));
        } catch (Throwable e) {
            reportError("The about box could not be displayed.", e, true);
        }
    }

    /**
     * Hides the main uploader window
     *
     * @param showUserDialogIfFailed if true, then show an error dialog if the hide operation fails. Generally, set this to true only if this operation was explicitly requested by the user.
     */
    public void hide(final boolean showUserDialogIfFailed) {
        try {
            mainFrame.hide();
        } catch (Throwable e) {
            reportError("Could not hide the application window.", e, showUserDialogIfFailed);
        }
    }

    /**
     * Shows the main uploader window
     *
     * @param showUserDialogIfFailed if true, then show an error dialog if the show operation fails. Generally, set this to true only if this operation was explicitly requested by the user.
     */
    public void show(final boolean showUserDialogIfFailed) {
        try {
            mainFrame.show();
        } catch (Throwable e) {
            reportError("Could not show the application window.", e, showUserDialogIfFailed);
        }
    }

    /**
     * Starts the service that passes pending files in the queue to the uploading thread. Does not start the uploading service.
     */
    public void startUploading() {
        try {
            uploaderController.startUploading();
        } catch (Throwable e) {
            reportError("Uploading could not be started.", e, true);
        }
    }

    /**
     * Pause the service that passes pending files in the queue to the uploading thread. Does not pause the uploading thread, so files already waiting to be processed by the uploading queue will still be processed
     */
    public void pauseUploading() {
        try {
            uploaderController.pauseUploading();
        } catch (Throwable e) {
            reportError("Uploading could not be paused.", e, true);
        }
    }

    /**
     * Performs a retrieve operation using the selections from the specified {@link QuerySelection}
     * @param currentRemoteQuerySelectionList the selections to retrieve from PACS
     */
    public void retrieve(List<QuerySelection> currentRemoteQuerySelectionList) {
        try {
            queryRetrieveController.retrieve(currentRemoteQuerySelectionList);
        } catch (Throwable e) {
            reportError(resourceBundle.getString("dicomRetrieveFailureMessage"), e, true);
        }
    }

    /**
     * Performs a Dicom query using the specified query parameters {@link QueryParams}
     * @param queryParams parameters used to perform the Dicom query
     */
    public void query(final QueryParams queryParams) {
        try {
            queryRetrieveController.query(queryParams);
        } catch (Throwable e) {
            reportError(resourceBundle.getString("dicomQueryFailureMessage"), e, true);
        }
    }

    /**
     * Imports a list of files into the uploading service
     * @param fileList
     * @param importAsReference
     * @param progress
     */
    public void runImport(List<File> fileList, final boolean importAsReference, final Progress progress) {
        try {
            uploaderController.runImport(fileList, importAsReference, progress);
        } catch (Throwable e) {
            reportError(resourceBundle.getString("fileImportFailureMessage"), e, true);
        }
    }

    /**
     * Brings up a local file import dialog and imports the selected files and folders
     */
    public void selectAndImport() {
        try {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        reporter.setWaitCursor();

                        Optional<GiftCloudDialogs.SelectedPathAndFiles> selectFileOrDirectory = giftCloudDialogs.selectMultipleFilesOrDirectors(giftCloudProperties.getLastImportDirectory());

                        if (selectFileOrDirectory.isPresent()) {
                            giftCloudProperties.setLastImportDirectory(selectFileOrDirectory.get().getParentPath());
                            giftCloudProperties.save();
                            uploaderController.runImport(selectFileOrDirectory.get().getSelectedFiles(), true, reporter);
                        }
                    } catch (Exception e) {
                        reporter.reportErrorToUser(resourceBundle.getString("fileImportFailureMessage") + e.getLocalizedMessage(), e);
                    } finally {
                        reporter.restoreCursor();
                    }
                }
            });
        } catch (Throwable e) {
            reportError("Could not start the file importer thread", e, true);
        }
    }

    /**
     * Stops and re-starts the Dicom Listener service
     */
    public void restartDicomService() {
        try {
            uploaderController.stopDicomListener();
        } catch (Throwable e) {
            reportError("Failed to shutdown the dicom node service", e, false);
        }
        try {
            uploaderController.startDicomListener();
        } catch (Throwable e) {
            reportError(resourceBundle.getString("dicomNodeFailureMessage"), e, true);
        }
    }

    /**
     * Re-starts the uploading service and forces the next upload to re-create the client connection to the server, allowing for a change in server properties
     */
    public void invalidateServerAndRestartUploader() {
        try {
            uploaderController.invalidateServerAndRestartUploader();
        } catch (Throwable e) {
            reportError("An error occurred when restarting the uploader service", e, true);
        }
    }

    /**
     * Shows the dialog for performing query-retrieve operations from PACS
     */
    public void importFromPacs() {
        try {
            queryRetrieveDialogController.showQueryRetrieveDialog();
        } catch (Throwable e) {
            reportError("The Import from PACS dialog could not be displayed", e, true);
        }
    }

    /**
     * Saves the current patient list to the location specified in the application settings
     * @param showUserDialogIfFailed if true then an error dialog will be shown to the user if the save fails
     */
    public void exportPatientList(final boolean showUserDialogIfFailed) {
        try {
            uploaderController.exportPatientList();
        } catch (Throwable e) {
            reportError("An error occurred while saving the patient list", e, showUserDialogIfFailed);
        }
    }

    /**
     * Shows the dialog for creating pixel data redaction templates
     */
    public void showPixelDataTemplateDialog() {
        try {
            pixelDataDialogController.showPixelDataTemplateDialog();
        } catch (Throwable e) {
            reportError("The pixel data template creation dialog could not be displayed", e, true);
        }
    }

    private void reportError(final String message, final Throwable throwable, final boolean showUser) {
        reporter.silentLogException(throwable, message);
        if (showUser) {
            reporter.reportErrorToUser(message, throwable);
        }
    }
}
