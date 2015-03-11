/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

public class TestSourceOfDoseInformation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSourceOfDoseInformation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSourceOfDoseInformation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSourceOfDoseInformation");
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_AutomatedDataCollection_Description"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_AutomatedDataCollection_Abbreviation"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_AutomatedDataCollection_Equality"));
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_ManualEntry_Description"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_ManualEntry_Abbreviation"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_ManualEntry_Equality"));
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_MPPSContent_Description"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_MPPSContent_Abbreviation"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_MPPSContent_Equality"));
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_Dosimeter_Description"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_Dosimeter_Abbreviation"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_Dosimeter_Equality"));
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_CopiedFromImageAttributes_Description"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_CopiedFromImageAttributes_Abbreviation"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_CopiedFromImageAttributes_Equality"));
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_ComputedFromImageAttributes_Description"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_ComputedFromImageAttributes_Abbreviation"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_ComputedFromImageAttributes_Equality"));
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_DerivedFromHumanReadableReports_Description"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_DerivedFromHumanReadableReports_Abbreviation"));
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_DerivedFromHumanReadableReports_Equality"));
		
		suite.addTest(new TestSourceOfDoseInformation("TestSourceOfDoseInformation_ManualEntry_ManualEntry_Inequality"));
		
		return suite;
	}

	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSourceOfDoseInformation_AutomatedDataCollection_Description() {
		
		assertEquals("Checking AUTOMATED_DATA_COLLECTION description",SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION.toString(),"Automated Data Collection");
	}
	
	public void TestSourceOfDoseInformation_ManualEntry_Description() {
		
		assertEquals("Checking MANUAL_ENTRY description",SourceOfDoseInformation.MANUAL_ENTRY.toString(),"Manual Entry");
	}
	
	public void TestSourceOfDoseInformation_MPPSContent_Description() {
		
		assertEquals("Checking MPPS_CONTENT description",SourceOfDoseInformation.MPPS_CONTENT.toString(),"MPPS Content");
	}
	
	public void TestSourceOfDoseInformation_Dosimeter_Description() {
		
		assertEquals("Checking DOSIMETER description",SourceOfDoseInformation.DOSIMETER.toString(),"Dosimeter");
	}
	
	public void TestSourceOfDoseInformation_CopiedFromImageAttributes_Description() {
		
		assertEquals("Checking COPIED_FROM_IMAGE_ATTRIBUTES description",SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES.toString(),"Copied From Image Attributes");
	}
	
	public void TestSourceOfDoseInformation_ComputedFromImageAttributes_Description() {
		
		assertEquals("Checking COMPUTED_FROM_IMAGE_ATTRIBUTES description",SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES.toString(),"Computed From Image Attributes");
	}
	
	public void TestSourceOfDoseInformation_DerivedFromHumanReadableReports_Description() {
		
		assertEquals("Checking DERIVED_FROM_HUMAN_READABLE_REPORTS description",SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS.toString(),"Derived From Human-Readable Reports");
	}
	

	
	public void TestSourceOfDoseInformation_AutomatedDataCollection_Abbreviation() {
		
		assertEquals("Checking AUTOMATED_DATA_COLLECTION abbreviation",SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION.toStringAbbreviation(),"MOD");
	}
	
	public void TestSourceOfDoseInformation_ManualEntry_Abbreviation() {
		
		assertEquals("Checking MANUAL_ENTRY abbreviation",SourceOfDoseInformation.MANUAL_ENTRY.toStringAbbreviation(),"ENTRY");
	}
	
	public void TestSourceOfDoseInformation_MPPSContent_Abbreviation() {
		
		assertEquals("Checking MPPS_CONTENT abbreviation",SourceOfDoseInformation.MPPS_CONTENT.toStringAbbreviation(),"MPPS");
	}
	
	public void TestSourceOfDoseInformation_Dosimeter_Abbreviation() {
		
		assertEquals("Checking DOSIMETER abbreviation",SourceOfDoseInformation.DOSIMETER.toStringAbbreviation(),"DSM");
	}
	
	public void TestSourceOfDoseInformation_CopiedFromImageAttributes_Abbreviation() {
		
		assertEquals("Checking COPIED_FROM_IMAGE_ATTRIBUTES abbreviation",SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES.toStringAbbreviation(),"HDR");
	}
	
	public void TestSourceOfDoseInformation_ComputedFromImageAttributes_Abbreviation() {
		
		assertEquals("Checking COMPUTED_FROM_IMAGE_ATTRIBUTES abbreviation",SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES.toStringAbbreviation(),"COMP");
	}
	
	public void TestSourceOfDoseInformation_DerivedFromHumanReadableReports_Abbreviation() {
		
		assertEquals("Checking DERIVED_FROM_HUMAN_READABLE_REPORTS abbreviation",SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS.toStringAbbreviation(),"OCR");
	}


	public void TestSourceOfDoseInformation_AutomatedDataCollection_Equality() throws Exception {
		
		assertEquals("Checking AUTOMATED_DATA_COLLECTION equality",SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION,SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION);
		assertEquals("Checking AUTOMATED_DATA_COLLECTION content item hashCode equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION).hashCode(),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION).hashCode());
		assertEquals("Checking AUTOMATED_DATA_COLLECTION content item equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION));
	}
	
	public void TestSourceOfDoseInformation_ManualEntry_Equality() throws Exception  {
		
		assertEquals("Checking MANUAL_ENTRY equality",SourceOfDoseInformation.MANUAL_ENTRY,SourceOfDoseInformation.MANUAL_ENTRY);
		assertEquals("Checking MANUAL_ENTRY content item hashCode equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MANUAL_ENTRY).hashCode(),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MANUAL_ENTRY).hashCode());
		assertEquals("Checking MANUAL_ENTRY content item equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MANUAL_ENTRY),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MANUAL_ENTRY));
	}
	
	public void TestSourceOfDoseInformation_MPPSContent_Equality() throws Exception  {
		
		assertEquals("Checking MPPS_CONTENT equality",SourceOfDoseInformation.MPPS_CONTENT,SourceOfDoseInformation.MPPS_CONTENT);
		assertEquals("Checking MPPS_CONTENT content item hashCode equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MPPS_CONTENT).hashCode(),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MPPS_CONTENT).hashCode());
		assertEquals("Checking MPPS_CONTENT content item equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MPPS_CONTENT),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MPPS_CONTENT));
	}
	
	public void TestSourceOfDoseInformation_Dosimeter_Equality() throws Exception  {
		
		assertEquals("Checking DOSIMETER equality",SourceOfDoseInformation.DOSIMETER,SourceOfDoseInformation.DOSIMETER);
		assertEquals("Checking DOSIMETER content item hashCode equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DOSIMETER).hashCode(),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DOSIMETER).hashCode());
		assertEquals("Checking DOSIMETER content item equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DOSIMETER),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DOSIMETER));
	}
	
	public void TestSourceOfDoseInformation_CopiedFromImageAttributes_Equality() throws Exception  {
		
		assertEquals("Checking COPIED_FROM_IMAGE_ATTRIBUTES equality",SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES,SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES);
		assertEquals("Checking COPIED_FROM_IMAGE_ATTRIBUTES content item hashCode equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES).hashCode(),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES).hashCode());
		assertEquals("Checking COPIED_FROM_IMAGE_ATTRIBUTES content item equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES));
	}
	
	public void TestSourceOfDoseInformation_ComputedFromImageAttributes_Equality() throws Exception  {
		
		assertEquals("Checking COMPUTED_FROM_IMAGE_ATTRIBUTES equality",SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES,SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES);
		assertEquals("Checking COMPUTED_FROM_IMAGE_ATTRIBUTES content item hashCode equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES).hashCode(),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES).hashCode());
		assertEquals("Checking COMPUTED_FROM_IMAGE_ATTRIBUTES content item equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.COMPUTED_FROM_IMAGE_ATTRIBUTES));
	}
	
	public void TestSourceOfDoseInformation_DerivedFromHumanReadableReports_Equality() throws Exception  {
		
		assertEquals("Checking DERIVED_FROM_HUMAN_READABLE_REPORTS equality",SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS,SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS);
		assertEquals("Checking DERIVED_FROM_HUMAN_READABLE_REPORTS content item hashCode equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS).hashCode(),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS).hashCode());
		assertEquals("Checking DERIVED_FROM_HUMAN_READABLE_REPORTS content item equality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS),SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS));
	}
	
	public void TestSourceOfDoseInformation_ManualEntry_ManualEntry_Inequality() throws Exception  {
		
		assertFalse("Checking AUTOMATED_DATA_COLLECTION versus MANUAL_ENTRY scope inequality",SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION.equals(SourceOfDoseInformation.MANUAL_ENTRY));
		assertFalse("Checking AUTOMATED_DATA_COLLECTION versus MANUAL_ENTRY content item hashCode inequality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION).hashCode() == SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MANUAL_ENTRY).hashCode());
		assertFalse("Checking AUTOMATED_DATA_COLLECTION versus MANUAL_ENTRY content item inequality",SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION).equals(SourceOfDoseInformation.getCodedSequenceItem(SourceOfDoseInformation.MANUAL_ENTRY)));
	}
	
	
}
