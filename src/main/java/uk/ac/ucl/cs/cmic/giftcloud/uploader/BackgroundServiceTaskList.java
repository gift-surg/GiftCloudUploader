package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.ArrayList;
import java.util.List;

public abstract class BackgroundServiceTaskList<T_taskType, T_resultType> {

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

    private final List<FailureRecord> uploadFailures = new ArrayList<FailureRecord>();

    BackgroundServiceTaskList(final BackgroundThreadTermination backgroundThreadTermination) {
        this.backgroundThreadTermination = backgroundThreadTermination;
    }

    public abstract void add(final T_taskType task, final BackgroundServiceErrorRecord errorRecord);
    public abstract BackgroundServiceTaskWrapper<T_taskType, T_resultType> take() throws InterruptedException;

    public final void addFailure(final T_taskType task, final BackgroundServiceErrorRecord errorRecord) {
        uploadFailures.add(new FailureRecord(task, errorRecord));
    }

    public final void add(final T_taskType task) {
        add(task, new BackgroundServiceErrorRecord());
    }

    public final boolean retry(final BackgroundServiceTaskWrapper<T_taskType, T_resultType> result) {
        if (result.shouldRetry()) {
            add(result.getTask(), result.getErrorRecord());
            return true;
        } else {
            addFailure(result.getTask(), result.getErrorRecord());
            return false;
        }
    }

    public final List<FailureRecord> getFailures() {
        return uploadFailures;
    }

    /**
     * BackgroundService calls this method to determine whether to make a further blocking take() call or to terminate
     * @return
     */
    public boolean continueProcessing() {
        if (backgroundThreadTermination.getStopWhenEmpty()) {
            return !isEmpty();
        } else {
            return false;
        }
    }

    protected abstract boolean isEmpty();

    class FailureRecord<T_taskType> {
        private T_taskType task;
        private BackgroundServiceErrorRecord errorRecord;

        FailureRecord(final T_taskType task, final BackgroundServiceErrorRecord errorRecord) {
            this.task = task;
            this.errorRecord = errorRecord;
        }

        public T_taskType getTask() {
            return task;
        }

        public BackgroundServiceErrorRecord getErrorRecord() {
            return errorRecord;
        }
    }

}
