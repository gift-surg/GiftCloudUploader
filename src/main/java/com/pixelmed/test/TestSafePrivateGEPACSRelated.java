/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSafePrivateGEPACSRelated extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSafePrivateGEPACSRelated(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSafePrivateGEPACSRelated.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSafePrivateGEPACSRelated");
		
		suite.addTest(new TestSafePrivateGEPACSRelated("TestSafePrivateGEPACSRelated_FromTag"));
		suite.addTest(new TestSafePrivateGEPACSRelated("TestSafePrivateGEPACSRelated_CreatorFromList"));
		suite.addTest(new TestSafePrivateGEPACSRelated("TestSafePrivateGEPACSRelated_AddedToList"));
		suite.addTest(new TestSafePrivateGEPACSRelated("TestSafePrivateGEPACSRelated_FromFile"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSafePrivateGEPACSRelated_FromTag() throws Exception {
		assertTrue("Checking Reject Image Flag is safe",	ClinicalTrialsAttributes.isSafePrivateAttribute("GEIIS PACS",  new AttributeTag(0x0903,0x1010)));
	}
	
	public void TestSafePrivateGEPACSRelated_CreatorFromList() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x0903,0x0010)); a.addValue("GEIIS PACS");   list.put(a); }
		assertTrue("Checking Reject Image Flag is safe",		ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0903,0x1010),list));
	}
	
	public void TestSafePrivateGEPACSRelated_AddedToList() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x0903,0x0010)); a.addValue("GEIIS PACS");   list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(new AttributeTag(0x0903,0x1010)); a.addValue(0);   list.put(a); }
		list.removeUnsafePrivateAttributes();
		assertTrue("Checking Creator is not removed",			list.get(new AttributeTag(0x0903,0x0010)) != null);
		assertTrue("Checking Reject Image Flag is not removed",	ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0903,0x1010),list));
	}
	
	public void TestSafePrivateGEPACSRelated_FromFile() throws Exception {
		AttributeList list = new AttributeList();
		String testFilePath = System.getProperty("com.pixelmed.test.filepath");
//System.err.println("TestSafePrivateGEPACSRelated.TestSafePrivateGEPACSRelated_FromFile(): testFilePath = "+testFilePath);
		list.read(new java.io.File(testFilePath,"philipsctwithgepacsprivateattributes.dcm"));
//System.err.print("TestSafePrivateGEPACSRelated.TestSafePrivateGEPACSRelated():\n"+list);
		list.removeUnsafePrivateAttributes();
		assertTrue("Checking Creator is not removed",			list.get(new AttributeTag(0x0903,0x0010)) != null);
		assertTrue("Checking Reject Image Flag is not removed",	list.get(new AttributeTag(0x0903,0x1010)) != null);
	}
	
}
