/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

import com.pixelmed.dicom.ContentItem;

public class TestPersonParticipant extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestPersonParticipant(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestPersonParticipant.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestPersonParticipant");
		
		suite.addTest(new TestPersonParticipant("TestPersonParticipant_WithAllParameters"));
		suite.addTest(new TestPersonParticipant("TestPersonParticipant_WithEmptyName"));
		suite.addTest(new TestPersonParticipant("TestPersonParticipant_WithNullName"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestPersonParticipant_WithAllParameters() throws Exception {
		String name = "Smith^John";
		RoleInProcedure roleInProcedure = RoleInProcedure.IRRADIATION_ADMINISTERING;
		String id = "74682364821";
		String idIssuer = "99BLA";
		String organization = "St. Elsewhere's";
		RoleInOrganization roleInOrganization = RoleInOrganization.TECHNOLOGIST;
		
		PersonParticipant pp = new PersonParticipant(name,roleInProcedure,id,idIssuer,organization,roleInOrganization);
		
		assertEquals("Checking name",name,pp.getName());
		assertEquals("Checking roleInProcedure",roleInProcedure,pp.getRoleInProcedure());
		assertEquals("Checking id",id,pp.getId());
		assertEquals("Checking idIssuer",idIssuer,pp.getIdIssuer());
		assertEquals("Checking organization",organization,pp.getOrganization());
		assertEquals("Checking roleInOrganization",roleInOrganization,pp.getRoleInOrganization());
		
		ContentItem ppRoot = pp.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: PNAME: Person Name = "+name,ppRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Procedure = Irradiation Administering",ppRoot.getNamedChild("DCM","113875").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID = "+id,ppRoot.getNamedChild("DCM","113871").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID Issuer = "+idIssuer,ppRoot.getNamedChild("DCM","113872").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Organization Name = "+organization,ppRoot.getNamedChild("DCM","113873").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Organization = Technologist",ppRoot.getNamedChild("DCM","113874").toString());
	}
	
	public void TestPersonParticipant_WithEmptyName() throws Exception {
		String name = "";
		RoleInProcedure roleInProcedure = RoleInProcedure.IRRADIATION_ADMINISTERING;
		String id = "74682364821";
		String idIssuer = "99BLA";
		String organization = "St. Elsewhere's";
		RoleInOrganization roleInOrganization = RoleInOrganization.TECHNOLOGIST;
		
		String expectedNameInSR = "Nobody";
		
		PersonParticipant pp = new PersonParticipant(name,roleInProcedure,id,idIssuer,organization,roleInOrganization);
		
		assertEquals("Checking name",name,pp.getName());
		assertEquals("Checking roleInProcedure",roleInProcedure,pp.getRoleInProcedure());
		assertEquals("Checking id",id,pp.getId());
		assertEquals("Checking idIssuer",idIssuer,pp.getIdIssuer());
		assertEquals("Checking organization",organization,pp.getOrganization());
		assertEquals("Checking roleInOrganization",roleInOrganization,pp.getRoleInOrganization());
		
		ContentItem ppRoot = pp.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: PNAME: Person Name = "+expectedNameInSR,ppRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Procedure = Irradiation Administering",ppRoot.getNamedChild("DCM","113875").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID = "+id,ppRoot.getNamedChild("DCM","113871").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID Issuer = "+idIssuer,ppRoot.getNamedChild("DCM","113872").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Organization Name = "+organization,ppRoot.getNamedChild("DCM","113873").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Organization = Technologist",ppRoot.getNamedChild("DCM","113874").toString());
	}
	
	public void TestPersonParticipant_WithNullName() throws Exception {
		String name = null;
		RoleInProcedure roleInProcedure = RoleInProcedure.IRRADIATION_ADMINISTERING;
		String id = "74682364821";
		String idIssuer = "99BLA";
		String organization = "St. Elsewhere's";
		RoleInOrganization roleInOrganization = RoleInOrganization.TECHNOLOGIST;
		
		String expectedNameInSR = "Nobody";
		
		PersonParticipant pp = new PersonParticipant(name,roleInProcedure,id,idIssuer,organization,roleInOrganization);
		
		assertEquals("Checking name",name,pp.getName());
		assertEquals("Checking roleInProcedure",roleInProcedure,pp.getRoleInProcedure());
		assertEquals("Checking id",id,pp.getId());
		assertEquals("Checking idIssuer",idIssuer,pp.getIdIssuer());
		assertEquals("Checking organization",organization,pp.getOrganization());
		assertEquals("Checking roleInOrganization",roleInOrganization,pp.getRoleInOrganization());
		
		ContentItem ppRoot = pp.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: PNAME: Person Name = "+expectedNameInSR,ppRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Procedure = Irradiation Administering",ppRoot.getNamedChild("DCM","113875").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID = "+id,ppRoot.getNamedChild("DCM","113871").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID Issuer = "+idIssuer,ppRoot.getNamedChild("DCM","113872").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Organization Name = "+organization,ppRoot.getNamedChild("DCM","113873").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Organization = Technologist",ppRoot.getNamedChild("DCM","113874").toString());
	}
}

