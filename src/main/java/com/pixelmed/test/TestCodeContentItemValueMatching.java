/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestCodeContentItemValueMatching extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCodeContentItemValueMatching(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCodeContentItemValueMatching.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCodeContentItemValueMatching");
		
		suite.addTest(new TestCodeContentItemValueMatching("TestCodeContentItemValueMatching_ClassMethod"));
		suite.addTest(new TestCodeContentItemValueMatching("TestCodeContentItemValueMatching_Static"));
		
		return suite;
	}
	
	ContentItemFactory cf;
		
	protected void setUp() {
		cf = new ContentItemFactory();
	}
	
	protected void tearDown() {
	}
	
	public void TestCodeContentItemValueMatching_ClassMethod() throws Exception {
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		
		String cvConcept = "122140";
		String csdConcept = "DCM";
		String cmConcept = "Comparison with Prior Exam Done";
				
		ContentItemFactory.CodeContentItem ci = cf.makeCodeContentItem(null/*parent*/,null/*relationshipType*/,
			new CodedSequenceItem(cvConcept,csdConcept,cmConcept),
			new CodedSequenceItem(cvValue,csdValue,cmValue));
	
		assertTrue("Checking concept name correct CSD and CV match",ci.contentItemNameMatchesCodeValueAndCodingSchemeDesignator(cvConcept,csdConcept));
		assertTrue("Checking concept name incorrect CSD does not match",!ci.contentItemNameMatchesCodeValueAndCodingSchemeDesignator(cvConcept,"bad"));
		assertTrue("Checking concept name incorrect CV does not match",!ci.contentItemNameMatchesCodeValueAndCodingSchemeDesignator("bad",csdConcept));
	
		assertTrue("Checking value correct CSD and CV match",ci.contentItemValueMatchesCodeValueAndCodingSchemeDesignator(cvValue,csdValue));
		assertTrue("Checking value incorrect CSD does not match",!ci.contentItemValueMatchesCodeValueAndCodingSchemeDesignator(cvValue,"bad"));
		assertTrue("Checking value incorrect CV does not match",!ci.contentItemValueMatchesCodeValueAndCodingSchemeDesignator("bad",csdValue));
	}
	
	public void TestCodeContentItemValueMatching_Static() throws Exception {
		String cvValue = "R-00339";
		String csdValue = "SRT";
		String cmValue = "No";
		
		String cvConcept = "122140";
		String csdConcept = "DCM";
		String cmConcept = "Comparison with Prior Exam Done";
		
		ContentItemFactory.CodeContentItem ci = cf.makeCodeContentItem(null/*parent*/,null/*relationshipType*/,
			new CodedSequenceItem(cvConcept,csdConcept,cmConcept),
			new CodedSequenceItem(cvValue,csdValue,cmValue));
	
		assertTrue("Checking concept name correct CSD and CV match",ContentItem.contentItemNameMatchesCodeValueAndCodingSchemeDesignator(ci,cvConcept,csdConcept));
		assertTrue("Checking concept name incorrect CSD does not match",!ContentItem.contentItemNameMatchesCodeValueAndCodingSchemeDesignator(ci,cvConcept,"bad"));
		assertTrue("Checking concept name incorrect CV does not match",!ContentItem.contentItemNameMatchesCodeValueAndCodingSchemeDesignator(ci,"bad",csdConcept));
	
		assertTrue("Checking value correct CSD and CV match",ContentItemFactory.codeContentItemValueMatchesCodeValueAndCodingSchemeDesignator(ci,cvValue,csdValue));
		assertTrue("Checking value incorrect CSD does not match",!ContentItemFactory.codeContentItemValueMatchesCodeValueAndCodingSchemeDesignator(ci,cvValue,"bad"));
		assertTrue("Checking value incorrect CV does not match",!ContentItemFactory.codeContentItemValueMatchesCodeValueAndCodingSchemeDesignator(ci,"bad",csdValue));
	}
	
}
