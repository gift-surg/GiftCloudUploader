/*
 * uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.UIUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;

public class UIUtils {
    private UIUtils() {
    }

    public static final String DEFAULT_DATE_FORMAT = "d MMM yyyy";

    public static Frame findParentFrame(final Component component) {
        for (Component c = component; null != c; c = c.getParent()) {
            if (c instanceof Frame) {
                return (Frame) c;
            }
        }
        return null;
    }

    /**
     * Initializes the cookie handler. This takes the root URL for the XNAT REST service as its parameter. All cookies
     * will be associated with that URL.
     * @param xnatUrl    The root URL for the XNAT REST service.
     * @throws URISyntaxException Thrown when the XNAT URL parameter is an invalid syntax for constructing a URI.
     * @throws IOException Thrown when there are format or validation errors with the submitted URL parameter.
     */

    /**
     * Handles applet errors in a consistent way.
     *
     * @param component The component from which the error was dispatched.
     * @param throwable A <b>Throwable</b> error object.
     */
    public static void handleAppletError(Component component, Throwable throwable) {
        String message = throwable.getMessage();
        String formattedMessage = StringUtils.isBlank(message) ? "" : message + "\n";
        handleAppletError(component, "Found exception of type: " + throwable.getClass().getName() + "\n" + formattedMessage + formatStackTrace(throwable.getStackTrace()));
    }

    /**
     * Handles applet errors in a consistent way.
     * @param component The component from which the error was dispatched.
     * @param message A message indicating the error.
     */
    public static void handleAppletError(Component component, String message) {
        handleAppletError(component, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Handles applet errors in a consistent way.
     * @param component The component from which the error was dispatched.
     * @param message A message indicating the error.
     * @param title The title for the error message.
     * @param messageType The type of error display.
     */
    public static void handleAppletError(Component component, String message, String title, int messageType) {
        Logger log;
        if (component != null) {
            if (StringUtils.isBlank(title)) {
                title = "Error";
            }
            JOptionPane.showMessageDialog(component, message, title, messageType);
            log = LoggerFactory.getLogger(component.getClass());
        } else {
            log = _log;
        }
        log.error(message);
    }

    /**
     * Displays a modal input box with the indicated message, title, and message type, which should be taken from the
     * <b>{@link JOptionPane}</b> class. The default value is set to the indicated value. Returns the input user value.
     * @param component    The component from which the input request was dispatched.
     * @param message      The message to be displayed.
     * @param title        The title to be displayed.
     * @param messageType  The message type.
     * @param defaultValue The default value to be populated.
     * @return The value entered by the user.
     */
    public static String handleAppletInput(Component component, String message, String title, int messageType, String defaultValue) {
        return (String) JOptionPane.showInputDialog(component, message, title, messageType, null, null, defaultValue);
    }

    public static String formatStackTrace(StackTraceElement[] elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        for (StackTraceElement element : elements) {
            buffer.append(element.toString()).append("\n");
        }
        return buffer.toString();
    }

    private static final Logger _log = LoggerFactory.getLogger(UIUtils.class);

    public static boolean getConfirmSessionDatePage() {
        return Boolean.parseBoolean(System.getProperty("verification.date.display", "true"));
    }
}
