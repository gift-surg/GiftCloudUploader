package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.apple.eawt.Application;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.LoggingReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.imageio.ImageIO;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Provides essential application configuration, logging and preference file loading. Generally this class should be instantiated before any GUI items are created
 */
public class GiftCloudUploaderAppConfiguration {
    private static String resourceBundleName  = "uk.ac.ucl.cs.cmic.giftcloud.GiftCloudUploader";
    private static String iconURLString  = "/uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png";
    private Optional<SingleInstanceService> singleInstanceService;
    private final ResourceBundle resourceBundle;
    private Optional<MainFrame> mainFrame = Optional.empty();
    private final String applicationTitle;
    private Optional<Image> iconImage = Optional.empty();
    private final GiftCloudLogger logger;
    private final GiftCloudPropertiesFromApplication properties;

    public GiftCloudUploaderAppConfiguration() {
        /// Get up root folder for logging
        final File appRoot = GiftCloudUtils.createOrGetGiftCloudFolder(Optional.<LoggingReporter>empty());
        System.setProperty("app.root", appRoot.getAbsolutePath());
        logger = new GiftCloudLogger();

        // Use the Java Web Start single instance mechanism to ensure only one instance of the application is running at a time. This is critical as the properties and patient list caching is not safe across multiple instances
        try {
            singleInstanceService = Optional.of((SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService"));
            GiftCloudUploaderSingleInstanceListener singleInstanceListener = new GiftCloudUploaderSingleInstanceListener();
            singleInstanceService.get().addSingleInstanceListener(singleInstanceListener);
        } catch (UnavailableServiceException e) {
            singleInstanceService = Optional.empty();
        }

        resourceBundle = ResourceBundle.getBundle(resourceBundleName);
        applicationTitle = resourceBundle.getString("applicationTitle");

        // Set the dock icon
        final URL iconURL = GiftCloudUploaderApp.class.getResource(iconURLString);

        if (iconURL == null) {
            logger.silentWarning("Could not find the Uploader icon resource.");
        } else {
            try {
                final Image loadedImage = ImageIO.read(iconURL);
                if (loadedImage == null) {
                    logger.silentWarning("Could not find the Uploader icon.");
                } else {
                    iconImage = Optional.of(loadedImage);

                    // OSX-specific code to set dock icon
                    if (isOSX()) {
                        Application.getApplication().setDockIconImage(iconImage.get());
                    }
                }
            } catch (Exception e) {
                logger.silentLogException(e, "Failed to load the Uploader icon.");
            }
        }

        if (isOSX()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.UIElement", "true");

            // This is used to set the application title on OSX, but may not work when run from the debugger
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationTitle);
        }

        // Initialise application properties
        properties = new GiftCloudPropertiesFromApplication(new PropertyStoreFromApplication(GiftCloudMainFrame.propertiesFileName, logger), resourceBundle, logger);
    }

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public void registerMainFrame(final MainFrame mainFrame) {
        this.mainFrame = Optional.of(mainFrame);
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    private static boolean isOSX() {
        return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }

    public Optional<Image> getIconImage() {
        return iconImage;
    }

    public ImageIcon getLargeIcon() {
        return new ImageIcon(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftCloud.png"));
    }

    public GiftCloudLogger getLogger() {
        return logger;
    }

    public GiftCloudPropertiesFromApplication getProperties() {
        return properties;
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
