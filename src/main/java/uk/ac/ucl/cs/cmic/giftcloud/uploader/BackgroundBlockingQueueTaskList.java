package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.concurrent.DelayQueue;

/**
 * A {@link BackgroundServiceTaskList} that is implemented using a blocking queue. This means that take() will wait
 * until an item is available before returning.
 *
 * @param <T> the type of the item that will be added to the queue. This item will also be the return value
 */
public class BackgroundBlockingQueueTaskList<T> extends BackgroundServiceTaskList<T, T> {
    private final DelayQueue<BackgroundServiceTaskWrapper<T, T>> pendingUploadItemList = new DelayQueue<BackgroundServiceTaskWrapper<T, T>>();
    private long fileNum = 0;

    @Override
    public void add(T task, BackgroundServiceErrorRecord errorRecord) {
        pendingUploadItemList.add(new BackgroundServiceTaskWrapper<T, T>(task, task, errorRecord, fileNum++));
    }

    @Override
    public BackgroundServiceTaskWrapper<T, T> take() throws InterruptedException {
        return pendingUploadItemList.take();
    }

    @Override
    protected boolean isEmpty() {
        return pendingUploadItemList.isEmpty();
    }

    @Override
    protected BackgroundServiceErrorRecord createErrorRecord() {
        return BackgroundServiceErrorRecord.createExponentialRepeater();
    }

}
