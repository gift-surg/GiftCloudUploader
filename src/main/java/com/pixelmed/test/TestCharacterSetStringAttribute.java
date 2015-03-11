/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.HexDump;

import junit.framework.*;

import java.io.ByteArrayOutputStream;

public class TestCharacterSetStringAttribute extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCharacterSetStringAttribute(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCharacterSetStringAttribute.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCharacterSetStringAttribute");
		
		suite.addTest(new TestCharacterSetStringAttribute("TestCharacterSetStringAttribute_AddValueAndWriteIt"));
		suite.addTest(new TestCharacterSetStringAttribute("TestCharacterSetStringAttribute_AddTwoValuesAndWriteThem"));
		suite.addTest(new TestCharacterSetStringAttribute("TestCharacterSetStringAttribute_AddTwoValuesAndWriteThemDefaultEncoding"));
		suite.addTest(new TestCharacterSetStringAttribute("TestCharacterSetStringAttribute_AddTwoValuesAndWriteASCII"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCharacterSetStringAttribute_AddValueAndWriteIt() throws Exception {
		{
			SpecificCharacterSet charSet = new SpecificCharacterSet(new String[] { new String("ISO_IR 192")});
			assertEquals("Checking getValueToUseInSpecificCharacterSetAttribute()","ISO_IR 192",charSet.getValueToUseInSpecificCharacterSetAttribute());

			String string = "Äneas^Rüdiger";
			PersonNameAttribute a = new PersonNameAttribute(TagFromName.PersonName,charSet);
			a.addValue(string);
			assertEquals("Checking StringAttribute.addValue() UTF-8 string",string,a.getStringValues()[0]);

			byte[] bytesSupplied = string.getBytes("UTF8");
			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as UTF-8 string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as UTF-8 string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking StringAttribute.getByteValues() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking StringAttribute.write() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*VL*/,lengthSupplied));
		}
	}
	
	public void TestCharacterSetStringAttribute_AddTwoValuesAndWriteThem() throws Exception {
		{
			SpecificCharacterSet charSet = new SpecificCharacterSet(new String[] { new String("ISO_IR 192")});
			assertEquals("Checking getValueToUseInSpecificCharacterSetAttribute()","ISO_IR 192",charSet.getValueToUseInSpecificCharacterSetAttribute());

			String string1 = "Äneas^Rüdiger";
			String string2 = "Buc^Jérôme";
			PersonNameAttribute a = new PersonNameAttribute(TagFromName.PersonName,charSet);
			a.addValue(string1);
			a.addValue(string2);
			assertEquals("Checking StringAttribute.addValue() UTF-8 string1",string1,a.getStringValues()[0]);
			assertEquals("Checking StringAttribute.addValue() UTF-8 string2",string2,a.getStringValues()[1]);

			byte[] bytesSupplied1 = string1.getBytes("UTF8");
			int lengthSupplied1 = bytesSupplied1.length;
			byte[] bytesSupplied2 = string2.getBytes("UTF8");
			int lengthSupplied2 = bytesSupplied2.length;

			byte[] bytesSupplied = new byte[lengthSupplied1+1+lengthSupplied2];
			System.arraycopy(bytesSupplied1,0,bytesSupplied,0,lengthSupplied1);
			bytesSupplied[lengthSupplied1]=0x5C;	// backslash delimited
			System.arraycopy(bytesSupplied2,0,bytesSupplied,lengthSupplied1+1,lengthSupplied2);
			
			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as UTF-8 string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as UTF-8 string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking StringAttribute.getByteValues() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking StringAttribute.write() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*VL*/,lengthSupplied));
		}
	}
	
	public void TestCharacterSetStringAttribute_AddTwoValuesAndWriteThemDefaultEncoding() throws Exception {
		{
			String string1 = "Doe^Jane";
			String string2 = "Smith^John";
			PersonNameAttribute a = new PersonNameAttribute(TagFromName.PersonName);
			a.addValue(string1);
			a.addValue(string2);
			assertEquals("Checking StringAttribute.addValue() default encoding string1",string1,a.getStringValues()[0]);
			assertEquals("Checking StringAttribute.addValue() default encoding string2",string2,a.getStringValues()[1]);
			
			SpecificCharacterSet charSet = ((StringAttribute)a).getSpecificCharacterSet();
			assertTrue("Checking getSpecificCharacterSet() is null",charSet == null);

			byte[] bytesSupplied1 = string1.getBytes("ASCII");
			int lengthSupplied1 = bytesSupplied1.length;
			byte[] bytesSupplied2 = string2.getBytes("ASCII");
			int lengthSupplied2 = bytesSupplied2.length;

			byte[] bytesSupplied = new byte[lengthSupplied1+1+lengthSupplied2];
			System.arraycopy(bytesSupplied1,0,bytesSupplied,0,lengthSupplied1);
			bytesSupplied[lengthSupplied1]=0x5C;	// backslash delimited
			System.arraycopy(bytesSupplied2,0,bytesSupplied,lengthSupplied1+1,lengthSupplied2);
			
			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as default encoding string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as default encoding string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking StringAttribute.getByteValues() bytes as default encoding",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking StringAttribute.write() bytes as default encoding",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*VL*/,lengthSupplied));
		}
	}
	
	public void TestCharacterSetStringAttribute_AddTwoValuesAndWriteASCII() throws Exception {
		{
			SpecificCharacterSet charSet = new SpecificCharacterSet(new String[] { new String("")});
			assertEquals("Checking getValueToUseInSpecificCharacterSetAttribute()","",charSet.getValueToUseInSpecificCharacterSetAttribute());

			String string1 = "Aneas^Rudiger";
			String string2 = "Buc^Jerome";
			PersonNameAttribute a = new PersonNameAttribute(TagFromName.PersonName,charSet);
			a.addValue(string1);
			a.addValue(string2);
			assertEquals("Checking StringAttribute.addValue() UTF-8 string1",string1,a.getStringValues()[0]);
			assertEquals("Checking StringAttribute.addValue() UTF-8 string2",string2,a.getStringValues()[1]);

			byte[] bytesSupplied1 = string1.getBytes("UTF8");
			int lengthSupplied1 = bytesSupplied1.length;
			byte[] bytesSupplied2 = string2.getBytes("UTF8");
			int lengthSupplied2 = bytesSupplied2.length;

			byte[] bytesSupplied = new byte[lengthSupplied1+1+lengthSupplied2];
			System.arraycopy(bytesSupplied1,0,bytesSupplied,0,lengthSupplied1);
			bytesSupplied[lengthSupplied1]=0x5C;	// backslash delimited
			System.arraycopy(bytesSupplied2,0,bytesSupplied,lengthSupplied1+1,lengthSupplied2);
			
			int lengthSupplied = bytesSupplied.length;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as UTF-8 string",lengthSupplied,a.getVL());
			
			int lengthSuppliedPadded = (lengthSupplied%2) == 0 ? lengthSupplied : lengthSupplied+1;
			assertEquals("Checking StringAttribute.getVL() correct for bytes as UTF-8 string",lengthSuppliedPadded,a.getPaddedVL());
			
			byte[] bytesRetrieved = a.getByteValues();
			assertEquals("Checking StringAttribute.getByteValues() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesRetrieved,0,lengthSupplied));

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DicomOutputStream dos = new DicomOutputStream(bos,TransferSyntax.ExplicitVRLittleEndian,TransferSyntax.ExplicitVRLittleEndian);
			a.write(dos);
			dos.close();
			byte[] bytesWritten = bos.toByteArray();
			assertEquals("Checking StringAttribute.write() bytes as UTF-8",HexDump.byteArrayToHexString(bytesSupplied),HexDump.byteArrayToHexString(bytesWritten,128/*preamble*/+4/*DICM*/+4/*tag*/+2/*VR*/+2/*VL*/,lengthSupplied));
		}
	}
	
}
