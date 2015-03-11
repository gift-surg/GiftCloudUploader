/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.ByteArrayOutputStream;

public class TestRepairAttributeValues extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestRepairAttributeValues(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestRepairAttributeValues.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestRepairAttributeValues");
		
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_CodeString_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_CodeString_Trim"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_CodeString_TooLong"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_CodeString_ControlCharacter"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_CodeString_EmbeddedBadCharacter"));
		
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortString_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortString_Trim"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortString_TooLong"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortString_ControlCharacter"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortString_EmbeddedBackslash"));
		
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_LongString_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_LongString_Trim"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_LongString_TooLong"));
		
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortText_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortText_Trim"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_ShortText_TooLong"));
		
		//suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DecimalString_Good"));
		//suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DecimalString_Trim"));
		//suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DecimalString_TooLong"));
		//suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DecimalString_ControlCharacter"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DecimalString_EmbeddedBadCharacter"));
		
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_IntegerString_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_IntegerString_Trim"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_IntegerString_TooLong"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_IntegerString_TooLargeAnIntegerValue"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_IntegerString_TooSmallAnIntegerValue"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_IntegerString_ControlCharacter"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_IntegerString_EmbeddedBadCharacter"));
		
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_UniqueIdentifier_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_UniqueIdentifier_Trim"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_UniqueIdentifier_TooLong"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_UniqueIdentifier_BadTrailingSpace"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_UniqueIdentifier_EmbeddedBadCharacter"));

		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_AgeString_GoodYear"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_AgeString_GoodMonth"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_AgeString_GoodWeek"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_AgeString_GoodDay"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_AgeString_BadUnit"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_AgeString_TooShort"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_AgeString_TooLong"));

		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Date_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Date_TooShort"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Date_TooLong"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Date_BadPeriods"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Date_BadSlashes"));

		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DateTime_GoodWithTimezoneAndFullFraction"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DateTime_GoodWithNoTimezoneAndNoFraction"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DateTime_TooLong"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_DateTime_EmbeddedBadCharacter"));
		
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Time_Good"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Time_TooShort"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Time_TooLong"));
		suite.addTest(new TestRepairAttributeValues("TestRepairAttributeValues_Time_BadColons"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestRepairAttributeValues_CodeString_Good() throws Exception {
		{
			String inputValue = "ORIGINAL";
			String expectValue = "ORIGINAL";
			Attribute a = new CodeStringAttribute(TagFromName.ImageType);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_CodeString_Trim() throws Exception {
		{
			String inputValue = "         ORIGINAL            ";
			String expectValue = "ORIGINAL";
			Attribute a = new CodeStringAttribute(TagFromName.ImageType);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_CodeString_TooLong() throws Exception {
		{
			String inputValue = "REALLY_REALLY ORIGINAL";
			String expectValue = "REALLY_REALLY ORIGINAL";		// not repairable
			Attribute a = new CodeStringAttribute(TagFromName.ImageType);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_CodeString_ControlCharacter() throws Exception {
		{
			String inputValue = "ORIGINAL\nTOO";
			String expectValue = "ORIGINAL\nTOO";		// not repairable
			Attribute a = new CodeStringAttribute(TagFromName.ImageType);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_CodeString_EmbeddedBadCharacter() throws Exception {
		{
			String inputValue = "ORIGINAL-TOO";
			String expectValue = "ORIGINAL-TOO";		// not repairable
			Attribute a = new CodeStringAttribute(TagFromName.ImageType);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_ShortString_Good() throws Exception {
		{
			String inputValue = "Hello, world.";
			String expectValue = "Hello, world.";
			Attribute a = new ShortStringAttribute(TagFromName.StudyID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_ShortString_Trim() throws Exception {
		{
			String inputValue = "         Hello, world.            ";
			String expectValue = "Hello, world.";
			Attribute a = new ShortStringAttribute(TagFromName.StudyID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_ShortString_TooLong() throws Exception {
		{
			String inputValue = "It was the best of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way";
			String expectValue = "It was the best";		// 15 not 16 characters, because trailing space trimmed
			Attribute a = new ShortStringAttribute(TagFromName.StudyID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_ShortString_ControlCharacter() throws Exception {
		{
			String inputValue = "Hello,\nworld.";
			String expectValue = "Hello, world.";
			Attribute a = new ShortStringAttribute(TagFromName.ImageType);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_ShortString_EmbeddedBackslash() throws Exception {
		{
			String inputValue = "Hello,\\world.";
			String expectValue = "Hello, world.";
			Attribute a = new ShortStringAttribute(TagFromName.ImageType);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is now invalid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	
	
	public void TestRepairAttributeValues_LongString_Good() throws Exception {
		{
			String inputValue = "It was the best of times, it was the worst of times, it was the ";
			String expectValue = "It was the best of times, it was the worst of times, it was the";
			Attribute a = new LongStringAttribute(TagFromName.StudyDescription);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_LongString_Trim() throws Exception {
		{
			String inputValue = "         It was the best of times, it was the worst of times, it was the            ";
			String expectValue = "It was the best of times, it was the worst of times, it was the";		// trimmed
			Attribute a = new LongStringAttribute(TagFromName.StudyDescription);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_LongString_TooLong() throws Exception {
		{
			String inputValue = "It was the best of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way";
			String expectValue = "It was the best of times, it was the worst of times, it was the";		// 63 not 64 characters, because trailing space trimmed
			Attribute a = new LongStringAttribute(TagFromName.StudyDescription);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	private static String longerThan1024 = "It was the best of times,\n"
			+ "it was the worst of times,\n"
			+ "it was the age of wisdom,\n"
			+ "it was the age of foolishness,\n"
			+ "it was the epoch of belief,\n"
			+ "it was the epoch of incredulity,\n"
			+ "it was the season of Light,\n"
			+ "it was the season of Darkness,\n"
			+ "it was the spring of hope,\n"
			+ "it was the winter of despair,\n"
			+ "we had everything before us,\n"
			+ "we had nothing before us,\n"
			+ "we were all going direct to Heaven,\n"
			+ "we were all going direct the other way--\n"
			+ "in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.\n"
			+ "\n"
			+ "There were a king with a large jaw and a queen with a plain face, on the throne of England; there were a king with a large jaw and a queen with a fair face, on the throne of France. In both countries it was clearer than crystal to the lords of the State preserves of loaves and fishes, that things in general were settled for ever.\n"
			+ "\n"
			+ "It was the year of Our Lord one thousand seven hundred and seventy-five. Spiritual revelations were conceded to England at that favoured period, as at this. Mrs. Southcott had recently attained her five-and-twentieth blessed birthday, of whom a prophetic private in the Life Guards had heralded the sublime appearance by announcing that arrangements were made for the swallowing up of London and Westminster. Even the Cock-lane ghost had been laid only a round dozen of years, after rapping out its messages, as the spirits of this very year last past (supernaturally deficient in originality) rapped out theirs. Mere messages in the earthly order of events had lately come to the English Crown and People, from a congress of British subjects in America: which, strange to relate, have proved more important to the human race than any communications yet received through any of the chickens of the Cock-lane brood.\n"
			;

	public void TestRepairAttributeValues_ShortText_Good() throws Exception {
		{
			String inputValue = longerThan1024.substring(0,1024);
			String expectValue = inputValue;
			Attribute a = new ShortTextAttribute(TagFromName.ReasonForStudy);
			a.addValue(inputValue);
			assertTrue("Is valid",a.isValid());
			assertEquals("Checking ",expectValue,a.getStringValues()[0]);
		}
	}
	
	public void TestRepairAttributeValues_ShortText_Trim() throws Exception {
		{
			String inputValue = "         "+longerThan1024.substring(0,1024)+"            ";
			Attribute a = new ShortTextAttribute(TagFromName.ReasonForStudy);
			a.addValue(inputValue);
			assertTrue("Is not valid",!a.isValid());
			assertEquals("Checking ",inputValue,a.getStringValues()[0]);	// i.e., no trimming
		}
	}

	public void TestRepairAttributeValues_ShortText_TooLong() throws Exception {
		{
			String inputValue = longerThan1024;
			String expectValue = longerThan1024.substring(0,1024).trim();
			Attribute a = new ShortTextAttribute(TagFromName.ReasonForStudy);
			a.addValue(inputValue);
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertEquals("Checking ",expectValue,a.getStringValues()[0]);
		}
	}
	
	
	public void TestRepairAttributeValues_DecimalString_Good() throws Exception {
		{
			String inputValue = "-23456.890123456";
			String expectValue = "-23456.890123456";
			Attribute a = new DecimalStringAttribute(TagFromName.ImagePositionPatient);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_DecimalString_Trim() throws Exception {
		{
			String inputValue = "         -23456.890123456            ";
			String expectValue = "-23456.890123456";
			Attribute a = new DecimalStringAttribute(TagFromName.ImagePositionPatient);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_DecimalString_TooLong() throws Exception {
		{
			String inputValue = "-23456.8901234567";
			String expectValue = "-23456.890123457";	// Note that rounding upwards is expected, rather than truncation
			Attribute a = new DecimalStringAttribute(TagFromName.ImagePositionPatient);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_DecimalString_ControlCharacter() throws Exception {
		{
			String inputValue = "-23456.89012345\n";
			String expectValue = "-23456.89012345";
			Attribute a = new DecimalStringAttribute(TagFromName.ImagePositionPatient);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_DecimalString_EmbeddedBadCharacter() throws Exception {
		{
			String inputValue = "-23456.8X0123456";
			String expectValue = "-23456.8X0123456";
			Attribute a = new DecimalStringAttribute(TagFromName.ImagePositionPatient);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	
	public void TestRepairAttributeValues_IntegerString_Good() throws Exception {
		{
			String inputValue = "-2147483648";	// -2^31, 11 bytes, plus 1 for even padding
			String expectValue = "-2147483648";
			Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_IntegerString_Trim() throws Exception {
		{
			String inputValue = "         -2147483648            ";
			String expectValue = "-2147483648";
			Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_IntegerString_TooLong() throws Exception {
		{
			String inputValue = "-9223372036854775808";
			String expectValue = "-9223372036854775808";	// Note that no truncation can be performed
			Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_IntegerString_TooLargeAnIntegerValue() throws Exception {
		{
			String inputValue = "4294967295";	// Only 11 bytes, but exceeds permitted value range
			String expectValue = "4294967295";	// Note that no truncation can be performed
			Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_IntegerString_TooSmallAnIntegerValue() throws Exception {
		{
			String inputValue = "-2147483649";	// -2^31 - 1, 11 bytes, plus 1 for even padding
			String expectValue = "-2147483649";	// Note that no truncation can be performed
			Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_IntegerString_ControlCharacter() throws Exception {
		{
			String inputValue = "-2147483648\n";	// -2^31, 11 bytes, plus \n for even padding
			String expectValue = "-2147483648";
			Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_IntegerString_EmbeddedBadCharacter() throws Exception {
		{
			String inputValue = "-214748X3648";
			String expectValue = "-214748X3648";
			Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is invalid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	
	public void TestRepairAttributeValues_UniqueIdentifier_Good() throws Exception {
		{
			String inputValue = "123456789.123456789.123456789.123456789.123456789.123456789.1234";
			String expectValue = "123456789.123456789.123456789.123456789.123456789.123456789.1234";
			Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_UniqueIdentifier_Trim() throws Exception {
		{
			String inputValue = "         123456789.123456789.123456789.123456789.123456789.123456789.1234            ";
			String expectValue = "123456789.123456789.123456789.123456789.123456789.123456789.1234";		// trimmed
			Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_UniqueIdentifier_TooLong() throws Exception {
		{
			String inputValue = "123456789.123456789.123456789.123456789.123456789.123456789.12345";
			String expectValue = "123456789.123456789.123456789.123456789.123456789.123456789.12345";
			Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_UniqueIdentifier_BadTrailingSpace() throws Exception {
		{
			String inputValue = "123456789.123456789.123456789.123456789.123456789.123456789.123 ";		// space should be zero value for padding
			String expectValue = "123456789.123456789.123456789.123456789.123456789.123456789.123";
			Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is now valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_UniqueIdentifier_EmbeddedBadCharacter() throws Exception {
		{
			String inputValue = "123456789.123456789.123456789.123456789.123456789.123456789.X123";		// space should be zero value for padding
			String expectValue = "123456789.123456789.123456789.123456789.123456789.123456789.X123";
			Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_AgeString_GoodYear() throws Exception {
		{
			String inputValue = "039Y";
			String expectValue = "039Y";
			Attribute a = new AgeStringAttribute(TagFromName.PatientAge);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_AgeString_GoodMonth() throws Exception {
		{
			String inputValue = "011M";
			String expectValue = "011M";
			Attribute a = new AgeStringAttribute(TagFromName.PatientAge);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_AgeString_GoodWeek() throws Exception {
		{
			String inputValue = "032W";
			String expectValue = "032W";
			Attribute a = new AgeStringAttribute(TagFromName.PatientAge);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_AgeString_GoodDay() throws Exception {
		{
			String inputValue = "021D";
			String expectValue = "021D";
			Attribute a = new AgeStringAttribute(TagFromName.PatientAge);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_AgeString_BadUnit() throws Exception {
		{
			String inputValue = "039X";
			String expectValue = "039X";
			Attribute a = new AgeStringAttribute(TagFromName.PatientAge);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_AgeString_TooShort() throws Exception {
		{
			String inputValue = "39Y";
			String expectValue = "39Y";
			Attribute a = new AgeStringAttribute(TagFromName.PatientAge);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	
	public void TestRepairAttributeValues_AgeString_TooLong() throws Exception {
		{
			String inputValue = "0039Y";
			String expectValue = "0039Y";
			Attribute a = new AgeStringAttribute(TagFromName.PatientAge);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Date_Good() throws Exception {
		{
			String inputValue = "20140203";
			String expectValue = "20140203";
			Attribute a = new DateAttribute(TagFromName.StudyDate);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Date_TooShort() throws Exception {
		{
			String inputValue = "201402";
			String expectValue = "201402";
			Attribute a = new DateAttribute(TagFromName.StudyDate);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Date_TooLong() throws Exception {
		{
			String inputValue = "201402031";
			String expectValue = "201402031";
			Attribute a = new DateAttribute(TagFromName.StudyDate);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Date_BadPeriods() throws Exception {
		{
			String inputValue = "2014.02.03";
			String expectValue = "20140203";
			Attribute a = new DateAttribute(TagFromName.StudyDate);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is now valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Date_BadSlashes() throws Exception {
		{
			String inputValue = "2014/02/03";
			String expectValue = "20140203";
			Attribute a = new DateAttribute(TagFromName.StudyDate);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is now valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_DateTime_GoodWithTimezoneAndFullFraction() throws Exception {
		{
			String inputValue = "20140203191536.123456-0500";
			String expectValue = "20140203191536.123456-0500";
			Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_DateTime_GoodWithNoTimezoneAndNoFraction() throws Exception {
		{
			String inputValue = "20140203191536";
			String expectValue = "20140203191536";
			Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_DateTime_TooLong() throws Exception {
		{
			String inputValue = "         20140203191536.123456-0500            ";
			String expectValue = "20140203191536.123456-0500";
			Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is now valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_DateTime_EmbeddedBadCharacter() throws Exception {
		{
			String inputValue = "20140203:191536";		// N.B., if we uses a period, it would pass, until we implement DateTimeAttribute.areValuesWellFormed() properly
			String expectValue = "20140203:191536";
			Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}
	

	public void TestRepairAttributeValues_Time_Good() throws Exception {
		{
			String inputValue = "193217.123456 ";	// note trailing padding
			String expectValue = "193217.123456";
			Attribute a = new TimeAttribute(TagFromName.StudyTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",inputValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Time_TooShort() throws Exception {
		{
			String inputValue = "1";
			String expectValue = "1";
			Attribute a = new TimeAttribute(TagFromName.StudyTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Time_TooLong() throws Exception {
		{
			String inputValue = "193217.12345678";		// needs to be one longer than with padding to fail until we distinguish trailing space from invalid extra fractional digit :(
			String expectValue = "193217.12345678";
			Attribute a = new TimeAttribute(TagFromName.StudyTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair unsuccessful",!a.repairValues());
			assertTrue("Is still not valid",!a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}

	public void TestRepairAttributeValues_Time_BadColons() throws Exception {
		{
			String inputValue = "19:32:17.123";
			String expectValue = "193217.123";
			Attribute a = new TimeAttribute(TagFromName.StudyTime);
			for (int i=0; i<3; ++i) {
				a.addValue(inputValue);
			}
			assertTrue("Is not valid",!a.isValid());
			assertTrue("Repair successful",a.repairValues());
			assertTrue("Is now valid",a.isValid());
			for (int i=0; i<3; ++i) {
				assertEquals("Checking ",expectValue,a.getOriginalStringValues()[i]);
				assertEquals("Checking ",expectValue,a.getStringValues()[i]);
			}
		}
	}


	
}
