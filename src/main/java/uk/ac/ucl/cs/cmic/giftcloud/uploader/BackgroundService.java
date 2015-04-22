package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

public abstract class BackgroundService<T_taskType, T_taskListType extends BackgroundServiceTaskList<T_taskType, T_resultType>, T_resultType> implements Runnable {

    private final T_taskListType backgroundServicePendingList;
    protected final MultiUploadReporter reporter;
    private Thread serviceThread = null;

    public BackgroundService(final T_taskListType backgroundServicePendingList, final MultiUploadReporter reporter) {
        this.backgroundServicePendingList = backgroundServicePendingList;
        this.reporter = reporter;
    }

    public synchronized void start() {

        // If the thread is still waiting to end from a previous stop() then we block and wait
        if (serviceThread != null) {
            try {
                serviceThread.join();
            } catch (InterruptedException e) {
            }
        }

        serviceThread = new Thread(this);
        serviceThread.start();
    }

    public synchronized void stop() {
        serviceThread.interrupt();
    }

    public void run() {

        // An InterruptedException is only received if the thread is currently blocking. If this happens the interrupted
        // flag is not set. If the thread is not blocking, the interrupted flag is set but an exception does not occur.
        // Therefore we must check both for the interrupted flag and for the exception in order to correctly process an interruption.
        while (!serviceThread.isInterrupted()) {
            try {
                final BackgroundServiceTaskWrapper<T_taskType, T_resultType> backgroundServiceResult = backgroundServicePendingList.take();
                try {
                    processItem(backgroundServiceResult.getResult());
                } catch (Exception e) {
                    reporter.silentLogException(e, "Service failed with the following error:" + e.getLocalizedMessage());
                    backgroundServiceResult.addError(e);
                    backgroundServicePendingList.retry(backgroundServiceResult);
                }

            } catch (InterruptedException e) {
                // The interrupted flag is not set if an InterruptedException was received
                serviceThread.interrupt();
            }
        }

        // We leave all remaining items on the queue so they can be processed if the thread is restarted
    }

    abstract protected void processItem(final T_resultType backgroundServiceResult) throws Exception;
}
