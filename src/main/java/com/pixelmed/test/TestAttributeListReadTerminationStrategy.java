/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestAttributeListReadTerminationStrategy extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAttributeListReadTerminationStrategy(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAttributeListReadTerminationStrategy.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAttributeListReadTerminationStrategy");
		
		suite.addTest(new TestAttributeListReadTerminationStrategy("TestAttributeListReadTerminationStrategy_SpecificAttribute"));
		suite.addTest(new TestAttributeListReadTerminationStrategy("TestAttributeListReadTerminationStrategy_SpecificAttributeWithMetaHeader"));
		suite.addTest(new TestAttributeListReadTerminationStrategy("TestAttributeListReadTerminationStrategy_TagRange"));
		suite.addTest(new TestAttributeListReadTerminationStrategy("TestAttributeListReadTerminationStrategy_ByteOffset"));
		suite.addTest(new TestAttributeListReadTerminationStrategy("TestAttributeListReadTerminationStrategy_SpecificAttributeWithinSequence"));

		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private String patientName = "Smith^Mary";
	private String patientID = "3764913624";
	private String patientBirthDate = "19600101";
	private String patientAge = "041Y";
	private String patientWeight = "68";
	private String patientSize = "1.55";
	private String patientSex = "F";
	private String studyID = "612386812";
	private String seriesNumber = "12";
	private String instanceNumber = "38";
	private String referringPhysicianName = "Jones^Harriet";
	private String studyDate = "20010203";
	private String studyTime = "043000";

	private AttributeList makeAttributeList() {
		AttributeList list = new AttributeList();
		try {
			UIDGenerator u = new UIDGenerator("9999");
			String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
			String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
			String studyInstanceUID = u.getNewStudyInstanceUID(studyID);

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.CTImageStorage); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
			{ Attribute a = new AgeStringAttribute(TagFromName.PatientAge); a.addValue(patientAge); list.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(patientWeight); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(patientSize); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); /*a.addValue(manufacturer);*/ list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		}
		catch (DicomException e) {
		}
		return list;
	}
	
	// Just like using the stopAtTag ...
	
	private class OurReadTerminationStrategy_SpecificAttribute implements AttributeList.ReadTerminationStrategy {
		public boolean terminate(AttributeList attributeList, AttributeTag tag, long byteOffset) {
			return tag.equals(TagFromName.PatientID);
		}
	}
	
	public void TestAttributeListReadTerminationStrategy_SpecificAttribute() throws Exception {
		String creatorValue = "Test Creator";
		File testFile = File.createTempFile("TestAttributeListReadTerminationStrategy_SpecificAttribute",".dcm");
		{
			AttributeList list = makeAttributeList();
			list.write(testFile);
		}
		{
			AttributeList list = new AttributeList();
			AttributeList.ReadTerminationStrategy strategy = new OurReadTerminationStrategy_SpecificAttribute();
			list.read(testFile,strategy);
//System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttribute():\n"+list);
			
			{
				Attribute a = list.get(TagFromName.StudyDate);
//System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttribute(): a "+a);
				assertTrue("Checking StudyDate was read",a != null);
				assertEquals("Checking StudyDate value",studyDate,a.getSingleStringValueOrNull());
			}
			
			{
				Attribute a = list.get(TagFromName.PatientAge);
//System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttribute(): a "+a);
				assertTrue("Checking PatientAge was not read",a == null);
			}
		}
	}
	
	public void TestAttributeListReadTerminationStrategy_SpecificAttributeWithMetaHeader() throws Exception {
		String creatorValue = "Test Creator";
		File testFile = File.createTempFile("TestAttributeListReadTerminationStrategy_SpecificAttributeWithMetaHeader",".dcm");
		{
			AttributeList list = makeAttributeList();
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile);
		}
		{
			AttributeList list = new AttributeList();
			AttributeList.ReadTerminationStrategy strategy = new OurReadTerminationStrategy_SpecificAttribute();
			list.read(testFile,strategy);
//System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttributeWithMetaHeader():\n"+list);
			
			{
				Attribute a = list.get(TagFromName.StudyDate);
//System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttributeWithMetaHeader(): a "+a);
				assertTrue("Checking StudyDate was read",a != null);
				assertEquals("Checking StudyDate value",studyDate,a.getSingleStringValueOrNull());
			}
			
			{
				Attribute a = list.get(TagFromName.PatientAge);
//System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttributeWithMetaHeader(): a "+a);
				assertTrue("Checking PatientAge was not read",a == null);
			}
		}
	}
	
	// Jim Irrer's pattern of stopping at the begining of a certain group ...
	
	private class OurReadTerminationStrategy_TagRange implements AttributeList.ReadTerminationStrategy {
		public boolean terminate(AttributeList attributeList, AttributeTag tag, long byteOffset) {
			return tag.getGroup() >= TagFromName.PatientID.getGroup();
		}
	}
	
	public void TestAttributeListReadTerminationStrategy_TagRange() throws Exception {
		String creatorValue = "Test Creator";
		File testFile = File.createTempFile("TestAttributeListReadTerminationStrategy_TagRange",".dcm");
		{
			AttributeList list = makeAttributeList();
			list.write(testFile);
		}
		{
			AttributeList list = new AttributeList();
			AttributeList.ReadTerminationStrategy strategy = new OurReadTerminationStrategy_TagRange();
			list.read(testFile,strategy);
//System.err.println("TestAttributeListReadTerminationStrategy_TagRange():\n"+list);
			
			{
				Attribute a = list.get(TagFromName.StudyDate);
//System.err.println("TestAttributeListReadTerminationStrategy_TagRange(): a "+a);
				assertTrue("Checking StudyDate was read",a != null);
				assertEquals("Checking StudyDate value",studyDate,a.getSingleStringValueOrNull());
			}
			
			{
				Attribute a = list.get(TagFromName.PatientAge);
//System.err.println("TestAttributeListReadTerminationStrategy_TagRange(): a "+a);
				assertTrue("Checking PatientAge was not read",a == null);
			}
		}
	}
	
	// Jim Irrer's pattern of stopping after a certain size ...
	
	private class OurReadTerminationStrategy_ByteOffset implements AttributeList.ReadTerminationStrategy {
		public boolean terminate(AttributeList attributeList, AttributeTag tag, long byteOffset) {
			return byteOffset >= 256;	// this number for this test gets us as far as Manufacturer and no further (NB. we have no meta-header)
		}
	}
	
	public void TestAttributeListReadTerminationStrategy_ByteOffset() throws Exception {
		String creatorValue = "Test Creator";
		File testFile = File.createTempFile("TestAttributeListReadTerminationStrategy_ByteOffset",".dcm");
		{
			AttributeList list = makeAttributeList();
			list.write(testFile);
		}
		{
			AttributeList list = new AttributeList();
			AttributeList.ReadTerminationStrategy strategy = new OurReadTerminationStrategy_ByteOffset();
			list.read(testFile,strategy);
//System.err.println("TestAttributeListReadTerminationStrategy_ByteOffset():\n"+list);
			
			{
				Attribute a = list.get(TagFromName.StudyDate);
//System.err.println("TestAttributeListReadTerminationStrategy_ByteOffset(): a "+a);
				assertTrue("Checking StudyDate was read",a != null);
				assertEquals("Checking StudyDate value",studyDate,a.getSingleStringValueOrNull());
			}
			
			{
				Attribute a = list.get(TagFromName.PatientAge);
//System.err.println("TestAttributeListReadTerminationStrategy_ByteOffset(): a "+a);
				assertTrue("Checking PatientAge was not read",a == null);
			}
		}
	}
	
		
	private class OurReadTerminationStrategy_SpecificAttributeWithinSequence implements AttributeList.ReadTerminationStrategy {
		public boolean terminate(AttributeList attributeList, AttributeTag tag, long byteOffset) {
			//return false;
			return tag.equals(TagFromName.CodingSchemeDesignator);		// is within a sequence, but will not be tested
		}
	}
	
