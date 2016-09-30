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

public class OneWayHashTest {
    @Test
    public void testCreateFromDicom() {
        final String hash1 = OneWayHash.hashUid("12.34.56.78");
        final String hash2 = OneWayHash.hashUid("12.34.56.78  ");
        final String hash3 = OneWayHash.hashUid("12.34.56.78.9");
        final String hashnull = OneWayHash.hashUid("");

        Assert.assertEquals(hash1, hash2);
        Assert.assertFalse(hash1.equals(hash3));
        Assert.assertFalse(hash2.equals(hash3));
        Assert.assertTrue(hashnull == null);
    }
}