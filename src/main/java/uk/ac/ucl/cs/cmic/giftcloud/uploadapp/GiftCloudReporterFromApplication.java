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
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class GiftCloudReporterFromApplication implements GiftCloudReporter, MessageLogger, Progress {

    private final Container container;
    private final GiftCloudDialogs giftCloudDialogs;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GiftCloudReporterFromApplication.class);

    protected final SafeCursorChanger cursorChanger;
    protected final MessageLogger messageLogger;

    private final ProgressModel progressModel = new ProgressModel();

    public GiftCloudReporterFromApplication(final Container container, final GiftCloudDialogs giftCloudDialogs) {
        this.container = container;
        this.giftCloudDialogs = giftCloudDialogs;
        configureLogging();
        messageLogger = new DialogMessageLogger("GIFT-Cloud Log", 512, 384, false/*exitApplicationOnClose*/, false/*visible*/);
        cursorChanger = new SafeCursorChanger(container);
        cursorChanger.saveCursor();
    }

    private void errorBox(final String errorMessage, final Optional<String> additionalText) {
        final StringWriter sw = new StringWriter();
        final PrintWriter writer = new PrintWriter(sw);
        writer.println(errorMessage);
        writer.println("Error details:");
        writer.println(additionalText);
        final JTextArea text = new JTextArea(sw.toString());
        text.setEditable(false);
        container.add(text);
        container.validate();
    }

    @Override
    public Container getContainer() {
        return container;
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

        giftCloudDialogs.showError(errorMessageForUser, additionalText);
        updateStatusText(errorMessageForStatusBar);
        logger.error(errorMessageForLog);
        throwable.printStackTrace(System.err);
    }

    @Override
    public void silentWarning(final String warning) {
        logger.info(warning);
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

    public void setWaitCursor() {
        cursorChanger.setWaitCursor();
    }

    public void restoreCursor() {
        cursorChanger.restoreCursor();
    }


    public void addProgressListener(final Progress progress) {
        progressModel.addListener(progress);
    }

    @Override
    public void sendLn(String message) {
        messageLogger.sendLn(message);
    }

    @Override
    public void send(String message) {
        messageLogger.send(message);
    }

    public void showMesageLogger() {
        if (logger instanceof DialogMessageLogger) {
            ((DialogMessageLogger) logger).setVisible(true);
        }
    }

    public void showError(final String errorMessage) {
        giftCloudDialogs.showError(errorMessage, Optional.<String>empty());
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


    // These are the preferred methods for reporting to the user

    public void silentError(final String errorMessage, final Throwable throwable) {
        if (throwable == null) {
            messageLogger.sendLn(errorMessage);
        } else {
            messageLogger.sendLn(errorMessage + " with exception:" + throwable.getLocalizedMessage());
        }
    }

    public void warnUser(final String warningMessage) {
        giftCloudDialogs.showMessage(warningMessage);
    }


}
