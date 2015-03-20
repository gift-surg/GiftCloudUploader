package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

public class GiftCloudSystemTray {

    private GiftCloudUploaderController controller;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private MenuItem hideItem;
    private MenuItem showItem;
    private MenuItem importItem;

    private GiftCloudSystemTray(final GiftCloudUploaderController controller, final ResourceBundle resourceBundle) throws AWTException, IOException {
        this.controller = controller;

        Image iconImage = ImageIO.read(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png"));;
        trayIcon = new TrayIcon(iconImage, resourceBundle.getString("systemTrayIconText"));
        tray = SystemTray.getSystemTray();
        trayIcon.setToolTip(resourceBundle.getString("systemTrayIconToolTip"));

        tray.add(trayIcon);

        final PopupMenu popup = new PopupMenu();

        MenuItem aboutItem = new MenuItem(resourceBundle.getString("systemTrayAbout"));
        popup.add(aboutItem);
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.showAboutDialog();
            }
        });

        popup.addSeparator();

        importItem = new MenuItem(resourceBundle.getString("systemTrayImport"));
        popup.add(importItem);
        importItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.selectAndImport();
            }
        });

        popup.addSeparator();

        hideItem = new MenuItem(resourceBundle.getString("systemTrayHide"));
        popup.add(hideItem);
        hideItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.hide();
            }
        });

        showItem = new MenuItem(resourceBundle.getString("systemTrayShow"));
        popup.add(showItem);
        showItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.show();
            }
        });

        popup.addSeparator();
        MenuItem configItem = new MenuItem(resourceBundle.getString("systemTraySettings"));
        popup.add(configItem);
        configItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    controller.showConfigureDialog();
                } catch (IOException e1) {
                    trayIcon.displayMessage("Warning", "Error occurred trying to change the settings", TrayIcon.MessageType.WARNING);
                    // Here there was a failure
                    e1.printStackTrace();
                } catch (DicomNode.DicomNodeStartException e1) {
                    trayIcon.displayMessage("Warning", "Could not restart the Dicom node. Please check the settings are correct", TrayIcon.MessageType.WARNING);
                    // Here there was a failure
                    e1.printStackTrace();
                }
            }
        });

        popup.addSeparator();
        MenuItem exitItem = new MenuItem(resourceBundle.getString("systemTrayExit"));
        popup.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        trayIcon.setPopupMenu(popup);

        updateMenu(GiftCloudMainFrame.MainWindowVisibility.HIDDEN);
    }

    static Optional<GiftCloudSystemTray> safeCreateSystemTray(final GiftCloudUploaderController controller, final ResourceBundle resourceBundle, final GiftCloudReporter reporter) {
        if (!SystemTray.isSupported()) {
            reporter.silentError("SystemTray is not supported on this system.", null);
            return Optional.empty();
        } else {
            try {
                return Optional.of(new GiftCloudSystemTray(controller, resourceBundle));
            } catch (Throwable t) {
                reporter.silentError("The system tray icon could not be created due to the following error: " + t.getLocalizedMessage(), t);
                return Optional.empty();
            }
        }
    }

    void updateMenu(final GiftCloudMainFrame.MainWindowVisibility mainWindowVisibility) {
        if (tray == null) {
            return;
        }

        hideItem.setEnabled(mainWindowVisibility.isVisible());
        showItem.setEnabled(!mainWindowVisibility.isVisible());
    }
}
