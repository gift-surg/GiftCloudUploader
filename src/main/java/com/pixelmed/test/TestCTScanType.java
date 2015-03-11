/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dose.*;

import junit.framework.*;

public class TestCTScanType extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCTScanType(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTScanType.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCTScanType");
		
		suite.addTest(new TestCTScanType("TestCTScanType_Localizer_Description"));
		suite.addTest(new TestCTScanType("TestCTScanType_Localizer_Equality"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_Helical_Description"));
		suite.addTest(new TestCTScanType("TestCTScanType_Helical_Equality"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_Axial_Description"));
		suite.addTest(new TestCTScanType("TestCTScanType_Axial_Equality"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_Stationary_Description"));
		suite.addTest(new TestCTScanType("TestCTScanType_Stationary_Equality"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_Free_Description"));
		suite.addTest(new TestCTScanType("TestCTScanType_Free_Equality"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_Unknown_Description"));
		suite.addTest(new TestCTScanType("TestCTScanType_Unknown_Equality"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_Localizer_Helical_Inequality"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_SelectFromDescription"));
		
		suite.addTest(new TestCTScanType("TestCTScanType_SelectFromCode"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCTScanType_Localizer_Description() {
		
		assertEquals("Checking LOCALIZER description",CTScanType.LOCALIZER.toString(),"Localizer");
	}
	
	public void TestCTScanType_Helical_Description() {
		
		assertEquals("Checking HELICAL description",CTScanType.HELICAL.toString(),"Helical");
	}
	
	public void TestCTScanType_Axial_Description() {
		
		assertEquals("Checking AXIAL description",CTScanType.AXIAL.toString(),"Axial");
	}
	
	public void TestCTScanType_Stationary_Description() {
		
		assertEquals("Checking STATIONARY description",CTScanType.STATIONARY.toString(),"Stationary");
	}
	
	public void TestCTScanType_Free_Description() {
		
		assertEquals("Checking FREE description",CTScanType.FREE.toString(),"Free");
	}
	
	public void TestCTScanType_Unknown_Description() {
		
		assertEquals("Checking UNKNOWN description",CTScanType.UNKNOWN.toString(),"Unknown");
	}
	
	public void TestCTScanType_Localizer_Equality() throws Exception {
		
		assertEquals("Checking LOCALIZER equality",CTScanType.LOCALIZER,CTScanType.LOCALIZER);
		assertEquals("Checking LOCALIZER content item hashCode equality",CTScanType.getCodedSequenceItem(CTScanType.LOCALIZER).hashCode(),CTScanType.getCodedSequenceItem(CTScanType.LOCALIZER).hashCode());
		assertEquals("Checking LOCALIZER content item equality",CTScanType.getCodedSequenceItem(CTScanType.LOCALIZER),CTScanType.getCodedSequenceItem(CTScanType.LOCALIZER));
	}
	
	public void TestCTScanType_Helical_Equality() throws Exception {
		
		assertEquals("Checking HELICAL equality",CTScanType.HELICAL,CTScanType.HELICAL);
		assertEquals("Checking HELICAL content item hashCode equality",CTScanType.getCodedSequenceItem(CTScanType.HELICAL).hashCode(),CTScanType.getCodedSequenceItem(CTScanType.HELICAL).hashCode());
		assertEquals("Checking HELICAL content item equality",CTScanType.getCodedSequenceItem(CTScanType.HELICAL),CTScanType.getCodedSequenceItem(CTScanType.HELICAL));
	}
	
	public void TestCTScanType_Axial_Equality() throws Exception {
		
		assertEquals("Checking AXIAL equality",CTScanType.AXIAL,CTScanType.AXIAL);
		assertEquals("Checking AXIAL content item hashCode equality",CTScanType.getCodedSequenceItem(CTScanType.AXIAL).hashCode(),CTScanType.getCodedSequenceItem(CTScanType.AXIAL).hashCode());
		assertEquals("Checking AXIAL content item equality",CTScanType.getCodedSequenceItem(CTScanType.AXIAL),CTScanType.getCodedSequenceItem(CTScanType.AXIAL));
	}
	
	public void TestCTScanType_Stationary_Equality() throws Exception {
		
		assertEquals("Checking STATIONARY equality",CTScanType.STATIONARY,CTScanType.STATIONARY);
		assertEquals("Checking STATIONARY content item hashCode equality",CTScanType.getCodedSequenceItem(CTScanType.STATIONARY).hashCode(),CTScanType.getCodedSequenceItem(CTScanType.STATIONARY).hashCode());
		assertEquals("Checking STATIONARY content item equality",CTScanType.getCodedSequenceItem(CTScanType.STATIONARY),CTScanType.getCodedSequenceItem(CTScanType.STATIONARY));
	}
	
	public void TestCTScanType_Free_Equality() throws Exception {
		
		assertEquals("Checking FREE equality",CTScanType.FREE,CTScanType.FREE);
		assertEquals("Checking FREE content item hashCode equality",CTScanType.getCodedSequenceItem(CTScanType.FREE).hashCode(),CTScanType.getCodedSequenceItem(CTScanType.FREE).hashCode());
		assertEquals("Checking FREE content item equality",CTScanType.getCodedSequenceItem(CTScanType.FREE),CTScanType.getCodedSequenceItem(CTScanType.FREE));
	}
	
	public void TestCTScanType_Unknown_Equality() throws Exception {
		
		assertEquals("Checking UNKNOWN equality",CTScanType.UNKNOWN,CTScanType.UNKNOWN);
		// can't check hashcode since should return null
		assertTrue("Checking UNKNOWN content item returns null",CTScanType.getCodedSequenceItem(CTScanType.UNKNOWN) == null);
		assertEquals("Checking UNKNOWN content item equality",CTScanType.getCodedSequenceItem(CTScanType.UNKNOWN),CTScanType.getCodedSequenceItem(CTScanType.UNKNOWN));
	}
	
	public void TestCTScanType_Localizer_Helical_Inequality() throws Exception {
		
		assertTrue("Checking LOCALIZER versus HELICAL inequality",!CTScanType.LOCALIZER.equals(CTScanType.HELICAL));
		assertFalse("Checking LOCALIZER versus HELICAL content item hashCode inequality",CTScanType.getCodedSequenceItem(CTScanType.LOCALIZER).hashCode() == CTScanType.getCodedSequenceItem(CTScanType.HELICAL).hashCode());
		assertFalse("Checking LOCALIZER versus HELICAL content item inequality",CTScanType.getCodedSequenceItem(CTScanType.LOCALIZER).equals(CTScanType.getCodedSequenceItem(CTScanType.HELICAL)));
	}
	
	public void TestCTScanType_SelectFromDescription() throws Exception {
		
		assertTrue("Checking select LOCALIZER",CTScanType.selectFromDescription("Localizer").equals(CTScanType.LOCALIZER));
		assertTrue("Checking select LOCALIZER with Scout",CTScanType.selectFromDescription("Scout").equals(CTScanType.LOCALIZER));
		assertTrue("Checking select LOCALIZER with CONSTANT_ANGLE",CTScanType.selectFromDescription("CONSTANT_ANGLE").equals(CTScanType.LOCALIZER));
		assertTrue("Checking select LOCALIZER with SCANOSCOPE",CTScanType.selectFromDescription("SCANOSCOPE").equals(CTScanType.LOCALIZER));
		assertTrue("Checking select AXIAL",CTScanType.selectFromDescription("Axial").equals(CTScanType.AXIAL));
		assertTrue("Checking select AXIAL with Sequenced",CTScanType.selectFromDescription("Sequenced").equals(CTScanType.AXIAL));
		assertTrue("Checking select AXIAL with Normal",CTScanType.selectFromDescription("Normal").equals(CTScanType.AXIAL));
		assertTrue("Checking select STATIONARY",CTScanType.selectFromDescription("Stationary").equals(CTScanType.STATIONARY));
		assertTrue("Checking select STATIONARY with Cine",CTScanType.selectFromDescription("Cine").equals(CTScanType.STATIONARY));
		assertTrue("Checking select STATIONARY with Dynamic",CTScanType.selectFromDescription("Dynamic").equals(CTScanType.STATIONARY));
		assertTrue("Checking select FREE",CTScanType.selectFromDescription("Free").equals(CTScanType.FREE));
		assertTrue("Checking select FREE",CTScanType.selectFromDescription("SmartView").equals(CTScanType.FREE));
		assertTrue("Checking select HELICAL",CTScanType.selectFromDescription("Helical").equals(CTScanType.HELICAL));
		assertTrue("Checking select HELICAL with Spiral",CTScanType.selectFromDescription("Spiral").equals(CTScanType.HELICAL));
		assertTrue("Checking select UNKNOWN",CTScanType.selectFromDescription("").equals(CTScanType.UNKNOWN));
	}
	
	public void TestCTScanType_SelectFromCode() throws Exception {
		
		assertTrue("Checking select LOCALIZER",CTScanType.selectFromCode(new CodedSequenceItem("113805","DCM","Constant Angle Acquisition")).equals(CTScanType.LOCALIZER));
		assertTrue("Checking select AXIAL",CTScanType.selectFromCode(new CodedSequenceItem("113804","DCM","Sequenced Acquisition")).equals(CTScanType.AXIAL));
		assertTrue("Checking select STATIONARY",CTScanType.selectFromCode(new CodedSequenceItem("113806","DCM","Stationary Acquisition")).equals(CTScanType.STATIONARY));
		assertTrue("Checking select FREE",CTScanType.selectFromCode(new CodedSequenceItem("113807","DCM","Free Acquisition")).equals(CTScanType.FREE));
		assertTrue("Checking select HELICAL",CTScanType.selectFromCode(new CodedSequenceItem("P5-08001","SRT","Spiral Acquisition")).equals(CTScanType.HELICAL));
	}
	
	
}
