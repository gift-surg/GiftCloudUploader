/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

import java.util.Locale;

public class TestScanRange extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestScanRange(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestScanRange.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestScanRange");
		
		suite.addTest(new TestScanRange("testScanRangeConstructor_WithAllParameters"));
		suite.addTest(new TestScanRange("testScanRangeConstructor_Equality"));
		suite.addTest(new TestScanRange("testScanRangeConstructor_Inequality"));
		
		suite.addTest(new TestScanRange("testScanRange_Range_ZeroI"));
		suite.addTest(new TestScanRange("testScanRange_Range_ZeroS"));
		suite.addTest(new TestScanRange("testScanRange_Range_IToS"));
		suite.addTest(new TestScanRange("testScanRange_Range_SToI"));
		suite.addTest(new TestScanRange("testScanRange_Range_SToI_NotPooled"));
		
		suite.addTest(new TestScanRange("testScanRangeConstructor_SignedNegativeConstructor"));
		suite.addTest(new TestScanRange("testScanRangeConstructor_SignedPositiveConstructor"));
		suite.addTest(new TestScanRange("testScanRangeConstructor_SignedPostiveAndNegativeConstructor"));

		return suite;
	}
	
	protected void setUp() {
		Locale.setDefault(Locale.FRENCH);	// forces check that "," is not being used as decimal point in any double to string conversions
	}
	
	protected void tearDown() {
	}
	
	public void testScanRangeConstructor_SignedNegativeConstructor() {
		
		String startDirection = "I";
		String startLocation = "12.750";
		String signedStartLocation = "-"+startLocation;

		String endDirection = "I";
		String endLocation = "602.750";
		String signedEndLocation = "-"+endLocation;
		
		String expectToString = "I12.750-I602.750";
		
		ScanRange scanRange = new ScanRange(signedStartLocation,signedEndLocation);
		
		assertEquals("Checking startDirection",startDirection,scanRange.getStartDirection());
		assertEquals("Checking startLocation",startLocation,scanRange.getStartLocation());
		assertEquals("Checking endDirection",endDirection,scanRange.getEndDirection());
		assertEquals("Checking endLocation",endLocation,scanRange.getEndLocation());
		
		assertEquals("Checking toString",expectToString,scanRange.toString());
	}
	
	public void testScanRangeConstructor_SignedPositiveConstructor() {
		
		String startDirection = "S";
		String startLocation = "12.750";
		String signedStartLocation = "+"+startLocation;

		String endDirection = "S";
		String endLocation = "602.750";
		String signedEndLocation = "+"+endLocation;
		
		String expectToString = "S12.750-S602.750";
		
		ScanRange scanRange = new ScanRange(signedStartLocation,signedEndLocation);
		
		assertEquals("Checking startDirection",startDirection,scanRange.getStartDirection());
		assertEquals("Checking startLocation",startLocation,scanRange.getStartLocation());
		assertEquals("Checking endDirection",endDirection,scanRange.getEndDirection());
		assertEquals("Checking endLocation",endLocation,scanRange.getEndLocation());
		
		assertEquals("Checking toString",expectToString,scanRange.toString());
	}
	
	public void testScanRangeConstructor_SignedPostiveAndNegativeConstructor() {
		
		String startDirection = "I";
		String startLocation = "12.750";
		String signedStartLocation = "-"+startLocation;

		String endDirection = "S";
		String endLocation = "602.750";
		String signedEndLocation = "+"+endLocation;
		
		String expectToString = "I12.750-S602.750";
		
		ScanRange scanRange = new ScanRange(signedStartLocation,signedEndLocation);
		
		assertEquals("Checking startDirection",startDirection,scanRange.getStartDirection());
		assertEquals("Checking startLocation",startLocation,scanRange.getStartLocation());
		assertEquals("Checking endDirection",endDirection,scanRange.getEndDirection());
		assertEquals("Checking endLocation",endLocation,scanRange.getEndLocation());
		
		assertEquals("Checking toString",expectToString,scanRange.toString());
	}
	
	public void testScanRangeConstructor_WithAllParameters() {
		
		String startDirection = "I";
		String startLocation = "12.750";
		String endDirection = "I";
		String endLocation = "602.750";
		
		String expectToString = "I12.750-I602.750";
		
		ScanRange scanRange = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		
		assertEquals("Checking startDirection",startDirection,scanRange.getStartDirection());
		assertEquals("Checking startLocation",startLocation,scanRange.getStartLocation());
		assertEquals("Checking endDirection",endDirection,scanRange.getEndDirection());
		assertEquals("Checking endLocation",endLocation,scanRange.getEndLocation());
		
		assertEquals("Checking toString",expectToString,scanRange.toString());
	}
	
	public void testScanRangeConstructor_Equality() {
		
		String startDirection = "I";
		String startLocation = "12.750";
		String endDirection = "I";
		String endLocation = "602.750";
		
		ScanRange scanRange1 = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		ScanRange scanRange2 = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		
		assertTrue("Checking equality",scanRange1.equals(scanRange2));
		assertTrue("Checking hashCode",scanRange1.hashCode() == scanRange2.hashCode());
	}
	
	public void testScanRangeConstructor_Inequality() {
		
		String startDirection1 = "I";
		String startLocation1 = "12.750";
		String endDirection1 = "I";
		String endLocation1 = "602.750";
		
		String startDirection2 = "S";
		String startLocation2 = "13.750";
		String endDirection2 = "S";
		String endLocation2 = "603.750";
		
		ScanRange scanRange = new ScanRange(startDirection1,startLocation1,endDirection1,endLocation1);
		
		assertTrue("Checking inequality endLocation",   !scanRange.equals(new ScanRange(startDirection1,startLocation1,endDirection1,endLocation2)));
		assertTrue("Checking inequality endDirection",  !scanRange.equals(new ScanRange(startDirection1,startLocation1,endDirection2,endLocation1)));
		assertTrue("Checking inequality startLocation", !scanRange.equals(new ScanRange(startDirection1,startLocation2,endDirection1,endLocation1)));
		assertTrue("Checking inequality startDirection",!scanRange.equals(new ScanRange(startDirection2,startLocation1,endDirection1,endLocation1)));
	}
	
	public void testScanRange_Range_ZeroI() {
		
		String startDirection = "I";
		String startLocation = "603.750";
		String endDirection = "I";
		String endLocation = "603.750";
		String expectAbsoluteRange = "0.000";
		
		ScanRange scanRange = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		
		assertEquals("Checking getRange Zero I",expectAbsoluteRange,scanRange.getAbsoluteRange());
	}
	
	public void testScanRange_Range_ZeroS() {
		
		String startDirection = "S";
		String startLocation = "603.750";
		String endDirection = "S";
		String endLocation = "603.750";
		String expectAbsoluteRange = "0.000";
		
		ScanRange scanRange = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		
		assertEquals("Checking getRange Zero S",expectAbsoluteRange,scanRange.getAbsoluteRange());
	}
	
	public void testScanRange_Range_IToS() {
		
		String startDirection = "I";
		String startLocation = "12.750";
		String endDirection = "S";
		String endLocation = "603.750";
		String expectAbsoluteRange = "616.500";
		
		ScanRange scanRange = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		
		assertEquals("Checking getRange I to S",expectAbsoluteRange,scanRange.getAbsoluteRange());
	}
	
	public void testScanRange_Range_SToI() {
		
		String startDirection = "S";
		String startLocation = "12.750";
		String endDirection = "I";
		String endLocation = "603.750";
		String expectAbsoluteRange = "616.500";
		
		ScanRange scanRange = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		
		assertEquals("Checking getRange S to I",expectAbsoluteRange,scanRange.getAbsoluteRange());
	}
	
	public void testScanRange_Range_SToI_NotPooled() {
		
		String startDirection = new String("S");
		String startLocation = "188.000";
		String endDirection = new String("I");
		String endLocation = "105.000";
		String expectAbsoluteRange = "293.000";
		
		ScanRange scanRange = new ScanRange(startDirection,startLocation,endDirection,endLocation);
		
		assertEquals("Checking getRange S to I not using String literal pool",expectAbsoluteRange,scanRange.getAbsoluteRange());
	}
}
