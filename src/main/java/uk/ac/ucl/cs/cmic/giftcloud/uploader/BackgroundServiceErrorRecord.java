/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;

import java.util.ArrayList;
import java.util.List;

class BackgroundServiceErrorRecord {

    private final List<ErrorRecordItem> errorList = new ArrayList<ErrorRecordItem>();
    private boolean allowRetry = true;
    private final int[] delays;

    void addException(final Throwable exception) {
        errorList.add(new ErrorRecordItem(exception));
        if (exception instanceof GiftCloudException && !((GiftCloudException)exception).allowRetry()) {
            allowRetry = false;
        }
    }

    boolean shouldRetry() {
        return (errorList.size() < delays.length) && allowRetry;
    }

    List<ErrorRecordItem> getErrorList() {
        return errorList;
    }

    int getNextDelay() {
        int delayIndex = errorList.size() < delays.length ? errorList.size() : delays.length - 1;
        return delays[delayIndex];
    }

    class ErrorRecordItem {
        private final Throwable exception;

        ErrorRecordItem(final Throwable exception) {
            this.exception = exception;
        }

        Throwable getException() {
            return exception;
        }
    }

    private BackgroundServiceErrorRecord(final int[] delays) {
        this.delays = delays;
    }

    static BackgroundServiceErrorRecord createExponentialRepeater() {
        return new BackgroundServiceErrorRecord(new int[] {0, 60000, 3600000});
    }

    static BackgroundServiceErrorRecord createExtendedExponentialRepeater() {
        return new BackgroundServiceErrorRecord(new int[] {0, 1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000, 2048000, 4096000, 8192000, 16384000, 32768000, 65536000});
    }

    static BackgroundServiceErrorRecord createInstantRepeater() {
        return new BackgroundServiceErrorRecord(new int[] {0, 0, 0});
    }

    static BackgroundServiceErrorRecord createSingleTry() {
        return new BackgroundServiceErrorRecord(new int[] {0});
    }
}
