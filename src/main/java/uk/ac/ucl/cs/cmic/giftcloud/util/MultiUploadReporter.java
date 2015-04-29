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

package uk.ac.ucl.cs.cmic.giftcloud.util;

import netscape.javascript.JSObject;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;

import java.awt.*;
import java.net.MalformedURLException;

public interface MultiUploadReporter extends Progress {

    void loadWebPage(String url) throws MalformedURLException;

    void exit();

    Container getContainer();

    JSObject getJSContext();

    void trace(String msg);
    void trace(String format, Object arg);
    void error(String msg, Throwable t);
    void info(String msg);
    void info(String msg, Throwable t);
    void warn(String msg);
    void debug(String msg);
    void debug(String format, Object arg1, Object arg2);
    void trace(String format, Object arg1, Object arg2);

    void debug(String msg, Throwable t);

    void info(String format, Object arg);

    void error(String format, Object arg);
    void error(String message);

    boolean isDebugEnabled();

    boolean askRetry(Component parentComponent, String title, String message);



    // The following methods are the new "preferred" methods for error and warning reporting


    /**
     * Used to display a message to the end user, unless running in background mode
     * @param errorText error text to display, unless a GiftCloudException is received
     * @param throwable exception to report. If this is a GiftCloudException then the exception's error text is used in
     *                  place of the error message, otherwise the exception's error text is appended to the error message
     */
    void reportErrorToUser(final String errorText, final Throwable throwable);

    /**
     * Indicates a warning that should not be reported to the user, but should be recorded in the log
     * @param warning the text of the warning
     */
    void silentWarning(final String warning);

    /**
     * Indicates that we wish to log an exception because it may be swallowed
     * @param errorMessage the text of the error
     */
    void silentLogException(final Throwable throwable, final String errorMessage);
}
