/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

public class TestSafePrivate_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestSafePrivatePhilipsPETRelated.suite());
		suite.addTest(TestSafePrivatePhilipsDoseRelated.suite());
		suite.addTest(TestSafePrivateGEDoseRelated.suite());
		suite.addTest(TestSafePrivateGEPACSRelated.suite());
		suite.addTest(TestSafePrivateGEMRRelated.suite());
		suite.addTest(TestSafePrivateNQResultsRelated.suite());
		return suite;
	}
	
}
