/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BackgroundServiceTaskListTest {


    @Test
    public void testAddNewTask() throws Exception {
        // Check that add(task) creates a new errorRecord and calls add(task, errorRecord)
        final BackgroundServiceTaskListFake backgroundServiceTaskList = new BackgroundServiceTaskListFake();
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 0);
        backgroundServiceTaskList.addNewTask("Task1");
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 1);
        Assert.assertNotNull(backgroundServiceTaskList.taskList.get(0).errorRecord);

        backgroundServiceTaskList.addNewTask("Task2");
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 2);
        Assert.assertNotNull(backgroundServiceTaskList.taskList.get(0).errorRecord);
        Assert.assertNotNull(backgroundServiceTaskList.taskList.get(1).errorRecord);
    }

    @Test
    public void testRetryTask() throws Exception {
        // Check that add(task) creates a new errorRecord and calls add(task, errorRecord)
        final BackgroundServiceTaskListFake backgroundServiceTaskList = new BackgroundServiceTaskListFake();
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 0);
        final BackgroundServiceErrorRecord errorRecord1 = BackgroundServiceErrorRecord.createInstantRepeater();
        backgroundServiceTaskList.retryTask("Task1", errorRecord1);
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 1);
        Assert.assertEquals(backgroundServiceTaskList.taskList.get(0).errorRecord, errorRecord1);

        final BackgroundServiceErrorRecord errorRecord2 = BackgroundServiceErrorRecord.createInstantRepeater();
        backgroundServiceTaskList.retryTask("Task2", errorRecord2);
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 2);
        Assert.assertEquals(backgroundServiceTaskList.taskList.get(1).errorRecord, errorRecord2);
    }


    class BackgroundServiceTaskListFake extends BackgroundServiceTaskList<String, String> {
        final List<TaskError> taskList = new ArrayList<TaskError>();

        @Override
        public void add(String task, BackgroundServiceErrorRecord errorRecord) {
            taskList.add(new TaskError(task, errorRecord));
        }

        @Override
        public BackgroundServiceTaskWrapper<String, String> take() throws InterruptedException {
            return null;
        }

        @Override
        protected boolean isEmpty() {
            return taskList.isEmpty();
        }

        @Override
        protected BackgroundServiceErrorRecord createErrorRecord() {
            return BackgroundServiceErrorRecord.createInstantRepeater();
        }

        class TaskError {
            final Object task;
            final BackgroundServiceErrorRecord errorRecord;

            TaskError(final Object task, final BackgroundServiceErrorRecord errorRecord) {
                this.task = task;
                this.errorRecord = errorRecord;
            }
        }
    }

}