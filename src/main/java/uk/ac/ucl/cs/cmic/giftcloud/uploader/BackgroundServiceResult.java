package uk.ac.ucl.cs.cmic.giftcloud.uploader;

public class BackgroundServiceResult<T> {
    private final T pendingItem;
    private final BackgroundServiceErrorRecord errorRecord;

    BackgroundServiceResult(final T pendingItem, final BackgroundServiceErrorRecord errorRecord) {
        this.pendingItem = pendingItem;
        this.errorRecord = errorRecord;
    }

    T getPendingItem() {
        return pendingItem;
    }

    BackgroundServiceErrorRecord getErrorRecord() {
        return errorRecord;
    }

    public void addError(final Exception e) {
        errorRecord.addException(e);
    }

    public boolean shouldRetry() {
        return (errorRecord.shouldRetry());
    }
}
