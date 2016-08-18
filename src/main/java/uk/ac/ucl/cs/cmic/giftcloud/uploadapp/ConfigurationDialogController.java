package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Creates and shows/hides the configuration dialog for the GIFT-Cloud Uploader
 */
class ConfigurationDialogController {
    private ConfigurationDialog configurationDialog = null;
    private MainFrame mainFrame;
    private UploaderGuiController uploaderGuiController;
    private ProjectListModel projectListModel;
    private GiftCloudDialogs dialogs;
    private GiftCloudReporter reporter;
    private GiftCloudUploaderAppConfiguration appConfiguration;

    /**
     * Creates a new controller/factory class for Configuration Dialogs
     *
     * @param appConfiguration
     * @param mainFrame
     * @param uploaderGuiController
     * @param projectListModel
     * @param dialogs
     * @param reporter
     */
    ConfigurationDialogController(final GiftCloudUploaderAppConfiguration appConfiguration, final MainFrame mainFrame, final UploaderGuiController uploaderGuiController, final ProjectListModel projectListModel, final GiftCloudDialogs dialogs, final GiftCloudReporter reporter) {
        this.appConfiguration = appConfiguration;
        this.mainFrame = mainFrame;
        this.uploaderGuiController = uploaderGuiController;
        this.projectListModel = projectListModel;
        this.dialogs = dialogs;
        this.reporter = reporter;
    }

    /**
     * Creates a new configuration dialog if it is not already visible
     * @param wait determines whether to block the thread waiting for the dialog to show
     */
    void showConfigureDialog(final boolean wait) {
        if (configurationDialog == null || !configurationDialog.isVisible()) {
            if (wait) {
                try {
                    GiftCloudUtils.runNowOnEdt(new Runnable() {
                        @Override
                        public void run() {
                            configurationDialog = new ConfigurationDialog(mainFrame.getContainer(), uploaderGuiController, appConfiguration.getProperties(), projectListModel, appConfiguration.getResourceBundle(), dialogs, reporter);
                        }
                    });
                } catch (InvocationTargetException e) {
                    reporter.silentLogException(e, "Failure in starting the configuration dialog");
                } catch (InterruptedException e) {
                    reporter.silentLogException(e, "Failure in starting the configuration dialog");
                }
            } else {
                GiftCloudUtils.runLaterOnEdt(new Runnable() {
                    @Override
                    public void run() {
                        configurationDialog = new ConfigurationDialog(mainFrame.getContainer(), uploaderGuiController, appConfiguration.getProperties(), projectListModel, appConfiguration.getResourceBundle(), dialogs, reporter);
                    }
                });
            }
        }
    }
}
