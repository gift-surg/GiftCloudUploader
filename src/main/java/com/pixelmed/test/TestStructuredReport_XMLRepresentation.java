/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import junit.framework.*;

import com.pixelmed.dicom.*;

import java.util.Locale;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class TestStructuredReport_XMLRepresentation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestStructuredReport_XMLRepresentation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestStructuredReport_XMLRepresentation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestStructuredReport_XMLRepresentation");
		
		suite.addTest(new TestStructuredReport_XMLRepresentation("testStructuredReport_XMLRepresentation_WithReferences"));
		
		return suite;
	}
	
	private static String deviceUID = "1.2.3.4";
	private static String deviceName = "station1";
	private static String manufacturer = "Acme";
	private static String modelName = "Scanner";
	private static String serialNumber = "72349236741";
	private static String location = "Suite1";
	
	private static String operatorName = "Smith^John";
	private static String operatorID = "26354781234";
	private static String physicianName = "Jones^Mary";
	private static String physicianID = "23491234234";
	private static String idIssuer = "99BLA";
	private static String organization = "St. Elsewhere's";

	private static String patientName = "Smith^Mary";
	private static String patientID = "3764913624";
	private static String patientBirthDate = "19600101";
	private static String patientAge = "041Y";
	private static String patientWeight = "68";
	private static String patientSize = "1.55";
	private static String patientSex = "F";
	private static String studyID = "612386812";
	private static String seriesNumber = "12";
	private static String instanceNumber = "38";
	private static String referringPhysicianName = "Jones^Harriet";
	private static String studyDate = "20010203";
	private static String studyTime = "043000";
	
	private static String sopInstanceUID;
	private static String seriesInstanceUID;
	private static String studyInstanceUID;
	
	protected static AttributeList getDefaultAttributeList() throws DicomException {
		Locale.setDefault(Locale.FRENCH);	// forces check that "," is not being used as decimal point in any double to string conversions
		AttributeList list = new AttributeList();
		{
			UIDGenerator u = new UIDGenerator("9999");
			sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
			seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
			studyInstanceUID = u.getNewStudyInstanceUID(studyID);
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
			//{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(modelName); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
//System.err.println("TestStructuredReport_XMLRepresentation.setUp(): compositeInstanceContext.getAttributeList() =\n"+list);
		}
		return list;
	}
	
	protected void tearDown() {
	}
	
	public void testStructuredReport_XMLRepresentation_WithReferences() throws Exception {

		ContentItemFactory cif = new ContentItemFactory();
		ContentItem root = cif.new ContainerContentItem(null/*no parent since root*/,null/*no relationshipType since root*/,new CodedSequenceItem("111036","DCM","Mammography CAD Report"),true/*continuityOfContentIsSeparate*/,"DCMR","5000");
		ContentItem imageLibrary = cif.new ContainerContentItem(root,"CONTAINS",new CodedSequenceItem("111028","DCM","Image Library"),true/*continuityOfContentIsSeparate*/);
		ContentItem image_1_1_1 = cif.new ImageContentItem(imageLibrary,"CONTAINS",null/*conceptName*/,
										SOPClass.DigitalMammographyXRayImageStorageForPresentation,"1.3.6.1.4.1.5962.99.1.993064428.2122236180.1358202762732.2.0",
										0/*referencedFrameNumber*/,0/*referencedSegmentNumber*/,
										null/*presentationStateSOPClassUID*/,null/*presentationStateSOPInstanceUID*/,
										null/*realWorldValueMappingSOPClassUID*/,null/*realWorldValueMappingSOPInstanceUID*/);

		ContentItem findingsSummary = cif.new CodeContentItem(root,"CONTAINS",new CodedSequenceItem("111017","DCM","CAD Processing and Findings Summary"),new CodedSequenceItem("111242","DCM","All algorithms succeeded; with findings"));
		{
			ContentItem individual = cif.new ContainerContentItem(findingsSummary,"CONTAINS",new CodedSequenceItem("111034","DCM","Individual Impression/Recommendation"),true/*continuityOfContentIsSeparate*/);
			cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));

			ContentItem cluster = cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111059","DCM","Single Image Finding"),new CodedSequenceItem("F-01775","SRT","Calcification Cluster"));
			cif.new CodeContentItem(cluster,"HAS CONCEPT MOD",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));
			ContentItem clusterCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111010","DCM","Center"),"POINT",new float[] { 165,2433 });
			new ContentItemWithReference(clusterCoord,"SELECTED FROM","1.1.1");
			cif.new NumericContentItem(cluster,"HAS PROPERTIES",new CodedSequenceItem("111038","DCM","Number of calcifications"),10,new CodedSequenceItem("111150","1","no units"));

			ContentItem single = cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111059","DCM","Single Image Finding"),new CodedSequenceItem("F-01776","SRT","Individual Calcification"));
			cif.new CodeContentItem(single,"HAS CONCEPT MOD",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));
			ContentItem singleCenterCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111010","DCM","Center"),"POINT",new float[] { 198,2389 });
			new ContentItemWithReference(singleCenterCoord,"SELECTED FROM","1.1.1");
			ContentItem singleOutlineCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111041","DCM","Outline"),"POLYLINE",new float[] { 199,2388,198,2388,197,2388,197,2389,197,2390,198,2390,199,2390,200,2390,200,2389 });
			new ContentItemWithReference(singleOutlineCoord,"SELECTED FROM","1.1.1");
		}
		
		StructuredReport sr = new StructuredReport(root);
		AttributeList list = sr.getAttributeList();
		list.putAll(getDefaultAttributeList());
		
//System.err.println(sr);
		
		Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(list);
		
		AttributeList roundTripList = new XMLRepresentationOfStructuredReportObjectFactory().getAttributeList(srDocument);

		assertEquals("Checking round trip AttributeList",list,roundTripList);
	}
}
