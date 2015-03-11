/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSequenceAttribute_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestSequenceAttributeDelimitedString.suite());
		suite.addTest(TestSequenceAttributeStringsWithinItems.suite());
		return suite;
	}
	
}
