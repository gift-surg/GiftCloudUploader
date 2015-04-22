package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.ArrayList;
import java.util.List;

public abstract class BackgroundServiceTaskList<T_taskType, T_resultType> {
    private final List<FailureRecord> uploadFailures = new ArrayList<FailureRecord>();

    public abstract void add(final T_taskType pendingItem, final BackgroundServiceErrorRecord errorRecord);
    public abstract BackgroundServiceTaskWrapper<T_taskType, T_resultType> take() throws InterruptedException;

    public void addFailure(final T_taskType pendingItem, final BackgroundServiceErrorRecord errorRecord) {
        uploadFailures.add(new FailureRecord(pendingItem, errorRecord));
    }

    public final void add(final T_taskType pendingItem) {
        add(pendingItem, new BackgroundServiceErrorRecord());
    }

    public final void retry(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> result) {
        if (result.shouldRetry()) {
            add(result.getTask(), result.getErrorRecord());
        } else {
            addFailure(result.getTask(), result.getErrorRecord());
        }
    }

    class FailureRecord {
        private T_taskType pendingItem;
        private BackgroundServiceErrorRecord errorRecord;

        FailureRecord(final T_taskType pendingItem, final BackgroundServiceErrorRecord errorRecord) {
            this.pendingItem = pendingItem;
            this.errorRecord = errorRecord;
        }
    }

}
