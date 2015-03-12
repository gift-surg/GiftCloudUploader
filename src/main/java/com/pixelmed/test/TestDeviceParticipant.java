/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import com.pixelmed.dose.*;

import junit.framework.*;

import com.pixelmed.dicom.ContentItem;

public class TestDeviceParticipant extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDeviceParticipant(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDeviceParticipant.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDeviceParticipant");
		
		suite.addTest(new TestDeviceParticipant("TestDeviceParticipant_WithManufacturerModelSerialNumberAndUID"));
		suite.addTest(new TestDeviceParticipant("TestDeviceParticipant_WithManufacturerModelSerialNumberButNoUID"));
		suite.addTest(new TestDeviceParticipant("TestDeviceParticipant_SerialNumberWithSerialNumberPresent"));
		suite.addTest(new TestDeviceParticipant("TestDeviceParticipant_SerialNumberWithNoSerialNumberPresentButStationName"));
		suite.addTest(new TestDeviceParticipant("TestDeviceParticipant_SerialNumberWithNoSerialNumberPresentButInstitutionName"));
		suite.addTest(new TestDeviceParticipant("TestDeviceParticipant_SerialNumberWithNoSerialNumberPresentButStationAndInstitutionName"));
		suite.addTest(new TestDeviceParticipant("TestDeviceParticipant_UIDSynthesisWithSerialNumberManufacturerModelStationInstitutionName"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestDeviceParticipant_WithManufacturerModelSerialNumberAndUID() throws Exception {
		String manufacturer = "Acme";
		String modelName = "Scanner";
		String serialNumber = "72349236741";
		String deviceUID = "1.2.3.4";
		
		DeviceParticipant dp = new DeviceParticipant(manufacturer,modelName,serialNumber,deviceUID);

		assertEquals("Checking manufacturer",manufacturer,dp.getManufacturer());
		assertEquals("Checking modelName",modelName,dp.getModelName());
		assertEquals("Checking serialNumber",serialNumber,dp.getSerialNumber());
		assertEquals("Checking deviceUID",deviceUID,dp.getUID());
		
		ContentItem dpRoot = dp.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: CODE: Device Role in Procedure = Irradiating Device",dpRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Manufacturer = "+manufacturer,dpRoot.getNamedChild("DCM","113878").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Model Name = "+modelName,dpRoot.getNamedChild("DCM","113879").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Serial Number = "+serialNumber,dpRoot.getNamedChild("DCM","113880").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: UIDREF: Device Observer UID = "+deviceUID,dpRoot.getNamedChild("DCM","121012").toString());
	}
	
	public void TestDeviceParticipant_WithManufacturerModelSerialNumberButNoUID() throws Exception {
		String manufacturer = "Acme";
		String modelName = "Scanner";
		String serialNumber = "72349236741";
		
		@SuppressWarnings("deprecation") DeviceParticipant dp = new DeviceParticipant(manufacturer,modelName,serialNumber);

		assertEquals("Checking manufacturer",manufacturer,dp.getManufacturer());
		assertEquals("Checking modelName",modelName,dp.getModelName());
		assertEquals("Checking serialNumber",serialNumber,dp.getSerialNumber());
		
		ContentItem dpRoot = dp.getStructuredReportFragment();
		assertEquals("Checking SR fragment","CONTAINS: CODE: Device Role in Procedure = Irradiating Device",dpRoot.toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Manufacturer = "+manufacturer,dpRoot.getNamedChild("DCM","113878").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Model Name = "+modelName,dpRoot.getNamedChild("DCM","113879").toString());
		assertEquals("Checking SR fragment","HAS PROPERTIES: TEXT: Device Serial Number = "+serialNumber,dpRoot.getNamedChild("DCM","113880").toString());
		assertEquals("Checking SR fragment",null,dpRoot.getNamedChild("DCM","121012"));
	}
	
	public void TestDeviceParticipant_SerialNumberWithSerialNumberPresent() throws Exception {
		String serialNumber = "72349236741";
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(TagFromName.DeviceSerialNumber); a.addValue(serialNumber); list.put(a); }
		String gotSerialNumber = DeviceParticipant.getDeviceSerialNumberOrSuitableAlternative(list,false);
		assertEquals("Checking serialNumber",serialNumber,gotSerialNumber);
	}
	
	public void TestDeviceParticipant_SerialNumberWithNoSerialNumberPresentButStationName() throws Exception {
		String stationName = "BLASTAT1";
		String serialNumberExpected = "420ea865fb1878adc17ddc02f83c97301862f183";
		AttributeList list = new AttributeList();
		{ Attribute a = new ShortStringAttribute(TagFromName.StationName); a.addValue(stationName); list.put(a); }
		assertEquals("Checking serialNumber returned",serialNumberExpected,DeviceParticipant.getDeviceSerialNumberOrSuitableAlternative(list,true));
		assertEquals("Checking serialNumber in list",serialNumberExpected,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.DeviceSerialNumber));
	}
	
	public void TestDeviceParticipant_SerialNumberWithNoSerialNumberPresentButInstitutionName() throws Exception {
		String institutionName = "St. Elsewhere";
		String serialNumberExpected = "f79b1268bfca6bfddcc9c8ef43c5312a63b5f4fd";
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(TagFromName.InstitutionName); a.addValue(institutionName); list.put(a); }
		assertEquals("Checking serialNumber returned",serialNumberExpected,DeviceParticipant.getDeviceSerialNumberOrSuitableAlternative(list,true));
		assertEquals("Checking serialNumber in list",serialNumberExpected,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.DeviceSerialNumber));
	}
	
	public void TestDeviceParticipant_SerialNumberWithNoSerialNumberPresentButStationAndInstitutionName() throws Exception {
		String institutionName = "St. Elsewhere";
		String stationName = "BLASTAT1";
		String serialNumberExpected = "72df0a054f1dd39cfa93d8ade3671371429fa4b6";
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(TagFromName.InstitutionName); a.addValue(institutionName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StationName); a.addValue(stationName); list.put(a); }
		assertEquals("Checking serialNumber returned",serialNumberExpected,DeviceParticipant.getDeviceSerialNumberOrSuitableAlternative(list,true));
		assertEquals("Checking serialNumber in list",serialNumberExpected,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.DeviceSerialNumber));
	}
	
	public void TestDeviceParticipant_UIDSynthesisWithSerialNumberManufacturerModelStationInstitutionName() throws Exception {
		String institutionName = "St. Elsewhere";
		String stationName = "BLASTAT1";
		String manufacturer = "Acme";
		String modelName = "Scanner";
		String serialNumber = "72349236741";
		
		String uidExpected = "2.25.233262431311849527083297767009481775975";

		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(TagFromName.InstitutionName); a.addValue(institutionName); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StationName); a.addValue(stationName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue(manufacturer); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(modelName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.DeviceSerialNumber); a.addValue(serialNumber); list.put(a); }

		assertEquals("Checking UID returned",uidExpected,DeviceParticipant.getDeviceObserverUIDOrSuitableAlternative(list));
	}
	
}

