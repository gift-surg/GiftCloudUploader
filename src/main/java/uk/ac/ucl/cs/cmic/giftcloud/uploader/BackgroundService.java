package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

public abstract class BackgroundService<T_taskType, T_resultType> implements Runnable {

    private final BackgroundServiceTaskList<T_taskType, T_resultType> backgroundServicePendingList;
    protected final MultiUploadReporter reporter;
    private Thread serviceThread = null;
    private boolean running = false;

    public BackgroundService(final BackgroundServiceTaskList<T_taskType, T_resultType> backgroundServicePendingList, final MultiUploadReporter reporter) {
        this.backgroundServicePendingList = backgroundServicePendingList;
        this.reporter = reporter;
    }

    public synchronized void start() {

        if (running) {
            return;
        }

        // If the thread is still waiting to end from a previous stop() then we block and wait
        if (serviceThread != null) {
            try {
                serviceThread.join();
            } catch (InterruptedException e) {
            }
        }

        serviceThread = new Thread(this);
        serviceThread.start();

        running = true;
    }

    public synchronized void stop() {
        running = false;
        
        serviceThread.interrupt();
    }

    public void run() {

        // An InterruptedException is only received if the thread is currently blocking. If this happens the interrupted
        // flag is not set. If the thread is not blocking, the interrupted flag is set but an exception does not occur.
        // Therefore we must check both for the interrupted flag and for the exception in order to correctly process an interruption.
        while (!serviceThread.isInterrupted() && backgroundServicePendingList.continueProcessing()) {
            try {
                final BackgroundServiceTaskWrapper<T_taskType, T_resultType> backgroundServiceResult = backgroundServicePendingList.take();
                try {
                    processItem(backgroundServiceResult.getResult());
                    notifySuccess(backgroundServiceResult);
                } catch (Throwable e) {
                    reporter.silentLogException(e, "Service failed with the following error:" + e.getLocalizedMessage());
                    backgroundServiceResult.addError(e);
                    if (!backgroundServicePendingList.retry(backgroundServiceResult)) {
                        notifyFailure(backgroundServiceResult);
                    }
                }

            } catch (InterruptedException e) {
                // The interrupted flag is not set if an InterruptedException was received
                serviceThread.interrupt();
            }
        }

        // We leave all remaining items on the queue so they can be processed if the thread is restarted
    }

    abstract protected void processItem(final T_resultType backgroundServiceResult) throws Exception;

    abstract protected void notifySuccess(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> taskWrapper);
    abstract protected void notifyFailure(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> taskWrapper);
}
