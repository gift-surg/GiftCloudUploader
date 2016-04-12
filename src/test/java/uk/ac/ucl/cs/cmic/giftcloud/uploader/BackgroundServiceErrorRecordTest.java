package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;
import org.junit.Test;

public class BackgroundServiceErrorRecordTest {

    @Test
    public void testAddException() throws Exception {
        final BackgroundServiceErrorRecord record = BackgroundServiceErrorRecord.createInstantRepeater();
        final Exception e1 = new Exception("TestException");
        record.addException(e1);
    }

    @Test
    public void testShouldRetry() throws Exception {

        final BackgroundServiceErrorRecord record = BackgroundServiceErrorRecord.createInstantRepeater();
        final Exception e1 = new Exception("TestException1");
        final Exception e2 = new Exception("TestException2");
        final Exception e3 = new Exception("TestException3");
        final Exception e4 = new Exception("TestException4");
        Assert.assertTrue(record.shouldRetry());
        record.addException(e1);
        Assert.assertTrue(record.shouldRetry());
        record.addException(e2);
        Assert.assertTrue(record.shouldRetry());
        record.addException(e3);
        Assert.assertFalse(record.shouldRetry());
        record.addException(e4);
        Assert.assertFalse(record.shouldRetry());
    }

    @Test
    public void testGetErrorList() throws Exception {
        final BackgroundServiceErrorRecord record = BackgroundServiceErrorRecord.createInstantRepeater();
        Assert.assertEquals(record.getErrorList().size(), 0);

        final Exception e1 = new Exception("TestException1");
        record.addException(e1);
        Assert.assertEquals(record.getErrorList().size(), 1);
        Assert.assertEquals(record.getErrorList().get(0).getException(), e1);

        final Exception e2 = new Exception("TestException2");
        record.addException(e2);
        Assert.assertEquals(record.getErrorList().size(), 2);
        Assert.assertEquals(record.getErrorList().get(1).getException(), e2);
    }
}