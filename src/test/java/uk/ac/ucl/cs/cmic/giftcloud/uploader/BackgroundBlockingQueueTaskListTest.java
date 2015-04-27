package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.junit.Assert;
import org.junit.Test;

public class BackgroundBlockingQueueTaskListTest {

    @Test
    public void testAddTake() throws Exception {
        final BackgroundBlockingQueueTaskList<String> list = new BackgroundBlockingQueueTaskList<String>(BackgroundServiceTaskList.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED);
        final String task1 = "Task1";
        final String task2 = "Task2";
        final String task3 = "Task3";
        final String task4 = "Task4";
        final String task5 = "Task5";
        list.add(task1);
        list.add(task2);
        list.add(task3);

        final BackgroundServiceTaskWrapper<String, String> resultWrapper1 = list.take();
        Assert.assertEquals(resultWrapper1.getResult(), task1);
        Assert.assertEquals(resultWrapper1.getTask(), task1);

        list.add(task4);
        list.add(task5);

        final BackgroundServiceTaskWrapper<String, String> resultWrapper2 = list.take();
        Assert.assertEquals(resultWrapper2.getResult(), task2);
        Assert.assertEquals(resultWrapper2.getTask(), task2);

        Assert.assertEquals(list.take().getResult(), task3);
        Assert.assertEquals(list.take().getResult(), task4);
        Assert.assertEquals(list.take().getResult(), task5);
    }
}