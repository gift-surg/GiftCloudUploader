package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.BackgroundService;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.StatusObservable;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 *  Creates menus for the main application and system tray, with some customisation for Windows/Mac platforms
 *
 * <p>This class is part of the GIFT-Cloud Uploader
 *
 * @author  Tom Doel
 */
public class MenuController {

    private final Optional<ApplicationSystemTray> systemTray;
    private final Optional<ApplicationMenu> menu;

    /**
     * Create a new application menu and system tray menu if supported
     * @param parent
     * @param controller
     * @param resourceBundle
     * @param reporter
     */
    MenuController(final JFrame parent, final UploaderGuiController controller, final ResourceBundle resourceBundle, final GiftCloudReporterFromApplication reporter) {
        // Try to create a system tray icon. If this fails, then we warn the user and make the main dialog visible
        final boolean isMac = isOSX();
        systemTray = ApplicationSystemTray.safeCreateSystemTray(controller, resourceBundle, isMac, reporter);
        menu = ApplicationMenu.safeCreateMenu(parent, controller, resourceBundle, isMac, reporter);
    }

    /**
     * @return true if a system tray is supported and was created successfully
     */
    public boolean isPresent() {
        return systemTray.isPresent() || menu.isPresent();
    }

    /**
     * Removes the system tray menu
     */
    public void remove() {
        if (systemTray.isPresent()) {
            systemTray.get().remove();
        }
    }

    private void windowVisibilityStatusChanged(GiftCloudMainFrame.MainWindowVisibility visibility) {
        if (systemTray.isPresent()) {
            systemTray.get().updateMenuForWindowVisibility(visibility);
        }
        if (menu.isPresent()) {
            menu.get().updateMenuForWindowVisibility(visibility);
        }
    }

    private void backgroundAddToUploaderServiceListenerServiceStatusChanged(final BackgroundService.ServiceStatus serviceStatus) {
        if (systemTray.isPresent()) {
            systemTray.get().updateMenuForBackgroundUploadingServiceStatus(serviceStatus);
        }
        if (menu.isPresent()) {
            menu.get().updateMenuForBackgroundUploadingServiceStatus(serviceStatus);
        }
    }

    public class MainWindowVisibilityListener implements StatusObservable.StatusListener<MainFrame.MainWindowVisibility> {
        @Override
        public void statusChanged(final MainFrame.MainWindowVisibility visibility) {
            windowVisibilityStatusChanged(visibility);
        }
    }

    public class BackgroundAddToUploaderServiceListener implements StatusObservable.StatusListener<BackgroundService.ServiceStatus> {
        @Override
        public void statusChanged(final BackgroundService.ServiceStatus serviceStatus) {
            backgroundAddToUploaderServiceListenerServiceStatusChanged(serviceStatus);
        }
    }

    private static boolean isOSX() {
        return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }
}
