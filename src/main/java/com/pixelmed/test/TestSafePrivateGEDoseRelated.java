/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSafePrivateGEDoseRelated extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSafePrivateGEDoseRelated(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSafePrivateGEDoseRelated.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSafePrivateGEDoseRelated");
		
		suite.addTest(new TestSafePrivateGEDoseRelated("TestSafePrivateGEDoseRelated_FromTag"));
		suite.addTest(new TestSafePrivateGEDoseRelated("TestSafePrivateGEDoseRelated_CreatorFromList"));
		suite.addTest(new TestSafePrivateGEDoseRelated("TestSafePrivateGEDoseRelated_AddedToList"));
		suite.addTest(new TestSafePrivateGEDoseRelated("TestSafePrivateGEDoseRelated_FromFile"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSafePrivateGEDoseRelated_FromTag() throws Exception {
		assertTrue("Checking Table Speed is safe",					   ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_ACQU_01",  new AttributeTag(0x0019,0x1023)));
		assertTrue("Checking Rotation Speed is safe",				   ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_ACQU_01",  new AttributeTag(0x0019,0x1027)));
		assertTrue("Checking Scan Pitch Ratio is safe",				   ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_PARM_01",  new AttributeTag(0x0043,0x1027)));
		assertTrue("Checking Number of Macro Rows in Detector is safe",ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_HELIOS_01",new AttributeTag(0x0045,0x1001)));
		assertTrue("Checking Macro width at ISO Center is safe",	   ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_HELIOS_01",new AttributeTag(0x0045,0x1002)));
	}
	
	public void TestSafePrivateGEDoseRelated_CreatorFromList() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x0019,0x0010)); a.addValue("GEMS_ACQU_01");   list.put(a); }
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x0043,0x0010)); a.addValue("GEMS_PARM_01");   list.put(a); }
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x0045,0x0010)); a.addValue("GEMS_HELIOS_01"); list.put(a); }
		assertTrue("Checking Table Speed is safe",					   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0019,0x1023),list));
		assertTrue("Checking Rotation Speed is safe",				   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0019,0x1027),list));
		assertTrue("Checking Scan Pitch Ratio is safe",				   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0043,0x1027),list));
		assertTrue("Checking Number of Macro Rows in Detector is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0045,0x1001),list));
		assertTrue("Checking Macro width at ISO Center is safe",	   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0045,0x1002),list));
	}
	
	public void TestSafePrivateGEDoseRelated_AddedToList() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute   (new AttributeTag(0x0019,0x0010)); a.addValue("GEMS_ACQU_01");   list.put(a); }
		{ Attribute a = new DecimalStringAttribute(new AttributeTag(0x0019,0x1023)); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(new AttributeTag(0x0019,0x1027)); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(new AttributeTag(0x0019,0x1023)); list.put(a); }
		{ Attribute a = new LongStringAttribute   (new AttributeTag(0x0043,0x0010)); a.addValue("GEMS_PARM_01");   list.put(a); }
		{ Attribute a = new ShortStringAttribute  (new AttributeTag(0x0043,0x1027)); list.put(a); }
		{ Attribute a = new LongStringAttribute   (new AttributeTag(0x0045,0x0010)); a.addValue("GEMS_HELIOS_01"); list.put(a); }
		{ Attribute a = new SignedShortAttribute  (new AttributeTag(0x0045,0x1001)); list.put(a); }
		{ Attribute a = new FloatSingleAttribute  (new AttributeTag(0x0045,0x1002)); list.put(a); }
		list.removeUnsafePrivateAttributes();
		assertTrue("Checking Creator is not removed",							list.get(new AttributeTag(0x0019,0x0010)) != null);
		assertTrue("Checking Table Speed is not removed",						ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0019,0x1023),list));
		assertTrue("Checking Rotation Speed is not removed",					ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0019,0x1027),list));
		assertTrue("Checking Creator is not removed",							list.get(new AttributeTag(0x0043,0x0010)) != null);
		assertTrue("Checking Scan Pitch Ratio is not removed",					ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0043,0x1027),list));
		assertTrue("Checking Creator is not removed",							list.get(new AttributeTag(0x0045,0x0010)) != null);
		assertTrue("Checking Number of Macro Rows in Detector is not removed",	ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0045,0x1001),list));
		assertTrue("Checking Macro width at ISO Center is not removed",			ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0045,0x1002),list));
	}
	
	public void TestSafePrivateGEDoseRelated_FromFile() throws Exception {
		AttributeList list = new AttributeList();
		String testFilePath = System.getProperty("com.pixelmed.test.filepath");
//System.err.println("TestSafePrivateGEDoseRelated.TestSafePrivateGEDoseRelated_FromFile(): testFilePath = "+testFilePath);
		list.read(new java.io.File(testFilePath,"gectwithsomeacquisitionparameters.dcm"));
//System.err.print("TestSafePrivateGEDoseRelated.TestSafePrivateGEDoseRelated():\n"+list);
		list.removeUnsafePrivateAttributes();
		assertTrue("Checking Creator is not removed",						  list.get(new AttributeTag(0x0019,0x0010)) != null);
		assertTrue("Checking Table Speed is not removed",					  list.get(new AttributeTag(0x0019,0x1023)) != null);
		assertTrue("Checking Rotation Speed is not removed",				  list.get(new AttributeTag(0x0019,0x1027)) != null);
		assertTrue("Checking Creator is not removed",						  list.get(new AttributeTag(0x0043,0x0010)) != null);
		assertTrue("Checking Scan Pitch Ratio is not removed",				  list.get(new AttributeTag(0x0043,0x1027)) != null);
		//assertTrue("Checking Creator is not removed",						  list.get(new AttributeTag(0x0045,0x0010)) != null);
		//assertTrue("Checking Number of Macro Rows in Detector is not removed",list.get(new AttributeTag(0x0045,0x1001)) != null);
		//assertTrue("Checking Macro width at ISO Center is not removed",		  list.get(new AttributeTag(0x0045,0x1002)) != null);
	}
	
}
