package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.ArrayList;
import java.util.List;

class BackgroundServiceErrorRecord {

    private final List<ErrorRecordItem> errorList = new ArrayList<ErrorRecordItem>();

    void addException(final Throwable exception) {
        errorList.add(new ErrorRecordItem(exception));
    }

    boolean shouldRetry() {
        return (errorList.size() < 3);
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
