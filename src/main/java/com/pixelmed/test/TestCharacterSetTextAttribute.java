/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.HexDump;

import junit.framework.*;

import java.io.ByteArrayOutputStream;

public class TestCharacterSetTextAttribute extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCharacterSetTextAttribute(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCharacterSetTextAttribute.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCharacterSetTextAttribute");
		
		suite.addTest(new TestCharacterSetTextAttribute("TestCharacterSetTextAttribute_AddValueAndWriteItDefaultEncoding"));
		suite.addTest(new TestCharacterSetTextAttribute("TestCharacterSetTextAttribute_AddValueAndWriteIt"));
		suite.addTest(new TestCharacterSetTextAttribute("TestCharacterSetTextAttribute_SetValueAndWriteIt"));
		suite.addTest(new TestCharacterSetTextAttribute("TestCharacterSetTextAttribute_AddStringValueFromByteArrayAndWriteIt"));
		//suite.addTest(new TestCharacterSetTextAttribute("TestCharacterSetTextAttribute_SetByteArrayValuesAndWriteIt"));	// this method is not implemented and returns exception
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCharacterSetTextAttribute_AddValueAndWriteItDefaultEncoding() throws Exception {
		{
			String string = "Doe^Jane";
			UnlimitedTextAttribute a = new UnlimitedTextAttribute(TagFromName.TextValue);
			a.addValue(string);
			assertEquals("Checking TextAttribute.addValue() default encoding string",string,a.getStringValues()[0]);

			byte[] bytesSupplied = string.getBytes("ASCII");
			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as default encoding string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as default encoding string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking TextAttribute.getByteValues() bytes as default encoding",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking TextAttribute.write() bytes as default encoding",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*zeroes*/+4/*VL*/,lengthSupplied));
		}
	}
	
	public void TestCharacterSetTextAttribute_AddValueAndWriteIt() throws Exception {
		{
			String string = "Äneas^Rüdiger";
			UnlimitedTextAttribute a = new UnlimitedTextAttribute(TagFromName.TextValue,new SpecificCharacterSet(new String[] {"ISO_IR 192"}));
			a.addValue(string);
			assertEquals("Checking TextAttribute.addValue() UTF-8 string",string,a.getStringValues()[0]);

			byte[] bytesSupplied = string.getBytes("UTF8");
			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking TextAttribute.getByteValues() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking TextAttribute.write() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*zeroes*/+4/*VL*/,lengthSupplied));
		}
	}
	
	public void TestCharacterSetTextAttribute_SetValueAndWriteIt() throws Exception {
		{
			String string = "Äneas^Rüdiger";
			UnlimitedTextAttribute a = new UnlimitedTextAttribute(TagFromName.TextValue,new SpecificCharacterSet(new String[] {"ISO_IR 192"}));
			a.setValue(string);
			assertEquals("Checking TextAttribute.setValue() UTF-8 string",string,a.getStringValues()[0]);

			byte[] bytesSupplied = string.getBytes("UTF8");
			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking TextAttribute.getByteValues() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking TextAttribute.write() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*zeroes*/+4/*VL*/,lengthSupplied));
		}
	}
		
	public void TestCharacterSetTextAttribute_AddStringValueFromByteArrayAndWriteIt() throws Exception {
		{
			byte[] bytesSupplied = { 97, 32, 61, 32, 57, 48, -17, -65, -67, 32, 40, 50, 55, 48, -17, -65, -67, 41 };	// values from Sputnik's bug report
			String string = new String(bytesSupplied,"UTF8");
			UnlimitedTextAttribute a = new UnlimitedTextAttribute(TagFromName.TextValue,new SpecificCharacterSet(new String[] {"ISO_IR 192"}));
			a.addValue(string);
			assertEquals("Checking TextAttribute.addValue() UTF-8 string",string,a.getStringValues()[0]);

			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking TextAttribute.getByteValues() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking TextAttribute.write() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*zeroes*/+4/*VL*/,lengthSupplied));
		}
	}
		
	public void TestCharacterSetTextAttribute_SetByteArrayValuesAndWriteIt() throws Exception {
		{
			byte[] bytesSupplied = { 97, 32, 61, 32, 57, 48, -17, -65, -67, 32, 40, 50, 55, 48, -17, -65, -67, 41 };	// values from Sputnik's bug report
			UnlimitedTextAttribute a = new UnlimitedTextAttribute(TagFromName.TextValue,new SpecificCharacterSet(new String[] {"ISO_IR 192"}));
			a.setValues(bytesSupplied);

			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking TextAttribute.getVL() correct for bytes as UTF-8 string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking TextAttribute.getByteValues() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking TextAttribute.write() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*zeroes*/+4/*VL*/,lengthSupplied));
		}
	}
}
