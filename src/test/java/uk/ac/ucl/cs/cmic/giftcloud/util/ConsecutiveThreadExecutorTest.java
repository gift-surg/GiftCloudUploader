package uk.ac.ucl.cs.cmic.giftcloud.util;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConsecutiveThreadExecutorTest {
    private List<String> results;
    private List<String> errors;
    private ConsecutiveThreadExecutor executor;
    private CountDownLatch endLatch;

    @Before
    public void runBeforeEachTest() {
        results = new ArrayList<String>();
        errors = new ArrayList<String>();
        executor = new ConsecutiveThreadExecutor();
    }

    @Test
    public void submit() throws Exception {
        endLatch = new CountDownLatch(3);
        submitWithDelay("A", 100);
        submitWithDelay("B", 10);
        submitWithDelay("C", 0);
        try {
            endLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        ArrayList<String> expectedResults = new ArrayList<String>();
        ArrayList<String> expectedErrors = new ArrayList<String>();
        expectedResults.add("A");
        expectedResults.add("B");
        expectedResults.add("C");
        Assert.assertEquals(results, expectedResults);
        Assert.assertEquals(errors, expectedErrors);
    }

    @Test
    public void shutdownThread() throws Exception {
        endLatch = new CountDownLatch(1);
        submitWithDelay("A", 100);
        submitWithDelay("B", 10);
        submitWithDelay("C", 0);
        executor.shutdownThread();
        Assert.assertTrue(executor.isShutdown());
    }

    private void submitWithDelay(final String result, final long delay) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    addResult(result);
                } catch (InterruptedException e) {
                    addError(result);
                }
                endLatch.countDown();
            }
        });
    }

    private synchronized void addResult(final String result) {
        results.add(result);
    }
    private synchronized void addError(final String result) {
        errors.add(result);
    }
}