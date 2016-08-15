package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.apple.eawt.Application;
import com.pixelmed.dicom.DicomException;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLoginDialog;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PropertyStore;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ImportWorker;

import javax.imageio.ImageIO;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The main controller class for the GIFT-Cloud Uploader gui
 *
 * @author  Tom Doel
 */
public class UploaderGuiController implements UserCallback {

    private final ResourceBundle resourceBundle;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final MainFrame mainFrame;
    private final GiftCloudDialogs giftCloudDialogs;
    private final DicomListener dicomListener;
    private final GiftCloudUploader giftCloudUploader;
    private final UploaderPanel uploaderPanel;
    private ConfigurationDialog configurationDialog = null;
    private PixelDataTemplateDialog pixelDataDialog = null;
    private final GiftCloudReporterFromApplication reporter;
    private final QueryRetrieveController queryRetrieveController;
    private final MenuController menuController;
    private final UploaderStatusModel uploaderStatusModel = new UploaderStatusModel();
    private Optional<SingleInstanceService> singleInstanceService;
    private GiftCloudLoginDialog loginDialog;

    public UploaderGuiController(final MainFrame mainFrame, final RestServerFactory restServerFactory, final PropertyStore propertyStore, final GiftCloudDialogs dialogs, final GiftCloudReporterFromApplication reporter) throws DicomException, IOException {
        resourceBundle = mainFrame.getResourceBundle();
        this.mainFrame = mainFrame;
        this.giftCloudDialogs = dialogs;
        this.reporter = reporter;

        // Get the GIFT-Cloud icon - this will return null if not found
        ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftCloud.png"));

        // Use the Java Web Start single instance mechanism to ensure only one instance of the application is running at a time. This is critical as the properties and patient list caching is not safe across multiple instances
        try {
            singleInstanceService = Optional.of((SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService"));
            GiftCloudUploaderSingleInstanceListener singleInstanceListener = new GiftCloudUploaderSingleInstanceListener();
            singleInstanceService.get().addSingleInstanceListener(singleInstanceListener);
        } catch (UnavailableServiceException e) {
            singleInstanceService = Optional.empty();
        }

        // Set the dock icon - we need to do this before the main class is created
        URL iconURL = GiftCloudUploaderApp.class.getResource("/uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png");

        if (iconURL == null) {
            reporter.silentWarning("Could not find Uploader icon resource.");
        } else {
            if (isOSX()) {
                try {
                    Image iconImage = ImageIO.read(iconURL);
                    if (iconImage == null) {
                        reporter.silentWarning("Could not find Uploader icon.");
                    } else {
                        Application.getApplication().setDockIconImage(new ImageIcon(iconImage).getImage());
                    }
                } catch (Exception e) {
                    reporter.silentLogException(e, "Could not configure the dock menu: " + e.getLocalizedMessage());
                }
            }
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.UIElement", "true");

        final String applicationTitle = resourceBundle.getString("applicationTitle");

        // This is used to set the application title on OSX, but may not work when run from the debugger
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationTitle);

        setSystemLookAndFeel();

        // Initialise application properties
        giftCloudProperties = new GiftCloudPropertiesFromApplication(propertyStore, resourceBundle, reporter);

        // Create the object used for creating user login dialogs if necessary
        this.loginDialog = new GiftCloudLoginDialog(icon, giftCloudProperties, mainFrame.getContainer());

        // Initialise the main GIFT-Cloud class
        final File pendingUploadFolder = giftCloudProperties.getUploadFolder(reporter);

        giftCloudUploader = new GiftCloudUploader(restServerFactory, giftCloudProperties, uploaderStatusModel, this, reporter);
        dicomListener = new DicomListener(giftCloudUploader, giftCloudProperties, uploaderStatusModel, reporter);

        uploaderPanel = new UploaderPanel(mainFrame.getParent(), this, giftCloudUploader.getTableModel(), giftCloudProperties, resourceBundle, uploaderStatusModel, reporter);
        queryRetrieveController = new QueryRetrieveController(uploaderPanel.getQueryRetrieveRemoteView(), giftCloudProperties, uploaderStatusModel, reporter);

        mainFrame.addMainPanel(uploaderPanel);

        menuController = new MenuController(mainFrame.getParent(), this, resourceBundle, reporter);
        mainFrame.addListener(menuController.new MainWindowVisibilityListener());
        giftCloudUploader.getBackgroundAddToUploaderService().addListener(menuController.new BackgroundAddToUploaderServiceListener());

        final Optional<Boolean> hideWindowOnStartupProperty = giftCloudProperties.getHideWindowOnStartup();

        // We hide the main window only if specified in the preferences, AND if the system tray or menu is supported
        final boolean hideMainWindow = hideWindowOnStartupProperty.isPresent() && hideWindowOnStartupProperty.get() && menuController.isPresent();
        if (hideMainWindow) {
            hide();
        } else {
            show();
        }

        // Add existing files to the upload queue
        runImport(Arrays.asList(pendingUploadFolder), false, reporter);
    }

    public void start(final boolean showImportDialog, final List<File> filesToImport) {
        Optional<Throwable> dicomNodeFailureException = Optional.empty();
        try {
            dicomListener.activateStorageSCP();
        } catch (Throwable e) {
            dicomNodeFailureException = Optional.of(e);
            reporter.silentLogException(e, resourceBundle.getString("dicomNodeFailureMessageWithDetails") + e.getLocalizedMessage());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Attempt to authenticate
//                giftCloudUploader.tryAuthentication();
                startUploading();
            }
        }).start();

        propertiesCheckAndImportLoop(dicomNodeFailureException, showImportDialog, filesToImport);
    }

