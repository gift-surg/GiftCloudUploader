/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestAnatomy_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestAnatomyConcept.suite());
		suite.addTest(TestAnatomyCombined.suite());
		suite.addTest(TestAnatomyFind.suite());
		suite.addTest(TestLaterality.suite());
		return suite;
	}
	
}
