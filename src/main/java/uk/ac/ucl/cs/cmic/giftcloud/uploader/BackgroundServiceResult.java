package uk.ac.ucl.cs.cmic.giftcloud.uploader;

public class BackgroundServiceResult<T1 extends BackgroundServicePendingItem, T2> {
    private final T1 pendingItem;
    private final T2 result;
    private final BackgroundServiceErrorRecord errorRecord;

    BackgroundServiceResult(final T1 pendingItem, final T2 result, final BackgroundServiceErrorRecord errorRecord) {
        this.pendingItem = pendingItem;
        this.result = result;
        this.errorRecord = errorRecord;
    }

    T1 getPendingItem() {
        return pendingItem;
    }

    T2 getResult() {
        return result;
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
