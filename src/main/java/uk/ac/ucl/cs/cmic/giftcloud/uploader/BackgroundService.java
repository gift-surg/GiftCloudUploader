package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.util.List;

public abstract class BackgroundService<T_taskType, T_resultType> implements Runnable {

    enum BackgroundThreadTermination {
        STOP_WHEN_LIST_EMPTY(true),
        CONTINUE_UNTIL_TERMINATED(false);

        private final boolean stopWhenEmpty;

        BackgroundThreadTermination(final boolean stopWhenEmpty) {
            this.stopWhenEmpty = stopWhenEmpty;
        }

        boolean getStopWhenEmpty() {
            return stopWhenEmpty;
        }
    }

    private final BackgroundThreadTermination backgroundThreadTermination;
    private final BackgroundServiceTaskList<T_taskType, T_resultType> backgroundServicePendingList;
    private final BackgroundServiceFailureList<T_taskType> backgroundServiceFailureList;
    protected final MultiUploadReporter reporter;
    private Thread serviceThread = null;
    private volatile boolean running = false;

    public BackgroundService(final BackgroundThreadTermination backgroundThreadTermination, final BackgroundServiceTaskList<T_taskType, T_resultType> backgroundServicePendingList, final MultiUploadReporter reporter) {
        this.backgroundThreadTermination = backgroundThreadTermination;
        this.backgroundServicePendingList = backgroundServicePendingList;
        this.backgroundServiceFailureList = new BackgroundServiceFailureList<T_taskType>();
        this.reporter = reporter;
    }

    public final synchronized void start() {

        if (running) {
            return;
        }

        // If the thread is still waiting to end from a previous stop() then we block and wait
        waitForThreadCompletion();

        serviceThread = new Thread(this);
        serviceThread.start();

        running = true;
    }

    public final synchronized void stop() {
        running = false;
        
        serviceThread.interrupt();
    }

    public final void run() {

        // An InterruptedException is only received if the thread is currently blocking. If this happens the interrupted
        // flag is not set. If the thread is not blocking, the interrupted flag is set but an exception does not occur.
        // Therefore we must check both for the interrupted flag and for the exception in order to correctly process an interruption.
        while (!serviceThread.isInterrupted() && continueProcessing()) {
            try {
                final BackgroundServiceTaskWrapper<T_taskType, T_resultType> backgroundServiceResult = backgroundServicePendingList.take();
                try {
                    processItem(backgroundServiceResult.getResult());
                    notifySuccess(backgroundServiceResult);

                } catch (Throwable e) {
                    reporter.silentLogException(e, "Service failed with the following error:" + e.getLocalizedMessage());
                    backgroundServiceResult.addError(e);

                    if (backgroundServiceResult.shouldRetry()) {
                        backgroundServicePendingList.retryTask(backgroundServiceResult.getTask(), backgroundServiceResult.getErrorRecord());
                    } else {
                        backgroundServiceFailureList.addFailure(backgroundServiceResult.getTask(), backgroundServiceResult.getErrorRecord());
                        notifyFailure(backgroundServiceResult);
                    }
                }

            } catch (InterruptedException e) {
                // The interrupted flag is not set if an InterruptedException was received
                serviceThread.interrupt();
            }
        }

        running = false;

        // We leave all remaining items on the queue so they can be processed if the thread is restarted
    }

    public final boolean isRunning() {
        return running;
    }

    public final List<BackgroundServiceFailureList.FailureRecord> getFailures() {
        return backgroundServiceFailureList.getFailures();
    }

    protected final void waitForThreadCompletion() {
        if (serviceThread != null) {
            try {
                serviceThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * BackgroundService calls this method to determine whether to make a further blocking take() call or to terminate
     * @return
     */
    private final boolean continueProcessing() {
        if (backgroundThreadTermination.getStopWhenEmpty()) {
            return !backgroundServicePendingList.isEmpty();
        } else {
            return true;
        }
    }


    abstract protected void processItem(final T_resultType backgroundServiceResult) throws Exception;

    abstract protected void notifySuccess(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> taskWrapper);
    abstract protected void notifyFailure(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> taskWrapper);
}
