package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import java.util.Optional;
import java.util.ResourceBundle;

public class SystemTrayController implements GiftCloudMainFrame.MainFrameListener {

    private final Optional<GiftCloudSystemTray> giftCloudSystemTray;

    SystemTrayController(final GiftCloudUploaderController controller, final ResourceBundle resourceBundle, final GiftCloudReporter reporter) {
        // Try to create a system tray icon. If this fails, then we warn the user and make the main dialog visible
        giftCloudSystemTray = GiftCloudSystemTray.safeCreateSystemTray(controller, resourceBundle, reporter);
    }

    public boolean isPresent() {
        return giftCloudSystemTray.isPresent();
    }

    @Override
    public void windowVisibilityChanged(GiftCloudMainFrame.MainWindowVisibility visibility) {
        if (giftCloudSystemTray.isPresent()) {
            giftCloudSystemTray.get().updateMenu(visibility);
        }
    }
}
