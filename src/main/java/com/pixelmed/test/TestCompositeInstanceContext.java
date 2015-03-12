/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestCompositeInstanceContext extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCompositeInstanceContext(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCompositeInstanceContext.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCompositeInstanceContext");
		
		suite.addTest(new TestCompositeInstanceContext("TestCompositeInstanceContext_Constructor"));
		suite.addTest(new TestCompositeInstanceContext("TestCompositeInstanceContext_RemoveAllButPatient"));
		suite.addTest(new TestCompositeInstanceContext("TestCompositeInstanceContext_RemoveAllButPatientAndStudy"));
		suite.addTest(new TestCompositeInstanceContext("TestCompositeInstanceContext_Equality"));
		suite.addTest(new TestCompositeInstanceContext("TestCompositeInstanceContext_SRSpecificRequestAndProcedure"));
		suite.addTest(new TestCompositeInstanceContext("TestCompositeInstanceContext_SRSpecificRequestAndProcedure_Default"));
		suite.addTest(new TestCompositeInstanceContext("TestCompositeInstanceContext_WithoutSRSpecificRequestAndProcedure"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCompositeInstanceContext_Constructor() throws Exception {
		
		String patientName = "Smith^Mary";
		String patientID = "3764913624";
		String patientBirthDate = "19600101";
		String patientSex = "F";
		String studyID = "612386812";
		String seriesNumber = "12";
		String instanceNumber = "38";
		String referringPhysicianName = "Jones^Harriet";
		
		String studyDate = "20100324";
		String studyTime = "142211";
				
		String contentDate = "20100325";
		String contentTime = "142238";
	
		UIDGenerator u = new UIDGenerator("9999");
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
		String studyInstanceUID = u.getNewStudyInstanceUID(studyID);
		
		String sopClassUID = SOPClass.ComputedRadiographyImageStorage;
		
		AttributeList list = new AttributeList();

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		CompositeInstanceContext cic = new CompositeInstanceContext(list,true/*forSR*/);
		AttributeList cicList = cic.getAttributeList();
		
		assertEquals("Checking SOPClassUID",sopClassUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPClassUID));
		assertEquals("Checking SOPInstanceUID",sopInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking SeriesInstanceUID",seriesInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking StudyInstanceUID",studyInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyInstanceUID));
		assertEquals("Checking PatientName",patientName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientName));
		assertEquals("Checking PatientID",patientID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientID));
		assertEquals("Checking PatientBirthDate",patientBirthDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientBirthDate));
		assertEquals("Checking PatientSex",patientSex,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientSex));
		assertEquals("Checking StudyID",studyID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyID));
		assertEquals("Checking ReferringPhysicianName",referringPhysicianName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ReferringPhysicianName));
		assertEquals("Checking AccessionNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.AccessionNumber));
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		assertEquals("Checking Manufacturer","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Manufacturer));
		assertEquals("Checking PatientOrientation","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientOrientation));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		assertEquals("Checking StudyDate",studyDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyDate));
		assertEquals("Checking StudyTime",studyTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyTime));
		assertEquals("Checking ContentDate",contentDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime",contentTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));
		
		cic.removeSeries();
		cicList = cic.getAttributeList();
		assertEquals("Checking SeriesInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking SeriesNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		{
			AttributeList newList = new AttributeList();
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); newList.put(a); }
			cic.putAll(newList);
		}
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		
		cic.removeInstance();
		assertEquals("Checking ContentDate","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));
		assertEquals("Checking SOPInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking InstanceNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); cic.put(a); }
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
	}
	
	public void TestCompositeInstanceContext_RemoveAllButPatient() throws Exception {
		
		String patientName = "Smith^Mary";
		String patientID = "3764913624";
		String patientBirthDate = "19600101";
		String patientSex = "F";
		String studyID = "612386812";
		String seriesNumber = "12";
		String instanceNumber = "38";
		String referringPhysicianName = "Jones^Harriet";
		
		String studyDate = "20100324";
		String studyTime = "142211";
			
		String contentDate = "20100325";
		String contentTime = "142238";
		
		String sopClassUID = SOPClass.ComputedRadiographyImageStorage;

		UIDGenerator u = new UIDGenerator("9999");
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
		String studyInstanceUID = u.getNewStudyInstanceUID(studyID);
		
		AttributeList list = new AttributeList();

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		CompositeInstanceContext cic = new CompositeInstanceContext(list,true/*forSR*/);
		
		cic.removeAllButPatient();
		AttributeList cicList = cic.getAttributeList();

		assertEquals("Checking PatientName",patientName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientName));
		assertEquals("Checking PatientID",patientID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientID));
		assertEquals("Checking PatientBirthDate",patientBirthDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientBirthDate));
		assertEquals("Checking PatientSex",patientSex,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientSex));
		
		assertTrue("Checking SOPClassUID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SOPClassUID) == null);
		assertTrue("Checking SOPInstanceUID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SOPInstanceUID) == null);
		assertTrue("Checking SeriesInstanceUID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SeriesInstanceUID) == null);
		assertTrue("Checking StudyInstanceUID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.StudyInstanceUID) == null);
		assertTrue("Checking StudyID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.StudyID) == null);
		assertTrue("Checking ReferringPhysicianName",Attribute.getSingleStringValueOrNull(cicList,TagFromName.ReferringPhysicianName) == null);
		assertTrue("Checking AccessionNumber",Attribute.getSingleStringValueOrNull(cicList,TagFromName.AccessionNumber) == null);
		assertTrue("Checking SeriesNumber",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SeriesNumber) == null);
		assertTrue("Checking InstanceNumber",Attribute.getSingleStringValueOrNull(cicList,TagFromName.InstanceNumber) == null);
		assertTrue("Checking Manufacturer",Attribute.getSingleStringValueOrNull(cicList,TagFromName.Manufacturer) == null);
		assertTrue("Checking PatientOrientation",Attribute.getSingleStringValueOrNull(cicList,TagFromName.PatientOrientation) == null);
		assertTrue("Checking Laterality",Attribute.getSingleStringValueOrNull(cicList,TagFromName.Laterality) == null);
		assertTrue("Checking StudyDate",Attribute.getSingleStringValueOrNull(cicList,TagFromName.StudyDate) == null);
		assertTrue("Checking StudyTime",Attribute.getSingleStringValueOrNull(cicList,TagFromName.StudyTime) == null);
		assertTrue("Checking ContentDate",Attribute.getSingleStringValueOrNull(cicList,TagFromName.ContentDate) == null);
		assertTrue("Checking ContentTime",Attribute.getSingleStringValueOrNull(cicList,TagFromName.ContentTime) == null);
	}
	
	public void TestCompositeInstanceContext_RemoveAllButPatientAndStudy() throws Exception {
		
		String patientName = "Smith^Mary";
		String patientID = "3764913624";
		String patientBirthDate = "19600101";
		String patientSex = "F";
		String studyID = "612386812";
		String seriesNumber = "12";
		String instanceNumber = "38";
		String referringPhysicianName = "Jones^Harriet";
		
		String studyDate = "20100324";
		String studyTime = "142211";
			
		String contentDate = "20100325";
		String contentTime = "142238";
	
		String sopClassUID = SOPClass.ComputedRadiographyImageStorage;

		UIDGenerator u = new UIDGenerator("9999");
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
		String studyInstanceUID = u.getNewStudyInstanceUID(studyID);
		
		AttributeList list = new AttributeList();

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		CompositeInstanceContext cic = new CompositeInstanceContext(list,true/*forSR*/);
		
		cic.removeAllButPatientAndStudy();
		AttributeList cicList = cic.getAttributeList();

		assertEquals("Checking PatientName",patientName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientName));
		assertEquals("Checking PatientID",patientID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientID));
		assertEquals("Checking PatientBirthDate",patientBirthDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientBirthDate));
		assertEquals("Checking PatientSex",patientSex,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientSex));

		assertEquals("Checking StudyInstanceUID",studyInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyInstanceUID));
		assertEquals("Checking StudyID",studyID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyID));
		assertEquals("Checking ReferringPhysicianName",referringPhysicianName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ReferringPhysicianName));
		assertEquals("Checking AccessionNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.AccessionNumber));
		assertEquals("Checking StudyDate",studyDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyDate));
		assertEquals("Checking StudyTime",studyTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyTime));
		
		assertTrue("Checking SOPClassUID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SOPClassUID) == null);
		assertTrue("Checking SOPInstanceUID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SOPInstanceUID) == null);
		assertTrue("Checking SeriesInstanceUID",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SeriesInstanceUID) == null);
		assertTrue("Checking SeriesNumber",Attribute.getSingleStringValueOrNull(cicList,TagFromName.SeriesNumber) == null);
		assertTrue("Checking InstanceNumber",Attribute.getSingleStringValueOrNull(cicList,TagFromName.InstanceNumber) == null);
		assertTrue("Checking Manufacturer",Attribute.getSingleStringValueOrNull(cicList,TagFromName.Manufacturer) == null);
		assertTrue("Checking PatientOrientation",Attribute.getSingleStringValueOrNull(cicList,TagFromName.PatientOrientation) == null);
		assertTrue("Checking Laterality",Attribute.getSingleStringValueOrNull(cicList,TagFromName.Laterality) == null);
		assertTrue("Checking ContentDate",Attribute.getSingleStringValueOrNull(cicList,TagFromName.ContentDate) == null);
		assertTrue("Checking ContentTime",Attribute.getSingleStringValueOrNull(cicList,TagFromName.ContentTime) == null);
	}

	
	public void TestCompositeInstanceContext_Equality() throws Exception {
		
		String patientName = "Smith^Mary";
		String patientID = "3764913624";
		String patientBirthDate = "19600101";
		String patientSex = "F";
		String studyID = "612386812";
		String seriesNumber = "12";
		String instanceNumber = "38";
		String referringPhysicianName = "Jones^Harriet";
		
		String studyDate = "20100324";
		String studyTime = "142211";
		
		String contentDate = "20100325";
		String contentTime = "142238";
		
		String sopClassUID = SOPClass.ComputedRadiographyImageStorage;

		UIDGenerator u = new UIDGenerator("9999");
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
		String studyInstanceUID = u.getNewStudyInstanceUID(studyID);
		
		AttributeList list = new AttributeList();

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		assertEquals("Checking AttributeList equality with self",list,list);	// this is a pre-requisite for CompositeInstanceContext.equals() to work, though might pass anyway even if list contents were not compared

		CompositeInstanceContext cic1 = new CompositeInstanceContext(list,true/*forSR*/);
		CompositeInstanceContext cic2 = new CompositeInstanceContext(list,true/*forSR*/);

		assertEquals("Checking CompositeInstanceContext.getAttributeList() equality",cic1.getAttributeList(),cic2.getAttributeList());	// this is a pre-requisite for CompositeInstanceContext.equals() to work
		
		assertEquals("Checking CompositeInstanceContext equality",cic1,cic2);
		assertEquals("Checking CompositeInstanceContext hashCode",cic1.hashCode(),cic2.hashCode());
		
		cic2.removeInstance();
		assertTrue("Checking CompositeInstanceContext different inequality",!cic1.equals(cic2));
		assertTrue("Checking CompositeInstanceContext differenthashCode",cic1.hashCode() != cic2.hashCode());
	}
	
	public void TestCompositeInstanceContext_SRSpecificRequestAndProcedure() throws Exception {
		
		String patientName = "Smith^Mary";
		String patientID = "3764913624";
		String patientBirthDate = "19600101";
		String patientSex = "F";
		String studyID = "612386812";
		String seriesNumber = "12";
		String instanceNumber = "38";
		String referringPhysicianName = "Jones^Harriet";
		
		String studyDate = "20100324";
		String studyTime = "142211";
				
		String contentDate = "20100325";
		String contentTime = "142238";
	
		UIDGenerator u = new UIDGenerator("9999");
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
		String studyInstanceUID = u.getNewStudyInstanceUID(studyID);
		
		String sopClassUID = SOPClass.ComputedRadiographyImageStorage;
		
		CodedSequenceItem procedureCode =  new CodedSequenceItem("CTCAP","99BLA","CT C/A/P");
		
		AttributeList list = new AttributeList();

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		{ SequenceAttribute a = new SequenceAttribute(TagFromName.ProcedureCodeSequence); a.addItem(procedureCode.getAttributeList()); list.put(a); }
		
		{
			AttributeList rasList = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); rasList.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.AccessionNumber); rasList.put(a); }
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.RequestedProcedureCodeSequence); a.addItem(procedureCode.getAttributeList()); rasList.put(a); }
			
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.RequestAttributesSequence); a.addItem(rasList); list.put(a); }
		}
		
		CompositeInstanceContext cic = new CompositeInstanceContext(list,true/*forSR*/);
		AttributeList cicList = cic.getAttributeList();
		
		assertEquals("Checking SOPClassUID",sopClassUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPClassUID));
		assertEquals("Checking SOPInstanceUID",sopInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking SeriesInstanceUID",seriesInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking StudyInstanceUID",studyInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyInstanceUID));
		assertEquals("Checking PatientName",patientName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientName));
		assertEquals("Checking PatientID",patientID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientID));
		assertEquals("Checking PatientBirthDate",patientBirthDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientBirthDate));
		assertEquals("Checking PatientSex",patientSex,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientSex));
		assertEquals("Checking StudyID",studyID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyID));
		assertEquals("Checking ReferringPhysicianName",referringPhysicianName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ReferringPhysicianName));
		assertEquals("Checking AccessionNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.AccessionNumber));
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		assertEquals("Checking Manufacturer","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Manufacturer));
		assertEquals("Checking PatientOrientation","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientOrientation));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		assertEquals("Checking StudyDate",studyDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyDate));
		assertEquals("Checking StudyTime",studyTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyTime));
		assertEquals("Checking ContentDate",contentDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime",contentTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));

		assertEquals("Checking ProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(cicList,TagFromName.ProcedureCodeSequence));
		// PerformedProcedureCodeSequence should have been automatically added ...
		assertEquals("Checking PerformedProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(cicList,TagFromName.PerformedProcedureCodeSequence));
		
		assertEquals("Checking RequestAttributesSequence StudyInstanceUID",studyInstanceUID,SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.RequestAttributesSequence,TagFromName.StudyInstanceUID)[0]);
		assertEquals("Checking RequestAttributesSequence AccessionNumber","",SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.RequestAttributesSequence,TagFromName.AccessionNumber)[0]);
		assertEquals("Checking RequestAttributes RequestedProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(((SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(cicList,TagFromName.RequestAttributesSequence,TagFromName.RequestedProcedureCodeSequence))));
		// ReferencedRequestSequence should have been automatically added ...
		assertEquals("Checking ReferencedRequestSequence StudyInstanceUID",studyInstanceUID,SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.ReferencedRequestSequence,TagFromName.StudyInstanceUID)[0]);
		assertEquals("Checking ReferencedRequestSequence AccessionNumber","",SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.ReferencedRequestSequence,TagFromName.AccessionNumber)[0]);
		assertEquals("Checking ReferencedRequestSequence RequestedProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(((SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(cicList,TagFromName.ReferencedRequestSequence,TagFromName.RequestedProcedureCodeSequence))));
		
		cic.removeSeries();
		cicList = cic.getAttributeList();
		assertEquals("Checking SeriesInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking SeriesNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		{
			AttributeList newList = new AttributeList();
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); newList.put(a); }
			cic.putAll(newList);
		}
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		
		cic.removeInstance();
		assertEquals("Checking ContentDate","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));
		assertEquals("Checking SOPInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking InstanceNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); cic.put(a); }
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
	}
	
	public void TestCompositeInstanceContext_SRSpecificRequestAndProcedure_Default() throws Exception {
		
		String patientName = "Smith^Mary";
		String patientID = "3764913624";
		String patientBirthDate = "19600101";
		String patientSex = "F";
		String studyID = "612386812";
		String seriesNumber = "12";
		String instanceNumber = "38";
		String referringPhysicianName = "Jones^Harriet";
		
		String studyDate = "20100324";
		String studyTime = "142211";
				
		String contentDate = "20100325";
		String contentTime = "142238";
	
		UIDGenerator u = new UIDGenerator("9999");
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
		String studyInstanceUID = u.getNewStudyInstanceUID(studyID);
		
		String sopClassUID = SOPClass.ComputedRadiographyImageStorage;
		
		CodedSequenceItem procedureCode =  new CodedSequenceItem("CTCAP","99BLA","CT C/A/P");
		
		AttributeList list = new AttributeList();

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		{ SequenceAttribute a = new SequenceAttribute(TagFromName.ProcedureCodeSequence); a.addItem(procedureCode.getAttributeList()); list.put(a); }
		
		{
			AttributeList rasList = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); rasList.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.AccessionNumber); rasList.put(a); }
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.RequestedProcedureCodeSequence); a.addItem(procedureCode.getAttributeList()); rasList.put(a); }
			
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.RequestAttributesSequence); a.addItem(rasList); list.put(a); }
		}
		
		@SuppressWarnings("deprecation") CompositeInstanceContext cic = new CompositeInstanceContext(list);		// default (deprecated) constructor without explicitly specifying forSR
		AttributeList cicList = cic.getAttributeList();
		
		assertEquals("Checking SOPClassUID",sopClassUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPClassUID));
		assertEquals("Checking SOPInstanceUID",sopInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking SeriesInstanceUID",seriesInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking StudyInstanceUID",studyInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyInstanceUID));
		assertEquals("Checking PatientName",patientName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientName));
		assertEquals("Checking PatientID",patientID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientID));
		assertEquals("Checking PatientBirthDate",patientBirthDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientBirthDate));
		assertEquals("Checking PatientSex",patientSex,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientSex));
		assertEquals("Checking StudyID",studyID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyID));
		assertEquals("Checking ReferringPhysicianName",referringPhysicianName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ReferringPhysicianName));
		assertEquals("Checking AccessionNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.AccessionNumber));
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		assertEquals("Checking Manufacturer","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Manufacturer));
		assertEquals("Checking PatientOrientation","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientOrientation));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		assertEquals("Checking StudyDate",studyDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyDate));
		assertEquals("Checking StudyTime",studyTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyTime));
		assertEquals("Checking ContentDate",contentDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime",contentTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));

		assertEquals("Checking ProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(cicList,TagFromName.ProcedureCodeSequence));
		// PerformedProcedureCodeSequence should have been automatically added ...
		assertEquals("Checking PerformedProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(cicList,TagFromName.PerformedProcedureCodeSequence));
		
		assertEquals("Checking RequestAttributesSequence StudyInstanceUID",studyInstanceUID,SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.RequestAttributesSequence,TagFromName.StudyInstanceUID)[0]);
		assertEquals("Checking RequestAttributesSequence AccessionNumber","",SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.RequestAttributesSequence,TagFromName.AccessionNumber)[0]);
		assertEquals("Checking RequestAttributes RequestedProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(((SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(cicList,TagFromName.RequestAttributesSequence,TagFromName.RequestedProcedureCodeSequence))));
		// ReferencedRequestSequence should have been automatically added ...
		assertEquals("Checking ReferencedRequestSequence StudyInstanceUID",studyInstanceUID,SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.ReferencedRequestSequence,TagFromName.StudyInstanceUID)[0]);
		assertEquals("Checking ReferencedRequestSequence AccessionNumber","",SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.ReferencedRequestSequence,TagFromName.AccessionNumber)[0]);
		assertEquals("Checking ReferencedRequestSequence RequestedProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(((SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(cicList,TagFromName.ReferencedRequestSequence,TagFromName.RequestedProcedureCodeSequence))));
		
		cic.removeSeries();
		cicList = cic.getAttributeList();
		assertEquals("Checking SeriesInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking SeriesNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		{
			AttributeList newList = new AttributeList();
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); newList.put(a); }
			cic.putAll(newList);
		}
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		
		cic.removeInstance();
		assertEquals("Checking ContentDate","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));
		assertEquals("Checking SOPInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking InstanceNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); cic.put(a); }
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
	}
	
	public void TestCompositeInstanceContext_WithoutSRSpecificRequestAndProcedure() throws Exception {
		
		String patientName = "Smith^Mary";
		String patientID = "3764913624";
		String patientBirthDate = "19600101";
		String patientSex = "F";
		String studyID = "612386812";
		String seriesNumber = "12";
		String instanceNumber = "38";
		String referringPhysicianName = "Jones^Harriet";
		
		String studyDate = "20100324";
		String studyTime = "142211";
				
		String contentDate = "20100325";
		String contentTime = "142238";
	
		UIDGenerator u = new UIDGenerator("9999");
		String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
		String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
		String studyInstanceUID = u.getNewStudyInstanceUID(studyID);
		
		String sopClassUID = SOPClass.ComputedRadiographyImageStorage;
		
		CodedSequenceItem procedureCode =  new CodedSequenceItem("CTCAP","99BLA","CT C/A/P");
		
		AttributeList list = new AttributeList();

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTime); list.put(a); }
		
		{ SequenceAttribute a = new SequenceAttribute(TagFromName.ProcedureCodeSequence); a.addItem(procedureCode.getAttributeList()); list.put(a); }
		
		{
			AttributeList rasList = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); rasList.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.AccessionNumber); rasList.put(a); }
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.RequestedProcedureCodeSequence); a.addItem(procedureCode.getAttributeList()); rasList.put(a); }
			
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.RequestAttributesSequence); a.addItem(rasList); list.put(a); }
		}
		
		CompositeInstanceContext cic = new CompositeInstanceContext(list,false/*forSR*/);
		AttributeList cicList = cic.getAttributeList();
		
		assertEquals("Checking SOPClassUID",sopClassUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPClassUID));
		assertEquals("Checking SOPInstanceUID",sopInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking SeriesInstanceUID",seriesInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking StudyInstanceUID",studyInstanceUID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyInstanceUID));
		assertEquals("Checking PatientName",patientName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientName));
		assertEquals("Checking PatientID",patientID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientID));
		assertEquals("Checking PatientBirthDate",patientBirthDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientBirthDate));
		assertEquals("Checking PatientSex",patientSex,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientSex));
		assertEquals("Checking StudyID",studyID,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyID));
		assertEquals("Checking ReferringPhysicianName",referringPhysicianName,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ReferringPhysicianName));
		assertEquals("Checking AccessionNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.AccessionNumber));
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		assertEquals("Checking Manufacturer","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Manufacturer));
		assertEquals("Checking PatientOrientation","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.PatientOrientation));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		assertEquals("Checking StudyDate",studyDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyDate));
		assertEquals("Checking StudyTime",studyTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.StudyTime));
		assertEquals("Checking ContentDate",contentDate,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime",contentTime,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));

		assertEquals("Checking ProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(cicList,TagFromName.ProcedureCodeSequence));
		// PerformedProcedureCodeSequence should NOT have been automatically added ...
		assertEquals("Checking PerformedProcedureCodeSequence",null,CodedSequenceItem.getSingleCodedSequenceItemOrNull(cicList,TagFromName.PerformedProcedureCodeSequence));
		
		assertEquals("Checking RequestAttributesSequence StudyInstanceUID",studyInstanceUID,SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.RequestAttributesSequence,TagFromName.StudyInstanceUID)[0]);
		assertEquals("Checking RequestAttributesSequence AccessionNumber","",SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(cicList,TagFromName.RequestAttributesSequence,TagFromName.AccessionNumber)[0]);
		assertEquals("Checking RequestAttributes RequestedProcedureCodeSequence",procedureCode,CodedSequenceItem.getSingleCodedSequenceItemOrNull(((SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(cicList,TagFromName.RequestAttributesSequence,TagFromName.RequestedProcedureCodeSequence))));
		// ReferencedRequestSequence should NOThave been automatically added ...
		assertEquals("Checking ReferencedRequestSequence",null,cicList.get(TagFromName.ReferencedRequestSequence));
		
		cic.removeSeries();
		cicList = cic.getAttributeList();
		assertEquals("Checking SeriesInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesInstanceUID));
		assertEquals("Checking SeriesNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		assertEquals("Checking Laterality","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.Laterality));
		{
			AttributeList newList = new AttributeList();
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); newList.put(a); }
			cic.putAll(newList);
		}
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SeriesNumber));
		
		cic.removeInstance();
		assertEquals("Checking ContentDate","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentDate));
		assertEquals("Checking ContentTime","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.ContentTime));
		assertEquals("Checking SOPInstanceUID","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.SOPInstanceUID));
		assertEquals("Checking InstanceNumber","",Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); cic.put(a); }
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(cicList,TagFromName.InstanceNumber));
	}
	

}
