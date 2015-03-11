/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSafePrivatePhilipsDoseRelated extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSafePrivatePhilipsDoseRelated(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSafePrivatePhilipsDoseRelated.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSafePrivatePhilipsDoseRelated");
		
		suite.addTest(new TestSafePrivatePhilipsDoseRelated("TestSafePrivatePhilipsDoseRelated_FromTag"));
		suite.addTest(new TestSafePrivatePhilipsDoseRelated("TestSafePrivatePhilipsDoseRelated_FromList"));
		suite.addTest(new TestSafePrivatePhilipsDoseRelated("TestSafePrivatePhilipsDoseRelated_FromFile"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSafePrivatePhilipsDoseRelated_FromTag() throws Exception {
		assertTrue("Checking DLP is safe",					   ClinicalTrialsAttributes.isSafePrivateAttribute("ELSCINT1",  new AttributeTag(0x00E1,0x1021)));
		assertTrue("Checking Phantom Type is safe",			   ClinicalTrialsAttributes.isSafePrivateAttribute("ELSCINT1",  new AttributeTag(0x01E1,0x1026)));
		assertTrue("Checking Acquisition Duration is safe",    ClinicalTrialsAttributes.isSafePrivateAttribute("ELSCINT1",  new AttributeTag(0x01E1,0x1050)));
		assertTrue("Checking Acquisition Type is safe",        ClinicalTrialsAttributes.isSafePrivateAttribute("ELSCINT1",  new AttributeTag(0x01F1,0x1001)));
		assertTrue("Checking Table Velocity is safe",          ClinicalTrialsAttributes.isSafePrivateAttribute("ELSCINT1",  new AttributeTag(0x01F1,0x1007)));
		assertTrue("Checking Pitch is safe",                   ClinicalTrialsAttributes.isSafePrivateAttribute("ELSCINT1",  new AttributeTag(0x01F1,0x1026)));
		assertTrue("Checking Rotation Time is safe",           ClinicalTrialsAttributes.isSafePrivateAttribute("ELSCINT1",  new AttributeTag(0x01F1,0x1027)));
	}
	
	public void TestSafePrivatePhilipsDoseRelated_FromList() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x00E1,0x0010)); a.addValue("ELSCINT1");   list.put(a); }
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x01E1,0x0010)); a.addValue("ELSCINT1");   list.put(a); }
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x01F1,0x0010)); a.addValue("ELSCINT1");   list.put(a); }
		assertTrue("Checking DLP is safe",					   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x00E1,0x1021),list));
		assertTrue("Checking Phantom Type is safe",			   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x01E1,0x1026),list));
		assertTrue("Checking Acquisition Duration is safe",	   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x01E1,0x1050),list));
		assertTrue("Checking Acquisition Type is safe",	       ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x01F1,0x1001),list));
		assertTrue("Checking Table Velocity is safe",	       ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x01F1,0x1007),list));
		assertTrue("Checking Pitch is safe",				   ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x01F1,0x1026),list));
		assertTrue("Checking Rotation Time is safe",	       ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x01F1,0x1027),list));
	}
	
	public void TestSafePrivatePhilipsDoseRelated_FromFile() throws Exception {
		AttributeList list = new AttributeList();
		String testFilePath = System.getProperty("com.pixelmed.test.filepath");
//System.err.println("TestSafePrivatePhilipsDoseRelated.TestSafePrivatePhilipsDoseRelated_FromFile(): testFilePath = "+testFilePath);
		list.read(new java.io.File(testFilePath,"philips_brilliance64_sc_blinded.dcm"));
//System.err.print("TestSafePrivatePhilipsDoseRelated.TestSafePrivatePhilipsDoseRelated():list\n"+list);
		list.removeUnsafePrivateAttributes();
//System.err.print("TestSafePrivatePhilipsDoseRelated.TestSafePrivatePhilipsDoseRelated():list after removeUnsafePrivateAttributes()\n"+list);
		AttributeList eds0l = ((SequenceAttribute)(list.get(TagFromName.ExposureDoseSequence))).getItem(1).getAttributeList();
//System.err.print("TestSafePrivatePhilipsDoseRelated.TestSafePrivatePhilipsDoseRelated():\n"+eds0l);
		assertTrue("Checking Creator is not removed",				  eds0l.get(new AttributeTag(0x00E1,0x0010)) != null);
		assertTrue("Checking DLP is not removed",					  eds0l.get(new AttributeTag(0x00E1,0x1021)) != null);
		//assertTrue("Checking Creator is not removed",				  eds0l.get(new AttributeTag(0x01E1,0x0010)) != null);
		//assertTrue("Checking Phantom Type is not removed",		  eds0l.get(new AttributeTag(0x01E1,0x1026)) != null);
		//assertTrue("Checking Acquisition Duration is not removed",  eds0l.get(new AttributeTag(0x01E1,0x1050)) != null);
		//assertTrue("Checking Creator is not removed",				  eds0l.get(new AttributeTag(0x01F1,0x0010)) != null);
		//assertTrue("Checking Acquisition Type is not removed",      eds0l.get(new AttributeTag(0x01F1,0x1001)) != null);
		//assertTrue("Checking Table Velocity is not removed",        eds0l.get(new AttributeTag(0x01F1,0x1007)) != null);
		//assertTrue("Checking Pitch is not removed",				  eds0l.get(new AttributeTag(0x01F1,0x1026)) != null);
		//assertTrue("Checking Rotation Time is not removed",		  eds0l.get(new AttributeTag(0x01F1,0x1027)) != null);
	}
	
}
