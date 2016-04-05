package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.junit.Assert;
import org.junit.Test;

public class ListMapTest {
    @Test
    public void put() throws Exception {
        ListMap<String, String> map = new ListMap<String, String>();
        Assert.assertEquals(0, map.put("A", "B"));
        Assert.assertEquals(1, map.put("B", "B"));
        Assert.assertEquals(2, map.put("C", "C"));

        try {
            map.put("B", "F");
            Assert.assertTrue(false);
        } catch (java.lang.IllegalArgumentException e) {
        }
    }

    @Test
    public void remove() throws Exception {
        ListMap<String, String> map = new ListMap<String, String>();
        Assert.assertEquals(0, map.put("A", "B1"));
        Assert.assertEquals(1, map.put("B", "B1"));
        Assert.assertEquals(2, map.put("C", "C1"));
        Assert.assertEquals(3, map.put("D", "D1"));
        map.remove("B");
        Assert.assertEquals(map.containsKey("B"), false);
        Assert.assertEquals(map.getIndex("A"), 0);
        Assert.assertEquals(map.getIndex("C"), 1);
        Assert.assertEquals(map.getIndex("D"), 2);
        map.remove("A");
        Assert.assertEquals(map.containsKey("A"), false);
        Assert.assertEquals(map.getIndex("C"), 0);
        Assert.assertEquals(map.getIndex("D"), 1);
        map.remove("D");
        Assert.assertEquals(map.containsKey("D"), false);
        Assert.assertEquals(map.getIndex("C"), 0);
        map.remove("C");
        Assert.assertEquals(map.containsKey("C"), false);
        try {
            map.getIndex("C");
            Assert.assertTrue(false);
        } catch (java.lang.IllegalArgumentException e) {
        }
    }

    @Test
    public void getFromIndex() throws Exception {
        ListMap<String, String> map = new ListMap<String, String>();
        map.put("A", "A1");
        map.put("B", "B1");
        map.put("C", "C1");
        map.put("D", "D1");
        Assert.assertEquals(map.getFromIndex(0), "A1");
        Assert.assertEquals(map.getFromIndex(1), "B1");
        Assert.assertEquals(map.getFromIndex(2), "C1");
        Assert.assertEquals(map.getFromIndex(3), "D1");
        map.remove("B");
        Assert.assertEquals(map.getFromIndex(0), "A1");
        Assert.assertEquals(map.getFromIndex(1), "C1");
        Assert.assertEquals(map.getFromIndex(2), "D1");
        try {
            map.getFromIndex(3);
            Assert.assertTrue(false);
        } catch (java.lang.IllegalArgumentException e) {
        }
    }

    @Test
    public void getFromKey() throws Exception {
        ListMap<String, String> map = new ListMap<String, String>();
        map.put("A", "A1");
        map.put("B", "B1");
        map.put("C", "C1");
        map.put("D", "D1");
        Assert.assertEquals(map.getFromKey("A"), "A1");
        Assert.assertEquals(map.getFromKey("B"), "B1");
        Assert.assertEquals(map.getFromKey("C"), "C1");
        Assert.assertEquals(map.getFromKey("D"), "D1");
        map.remove("B");
        Assert.assertEquals(map.getFromKey("A"), "A1");
        Assert.assertEquals(map.getFromKey("C"), "C1");
        Assert.assertEquals(map.getFromKey("D"), "D1");
        try {
            map.getFromKey("B");
            Assert.assertTrue(false);
        } catch (java.lang.IllegalArgumentException e) {
        }
    }

    @Test
    public void size() throws Exception {
        ListMap<String, String> map = new ListMap<String, String>();
        Assert.assertEquals(map.size(), 0);
        map.put("A", "A1");
        Assert.assertEquals(map.size(), 1);
        map.put("B", "B1");
        Assert.assertEquals(map.size(), 2);
        map.remove("A");
        Assert.assertEquals(map.size(), 1);
    }

    @Test
    public void containsKey() throws Exception {
        ListMap<String, String> map = new ListMap<String, String>();
        map.put("A", "A1");
        map.put("B", "B1");
        map.put("C", "C1");
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertTrue(map.containsKey("C"));
        Assert.assertFalse(map.containsKey("D"));
        map.remove("B");
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertFalse(map.containsKey("B"));
        Assert.assertTrue(map.containsKey("C"));
        Assert.assertFalse(map.containsKey("D"));
        map.remove("A");
        map.remove("C");
        Assert.assertFalse(map.containsKey("A"));
        Assert.assertFalse(map.containsKey("B"));
        Assert.assertFalse(map.containsKey("C"));
        Assert.assertFalse(map.containsKey("D"));
    }

    @Test
    public void getIndex() throws Exception {
        ListMap<String, String> map = new ListMap<String, String>();
        map.put("A", "A1");
        map.put("B", "B1");
        map.put("C", "C1");
        Assert.assertEquals(map.getIndex("A"), 0);
        Assert.assertEquals(map.getIndex("B"), 1);
        Assert.assertEquals(map.getIndex("C"), 2);
        map.remove("B");
        Assert.assertEquals(map.getIndex("A"), 0);
        Assert.assertEquals(map.getIndex("C"), 1);
        map.remove("A");
        Assert.assertEquals(map.getIndex("C"), 0);
    }

}