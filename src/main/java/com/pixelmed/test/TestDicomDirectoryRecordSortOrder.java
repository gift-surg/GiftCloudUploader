/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestDicomDirectoryRecordSortOrder extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDicomDirectoryRecordSortOrder(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDicomDirectoryRecordSortOrder.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDicomDirectoryRecordSortOrder");
		
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_IdenticalUnrecognizedRecord"));
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_IdenticalSeriesRecord"));
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_IdenticalStudyRecord"));
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_SameStringDifferentUIDStudyRecord"));
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_IdenticalPatientRecord"));
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesString"));
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesInteger"));
		
		suite.addTest(new TestDicomDirectoryRecordSortOrder("TestDicomDirectoryRecordSortOrder_IdenticalPatientStudySeriesAttributes"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestDicomDirectoryRecordSortOrder_IdenticalUnrecognizedRecord() throws Exception {
	
		// Even though the contents of the two records are identical, still expect two separate nodes
	
		AttributeList record1list = new AttributeList();

		AttributeList record2list = new AttributeList();
		
		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord parent = nodeFactory.getNewTopDirectoryRecord();
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalUnrecognizedRecord(): creating record1");
		DicomDirectoryRecord record1 = nodeFactory.getNewDicomDirectoryRecord(parent,record1list);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalUnrecognizedRecord(): adding record1 to parent");
		parent.addChild(record1);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalUnrecognizedRecord(): creating record2");
		DicomDirectoryRecord record2 = nodeFactory.getNewDicomDirectoryRecord(parent,record2list);
		parent.addChild(record2);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalUnrecognizedRecord(): adding record2 to parent");

		assertEquals("Checking two children",2,parent.getChildCount());
	}
	
	public void TestDicomDirectoryRecordSortOrder_IdenticalSeriesRecord() throws Exception {
	
		// Even though the contents of the two records are identical, still expect two separate nodes
	
		String directoryRecordType = "SERIES";
		String commonSeriesInstanceUID = "1.2.3.4.5.6.7";
		String commonSeriesNumber = "001";
	
		AttributeList record1list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(      TagFromName.DirectoryRecordType); a.addValue(directoryRecordType);     record1list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID);   a.addValue(commonSeriesInstanceUID); record1list.put(a); }
		{ Attribute a = new IntegerStringAttribute(   TagFromName.SeriesNumber);        a.addValue(commonSeriesNumber);      record1list.put(a); }

		AttributeList record2list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(      TagFromName.DirectoryRecordType); a.addValue(directoryRecordType);     record2list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID);   a.addValue(commonSeriesInstanceUID); record2list.put(a); }
		{ Attribute a = new IntegerStringAttribute(   TagFromName.SeriesNumber);        a.addValue(commonSeriesNumber);      record2list.put(a); }
		
		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord parent = nodeFactory.getNewTopDirectoryRecord();
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalSeriesRecord(): creating record1");
		DicomDirectoryRecord record1 = nodeFactory.getNewDicomDirectoryRecord(parent,record1list);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalSeriesRecord(): adding record1 to parent");
		parent.addChild(record1);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalSeriesRecord(): creating record2");
		DicomDirectoryRecord record2 = nodeFactory.getNewDicomDirectoryRecord(parent,record2list);
		parent.addChild(record2);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalSeriesRecord(): adding record2 to parent");

		assertEquals("Checking two children",2,parent.getChildCount());
	}
	
	public void TestDicomDirectoryRecordSortOrder_IdenticalStudyRecord() throws Exception {
	
		// Even though the contents of the two records are identical, still expect two separate nodes
	
		String directoryRecordType = "STUDY";
		String commonStudyInstanceUID = "1.2.3.4.5.6.7";
		String commonStudyDate = "20110904";
		String commonStudyID = "567891";
		String commonStudyDescription = "Same study description";
	
		AttributeList record1list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue(directoryRecordType);    record1list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(commonStudyInstanceUID); record1list.put(a); }
		{ Attribute a = new DateAttribute(            TagFromName.StudyDate);        a.addValue(commonStudyDate);        record1list.put(a); }
		{ Attribute a = new ShortStringAttribute(     TagFromName.StudyID);          a.addValue(commonStudyID);          record1list.put(a); }
		{ Attribute a = new LongStringAttribute(      TagFromName.StudyDescription); a.addValue(commonStudyDescription); record1list.put(a); }

		AttributeList record2list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue(directoryRecordType);    record2list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(commonStudyInstanceUID); record2list.put(a); }
		{ Attribute a = new DateAttribute(            TagFromName.StudyDate);        a.addValue(commonStudyDate);        record2list.put(a); }
		{ Attribute a = new ShortStringAttribute(     TagFromName.StudyID);          a.addValue(commonStudyID);          record2list.put(a); }
		{ Attribute a = new LongStringAttribute(      TagFromName.StudyDescription); a.addValue(commonStudyDescription); record2list.put(a); }
		
		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord parent = nodeFactory.getNewTopDirectoryRecord();
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalStudyRecord(): creating record1");
		DicomDirectoryRecord record1 = nodeFactory.getNewDicomDirectoryRecord(parent,record1list);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalStudyRecord(): adding record1 to parent");
		parent.addChild(record1);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalStudyRecord(): creating record2");
		DicomDirectoryRecord record2 = nodeFactory.getNewDicomDirectoryRecord(parent,record2list);
		parent.addChild(record2);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalStudyRecord(): adding record2 to parent");

		assertEquals("Checking two children",2,parent.getChildCount());
	}
	
	public void TestDicomDirectoryRecordSortOrder_SameStringDifferentUIDStudyRecord() throws Exception {
	
		// Even though the contents of the two records have same description, still expect two separate nodes if different UID
	
		String directoryRecordType = "STUDY";
		String commonStudyDate = "20110904";
		String commonStudyID = "567891";
		String commonStudyDescription = "Same study description";
	
		AttributeList record1list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue(directoryRecordType);    record1list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue("1.2.3.4.5.6.7");        record1list.put(a); }
		{ Attribute a = new DateAttribute(            TagFromName.StudyDate);        a.addValue(commonStudyDate);        record1list.put(a); }
		{ Attribute a = new ShortStringAttribute(     TagFromName.StudyID);          a.addValue(commonStudyID);          record1list.put(a); }
		{ Attribute a = new LongStringAttribute(      TagFromName.StudyDescription); a.addValue(commonStudyDescription); record1list.put(a); }

		AttributeList record2list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue(directoryRecordType);    record2list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue("1.2.3.4.5.6.8");        record2list.put(a); }
		{ Attribute a = new DateAttribute(            TagFromName.StudyDate);        a.addValue(commonStudyDate);        record2list.put(a); }
		{ Attribute a = new ShortStringAttribute(     TagFromName.StudyID);          a.addValue(commonStudyID);          record2list.put(a); }
		{ Attribute a = new LongStringAttribute(      TagFromName.StudyDescription); a.addValue(commonStudyDescription); record2list.put(a); }
		
		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord parent = nodeFactory.getNewTopDirectoryRecord();
