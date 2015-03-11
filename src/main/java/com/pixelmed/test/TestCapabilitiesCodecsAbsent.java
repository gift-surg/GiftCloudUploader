/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.CapabilitiesAvailable;

import junit.framework.*;

public class TestCapabilitiesCodecsAbsent extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCapabilitiesCodecsAbsent(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCapabilitiesCodecsAbsent.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCapabilitiesCodecsAbsent");
		
		suite.addTest(new TestCapabilitiesCodecsAbsent("TestCapabilitiesCodecsAbsent_JPEGBaselineSelectiveBlockRedaction"));
		suite.addTest(new TestCapabilitiesCodecsAbsent("TestCapabilitiesCodecsAbsent_Bzip2"));
		suite.addTest(new TestCapabilitiesCodecsAbsent("TestCapabilitiesCodecsAbsent_LosslessJPEG"));
		suite.addTest(new TestCapabilitiesCodecsAbsent("TestCapabilitiesCodecsAbsent_JPEG2000Part1"));
		suite.addTest(new TestCapabilitiesCodecsAbsent("TestCapabilitiesCodecsAbsent_JPEGLS"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCapabilitiesCodecsAbsent_JPEGBaselineSelectiveBlockRedaction() throws Exception {
		assertTrue("Checking JPEGBaselineSelectiveBlockRedaction is absent",!CapabilitiesAvailable.haveJPEGBaselineSelectiveBlockRedaction());
	}
	
	public void TestCapabilitiesCodecsAbsent_Bzip2() throws Exception {
		assertTrue("Checking Bzip2 is absent",!CapabilitiesAvailable.haveBzip2Support());
	}
	
	public void TestCapabilitiesCodecsAbsent_LosslessJPEG() throws Exception {
		assertTrue("Checking Lossless JPEG is absent",!CapabilitiesAvailable.haveJPEGLosslessCodec());
	}
	
	public void TestCapabilitiesCodecsAbsent_JPEG2000Part1() throws Exception {
		assertTrue("Checking JPEG 2000 Part 1 is absent",!CapabilitiesAvailable.haveJPEG2000Part1Codec());
	}
	
	public void TestCapabilitiesCodecsAbsent_JPEGLS() throws Exception {
		assertTrue("Checking JPEG-LS is absent",!CapabilitiesAvailable.haveJPEGLSCodec());
	}

}
