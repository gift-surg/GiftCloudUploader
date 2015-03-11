/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.anatproc.*;

import com.pixelmed.dicom.CodedSequenceItem;

import junit.framework.*;

import java.io.File;

public class TestLaterality extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestLaterality(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestLaterality.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestLaterality");
		
		suite.addTest(new TestLaterality("TestLaterality_Methods"));
		suite.addTest(new TestLaterality("TestLaterality_Equality"));
		suite.addTest(new TestLaterality("TestLaterality_DictionaryLookup"));
		suite.addTest(new TestLaterality("TestLaterality_MammoConversion"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestLaterality_Methods() throws Exception {
		String uid = "C0205091";
		Concept concept = new Concept("C0205091");
		assertEquals("Checking UID",uid,concept.getConceptUniqueIdentifier());
	}
	
	public void TestLaterality_Equality() throws Exception {

		Concept left1 = new                      Concept("C0205091");
		Concept left2 = new DisplayableLateralityConcept("C0205091","7771000",	"SRT",	"SNM3",	null,	"G-A101",	"Left",			"L",		null,	null,			null);
		Concept right = new DisplayableLateralityConcept("C0205090","24028007",	"SRT",	"SNM3",	null,	"G-A100",	"Right",		"R",		null,	null,			null);

		assertEquals("Checking self",left1,left1);
		assertTrue("Checking self hashCode",left1.hashCode() == left1.hashCode());
		
		assertEquals("Checking same Concept and DisplayableAnatomicConcept",left1,left2);
		assertTrue("Checking same Concept and DisplayableAnatomicConcept hashCode",left1.hashCode() == left1.hashCode());
		
		assertTrue("Checking different Concepts",!left2.equals(right));
		assertTrue("Checking different Concepts hashCode",left2.hashCode() != right.hashCode());
		
		assertTrue("Checking null",!left2.equals(null));
	}
	
	public void TestLaterality_DictionaryLookup() throws Exception {
		{
			Concept concept = new Concept("C0205091");
			String expectedCodeMeaning = "Left";
			assertEquals("Checking look up by Concept",expectedCodeMeaning,ProjectionXRayAnatomy.getLateralityConcepts().find(concept).getCodeMeaning());
		}
	}
	
	public void TestLaterality_MammoConversion() throws Exception {
		{
			DisplayableLateralityConcept generic = new DisplayableLateralityConcept("C0205091","7771000","SRT","SNM3",null,"G-A101","Left","L",null,null,null);
			DisplayableLateralityConcept breastSpecific  = new DisplayableLateralityConcept("C0222601","80248007","SRT","SNM3",null,"T-04030","Left breast",null,null,null,null);
			String expectedCodeMeaning = "Left breast";
			
			DisplayableLateralityConcept converted = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic);
			
			assertEquals("Checking converted left by concept",breastSpecific,converted);
			assertEquals("Checking converted left by code meaning",expectedCodeMeaning,converted.getCodeMeaning());
			
			CodedSequenceItem convertedCodedSequenceItem = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic.getCodedSequenceItem());
			assertEquals("Checking converted left by CodedSequenceItem code meaning",expectedCodeMeaning,convertedCodedSequenceItem.getCodeMeaning());
		}
		{
			DisplayableLateralityConcept generic = new DisplayableLateralityConcept("C0205090","24028007","SRT","SRT",null,"G-A100","Right","R",null,null,null);
			DisplayableLateralityConcept breastSpecific  = new DisplayableLateralityConcept("C0222600","73056007","SRT","SNM3",null,"T-04020","Right breast",null,null,null,null);
			String expectedCodeMeaning = "Right breast";
			
			DisplayableLateralityConcept converted = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic);
			
			assertEquals("Checking converted right by concept",breastSpecific,converted);
			assertEquals("Checking converted right by code meaning",expectedCodeMeaning,converted.getCodeMeaning());
			
			CodedSequenceItem convertedCodedSequenceItem = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic.getCodedSequenceItem());
			assertEquals("Checking converted right by CodedSequenceItem code meaning",expectedCodeMeaning,convertedCodedSequenceItem.getCodeMeaning());
		}
		{
			DisplayableLateralityConcept generic = new DisplayableLateralityConcept("C0238767","51440002","SRT","SRT",null,"G-A102","Right and left","B",null,null,null);
			DisplayableLateralityConcept breastSpecific = new DisplayableLateralityConcept("C0222605","63762007","SRT","SNM3",null,"T-04080","Both breasts",null,null,null,null);
			String expectedCodeMeaning = "Both breasts";
			
			DisplayableLateralityConcept converted = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic);
			
			assertEquals("Checking converted both by concept",breastSpecific,converted);
			assertEquals("Checking converted both by code meaning",expectedCodeMeaning,converted.getCodeMeaning());
			
			CodedSequenceItem convertedCodedSequenceItem = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic.getCodedSequenceItem());
			assertEquals("Checking converted both by CodedSequenceItem code meaning",expectedCodeMeaning,convertedCodedSequenceItem.getCodeMeaning());
		}
		{
			DisplayableLateralityConcept generic = new DisplayableLateralityConcept("C0205092","66459002","SRT","SRT",null,"G-A103","Unilateral","U",null,null,null);
			String expectedCodeMeaning = "Unilateral";
			
			DisplayableLateralityConcept converted = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic);
			
			assertEquals("Checking unconverted unpaired by concept",generic,converted);
			assertEquals("Checking unconverted unpaired by code meaning",expectedCodeMeaning,converted.getCodeMeaning());
			
			CodedSequenceItem convertedCodedSequenceItem = MammographyLaterality.convertGenericLateralityToBreastSpecificLaterality(generic.getCodedSequenceItem());
			assertEquals("Checking converted unpaired by CodedSequenceItem code meaning",expectedCodeMeaning,convertedCodedSequenceItem.getCodeMeaning());
		}
	}
	
}
