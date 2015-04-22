package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.ArrayList;
import java.util.List;

public abstract class BackgroundServicePendingList<TItemType, TResultType extends BackgroundServiceResult<TItemType>> {
    private final List<FailureRecord> uploadFailures = new ArrayList<FailureRecord>();

    public abstract void add(final TItemType pendingItem, final BackgroundServiceErrorRecord errorRecord);
    public abstract TResultType take() throws InterruptedException;

    public void addFailure(final TItemType pendingItem, final BackgroundServiceErrorRecord errorRecord) {
        uploadFailures.add(new FailureRecord(pendingItem, errorRecord));
    }

    public final void add(final TItemType pendingItem) {
        add(pendingItem, new BackgroundServiceErrorRecord());
    }

    public final void retry(final BackgroundServiceResult<TItemType> result) {
        if (result.shouldRetry()) {
            add(result.getPendingItem(), result.getErrorRecord());
        } else {
            addFailure(result.getPendingItem(), result.getErrorRecord());
        }
    }

    class FailureRecord {
        private TItemType pendingItem;
        private BackgroundServiceErrorRecord errorRecord;

        FailureRecord(final TItemType pendingItem, final BackgroundServiceErrorRecord errorRecord) {
            this.pendingItem = pendingItem;
            this.errorRecord = errorRecord;
        }
    }

}
