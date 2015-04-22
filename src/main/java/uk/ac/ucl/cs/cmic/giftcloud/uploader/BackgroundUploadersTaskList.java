package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;

import java.util.*;
import java.util.concurrent.*;

public class BackgroundUploadersTaskList extends BackgroundServiceTaskList<CallableUploader, Future<Set<String>>> {
    private final CompletionService<Set<String>> completionService;
    private final Map<Future<Set<String>>, BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>>> uploaderResultMap = new HashMap<Future<Set<String>>, BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>>>();
    private final ExecutorService executor;

    BackgroundUploadersTaskList() {
        final int numThreads = 1;

        executor = Executors.newFixedThreadPool(numThreads);
        completionService = new ExecutorCompletionService<Set<String>>(executor);
    }

    @Override
    public void add(final CallableUploader callableUploader, final BackgroundServiceErrorRecord errorRecord) {
        final Future<Set<String>> future = completionService.submit(callableUploader);
        uploaderResultMap.put(future, new BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>>(callableUploader, future, errorRecord));
    }

    @Override
    public BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>> take() throws InterruptedException {
        final Future<Set<String>> future = completionService.take();
        return uploaderResultMap.remove(future);
    }

}
