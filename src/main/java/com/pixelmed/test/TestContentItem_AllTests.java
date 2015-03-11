/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import junit.framework.*;

public class TestContentItem_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestCodeContentItemValueMatching.suite());
		suite.addTest(TestNumericContentItemLocaleEffect.suite());
		suite.addTest(TestSpatialCoordinatesContentItemLocaleEffect.suite());
		suite.addTest(TestSpatialCoordinates3DContentItemLocaleEffect.suite());
		suite.addTest(TestNumericContentItemFloatingAndRational.suite());
		return suite;
	}
	
}
