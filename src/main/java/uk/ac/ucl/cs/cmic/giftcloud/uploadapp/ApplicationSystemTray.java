package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.BackgroundService;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 *  Creates an icon and menu in the system tray, allowing the user to execute actions and monitor progress from the GIFT-Cloud uploader while the main window is hidden
 *
 * <p>This class is part of the GIFT-Cloud Uploader
 *
 * @author  Tom Doel
 */
public class ApplicationSystemTray {

    private final SystemTray tray;
    private final TrayIcon trayIcon;
    private final MenuItem hideItem;
    private final MenuItem showItem;
    private final MenuItem importItem;
    private final MenuItem importFromPacsItem;
    private final MenuItem startUploaderItem;
    private final MenuItem pauseUploaderItem;
    private final MenuItem restartListenerItem;

    private boolean startIsResume = false;
    private final String resumeText;

    /**
     * Private constructor for creating a new menu and icon for the system tray
     *
     * @param controller        the controller used to perform menu actions
     * @param resourceBundle    the application resources used to choose menu text
     *
     * @param reporter
     * @throws AWTException     if the desktop system tray is missing
     * @throws IOException      if an error occured while attempting to read the icon file
     */
    private ApplicationSystemTray(final UploaderGuiController controller, final ResourceBundle resourceBundle, final boolean isMac, final GiftCloudReporterFromApplication reporter) throws AWTException, IOException {

        Image iconImage = ImageIO.read(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png"));
        trayIcon = new TrayIcon(iconImage, resourceBundle.getString("systemTrayIconText"));
        trayIcon.setImageAutoSize(true);
        tray = SystemTray.getSystemTray();
        trayIcon.setToolTip(resourceBundle.getString("systemTrayIconToolTip"));

        tray.add(trayIcon);

        final PopupMenu popup = new PopupMenu();

        MenuItem aboutItem = new MenuItem(resourceBundle.getString("menuAbout"));
        popup.add(aboutItem);
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.showAboutDialog();
            }
        });

        popup.addSeparator();

        importItem = new MenuItem(resourceBundle.getString("menuImport"));
        popup.add(importItem);
        importItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.selectAndImport();
            }
        });

        importFromPacsItem = new MenuItem(resourceBundle.getString("menuImportFromPacs"));
        popup.add(importFromPacsItem);
        importFromPacsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.importFromPacs();
            }
        });

        popup.addSeparator();

        hideItem = new MenuItem(resourceBundle.getString("menuHide"));
        popup.add(hideItem);
        hideItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.hide();
            }
        });

        showItem = new MenuItem(resourceBundle.getString("menuShow"));
        popup.add(showItem);
        showItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.show();
            }
        });

        popup.addSeparator();

        restartListenerItem = new MenuItem(resourceBundle.getString("menuRestartListener"));
        popup.add(restartListenerItem);
        restartListenerItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.restartDicomService();
            }
        });

        popup.addSeparator();


        startUploaderItem = new MenuItem(resourceBundle.getString("menuStartUploader"));
        popup.add(startUploaderItem);
        startUploaderItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.startUploading();
            }
        });

        pauseUploaderItem = new MenuItem(resourceBundle.getString("menuPauseUploader"));
        popup.add(pauseUploaderItem);
        pauseUploaderItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.pauseUploading();
            }
        });

        // After pausing, the "start" item changes to "resume"
        resumeText = resourceBundle.getString("menuResumeUploader");

        popup.addSeparator();

        MenuItem configItem = new MenuItem(isMac ? resourceBundle.getString("menuSettingsMac") : resourceBundle.getString("menuSettingsWin"));
        popup.add(configItem);
        configItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    controller.showConfigureDialog(false);
                } catch (Throwable throwable) {
                    trayIcon.displayMessage("Warning", "Error occurred while showing the settings dialog", TrayIcon.MessageType.WARNING);
                    reporter.silentLogException(throwable, "Error occurred while showing the settings dialog");
                }
            }
        });

        popup.addSeparator();
        MenuItem exitItem = new MenuItem(isMac ? resourceBundle.getString("menuExitMac") : resourceBundle.getString("menuExitWin"));
        popup.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //ToDo: remove system tray icon here
                System.exit(0);
            }
        });

        trayIcon.setPopupMenu(popup);

        updateMenuForWindowVisibility(GiftCloudMainFrame.MainWindowVisibility.HIDDEN);
        updateMenuForBackgroundUploadingServiceStatus(BackgroundService.ServiceStatus.INITIALIZED);
    }

    /**
     * Static factory method for creating the GIFT-Cloud Uploader menu and icon for the system tray.
     *
     * <p>Does not throw any exceptions. If the menu cannot be created or an error occurs, an empty Optional is returned.
     *
     * @param controller        the controller used to perform menu actions
     * @param resourceBundle    the application resources used to choose menu text
     * @param reporter          the reporter object used to record errors
     * @return                  an (@link Optional) containing the (@link ApplicationSystemTray) object or an empty (@link Optional) if the SystemTray is not supported, or an error occurred, e.g. in attempting to load the icon
     */
    static Optional<ApplicationSystemTray> safeCreateSystemTray(final UploaderGuiController controller, final ResourceBundle resourceBundle, final boolean isMac, final GiftCloudReporterFromApplication reporter) {
        if (!SystemTray.isSupported()) {
            reporter.silentError("SystemTray is not supported on this system.", null);
            return Optional.empty();
        } else {
            try {
                return Optional.of(new ApplicationSystemTray(controller, resourceBundle, isMac, reporter));
            } catch (Throwable t) {
                reporter.silentError("The system tray icon could not be created due to the following error: " + t.getLocalizedMessage(), t);
                return Optional.empty();
            }
        }
    }

    /**
     * Updates the system tray menu according to enable/disable the show/hide menu items according to the visibility of the main window
     *
     * @param mainWindowVisibility  whether the main window is currently hidden or visible
     */
    void updateMenuForWindowVisibility(final GiftCloudMainFrame.MainWindowVisibility mainWindowVisibility) {
        if (tray == null) {
            return;
        }

        hideItem.setEnabled(mainWindowVisibility.isVisible());
        showItem.setEnabled(!mainWindowVisibility.isVisible());
    }

    /**
     * Updates the system tray menu according to enable/disable the start/pause menu items according to the status of the uploading thread
     *
     * @param serviceStatus  whether the uploader service is currently running
     */
    void updateMenuForBackgroundUploadingServiceStatus(final BackgroundService.ServiceStatus serviceStatus) {
        if (tray == null) {
            return;
        }

        boolean startItemEnabled = false;
        boolean pauseItemEnabled = false;
        boolean startBecomesResume = false;

        switch (serviceStatus) {
            case INITIALIZED:
                startItemEnabled = true;
                pauseItemEnabled = false;
                startBecomesResume = false;
                break;

            case STOP_REQUESTED:
                startItemEnabled = false;
                pauseItemEnabled = false;
                startBecomesResume = false;
                break;

            case RUNNING:
                startItemEnabled = false;
                pauseItemEnabled = true;
                startBecomesResume = true;
                break;

            case COMPLETE:
                startItemEnabled = true;
                pauseItemEnabled = false;
                startBecomesResume = true;
                break;
        }
        startUploaderItem.setEnabled(startItemEnabled);
        pauseUploaderItem.setEnabled(pauseItemEnabled);

        // Update the "start" menu item to become "resume" after a pause
        if (startBecomesResume && !startIsResume) {
            startUploaderItem.setLabel(resumeText);
            startIsResume = true;
        }

    }

    /**
     * Removes the menu from the system tray
     */
    void remove() {
        tray.remove(trayIcon);
    }
}
