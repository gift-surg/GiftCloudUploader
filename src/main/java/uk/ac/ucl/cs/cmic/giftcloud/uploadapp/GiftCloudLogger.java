/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import uk.ac.ucl.cs.cmic.giftcloud.util.LoggingReporter;

public class GiftCloudLogger implements LoggingReporter {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GiftCloudLogger.class);

    public GiftCloudLogger() {
        configureLogging();
    }

    @Override
    public void silentWarning(final String warning) {
        logger.info(warning);
    }

    @Override
    public void silentError(final String error) {
        logger.error(error);
    }

    @Override
    public void silentLogException(final Throwable throwable, final String errorMessage) {
        logger.info(errorMessage + ":" + throwable.getLocalizedMessage());
    }

    /**
     * Loads logging resources
     */
    private void configureLogging() {
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/log4j.properties"));
    }
}
