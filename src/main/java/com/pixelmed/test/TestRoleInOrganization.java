/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

public class TestRoleInOrganization extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestRoleInOrganization(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestRoleInOrganization.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestRoleInOrganization");
		
		suite.addTest(new TestRoleInOrganization("TestRoleInOrganization_Physician_Description"));
		suite.addTest(new TestRoleInOrganization("TestRoleInOrganization_Physician_Equality"));
		
		suite.addTest(new TestRoleInOrganization("TestRoleInOrganization_Technologist_Description"));
		suite.addTest(new TestRoleInOrganization("TestRoleInOrganization_Technologist_Equality"));
		
		suite.addTest(new TestRoleInOrganization("TestRoleInOrganization_RadiationPhysicist_Description"));
		suite.addTest(new TestRoleInOrganization("TestRoleInOrganization_RadiationPhysicist_Equality"));
		
		suite.addTest(new TestRoleInOrganization("TestRoleInOrganization_Physician_Technologist_Inequality"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestRoleInOrganization_Physician_Description() {
		
		assertEquals("Checking PHYSICIAN scope description",RoleInOrganization.PHYSICIAN.toString(),"Physician");
	}
	
	public void TestRoleInOrganization_Technologist_Description() {
		
		assertEquals("Checking TECHNOLOGIST scope description",RoleInOrganization.TECHNOLOGIST.toString(),"Technologist");
	}
	
	public void TestRoleInOrganization_RadiationPhysicist_Description() {
		
		assertEquals("Checking RADIATION_PHYSICIST scope description",RoleInOrganization.RADIATION_PHYSICIST.toString(),"Radiation Physicist");
	}
	
	public void TestRoleInOrganization_Physician_Equality() throws Exception {
		
		assertEquals("Checking PHYSICIAN scope equality",RoleInOrganization.PHYSICIAN,RoleInOrganization.PHYSICIAN);
		assertEquals("Checking PHYSICIAN content item hashCode equality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.PHYSICIAN).hashCode(),RoleInOrganization.getCodedSequenceItem(RoleInOrganization.PHYSICIAN).hashCode());
		assertEquals("Checking PHYSICIAN content item equality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.PHYSICIAN),RoleInOrganization.getCodedSequenceItem(RoleInOrganization.PHYSICIAN));
	}
	
	public void TestRoleInOrganization_Technologist_Equality() throws Exception  {
		
		assertEquals("Checking TECHNOLOGIST scope equality",RoleInOrganization.TECHNOLOGIST,RoleInOrganization.TECHNOLOGIST);
		assertEquals("Checking TECHNOLOGIST content item hashCode equality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.TECHNOLOGIST).hashCode(),RoleInOrganization.getCodedSequenceItem(RoleInOrganization.TECHNOLOGIST).hashCode());
		assertEquals("Checking TECHNOLOGIST content item equality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.TECHNOLOGIST),RoleInOrganization.getCodedSequenceItem(RoleInOrganization.TECHNOLOGIST));
	}
	
	public void TestRoleInOrganization_RadiationPhysicist_Equality() throws Exception  {
		
		assertEquals("Checking RADIATION_PHYSICIST scope equality",RoleInOrganization.RADIATION_PHYSICIST,RoleInOrganization.RADIATION_PHYSICIST);
		assertEquals("Checking RADIATION_PHYSICIST content item hashCode equality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.RADIATION_PHYSICIST).hashCode(),RoleInOrganization.getCodedSequenceItem(RoleInOrganization.RADIATION_PHYSICIST).hashCode());
		assertEquals("Checking RADIATION_PHYSICIST content item equality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.RADIATION_PHYSICIST),RoleInOrganization.getCodedSequenceItem(RoleInOrganization.RADIATION_PHYSICIST));
	}
	
	public void TestRoleInOrganization_Physician_Technologist_Inequality() throws Exception  {
		
		assertFalse("Checking PHYSICIAN versus TECHNOLOGIST scope inequality",RoleInOrganization.PHYSICIAN.equals(RoleInOrganization.TECHNOLOGIST));
		assertFalse("Checking PHYSICIAN versus TECHNOLOGIST content item hashCode inequality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.PHYSICIAN).hashCode() == RoleInOrganization.getCodedSequenceItem(RoleInOrganization.TECHNOLOGIST).hashCode());
		assertFalse("Checking PHYSICIAN versus TECHNOLOGIST content item inequality",RoleInOrganization.getCodedSequenceItem(RoleInOrganization.PHYSICIAN).equals(RoleInOrganization.getCodedSequenceItem(RoleInOrganization.TECHNOLOGIST)));
	}
	
	
}
