/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestCodedSequenceItemParseStringTuple extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCodedSequenceItemParseStringTuple(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCodedSequenceItemParseStringTuple.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCodedSequenceItemParseStringTuple");
		
		suite.addTest(new TestCodedSequenceItemParseStringTuple("TestCodedSequenceItemParseStringTuple_Three"));
		suite.addTest(new TestCodedSequenceItemParseStringTuple("TestCodedSequenceItemParseStringTuple_Three_NoQuotesAroundMeaning"));
		suite.addTest(new TestCodedSequenceItemParseStringTuple("TestCodedSequenceItemParseStringTuple_Three_QuotesAroundEverything"));
		suite.addTest(new TestCodedSequenceItemParseStringTuple("TestCodedSequenceItemParseStringTuple_Three_LeadingAndTrailingSpaces"));
		suite.addTest(new TestCodedSequenceItemParseStringTuple("TestCodedSequenceItemParseStringTuple_Four"));
		suite.addTest(new TestCodedSequenceItemParseStringTuple("TestCodedSequenceItemParseStringTuple_Four_QuotesAroundEverything"));
		suite.addTest(new TestCodedSequenceItemParseStringTuple("TestCodedSequenceItemParseStringTuple_Four_LeadingAndTrailingSpaces"));
		
		return suite;
	}
			
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCodedSequenceItemParseStringTuple_Three() throws Exception {
		String tuple = "(R-00339,SRT,\"No\")";
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		
		CodedSequenceItem csi = new CodedSequenceItem(tuple);
						
		assertEquals("Checking cv",cvValue,csi.getCodeValue());
		assertEquals("Checking csd",csdValue,csi.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi.getCodeMeaning());
		
		// also check round trip via toString() method ...
		CodedSequenceItem csi2 = new CodedSequenceItem(csi.toString());
		assertEquals("Checking cv",cvValue,csi2.getCodeValue());
		assertEquals("Checking csd",csdValue,csi2.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi2.getCodeMeaning());
	}
	
	public void TestCodedSequenceItemParseStringTuple_Three_NoQuotesAroundMeaning() throws Exception {
		String tuple = "(R-00339,SRT,No)";
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		
		CodedSequenceItem csi = new CodedSequenceItem(tuple);
						
		assertEquals("Checking cv",cvValue,csi.getCodeValue());
		assertEquals("Checking csd",csdValue,csi.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi.getCodeMeaning());
		
		// also check round trip via toString() method ...
		CodedSequenceItem csi2 = new CodedSequenceItem(csi.toString());
		assertEquals("Checking cv",cvValue,csi2.getCodeValue());
		assertEquals("Checking csd",csdValue,csi2.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi2.getCodeMeaning());
	}
	
	public void TestCodedSequenceItemParseStringTuple_Three_QuotesAroundEverything() throws Exception {
		String tuple = "(\"R-00339\",\"SRT\",\"No\")";
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		
		CodedSequenceItem csi = new CodedSequenceItem(tuple);
						
		assertEquals("Checking cv",cvValue,csi.getCodeValue());
		assertEquals("Checking csd",csdValue,csi.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi.getCodeMeaning());
		
		// also check round trip via toString() method ...
		CodedSequenceItem csi2 = new CodedSequenceItem(csi.toString());
		assertEquals("Checking cv",cvValue,csi2.getCodeValue());
		assertEquals("Checking csd",csdValue,csi2.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi2.getCodeMeaning());
	}

	public void TestCodedSequenceItemParseStringTuple_Three_LeadingAndTrailingSpaces() throws Exception {
		String tuple = "  ( R-00339   , SRT , \"No\"   )  ";
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		
		CodedSequenceItem csi = new CodedSequenceItem(tuple);
						
		assertEquals("Checking cv",cvValue,csi.getCodeValue());
		assertEquals("Checking csd",csdValue,csi.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi.getCodeMeaning());
		
		// also check round trip via toString() method ...
		CodedSequenceItem csi2 = new CodedSequenceItem(csi.toString());
		assertEquals("Checking cv",cvValue,csi2.getCodeValue());
		assertEquals("Checking csd",csdValue,csi2.getCodingSchemeDesignator());
		assertEquals("Checking cm",cmValue,csi2.getCodeMeaning());
	}
	
	public void TestCodedSequenceItemParseStringTuple_Four() throws Exception {
		String tuple = "(R-00339,SRT,1.0,\"No\")";
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		String csvValue = "1.0";
		
		CodedSequenceItem csi = new CodedSequenceItem(tuple);
						
		assertEquals("Checking cv",cvValue,csi.getCodeValue());
		assertEquals("Checking csd",csdValue,csi.getCodingSchemeDesignator());
		assertEquals("Checking csv",csvValue,csi.getCodingSchemeVersion());
		assertEquals("Checking cm",cmValue,csi.getCodeMeaning());
		
		// also check round trip via toString() method ...
		CodedSequenceItem csi2 = new CodedSequenceItem(csi.toString());
		assertEquals("Checking cv",cvValue,csi2.getCodeValue());
		assertEquals("Checking csd",csdValue,csi2.getCodingSchemeDesignator());
		assertEquals("Checking csv",csvValue,csi2.getCodingSchemeVersion());
		assertEquals("Checking cm",cmValue,csi2.getCodeMeaning());
	}
	
	public void TestCodedSequenceItemParseStringTuple_Four_QuotesAroundEverything() throws Exception {
		String tuple = "(\"R-00339\",\"SRT\",\"1.0\",\"No\")";
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		String csvValue = "1.0";
		
		CodedSequenceItem csi = new CodedSequenceItem(tuple);
						
		assertEquals("Checking cv",cvValue,csi.getCodeValue());
		assertEquals("Checking csd",csdValue,csi.getCodingSchemeDesignator());
		assertEquals("Checking csv",csvValue,csi.getCodingSchemeVersion());
		assertEquals("Checking cm",cmValue,csi.getCodeMeaning());
		
		// also check round trip via toString() method ...
		CodedSequenceItem csi2 = new CodedSequenceItem(csi.toString());
		assertEquals("Checking cv",cvValue,csi2.getCodeValue());
		assertEquals("Checking csd",csdValue,csi2.getCodingSchemeDesignator());
		assertEquals("Checking csv",csvValue,csi2.getCodingSchemeVersion());
		assertEquals("Checking cm",cmValue,csi2.getCodeMeaning());
	}

	public void TestCodedSequenceItemParseStringTuple_Four_LeadingAndTrailingSpaces() throws Exception {
		String tuple = "  ( R-00339   , SRT , 1.0  , \"No\"   )  ";
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		String csvValue = "1.0";
		
		CodedSequenceItem csi = new CodedSequenceItem(tuple);
						
		assertEquals("Checking cv",cvValue,csi.getCodeValue());
		assertEquals("Checking csd",csdValue,csi.getCodingSchemeDesignator());
		assertEquals("Checking csv",csvValue,csi.getCodingSchemeVersion());
		assertEquals("Checking cm",cmValue,csi.getCodeMeaning());
		
		// also check round trip via toString() method ...
		CodedSequenceItem csi2 = new CodedSequenceItem(csi.toString());
		assertEquals("Checking cv",cvValue,csi2.getCodeValue());
		assertEquals("Checking csd",csdValue,csi2.getCodingSchemeDesignator());
		assertEquals("Checking csv",csvValue,csi2.getCodingSchemeVersion());
		assertEquals("Checking cm",cmValue,csi2.getCodeMeaning());
	}
	
}
