/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.CapabilitiesAvailable;

import junit.framework.*;

public class TestCapabilitiesCodecsPresent extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCapabilitiesCodecsPresent(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCapabilitiesCodecsPresent.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCapabilitiesCodecsPresent");
		
		suite.addTest(new TestCapabilitiesCodecsPresent("TestCapabilitiesCodecsPresent_JPEGBaselineSelectiveBlockRedaction"));
		suite.addTest(new TestCapabilitiesCodecsPresent("TestCapabilitiesCodecsPresent_Bzip2"));
		//suite.addTest(new TestCapabilitiesCodecsPresent("TestCapabilitiesCodecsPresent_LosslessJPEG"));	// cannot test in Mac development environment, since never present :(
		suite.addTest(new TestCapabilitiesCodecsPresent("TestCapabilitiesCodecsPresent_JPEG2000Part1"));
		//suite.addTest(new TestCapabilitiesCodecsPresent("TestCapabilitiesCodecsPresent_JPEGLS"));			// cannot test in Mac development environment, since never present :(
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCapabilitiesCodecsPresent_JPEGBaselineSelectiveBlockRedaction() throws Exception {
		assertTrue("Checking JPEGBaselineSelectiveBlockRedaction is present",CapabilitiesAvailable.haveJPEGBaselineSelectiveBlockRedaction());
	}
	
	public void TestCapabilitiesCodecsPresent_Bzip2() throws Exception {
		assertTrue("Checking Bzip2 is present",CapabilitiesAvailable.haveBzip2Support());
	}
	
	public void TestCapabilitiesCodecsPresent_LosslessJPEG() throws Exception {
		assertTrue("Checking Lossless JPEG is present",CapabilitiesAvailable.haveJPEGLosslessCodec());
	}

	public void TestCapabilitiesCodecsPresent_JPEG2000Part1() throws Exception {
		assertTrue("Checking JPEG 2000 Part 1 is present",CapabilitiesAvailable.haveJPEG2000Part1Codec());
	}
	
	public void TestCapabilitiesCodecsPresent_JPEGLS() throws Exception {
		assertTrue("Checking JPEG-LS is present",CapabilitiesAvailable.haveJPEGLSCodec());
	}

}
