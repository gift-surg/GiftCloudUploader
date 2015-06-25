package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PatientAliasMapTest {

    @Test
    public void testContainsKey() throws Exception {
        final PatientAliasMap patientAliasMap = new PatientAliasMap();
        Assert.assertFalse(patientAliasMap.containsPpid("HASHEDPATIENTID1"));

        patientAliasMap.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        Assert.assertTrue(patientAliasMap.containsPpid("HASHEDPATIENTID1"));
        Assert.assertFalse(patientAliasMap.containsPpid("HASHEDPATIENTID2"));

        patientAliasMap.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");
        Assert.assertTrue(patientAliasMap.containsPpid("HASHEDPATIENTID1"));
        Assert.assertTrue(patientAliasMap.containsPpid("HASHEDPATIENTID2"));
    }

    @Test
    public void testAddAliasAndGetAlias() throws Exception {
        final PatientAliasMap patientAliasMap = new PatientAliasMap();
        Assert.assertFalse(patientAliasMap.getAlias("HASHEDPATIENTID1").isPresent());

        patientAliasMap.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        Assert.assertTrue(patientAliasMap.getAlias("HASHEDPATIENTID1").isPresent());
        Assert.assertEquals(patientAliasMap.getAlias("HASHEDPATIENTID1").get(), "ALIAS1");
        Assert.assertFalse(patientAliasMap.getAlias("HASHEDPATIENTID2").isPresent());

        patientAliasMap.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");
        Assert.assertTrue(patientAliasMap.getAlias("HASHEDPATIENTID1").isPresent());
        Assert.assertEquals(patientAliasMap.getAlias("HASHEDPATIENTID1").get(), "ALIAS1");
        Assert.assertTrue(patientAliasMap.getAlias("HASHEDPATIENTID2").isPresent());
        Assert.assertEquals(patientAliasMap.getAlias("HASHEDPATIENTID2").get(), "ALIAS2");
    }

    @Test
    public void testGetMap() throws Exception {
        final PatientAliasMap patientAliasMap1 = new PatientAliasMap();
        patientAliasMap1.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        patientAliasMap1.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final Map<String, PatientAliasMap.PatientAliasRecord> aliasRecordMap = patientAliasMap1.getMap();
        final Map<String, PatientAliasMap.PatientAliasRecord> expectedMap = new HashMap<String, PatientAliasMap.PatientAliasRecord>();
        expectedMap.put("HASHEDPATIENTID1", new PatientAliasMap.PatientAliasRecord("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1"));
        expectedMap.put("HASHEDPATIENTID2", new PatientAliasMap.PatientAliasRecord("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2"));
        Assert.assertEquals(expectedMap, aliasRecordMap);
    }

    @Test
    public void testEquals() throws Exception {
        final PatientAliasMap patientAliasMap1 = new PatientAliasMap();
        patientAliasMap1.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        patientAliasMap1.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap2 = new PatientAliasMap();
        patientAliasMap2.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        patientAliasMap2.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap3 = new PatientAliasMap();
        patientAliasMap3.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        patientAliasMap3.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap4 = new PatientAliasMap();
        patientAliasMap4.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        patientAliasMap4.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTIDx", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap5 = new PatientAliasMap();
        patientAliasMap5.addAlias("HASHEDPATIENTIDh", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        patientAliasMap5.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTIDx", "PATIENTNAME2");

        final PatientAliasMap patientAliasMap6 = new PatientAliasMap();
        patientAliasMap6.addAlias("HASHEDPATIENTIDh", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        patientAliasMap6.addAlias("HASHEDPATIENTID2", "ALIAS2x", "PATIENTIDx", "PATIENTNAME2");

        Assert.assertTrue(patientAliasMap1.equals(patientAliasMap2));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap3));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap4));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap5));
        Assert.assertFalse(patientAliasMap1.equals(patientAliasMap6));
    }
}