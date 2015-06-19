package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AliasMapTest {

    @Test
    public void testContainsKey() throws Exception {
        final AliasMap aliasMap = new AliasMap();
        Assert.assertFalse(aliasMap.containsKey("HASHEDPATIENTID1"));

        aliasMap.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        Assert.assertTrue(aliasMap.containsKey("HASHEDPATIENTID1"));
        Assert.assertFalse(aliasMap.containsKey("HASHEDPATIENTID2"));

        aliasMap.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");
        Assert.assertTrue(aliasMap.containsKey("HASHEDPATIENTID1"));
        Assert.assertTrue(aliasMap.containsKey("HASHEDPATIENTID2"));
    }

    @Test
    public void testAddAliasAndGetAlias() throws Exception {
        final AliasMap aliasMap = new AliasMap();
        Assert.assertFalse(aliasMap.getAlias("HASHEDPATIENTID1").isPresent());

        aliasMap.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        Assert.assertTrue(aliasMap.getAlias("HASHEDPATIENTID1").isPresent());
        Assert.assertEquals(aliasMap.getAlias("HASHEDPATIENTID1").get(), "ALIAS1");
        Assert.assertFalse(aliasMap.getAlias("HASHEDPATIENTID2").isPresent());

        aliasMap.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");
        Assert.assertTrue(aliasMap.getAlias("HASHEDPATIENTID1").isPresent());
        Assert.assertEquals(aliasMap.getAlias("HASHEDPATIENTID1").get(), "ALIAS1");
        Assert.assertTrue(aliasMap.getAlias("HASHEDPATIENTID2").isPresent());
        Assert.assertEquals(aliasMap.getAlias("HASHEDPATIENTID2").get(), "ALIAS2");
    }

    @Test
    public void testGetMap() throws Exception {
        final AliasMap aliasMap1 = new AliasMap();
        aliasMap1.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        aliasMap1.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final Map<String, AliasMap.AliasRecord> aliasRecordMap = aliasMap1.getMap();
        final Map<String, AliasMap.AliasRecord> expectedMap = new HashMap<String, AliasMap.AliasRecord>();
        expectedMap.put("HASHEDPATIENTID1", new AliasMap.AliasRecord("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1"));
        expectedMap.put("HASHEDPATIENTID2", new AliasMap.AliasRecord("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2"));
        Assert.assertEquals(expectedMap, aliasRecordMap);
    }

    @Test
    public void testEquals() throws Exception {
        final AliasMap aliasMap1 = new AliasMap();
        aliasMap1.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        aliasMap1.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final AliasMap aliasMap2 = new AliasMap();
        aliasMap2.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME1");
        aliasMap2.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final AliasMap aliasMap3 = new AliasMap();
        aliasMap3.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        aliasMap3.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTID2", "PATIENTNAME2");

        final AliasMap aliasMap4 = new AliasMap();
        aliasMap4.addAlias("HASHEDPATIENTID1", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        aliasMap4.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTIDx", "PATIENTNAME2");

        final AliasMap aliasMap5 = new AliasMap();
        aliasMap5.addAlias("HASHEDPATIENTIDh", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        aliasMap5.addAlias("HASHEDPATIENTID2", "ALIAS2", "PATIENTIDx", "PATIENTNAME2");

        final AliasMap aliasMap6 = new AliasMap();
        aliasMap6.addAlias("HASHEDPATIENTIDh", "ALIAS1", "PATIENTID1", "PATIENTNAME3");
        aliasMap6.addAlias("HASHEDPATIENTID2", "ALIAS2x", "PATIENTIDx", "PATIENTNAME2");

        Assert.assertTrue(aliasMap1.equals(aliasMap2));
        Assert.assertFalse(aliasMap1.equals(aliasMap3));
        Assert.assertFalse(aliasMap1.equals(aliasMap4));
        Assert.assertFalse(aliasMap1.equals(aliasMap5));
        Assert.assertFalse(aliasMap1.equals(aliasMap6));
    }
}