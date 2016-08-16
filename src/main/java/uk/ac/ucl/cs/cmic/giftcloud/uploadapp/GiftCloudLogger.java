/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

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