    private void propertiesCheckAndImportLoop(final Optional<Throwable> dicomNodeFailureException, final boolean startImport, final List<File> filesToImport) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // We check whether the main properties have been set. If not, we warn the user and bring up the configuration dialog. We suppress the Dicom node start failure in this case, as we assume the lack of properties is responsible
                final Optional<String> propertiesNotConfigured = checkProperties();
                if (propertiesNotConfigured.isPresent()) {
                    reporter.showMessageToUser(propertiesNotConfigured.get());
                    showConfigureDialog(startImport);

                } else {
                    // If the properties have been set but the Dicom node still fails to start, then we report this to the user.
                    if (dicomNodeFailureException.isPresent()) {
                        reporter.reportErrorToUser(resourceBundle.getString("dicomNodeFailureMessage"), dicomNodeFailureException.get());
                        showConfigureDialog(startImport);
                    }
                }
                if (!filesToImport.isEmpty()) {
                    runImport(filesToImport, true, reporter);
                }
                if (startImport) {
                    selectAndImport();
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

    public void showConfigureDialog(final boolean wait) {
        if (configurationDialog == null || !configurationDialog.isVisible()) {
            if (wait) {
                try {
                    java.awt.EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            configurationDialog = new ConfigurationDialog(mainFrame.getContainer(), UploaderGuiController.this, giftCloudProperties, giftCloudUploader.getProjectListModel(), resourceBundle, giftCloudDialogs, reporter);
                        }
                    });
                } catch (InvocationTargetException e) {
                    reporter.silentLogException(e, "Failure in starting the configuration dialog");
                } catch (InterruptedException e) {
                    reporter.silentLogException(e, "Failure in starting the configuration dialog");
                }
            } else {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        configurationDialog = new ConfigurationDialog(mainFrame.getContainer(), UploaderGuiController.this, giftCloudProperties, giftCloudUploader.getProjectListModel(), resourceBundle, giftCloudDialogs, reporter);
                    }
                });
            }
        }
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
        giftCloudUploader.setUploadServiceRunningState(true);
    }

    public void pauseUploading() {
        giftCloudUploader.setUploadServiceRunningState(false);
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
        new Thread(new ImportWorker(fileList, progress, giftCloudProperties.acceptAnyTransferSyntax(), giftCloudUploader, importAsReference, uploaderStatusModel, reporter)).start();
    }

    public void selectAndImport() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    reporter.setWaitCursor();
                    reporter.showMesageLogger();

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
            dicomListener.shutdownStorageSCPAndWait(giftCloudProperties.getShutdownTimeoutMs());
        } catch (Throwable e) {
            reporter.silentLogException(e, "Failed to shutdown the dicom node service");
        }
        try {
            dicomListener.activateStorageSCP();
        } catch (Throwable e) {
            reporter.silentLogException(e, resourceBundle.getString("dicomNodeFailureMessageWithDetails") + e.getLocalizedMessage());
            reporter.reportErrorToUser(resourceBundle.getString("dicomNodeFailureMessage"), e);
        }
    }

    public void invalidateServerAndRestartUploader() {
        pauseUploading();
        giftCloudUploader.invalidateServer();
        startUploading();
    }

    public void importFromPacs() {
        uploaderPanel.showQueryRetrieveDialog();
    }

    public void exportPatientList() {
        giftCloudUploader.exportPatientList();
    }

    public void showPixelDataTemplateDialog() {
        if (pixelDataDialog == null || !pixelDataDialog.isVisible()) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pixelDataDialog = new PixelDataTemplateDialog(mainFrame.getContainer(), resourceBundle.getString("pixelDataDialogTitle"), giftCloudUploader.getPixelDataAnonymiserFilterCache(), giftCloudProperties, giftCloudDialogs, reporter);
                }
            });
        }
    }

    private static boolean isOSX() {
        return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }


    @Override
    public String getProjectName(GiftCloudServer server) throws IOException {
        final Optional<String> lastProjectName = giftCloudProperties.getLastProject();
        if (lastProjectName.isPresent() && StringUtils.isNotBlank(lastProjectName.get())) {
            return lastProjectName.get();
        } else {
            try {
                final String selectedProject = giftCloudDialogs.showInputDialogToSelectProject(server.getListOfProjects(), mainFrame.getContainer(), lastProjectName);
                giftCloudProperties.setLastProject(selectedProject);
                giftCloudProperties.save();
                return selectedProject;
            } catch (IOException e) {
                throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(final String supplementalMessage) {
        return loginDialog.getPasswordAuthentication(supplementalMessage);
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

}