//	public void TestAttributeListReadTerminationStrategy_SpecificAttributeWithinSequence() throws Exception {
//		String creatorValue = "Test Creator";
//		File testFile = File.createTempFile("TestAttributeListReadTerminationStrategy_SpecificAttributeWithinSequence",".dcm");
//		{
//			AttributeList list = makeAttributeList();
//			{
//				SequenceAttribute s1 = new SequenceAttribute(TagFromName.RequestAttributesSequence);
//				list.put(s1);
//				AttributeList s1list = new AttributeList();
//				s1.addItem(s1list);
//				{
//					SequenceAttribute s2 = new SequenceAttribute(TagFromName.RequestedProcedureCodeSequence);
//					s1list.put(s2);
//					AttributeList s2list = new CodedSequenceItem("43254","99BLA","CT Brain").getAttributeList();
//					s2.addItem(s2list);
//				}
//			}
//			list.put(new SequenceAttribute(TagFromName.Relevant​Information​Sequence));
//
//			list.write(testFile);
//		}
//		{
//			AttributeList list = new AttributeList();
//			AttributeList.ReadTerminationStrategy strategy = new OurReadTerminationStrategy_SpecificAttributeWithinSequence();
//			list.read(testFile,strategy);
////System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttributeWithinSequence():\n"+list);
//
//			{
//				Attribute a = list.get(TagFromName.StudyDate);
////System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttributeWithinSequence(): a "+a);
//				assertTrue("Checking StudyDate was read",a != null);
//				assertEquals("Checking StudyDate value",studyDate,a.getSingleStringValueOrNull());
//			}
//
//			{
//				Attribute a = list.get(TagFromName.Relevant​Information​Sequence);
////System.err.println("TestAttributeListReadTerminationStrategy_SpecificAttributeWithinSequence(): a "+a);
//				assertTrue("Checking Relevant​Information​Sequence was read",a != null);
//			}
//		}
//	}

	
}
