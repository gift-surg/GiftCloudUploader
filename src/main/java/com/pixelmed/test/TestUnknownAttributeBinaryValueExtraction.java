/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.HexDump;

import junit.framework.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TestUnknownAttributeBinaryValueExtraction extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestUnknownAttributeBinaryValueExtraction(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestUnknownAttributeBinaryValueExtraction.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestUnknownAttributeBinaryValueExtraction");
		
		suite.addTest(new TestUnknownAttributeBinaryValueExtraction("TestUnknownAttributeBinaryValueExtraction_WriteFloatDoubleValuesAsPrivateAttributeInImplicitVRAndExtractFromUnkownVRAfterReading"));
		suite.addTest(new TestUnknownAttributeBinaryValueExtraction("TestUnknownAttributeBinaryValueExtraction_WriteFloatSingleValuesAsPrivateAttributeInImplicitVRAndExtractFromUnkownVRAfterReading"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestUnknownAttributeBinaryValueExtraction_WriteFloatDoubleValuesAsPrivateAttributeInImplicitVRAndExtractFromUnkownVRAfterReading() throws Exception {
		{
			double value1 = 39.76d;
			double value2 = -56356.457457d;
			double value3 = 0d;
			double value4 = 7.45345345e-21d;
			
			AttributeTag tag = new AttributeTag(0x0011,0x1010);
			
			FloatDoubleAttribute a = new FloatDoubleAttribute(tag);
			a.addValue(value1);
			a.addValue(value2);
			a.addValue(value3);
			a.addValue(value4);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,null/*meta ts*/,TransferSyntax.ImplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			AttributeList list = new AttributeList();
			list.read(new DicomInputStream(bis,TransferSyntax.ImplicitVRLittleEndian,false/*meta*/));
			Attribute aRead = list.get(tag);
			assertEquals("VR",ValueRepresentation.UN,aRead.getVR());
			double[] readValues = aRead.getDoubleValues();
			assertEquals("Checking value1",value1,readValues[0]);
			assertEquals("Checking value2",value2,readValues[1]);
			assertEquals("Checking value3",value3,readValues[2]);
			assertEquals("Checking value4",value4,readValues[3]);
		}
	}
	
	public void TestUnknownAttributeBinaryValueExtraction_WriteFloatSingleValuesAsPrivateAttributeInImplicitVRAndExtractFromUnkownVRAfterReading() throws Exception {
		{
			float value1 = 39.76f;
			float value2 = -56356.457457f;
			float value3 = 0f;
			float value4 = 7.45345345e-21f;
			
			AttributeTag tag = new AttributeTag(0x0011,0x1010);
			
			FloatSingleAttribute a = new FloatSingleAttribute(tag);
			a.addValue(value1);
			a.addValue(value2);
			a.addValue(value3);
			a.addValue(value4);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,null/*meta ts*/,TransferSyntax.ImplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			AttributeList list = new AttributeList();
			list.read(new DicomInputStream(bis,TransferSyntax.ImplicitVRLittleEndian,false/*meta*/));
			Attribute aRead = list.get(tag);
			assertEquals("VR",ValueRepresentation.UN,aRead.getVR());
			float[] readValues = aRead.getFloatValues();
			assertEquals("Checking value1",value1,readValues[0]);
			assertEquals("Checking value2",value2,readValues[1]);
			assertEquals("Checking value3",value3,readValues[2]);
			assertEquals("Checking value4",value4,readValues[3]);
		}
	}
	
}
