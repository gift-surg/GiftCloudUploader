/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.database.*;

import com.pixelmed.dicom.*;

import java.util.ArrayList;
import java.util.TreeMap;

import junit.framework.*;

public class TestDatabaseDateTimeConversionInsertion extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDatabaseDateTimeConversionInsertion(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAgeCalculation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDatabaseDateTimeConversionInsertion");
		
		suite.addTest(new TestDatabaseDateTimeConversionInsertion("TestDatabaseDateTimeConversionInsertion_ValidDateOnly"));
		suite.addTest(new TestDatabaseDateTimeConversionInsertion("TestDatabaseDateTimeConversionInsertion_ValidDateTime"));
		suite.addTest(new TestDatabaseDateTimeConversionInsertion("TestDatabaseDateTimeConversionInsertion_InvalidDateValidTime"));
		
		return suite;
	}
	
	String studyID;
	String seriesNumber;
	String instanceNumber;
	
	UIDGenerator u;

	protected void setUp() {
		studyID = "612386812";
		seriesNumber = "12";
		instanceNumber = "38";
		
		u = new UIDGenerator("9999");
	}
	
	protected void tearDown() {
	}
	
	public void TestDatabaseDateTimeConversionInsertion_ValidDateOnly() throws Exception {
		
		DatabaseInformationModel d = new StudySeriesInstanceModel("mem:test1");
		
		String contentDate = "20121230";
		String expectedDerivedContentDateTime = "2012-12-30 00:00:00.000000";
		
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		
		AttributeList list = new AttributeList();
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		
		d.insertObject(list,"Dummy"/*fileName*/,DatabaseInformationModel.FILE_REFERENCED);
		
		ArrayList<TreeMap<String,String>> results = d.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedUID(InformationEntity.INSTANCE,sopInstanceUID);
		
		assertEquals("Checking SOPInstanceUID",sopInstanceUID,results.get(0).get("SOPINSTANCEUID"));
		assertEquals("Checking ContentDate",contentDate,results.get(0).get("CONTENTDATE"));
		assertEquals("Checking derivedContentDateTime",expectedDerivedContentDateTime,results.get(0).get("PM_CONTENTDATETIME"));
	}
	
	public void TestDatabaseDateTimeConversionInsertion_ValidDateTime() throws Exception {
		
		DatabaseInformationModel d = new StudySeriesInstanceModel("mem:test2");
		
		String contentDate = "20121230";
		String contentTime = "143752.46";
		String expectedDerivedContentDateTime = "2012-12-30 14:37:52.460000";
		
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		
		AttributeList list = new AttributeList();
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		d.insertObject(list,"Dummy"/*fileName*/,DatabaseInformationModel.FILE_REFERENCED);
		
		ArrayList<TreeMap<String,String>> results = d.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedUID(InformationEntity.INSTANCE,sopInstanceUID);
		
		assertEquals("Checking SOPInstanceUID",sopInstanceUID,results.get(0).get("SOPINSTANCEUID"));
		assertEquals("Checking ContentDate",contentDate,results.get(0).get("CONTENTDATE"));
		assertEquals("Checking ContentTime",contentTime,results.get(0).get("CONTENTTIME"));
		assertEquals("Checking derivedContentDateTime",expectedDerivedContentDateTime,results.get(0).get("PM_CONTENTDATETIME"));
	}
	
	public void TestDatabaseDateTimeConversionInsertion_InvalidDateValidTime() throws Exception {
		
		DatabaseInformationModel d = new StudySeriesInstanceModel("mem:test3");
		
		String contentDate = "99999999";		// Test for fix to (000679)
		String contentTime = "143752.46";
		String expectedDerivedContentDateTime = null;
		
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		
		AttributeList list = new AttributeList();
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		d.insertObject(list,"Dummy"/*fileName*/,DatabaseInformationModel.FILE_REFERENCED);
		
		ArrayList<TreeMap<String,String>> results = d.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedUID(InformationEntity.INSTANCE,sopInstanceUID);
		
		assertEquals("Checking SOPInstanceUID",sopInstanceUID,results.get(0).get("SOPINSTANCEUID"));
		assertEquals("Checking ContentDate",contentDate,results.get(0).get("CONTENTDATE"));
		assertEquals("Checking ContentTime",contentTime,results.get(0).get("CONTENTTIME"));
		assertEquals("Checking derivedContentDateTime",expectedDerivedContentDateTime,results.get(0).get("PM_CONTENTDATETIME"));
		
	}
	
}
