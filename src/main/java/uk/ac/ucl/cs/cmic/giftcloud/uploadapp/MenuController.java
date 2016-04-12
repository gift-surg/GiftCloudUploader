package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.BackgroundService;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.StatusObservable;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import java.util.ResourceBundle;

public class MenuController {

    private final Optional<ApplicationSystemTray> systemTray;
    private final Optional<ApplicationMenu> menu;

    MenuController(JFrame parent, final GiftCloudUploaderController controller, final ResourceBundle resourceBundle, final GiftCloudReporterFromApplication reporter) {
        // Try to create a system tray icon. If this fails, then we warn the user and make the main dialog visible
        final boolean isMac = isOSX();
        systemTray = ApplicationSystemTray.safeCreateSystemTray(controller, resourceBundle, isMac, reporter);
        menu = ApplicationMenu.safeCreateMenu(parent, controller, resourceBundle, isMac, reporter);
    }

    public boolean isPresent() {
        return systemTray.isPresent() || menu.isPresent();
    }

    public void windowVisibilityStatusChanged(GiftCloudMainFrame.MainWindowVisibility visibility) {
        if (systemTray.isPresent()) {
            systemTray.get().updateMenuForWindowVisibility(visibility);
        }
        if (menu.isPresent()) {
            menu.get().updateMenuForWindowVisibility(visibility);
        }
    }

    public void backgroundAddToUploaderServiceListenerServiceStatusChanged(final BackgroundService.ServiceStatus serviceStatus) {
        if (systemTray.isPresent()) {
            systemTray.get().updateMenuForBackgroundUploadingServiceStatus(serviceStatus);
        }
        if (menu.isPresent()) {
            menu.get().updateMenuForBackgroundUploadingServiceStatus(serviceStatus);
        }
    }

    public void remove() {
        if (systemTray.isPresent()) {
            systemTray.get().remove();
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
