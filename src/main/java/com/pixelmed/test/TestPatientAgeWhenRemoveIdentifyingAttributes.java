/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestPatientAgeWhenRemoveIdentifyingAttributes extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestPatientAgeWhenRemoveIdentifyingAttributes(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestPatientAgeWhenRemoveIdentifyingAttributes.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestPatientAgeWhenRemoveIdentifyingAttributes");
		
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_DoNotKeepIdentifyingAttributes"));
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeAlreadyPresent"));
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeAbsent"));
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLength"));
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudyDateZeroLength"));
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudySeriesDateZeroLength"));
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudySeriesAcquisitionDateZeroLength"));
		suite.addTest(new TestPatientAgeWhenRemoveIdentifyingAttributes("TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudySeriesAcquisitionContentDateZeroLength"));
		
		return suite;
	}
	
	protected String ageEncoded = "015Y";
	protected String ageDerived = "050Y";
	protected String dob = "19610714";
	protected String currentDate = "20120625";
	
	protected AttributeList list;
	
	protected void setUp() throws DicomException {
		list = new AttributeList();
		{ Attribute a = new AgeStringAttribute(TagFromName.PatientAge); a.addValue(ageEncoded); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(dob); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(currentDate); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate); a.addValue(currentDate); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(currentDate); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(currentDate); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.InstanceCreationDate); a.addValue(currentDate); list.put(a); }	// is NOT used
	}
	
	protected void tearDown() {
	}
	
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_DoNotKeepIdentifyingAttributes() throws Exception {

		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,false/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertTrue("Checking PatientAge is zero length",a == null || a.getVL() == 0);
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeAlreadyPresent() throws Exception {

		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertEquals("Checking PatientAge",ageEncoded,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeAbsent() throws Exception {

		list.remove(TagFromName.PatientAge);
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertEquals("Checking PatientAge",ageDerived,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLength() throws Exception {

		list.put(new AgeStringAttribute(TagFromName.PatientAge));
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertEquals("Checking PatientAge",ageDerived,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudyDateZeroLength() throws Exception {

		list.put(new DateAttribute(TagFromName.StudyDate));
		list.put(new AgeStringAttribute(TagFromName.PatientAge));
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertEquals("Checking PatientAge",ageDerived,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudySeriesDateZeroLength() throws Exception {

		list.put(new DateAttribute(TagFromName.StudyDate));
		list.put(new DateAttribute(TagFromName.SeriesDate));
		list.put(new AgeStringAttribute(TagFromName.PatientAge));
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertEquals("Checking PatientAge",ageDerived,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudySeriesAcquisitionDateZeroLength() throws Exception {

		list.put(new DateAttribute(TagFromName.StudyDate));
		list.put(new DateAttribute(TagFromName.SeriesDate));
		list.put(new DateAttribute(TagFromName.AcquisitionDate));
		list.put(new AgeStringAttribute(TagFromName.PatientAge));
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertEquals("Checking PatientAge",ageDerived,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudySeriesAcquisitionContentDateZeroLength() throws Exception {

		list.put(new DateAttribute(TagFromName.StudyDate));
		list.put(new DateAttribute(TagFromName.SeriesDate));
		list.put(new DateAttribute(TagFromName.AcquisitionDate));
		list.put(new DateAttribute(TagFromName.ContentDate));
		list.put(new AgeStringAttribute(TagFromName.PatientAge));
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertTrue("Checking PatientAge is zero length",a == null || a.getVL() == 0);
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}

	
	public void TestPatientAgeWhenRemoveIdentifyingAttributes_KeepIdentifyingAttributesAgeZeroLengthAndStudySeriesAcquisitionDateAbsent() throws Exception {

		list.remove(TagFromName.StudyDate);
		list.remove(TagFromName.SeriesDate);
		list.remove(TagFromName.AcquisitionDate);
		list.put(new AgeStringAttribute(TagFromName.PatientAge));
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		{
			Attribute a = list.get(TagFromName.PatientAge);
			assertEquals("Checking PatientAge",ageDerived,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		}
		{
			Attribute a = list.get(TagFromName.PatientBirthDate);
			assertTrue("Checking PatientBirthDate is zero length",a == null || a.getVL() == 0);
		}
	}
}
