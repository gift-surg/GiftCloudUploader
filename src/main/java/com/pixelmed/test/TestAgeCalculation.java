/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.DateUtilities;

import java.text.ParseException;

import junit.framework.*;

public class TestAgeCalculation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAgeCalculation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAgeCalculation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAgeCalculation");
		
		suite.addTest(new TestAgeCalculation("TestAgeCalculation_Valid"));
		suite.addTest(new TestAgeCalculation("TestAgeCalculation_InvalidSwapped"));
		suite.addTest(new TestAgeCalculation("TestAgeCalculation_Invalid"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private class EarlierLaterAge {
		String earlier;
		String later;
		String age;
		
		EarlierLaterAge(String earlier,String later,String age) {
			this.earlier = earlier;
			this.later = later;
			this.age = age;
		}
	};
	
	EarlierLaterAge[] valid = {
		new EarlierLaterAge ("19610625","20120624","050Y"),
		new EarlierLaterAge ("19610625","20120625","051Y"),
		new EarlierLaterAge ("19610714","20120625","050Y"),
		new EarlierLaterAge ("20120103","20120625","005M"),
		new EarlierLaterAge ("20120525","20120625","001M"),
		new EarlierLaterAge ("20120526","20120625","004W"),
		new EarlierLaterAge ("20120611","20120625","002W"),
		new EarlierLaterAge ("20120612","20120625","001W"),
		new EarlierLaterAge ("20120618","20120625","001W"),
		new EarlierLaterAge ("20120622","20120625","003D"),
		new EarlierLaterAge ("20000229","20110301","011Y"),		// born on 02/29 on a leap year - definitely past birthday 
		new EarlierLaterAge ("20000229","20110228","010Y"),		// born on 02/29 on a leap year - NOT past birthday (varies by country - use UK not US interpretation since not worth correcting; see "http://en.wikipedia.org/wiki/February_29#Births")
		new EarlierLaterAge ("20000229","20120228","011Y"),		// born on 02/29 on a leap year - before leap year birthday
		new EarlierLaterAge ("20000229","20120229","012Y"),		// born on 02/29 on a leap year - on leap year birthday
		new EarlierLaterAge ("20000229","20120301","012Y"),		// born on 02/29 on a leap year - after leap year birthday
		new EarlierLaterAge ("20000228","20120228","012Y"),
		new EarlierLaterAge ("20000228","20120229","012Y")
	};
	
	EarlierLaterAge[] invalid = {
		new EarlierLaterAge ("19610625","20120631","051Y"),		// invalid date - day out of range
		new EarlierLaterAge ("20000231","20110231","011Y"),		// invalid date - day out of range
		new EarlierLaterAge ("20000230","20110230","011Y"),		// invalid date - day out of range
		new EarlierLaterAge ("20000229","20110229","011Y")		// invalid date - day out of range since 2011 is not a leap year
	};
	
	public void TestAgeCalculation_Valid() throws Exception {
		for (EarlierLaterAge eal : valid) {
			assertEquals("Test age between "+eal.earlier+" and "+eal.later,eal.age,DateUtilities.getAgeBetweenAsDICOMAgeString(eal.earlier,eal.later));
		}
	}
	
	public void TestAgeCalculation_InvalidSwapped() throws Exception {
		for (EarlierLaterAge eal : valid) {
			// use the earlier and later dates swapped around to make them invalid ...
			try {
				DateUtilities.getAgeBetweenAsDICOMAgeString(eal.later,eal.earlier);
				fail("getAgeBetweenAsDICOMAgeString() should have thrown an exception for age between "+eal.later+" and "+eal.earlier);
			}
			catch (IllegalArgumentException e) {
				// expected result
			}
			catch (Exception e) {
				fail("getAgeBetweenAsDICOMAgeString() should have thrown an IllegalArgumentException for age between "+eal.later+" and "+eal.earlier+", but threw instead "+e);
			}
		}
	}
	
	public void TestAgeCalculation_Invalid() throws Exception {
		for (EarlierLaterAge eal : invalid) {
			try {
				DateUtilities.getAgeBetweenAsDICOMAgeString(eal.earlier,eal.later);
				fail("getAgeBetweenAsDICOMAgeString() should have thrown an exception for age between "+eal.earlier+" and "+eal.later);
			}
			catch (IllegalArgumentException e) {
				// expected result
			}
			catch (ParseException e) {
				// expected result
				//System.err.println(e);
			}
			catch (Exception e) {
				fail("getAgeBetweenAsDICOMAgeString() should have thrown an IllegalArgumentException for age between "+eal.earlier+" and "+eal.later+", but threw instead "+e);
			}
		}
	}
}