//System.err.println("TestDicomDirectoryRecordSortOrder_SameStringDifferentUIDStudyRecord(): creating record1");
		DicomDirectoryRecord record1 = nodeFactory.getNewDicomDirectoryRecord(parent,record1list);
//System.err.println("TestDicomDirectoryRecordSortOrder_SameStringDifferentUIDStudyRecord(): adding record1 to parent");
		parent.addChild(record1);
//System.err.println("TestDicomDirectoryRecordSortOrder_SameStringDifferentUIDStudyRecord(): creating record2");
		DicomDirectoryRecord record2 = nodeFactory.getNewDicomDirectoryRecord(parent,record2list);
		parent.addChild(record2);
//System.err.println("TestDicomDirectoryRecordSortOrder_SameStringDifferentUIDStudyRecord(): adding record2 to parent");

		assertEquals("Checking two children",2,parent.getChildCount());
	}
	
	public void TestDicomDirectoryRecordSortOrder_IdenticalPatientRecord() throws Exception {
	
		// Even though the contents of the two records are identical, still expect two separate nodes
	
		String directoryRecordType = "PATIENT";
		String commonPatientName = "Smith^John";
		String commonPatientID = "123456";
	
		AttributeList record1list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue(directoryRecordType); record1list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName);            a.addValue(commonPatientName);   record1list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.PatientName);           a.addValue(commonPatientID);     record1list.put(a); }

		AttributeList record2list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue(directoryRecordType); record2list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName);            a.addValue(commonPatientName);   record2list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.PatientName);           a.addValue(commonPatientID);     record2list.put(a); }
		
		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord parent = nodeFactory.getNewTopDirectoryRecord();
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalPatientRecord(): creating record1");
		DicomDirectoryRecord record1 = nodeFactory.getNewDicomDirectoryRecord(parent,record1list);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalPatientRecord(): adding record1 to parent");
		parent.addChild(record1);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalPatientRecord(): creating record2");
		DicomDirectoryRecord record2 = nodeFactory.getNewDicomDirectoryRecord(parent,record2list);
		parent.addChild(record2);
