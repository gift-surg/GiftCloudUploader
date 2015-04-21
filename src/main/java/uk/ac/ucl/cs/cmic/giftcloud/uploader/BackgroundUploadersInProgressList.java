package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;

import java.util.*;
import java.util.concurrent.*;

public class BackgroundUploadersInProgressList extends BackgroundServicePendingList<BackgroundUploaderItem, Future<Set<String>>, BackgroundUploadersInProgressList.BackgroundUploaderResult> {
    private final CompletionService<Set<String>> completionService;
    private final Map<Future<Set<String>>, BackgroundUploaderResult> uploaderResultMap = new HashMap<Future<Set<String>>, BackgroundUploaderResult>();
    private final ExecutorService executor;

    BackgroundUploadersInProgressList() {
        final int numThreads = 1;

        executor = Executors.newFixedThreadPool(numThreads);
        completionService = new ExecutorCompletionService<Set<String>>(executor);
    }

    @Override
    public void add(final BackgroundUploaderItem pendingItem, final BackgroundServiceErrorRecord errorRecord) {
        final Future<Set<String>> future = completionService.submit(pendingItem.getCallableUploader());
        uploaderResultMap.put(future, new BackgroundUploaderResult(pendingItem, future, errorRecord));
    }

    @Override
    public BackgroundUploaderResult take() throws InterruptedException {
        final Future<Set<String>> future = completionService.take();
        return uploaderResultMap.remove(future);
    }

    void add(final CallableUploader callableUploader) {
        add(new BackgroundUploaderItem(callableUploader));
    }

    class BackgroundUploaderResult extends BackgroundServiceResult<BackgroundUploaderItem, Future<Set<String>>> {
        BackgroundUploaderResult(final BackgroundUploaderItem pendingItem, final Future<Set<String>> result, final BackgroundServiceErrorRecord errorRecord) {
            super(pendingItem, result, errorRecord);
        }
    }

}
