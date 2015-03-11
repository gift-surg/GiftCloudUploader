/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestDicomDirectoryRecordFactory extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDicomDirectoryRecordFactory(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDicomDirectoryRecordFactory.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDicomDirectoryRecordFactory");
		
		suite.addTest(new TestDicomDirectoryRecordFactory("TestDicomDirectoryRecordFactory_MissingDirectoryRecordType"));
		suite.addTest(new TestDicomDirectoryRecordFactory("TestDicomDirectoryRecordFactory_ZeroLengthDirectoryRecordType"));
		suite.addTest(new TestDicomDirectoryRecordFactory("TestDicomDirectoryRecordFactory_UnrecognizableDirectoryRecordType"));
		suite.addTest(new TestDicomDirectoryRecordFactory("TestDicomDirectoryRecordFactory_PatientDirectoryRecordType"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestDicomDirectoryRecordFactory_MissingDirectoryRecordType() throws Exception {
		
		AttributeList recordlist = new AttributeList();

		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord record = nodeFactory.getNewDicomDirectoryRecord(null,recordlist);
		
		assertTrue("DirectoryRecordType is Unrecognized",record instanceof DicomDirectoryRecordFactory.UnrecognizedDirectoryRecord);
	}
	
	public void TestDicomDirectoryRecordFactory_ZeroLengthDirectoryRecordType() throws Exception {
	
		String directoryRecordType = "  ";
	
		AttributeList recordlist = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType); a.addValue(directoryRecordType); recordlist.put(a); }

		assertTrue("DirectoryRecordType is not zero length in originalStringValues in Attribute",recordlist.get(TagFromName.DirectoryRecordType).getOriginalStringValues()[0].length() > 0);
		assertTrue("DirectoryRecordType is zero length in getSingleStringValue in Attribute",Attribute.getSingleStringValueOrEmptyString(recordlist,TagFromName.DirectoryRecordType).length() == 0);

		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord record = nodeFactory.getNewDicomDirectoryRecord(null,recordlist);
		
		assertTrue("DirectoryRecordType is Unrecognized",record instanceof DicomDirectoryRecordFactory.UnrecognizedDirectoryRecord);
	}
	
	public void TestDicomDirectoryRecordFactory_UnrecognizableDirectoryRecordType() throws Exception {
	
		String directoryRecordType = "KASUFDGYUWQERF";
	
		AttributeList recordlist = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType); a.addValue(directoryRecordType); recordlist.put(a); }

		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord record = nodeFactory.getNewDicomDirectoryRecord(null,recordlist);
		
		assertTrue("DirectoryRecordType is Unrecognized",record instanceof DicomDirectoryRecordFactory.UnrecognizedDirectoryRecord);
	}
	
	public void TestDicomDirectoryRecordFactory_PatientDirectoryRecordType() throws Exception {
	
		String directoryRecordType = "PATIENT";
	
		AttributeList recordlist = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.DirectoryRecordType); a.addValue(directoryRecordType); recordlist.put(a); }

		DicomDirectoryRecordFactory nodeFactory = new DicomDirectoryRecordFactory();
		DicomDirectoryRecord record = nodeFactory.getNewDicomDirectoryRecord(null,recordlist);
		
		assertTrue("DirectoryRecordType is Patient",record instanceof DicomDirectoryRecordFactory.PatientDirectoryRecord);
	}
	
	public void TestDicomDirectoryRecordFactory_StudyRecord() throws Exception {
	}
}

