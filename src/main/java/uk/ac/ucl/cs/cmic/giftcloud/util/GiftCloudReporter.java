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

import uk.ac.ucl.cs.cmic.giftcloud.Progress;

public interface GiftCloudReporter extends LoggingReporter, Progress {

    /**
     * Used to display a message to the end user, unless running in background mode
     * @param errorText error text to display, unless a GiftCloudException is received
     * @param throwable exception to report. If this is a GiftCloudException then the exception's error text is used in
     *                  place of the error message, otherwise the exception's error text is appended to the error message
     */
    void reportErrorToUser(final String errorText, final Throwable throwable);

    /**
     * Used to display a message to the end user, unless running in background mode
     * @param messageText text to display
     */
    void showMessageToUser(final String messageText);
}
