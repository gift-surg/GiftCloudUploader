package uk.ac.ucl.cs.cmic.giftcloud.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/**
 * Created by tom on 12/11/2015.
 */
public class OptionalTest {

    @Test
    public void testEmpty() throws Exception {
        Optional<String> o = Optional.empty();
        Assert.assertFalse(o.isPresent());

        // Check that get() fails
        try {
            o.get();
            Assert.fail();
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void testOf() throws Exception {
        Optional<String> o = Optional.of("ABCDE");
        Assert.assertTrue(o.isPresent());
        Assert.assertEquals(o.get(), "ABCDE");

        // Check that of() with null value fails
        try {
            Optional.of(null);
            Assert.fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testOfNullable() throws Exception {
        {
            Optional<String> o = Optional.ofNullable("ABCDE");
            Assert.assertTrue(o.isPresent());
            Assert.assertEquals(o.get(), "ABCDE");
        }
        {
            Optional<String> o = Optional.ofNullable(null);
            Assert.assertFalse(o.isPresent());
        }
    }

    @Test
    public void testGet() throws Exception {
        {
            Optional<String> o = Optional.of("ABCDE");
            Assert.assertEquals(o.get(), "ABCDE");
        }
        {
            Optional<String> o = Optional.empty();
            try {
                o.get();
                Assert.fail();
            } catch (NoSuchElementException e) {
            }
        }
    }

    @Test
    public void testIsPresent() throws Exception {
        {
            Optional<String> o = Optional.of("ABCDE");
            Assert.assertTrue(o.isPresent());
        }
        {
            Optional<String> o = Optional.empty();
            Assert.assertFalse(o.isPresent());
        }
    }

    @Test
    public void testOrElse() throws Exception {
        {
            Optional<String> o = Optional.of("ABCDE");
            Assert.assertEquals(o.orElse("NOTSET"), "ABCDE");
        }
        {
            Optional<String> o = Optional.empty();
            Assert.assertEquals(o.orElse("NOTSET"), "NOTSET");
        }
    }

    @Test
    public void testEquals() throws Exception {
        {
            Optional<String> o = Optional.of("ABCDE");
            Optional<String> o2 = Optional.of("ABCDE");
            Assert.assertEquals(o, o2);
        }
        {
            Optional<String> o = Optional.of("ABCDE");
            Optional<String> o2 = Optional.of("ABCDEF");
            Assert.assertNotSame(o, o2);
        }
        {
            Optional<String> o = Optional.of("ABCDE");
            Optional<String> o2 = Optional.empty();
            Assert.assertNotSame(o, o2);
        }
        {
            Optional<String> o = Optional.empty();
            Optional<String> o2 = Optional.of("ABCDE");
            Assert.assertNotSame(o, o2);
        }
    }

    @Test
    public void testHashCode() throws Exception {

    }

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals(Optional.of("ABCDE").toString(), "Optional[ABCDE]");
        Assert.assertEquals(Optional.empty().toString(), "Optional.empty");
    }
}