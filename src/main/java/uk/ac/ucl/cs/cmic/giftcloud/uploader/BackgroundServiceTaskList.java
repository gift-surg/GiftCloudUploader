package uk.ac.ucl.cs.cmic.giftcloud.uploader;

/**
 * Base class for managing a list of tasks to be processed by a (@link BackgroundService)
 *
 * @param <T_taskType> the class of the objects to be processed
 * @param <T_resultType> the return type resulting from the processing of the task
 */
public abstract class BackgroundServiceTaskList<T_taskType, T_resultType> {

    /**
     * Add a new task to the list (i.e. a task that has not previously failed)
     *
     * @param task the task to be processed
     */
    public final void addNewTask(final T_taskType task) {
        add(task, new BackgroundServiceErrorRecord());
    }

    /**
     * Re-add a task to the list. This allows errors to accumulate in the (@link BackgroundServiceErrorRecord) so that
     * tasks can be re-tried or cancelled according to the history of errors
     *
     * @param task the task to be tried again
     * @param errorRecord the cumulative error history from previous attempts to process this task. This is obtained
     *                    from the (@BackgroundServiceTaskWrapper) obtained from (@link take())
     */
    public final void retryTask(final T_taskType task, final BackgroundServiceErrorRecord errorRecord) {
        add(task, errorRecord);
    }

    /**
     * Returns a task from the list to be processed
     *
     * @return a wrapper containing the original task to be processed, the result of the processing, and the error history
     * @throws InterruptedException if an exception occurred during task preprocessing. This might occur as a result of
     * using CompletionService, where the task is only added to the list when the Callable completes or fails
     */
    public abstract BackgroundServiceTaskWrapper<T_taskType, T_resultType> take() throws InterruptedException;

    /**
     * Adds a task to the list with an error history
     *
     * @param task the task to be added to the list
     * @param errorRecord the cumulative error history from previous attempts to process this task. This is obtained
     *                    from the (@BackgroundServiceTaskWrapper) obtained from (@link take())
     */
    protected abstract void add(final T_taskType task, final BackgroundServiceErrorRecord errorRecord);

    /**
     * Determines if there are further tasks to perform
     *
     * @return true if there are no tasks on the list to be processed
     */
    protected abstract boolean isEmpty();
}
