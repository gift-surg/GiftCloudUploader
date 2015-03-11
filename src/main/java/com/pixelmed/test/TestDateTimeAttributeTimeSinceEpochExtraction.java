/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.DateTimeAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.ShortStringAttribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TimeAttribute;

import junit.framework.*;

public class TestDateTimeAttributeTimeSinceEpochExtraction extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDateTimeAttributeTimeSinceEpochExtraction(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDateTimeAttributeTimeSinceEpochExtraction.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDateTimeAttributeTimeSinceEpochExtraction");
		
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochNoFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochIllegalZeroDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochSingleDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochThreeDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochSixDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthDayOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthDayHourOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthDayHourMinuteOnlyDefaultTimezone"));
		
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochMissingDateValueDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochNoFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochIllegalZeroDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSingleDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochThreeDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSixDigitFractionDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourOnlyDefaultTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourMinuteOnlyDefaultTimezone"));
		
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochNoFractionExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochIllegalZeroDigitFractionExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSingleDigitFractionExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochThreeDigitFractionExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSixDigitFractionExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearOnlyExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthOnlyExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayOnlyExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourOnlyExplicitUTCTimezone"));
		suite.addTest(new TestDateTimeAttributeTimeSinceEpochExtraction("TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourMinuteOnlyExplicitUTCTimezone"));
		

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochNoFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochIllegalZeroDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochSingleDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.1";
		long expectTime = 100;
		String expectFormattedDateUTC = "19700101000000.100+0000";
		String expectFormattedDatePDT = "19691231170000.100-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochThreeDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.123";
		long expectTime = 123;
		String expectFormattedDateUTC = "19700101000000.123+0000";
		String expectFormattedDatePDT = "19691231170000.123-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochSixDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.123999";
		long expectTime = 123;	// expect truncation, not round
		String expectFormattedDateUTC = "19700101000000.123+0000";
		String expectFormattedDatePDT = "19691231170000.123-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearOnlyDefaultTimezone() throws Exception {
		String dateValue = "1970";
		String timeValue = "";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthOnlyDefaultTimezone() throws Exception {
		String dateValue = "197001";
		String timeValue = "";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthDayOnlyDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthDayHourOnlyDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "00";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromSingleString_AtEpochYearMonthDayHourMinuteOnlyDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(dateValue+timeValue);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(dateValue+timeValue);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}








	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochNoFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}

	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochMissingDateValueDefaultTimezone() throws Exception {
		String dateValue = "";
		String timeValue = "000000";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }

		try {
			long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
			assertTrue("Missing exception for getTimeInMilliSecondsSinceEpoch expected when date absent",false);
		}
		catch (DicomException e) {
		}
		
		try {
			java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
			assertTrue("Missing exception for getDateFromFormattedString expected when date absent",false);
		}
		catch (DicomException e) {
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochIllegalZeroDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSingleDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.1";
		long expectTime = 100;
		String expectFormattedDateUTC = "19700101000000.100+0000";
		String expectFormattedDatePDT = "19691231170000.100-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochThreeDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.123";
		long expectTime = 123;
		String expectFormattedDateUTC = "19700101000000.123+0000";
		String expectFormattedDatePDT = "19691231170000.123-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSixDigitFractionDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.123999";
		long expectTime = 123;	// expect truncation, not round
		String expectFormattedDateUTC = "19700101000000.123+0000";
		String expectFormattedDatePDT = "19691231170000.123-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearOnlyDefaultTimezone() throws Exception {
		String dateValue = "1970";
		String timeValue = "";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthOnlyDefaultTimezone() throws Exception {
		String dateValue = "197001";
		String timeValue = "";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayOnlyDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourOnlyDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "00";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourMinuteOnlyDefaultTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}







	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochNoFractionExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000";
		String timeZoneValue = "+0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochIllegalZeroDigitFractionExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.";
		String timeZoneValue = "+0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSingleDigitFractionExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.1";
		String timeZoneValue = "+0000";
		long expectTime = 100;
		String expectFormattedDateUTC = "19700101000000.100+0000";
		String expectFormattedDatePDT = "19691231170000.100-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochThreeDigitFractionExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.123";
		String timeZoneValue = "+0000";
		long expectTime = 123;
		String expectFormattedDateUTC = "19700101000000.123+0000";
		String expectFormattedDatePDT = "19691231170000.123-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochSixDigitFractionExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "000000.123999";
		String timeZoneValue = "+0000";
		long expectTime = 123;	// expect truncation, not round
		String expectFormattedDateUTC = "19700101000000.123+0000";
		String expectFormattedDatePDT = "19691231170000.123-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearOnlyExplicitUTCTimezone() throws Exception {
		String dateValue = "1970";
		String timeValue = "";
		String timeZoneValue = "+0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthOnlyExplicitUTCTimezone() throws Exception {
		String dateValue = "197001";
		String timeValue = "";
		String timeZoneValue = "+0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayOnlyExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "";
		String timeZoneValue = "+0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourOnlyExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "00";
		String timeZoneValue = "+0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}
	
	public void TestDateTimeAttributeTimeSinceEpochExtraction_FromAttributeList_AtEpochYearMonthDayHourMinuteOnlyExplicitUTCTimezone() throws Exception {
		String dateValue = "19700101";
		String timeValue = "0000";
		String timeZoneValue = "+0000";
		long expectTime = 0;
		String expectFormattedDateUTC = "19700101000000.000+0000";
		String expectFormattedDatePDT = "19691231170000.000-0700";

		AttributeList list = new AttributeList();
		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue(dateValue); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue(timeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue(timeZoneValue); list.put(a); }

		long time = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		
		assertEquals(dateValue+timeValue,expectTime,time);
		
		java.util.Date date = DateTimeAttribute.getDateFromFormattedString(list,TagFromName.SeriesDate,TagFromName.SeriesTime);
		{
			String formattedDate = DateTimeAttribute.getFormattedStringUTC(date);

			assertEquals(dateValue+timeValue,expectFormattedDateUTC,formattedDate);
		}
		{
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT-7"));
			String formattedDate = DateTimeAttribute.getFormattedStringDefaultTimeZone(date);
			java.util.TimeZone.setDefault(null);	// reset after use

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
		{
			String formattedDate = DateTimeAttribute.getFormattedString(date,java.util.TimeZone.getTimeZone("GMT-7"));

			assertEquals(dateValue+timeValue,expectFormattedDatePDT,formattedDate);
		}
	}

}
