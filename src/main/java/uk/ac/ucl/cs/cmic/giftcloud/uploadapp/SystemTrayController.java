package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.BackgroundService;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.StatusObservable;

import java.util.Optional;
import java.util.ResourceBundle;

public class SystemTrayController {

    private final Optional<GiftCloudSystemTray> giftCloudSystemTray;

    SystemTrayController(final GiftCloudUploaderController controller, final ResourceBundle resourceBundle, final GiftCloudReporterFromApplication reporter) {
        // Try to create a system tray icon. If this fails, then we warn the user and make the main dialog visible
        giftCloudSystemTray = GiftCloudSystemTray.safeCreateSystemTray(controller, resourceBundle, reporter);
    }

    public boolean isPresent() {
        return giftCloudSystemTray.isPresent();
    }

    public void windowVisibilityStatusChanged(GiftCloudMainFrame.MainWindowVisibility visibility) {
        if (giftCloudSystemTray.isPresent()) {
            giftCloudSystemTray.get().updateMenuForWindowVisibility(visibility);
        }
    }

    public void backgroundAddToUploaderServiceListenerServiceStatusChanged(final BackgroundService.ServiceStatus serviceStatus) {
        if (giftCloudSystemTray.isPresent()) {
            giftCloudSystemTray.get().updateMenuForBackgroundUploadingServiceStatus(serviceStatus);
        }
    }

    public void remove() {
        if (giftCloudSystemTray.isPresent()) {
            giftCloudSystemTray.get().remove();
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

}
