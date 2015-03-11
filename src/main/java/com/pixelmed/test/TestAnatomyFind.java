/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.anatproc.Concept;
import com.pixelmed.anatproc.CTAnatomy;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.LongStringAttribute;
import com.pixelmed.dicom.TagFromName;

import junit.framework.*;

import java.io.File;

public class TestAnatomyFind extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAnatomyFind(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAnatomyCombined.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAnatomyFind");
		
		suite.addTest(new TestAnatomyFind("TestAnatomyFind_SeriesDescription"));
		suite.addTest(new TestAnatomyFind("TestAnatomyFind_BodyPartExaminedOverridesSeriesDescription"));
		suite.addTest(new TestAnatomyFind("TestAnatomyFind_SeriesDescriptionOverridesStudyDescription"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private static final Concept chest = new Concept("C0817096");
	private static final Concept pelvis = new Concept("C0030797");

	public void TestAnatomyFind_SeriesDescription() throws Exception {
		AttributeList list = new AttributeList();
		
		{ Attribute a = new LongStringAttribute(TagFromName.SeriesDescription); a.addValue("Chest"); list.put(a); }
		
		Concept found = CTAnatomy.findAnatomicConcept(list);
		
		assertEquals("Checking found chest in series description",chest,found);
		assertTrue("Checking found is not pelvis",!pelvis.equals(found));
	}

	public void TestAnatomyFind_BodyPartExaminedOverridesSeriesDescription() throws Exception {
		AttributeList list = new AttributeList();
		
		{ Attribute a = new CodeStringAttribute(TagFromName.BodyPartExamined); a.addValue("Chest"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.SeriesDescription); a.addValue("Pelvis"); list.put(a); }
		
		Concept found = CTAnatomy.findAnatomicConcept(list);
		
		assertEquals("Checking found chest in BodyPartExamined overrides pelvis in SeriesDescription",chest,found);
		assertTrue("Checking found is not pelvis",!pelvis.equals(found));
	}

	public void TestAnatomyFind_SeriesDescriptionOverridesStudyDescription() throws Exception {
		AttributeList list = new AttributeList();
		
		{ Attribute a = new LongStringAttribute(TagFromName.StudyDescription); a.addValue("Pelvis"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.SeriesDescription); a.addValue("Chest"); list.put(a); }
		
		Concept found = CTAnatomy.findAnatomicConcept(list);
		
		assertEquals("Checking found chest in SeriesDescription overrides pelvis in StudyDescription",chest,found);
		assertTrue("Checking found is not pelvis",!pelvis.equals(found));
	}
}
