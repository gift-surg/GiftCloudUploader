/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestPrivateCreatorValueRepresentation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestPrivateCreatorValueRepresentation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestPrivateCreatorValueRepresentation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestPrivateCreatorValueRepresentation");
		
		suite.addTest(new TestPrivateCreatorValueRepresentation("TestPrivateCreatorValueRepresentation_FromImplicitVR"));
		suite.addTest(new TestPrivateCreatorValueRepresentation("TestPrivateCreatorValueRepresentation_FromExplicitVR"));
		suite.addTest(new TestPrivateCreatorValueRepresentation("TestPrivateCreatorValueRepresentation_FromExplicitUNVR"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestPrivateCreatorValueRepresentation_FromImplicitVR() throws Exception {
		String creatorValue = "Test Creator";
		File testFile = File.createTempFile("TestPrivateCreatorValueRepresentation_FromImplicitVR",".dcm");
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0011,0x0010)); a.addValue(creatorValue); list.put(a); }
//System.err.print("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromImplicitVR(): writing\n"+list);
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile);
//System.err.print("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromImplicitVR(): read\n"+list);
			Attribute a = list.get(new AttributeTag(0x0011,0x0010));
//System.err.println("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromImplicitVR(): a "+a);
			assertTrue("Checking creator was read",a != null);
			assertTrue("Checking creator is LO",a instanceof LongStringAttribute);
			assertEquals("Checking creator value",creatorValue,a.getSingleStringValueOrNull());

		}
	}
	
	public void TestPrivateCreatorValueRepresentation_FromExplicitVR() throws Exception {
		String creatorValue = "Test Creator";
		File testFile = File.createTempFile("TestPrivateCreatorValueRepresentation_FromExplicitVR",".dcm");
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0011,0x0010)); a.addValue(creatorValue); list.put(a); }
//System.err.print("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromExplicitVR(): writing\n"+list);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile);
//System.err.print("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromExplicitVR(): read\n"+list);
			Attribute a = list.get(new AttributeTag(0x0011,0x0010));
//System.err.println("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromExplicitVR(): a "+a);
			assertTrue("Checking creator was read",a != null);
			assertTrue("Checking creator is LO",a instanceof LongStringAttribute);
			assertEquals("Checking creator value",creatorValue,a.getSingleStringValueOrNull());

		}
	}
	
	protected class OurUnknownAttribute extends UnknownAttribute {
		OurUnknownAttribute(AttributeTag t,String value) {
			super(t);
			originalLittleEndianByteValues = value.getBytes();
			valueLength=originalLittleEndianByteValues.length;
			assert(valueLength%2 == 0);
			valueMultiplicity=1;
		}
	}
	
	public void TestPrivateCreatorValueRepresentation_FromExplicitUNVR() throws Exception {
		String creatorValue = "Test Creator";
		//File testFile = File.createTempFile("TestPrivateCreatorValueRepresentation_FromExplicitUNVR",".dcm");
		File testFile = new File("/tmp/holdforme");
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new OurUnknownAttribute(new AttributeTag(0x0011,0x0010),creatorValue); list.put(a); }
//System.err.print("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromExplicitUNVR(): writing\n"+list);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile);
//System.err.print("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromExplicitUNVR(): read\n"+list);
			Attribute a = list.get(new AttributeTag(0x0011,0x0010));
//System.err.println("TestPrivateCreatorValueRepresentation.TestPrivateCreatorValueRepresentation_FromExplicitUNVR(): a "+a);
			assertTrue("Checking creator was read",a != null);
			assertTrue("Checking creator is LO",a instanceof LongStringAttribute);
			assertEquals("Checking creator value",creatorValue,a.getSingleStringValueOrNull());

		}
	}
	
}