//System.err.println("TestDicomDirectoryRecordSortOrder_IdenticalPatientRecord(): adding record2 to parent");

		assertEquals("Checking two children",2,parent.getChildCount());
	}
	public void TestDicomDirectoryRecordSortOrder_DifferentRecordTypesString() throws Exception {
	
		// Even though the contents of the two records are identical, still expect two separate nodes
	
		AttributeList record1list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue("PATIENT"); record1list.put(a); }

		AttributeList record2list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue("STUDY"); record2list.put(a); }
		
		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord parent = nodeFactory.getNewTopDirectoryRecord();
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesString(): creating record1");
		DicomDirectoryRecord record1 = nodeFactory.getNewDicomDirectoryRecord(parent,record1list);
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesString(): adding record1 to parent");
		parent.addChild(record1);
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesString(): creating record2");
		DicomDirectoryRecord record2 = nodeFactory.getNewDicomDirectoryRecord(parent,record2list);
		parent.addChild(record2);
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesString(): adding record2 to parent");

		assertEquals("Checking two children",2,parent.getChildCount());
	}
	
	public void TestDicomDirectoryRecordSortOrder_DifferentRecordTypesInteger() throws Exception {
	
		// Even though the contents of the two records are identical, still expect two separate nodes
	
		AttributeList record1list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue("SERIES"); record1list.put(a); }

		AttributeList record2list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType);    a.addValue("IMAGE"); record2list.put(a); }
		
		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord parent = nodeFactory.getNewTopDirectoryRecord();
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesInteger(): creating record1");
		DicomDirectoryRecord record1 = nodeFactory.getNewDicomDirectoryRecord(parent,record1list);
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesInteger(): adding record1 to parent");
		parent.addChild(record1);
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesInteger(): creating record2");
		DicomDirectoryRecord record2 = nodeFactory.getNewDicomDirectoryRecord(parent,record2list);
		parent.addChild(record2);
