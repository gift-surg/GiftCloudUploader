package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 *  Creates an icon and menu in the system tray, allowing the user to execute actions and monitor progress from the GIFT-Cloud uploader while the main window is hidden
 *
 * <p>This class is part of the GIFT-Cloud Uploader
 *
 * @author  Tom Doel
 */
public class GiftCloudSystemTray {

    private GiftCloudUploaderController controller;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private MenuItem hideItem;
    private MenuItem showItem;
    private MenuItem importItem;
    private MenuItem startUploaderItem;
    private MenuItem pauseUploaderItem;

    /**
     * Private constructor for creating a new menu and icon for the system tray
     *
     *
     * @param controller        the controller used to perform menu actions
     * @param resourceBundle    the application resources used to choose menu text
     *
     * @throws AWTException     if the desktop system tray is missing
     * @throws IOException      if an error occured while attempting to read the icon file
     */
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

        startUploaderItem = new MenuItem(resourceBundle.getString("systemTrayStartUploader"));
        popup.add(startUploaderItem);
        startUploaderItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.startUploading();
            }
        });

        pauseUploaderItem = new MenuItem(resourceBundle.getString("systemTrayPauseUploader"));
        popup.add(pauseUploaderItem);
        pauseUploaderItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.pauseUploading();
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

    /**
     * Static factory method for creating the GIFT-Cloud Uploader menu and icon for the system tray.
     *
     * <p>Does not throw any exceptions. If the menu cannot be created or an error occurs, an empty Optional is returned.
     *
     * @param controller        the controller used to perform menu actions
     * @param resourceBundle    the application resources used to choose menu text
     * @param reporter          the reporter object used to record errors
     * @return                  an (@link Optional) containing the (@link GiftCloudSystemTray) object or an empty (@link Optional) if the SystemTray is not supported, or an error occurred, e.g. in attempting to load the icon
     */
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

    /**
     * Updates the system tray menu according to enable/disable the show/hide menu items according to the visibility of the main window
     *
     * @param mainWindowVisibility  whether the main window is currently hidden or visible
     */
    void updateMenu(final GiftCloudMainFrame.MainWindowVisibility mainWindowVisibility) {
        if (tray == null) {
            return;
        }

        hideItem.setEnabled(mainWindowVisibility.isVisible());
        showItem.setEnabled(!mainWindowVisibility.isVisible());
    }
}
