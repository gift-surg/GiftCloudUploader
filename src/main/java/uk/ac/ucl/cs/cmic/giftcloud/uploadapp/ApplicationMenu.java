/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.BackgroundService;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 *  Creates an application menu, allowing the user to execute actions and monitor progress from the GIFT-Cloud uploader while the main window is hidden
 *
 * <p>This class is part of the GIFT-Cloud Uploader
 *
 * @author  Tom Doel
 */
public class ApplicationMenu {

    private final JMenuBar menuBar = new JMenuBar();
    private final JFrame frame;
    private JMenuItem startUploaderItem;
    private JMenuItem pauseUploaderItem;
    private JMenuItem hideItem;
    private JMenuItem showItem;
    private boolean startIsResume = false;
    private final String resumeText;

    /**
     * Creates a new application menu
     *
     * @param frame          the frame to whcih the menu will be added
     * @param controller     the controller used to perform menu actions
     * @param resourceBundle the application resources used to choose menu text
     * @param reporter
     * @param isMac          true if this is running on a Mac system
     * @throws AWTException if the desktop system tray is missing
     * @throws IOException  if an error occurred while attempting to read the icon file
     */
    public ApplicationMenu(final JFrame frame, final UploaderGuiController controller, final ResourceBundle resourceBundle, final boolean isMac, final GiftCloudReporterFromApplication reporter) throws AWTException, IOException {

        this.frame = frame;
        // After pausing, the "start" item changes to "resume"
        resumeText = resourceBundle.getString("menuResumeUploader");

        // Register custom events for OSX built-in menu functions (About, Preferences, Quit)
        if (isMac) {
            MRJApplicationUtils.registerAboutHandler(new MRJAboutHandler() {
                @Override
                public void handleAbout() {
                    controller.showAboutDialog();
                }
            });
            MRJApplicationUtils.registerPrefsHandler(new MRJPrefsHandler() {
                @Override
                public void handlePrefs() throws IllegalStateException {
                    controller.showConfigureDialog(false, true);
                }
            });
            MRJApplicationUtils.registerQuitHandler(new MRJQuitHandler() {
                @Override
                public void handleQuit() {
                    controller.quit();
                }
            });
        }

        {
            JMenu menu = new JMenu("File");
            menu.setMnemonic(KeyEvent.VK_F);
            menu.getAccessibleContext().setAccessibleDescription("File");

            {
                JMenuItem menuItem = new JMenuItem(resourceBundle.getString("menuImport"), KeyEvent.VK_F);
                menuItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuImportToolTipText"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.selectAndImport();
                    }
                });
                menu.add(menuItem);
            }
            {
                JMenuItem menuItem = new JMenuItem(resourceBundle.getString("menuImportFromPacs"), KeyEvent.VK_P);
                menuItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuImportFromPacsToolTipText"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.importFromPacs();
                    }
                });
                menu.add(menuItem);
            }
            if (!isMac) {
                JMenuItem menuItem = new JMenuItem(resourceBundle.getString("menuExitWin"), KeyEvent.VK_Q);
                menuItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuExitWin"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.quit();
                    }
                });
                menu.add(menuItem);
            }
            menuBar.add(menu);
        }

        {
            JMenu menu = new JMenu("Uploader");
            menu.setMnemonic(KeyEvent.VK_U);
            menu.getAccessibleContext().setAccessibleDescription("Uploader");

            {
                {
                    startUploaderItem = new JMenuItem(resourceBundle.getString("menuStartUploader"));
                    startUploaderItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuStartUploader"));
                    startUploaderItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            controller.startUploading();
                        }
                    });
                    menu.add(startUploaderItem);
                }
                {
                    pauseUploaderItem = new JMenuItem(resourceBundle.getString("menuPauseUploader"));
                    pauseUploaderItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuPauseUploader"));
                    pauseUploaderItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            controller.pauseUploading();
                        }
                    });
                    menu.add(pauseUploaderItem);
                }
                menu.addSeparator();
                {
                    JMenuItem menuItem = new JMenuItem(resourceBundle.getString("menuRestartListener"));
                    menuItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuRestartListener"));
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            controller.restartDicomService();
                        }
                    });
                    menu.add(menuItem);
                }
            }
            menuBar.add(menu);
        }

       if (!isMac) {
            JMenu menu = new JMenu("Tools");
            menu.setMnemonic(KeyEvent.VK_T);
            menu.getAccessibleContext().setAccessibleDescription("Tools");

            {
                JMenuItem menuItem = new JMenuItem(resourceBundle.getString("menuSettingsWin"));
                menuItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuSettingsTooltip"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.showConfigureDialog(false, true);
                    }
                });
                menu.add(menuItem);
            }

            menuBar.add(menu);
        }

        {
            JMenu menu = new JMenu("View");
            menu.setMnemonic(KeyEvent.VK_V);
            menu.getAccessibleContext().setAccessibleDescription("View");

            {
                hideItem = new JMenuItem(resourceBundle.getString("menuHide"), KeyEvent.VK_H);
                hideItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuHide"));
                hideItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.hide(true);
                    }
                });
                menu.add(hideItem);
            }
            {
                showItem = new JMenuItem(resourceBundle.getString("menuShow"), KeyEvent.VK_S);
                showItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuShow"));
                showItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.show(true);
                    }
                });
                menu.add(showItem);
            }

            menuBar.add(menu);
        }
        if (!isMac) {
            JMenu menu = new JMenu("Help");
            menu.setMnemonic(KeyEvent.VK_H);
            menu.getAccessibleContext().setAccessibleDescription("Help");

            {
                JMenuItem menuItem = new JMenuItem(resourceBundle.getString("menuAbout"));
                menuItem.getAccessibleContext().setAccessibleDescription(resourceBundle.getString("menuAbout"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.showAboutDialog();
                    }
                });
                menu.add(menuItem);
            }

            menuBar.add(menu);
        }
        frame.setJMenuBar(menuBar);
    }

    public void remove() {
        frame.setJMenuBar(null);
    }

    /**
     * Static factory method for creating the GIFT-Cloud Uploader menu
     *
     * <p>Does not throw any exceptions. If the menu cannot be created or an error occurs, an empty Optional is returned.
     *
     * @param controller        the controller used to perform menu actions
     * @param resourceBundle    the application resources used to choose menu text
     * @param reporter          the reporter object used to record errors
     * @return                  an (@link Optional) containing the (@link ApplicationMenu) object or an empty (@link Optional) if the menu is not supported, or an error occurred
     */
    static Optional<ApplicationMenu> safeCreateMenu(final JFrame frame, final UploaderGuiController controller, final ResourceBundle resourceBundle, final boolean isMac, final GiftCloudReporterFromApplication reporter) {
        try {
            return Optional.of(new ApplicationMenu(frame, controller, resourceBundle, isMac, reporter));
        } catch (Throwable t) {
            reporter.silentLogException(t, "The application menu could not be created.");
            return Optional.empty();
        }
    }

    /**
     * Updates the system tray menu according to enable/disable the show/hide menu items according to the visibility of the main window
     *
     * @param mainWindowVisibility  whether the main window is currently hidden or visible
     */
    void updateMenuForWindowVisibility(final MainFrame.MainWindowVisibility mainWindowVisibility) {
        hideItem.setEnabled(mainWindowVisibility.isVisible());
        showItem.setEnabled(!mainWindowVisibility.isVisible());
    }

    /**
     * Updates the system tray menu according to enable/disable the start/pause menu items according to the status of the uploading thread
     *
     * @param serviceStatus  whether the uploader service is currently running
     */
    void updateMenuForBackgroundUploadingServiceStatus(final BackgroundService.ServiceStatus serviceStatus) {
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
            startUploaderItem.setText(resumeText);
            startIsResume = true;
        }
    }
}
