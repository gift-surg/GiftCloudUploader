/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

public class TestCTDose_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestCTIrradiationEventDataFromImages.suite());
		suite.addTest(TestScopeOfDoseAccummulation.suite());
		suite.addTest(TestCTDoseAcquisition.suite());
		suite.addTest(TestCTDose.suite());
		suite.addTest(TestCTScanType.suite());
		suite.addTest(TestScanRange.suite());
		suite.addTest(TestCTPhantomType.suite());
		suite.addTest(TestCommonDoseObserverContext.suite());
		suite.addTest(TestPersonParticipant.suite());
		suite.addTest(TestDeviceParticipant.suite());
		suite.addTest(TestRoleInOrganization.suite());
		suite.addTest(TestRoleInProcedure.suite());
		suite.addTest(TestRecordingDeviceObserverContext.suite());
		suite.addTest(TestCTAcquisitionParameters.suite());
		suite.addTest(TestSourceOfDoseInformation.suite());
		return suite;
	}
	
}
