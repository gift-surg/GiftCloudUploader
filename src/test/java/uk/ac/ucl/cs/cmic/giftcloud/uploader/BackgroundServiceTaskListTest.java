package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackgroundServiceTaskListTest {

    @Test
    public void testAddFailure() throws Exception {
        // Test that addFailure() adds a to the list of failures
        final BackgroundServiceTaskListFake backgroundServiceTaskList = new BackgroundServiceTaskListFake();

        final String task1 = "Task1";
        final Exception exception1 = new Exception("TestException1");
        final BackgroundServiceErrorRecord errorRecord1 = new BackgroundServiceErrorRecord();

        final String task2 = "Task2";
        final Exception exception2 = new Exception("TestException2");
        final BackgroundServiceErrorRecord errorRecord2 = new BackgroundServiceErrorRecord();

        {
            errorRecord1.addException(exception1);
            backgroundServiceTaskList.addFailure(task1, errorRecord1);
            final List<BackgroundServiceTaskList.FailureRecord> failures = backgroundServiceTaskList.getFailures();
            Assert.assertEquals(failures.size(), 1);
            Assert.assertEquals(failures.get(0).getTask(), task1);
            Assert.assertEquals(failures.get(0).getErrorRecord(), errorRecord1);
        }

        {
            errorRecord2.addException(exception2);
            backgroundServiceTaskList.addFailure(task2, errorRecord2);
            final List<BackgroundServiceTaskList.FailureRecord> failures = backgroundServiceTaskList.getFailures();
            Assert.assertEquals(failures.size(), 2);
            Assert.assertEquals(failures.get(0).getTask(), task1);
            Assert.assertEquals(failures.get(0).getErrorRecord(), errorRecord1);
            Assert.assertEquals(failures.get(1).getTask(), task2);
            Assert.assertEquals(failures.get(1).getErrorRecord(), errorRecord2);
        }

    }

    @Test
    public void testRetry() throws Exception {
        // Check that retry() calls add() or addFailure() according to the response of TaskWrapper.shouldRetry()

        final BackgroundServiceTaskListFake backgroundServiceTaskList = new BackgroundServiceTaskListFake();
        final Exception exception1 = new Exception("TestException1");
        final BackgroundServiceErrorRecord errorRecord1 = new BackgroundServiceErrorRecord();

        errorRecord1.addException(exception1);

        BackgroundServiceTaskWrapper<String, String> taskWrapper = mock(BackgroundServiceTaskWrapper.class);

        {
            Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 0);
            final List<BackgroundServiceTaskList.FailureRecord> failures = backgroundServiceTaskList.getFailures();
            Assert.assertEquals(failures.size(), 0);
        }

        {
            when(taskWrapper.shouldRetry()).thenReturn(true);
            backgroundServiceTaskList.retry(taskWrapper);
            final List<BackgroundServiceTaskList.FailureRecord> failures = backgroundServiceTaskList.getFailures();
            Assert.assertEquals(failures.size(), 0);
            Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 1);
        }

        {
            when(taskWrapper.shouldRetry()).thenReturn(false);
            backgroundServiceTaskList.retry(taskWrapper);
            final List<BackgroundServiceTaskList.FailureRecord> failures = backgroundServiceTaskList.getFailures();
            Assert.assertEquals(failures.size(), 1);
            Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 1);
        }


    }

    @Test
    public void testAdd() throws Exception {
        // Check that add(task) creates a new errorRecord and calls add(task, errorRecord)
        final BackgroundServiceTaskListFake backgroundServiceTaskList = new BackgroundServiceTaskListFake();
        final String task1 = "Task1";
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 0);
        backgroundServiceTaskList.add(task1);
        Assert.assertEquals(backgroundServiceTaskList.taskList.size(), 1);
        Assert.assertNotNull(backgroundServiceTaskList.taskList.get(0).errorRecord);
    }

    class BackgroundServiceTaskListFake extends BackgroundServiceTaskList<String, String> {
        final List<TaskError> taskList = new ArrayList<TaskError>();

        BackgroundServiceTaskListFake() {
            super(BackgroundServiceTaskList.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED);
        }

        @Override
        public void add(String task, BackgroundServiceErrorRecord errorRecord) {
            taskList.add(new TaskError(task, errorRecord));
        }

        @Override
        public BackgroundServiceTaskWrapper take() throws InterruptedException {
            return null;
        }

        @Override
        protected boolean isEmpty() {
            return taskList.isEmpty();
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