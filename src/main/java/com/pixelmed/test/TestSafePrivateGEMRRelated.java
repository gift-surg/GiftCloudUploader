/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSafePrivateGEMRRelated extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSafePrivateGEMRRelated(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSafePrivateGEMRRelated.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSafePrivateGEMRRelated");
		
		suite.addTest(new TestSafePrivateGEMRRelated("TestSafePrivateGEMRRelated_FromTag"));
		suite.addTest(new TestSafePrivateGEMRRelated("TestSafePrivateGEMRRelated_CreatorFromList"));
		suite.addTest(new TestSafePrivateGEMRRelated("TestSafePrivateGEMRRelated_AddedToList"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSafePrivateGEMRRelated_FromTag() throws Exception {
		assertTrue("Internal Pulse Sequence Name",	               ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_ACQU_01",new AttributeTag(0x0019,0x109E)));
		assertTrue("Images In Series",	                           ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_SERS_01",new AttributeTag(0x0025,0x1007)));
		assertTrue("Scanner Table Entry + Gradient Coil Selected", ClinicalTrialsAttributes.isSafePrivateAttribute("GEMS_PARM_01",new AttributeTag(0x0043,0x106F)));
	}
	
	public void TestSafePrivateGEMRRelated_CreatorFromList() throws Exception {
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0019,0x0010)); a.addValue("GEMS_ACQU_01");   list.put(a); }
			assertTrue("Internal Pulse Sequence Name is safe", ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0019,0x109E),list));
		}
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0025,0x0010)); a.addValue("GEMS_SERS_01");   list.put(a); }
			assertTrue("Images In Series is safe", ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0025,0x1007),list));
		}
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0043,0x0010)); a.addValue("GEMS_PARM_01");   list.put(a); }
			assertTrue("Scanner Table Entry + Gradient Coil Selected", ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0043,0x106F),list));
		}
	}
	
	private void testIsNotRemovedAndRetainsValue(String description,AttributeTag creatorTag,String creator,Attribute privateAttribute,String expectValue) throws DicomException {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(creatorTag); a.addValue(creator);	list.put(a); }
		list.put(privateAttribute);
		AttributeTag privateTag = privateAttribute.getTag();
		list.removeUnsafePrivateAttributes();
		assertTrue("Checking Creator is not removed",						list.get(creatorTag) != null);
		assertEquals("Checking Creator retains value",creator,				Attribute.getDelimitedStringValuesOrEmptyString(list,creatorTag));
		assertTrue("Checking "+description+" is not removed",				list.get(privateTag) != null);
		assertEquals("Checking "+description+" retains value",expectValue,	Attribute.getDelimitedStringValuesOrEmptyString(list,privateTag));
	}
	
	public void TestSafePrivateGEMRRelated_AddedToList() throws Exception {
		{
			String value = "MySequence";
			Attribute a = new LongStringAttribute(new AttributeTag(0x0019,0x109E));
			a.addValue(value);
			testIsNotRemovedAndRetainsValue("Internal Pulse Sequence Name",					new AttributeTag(0x0019,0x0010),"GEMS_ACQU_01",a,value);
		}
		{
			String value = "35";
			Attribute a = new SignedLongAttribute(new AttributeTag(0x0025,0x1007));
			a.addValue(value);
			testIsNotRemovedAndRetainsValue("Images In Series",								new AttributeTag(0x0025,0x0010),"GEMS_SERS_01",a,value);
		}
		{
			String expectValue = "0\\0\\0\\2";
			Attribute a = new DecimalStringAttribute(new AttributeTag(0x0043,0x106F));
			a.addValue("0"); a.addValue("0"); a.addValue("0"); a.addValue("2");
			testIsNotRemovedAndRetainsValue("Scanner Table Entry + Gradient Coil Selected",	new AttributeTag(0x0043,0x0010),"GEMS_PARM_01",a,expectValue);
		}
	}
	
}
