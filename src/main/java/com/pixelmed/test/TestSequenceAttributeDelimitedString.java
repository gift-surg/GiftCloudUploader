/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.IntegerStringAttribute;
import com.pixelmed.dicom.LongStringAttribute;
import com.pixelmed.dicom.PersonNameAttribute;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Locale;

import junit.framework.*;

public class TestSequenceAttributeDelimitedString extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSequenceAttributeDelimitedString(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSequenceAttributeDelimitedString.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSequenceAttributeDelimitedString");
		
		suite.addTest(new TestSequenceAttributeDelimitedString("TestSequenceAttributeDelimitedString_EmptySequence"));
		suite.addTest(new TestSequenceAttributeDelimitedString("TestSequenceAttributeDelimitedString_MultipleEmptyItems"));
		suite.addTest(new TestSequenceAttributeDelimitedString("TestSequenceAttributeDelimitedString_MultipleItemsWithDefaultValues"));
		suite.addTest(new TestSequenceAttributeDelimitedString("TestSequenceAttributeDelimitedString_MultipleItemsWithSpecifiedValues"));
		suite.addTest(new TestSequenceAttributeDelimitedString("TestSequenceAttributeDelimitedString_NestedEmptySequence"));
		suite.addTest(new TestSequenceAttributeDelimitedString("TestSequenceAttributeDelimitedString_NestedEmptyItems"));
		suite.addTest(new TestSequenceAttributeDelimitedString("TestSequenceAttributeDelimitedString_NestedNonEmptyItems"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private NumberFormat decimalFormat = new DecimalFormat("######.###");
	
	public void TestSequenceAttributeDelimitedString_EmptySequence() {
		Attribute sa = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		
		assertEquals("Checking getDelimitedStringValuesOrDefault()",                  "<>",sa.getDelimitedStringValuesOrDefault("DEFAULT"));
		assertEquals("Checking getDelimitedStringValuesOrDefault() with NumberFormat","<>",sa.getDelimitedStringValuesOrDefault("DEFAULT",decimalFormat));
	}
	
	public void TestSequenceAttributeDelimitedString_MultipleEmptyItems() {
		Attribute sa = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
		}
		
		assertEquals("Checking getDelimitedStringValuesOrDefault()",                  "<{},{},{}>",sa.getDelimitedStringValuesOrDefault("DEFAULT"));
		assertEquals("Checking getDelimitedStringValuesOrDefault() with NumberFormat","<{},{},{}>",sa.getDelimitedStringValuesOrDefault("DEFAULT",decimalFormat));
	}
	
	public void TestSequenceAttributeDelimitedString_MultipleItemsWithDefaultValues() {
		Attribute sa = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); itemList.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); itemList.put(a); }
		}
		
		assertEquals("Checking getDelimitedStringValuesOrDefault()",                  "<{DEFAULT,DEFAULT,DEFAULT},{DEFAULT},{DEFAULT}>",sa.getDelimitedStringValuesOrDefault("DEFAULT"));
		assertEquals("Checking getDelimitedStringValuesOrDefault() with NumberFormat","<{DEFAULT,DEFAULT,DEFAULT},{DEFAULT},{DEFAULT}>",sa.getDelimitedStringValuesOrDefault("DEFAULT",decimalFormat));
	}
	
	public void TestSequenceAttributeDelimitedString_MultipleItemsWithSpecifiedValues() throws DicomException {
		Attribute sa = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue("39"); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); a.addValue("0.793456789"); a.addValue("0.793456789"); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
		}
		
		assertEquals("Checking getDelimitedStringValuesOrDefault()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{DEFAULT},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT"));
		assertEquals("Checking getDelimitedStringValuesOrDefault() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{DEFAULT},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT",decimalFormat));
	}
	
	public void TestSequenceAttributeDelimitedString_NestedEmptySequence() throws DicomException {
		Attribute sa = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue("39"); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); a.addValue("0.793456789"); a.addValue("0.793456789"); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); itemList.put(a); }
			{
				Attribute nsa = new SequenceAttribute(TagFromName.OriginalAttributesSequence);
				itemList.put(nsa);
			}
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
		}
		
		assertEquals("Checking getDelimitedStringValuesOrDefault()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{DEFAULT,<>},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT"));
		assertEquals("Checking getDelimitedStringValuesOrDefault() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{DEFAULT,<>},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT",decimalFormat));
	}
	
	public void TestSequenceAttributeDelimitedString_NestedEmptyItems() throws DicomException {
		Attribute sa = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue("39"); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); a.addValue("0.793456789"); a.addValue("0.793456789"); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); itemList.put(a); }
			{
				Attribute nsa = new SequenceAttribute(TagFromName.OriginalAttributesSequence);
				itemList.put(nsa);
				AttributeList nestedItemList = new AttributeList();
				((SequenceAttribute)nsa).addItem(nestedItemList);
			}
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
		}
		
		assertEquals("Checking getDelimitedStringValuesOrDefault()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{DEFAULT,<{}>},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT"));
		assertEquals("Checking getDelimitedStringValuesOrDefault() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{DEFAULT,<{}>},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT",decimalFormat));
	}

	
	public void TestSequenceAttributeDelimitedString_NestedNonEmptyItems() throws DicomException {
		Attribute sa = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue("39"); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); a.addValue("0.793456789"); a.addValue("0.793456789"); itemList.put(a); }
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); itemList.put(a); }
			{
				Attribute nsa = new SequenceAttribute(TagFromName.OriginalAttributesSequence);
				itemList.put(nsa);
				AttributeList nestedItemList = new AttributeList();
				((SequenceAttribute)nsa).addItem(nestedItemList);
				{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue("Smith^Jane"); nestedItemList.put(a); }
			}
		}
		{
			AttributeList itemList = new AttributeList();
			((SequenceAttribute)sa).addItem(itemList);
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("Acme Devices"); itemList.put(a); }
		}
		
		assertEquals("Checking getDelimitedStringValuesOrDefault()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT"));
		assertEquals("Checking getDelimitedStringValuesOrDefault() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",sa.getDelimitedStringValuesOrDefault("DEFAULT",decimalFormat));
		
		assertEquals("Checking static getDelimitedStringValuesOrDefault()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrDefault(sa,"DEFAULT"));
		assertEquals("Checking static getDelimitedStringValuesOrDefault() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrDefault(sa,"DEFAULT",decimalFormat));
		
		assertEquals("Checking getDelimitedStringValuesOrEmptyString()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{,<{Smith^Jane}>},{Acme Devices}>",sa.getDelimitedStringValuesOrEmptyString());
		assertEquals("Checking getDelimitedStringValuesOrEmptyString() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{,<{Smith^Jane}>},{Acme Devices}>",sa.getDelimitedStringValuesOrEmptyString(decimalFormat));
		
		assertEquals("Checking static getDelimitedStringValuesOrEmptyString()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrEmptyString(sa));
		assertEquals("Checking static getDelimitedStringValuesOrEmptyString() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrEmptyString(sa,decimalFormat));
	
		AttributeList parentList = new AttributeList();
		parentList.put(sa);
		
		assertEquals("Checking static list getDelimitedStringValuesOrDefault()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrDefault(parentList,TagFromName.ContributingEquipmentSequence,"DEFAULT"));
		assertEquals("Checking static list getDelimitedStringValuesOrDefault() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrDefault(parentList,TagFromName.ContributingEquipmentSequence,"DEFAULT",decimalFormat));
		
		assertEquals("Checking static list getDelimitedStringValuesOrDefault()",                  "<{Acme Devices,39,0.793456789\\0.793456789},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrDefault(parentList,TagFromName.ContributingEquipmentSequence,"DEFAULT"));
		assertEquals("Checking static list getDelimitedStringValuesOrDefault() with NumberFormat","<{Acme Devices,39,0.793\\0.793},{DEFAULT,<{Smith^Jane}>},{Acme Devices}>",Attribute.getDelimitedStringValuesOrDefault(parentList,TagFromName.ContributingEquipmentSequence,"DEFAULT",decimalFormat));
	}

}
