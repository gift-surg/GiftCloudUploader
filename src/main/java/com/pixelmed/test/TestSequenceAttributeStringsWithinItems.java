/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.ShortStringAttribute;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;

import java.util.Locale;

import junit.framework.*;

public class TestSequenceAttributeStringsWithinItems extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSequenceAttributeStringsWithinItems(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSequenceAttributeStringsWithinItems.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSequenceAttributeStringsWithinItems");
		
		suite.addTest(new TestSequenceAttributeStringsWithinItems("TestSequenceAttributeStringsWithinItems_MultipleItems"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSequenceAttributeStringsWithinItems_MultipleItems() throws DicomException {
		Attribute sa = new SequenceAttribute(TagFromName.RequestAttributesSequence);
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new ShortStringAttribute(TagFromName.ScheduledProcedureStepID); a.addValue("SPSID1"); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new ShortStringAttribute(TagFromName.ScheduledProcedureStepID); a.addValue("SPSID2"); itemList.put(a); }
		}
		
		AttributeList list = new AttributeList();
		list.put(sa);
		
		{
			Attribute aScheduledProcedureStepID = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,TagFromName.RequestAttributesSequence, TagFromName.ScheduledProcedureStepID);
			if (aScheduledProcedureStepID != null) {
				String scheduledProcedureStepID = aScheduledProcedureStepID.getSingleStringValueOrEmptyString();
				assertEquals("Checking string","SPSID1",scheduledProcedureStepID);
			}
		}
		
		{
			Attribute ras = list.get(TagFromName.RequestAttributesSequence);
			if (ras != null && ras instanceof SequenceAttribute) {
				SequenceAttribute sras = (SequenceAttribute)ras;
				int n = sras.getNumberOfItems();
				assertEquals("Checking number of strings",2,n);
				for (int i=0; i<n; ++i) {
					Attribute aScheduledProcedureStepID = SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(sras,i,TagFromName.ScheduledProcedureStepID);
					if (aScheduledProcedureStepID != null) {
						String scheduledProcedureStepID = aScheduledProcedureStepID.getSingleStringValueOrEmptyString();
						assertEquals("Checking string","SPSID"+(i+1),scheduledProcedureStepID);
					}
				}
			}
		}
		
		{
			Attribute ras = list.get(TagFromName.RequestAttributesSequence);
			if (ras != null && ras instanceof SequenceAttribute) {
				SequenceAttribute sras = (SequenceAttribute)ras;
				int n = sras.getNumberOfItems();
				assertEquals("Checking number of strings",2,n);
				for (int i=0;i<n;++i) {
					String scheduledProcedureStepID = Attribute.getSingleStringValueOrEmptyString(sras.getItem(i).getAttributeList(),TagFromName.ScheduledProcedureStepID);
					assertEquals("Checking string","SPSID"+(i+1),scheduledProcedureStepID);
				}
			}
		}
		
		{
			String[] scheduledProcedureStepIDs  = SequenceAttribute.getArrayOfSingleStringValueOrEmptyStringOfNamedAttributeWithinSequenceItems(list,TagFromName.RequestAttributesSequence,TagFromName.ScheduledProcedureStepID);
			assertEquals("Checking number of strings",2,scheduledProcedureStepIDs.length);
			for (int i=0;i<scheduledProcedureStepIDs.length;++i) {
				assertEquals("Checking string","SPSID"+(i+1),scheduledProcedureStepIDs[i]);
			}
		}
	}

}
