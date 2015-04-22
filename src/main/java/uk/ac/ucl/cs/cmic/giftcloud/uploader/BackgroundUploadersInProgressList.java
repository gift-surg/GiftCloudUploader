package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;

import java.util.*;
import java.util.concurrent.*;

public class BackgroundUploadersInProgressList extends BackgroundServicePendingList<CallableUploader, BackgroundUploadersInProgressList.BackgroundUploaderResult> {
    private final CompletionService<Set<String>> completionService;
    private final Map<Future<Set<String>>, BackgroundUploaderResult> uploaderResultMap = new HashMap<Future<Set<String>>, BackgroundUploaderResult>();
    private final ExecutorService executor;

    BackgroundUploadersInProgressList() {
        final int numThreads = 1;

        executor = Executors.newFixedThreadPool(numThreads);
        completionService = new ExecutorCompletionService<Set<String>>(executor);
    }

    @Override
    public void add(final CallableUploader callableUploader, final BackgroundServiceErrorRecord errorRecord) {
        final Future<Set<String>> future = completionService.submit(callableUploader);
        uploaderResultMap.put(future, new BackgroundUploaderResult(callableUploader, future, errorRecord));
    }

    @Override
    public BackgroundUploaderResult take() throws InterruptedException {
        final Future<Set<String>> future = completionService.take();
        return uploaderResultMap.remove(future);
    }

    class BackgroundUploaderResult extends BackgroundServiceResult<CallableUploader> {
        final Future<Set<String>> futureResult;
        BackgroundUploaderResult(final CallableUploader callableUploader, final Future<Set<String>> futureResult, final BackgroundServiceErrorRecord errorRecord) {
            super(callableUploader, errorRecord);
            this.futureResult = futureResult;
        }

        Future<Set<String>> getFutureResult() {
            return getFutureResult();
        }
    }

}
