package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GiftCloudSystemTray {

    private final MainFrame mainFrame;
    private String productName;

    GiftCloudSystemTray(final MainFrame mainFrame, final GiftCloudDialogs giftCloudDialogs, final String productName) throws IOException {
        this.mainFrame = mainFrame;
        this.productName = productName;

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        final PopupMenu popup = new PopupMenu();
        Image image = ImageIO.read(this.getClass().getClassLoader().getResource("GiftSurgMiniIcon.png"));
        final TrayIcon trayIcon = new TrayIcon(image, "GIFT-Cloud Uploader");
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");
        MenuItem showItem = new MenuItem("Show");
        MenuItem hideItem = new MenuItem("Hide");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(showItem);
        popup.add(hideItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        trayIcon.setToolTip("GIFT-Cloud Uploader");

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.show();
                giftCloudDialogs.showMessage(productName);
            }
        });

        showItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.show();
            }
        });

        hideItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.hide();
            }
        });

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }


}
