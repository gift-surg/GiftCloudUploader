package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FileStatusGroupTest {
    @Test
    public void getDate() throws Exception {
        List<String> uids1 = new ArrayList<String>();
        uids1.add("uid1");
        FileStatusGroup group = new FileStatusGroup("a date", "a modality", "A description", uids1);
        org.junit.Assert.assertEquals(group.getDate(), "a date");
    }

    @Test
    public void getModality() throws Exception {
        List<String> uids1 = new ArrayList<String>();
        uids1.add("uid1");
        FileStatusGroup group = new FileStatusGroup("a date", "a modality", "A description", uids1);
        org.junit.Assert.assertEquals(group.getModality(), "a modality");
    }

    @Test
    public void getDescription() throws Exception {
        List<String> uids1 = new ArrayList<String>();
        uids1.add("uid1");
        FileStatusGroup group = new FileStatusGroup("a date", "a modality", "A description", uids1);
        org.junit.Assert.assertEquals(group.getDescription(), "A description");
    }

    @Test
    public void getFileNumbers() throws Exception {
        List<String> uids1 = new ArrayList<String>();
        uids1.add("uid1");
        uids1.add("uid2");
        uids1.add("uid3");
        FileStatusGroup group = new FileStatusGroup("a date", "a modality", "A description", uids1);
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(0/3)");
        group.done("uid3");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(1/3)");
        group.done("uid3");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(1/3)");
        group.done("uid1");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(2/3)");
        group.done("uid2");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(3/3)");
    }

    @Test
    public void getStatus() throws Exception {
        List<String> uids1 = new ArrayList<String>();
        uids1.add("uid1");
        uids1.add("uid2");
        uids1.add("uid3");
        FileStatusGroup group = new FileStatusGroup("a date", "a modality", "A description", uids1);
        org.junit.Assert.assertEquals(group.getStatus(), "Waiting");
        group.done("uid3");
        org.junit.Assert.assertEquals(group.getStatus(), "Uploading");
        group.done("uid3");
        org.junit.Assert.assertEquals(group.getStatus(), "Uploading");
        group.done("uid1");
        org.junit.Assert.assertEquals(group.getStatus(), "Uploading");
        group.done("uid2");
        org.junit.Assert.assertEquals(group.getStatus(), "Done");
    }

    @Test
    public void add() throws Exception {
        List<String> uids1 = new ArrayList<String>();
        uids1.add("uid1");
        uids1.add("uid2");
        FileStatusGroup group = new FileStatusGroup("a date", "a modality", "A description", uids1);

        List<String> uids2 = new ArrayList<String>();
        uids2.add("uid3");
        uids2.add("uid4");
        group.add(uids2);

        org.junit.Assert.assertEquals(group.getFileNumbers(), "(0/4)");
        group.done("uid3");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(1/4)");
    }

    @Test
    public void done() throws Exception {
        List<String> uids1 = new ArrayList<String>();
        uids1.add("uid1");
        uids1.add("uid2");
        uids1.add("uid3");
        FileStatusGroup group = new FileStatusGroup("a date", "a modality", "A description", uids1);
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(0/3)");
        group.done("uid3");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(1/3)");
        group.done("uid3");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(1/3)");
        group.done("uid1");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(2/3)");
        group.done("uid2");
        org.junit.Assert.assertEquals(group.getFileNumbers(), "(3/3)");
    }
}