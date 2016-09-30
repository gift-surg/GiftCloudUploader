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

import java.util.ArrayList;

public class FileStatusGrouperTest {
    @Test
    public void resetChanged() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        Assert.assertFalse(grouper.getAnyGroupsAddedOrRemoved());
        Assert.assertEquals(grouper.getMinGroupChanged(), -1);
        Assert.assertEquals(grouper.getMaxGroupChanged(), -1);
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertTrue(grouper.getAnyGroupsAddedOrRemoved());
        Assert.assertEquals(grouper.getMinGroupChanged(), 0);
        Assert.assertEquals(grouper.getMaxGroupChanged(), 0);
        grouper.resetChanged();
        Assert.assertFalse(grouper.getAnyGroupsAddedOrRemoved());
        Assert.assertEquals(grouper.getMinGroupChanged(), -1);
        Assert.assertEquals(grouper.getMaxGroupChanged(), -1);
    }

    @Test
    public void addFiles() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertEquals(grouper.numGroups(), 2);
        Assert.assertEquals(grouper.getFileNumbers(0), "(0/3)");
        Assert.assertEquals(grouper.getFileNumbers(1), "(0/2)");
    }

    @Test
    public void fileDone() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertEquals(grouper.getFileNumbers(0), "(0/3)");
        Assert.assertEquals(grouper.getFileNumbers(1), "(0/2)");
        grouper.fileDone("G1", "U2");
        Assert.assertEquals(grouper.getFileNumbers(0), "(1/3)");
        Assert.assertEquals(grouper.getFileNumbers(1), "(0/2)");
        grouper.fileDone("G2", "U5");
        Assert.assertEquals(grouper.getFileNumbers(1), "(1/2)");
        grouper.fileDone("G1", "U1");
        grouper.fileDone("G1", "U3");
        Assert.assertEquals(grouper.getFileNumbers(0), "(3/3)");
    }

    @Test
    public void numGroups() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        Assert.assertEquals(grouper.numGroups(), 0);
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        Assert.assertEquals(grouper.numGroups(), 1);
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U4"); add("U5"); add("U6"); }});
        Assert.assertEquals(grouper.numGroups(), 2);
    }

    @Test
    public void getDescription() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U4"); add("U5"); add("U6"); }});
        Assert.assertEquals(grouper.getDescription(0), "desc1");
        Assert.assertEquals(grouper.getDescription(1), "desc2");
    }

    @Test
    public void getDate() throws Exception {

    }

    @Test
    public void getModality() throws Exception {

    }


    @Test
    public void getFileNumbers() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertEquals(grouper.getFileNumbers(0), "(0/3)");
        Assert.assertEquals(grouper.getFileNumbers(1), "(0/2)");
        grouper.fileDone("G1", "U2");
        Assert.assertEquals(grouper.getFileNumbers(0), "(1/3)");
        Assert.assertEquals(grouper.getFileNumbers(1), "(0/2)");
        grouper.fileDone("G2", "U5");
        Assert.assertEquals(grouper.getFileNumbers(1), "(1/2)");
        grouper.fileDone("G1", "U1");
        grouper.fileDone("G1", "U3");
        Assert.assertEquals(grouper.getFileNumbers(0), "(3/3)");
    }

    @Test
    public void getStatus() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertEquals(grouper.getStatus(0), "Waiting");
        Assert.assertEquals(grouper.getStatus(1), "Waiting");
        grouper.fileDone("G1", "U2");
        Assert.assertEquals(grouper.getStatus(0), "Uploading");
        Assert.assertEquals(grouper.getStatus(1), "Waiting");
        grouper.fileDone("G1", "U1");
        grouper.fileDone("G1", "U3");
        grouper.fileDone("G2", "U4");
        Assert.assertEquals(grouper.getStatus(0), "Done");
        Assert.assertEquals(grouper.getStatus(1), "Uploading");
        grouper.fileDone("G2", "U5");
        Assert.assertEquals(grouper.getStatus(0), "Done");
        Assert.assertEquals(grouper.getStatus(1), "Done");
    }

    @Test
    public void getMaxGroupChanged() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        Assert.assertFalse(grouper.getAnyGroupsAddedOrRemoved());
        Assert.assertEquals(grouper.getMinGroupChanged(), -1);
        Assert.assertEquals(grouper.getMaxGroupChanged(), -1);
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U7"); add("U8"); }});

        grouper.resetChanged();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U4"); add("U5"); add("U6"); }});
        Assert.assertEquals(grouper.getMinGroupChanged(), 0);
        Assert.assertEquals(grouper.getMaxGroupChanged(), 0);
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U9"); add("U10"); add("U11"); }});
        Assert.assertEquals(grouper.getMinGroupChanged(), 0);
        Assert.assertEquals(grouper.getMaxGroupChanged(), 1);

        grouper.addFiles("G3", "date", "modality", "desc3", new ArrayList<String>() {{ add("U34"); add("U35"); add("U36"); }});
        grouper.resetChanged();
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U9"); add("U10"); add("U11"); }});
        grouper.addFiles("G3", "date", "modality", "desc3", new ArrayList<String>() {{ add("U341"); add("U351"); add("U361"); }});
        Assert.assertEquals(grouper.getMinGroupChanged(), 1);
        Assert.assertEquals(grouper.getMaxGroupChanged(), 2);
    }

    @Test
    public void getMinGroupChanged() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        Assert.assertFalse(grouper.getAnyGroupsAddedOrRemoved());
        Assert.assertEquals(grouper.getMinGroupChanged(), -1);
        Assert.assertEquals(grouper.getMaxGroupChanged(), -1);
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U7"); add("U8"); }});

        grouper.resetChanged();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U4"); add("U5"); add("U6"); }});
        Assert.assertEquals(grouper.getMinGroupChanged(), 0);
        Assert.assertEquals(grouper.getMaxGroupChanged(), 0);
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U9"); add("U10"); add("U11"); }});
        Assert.assertEquals(grouper.getMinGroupChanged(), 0);
        Assert.assertEquals(grouper.getMaxGroupChanged(), 1);

        grouper.addFiles("G3", "date", "modality", "desc3", new ArrayList<String>() {{ add("U34"); add("U35"); add("U36"); }});
        grouper.resetChanged();
        grouper.addFiles("G2", "date", "modality", "desc2", new ArrayList<String>() {{ add("U9"); add("U10"); add("U11"); }});
        grouper.addFiles("G3", "date", "modality", "desc3", new ArrayList<String>() {{ add("U341"); add("U351"); add("U361"); }});
        Assert.assertEquals(grouper.getMinGroupChanged(), 1);
        Assert.assertEquals(grouper.getMaxGroupChanged(), 2);
    }

    @Test
    public void getAnyGroupsAddedOrRemoved() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper();
        Assert.assertFalse(grouper.getAnyGroupsAddedOrRemoved());
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        Assert.assertTrue(grouper.getAnyGroupsAddedOrRemoved());
        grouper.resetChanged();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertFalse(grouper.getAnyGroupsAddedOrRemoved());
    }

    @Test
    public void maxGroups() throws Exception {
        FileStatusGrouper grouper = new FileStatusGrouper(3);
        Assert.assertFalse(grouper.getAnyGroupsAddedOrRemoved());
        Assert.assertEquals(grouper.numGroups(), 0);
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U1"); add("U2"); add("U3"); }});
        Assert.assertEquals(grouper.numGroups(), 1);
        grouper.addFiles("G2", "date", "modality", "desc1", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertEquals(grouper.numGroups(), 2);
        grouper.addFiles("G3", "date", "modality", "desc1", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertEquals(grouper.numGroups(), 3);
        grouper.addFiles("G4", "date", "modality", "desc1", new ArrayList<String>() {{ add("U6"); add("U7"); }});
        Assert.assertEquals(grouper.numGroups(), 3);
        grouper.resetChanged();
        grouper.addFiles("G1", "date", "modality", "desc1", new ArrayList<String>() {{ add("U4"); add("U5"); }});
        Assert.assertEquals(grouper.numGroups(), 3);
    }
}