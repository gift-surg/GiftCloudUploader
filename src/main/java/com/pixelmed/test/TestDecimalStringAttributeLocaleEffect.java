/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.TagFromName;
import java.util.Locale;

import junit.framework.*;

public class TestDecimalStringAttributeLocaleEffect extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDecimalStringAttributeLocaleEffect(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDecimalStringAttributeLocaleEffect.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDecimalStringAttributeLocaleEffect");
		
		suite.addTest(new TestDecimalStringAttributeLocaleEffect("TestDecimalStringAttributeLocaleEffect_Double_DefaultLocale"));
		suite.addTest(new TestDecimalStringAttributeLocaleEffect("TestDecimalStringAttributeLocaleEffect_Double_AllAvailableLocales"));
		
		suite.addTest(new TestDecimalStringAttributeLocaleEffect("TestDecimalStringAttributeLocaleEffect_Float_DefaultLocale"));
		suite.addTest(new TestDecimalStringAttributeLocaleEffect("TestDecimalStringAttributeLocaleEffect_Float_AllAvailableLocales"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestDecimalStringAttributeLocaleEffect_Double_DefaultLocale() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		{
			Attribute a = new DecimalStringAttribute(TagFromName.ContourData);
			a.addValue(value1);
			a.addValue(value2);
			a.addValue(value3);
			a.addValue(value4);

			double[] returnedValues = a.getDoubleValues();
			assertEquals("Default",value1,returnedValues[0]);
			assertEquals("Default",value2,returnedValues[1]);
			assertEquals("Default",value3,returnedValues[2]);
			assertEquals("Default",value4,returnedValues[3]);
		}
	}
	
	public void TestDecimalStringAttributeLocaleEffect_Double_AllAvailableLocales() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		Locale[] localesToTest = Locale.getAvailableLocales();
		for (Locale l : localesToTest) {
//System.err.println("Testing effect on DS getDoubleValues() of default locale "+l);
			Locale.setDefault(l);
			Attribute a = new DecimalStringAttribute(TagFromName.ContourData);
			a.addValue(value1);
			a.addValue(value2);
			a.addValue(value3);
			a.addValue(value4);

			double[] returnedValues = a.getDoubleValues();
			assertEquals(l.toString(),value1,returnedValues[0]);
			assertEquals(l.toString(),value2,returnedValues[1]);
			assertEquals(l.toString(),value3,returnedValues[2]);
			assertEquals(l.toString(),value4,returnedValues[3]);
		}
	}
	
	public void TestDecimalStringAttributeLocaleEffect_Float_DefaultLocale() throws Exception {
		float value1 = 39.76f;
		float value2 = -56356.457457f;
		float value3 = 0f;
		float value4 = 7.45345345e-21f;
		
		{
			Attribute a = new DecimalStringAttribute(TagFromName.ContourData);
			a.addValue(value1);
			a.addValue(value2);
			a.addValue(value3);
			a.addValue(value4);

			float[] returnedValues = a.getFloatValues();
			assertEquals("Default",value1,returnedValues[0]);
			assertEquals("Default",value2,returnedValues[1]);
			assertEquals("Default",value3,returnedValues[2]);
			assertEquals("Default",value4,returnedValues[3]);
		}
	}
	
	public void TestDecimalStringAttributeLocaleEffect_Float_AllAvailableLocales() throws Exception {
		float value1 = 39.76f;
		float value2 = -56356.457457f;
		float value3 = 0f;
		float value4 = 7.45345345e-21f;
		
		Locale[] localesToTest = Locale.getAvailableLocales();
		for (Locale l : localesToTest) {
//System.err.println("Testing effect on DS getFloatValues() of default locale "+l);
			Locale.setDefault(l);
			Attribute a = new DecimalStringAttribute(TagFromName.ContourData);
			a.addValue(value1);
			a.addValue(value2);
			a.addValue(value3);
			a.addValue(value4);

			float[] returnedValues = a.getFloatValues();
			assertEquals(l.toString(),value1,returnedValues[0]);
			assertEquals(l.toString(),value2,returnedValues[1]);
			assertEquals(l.toString(),value3,returnedValues[2]);
			assertEquals(l.toString(),value4,returnedValues[3]);
		}
	}

}
