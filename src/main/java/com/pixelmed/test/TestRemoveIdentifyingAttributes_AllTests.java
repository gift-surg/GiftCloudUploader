/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import junit.framework.*;

public class TestRemoveIdentifyingAttributes_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestRemoveIdentifyingAttributes.suite());
		suite.addTest(TestPatientAgeWhenRemoveIdentifyingAttributes.suite());
		return suite;
	}
	
}
