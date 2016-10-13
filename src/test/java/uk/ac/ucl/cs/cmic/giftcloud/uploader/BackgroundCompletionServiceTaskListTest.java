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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableWithParameter;

import java.util.*;
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
        final BackgroundCompletionServiceTaskList<String, String> list = new BackgroundCompletionServiceTaskList<String, String>(numThreads);

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
            list.addNewTask(tasks.get(i));
        }

        // Retrieve some of the tasks
        for (int i = 0; i < 18; i++) {
            final BackgroundServiceTaskWrapper<CallableWithParameter<String, String>, Future<String>> resultWrapper = list.take();
            completedResults.add(resultWrapper.getResult().get());
        }

        // Add the remaining tasks
        for (int i = 31; i < 50; i++) {
            list.addNewTask(tasks.get(i));
        }

        // Retrieve the remaining tasks
        for (int i = 18; i < 50; i++) {
            final BackgroundServiceTaskWrapper<CallableWithParameter<String, String>, Future<String>> resultWrapper = list.take();
            completedResults.add(resultWrapper.getResult().get());
        }

        Assert.assertEquals(submittedResults, completedResults);
    }

    public void testIsEmpty() {
        final BackgroundCompletionServiceTaskList<String, String> list = new BackgroundCompletionServiceTaskList<String, String>(numThreads);
        Assert.assertTrue(list.isEmpty());
        list.addNewTask(new FakeCallable("Task1"));
        Assert.assertFalse(list.isEmpty());
        list.addNewTask(new FakeCallable("Task2"));
        Assert.assertFalse(list.isEmpty());
    }


    @Parameterized.Parameters
    public static Collection threads() {
        return Arrays.asList(new Object[][] {{1},{2},{3},{4},{5},{6},{7},{8},{9},{10}});
    }

    public class FakeCallable implements CallableWithParameter<String, String> {
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

        @Override
        public String getParameter() {
            return result;
        }
    }
}