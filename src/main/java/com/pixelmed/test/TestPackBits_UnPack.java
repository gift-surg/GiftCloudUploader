/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.UnPackBits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Arrays;

import junit.framework.*;

public class TestPackBits_UnPack extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestPackBits_UnPack(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestPackBits_UnPack.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestPackBits_UnPack");
		
		suite.addTest(new TestPackBits_UnPack("TestPackBits_UnPack_AppleSample_Whole_Arrays"));
		suite.addTest(new TestPackBits_UnPack("TestPackBits_UnPack_AppleSample_Whole_Streams"));
		suite.addTest(new TestPackBits_UnPack("TestPackBits_UnPack_AppleSample_Size"));
		suite.addTest(new TestPackBits_UnPack("TestPackBits_UnPack_AppleSample_Parts"));
		suite.addTest(new TestPackBits_UnPack("TestPackBits_UnPack_AppleSample_IntoWholeArray"));
		suite.addTest(new TestPackBits_UnPack("TestPackBits_UnPack_AppleSample_IntoTooShortArray"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	// Apple example from "http://en.wikipedia.org/wiki/PackBits" and "http://web.archive.org/web/20080705155158/http://developer.apple.com/technotes/tn/tn1023.html"
	
	byte[] packed = { (byte)0xFE, (byte)0xAA, (byte)0x02, (byte)0x80, (byte)0x00, (byte)0x2A, (byte)0xFD, (byte)0xAA, (byte)0x03, (byte)0x80, (byte)0x00, (byte)0x2A, (byte)0x22, (byte)0xF7, (byte)0xAA };
	byte[] unpacked = { (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x80, (byte)0x00, (byte)0x2A, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x80, (byte)0x00, (byte)0x2A, (byte)0x22, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA };
	
	public void TestPackBits_UnPack_AppleSample_Whole_Arrays() throws Exception {
		byte[] decoded = UnPackBits.decode(packed);
		assertTrue("Checking unpacked",Arrays.equals(unpacked,decoded));
	}
	
	public void TestPackBits_UnPack_AppleSample_Whole_Streams() throws Exception {
		ByteArrayOutputStream o = UnPackBits.decode(new ByteArrayInputStream(packed));
		assertTrue("Checking unpacked",Arrays.equals(unpacked,o.toByteArray()));
	}
	
	public void TestPackBits_UnPack_AppleSample_Size() throws Exception {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		UnPackBits.decode(new ByteArrayInputStream(packed),o,unpacked.length);
		byte[] decoded = o.toByteArray();
		assertTrue("Checking unpacked",Arrays.equals(unpacked,decoded));
	}
	
	public void TestPackBits_UnPack_AppleSample_Parts() throws Exception {
		ByteArrayInputStream i = new ByteArrayInputStream(packed);
		ByteArrayOutputStream o = new ByteArrayOutputStream();
//System.err.println("Need total of "+unpacked.length+" output values");
		int first = unpacked.length/2;
//System.err.println("Decoding "+first+" output values");
		int second = unpacked.length - UnPackBits.decode(i,o,first);
//System.err.println("Decoding "+second+" output values");
		UnPackBits.decode(i,o,second);
		byte[] decoded = o.toByteArray();
		assertTrue("Checking unpacked",Arrays.equals(unpacked,decoded));
	}
		
	public void TestPackBits_UnPack_AppleSample_IntoWholeArray() throws Exception {
		byte[] decoded = new byte[unpacked.length];
		int done = UnPackBits.decode(new ByteArrayInputStream(packed),decoded,0/*offset*/,unpacked.length);
		assertTrue("Checking unpacked",Arrays.equals(unpacked,decoded));
	}
		
	public void TestPackBits_UnPack_AppleSample_IntoTooShortArray() throws Exception {
		byte[] decoded = new byte[unpacked.length-1];
		int done = UnPackBits.decode(new ByteArrayInputStream(packed),decoded,0/*offset*/,decoded.length);
		assertTrue("Checking unpacked",Arrays.equals(Arrays.copyOf(unpacked,decoded.length),decoded));
		assertEquals("Checking length done",decoded.length,done);
	}
	
}
