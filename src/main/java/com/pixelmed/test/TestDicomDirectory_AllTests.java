/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestDicomDirectory_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestDicomDirectoryRecordFactory.suite());
		suite.addTest(TestDicomDirectoryRecordSortOrder.suite());
		return suite;
	}
	
}
