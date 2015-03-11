/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.DateTimeAttribute;

import java.util.TimeZone;

import junit.framework.*;

public class TestDateTimeAttributeTimeZone extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDateTimeAttributeTimeZone(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDateTimeAttributeTimeZone.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDateTimeAttributeTimeZone");
		
		suite.addTest(new TestDateTimeAttributeTimeZone("TestDateTimeAttributeTimeZone_Compare"));
		

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestDateTimeAttributeTimeZone_Compare() {
		String[] dicom = {
			"+0000",
			"-0800",
			"+0100",
			"+1000"
		};
		TimeZone[] java = {
			TimeZone.getTimeZone("GMT"),
			TimeZone.getTimeZone("America/Los_Angeles"),
			TimeZone.getTimeZone("Europe/Berlin"),
			TimeZone.getTimeZone("Australia/Melbourne")
		};
		
		for (int i=0; i<dicom.length; ++i ) {
			assertEquals(dicom[i],java[i].getRawOffset(),DateTimeAttribute.getTimeZone(dicom[i]).getRawOffset());	// test raw offset, not just eqiality, since have different IDs
		}
	}

}
