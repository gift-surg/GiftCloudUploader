package uk.ac.ucl.cs.cmic.giftcloud.uploader;

public class BackgroundServiceTaskWrapper<T_taskType, T_resultType> {
    private final T_taskType task;
    private final T_resultType result;
    private final BackgroundServiceErrorRecord errorRecord;

    BackgroundServiceTaskWrapper(final T_taskType task, final T_resultType result, final BackgroundServiceErrorRecord errorRecord) {
        this.task = task;
        this.result = result;
        this.errorRecord = errorRecord;
    }

    T_taskType getTask() {
        return task;
    }

    BackgroundServiceErrorRecord getErrorRecord() {
        return errorRecord;
    }

    public void addError(final Exception e) {
        errorRecord.addException(e);
    }

    public boolean shouldRetry() {
        return (errorRecord.shouldRetry());
    }

    public T_resultType getResult() {
        return result;
    }
}
