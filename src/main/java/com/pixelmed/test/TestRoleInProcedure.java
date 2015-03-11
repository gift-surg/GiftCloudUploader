/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

public class TestRoleInProcedure extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestRoleInProcedure(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestRoleInProcedure.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestRoleInProcedure");
		
		suite.addTest(new TestRoleInProcedure("TestRoleInProcedure_IrradiationAdministering_Description"));
		suite.addTest(new TestRoleInProcedure("TestRoleInProcedure_IrradiationAdministering_Equality"));
		
		suite.addTest(new TestRoleInProcedure("TestRoleInProcedure_IrradiationAuthorizing_Description"));
		suite.addTest(new TestRoleInProcedure("TestRoleInProcedure_IrradiationAuthorizing_Equality"));
		
		suite.addTest(new TestRoleInProcedure("TestRoleInProcedure_IrradiationAdministering_IrradiationAuthorizing_Inequality"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestRoleInProcedure_IrradiationAdministering_Description() {
		
		assertEquals("Checking IRRADIATION_ADMINISTERING scope description",RoleInProcedure.IRRADIATION_ADMINISTERING.toString(),"Irradiation Administering");
	}
	
	public void TestRoleInProcedure_IrradiationAuthorizing_Description() {
		
		assertEquals("Checking IRRADIATION_AUTHORIZING scope description",RoleInProcedure.IRRADIATION_AUTHORIZING.toString(),"Irradiation Authorizing");
	}
	
	public void TestRoleInProcedure_IrradiationAdministering_Equality() throws Exception {
		
		assertEquals("Checking IRRADIATION_ADMINISTERING scope equality",RoleInProcedure.IRRADIATION_ADMINISTERING,RoleInProcedure.IRRADIATION_ADMINISTERING);
		assertEquals("Checking IRRADIATION_ADMINISTERING content item hashCode equality",RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_ADMINISTERING).hashCode(),RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_ADMINISTERING).hashCode());
		assertEquals("Checking IRRADIATION_ADMINISTERING content item equality",RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_ADMINISTERING),RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_ADMINISTERING));
	}
	
	public void TestRoleInProcedure_IrradiationAuthorizing_Equality() throws Exception  {
		
		assertEquals("Checking IRRADIATION_AUTHORIZING scope equality",RoleInProcedure.IRRADIATION_AUTHORIZING,RoleInProcedure.IRRADIATION_AUTHORIZING);
		assertEquals("Checking IRRADIATION_AUTHORIZING content item hashCode equality",RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_AUTHORIZING).hashCode(),RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_AUTHORIZING).hashCode());
		assertEquals("Checking IRRADIATION_AUTHORIZING content item equality",RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_AUTHORIZING),RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_AUTHORIZING));
	}
	
	public void TestRoleInProcedure_IrradiationAdministering_IrradiationAuthorizing_Inequality() throws Exception  {
		
		assertFalse("Checking IRRADIATION_ADMINISTERING versus IRRADIATION_AUTHORIZING scope inequality",RoleInProcedure.IRRADIATION_ADMINISTERING.equals(RoleInProcedure.IRRADIATION_AUTHORIZING));
		assertFalse("Checking IRRADIATION_ADMINISTERING versus IRRADIATION_AUTHORIZING content item hashCode inequality",RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_ADMINISTERING).hashCode() == RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_AUTHORIZING).hashCode());
		assertFalse("Checking IRRADIATION_ADMINISTERING versus IRRADIATION_AUTHORIZING content item inequality",RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_ADMINISTERING).equals(RoleInProcedure.getCodedSequenceItem(RoleInProcedure.IRRADIATION_AUTHORIZING)));
	}
	
	
}
