package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.ArrayList;
import java.util.List;

class BackgroundServiceErrorRecord {

    private final List<ErrorRecordItem> errorList = new ArrayList<ErrorRecordItem>();

    void addException(final Exception exception) {
        errorList.add(new ErrorRecordItem(exception));
    }

    public boolean shouldRetry() {
        return (errorList.size() < 3);
    }

    class ErrorRecordItem {
        private final Exception exception;

        ErrorRecordItem(final Exception exception) {
            this.exception = exception;
        }

    }
}
