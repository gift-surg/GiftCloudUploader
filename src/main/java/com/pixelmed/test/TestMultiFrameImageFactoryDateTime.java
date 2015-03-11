/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import java.util.HashMap;
import java.util.Map;

import junit.framework.*;

public class TestMultiFrameImageFactoryDateTime extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestMultiFrameImageFactoryDateTime(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDateTimeAttributeTimeSinceEpochExtraction.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestMultiFrameImageFactoryDateTime");
		
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateAndTimeSameDefaultTimezone"));
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateAndTimeDifferentDefaultTimezone"));
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateTimeSameDefaultTimezone"));
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentDefaultTimezone"));
		
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateAndTimeSameTimezoneAttribute"));
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateAndTimeDifferentTimezoneAttribute"));
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateTimeSameTimezoneAttribute"));
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentTimezoneAttribute"));

		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentAndExplicitTimezoneDifferentFromTimezoneAttribute"));
		suite.addTest(new TestMultiFrameImageFactoryDateTime("TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentAndExplicitTimezoneDifferentFromDefaultTimezone"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateAndTimeSameDefaultTimezone() throws Exception {
		String     contentDateValue = "20130217";
		String     contentTimeValue = "114501.765";
		String acquisitionDateValue = "20130217";
		String acquisitionTimeValue = "114439.234";
		
		String expectedTopLevelAcquisitionDateTimeValue = "20130217114439.234";
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list1.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list2.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedTopLevelAcquisitionDateTimeValue,aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f+" is absent",aContentTime == null);
				}
			}
		}
	}
	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateAndTimeDifferentDefaultTimezone() throws Exception {
		String     contentDateValue1 = "20130217";
		String     contentTimeValue1 = "114501.765";						// the earliest
		String acquisitionDateValue1 = "20130217";
		String acquisitionTimeValue1 = "114439.234";						// the earliest
		
		String     contentDateValue2 = "20130217";
		String     contentTimeValue2 = "114502.897";
		String acquisitionDateValue2 = "20130217";
		String acquisitionTimeValue2 = "114441.123";

		String[] expectedFrameContentTimeValue = { contentTimeValue1, contentTimeValue2 };
		String[] expectedFrameAcquisitionDateTimeValue = { "20130217114439.234", "20130217114441.123" };
		
		String expectedTopLevelAcquisitionDateTimeValue = "20130217114439.234";	// the earliest
		
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue1); list1.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue1); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue2); list2.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue2); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedFrameAcquisitionDateTimeValue[f],aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f,aContentTime != null);
					assertEquals("ContentTime for frame"+f,expectedFrameContentTimeValue[f],aContentTime.getSingleStringValueOrEmptyString());
				}
			}
		}
	}

	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateTimeSameDefaultTimezone() throws Exception {
		String         contentDateValue = "20130217";
		String         contentTimeValue = "114501.765";
		String acquisitionDateTimeValue = "20130217114439.234";
		
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list1.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateTimeValue); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list2.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",acquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,acquisitionDateTimeValue,aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f+" is absent",aContentTime == null);
				}
			}
		}
	}
	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentDefaultTimezone() throws Exception {
		String         contentDateValue1 = "20130217";
		String         contentTimeValue1 = "114501.765";								// the earliest
		String acquisitionDateTimeValue1 = "20130217114439.234";						// the earliest
		
		String         contentDateValue2 = "20130217";
		String         contentTimeValue2 = "114502.897";
		String acquisitionDateTimeValue2 = "20130217114441.123";

		String[] expectedFrameContentTimeValue = { contentTimeValue1, contentTimeValue2 };
		String[] expectedFrameAcquisitionDateTimeValue = { acquisitionDateTimeValue1, acquisitionDateTimeValue2 };
		
		String expectedTopLevelAcquisitionDateTimeValue = acquisitionDateTimeValue1;	// the earliest
		
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue1); list1.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue1); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue2); list2.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue2); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",acquisitionDateTimeValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedFrameAcquisitionDateTimeValue[f],aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f,aContentTime != null);
					assertEquals("ContentTime for frame"+f,expectedFrameContentTimeValue[f],aContentTime.getSingleStringValueOrEmptyString());
				}
			}
		}
	}







	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateAndTimeSameTimezoneAttribute() throws Exception {
		String     contentDateValue = "20130217";
		String     contentTimeValue = "114501.765";
		String acquisitionDateValue = "20130217";
		String acquisitionTimeValue = "114439.234";
		String       timezoneString = "-0800";
		
		String expectedTopLevelAcquisitionDateTimeValue = "20130217114439.234-0800";
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list1.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue); list1.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list2.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue); list2.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			assertEquals("TimezoneOffsetFromUTC in top level dataset",timezoneString,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.TimezoneOffsetFromUTC));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedTopLevelAcquisitionDateTimeValue,aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f+" is absent",aContentTime == null);
				}
			}
		}
	}
	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateAndTimeDifferentTimezoneAttribute() throws Exception {
		String     contentDateValue1 = "20130217";
		String     contentTimeValue1 = "114501.765";						// the earliest
		String acquisitionDateValue1 = "20130217";
		String acquisitionTimeValue1 = "114439.234";						// the earliest
		
		String     contentDateValue2 = "20130217";
		String     contentTimeValue2 = "114502.897";
		String acquisitionDateValue2 = "20130217";
		String acquisitionTimeValue2 = "114441.123";

		String       timezoneString = "-0800";

		String[] expectedFrameContentTimeValue = { contentTimeValue1, contentTimeValue2 };
		String[] expectedFrameAcquisitionDateTimeValue = { "20130217114439.234-0800", "20130217114441.123-0800" };
		
		String expectedTopLevelAcquisitionDateTimeValue = "20130217114439.234-0800";	// the earliest
		
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue1); list1.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue1); list1.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue2); list2.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(acquisitionTimeValue2); list2.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			assertEquals("TimezoneOffsetFromUTC in top level dataset",timezoneString,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.TimezoneOffsetFromUTC));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedFrameAcquisitionDateTimeValue[f],aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f,aContentTime != null);
					assertEquals("ContentTime for frame"+f,expectedFrameContentTimeValue[f],aContentTime.getSingleStringValueOrEmptyString());
				}
			}
		}
	}

	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeSameAndAcquisitionDateTimeSameTimezoneAttribute() throws Exception {
		String         contentDateValue = "20130217";
		String         contentTimeValue = "114501.765";
		String acquisitionDateTimeValue = "20130217114439.234";
		String           timezoneString = "-0800";
		
		int expectedNumberOfFrames = 2;

		String expectedTopLevelAcquisitionDateTimeValue = "20130217114439.234-0800";	// the earliest

		UIDGenerator u = new UIDGenerator();

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list1.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDate); a.addValue(acquisitionDateTimeValue); list1.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue); list2.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue); list2.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			assertEquals("TimezoneOffsetFromUTC in top level dataset",timezoneString,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.TimezoneOffsetFromUTC));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedTopLevelAcquisitionDateTimeValue,aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f+" is absent",aContentTime == null);
				}
			}
		}
	}
	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentTimezoneAttribute() throws Exception {
		String         contentDateValue1 = "20130217";
		String         contentTimeValue1 = "114501.765";								// the earliest
		String acquisitionDateTimeValue1 = "20130217114439.234";						// the earliest
		
		String         contentDateValue2 = "20130217";
		String         contentTimeValue2 = "114502.897";
		String acquisitionDateTimeValue2 = "20130217114441.123";

		String            timezoneString = "-0800";

		String[] expectedFrameContentTimeValue = { contentTimeValue1, contentTimeValue2 };
		String[] expectedFrameAcquisitionDateTimeValue = { "20130217114439.234-0800", "20130217114441.123-0800" };
		
		String expectedTopLevelAcquisitionDateTimeValue = "20130217114439.234-0800";	// the earliest
		
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue1); list1.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue1); list1.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue2); list2.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue2); list2.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			assertEquals("TimezoneOffsetFromUTC in top level dataset",timezoneString,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.TimezoneOffsetFromUTC));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedFrameAcquisitionDateTimeValue[f],aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f,aContentTime != null);
					assertEquals("ContentTime for frame"+f,expectedFrameContentTimeValue[f],aContentTime.getSingleStringValueOrEmptyString());
				}
			}
		}
	}
	
	
	
	
	
	
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentAndExplicitTimezoneDifferentFromTimezoneAttribute() throws Exception {
		String         contentDateValue1 = "20130217";
		String         contentTimeValue1 = "114501.765";								// the earliest
		String acquisitionDateTimeValue1 = "20130217114439.234-0830";					// the earliest
		
		String         contentDateValue2 = "20130217";
		String         contentTimeValue2 = "114502.897";
		String acquisitionDateTimeValue2 = "20130217114441.123-0830";

		String            timezoneString = "-0800";

		String[] expectedFrameContentTimeValue = { contentTimeValue1, contentTimeValue2 };
		String[] expectedFrameAcquisitionDateTimeValue = { "20130217114439.234-0830", "20130217114441.123-0830" };
		
		String expectedTopLevelAcquisitionDateTimeValue = "20130217121439.234-0800";	// the earliest ... note that it is in the TimezoneOffsetFromUTC !
		
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue1); list1.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue1); list1.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list1.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue2); list2.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue2); list2.put(a); }
			
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timezoneString); list2.put(a); }
		
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			assertEquals("TimezoneOffsetFromUTC in top level dataset",timezoneString,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.TimezoneOffsetFromUTC));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedFrameAcquisitionDateTimeValue[f],aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f,aContentTime != null);
					assertEquals("ContentTime for frame"+f,expectedFrameContentTimeValue[f],aContentTime.getSingleStringValueOrEmptyString());
				}
			}
		}
	}
		
	public void TestMultiFrameImageFactoryDateTime_ContentDateAndTimeDifferentAndAcquisitionDateTimeDifferentAndExplicitTimezoneDifferentFromDefaultTimezone() throws Exception {
		String         contentDateValue1 = "20130217";
		String         contentTimeValue1 = "114501.765";								// the earliest
		String acquisitionDateTimeValue1 = "20130217114439.234-0830";					// the earliest
		
		String         contentDateValue2 = "20130217";
		String         contentTimeValue2 = "114502.897";
		String acquisitionDateTimeValue2 = "20130217114441.123-0830";

		String[] expectedFrameContentTimeValue = { contentTimeValue1, contentTimeValue2 };
		String[] expectedFrameAcquisitionDateTimeValue = { "20130217114439.234-0830", "20130217114441.123-0830" };
		
		String expectedTopLevelAcquisitionDateTimeValue = "20130217201439.234";			// the earliest ... note that it is in UTC !
		
		int expectedNumberOfFrames = 2;

		UIDGenerator u = new UIDGenerator();	

		Map<String,AttributeList> listsBySOPInstanceUID = new HashMap<String,AttributeList>();
		SetOfFrameSets setOfFrameSets = new SetOfFrameSets();

		AttributeList list1 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list1.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list1.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list1.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue1); list1.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue1); list1.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue1); list1.put(a); }
					
			listsBySOPInstanceUID.put(sopInstanceUID,list1);
			setOfFrameSets.insertIntoFrameSets(list1);
		}

		AttributeList list2 = new AttributeList();
		{
			String sopInstanceUID = u.getAnotherNewUID();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(1); list2.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(1); list2.put(a); }
			{ Attribute a = new OtherByteAttribute(TagFromName.PixelData); list1.put(a); byte[] bytes = new byte[1]; a.setValues(bytes); list2.put(a); }
		
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); a.addValue(contentDateValue2); list2.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime); a.addValue(contentTimeValue2); list2.put(a); }
			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(acquisitionDateTimeValue2); list2.put(a); }
					
			listsBySOPInstanceUID.put(sopInstanceUID,list2);
			setOfFrameSets.insertIntoFrameSets(list2);
		}
		
		Map<String,HierarchicalImageReference> multiFrameReferenceBySingleFrameUID = new HashMap<String,HierarchicalImageReference>();
		for (FrameSet frameSet : setOfFrameSets) {
			AttributeList enhancedList = MultiFrameImageFactory.createEnhancedImageFromFrameSet(frameSet,null/*filesBySOPInstanceUID*/,listsBySOPInstanceUID,multiFrameReferenceBySingleFrameUID);
//System.err.println(enhancedList);
			assertEquals("ContentDate in top level dataset",contentDateValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentDate));
			assertEquals("ContentTime in top level dataset",contentTimeValue1,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.ContentTime));
			assertEquals("AcquisitionDateTime in top level dataset",expectedTopLevelAcquisitionDateTimeValue,Attribute.getSingleStringValueOrEmptyString(enhancedList,TagFromName.AcquisitionDateTime));
			{
				{
					SequenceAttribute aUnassignedSharedConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(enhancedList,TagFromName.SharedFunctionalGroupsSequence,TagFromName.UnassignedSharedConvertedAttributesSequence));
					assertTrue("aUnassignedSharedConvertedAttributesSequence",aUnassignedSharedConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedSharedConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime is absent",aContentTime == null);
				}
			}
			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(enhancedList,TagFromName.NumberOfFrames,-1);
			assertEquals("Number of Frames",expectedNumberOfFrames,numberOfFrames);
			for (int f=0; f<numberOfFrames; ++f) {
//System.err.println("Checking frame "+f);
				{
					SequenceAttribute aFrameContentSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.FrameContentSequence));
					assertTrue("FrameContentSequence for frame "+f,aFrameContentSequence != null);
					Attribute aFrameAcquisitionDateTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aFrameContentSequence,TagFromName.FrameAcquisitionDateTime);
					assertTrue("FrameAcquisitionDateTime for frame "+f,aFrameAcquisitionDateTime != null);
					assertEquals("FrameAcquisitionDateTime for frame"+f,expectedFrameAcquisitionDateTimeValue[f],aFrameAcquisitionDateTime.getSingleStringValueOrEmptyString());
				}
				{
					SequenceAttribute aUnassignedPerFrameConvertedAttributesSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(enhancedList,TagFromName.PerFrameFunctionalGroupsSequence,f,TagFromName.UnassignedPerFrameConvertedAttributesSequence));
					assertTrue("UnassignedPerFrameConvertedAttributesSequence for frame "+f,aUnassignedPerFrameConvertedAttributesSequence != null);
					Attribute aContentTime = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aUnassignedPerFrameConvertedAttributesSequence,TagFromName.ContentTime);
					assertTrue("ContentTime for frame "+f,aContentTime != null);
					assertEquals("ContentTime for frame"+f,expectedFrameContentTimeValue[f],aContentTime.getSingleStringValueOrEmptyString());
				}
			}
		}
	}
}


