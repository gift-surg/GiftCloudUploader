package uk.ac.ucl.cs.cmic.giftcloud.util;

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

public class MultiUploaderUtilsTest {

    @Test
    public void testIsDirectoryWritable() throws Exception {
        File tempDir = Files.createTempDir();
        final String tempDirString = tempDir.getPath();
        Assert.assertTrue(MultiUploaderUtils.isDirectoryWritable(tempDirString));

        final String rootDir = "/";
        Assert.assertFalse(MultiUploaderUtils.isDirectoryWritable(rootDir));
    }
}