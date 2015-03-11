/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.util.Locale;

public class TestNumericContentItemLocaleEffect extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestNumericContentItemLocaleEffect(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestNumericContentItemLocaleEffect.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestNumericContentItemLocaleEffect");
		
		suite.addTest(new TestNumericContentItemLocaleEffect("TestNumericContentItemLocaleEffect_AllAvailableLocales"));
		
		return suite;
	}
	
	ContentItemFactory cf;
		
	protected void setUp() {
		cf = new ContentItemFactory();
	}
	
	protected void tearDown() {
	}
	
	public void TestNumericContentItemLocaleEffect_AllAvailableLocales() throws Exception {
		String cvConcept = "G-A22A";
		String csdConcept = "SRT";
		String cmConcept = "Length";
		
		String cvUnits = "mm";
		String csdUnits = "UCUM";
		String cmUnits = "mm";
		
		double value = -7.45345345e-21d;	// challenging because of -ve sign, decimal point, case of exponent, and -ve exponent
		
		Locale[] localesToTest = Locale.getAvailableLocales();
		for (Locale l : localesToTest) {
//System.err.println("Testing effect on NumericContentItem constructor with double of default locale "+l);
			Locale.setDefault(l);
			
			ContentItemFactory.NumericContentItem ci = cf.makeNumericContentItem(null/*parent*/,null/*relationshipType*/,
				new CodedSequenceItem(cvConcept,csdConcept,cmConcept),
				value,
				new CodedSequenceItem(cvUnits,csdUnits,cmUnits));
	
			assertEquals(l.toString(),value,Double.valueOf(ci.getNumericValue()).doubleValue());
		}
	}
	
}
