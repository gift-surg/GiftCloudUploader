/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.apple.eawt.Application;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.imageio.ImageIO;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Provides essential application configuration, logging and preference file loading. Generally this class should be instantiated before any GUI items are created
 */
public class GiftCloudUploaderAppConfiguration {
    private static String mainLogoURLString = "/uk/ac/ucl/cs/cmic/giftcloud/GiftCloudLogo.png";
    private static String trayIconURLString  = "/uk/ac/ucl/cs/cmic/giftcloud/TrayIcon.png";

    private static String icon16 = "/uk/ac/ucl/cs/cmic/giftcloud/gs16x16.png";
    private static String icon32 = "/uk/ac/ucl/cs/cmic/giftcloud/gs32x32.png";
    private static String icon64 = "/uk/ac/ucl/cs/cmic/giftcloud/gs64x64.png";
    private static String icon128 = "/uk/ac/ucl/cs/cmic/giftcloud/gs128x128.png";
    private static String icon256 = "/uk/ac/ucl/cs/cmic/giftcloud/gs256x256.png";
    private static String icon512 = "/uk/ac/ucl/cs/cmic/giftcloud/gs512x512.png";
    private static String icon1024 = "/uk/ac/ucl/cs/cmic/giftcloud/gs1024x1024.png";

    private Optional<SingleInstanceService> singleInstanceService;
    private Optional<MainFrame> mainFrame = Optional.empty();
    private final String applicationTitle;
    private final Optional<ImageIcon> mainLogo;

    private final GiftCloudUploaderConfiguration configuration;

    public GiftCloudUploaderAppConfiguration() {
        configuration = new GiftCloudUploaderConfiguration();

        // Use the Java Web Start single instance mechanism to ensure only one instance of the application is running at a time. This is critical as the properties and patient list caching is not safe across multiple instances
        try {
            singleInstanceService = Optional.of((SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService"));
            GiftCloudUploaderSingleInstanceListener singleInstanceListener = new GiftCloudUploaderSingleInstanceListener();
            singleInstanceService.get().addSingleInstanceListener(singleInstanceListener);
        } catch (UnavailableServiceException e) {
            singleInstanceService = Optional.empty();
        }

        applicationTitle = configuration.getResourceBundle().getString("applicationTitle");

        mainLogo = loadImageIcon(mainLogoURLString);

        if (isOSX()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.UIElement", "true");

            // This is used to set the application title on OSX, but may not work when run from the debugger
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationTitle);

            final Optional<Image> dockIconImage = loadIcon(icon1024);
            if (dockIconImage.isPresent()) {
                Application.getApplication().setDockIconImage(dockIconImage.get());
            }

        }
    }

    private Optional<ImageIcon> loadImageIcon(final String urlString) {
        final URL url = GiftCloudUploaderApp.class.getResource(urlString);
        if (url == null) {
            getLogger().silentWarning("Could not find the Uploader icon resource.");
        } else {
            try {
                return Optional.of(new ImageIcon(url));
            } catch (Exception e) {
                getLogger().silentLogException(e, "Failed to load the Uploader icon.");
            }
        }
        return Optional.empty();
    }

    private Optional<Image> loadIcon(final String urlString) {
        final URL url = GiftCloudUploaderApp.class.getResource(urlString);

        if (url == null) {
            getLogger().silentWarning("Could not find the Uploader icon resource.");
        } else {
            try {
                final Image loadedImage = ImageIO.read(url);
                if (loadedImage == null) {
                    getLogger().silentWarning("Could not find the Uploader icon.");
                } else {
                    return Optional.of(loadedImage);
                }
            } catch (Exception e) {
                getLogger().silentLogException(e, "Failed to load the Uploader icon.");
            }
        }
        return Optional.empty();
    }

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public void registerMainFrame(final MainFrame mainFrame) {
        this.mainFrame = Optional.of(mainFrame);
    }

    public ResourceBundle getResourceBundle() {
        return configuration.getResourceBundle();
    }

    private static boolean isOSX() {
        return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }

    public ImageIcon getMainLogo() {
        return mainLogo.orElse(null);
    }

    public GiftCloudLogger getLogger() {
        return configuration.getLogger();
    }

    public GiftCloudPropertiesFromApplication getProperties() {
        return configuration.getProperties();
    }

    public java.util.List<Image> getIconList() {
        final java.util.List<Image> imageList = new ArrayList<Image>();
        addImageIfExists(imageList, icon16);
        addImageIfExists(imageList, icon32);
        addImageIfExists(imageList, icon64);
        addImageIfExists(imageList, icon128);
        addImageIfExists(imageList, icon256);
        addImageIfExists(imageList, icon512);
        addImageIfExists(imageList, icon1024);

        return imageList;
    }

    private void addImageIfExists(final java.util.List<Image> imageList, final String resourceName) {
        final Optional<Image> image = loadIcon(resourceName);
        if (image.isPresent()) {
            imageList.add(image.get());
        }
    }

    public Image getTrayIcon() throws IOException {
        final Optional<Image> image = loadIcon(trayIconURLString);
        return image.orElse(null);
    }

    /**
     * This class is used in conjunction with Java Web Start to ensure singleton operation
     */
    private class GiftCloudUploaderSingleInstanceListener implements SingleInstanceListener {

        public GiftCloudUploaderSingleInstanceListener() {

            // Add a shutdown hook to unregister the single instance
            // ShutdownHook will run regardless of whether Command-Q (on Mac) or window closed ...
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (singleInstanceService.isPresent()) {
                        singleInstanceService.get().removeSingleInstanceListener(GiftCloudUploaderSingleInstanceListener.this);
                    }
                }
            });

        }

        @Override
        public void newActivation(String[] strings) {
            if (mainFrame.isPresent()) {
                mainFrame.get().show();
            }
        }
    }
}
