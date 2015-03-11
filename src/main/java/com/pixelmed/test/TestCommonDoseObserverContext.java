/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

import java.util.Map;

import com.pixelmed.dicom.ContentItem;

public class TestCommonDoseObserverContext extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCommonDoseObserverContext(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTDose.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCommonDoseObserverContext");
		
		suite.addTest(new TestCommonDoseObserverContext("testCommonDoseObserverContext_WithAllParameters"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void testCommonDoseObserverContext_WithAllParameters() throws Exception {
		String uid = "1.2.3.4";
		String name = "station1";
		String manufacturer = "Acme";
		String modelName = "Scanner";
		String serialNumber = "72349236741";
		String location = "Suite1";
		
		String operatorName = "Smith^John";
		String operatorID = "26354781234";
		String physicianName = "Jones^Mary";
		String physicianID = "23491234234";
		String idIssuer = "99BLA";
		String organization = "St. Elsewhere's";
		
		CommonDoseObserverContext cdoc = new CommonDoseObserverContext(uid,name,manufacturer,modelName,serialNumber,location,operatorName,operatorID,physicianName,physicianID,idIssuer,organization);

		RecordingDeviceObserverContext rdoc = cdoc.getRecordingDeviceObserverContext();
		
		assertEquals("Checking uid",uid,rdoc.getUID());
		assertEquals("Checking name",name,rdoc.getName());
		assertEquals("Checking manufacturer",manufacturer,rdoc.getManufacturer());
		assertEquals("Checking modelName",modelName,rdoc.getModelName());
		assertEquals("Checking serialNumber",serialNumber,rdoc.getSerialNumber());
		assertEquals("Checking location",location,rdoc.getLocation());
		
		Map<RecordingDeviceObserverContext.Key,ContentItem> cimap = rdoc.getStructuredReportFragment();
		assertEquals("Checking SR fragment","HAS OBS CONTEXT: CODE: Observer Type = Device",cimap.get(RecordingDeviceObserverContext.Key.DEVICE).toString());
		assertEquals("Checking SR fragment","HAS OBS CONTEXT: UIDREF: Device Observer UID = "+uid,cimap.get(RecordingDeviceObserverContext.Key.UID).toString());
		assertEquals("Checking SR fragment","HAS OBS CONTEXT: TEXT: Device Observer Name = "+name,cimap.get(RecordingDeviceObserverContext.Key.NAME).toString());
		assertEquals("Checking SR fragment","HAS OBS CONTEXT: TEXT: Device Observer Manufacturer = "+manufacturer,cimap.get(RecordingDeviceObserverContext.Key.MANUFACTURER).toString());
		assertEquals("Checking SR fragment","HAS OBS CONTEXT: TEXT: Device Observer Model Name = "+modelName,cimap.get(RecordingDeviceObserverContext.Key.MODEL_NAME).toString());
		assertEquals("Checking SR fragment","HAS OBS CONTEXT: TEXT: Device Observer Serial Number = "+serialNumber,cimap.get(RecordingDeviceObserverContext.Key.SERIAL_NUMBER).toString());
		assertEquals("Checking SR fragment","HAS OBS CONTEXT: TEXT: Device Observer Physical Location During Observation = "+location,cimap.get(RecordingDeviceObserverContext.Key.LOCATION).toString());

		DeviceParticipant dp = cdoc.getDeviceParticipant();
		
		assertEquals("Checking manufacturer",manufacturer,dp.getManufacturer());
		assertEquals("Checking modelName",modelName,dp.getModelName());
		assertEquals("Checking serialNumber",serialNumber,dp.getSerialNumber());
		assertEquals("Checking UID",uid,dp.getUID());
		
		ContentItem dpRoot = dp.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: CODE: Device Role in Procedure = Irradiating Device",dpRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Manufacturer = "+manufacturer,dpRoot.getNamedChild("DCM","113878").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Model Name = "+modelName,dpRoot.getNamedChild("DCM","113879").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Serial Number = "+serialNumber,dpRoot.getNamedChild("DCM","113880").toString());
		
		PersonParticipant ppad = cdoc.getPersonParticipantAdministering();
		
		assertEquals("Checking operator name",operatorName,ppad.getName());
		assertEquals("Checking operator roleInProcedure",RoleInProcedure.IRRADIATION_ADMINISTERING,ppad.getRoleInProcedure());
		assertEquals("Checking operator id",operatorID,ppad.getId());
		assertEquals("Checking operator idIssuer",idIssuer,ppad.getIdIssuer());
		assertEquals("Checking operator organization",organization,ppad.getOrganization());
		assertEquals("Checking operator roleInOrganization",RoleInOrganization.TECHNOLOGIST,ppad.getRoleInOrganization());
		
		ContentItem ppadRoot = ppad.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: PNAME: Person Name = "+operatorName,ppadRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Procedure = Irradiation Administering",ppadRoot.getNamedChild("DCM","113875").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID = "+operatorID,ppadRoot.getNamedChild("DCM","113871").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID Issuer = "+idIssuer,ppadRoot.getNamedChild("DCM","113872").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Organization Name = "+organization,ppadRoot.getNamedChild("DCM","113873").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Organization = Technologist",ppadRoot.getNamedChild("DCM","113874").toString());
		
		PersonParticipant ppau = cdoc.getPersonParticipantAuthorizing();
		
		assertEquals("Checking physician name",physicianName,ppau.getName());
		assertEquals("Checking physician roleInProcedure",RoleInProcedure.IRRADIATION_AUTHORIZING,ppau.getRoleInProcedure());
		assertEquals("Checking physician id",physicianID,ppau.getId());
		assertEquals("Checking physician idIssuer",idIssuer,ppau.getIdIssuer());
		assertEquals("Checking physician organization",organization,ppau.getOrganization());
		assertEquals("Checking physician roleInOrganization",RoleInOrganization.PHYSICIAN,ppau.getRoleInOrganization());
		
		ContentItem ppauRoot = ppau.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: PNAME: Person Name = "+physicianName,ppauRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Procedure = Irradiation Authorizing",ppauRoot.getNamedChild("DCM","113875").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID = "+physicianID,ppauRoot.getNamedChild("DCM","113871").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Person ID Issuer = "+idIssuer,ppauRoot.getNamedChild("DCM","113872").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Organization Name = "+organization,ppauRoot.getNamedChild("DCM","113873").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: CODE: Person Role in Organization = Physician",ppauRoot.getNamedChild("DCM","113874").toString());
	}

}

