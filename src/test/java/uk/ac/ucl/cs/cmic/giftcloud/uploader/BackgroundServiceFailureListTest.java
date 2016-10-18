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

import java.util.List;

public class BackgroundServiceFailureListTest {

    @Test
    public void testAddFailureAndGetFailures() throws Exception {
        // Test that addFailure() adds a to the list of failures
        final BackgroundServiceFailureListFake backgroundServiceFailureList = new BackgroundServiceFailureListFake();

        final String task1 = "Task1";
        final Exception exception1 = new Exception("TestException1");
        final BackgroundServiceErrorRecord errorRecord1 = BackgroundServiceErrorRecord.createInstantRepeater();

        final String task2 = "Task2";
        final Exception exception2 = new Exception("TestException2");
        final BackgroundServiceErrorRecord errorRecord2 = BackgroundServiceErrorRecord.createInstantRepeater();

        {
            errorRecord1.addException(exception1);

            backgroundServiceFailureList.addFailure(task1, errorRecord1);

            final List<BackgroundServiceFailureList<String>.FailureRecord> failures = backgroundServiceFailureList.getFailures();
            Assert.assertEquals(failures.size(), 1);
            Assert.assertEquals(failures.get(0).getTask(), task1);
            Assert.assertEquals(failures.get(0).getErrorRecord(), errorRecord1);
        }

        {
            errorRecord2.addException(exception2);
            backgroundServiceFailureList.addFailure(task2, errorRecord2);
            final List<BackgroundServiceFailureList<String>.FailureRecord> failures = backgroundServiceFailureList.getFailures();
            Assert.assertEquals(failures.size(), 2);
            Assert.assertEquals(failures.get(0).getTask(), task1);
            Assert.assertEquals(failures.get(0).getErrorRecord(), errorRecord1);
            Assert.assertEquals(failures.get(1).getTask(), task2);
            Assert.assertEquals(failures.get(1).getErrorRecord(), errorRecord2);
        }

    }

    @Test
    public void testIsEmpty() throws Exception {
        final BackgroundServiceFailureListFake backgroundServiceFailureList = new BackgroundServiceFailureListFake();

        Assert.assertTrue(backgroundServiceFailureList.isEmpty());
        backgroundServiceFailureList.addFailure("Task1", BackgroundServiceErrorRecord.createInstantRepeater());
        Assert.assertFalse(backgroundServiceFailureList.isEmpty());
        backgroundServiceFailureList.addFailure("Task2", BackgroundServiceErrorRecord.createInstantRepeater());
        Assert.assertFalse(backgroundServiceFailureList.isEmpty());
    }


    class BackgroundServiceFailureListFake extends BackgroundServiceFailureList<String> {}

}