//System.err.println("TestDicomDirectoryRecordSortOrder_DifferentRecordTypesInteger(): adding record2 to parent");

		assertEquals("Checking two children",2,parent.getChildCount());
	}
	
	public void TestDicomDirectoryRecordSortOrder_IdenticalPatientStudySeriesAttributes() throws Exception {
	
		// Even though the contents of the two records are identical, still expect two separate nodes
	
		String directoryRecordType = "PATIENT";
		String commonPatientName = "Smith^John";
		String commonPatientID = "123456";

		String commonStudyInstanceUID = "1.2.3.4.5.6.7";
		String commonStudyDate = "20110904";
		String commonStudyID = "567891";
		String commonStudyDescription = "Same study description";
	
		String commonSeriesInstanceUID = "1.2.3.4.5.6.7.8";
		String commonSeriesNumber = "001";
	
		String commonSOPClassUID = SOPClass.SecondaryCaptureImageStorage;

		AttributeList record1list = new AttributeList();
		
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName);				a.addValue(commonPatientName);   record1list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.PatientName);				a.addValue(commonPatientID);     record1list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);	a.addValue(commonStudyInstanceUID); record1list.put(a); }
		{ Attribute a = new DateAttribute(            TagFromName.StudyDate);			a.addValue(commonStudyDate);        record1list.put(a); }
		{ Attribute a = new ShortStringAttribute(     TagFromName.StudyID);				a.addValue(commonStudyID);          record1list.put(a); }
		{ Attribute a = new LongStringAttribute(      TagFromName.StudyDescription);	a.addValue(commonStudyDescription); record1list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID);   a.addValue(commonSeriesInstanceUID); record1list.put(a); }
		{ Attribute a = new IntegerStringAttribute(   TagFromName.SeriesNumber);        a.addValue(commonSeriesNumber);      record1list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);		a.addValue("1.2.3.4.5.6.7.8.9");	 record1list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);			a.addValue(commonSOPClassUID);		 record1list.put(a); }
		{ Attribute a = new IntegerStringAttribute(   TagFromName.InstanceNumber);      a.addValue(1);						 record1list.put(a); }
		
		AttributeList record2list = new AttributeList();
		
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName);				a.addValue(commonPatientName);   record2list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.PatientName);				a.addValue(commonPatientID);     record2list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);	a.addValue(commonStudyInstanceUID); record2list.put(a); }
		{ Attribute a = new DateAttribute(            TagFromName.StudyDate);			a.addValue(commonStudyDate);        record2list.put(a); }
		{ Attribute a = new ShortStringAttribute(     TagFromName.StudyID);				a.addValue(commonStudyID);          record2list.put(a); }
		{ Attribute a = new LongStringAttribute(      TagFromName.StudyDescription);	a.addValue(commonStudyDescription); record2list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID);   a.addValue(commonSeriesInstanceUID); record2list.put(a); }
		{ Attribute a = new IntegerStringAttribute(   TagFromName.SeriesNumber);        a.addValue(commonSeriesNumber);      record2list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);		a.addValue("1.2.3.4.5.6.7.8.10");	 record2list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);			a.addValue(commonSOPClassUID);		 record2list.put(a); }
		{ Attribute a = new IntegerStringAttribute(   TagFromName.InstanceNumber);      a.addValue(2);						 record2list.put(a); }
		
		DicomDirectory dicomdir = new DicomDirectory();
		
		dicomdir.addAttributeListFromDicomFileToDirectory(record1list,"FILE1");
		dicomdir.addAttributeListFromDicomFileToDirectory(record2list,"FILE2");
//System.err.print("TestDicomDirectoryRecordSortOrder_IdenticalPatientStudySeriesAttributes(): DICOMDIR:\n"+dicomdir.toString());
		
		DicomDirectoryRecord parent = (DicomDirectoryRecord)(dicomdir.getRoot());
		assertTrue(parent instanceof DicomDirectoryRecordFactory.TopDirectoryRecord);
		assertEquals("Checking parent has one patient child",1,parent.getChildCount());
		
		DicomDirectoryRecord patient = (DicomDirectoryRecord)(parent.getChildAt(0));
//System.err.print(patient.toString());
		assertTrue(patient instanceof DicomDirectoryRecordFactory.PatientDirectoryRecord);
		assertEquals("Checking patient has one study child",1,patient.getChildCount());
		
		DicomDirectoryRecord study = (DicomDirectoryRecord)(patient.getChildAt(0));
//System.err.print(study.toString());
		assertTrue(study instanceof DicomDirectoryRecordFactory.StudyDirectoryRecord);
		assertEquals("Checking study has one series child",1,study.getChildCount());
		
		DicomDirectoryRecord series = (DicomDirectoryRecord)(study.getChildAt(0));
//System.err.print(series.toString());
		assertTrue(series instanceof DicomDirectoryRecordFactory.SeriesDirectoryRecord);
		assertEquals("Checking series has two image children",2,series.getChildCount());
		
		assertTrue(series.getChildAt(0) instanceof DicomDirectoryRecordFactory.ImageDirectoryRecord);
		assertTrue(series.getChildAt(1) instanceof DicomDirectoryRecordFactory.ImageDirectoryRecord);
	}
	
}

