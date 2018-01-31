/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel

=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.apache.log4j.PropertyConfigurator;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

/**
 * Creates a reporting object suitable for headless (daemon) operation
 */
public class GiftCloudReporterFromDaemon implements GiftCloudReporter {

    private final GiftCloudLogger logger;

    public GiftCloudReporterFromDaemon(final GiftCloudLogger logger) {
        this.logger = logger;
        configureLogging();
    }

    @Override
    public void showMessageToUser(final String messageText) {
        logger.silentWarning(messageText);
    }

    @Override
    public void reportErrorToUser(String errorText, Throwable throwable) {
        String errorMessageForUser;
        String errorMessageForStatusBar;
        String errorMessageForLog;
        Optional<String> additionalText = Optional.empty();

        if (throwable instanceof GiftCloudException) {
            errorMessageForUser = "Error " + throwable.getLocalizedMessage();
            final String cause = throwable.getCause() != null ? " Cause:" + throwable.getCause().getLocalizedMessage() : "";
            errorMessageForLog = errorMessageForUser + cause;
        } else {
            errorMessageForUser = errorText;
            errorMessageForLog = errorMessageForUser;
        }

        logger.silentError(errorMessageForLog);
        throwable.printStackTrace(System.err);
    }

    @Override
    public void silentWarning(final String warning) {
        logger.silentWarning(warning);
    }

    @Override
    public void silentError(final String error) {
        logger.silentError(error);
    }

    @Override
    public void silentLogException(final Throwable throwable, final String errorMessage) {
        logger.silentLogException(throwable, errorMessage);
    }

    /**
     * Loads logging resources
     */
    private void configureLogging() {
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/log4j.properties"));
    }

    @Override
    public void startProgressBar(int maximum) {

    }

    @Override
    public void startProgressBar() {

    }

    @Override
    public void updateProgressBar(int value) {

    }

    @Override
    public void updateProgressBar(int value, int maximum) {

    }

    @Override
    public void endProgressBar() {

    }

    @Override
    public void updateStatusText(String progressText) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
