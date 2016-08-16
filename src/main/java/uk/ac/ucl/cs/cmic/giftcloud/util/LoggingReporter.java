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

public interface LoggingReporter {

    /**
     * Indicates a warning that should not be reported to the user, but should be recorded in the log
     * @param warning the text of the warning
     */
    void silentWarning(final String warning);

    /**
     * Indicates an error that should not be reported to the user, but should be recorded in the log
     * @param error the text of the error
     */
    void silentError(final String error);

    /**
     * Indicates that we wish to log an exception because it may be swallowed
     * @param errorMessage the text of the error
     */
    void silentLogException(final Throwable throwable, final String errorMessage);
}
