/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/



package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Test;

public class GiftCloudLabelTest {

    @Test
    public void testSubjectLabel() {
        testForType(GiftCloudLabel.SubjectLabel.getFactory());
    }

    @Test
    public void testExperimentLabel() {
        testForType(GiftCloudLabel.ExperimentLabel.getFactory());
    }

    @Test
    public void testScanLabel() {
        testForType(GiftCloudLabel.ScanLabel.getFactory());
    }

    /**
     * Runs a test for objects of each class factory passed in
     */
    private void testForType(final GiftCloudLabel.LabelFactory factory) {
        testEquals(factory);
        testIsBlank(factory);
        testGetStringLabel(factory);
     }

    private void testEquals(GiftCloudLabel.LabelFactory factory) {
        final GiftCloudLabel label1 = factory.create("LABEL1");
        final GiftCloudLabel label2 = factory.create("LABEL1");
        final GiftCloudLabel label3 = factory.create("LABEL2");

        Assert.assertTrue(label1.equals(label2));
        Assert.assertTrue(label2.equals(label1));
        Assert.assertFalse(label1.equals(label3));
        Assert.assertFalse(label2.equals(label3));
        Assert.assertFalse(label3.equals(label1));
        Assert.assertFalse(label3.equals(label2));
    }

    private void testIsBlank(GiftCloudLabel.LabelFactory factory) {
        final GiftCloudLabel label1 = factory.create("LABEL1");
        final GiftCloudLabel label2 = factory.create("");
        final GiftCloudLabel label3 = factory.create(null);
        final GiftCloudLabel label4 = null;
        final GiftCloudLabel label5 = factory.create(" ");

        Assert.assertFalse(GiftCloudLabel.isBlank(label1));
        Assert.assertTrue(GiftCloudLabel.isBlank(label2));
        Assert.assertTrue(GiftCloudLabel.isBlank(label3));
        Assert.assertTrue(GiftCloudLabel.isBlank(label4));
        Assert.assertTrue(GiftCloudLabel.isBlank(label5));
    }

    private void testGetStringLabel(GiftCloudLabel.LabelFactory factory) {
        final GiftCloudLabel label1 = factory.create("LABEL1");
        final GiftCloudLabel label2 = factory.create("LABEL2");

        Assert.assertEquals(label1.getStringLabel(), "LABEL1");
        Assert.assertEquals(label2.getStringLabel(), "LABEL2");
    }

}