/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestDateTimeAttribute_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestDateTimeAttributeTimeSinceEpochExtraction.suite());
		suite.addTest(TestDateTimeAttributeTimeZone.suite());
		return suite;
	}
	
}
