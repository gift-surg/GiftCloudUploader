/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.LoggingReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Provides essential application configuration, logging and preference file loading. Generally this class should be instantiated before any GUI items are created
 */
public class GiftCloudUploaderConfiguration {
    private static String resourceBundleName = "uk.ac.ucl.cs.cmic.giftcloud.GiftCloudUploader";

    private final ResourceBundle resourceBundle;
    private Optional<MainFrame> mainFrame = Optional.empty();
    private final GiftCloudLogger logger;

    public static String propertiesFileName  = "GiftCloudUploader.properties";

    private final GiftCloudPropertiesFromApplication properties;

    public GiftCloudUploaderConfiguration() {
        /// Get up root folder for logging
        final File appRoot = GiftCloudUtils.createOrGetGiftCloudFolder(Optional.<LoggingReporter>empty());
        System.setProperty("app.root", appRoot.getAbsolutePath());
        logger = new GiftCloudLogger();

        resourceBundle = ResourceBundle.getBundle(resourceBundleName);

        // Initialise application properties
        properties = new GiftCloudPropertiesFromApplication(new PropertyStoreFromApplication(propertiesFileName, logger), resourceBundle, logger);
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public GiftCloudLogger getLogger() {
        return logger;
    }

    public GiftCloudPropertiesFromApplication getProperties() {
        return properties;
    }
}
