package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@RunWith(Parameterized.class)
public class BackgroundCompletionServiceTaskListTest {

    private int numThreads;

    public BackgroundCompletionServiceTaskListTest(final int numThreads) {
        this.numThreads = numThreads;
    }

    @Test
    public void testAddTake() throws Exception {
        // We will try running 50 callable tasks using a variable number of threads
        final BackgroundCompletionServiceTaskList<String> list = new BackgroundCompletionServiceTaskList<String>(numThreads);

        // Ordered set of results
        final LinkedHashSet<String> submittedResults = new LinkedHashSet<String>();
        final LinkedHashSet<String> completedResults = new LinkedHashSet<String>();

        // Create a bunch of tasks
        final List<FakeCallable> tasks = new ArrayList<FakeCallable>();
        for (int i = 0; i < 50; i++) {
            final String task = "Task" + String.valueOf(i);
            tasks.add(new FakeCallable(task));
            submittedResults.add(task);
        }

        // Add some of the tasks
        for (int i = 0; i < 31; i++) {
            list.add(tasks.get(i));
        }

        // Retrieve some of the tasks
        for (int i = 0; i < 18; i++) {
            final BackgroundServiceTaskWrapper<Callable<String>, Future<String>> resultWrapper = list.take();
            completedResults.add(resultWrapper.getResult().get());
        }

        // Add the remaining tasks
        for (int i = 31; i < 50; i++) {
            list.add(tasks.get(i));
        }

        // Retrieve the remaining tasks
        for (int i = 18; i < 50; i++) {
            final BackgroundServiceTaskWrapper<Callable<String>, Future<String>> resultWrapper = list.take();
            completedResults.add(resultWrapper.getResult().get());
        }

        Assert.assertEquals(submittedResults, completedResults);
    }

    @Parameterized.Parameters
    public static Collection threads() {
        return Arrays.asList(new Object[][] {{1},{2},{3},{4},{5},{6},{7},{8},{9},{10}});
    }

    public class FakeCallable implements Callable<String> {
        public final String result;

        public FakeCallable(final String result) {
            this.result = result;
        }

        @Override
        public String call() throws Exception {
            // Sleep for a random time so that the results are received out of order
            Thread.sleep((long)(Math.random() * 5));
            return result;
        }
    }
}