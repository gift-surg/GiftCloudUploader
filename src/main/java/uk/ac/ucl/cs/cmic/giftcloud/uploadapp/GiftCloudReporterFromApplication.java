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


import com.pixelmed.display.DialogMessageLogger;
import com.pixelmed.display.SafeCursorChanger;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.utils.MessageLogger;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.awt.*;

public class GiftCloudReporterFromApplication implements GiftCloudReporter, MessageLogger, Progress {

    private final GiftCloudDialogs giftCloudDialogs;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GiftCloudReporterFromApplication.class);

    protected final SafeCursorChanger cursorChanger;
    protected final MessageLogger messageLogger;

    private final ProgressModel progressModel = new ProgressModel();

    public GiftCloudReporterFromApplication(final Container container, final GiftCloudDialogs giftCloudDialogs) {
        this.giftCloudDialogs = giftCloudDialogs;
        configureLogging();
        messageLogger = new DialogMessageLogger("GIFT-Cloud Log", 512, 384, false/*exitApplicationOnClose*/, false/*visible*/);
        cursorChanger = new SafeCursorChanger(container);
        cursorChanger.saveCursor();
    }

    @Override
    public void showMessageToUser(final String messageText) {
        giftCloudDialogs.showMessage(messageText);
        updateStatusText(messageText);
    }

    @Override
    public void reportErrorToUser(String errorText, Throwable throwable) {
        String errorMessageForUser;
        String errorMessageForStatusBar;
        String errorMessageForLog;
        Optional<String> additionalText = Optional.empty();

        if (throwable instanceof GiftCloudException) {
            errorMessageForUser = "Error " + throwable.getLocalizedMessage();
            errorMessageForStatusBar = errorMessageForUser;
            final String cause = throwable.getCause() != null ? " Cause:" + throwable.getCause().getLocalizedMessage() : "";
            errorMessageForLog = errorMessageForUser + cause;
        } else {
            errorMessageForUser = errorText;
            errorMessageForStatusBar = errorText + " " + throwable.getLocalizedMessage();
            errorMessageForLog = errorMessageForUser;
            additionalText = Optional.of(throwable.getLocalizedMessage());
        }

        giftCloudDialogs.showError("<html>" + errorMessageForUser + "</html>", additionalText);
        updateStatusText(errorMessageForStatusBar);
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

    public void setWaitCursor() {
        cursorChanger.setWaitCursor();
    }

    public void restoreCursor() {
        cursorChanger.restoreCursor();
    }

    public void addProgressListener(final Progress progress) {
        progressModel.addListener(progress);
    }

    public void startProgressBar(int maximum) {
        progressModel.startProgress(maximum);
    }

    public void startProgressBar() {
        progressModel.startProgress();
    }

    public void updateProgressBar(int value) {
        progressModel.updateProgressBar(value);
    }

    public void updateProgressBar(int value, int maximum) {
        progressModel.updateProgressBar(value, maximum);
    }

    public void endProgressBar() {
        progressModel.endProgressBar();
    }

    @Override
    public void updateStatusText(String progressText) {
        progressModel.updateProgressText(progressText);
        ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent(progressText));
    }

    @Override
    public boolean isCancelled() {
        return progressModel.isCancelled();
    }
}
