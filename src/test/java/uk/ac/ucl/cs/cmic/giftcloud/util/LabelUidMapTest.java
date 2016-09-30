/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class LabelUidMapTest {

    class LabelType {

    }

    class ValueType {

    }

    @Test
    public void testGetValueForLabel() throws Exception {
        LabelType label1 = new LabelType();
        ValueType value1 = new ValueType();
        String uid1 = "uid1";

        LabelType label2 = new LabelType();
        ValueType value2 = new ValueType();
        String uid2 = "uid2";

        LabelUidMap<LabelType, ValueType> map = new LabelUidMap<LabelType, ValueType>();
        map.put(label1, uid1, value1);
        map.put(label2, uid2, value2);

        ValueType result1 = map.getValueForLabel(label1);
        ValueType result2 = map.getValueForLabel(label2);

        Assert.assertSame(result1, value1);
        Assert.assertSame(result2, value2);
        Assert.assertFalse(result2.equals(value1));
        Assert.assertFalse(result1.equals(value2));
    }

    @Test
    public void testGetValueForUid() throws Exception {
        LabelType label1 = new LabelType();
        ValueType value1 = new ValueType();
        String uid1 = "uid1";

        LabelType label2 = new LabelType();
        ValueType value2 = new ValueType();
        String uid2 = "uid2";

        LabelUidMap<LabelType, ValueType> map = new LabelUidMap<LabelType, ValueType>();
        map.put(label1, uid1, value1);
        map.put(label2, uid2, value2);

        ValueType result1 = map.getValueForUid(uid1);
        ValueType result2 = map.getValueForUid(uid2);

        Assert.assertSame(result1, value1);
        Assert.assertSame(result2, value2);
        Assert.assertFalse(result2.equals(value1));
        Assert.assertFalse(result1.equals(value2));
    }

    @Test
    public void testContainsLabel() throws Exception {
        LabelType label1 = new LabelType();
        ValueType value1 = new ValueType();
        String uid1 = "uid1";

        LabelType label2 = new LabelType();
        ValueType value2 = new ValueType();
        String uid2 = "uid2";

        LabelType label3 = new LabelType();
        ValueType value3 = new ValueType();
        String uid3 = "uid3";

        LabelUidMap<LabelType, ValueType> map = new LabelUidMap<LabelType, ValueType>();
        map.put(label1, uid1, value1);
        map.put(label2, uid2, value2);

        Assert.assertTrue(map.containsLabel(label1));
        Assert.assertTrue(map.containsLabel(label2));
        Assert.assertFalse(map.containsLabel(label3));
    }

    @Test
    public void testContainsUid() throws Exception {
        LabelType label1 = new LabelType();
        ValueType value1 = new ValueType();
        String uid1 = "uid1";

        LabelType label2 = new LabelType();
        ValueType value2 = new ValueType();
        String uid2 = "uid2";

        LabelType label3 = new LabelType();
        ValueType value3 = new ValueType();
        String uid3 = "uid3";

        LabelUidMap<LabelType, ValueType> map = new LabelUidMap<LabelType, ValueType>();
        map.put(label1, uid1, value1);
        map.put(label2, uid2, value2);

        Assert.assertTrue(map.containsUid(uid1));
        Assert.assertTrue(map.containsUid(uid2));
        Assert.assertFalse(map.containsUid(uid3));
    }

    @Test
    public void testGetLabelMap() throws Exception {
        LabelType label1 = new LabelType();
        ValueType value1 = new ValueType();
        String uid1 = "uid1";

        LabelType label2 = new LabelType();
        ValueType value2 = new ValueType();
        String uid2 = "uid2";

        LabelType label3 = new LabelType();
        ValueType value3 = new ValueType();
        String uid3 = "uid3";

        LabelUidMap<LabelType, ValueType> map = new LabelUidMap<LabelType, ValueType>();
        map.put(label1, uid1, value1);
        map.put(label2, uid2, value2);
        map.put(label3, uid3, value3);

        Map<LabelType, ValueType> labelMap = map.getLabelMap();

        Map<LabelType, ValueType> expectedLabelMap = new HashMap<LabelType, ValueType>();
        expectedLabelMap.put(label1, value1);
        expectedLabelMap.put(label2, value2);
        expectedLabelMap.put(label3, value3);

        Assert.assertEquals(labelMap, expectedLabelMap);
    }

    @Test
    public void testGetUidMap() throws Exception {
        LabelType label1 = new LabelType();
        ValueType value1 = new ValueType();
        String uid1 = "uid1";

        LabelType label2 = new LabelType();
        ValueType value2 = new ValueType();
        String uid2 = "uid2";

        LabelType label3 = new LabelType();
        ValueType value3 = new ValueType();
        String uid3 = "uid3";

        LabelUidMap<LabelType, ValueType> map = new LabelUidMap<LabelType, ValueType>();
        map.put(label1, uid1, value1);
        map.put(label2, uid2, value2);
        map.put(label3, uid3, value3);

        Map<String, ValueType> uidMap = map.getUidMap();

        Map<String, ValueType> expectedUidMap = new HashMap<String, ValueType>();
        expectedUidMap.put(uid1, value1);
        expectedUidMap.put(uid2, value2);
        expectedUidMap.put(uid3, value3);

        Assert.assertEquals(uidMap, expectedUidMap);
    }
}