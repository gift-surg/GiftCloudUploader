package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;

import java.util.ArrayList;
import java.util.List;

class BackgroundServiceErrorRecord {

    private final List<ErrorRecordItem> errorList = new ArrayList<ErrorRecordItem>();
    private boolean allowRetry = true;

    void addException(final Throwable exception) {
        errorList.add(new ErrorRecordItem(exception));
        if (exception instanceof GiftCloudException && !((GiftCloudException)exception).allowRetry()) {
            allowRetry = false;
        }
    }

    boolean shouldRetry() {
        return (errorList.size() < 3) && allowRetry;
    }

    List<ErrorRecordItem> getErrorList() {
        return errorList;
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
}
