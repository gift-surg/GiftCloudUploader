/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

import java.util.Map;

import com.pixelmed.dicom.ContentItem;

public class TestRecordingDeviceObserverContext extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestRecordingDeviceObserverContext(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestRecordingDeviceObserverContext.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestRecordingDeviceObserverContext");
		
		suite.addTest(new TestRecordingDeviceObserverContext("TestRecordingDeviceObserverContext_WithAllParameters"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestRecordingDeviceObserverContext_WithAllParameters() throws Exception {
		String uid = "1.2.3.4";
		String name = "station1";
		String manufacturer = "Acme";
		String modelName = "Scanner";
		String serialNumber = "72349236741";
		String location = "Suite1";
		
		RecordingDeviceObserverContext rdoc = new RecordingDeviceObserverContext(uid,name,manufacturer,modelName,serialNumber,location);
		
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
	}
	
}

