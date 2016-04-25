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

package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudDialogs;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.SwingProgressMonitorWrapper;

import javax.swing.*;
import java.applet.Applet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class GiftCloudReporterFromApplet extends GiftCloudReporterFromApplication {

    private Applet applet;

    private static final Logger logger = LoggerFactory.getLogger(GiftCloudReporterFromApplet.class);

    private final SwingProgressMonitorWrapper progressWrapper;

    public GiftCloudReporterFromApplet(final Applet applet, final GiftCloudDialogs dialogs) {
        super(applet, dialogs);
        this.applet = applet;
        configureLogging();

        progressWrapper = new SwingProgressMonitorWrapper(applet);
    }

    public void errorBox(final String errorMessage, final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter writer = new PrintWriter(sw);
        writer.println(errorMessage);
        writer.println("Error details:");
        throwable.printStackTrace(writer);
        final JTextArea text = new JTextArea(sw.toString());
        text.setEditable(false);
        applet.add(text);
        applet.validate();
    }

    public void messageBox(final String message) {
        final StringWriter sw = new StringWriter();
        final PrintWriter writer = new PrintWriter(sw);
        writer.println(message);
        final JTextArea text = new JTextArea(sw.toString());
        text.setEditable(false);
        applet.add(text);
        applet.validate();
    }


    @Override
    public void reportErrorToUser(String errorText, Throwable throwable) {
        String finalErrorText;
        if (throwable instanceof GiftCloudException) {
            finalErrorText = throwable.getLocalizedMessage();
        } else {
            finalErrorText = errorText + " " + throwable.getLocalizedMessage();
        }
        errorBox(finalErrorText, throwable);
        updateStatusText("GIFT-Cloud upload failed: " + throwable);
        logger.error("GIFT-Cloud upload failed: " + throwable.getLocalizedMessage(), throwable);
        throwable.printStackTrace(System.err);
    }

    @Override
    public void showMessageToUser(String messageText) {
        messageBox(messageText);
        updateStatusText("GIFT-Cloud upload failed: " + messageText);
        logger.error("GIFT-Cloud upload failed: " + messageText);
    }

    @Override
    public void silentWarning(String warning) {
        logger.info(warning);
    }

    @Override
    public void silentLogException(final Throwable throwable, final String errorMessage) {
        logger.info(errorMessage + ":" + throwable.getLocalizedMessage());
    }

    /**
     * Loads logging resources, including loading logging properties from custom URLs specified by the
     * LOG4J_PROPS_URL applet parameter.
     */
    private void configureLogging() {
        final String log4jProps = applet.getParameter(MultiUploadParameters.LOG4J_PROPS_URL);
        if (StringUtils.isNotBlank(log4jProps)) {
            try {
                PropertyConfigurator.configure(new URL(log4jProps));
            } catch (MalformedURLException e) {
                logger.error("Unable to read remote log4j configuration file " + log4jProps, e);
            }
        }
    }

    @Override
    public void startProgressBar(int maximum) {
        progressWrapper.startProgressBar(maximum);
    }

    @Override
    public void startProgressBar() {
        progressWrapper.startProgressBar();
    }

    @Override
    public void updateProgressBar(int value) {
        progressWrapper.updateProgressBar(value);
    }

    @Override
    public void updateProgressBar(int value, int maximum) {
        progressWrapper.updateProgressBar(value, maximum);
    }

    @Override
    public void endProgressBar() {
        progressWrapper.endProgressBar();
    }

    @Override
    public void updateStatusText(String progressText) {
        progressWrapper.updateStatusText(progressText);
    }

    @Override
    public boolean isCancelled() {
        return progressWrapper.isCancelled();
    }
}
