package uk.ac.ucl.cs.cmic.giftcloud.util;

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

public class GiftCloudUtilsTest {

    @Test
    public void testIsDirectoryWritable() throws Exception {
        {
            File tempDir = Files.createTempDir();
            final String tempDirString = tempDir.getPath();
            Assert.assertTrue(GiftCloudUtils.isDirectoryWritable(tempDirString));
            tempDir.delete();
        }
        {
            File tempDir = Files.createTempDir();
            tempDir.setWritable(false);
            final String tempDirString = tempDir.getPath();
            Assert.assertFalse(GiftCloudUtils.isDirectoryWritable(tempDirString));
            tempDir.setWritable(true);
            tempDir.delete();
        }
    }

    @Test
    public void testCompareVersionStrings() throws Exception {
        Assert.assertEquals(0, GiftCloudUtils.compareVersionStrings("", ""));
        Assert.assertEquals(0, GiftCloudUtils.compareVersionStrings("1", "1"));
        Assert.assertEquals(0, GiftCloudUtils.compareVersionStrings("1.0", "1.0"));
        Assert.assertEquals(0, GiftCloudUtils.compareVersionStrings("1.2.0", "1.2.0"));
        Assert.assertEquals(0, GiftCloudUtils.compareVersionStrings("1.3.2", "1.3.2"));

        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("", "1"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1", "1.0"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1", "2"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1", "1."));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1.2.2", "1.3.0"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1.2.2", "2.2.2"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1.2.2", "1.3.2"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1.3.2", "1.3.3"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1.3.2", "1.3.2.0"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1.3.2", "1.3.2.1"));
        Assert.assertEquals(-1, GiftCloudUtils.compareVersionStrings("1.3.2", "1.3.2.debug"));

        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1", ""));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.0", "1"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("2", "1"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.", "1"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.3.2", "1.2.0"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("2.2.2", "1.2.2"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.3.2", "1.2.2"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.3.3", "1.3.2"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.3.2.0", "1.3.2"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.3.2.1", "1.3.2"));
        Assert.assertEquals(1, GiftCloudUtils.compareVersionStrings("1.3.2.debug", "1.3.2"));
    }
}