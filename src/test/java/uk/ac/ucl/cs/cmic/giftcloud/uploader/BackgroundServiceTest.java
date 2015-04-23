package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import static org.mockito.Mockito.mock;

public class BackgroundServiceTest {

    @Test
    public void testStart() throws Exception {
        final BackgroundServiceTaskList<String, String> backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(backgroundServicePendingList);

        // Test there are no processed results
        Assert.assertEquals(backgroundService.processed.size(), 0);
        backgroundService.start();
        Assert.assertEquals(backgroundService.processed.size(), 0);

        final String task1 = "task1";

        backgroundService.setNumberOfTasksToWaitFor(1);
        backgroundServicePendingList.add(task1);
        backgroundService.waitForCompletion();

        Assert.assertEquals(backgroundService.processed.size(), 1);
        Assert.assertEquals(backgroundService.processed.get(0), task1);

        // Add a couple more tasks and check that these are processed
        final String task2 = "task2";
        final String task3 = "task3";
        backgroundService.setNumberOfTasksToWaitFor(2);
        backgroundServicePendingList.add(task2);
        backgroundServicePendingList.add(task3);
        backgroundService.waitForCompletion();
        Assert.assertEquals(backgroundService.processed.size(), 3);
    }

    @Test
    public void testDelayedStart() throws Exception {
        final BackgroundServiceTaskList<String, String> backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(backgroundServicePendingList);

        final String task1 = "task1";
        final String task2 = "task2";
        final String task3 = "task3";

        backgroundServicePendingList.add(task1);
        backgroundServicePendingList.add(task2);
        backgroundServicePendingList.add(task3);

        backgroundService.setNumberOfTasksToWaitFor(3);
        backgroundService.start();
        backgroundService.waitForCompletion();
        Assert.assertEquals(backgroundService.processed.size(), 3);
    }

    @Test
    public void testStop() throws Exception {
        final BackgroundServiceTaskList<String, String> backgroundServicePendingList = new FakeBackgroundServiceTaskList();
        final FakeBackgroundService backgroundService = new FakeBackgroundService(backgroundServicePendingList);

        final String task1 = "task1";
        final String task2 = "task2";
        final String task3 = "task3";

        backgroundService.setNumberOfTasksToWaitFor(3);

        backgroundService.start();
        backgroundService.stop();
        backgroundService.start();

        backgroundServicePendingList.add(task1);
        backgroundServicePendingList.add(task2);
        backgroundServicePendingList.add(task3);

        backgroundService.waitForCompletion();
        Assert.assertEquals(backgroundService.processed.size(), 3);
    }

    class FakeBackgroundServiceTaskList extends BackgroundServiceTaskList<String, String> {

        final BlockingQueue<BackgroundServiceTaskWrapper<String, String>> tasks = new LinkedBlockingDeque<BackgroundServiceTaskWrapper<String, String>>();

        @Override
        public void add(String task, BackgroundServiceErrorRecord errorRecord) {
            tasks.add(new BackgroundServiceTaskWrapper<String, String>(task, task, errorRecord));
        }

        @Override
        public BackgroundServiceTaskWrapper<String, String> take() throws InterruptedException {
            return tasks.take();
        }
    }

    class FakeBackgroundService extends BackgroundService<String, String> {

        List<String> processed = new ArrayList<String>();
        private CountDownLatch latch;

        FakeBackgroundService(final BackgroundServiceTaskList<String, String> backgroundServicePendingList) {
            super(backgroundServicePendingList, mock(MultiUploadReporter.class));
        }

        @Override
        protected void processItem(String backgroundServiceResult) throws Exception {
            processed.add(backgroundServiceResult);
            latch.countDown();
        }

        void setNumberOfTasksToWaitFor(final int numberOfTasksToWait) {
            latch = new CountDownLatch(numberOfTasksToWait);
        }

        void waitForCompletion() throws InterruptedException {
            latch.await();
        }
    }
}