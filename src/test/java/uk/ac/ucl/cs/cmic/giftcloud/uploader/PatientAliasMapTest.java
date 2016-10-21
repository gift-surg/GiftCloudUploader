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
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;

import java.util.HashMap;
import java.util.Map;

public class PatientAliasMapTest {

    @Test
    public void testContainsKey() throws Exception {
        final PatientAliasMap patientAliasMap = new PatientAliasMap();
        Assert.assertFalse(patientAliasMap.containsPpid("HASHEDPATIENTID1"));

        patientAliasMap.addSubjectAlias("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        Assert.assertTrue(patientAliasMap.containsPpid("HASHEDPATIENTID1"));
        Assert.assertFalse(patientAliasMap.containsPpid("HASHEDPATIENTID2"));

        patientAliasMap.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");
        Assert.assertTrue(patientAliasMap.containsPpid("HASHEDPATIENTID1"));
        Assert.assertTrue(patientAliasMap.containsPpid("HASHEDPATIENTID2"));
    }

    @Test
    public void testAddAliasAndGetAlias() throws Exception {
        final PatientAliasMap patientAliasMap = new PatientAliasMap();
        Assert.assertFalse(patientAliasMap.getSubjectLabel("HASHEDPATIENTID1").isPresent());

        patientAliasMap.addSubjectAlias("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        Assert.assertTrue(patientAliasMap.getSubjectLabel("HASHEDPATIENTID1").isPresent());
        Assert.assertEquals(patientAliasMap.getSubjectLabel("HASHEDPATIENTID1").get(), GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"));
        Assert.assertFalse(patientAliasMap.getSubjectLabel("HASHEDPATIENTID2").isPresent());

        patientAliasMap.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");
        Assert.assertTrue(patientAliasMap.getSubjectLabel("HASHEDPATIENTID1").isPresent());
        Assert.assertEquals(patientAliasMap.getSubjectLabel("HASHEDPATIENTID1").get(), GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"));
        Assert.assertTrue(patientAliasMap.getSubjectLabel("HASHEDPATIENTID2").isPresent());
        Assert.assertEquals(patientAliasMap.getSubjectLabel("HASHEDPATIENTID2").get(), GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"));
    }

    @Test
    public void testGetMap() throws Exception {
        final PatientAliasMap patientAliasMap1 = new PatientAliasMap();
        patientAliasMap1.addSubjectAlias("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        patientAliasMap1.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");

        final Map<String, PatientAliasMap.SubjectAliasRecord> aliasRecordMap = patientAliasMap1.getMap();
        final Map<String, PatientAliasMap.SubjectAliasRecord> expectedMap = new HashMap<String, PatientAliasMap.SubjectAliasRecord>();
        expectedMap.put("HASHEDPATIENTID1", new PatientAliasMap.SubjectAliasRecord("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1"));
        expectedMap.put("HASHEDPATIENTID2", new PatientAliasMap.SubjectAliasRecord("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2"));
        Assert.assertEquals(expectedMap, aliasRecordMap);
    }

    @Test
    public void testEquals() throws Exception {
        final PatientAliasMap patientAliasMap1 = new PatientAliasMap();
        patientAliasMap1.addSubjectAlias("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        patientAliasMap1.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap2 = new PatientAliasMap();
        patientAliasMap2.addSubjectAlias("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        patientAliasMap2.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap3 = new PatientAliasMap();
        patientAliasMap3.addSubjectAlias("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME3");
        patientAliasMap3.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap4 = new PatientAliasMap();
        patientAliasMap4.addSubjectAlias("HASHEDPATIENTID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME3");
        patientAliasMap4.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTIDx", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap5 = new PatientAliasMap();
        patientAliasMap5.addSubjectAlias("HASHEDPATIENTIDh", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME3");
        patientAliasMap5.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTIDx", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap6 = new PatientAliasMap();
        patientAliasMap6.addSubjectAlias("HASHEDPATIENTIDh", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME3");
        patientAliasMap6.addSubjectAlias("HASHEDPATIENTID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2x"), "PATIENTIDx", "PATIENTNAME2");

        Assert.assertTrue(patientAliasMap1.equals(patientAliasMap2));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap3));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap4));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap5));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap6));
    }
}