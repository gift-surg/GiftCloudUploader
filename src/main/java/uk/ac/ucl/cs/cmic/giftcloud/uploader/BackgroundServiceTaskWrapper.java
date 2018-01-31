/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * This class wraps around a task that is scheduled for execution by a {@link BackgroundService} and provide the logic for delayed retry and error recording
 * @param <T_taskType> The type of the task to be executed
 * @param <T_resultType> The type of result returned by the task
 */
class BackgroundServiceTaskWrapper<T_taskType, T_resultType>  implements Delayed {
    private final T_taskType task;
    private final T_resultType result;
    private final BackgroundServiceErrorRecord errorRecord;
    private final long startTime;
    private final long fileNumber;


    /**
     * @param task
     * @param result
     * @param errorRecord
     * @param fileNumber a sequential number indicating the order of task creation. This ensures that (once required delays have expired), tasks are executed in order of creation
     */
    BackgroundServiceTaskWrapper(final T_taskType task, final T_resultType result, final BackgroundServiceErrorRecord errorRecord, final long fileNumber) {
        this.task = task;
        this.result = result;
        this.errorRecord = errorRecord;
        this.startTime = System.currentTimeMillis() + errorRecord.getNextDelay();
        this.fileNumber = fileNumber;
    }

    T_taskType getTask() {
        return task;
    }

    BackgroundServiceErrorRecord getErrorRecord() {
        return errorRecord;
    }

    void addError(final Throwable e) {
        errorRecord.addException(e);
    }

    boolean shouldRetry() {
        return errorRecord.shouldRetry();
    }

    T_resultType getResult() {
        return result;
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((BackgroundServiceTaskWrapper) o).startTime) {
            return -1;
        }
        if (this.startTime > ((BackgroundServiceTaskWrapper) o).startTime) {
            return 1;
        }
        if (this.fileNumber < ((BackgroundServiceTaskWrapper) o).fileNumber) {
            return -1;
        }
        if (this.fileNumber > ((BackgroundServiceTaskWrapper) o).fileNumber) {
            return 1;
        }
        return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }
}
