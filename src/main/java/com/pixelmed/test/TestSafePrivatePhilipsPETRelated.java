/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSafePrivatePhilipsPETRelated extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSafePrivatePhilipsPETRelated(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSafePrivatePhilipsPETRelated.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSafePrivatePhilipsPETRelated");
		
		suite.addTest(new TestSafePrivatePhilipsPETRelated("TestSafePrivatePhilipsPETRelated_ScaleFactors_FromTag"));
		suite.addTest(new TestSafePrivatePhilipsPETRelated("TestSafePrivatePhilipsPETRelated_ScaleFactors_FromList"));
		suite.addTest(new TestSafePrivatePhilipsPETRelated("TestSafePrivatePhilipsPETRelated_ScaleFactors_FromFile"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSafePrivatePhilipsPETRelated_ScaleFactors_FromTag() throws Exception {
		assertTrue("Checking SUV Factor is safe",ClinicalTrialsAttributes.isSafePrivateAttribute("Philips PET Private Group",new AttributeTag(0x7053,0x1000)));
		assertTrue("Checking Activity Concentration Factor is safe",ClinicalTrialsAttributes.isSafePrivateAttribute("Philips PET Private Group",new AttributeTag(0x7053,0x1009)));
	}
	
	public void TestSafePrivatePhilipsPETRelated_ScaleFactors_FromList() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x7053,0x0010)); a.addValue("Philips PET Private Group"); list.put(a); }
		assertTrue("Checking SUV Factor is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x7053,0x1000),list));
		assertTrue("Checking Activity Concentration Factor is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x7053,0x1009),list));
	}
	
	public void TestSafePrivatePhilipsPETRelated_ScaleFactors_FromFile() throws Exception {
		AttributeList list = new AttributeList();
		String testFilePath = System.getProperty("com.pixelmed.test.filepath");
//System.err.println("TestSafePrivatePhilipsPETRelated.TestSafePrivatePhilipsPETRelated_ScaleFactors_FromFile(): testFilePath = "+testFilePath);
		list.read(new java.io.File(testFilePath,"philipssuvandactivityscalefactors.dcm"));
//System.err.print("TestSafePrivatePhilipsPETRelated.TestSafePrivatePhilipsPETRelated_ScaleFactors():\n"+list);
		list.removeUnsafePrivateAttributes();
		assertTrue("Checking Creator is not removed",	list.get(new AttributeTag(0x7053,0x0010)) != null);
		assertTrue("Checking SUV Factor is not removed",list.get(new AttributeTag(0x7053,0x1000)) != null);
		assertTrue("Checking Activity Concentration Factor is not removed",list.get(new AttributeTag(0x7053,0x1009)) != null);
	}
	
}
