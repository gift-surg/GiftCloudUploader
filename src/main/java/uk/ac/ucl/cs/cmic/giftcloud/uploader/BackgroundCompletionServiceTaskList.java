package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

class BackgroundCompletionServiceTaskList<T, U> extends BackgroundServiceTaskList<CallableWithParameter<T, U>, Future<T>> {
    private final CompletionService<T> completionService;
    private final Map<Future<T>, BackgroundServiceTaskWrapper<CallableWithParameter<T, U>, Future<T>>> uploaderResultMap = new HashMap<Future<T>, BackgroundServiceTaskWrapper<CallableWithParameter<T, U>, Future<T>>>();
    private final ExecutorService executor;

    BackgroundCompletionServiceTaskList(final int numThreads) {
        executor = Executors.newFixedThreadPool(numThreads);
        completionService = new ExecutorCompletionService<T>(executor);
    }

    @Override
    public final void add(final CallableWithParameter<T, U> callable, final BackgroundServiceErrorRecord errorRecord) {
        final Future<T> future = completionService.submit(callable);
        uploaderResultMap.put(future, new BackgroundServiceTaskWrapper<CallableWithParameter<T, U>, Future<T>>(callable, future, errorRecord));
    }

    @Override
    public final BackgroundServiceTaskWrapper<CallableWithParameter<T, U>, Future<T>> take() throws InterruptedException {
        final Future<T> future = completionService.take();
        return uploaderResultMap.remove(future);
    }

    public final void cancelAllAndShutdown() {
        executor.shutdownNow();
        for (final Map.Entry<Future<T>, BackgroundServiceTaskWrapper<CallableWithParameter<T, U>, Future<T>>> mapEntry : uploaderResultMap.entrySet()) {
            mapEntry.getKey().cancel(true);
        }
    }

    @Override
    protected final boolean isEmpty() {
        return uploaderResultMap.isEmpty();
    }

}
