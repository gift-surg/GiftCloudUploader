package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GiftCloudSystemTray {

    private GiftCloudUploaderController controller;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private MenuItem hideItem;
    private MenuItem showItem;

    GiftCloudSystemTray(final GiftCloudUploaderController controller, final boolean isVisible) throws IOException {
        this.controller = controller;

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        Image image = ImageIO.read(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png"));
        trayIcon = new TrayIcon(image, "GIFT-Cloud Uploader");
        tray = SystemTray.getSystemTray();
        trayIcon.setToolTip("GIFT-Cloud Uploader");

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        final PopupMenu popup = new PopupMenu();

        MenuItem aboutItem = new MenuItem("About");
        popup.add(aboutItem);
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.showAboutDialog();
            }
        });

        popup.addSeparator();

        hideItem = new MenuItem("Hide");
        popup.add(hideItem);
        hideItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.hide();
            }
        });

        showItem = new MenuItem("Show");
        popup.add(showItem);
        showItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.show();
            }
        });

        popup.addSeparator();
        MenuItem configItem = new MenuItem("Settings");
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
        MenuItem exitItem = new MenuItem("Exit");
        popup.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        trayIcon.setPopupMenu(popup);

        updateMenu(true);
    }

    void updateMenu(final boolean isVisible) {
        if (tray == null) {
            return;
        }

        hideItem.setEnabled(isVisible);
        showItem.setEnabled(!isVisible);

    }

}
