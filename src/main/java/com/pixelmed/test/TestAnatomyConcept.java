/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.anatproc.*;

import junit.framework.*;

import java.io.File;

public class TestAnatomyConcept extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAnatomyConcept(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAnatomyConcept.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAnatomyConcept");
		
		suite.addTest(new TestAnatomyConcept("TestAnatomyConcept_Methods"));
		suite.addTest(new TestAnatomyConcept("TestAnatomyConcept_Equality"));
		suite.addTest(new TestAnatomyConcept("TestAnatomyConcept_DictionaryLookup"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestAnatomyConcept_Methods() throws Exception {
		String uid = "C0020885";
		Concept concept = new Concept("C0020885");
		assertEquals("Checking UID",uid,concept.getConceptUniqueIdentifier());
	}
	
	public void TestAnatomyConcept_Equality() throws Exception {

		Concept ileum1 = new                    Concept("C0020885");
		Concept ileum2 = new DisplayableAnatomicConcept("C0020885","34516001",	false/*unpaired*/,	"SRT",	"SNM3",	null,	"T-58600",	"Ileum",			"ILEUM",		null,	null,			null);
		Concept ilium  = new DisplayableAnatomicConcept("C0020889","22356005",	true   /*paired*/,	"SRT",	"SNM3",	null,	"T-12340",	"Ilium",			"ILIUM",		null,	null,			null);

		assertEquals("Checking self",ileum1,ileum1);
		assertTrue("Checking self hashCode",ileum1.hashCode() == ileum1.hashCode());
		
		assertEquals("Checking same Concept and DisplayableAnatomicConcept",ileum1,ileum2);
		assertTrue("Checking same Concept and DisplayableAnatomicConcept hashCode",ileum1.hashCode() == ileum2.hashCode());
		
		assertTrue("Checking different Concepts",!ileum2.equals(ilium));
		assertTrue("Checking different Concepts hashCode",ileum1.hashCode() != ilium.hashCode());
		
		assertTrue("Checking null",!ileum2.equals(null));
	}
	
	public void TestAnatomyConcept_DictionaryLookup() throws Exception {
		{
			Concept concept = new Concept("C0020885");
			String expectedCodeMeaning = "Ileum";
			assertEquals("Checking look up by Concept",expectedCodeMeaning,CTAnatomy.getAnatomyConcepts().find(concept).getCodeMeaning());
		}
		{
			Concept concept = new Concept("C0037303");
			String expectedCodeMeaning = "Skull";
			assertEquals("Checking look up by Concept",expectedCodeMeaning,ProjectionXRayAnatomy.getAnatomyConcepts().find(concept).getCodeMeaning());
		}
	}
	
}
