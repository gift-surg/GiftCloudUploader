/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dose.*;

import junit.framework.*;

public class TestScopeOfDoseAccummulation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestScopeOfDoseAccummulation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestScopeOfDoseAccummulation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestScopeOfDoseAccummulation");
		
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_Study_Description"));
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_Study_Equality"));
		
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_Series_Description"));
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_Series_Equality"));
		
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_PerformedProcedureStep_Description"));
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_PerformedProcedureStep_Equality"));
		
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_IrradiationEvent_Description"));
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_IrradiationEvent_Equality"));
		
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_Study_Series_Inequality"));
		
		suite.addTest(new TestScopeOfDoseAccummulation("TestScopeOfDoseAccummulation_SelectFromCode"));
	
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestScopeOfDoseAccummulation_Study_Description() {
		
		assertEquals("Checking STUDY scope description",ScopeOfDoseAccummulation.STUDY.toString(),"Study");
	}
	
	public void TestScopeOfDoseAccummulation_Series_Description() {
		
		assertEquals("Checking SERIES scope description",ScopeOfDoseAccummulation.SERIES.toString(),"Series");
	}
	
	public void TestScopeOfDoseAccummulation_PerformedProcedureStep_Description() {
		
		assertEquals("Checking PPS scope description",ScopeOfDoseAccummulation.PPS.toString(),"Performed Procedure Step");
	}
	
	public void TestScopeOfDoseAccummulation_IrradiationEvent_Description() {
		
		assertEquals("Checking IRRADIATION_EVENT scope description",ScopeOfDoseAccummulation.IRRADIATION_EVENT.toString(),"Irradiation Event");
	}
	
	public void TestScopeOfDoseAccummulation_Study_Equality() throws Exception {
		
		assertEquals("Checking STUDY scope equality",ScopeOfDoseAccummulation.STUDY,ScopeOfDoseAccummulation.STUDY);
		assertEquals("Checking STUDY content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.STUDY).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.STUDY).hashCode());
		assertEquals("Checking STUDY content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.STUDY),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.STUDY));
		assertEquals("Checking STUDY content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.STUDY).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.STUDY).hashCode());
		assertEquals("Checking STUDY content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.STUDY),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.STUDY));
	}
	
	public void TestScopeOfDoseAccummulation_Series_Equality() throws Exception  {
		
		assertEquals("Checking SERIES scope equality",ScopeOfDoseAccummulation.SERIES,ScopeOfDoseAccummulation.SERIES);
		assertEquals("Checking SERIES content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.SERIES).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.SERIES).hashCode());
		assertEquals("Checking SERIES content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.SERIES),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.SERIES));
		assertEquals("Checking SERIES content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.SERIES).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.SERIES).hashCode());
		assertEquals("Checking SERIES content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.SERIES),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.SERIES));
	}
	
	public void TestScopeOfDoseAccummulation_PerformedProcedureStep_Equality() throws Exception  {
		
		assertEquals("Checking PPS scope equality",ScopeOfDoseAccummulation.PPS,ScopeOfDoseAccummulation.PPS);
		assertEquals("Checking PPS content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.PPS).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.PPS).hashCode());
		assertEquals("Checking PPS content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.PPS),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.PPS));
		assertEquals("Checking PPS content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.PPS).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.PPS).hashCode());
		assertEquals("Checking PPS content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.PPS),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.PPS));
	}
	
	public void TestScopeOfDoseAccummulation_IrradiationEvent_Equality() throws Exception  {
		
		assertEquals("Checking IRRADIATION_EVENT scope equality",ScopeOfDoseAccummulation.IRRADIATION_EVENT,ScopeOfDoseAccummulation.IRRADIATION_EVENT);
		assertEquals("Checking IRRADIATION_EVENT content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT).hashCode());
		assertEquals("Checking IRRADIATION_EVENT content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT),ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT));
		assertEquals("Checking IRRADIATION_EVENT content item hashCode equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT).hashCode(),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT).hashCode());
		assertEquals("Checking IRRADIATION_EVENT content item equality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT),ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.IRRADIATION_EVENT));
	}
	
	public void TestScopeOfDoseAccummulation_Study_Series_Inequality() throws Exception  {
		
		assertFalse("Checking STUDY versus SERIES scope inequality",ScopeOfDoseAccummulation.STUDY.equals(ScopeOfDoseAccummulation.SERIES));
		assertFalse("Checking STUDY versus SERIES content item hashCode inequality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.STUDY).hashCode() == ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.SERIES).hashCode());
		assertFalse("Checking STUDY versus SERIES content item inequality",ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.STUDY).equals(ScopeOfDoseAccummulation.getCodedSequenceItemForScopeConcept(ScopeOfDoseAccummulation.SERIES)));
		assertFalse("Checking STUDY versus SERIES content item hashCode inequality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.STUDY).hashCode() == ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.SERIES).hashCode());
		assertFalse("Checking STUDY versus SERIES content item inequality",ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.STUDY).equals(ScopeOfDoseAccummulation.getCodedSequenceItemForUIDConcept(ScopeOfDoseAccummulation.SERIES)));
	}
	
	public void TestScopeOfDoseAccummulation_SelectFromCode() throws Exception {
	
		assertTrue("Checking select STUDY",ScopeOfDoseAccummulation.selectFromCode(new CodedSequenceItem("113014","DCM","Study")).equals(ScopeOfDoseAccummulation.STUDY));
		assertTrue("Checking select SERIES",ScopeOfDoseAccummulation.selectFromCode(new CodedSequenceItem("113015","DCM","Series")).equals(ScopeOfDoseAccummulation.SERIES));
		assertTrue("Checking select PPS",ScopeOfDoseAccummulation.selectFromCode(new CodedSequenceItem("113016","DCM","Performed Procedure Step")).equals(ScopeOfDoseAccummulation.PPS));
		assertTrue("Checking select IRRADIATION_EVENT",ScopeOfDoseAccummulation.selectFromCode(new CodedSequenceItem("113852","DCM","Irradiation Event")).equals(ScopeOfDoseAccummulation.IRRADIATION_EVENT));
		assertTrue("Checking select BLA finds nothing",ScopeOfDoseAccummulation.selectFromCode(new CodedSequenceItem("43643432","99BLA","Unknown")) == null);
	}
	
}
