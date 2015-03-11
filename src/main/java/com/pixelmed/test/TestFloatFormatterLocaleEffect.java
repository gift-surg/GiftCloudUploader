/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.FloatFormatter;

import java.text.NumberFormat;
import java.util.Locale;

import junit.framework.*;

public class TestFloatFormatterLocaleEffect extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestFloatFormatterLocaleEffect(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestFloatFormatterLocaleEffect.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestFloatFormatterLocaleEffect");
		
		suite.addTest(new TestFloatFormatterLocaleEffect("TestFloatFormatterLocaleEffect_Double_DefaultLocale"));
		suite.addTest(new TestFloatFormatterLocaleEffect("TestFloatFormatterLocaleEffect_Double_DefaultFrenchLocale"));
		suite.addTest(new TestFloatFormatterLocaleEffect("TestFloatFormatterLocaleEffect_Double_WantFrenchLocaleUsingDefault"));
		suite.addTest(new TestFloatFormatterLocaleEffect("TestFloatFormatterLocaleEffect_Double_WantFrenchLocaleUsingExplicitLocale"));
		suite.addTest(new TestFloatFormatterLocaleEffect("TestFloatFormatterLocaleEffect_Double_DefaultAllAvailableLocales"));
		suite.addTest(new TestFloatFormatterLocaleEffect("TestFloatFormatterLocaleEffect_Double_ExplicitAllAvailableLocales"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestFloatFormatterLocaleEffect_Double_DefaultLocale() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		{
			// NB. Just FloatFormatter.toString() would fail because of round-trip precision issues, that are nothing to do with Locale decimal point character
			// FYI. FloatFormatter.toStringOfFixedMaximumLength(n,16,false) is the method invoked, for example, in DecimalString.addValue(double)
			
			assertEquals("Default",value1,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals("Default",value2,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals("Default",value3,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals("Default",value4,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
		}
	}
	
	// Test the French locale specifically, because this was where the problem of Locale-specific decimal point characters was first observed
	// Unnecessary really, since will be one of the available locales, and if it weren't would fail to compile (which we want to check for)
	
	public void TestFloatFormatterLocaleEffect_Double_DefaultFrenchLocale() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		{
			Locale.setDefault(Locale.FRENCH);
			
			assertEquals(Locale.getDefault().toString(),value1,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value2,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value3,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value4,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
		}
	}
	
	// Test the French locale specifically, because this was where the problem of Locale-specific decimal point characters was first observed
	
	public void TestFloatFormatterLocaleEffect_Double_WantFrenchLocaleUsingDefault() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		String expectValue1 = "39,76";
		String expectValue2 = "-56356,457457";
		String expectValue3 = "0";
		String expectValue4 = ",745345345E-20";	// this is what we actually seem to get - note the case change as well as the position of the decimal point

		{
			Locale.setDefault(Locale.FRENCH);
			
			assertEquals(Locale.getDefault().toString(),expectValue1,FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/));
			assertEquals(Locale.getDefault().toString(),expectValue2,FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/));
			assertEquals(Locale.getDefault().toString(),expectValue3,FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/));
			assertEquals(Locale.getDefault().toString(),expectValue4,FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/));
			
			// Double.valueOf().doubleValue() will NOT work, since not Locale aware and will puke on "," rather than"." with NumberFormatExcption,
			// so use Locale-aware NumberFormat.getInstance().parse().doubleValue() instead (with same Locale, of course) ...
			
			assertEquals(Locale.getDefault().toString(),value1,NumberFormat.getInstance().parse(FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value2,NumberFormat.getInstance().parse(FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value3,NumberFormat.getInstance().parse(FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value4,NumberFormat.getInstance().parse(FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/)).doubleValue());
		}
	}
	
	// Test the French locale specifically, because this was where the problem of Locale-specific decimal point characters was first observed
	
	public void TestFloatFormatterLocaleEffect_Double_WantFrenchLocaleUsingExplicitLocale() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		String expectValue1 = "39,76";
		String expectValue2 = "-56356,457457";
		String expectValue3 = "0";
		String expectValue4 = ",745345345E-20";	// this is what we actually seem to get - note the case change as well as the position of the decimal point

		{
			assertEquals(Locale.getDefault().toString(),expectValue1,FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/,Locale.FRENCH));
			assertEquals(Locale.getDefault().toString(),expectValue2,FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/,Locale.FRENCH));
			assertEquals(Locale.getDefault().toString(),expectValue3,FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/,Locale.FRENCH));
			assertEquals(Locale.getDefault().toString(),expectValue4,FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/,Locale.FRENCH));
			
			// Double.valueOf().doubleValue() will NOT work, since not Locale aware and will puke on "," rather than"." with NumberFormatExcption,
			// so use Locale-aware NumberFormat.getInstance().parse().doubleValue() instead (with same Locale, of course) ...
			
			assertEquals(Locale.getDefault().toString(),value1,NumberFormat.getInstance(Locale.FRENCH).parse(FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/,Locale.FRENCH)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value2,NumberFormat.getInstance(Locale.FRENCH).parse(FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/,Locale.FRENCH)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value3,NumberFormat.getInstance(Locale.FRENCH).parse(FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/,Locale.FRENCH)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value4,NumberFormat.getInstance(Locale.FRENCH).parse(FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/,Locale.FRENCH)).doubleValue());
		}
	}
	
	public void TestFloatFormatterLocaleEffect_Double_DefaultAllAvailableLocales() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		Locale[] localesToTest = Locale.getAvailableLocales();
		for (Locale l : localesToTest) {
//System.err.println("Testing effect on FloatFormatter with double of default locale "+l);
			Locale.setDefault(l);
			
			assertEquals(l.toString(),value1,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals(l.toString(),value2,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals(l.toString(),value3,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
			assertEquals(l.toString(),value4,Double.valueOf(FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/,Locale.US)).doubleValue());
		}
	}
	
	public void TestFloatFormatterLocaleEffect_Double_ExplicitAllAvailableLocales() throws Exception {
		double value1 = 39.76d;
		double value2 = -56356.457457d;
		double value3 = 0d;
		double value4 = 7.45345345e-21d;
		
		Locale[] localesToTest = Locale.getAvailableLocales();
		for (Locale l : localesToTest) {
//System.err.println("Testing effect on FloatFormatter with double of explicit locale "+l);
			
			assertEquals(Locale.getDefault().toString(),value1,NumberFormat.getInstance(l).parse(FloatFormatter.toStringOfFixedMaximumLength(value1,16,false/*allowNonNumbers*/,l)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value2,NumberFormat.getInstance(l).parse(FloatFormatter.toStringOfFixedMaximumLength(value2,16,false/*allowNonNumbers*/,l)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value3,NumberFormat.getInstance(l).parse(FloatFormatter.toStringOfFixedMaximumLength(value3,16,false/*allowNonNumbers*/,l)).doubleValue());
			assertEquals(Locale.getDefault().toString(),value4,NumberFormat.getInstance(l).parse(FloatFormatter.toStringOfFixedMaximumLength(value4,16,false/*allowNonNumbers*/,l)).doubleValue());
		}
	}

}
