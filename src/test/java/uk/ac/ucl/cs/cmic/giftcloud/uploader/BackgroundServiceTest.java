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
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackgroundServiceTest {

    @Test
    public void testStart() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundServicePendingList);

        // Test there are no processed results
        Assert.assertEquals(backgroundService.processed.size(), 0);
        backgroundService.start();
        Assert.assertEquals(backgroundService.processed.size(), 0);

        final FakeTask task1 = new FakeTask("task1");

        backgroundService.setNumberOfTasksToWaitFor(1);
        backgroundServicePendingList.addNewTask(task1);
        backgroundService.waitForCompletion();

        Assert.assertEquals(backgroundService.processed.size(), 1);
        Assert.assertEquals(backgroundService.processed.get(0), task1);

        // Add a couple more tasks and check that these are processed
        final FakeTask task2 = new FakeTask("task2");
        final FakeTask task3 = new FakeTask("task3");
        backgroundService.setNumberOfTasksToWaitFor(2);
        backgroundServicePendingList.addNewTask(task2);
        backgroundServicePendingList.addNewTask(task3);
        backgroundService.waitForCompletion();
        Assert.assertEquals(backgroundService.processed.size(), 3);

        // Ensure there are no tasks left
        Assert.assertEquals(backgroundServicePendingList.taskList.size(), 0);
    }

    @Test
    public void testIsRunning() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundServicePendingList);

        Assert.assertFalse(backgroundService.isRunning());
        backgroundService.start();
        Assert.assertTrue(backgroundService.isRunning());

        backgroundService.stop();
        Assert.assertFalse(backgroundService.isRunning());

        final FakeTask task1 = new FakeTask("task1");

        backgroundService.start();
        Assert.assertTrue(backgroundService.isRunning());

        backgroundService.setNumberOfTasksToWaitFor(1);
        backgroundServicePendingList.addNewTask(task1);
        backgroundService.waitForCompletion();

        Assert.assertTrue(backgroundService.isRunning());

        backgroundService.stop();
        Assert.assertFalse(backgroundService.isRunning());
    }

    @Test
    public void testFailure() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundServicePendingList);

        // Test there are no processed results
        Assert.assertEquals(backgroundService.processed.size(), 0);
        backgroundService.start();
        Assert.assertEquals(backgroundService.processed.size(), 0);

        final FakeTask task1 = new FakeTask("task1");
        final FakeTask failtask1 = new FakeTask("fail1", false);

        backgroundService.setNumberOfTasksToWaitFor(2);
        backgroundServicePendingList.addNewTask(task1);
        backgroundServicePendingList.addNewTask(failtask1);
        backgroundService.waitForCompletion();


        Assert.assertEquals(backgroundService.processed.size(), 1);
        Assert.assertEquals(backgroundService.processed.get(0), task1);

        Assert.assertEquals(backgroundService.successNotify.size(), 1);
        Assert.assertEquals(backgroundService.failNotify.size(), 1);

        // Ensure there are no tasks left
        Assert.assertTrue(backgroundServicePendingList.isEmpty());

        // Add a couple more tasks and a couple more failures and check that these are processed
        final FakeTask failtask2 = new FakeTask("fail2", false);
        final FakeTask failtask3 = new FakeTask("fail3", false);
        final FakeTask task2 = new FakeTask("task2");
        final FakeTask task3 = new FakeTask("task3");
        backgroundService.setNumberOfTasksToWaitFor(4);
        backgroundServicePendingList.addNewTask(failtask2);
        backgroundServicePendingList.addNewTask(task2);
        backgroundServicePendingList.addNewTask(task3);
        backgroundServicePendingList.addNewTask(failtask3);
        backgroundService.waitForCompletion();

        Assert.assertEquals(backgroundService.processed.size(), 3);

        Assert.assertEquals(backgroundService.successNotify.size(), 3);
        Assert.assertEquals(backgroundService.failNotify.size(), 3);

        // Ensure there are no tasks left
        Assert.assertTrue(backgroundServicePendingList.isEmpty());

    }

    @Test
    public void testDelayedStart() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundServicePendingList);

        final FakeTask task1 = new FakeTask("task1");
        final FakeTask task2 = new FakeTask("task2");
        final FakeTask task3 = new FakeTask("task3");

        backgroundServicePendingList.addNewTask(task1);
        backgroundServicePendingList.addNewTask(task2);
        backgroundServicePendingList.addNewTask(task3);

        backgroundService.setNumberOfTasksToWaitFor(3);
        backgroundService.start();
        backgroundService.waitForCompletion();
        Assert.assertEquals(backgroundService.processed.size(), 3);

        // Ensure there are no tasks left
        Assert.assertTrue(backgroundServicePendingList.isEmpty());

    }

    @Test
    public void testStop() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundServicePendingList);

        final FakeTask task1 = new FakeTask("task1");
        final FakeTask task2 = new FakeTask("task2");
        final FakeTask task3 = new FakeTask("task3");

        backgroundService.setNumberOfTasksToWaitFor(3);

        backgroundService.start();
        backgroundService.stop();
        backgroundService.start();

        backgroundServicePendingList.addNewTask(task1);
        backgroundServicePendingList.addNewTask(task2);
        backgroundServicePendingList.addNewTask(task3);

        backgroundService.waitForCompletion();
        Assert.assertEquals(backgroundService.processed.size(), 3);

        // Ensure there are no tasks left
        Assert.assertTrue(backgroundServicePendingList.isEmpty());

    }



    @Test
    public void testFailSucceed() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServiceTaskList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundServiceTaskList);

        backgroundService.start();

        // Add a task which fails once then succeeds
        {
            final FakeTask task1 = mock(FakeTask.class);
            when(task1.shouldSucceed()).thenReturn(false).thenReturn(true);
            final BackgroundServiceErrorRecord errorRecord1 = mock(BackgroundServiceErrorRecord.class);
            when(errorRecord1.shouldRetry()).thenReturn(true).thenReturn(false);

            backgroundService.setNumberOfTasksToWaitFor(1);
            backgroundServiceTaskList.add(task1, errorRecord1);
            backgroundService.waitForCompletion();
            final List<BackgroundServiceFailureList.FailureRecord> failures = backgroundService.getFailures();
            Assert.assertEquals(failures.size(), 0);
            Assert.assertEquals(backgroundService.processed.size(), 1);
        }
    }

    @Test
    public void testFailFail() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServiceTaskList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundServiceTaskList);

        backgroundService.start();

        // Add a task which fails once then succeeds
        {
            final FakeTask task1 = mock(FakeTask.class);
            when(task1.shouldSucceed()).thenReturn(false).thenReturn(false);
            final BackgroundServiceErrorRecord errorRecord1 = mock(BackgroundServiceErrorRecord.class);
            when(errorRecord1.shouldRetry()).thenReturn(true).thenReturn(false);

            backgroundService.setNumberOfTasksToWaitFor(1);
            backgroundServiceTaskList.add(task1, errorRecord1);
            backgroundService.waitForCompletion();
            final List<BackgroundServiceFailureList.FailureRecord> failures = backgroundService.getFailures();
            Assert.assertEquals(failures.size(), 1);
            Assert.assertEquals(backgroundService.processed.size(), 0);
        }
    }

    @Test
    public void testAutoStop() throws Exception {
        final FakeBackgroundServiceTaskList backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(BackgroundService.BackgroundThreadTermination.STOP_WHEN_LIST_EMPTY, backgroundServicePendingList);


        final FakeTask task1 = new FakeTask("task1");
        final FakeTask task2 = new FakeTask("task2");
        final FakeTask task3 = new FakeTask("task3");

        backgroundService.setNumberOfTasksToWaitFor(3);
        backgroundServicePendingList.addNewTask(task1);
        backgroundServicePendingList.addNewTask(task2);
        backgroundServicePendingList.addNewTask(task3);

        backgroundService.start();

        backgroundService.waitForCompletion();

        // Ensure all tasks are processed
        Assert.assertEquals(backgroundServicePendingList.taskList.size(), 0);
        Assert.assertEquals(backgroundService.processed.size(), 3);

        // Check that the service terminates after all tasks have been completed
        backgroundService.waitForThreadToComplete();
        Assert.assertFalse(backgroundService.isRunning());
    }

    class FakeBackgroundServiceTaskList extends BackgroundServiceTaskList<FakeTask, FakeTask> {

        final BlockingQueue<BackgroundServiceTaskWrapper<FakeTask, FakeTask>> taskList = new LinkedBlockingDeque<BackgroundServiceTaskWrapper<FakeTask, FakeTask>>();
        private long fileNum = 0;

        @Override
        public void add(FakeTask task, BackgroundServiceErrorRecord errorRecord) {
            taskList.add(new BackgroundServiceTaskWrapper<FakeTask, FakeTask>(task, task, errorRecord, fileNum++));
        }

        @Override
        public BackgroundServiceTaskWrapper<FakeTask, FakeTask> take() throws InterruptedException {
            return taskList.take();
        }

        @Override
        protected boolean isEmpty() {
            return taskList.isEmpty();
        }

        @Override
        protected BackgroundServiceErrorRecord createErrorRecord() {
            return BackgroundServiceErrorRecord.createInstantRepeater();
        }
    }

    class FakeBackgroundService extends BackgroundService<FakeTask, FakeTask> {

        List<FakeTask> processed = new ArrayList<FakeTask>();
        private CountDownLatch latch;

        List<BackgroundServiceTaskWrapper<FakeTask, FakeTask>> successNotify = new ArrayList<BackgroundServiceTaskWrapper<FakeTask, FakeTask>>();
        List<BackgroundServiceTaskWrapper<FakeTask, FakeTask>> failNotify = new ArrayList<BackgroundServiceTaskWrapper<FakeTask, FakeTask>>();

        FakeBackgroundService(final BackgroundThreadTermination threadTermination, final BackgroundServiceTaskList<FakeTask, FakeTask> backgroundServicePendingList) {
            super(threadTermination, backgroundServicePendingList, 1000, mock(GiftCloudReporter.class));
        }

        @Override
        protected void processItem(FakeTask backgroundServiceResult) throws Exception {
            if (backgroundServiceResult.shouldSucceed()) {
                processed.add(backgroundServiceResult);
            } else {
                throw new Exception("Unit test failure simulation");
            }
        }

        @Override
        protected void notifySuccess(BackgroundServiceTaskWrapper<FakeTask, FakeTask> taskWrapper) {
            successNotify.add(taskWrapper);
            latch.countDown();
        }

        @Override
        protected void notifyFailure(BackgroundServiceTaskWrapper<FakeTask, FakeTask> taskWrapper) {
            failNotify.add(taskWrapper);
            latch.countDown();
        }

        void setNumberOfTasksToWaitFor(final int numberOfTasksToWait) {
            latch = new CountDownLatch(numberOfTasksToWait);
        }

        void waitForCompletion() throws InterruptedException {
            latch.await(1000, TimeUnit.MILLISECONDS);
        }

        void waitForThreadToComplete() {
            waitForThreadCompletion(0);
        }
    }

    class FakeTask {
        final String taskName;
        boolean succeed = true;

        FakeTask(final String taskName) {
            this.taskName = taskName;
        }

        FakeTask(final String taskName, boolean succeed) {
            this.taskName = taskName;
            this.succeed = succeed;
        }

        boolean shouldSucceed() {
            return succeed;
        }

    }
}