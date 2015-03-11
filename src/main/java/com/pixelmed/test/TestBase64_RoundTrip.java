/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.Base64;

import junit.framework.*;

public class TestBase64_RoundTrip extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestBase64_RoundTrip(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestBase64_RoundTrip.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestBase64_RoundTrip");
		
		suite.addTest(new TestBase64_RoundTrip("TestBase64_RoundTrip_SubNormalAllMantissaBitsSet"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestBase64_RoundTrip_SubNormalAllMantissaBitsSet() throws Exception {
		// See "http://randomascii.wordpress.com/2012/03/08/float-precisionfrom-zero-to-100-digits-2/" ...
		// See "http://en.wikipedia.org/wiki/Double-precision_floating-point_format#Double-precision_examples"

		double doubleValue = Double.longBitsToDouble(0x000fffffffffffffl);
		
		System.err.println();
		assertEquals("string round trip",doubleValue,Double.parseDouble(Double.toString(doubleValue)));	// assures that double has been uniquely identified (even if string representation is not precise)
		
		String stringValue = Base64.getBase64(doubleValue);
		assertEquals("floatingpointvalue",doubleValue,Base64.getDouble(stringValue));
	}
	

}
