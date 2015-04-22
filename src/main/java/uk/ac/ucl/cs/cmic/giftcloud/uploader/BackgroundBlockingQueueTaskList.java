package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BackgroundBlockingQueueTaskList<T> extends BackgroundServiceTaskList<T, T> {
    private final BlockingQueue<BackgroundServiceTaskWrapper<T, T>> pendingUploadItemList = new LinkedBlockingQueue<BackgroundServiceTaskWrapper<T, T>>();

    @Override
    public void add(T task, BackgroundServiceErrorRecord errorRecord) {
        pendingUploadItemList.add(new BackgroundServiceTaskWrapper<T, T>(task, task, errorRecord));
    }

    @Override
    public BackgroundServiceTaskWrapper<T, T> take() throws InterruptedException {
        return pendingUploadItemList.take();
    }
}
