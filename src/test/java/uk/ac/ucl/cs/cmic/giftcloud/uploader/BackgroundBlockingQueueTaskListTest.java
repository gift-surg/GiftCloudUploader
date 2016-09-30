/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.junit.Assert;
import org.junit.Test;

public class BackgroundBlockingQueueTaskListTest {

    @Test
    public void testAddTake() throws Exception {
        final BackgroundBlockingQueueTaskList<String> list = new BackgroundBlockingQueueTaskList<String>();
        final String task1 = "Task1";
        final String task2 = "Task2";
        final String task3 = "Task3";
        final String task4 = "Task4";
        final String task5 = "Task5";
        list.addNewTask(task1);
        list.addNewTask(task2);
        list.addNewTask(task3);

        final BackgroundServiceTaskWrapper<String, String> resultWrapper1 = list.take();
        Assert.assertEquals(resultWrapper1.getResult(), task1);
        Assert.assertEquals(resultWrapper1.getTask(), task1);

        list.addNewTask(task4);
        list.addNewTask(task5);

        final BackgroundServiceTaskWrapper<String, String> resultWrapper2 = list.take();
        Assert.assertEquals(resultWrapper2.getResult(), task2);
        Assert.assertEquals(resultWrapper2.getTask(), task2);

        Assert.assertEquals(list.take().getResult(), task3);
        Assert.assertEquals(list.take().getResult(), task4);
        Assert.assertEquals(list.take().getResult(), task5);
    }

    @Test
    public void testIsEmpty() {
        final BackgroundBlockingQueueTaskList<String> list = new BackgroundBlockingQueueTaskList<String>();
        Assert.assertTrue(list.isEmpty());
        list.addNewTask("Task1");
        Assert.assertFalse(list.isEmpty());
        list.addNewTask("Task2");
        Assert.assertFalse(list.isEmpty());
    }
}