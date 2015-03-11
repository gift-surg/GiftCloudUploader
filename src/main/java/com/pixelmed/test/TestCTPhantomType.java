/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dose.*;

import junit.framework.*;

public class TestCTPhantomType extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCTPhantomType(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTPhantomType.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCTPhantomType");
		
		suite.addTest(new TestCTPhantomType("TestCTPhantomType_Head16_Description"));
		suite.addTest(new TestCTPhantomType("TestCTPhantomType_Head16_Equality"));
		
		suite.addTest(new TestCTPhantomType("TestCTPhantomType_Body32_Description"));
		suite.addTest(new TestCTPhantomType("TestCTPhantomType_Body32_Equality"));
		
		suite.addTest(new TestCTPhantomType("TestCTPhantomType_Head16_Body32_Inequality"));
		
		suite.addTest(new TestCTPhantomType("TestCTPhantomType_SelectFromDescription"));
		
		suite.addTest(new TestCTPhantomType("TestCTPhantomType_SelectFromCode"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCTPhantomType_Head16_Description() {
		
		assertEquals("Checking HEAD16 description",CTPhantomType.HEAD16.toString(),"HEAD16");
	}
	
	public void TestCTPhantomType_Body32_Description() {
		
		assertEquals("Checking BODY32 description",CTPhantomType.BODY32.toString(),"BODY32");
	}
	
	public void TestCTPhantomType_Head16_Equality() throws Exception {
		
		assertEquals("Checking HEAD16 equality",CTPhantomType.HEAD16,CTPhantomType.HEAD16);
		assertEquals("Checking HEAD16 content item hashCode equality",CTPhantomType.getCodedSequenceItem(CTPhantomType.HEAD16).hashCode(),CTPhantomType.getCodedSequenceItem(CTPhantomType.HEAD16).hashCode());
		assertEquals("Checking HEAD16 content item equality",CTPhantomType.getCodedSequenceItem(CTPhantomType.HEAD16),CTPhantomType.getCodedSequenceItem(CTPhantomType.HEAD16));
	}
	
	public void TestCTPhantomType_Body32_Equality() throws Exception {
		
		assertEquals("Checking BODY32 equality",CTPhantomType.BODY32,CTPhantomType.BODY32);
		assertEquals("Checking BODY32 content item hashCode equality",CTPhantomType.getCodedSequenceItem(CTPhantomType.BODY32).hashCode(),CTPhantomType.getCodedSequenceItem(CTPhantomType.BODY32).hashCode());
		assertEquals("Checking BODY32 content item equality",CTPhantomType.getCodedSequenceItem(CTPhantomType.BODY32),CTPhantomType.getCodedSequenceItem(CTPhantomType.BODY32));
	}
	
	public void TestCTPhantomType_Head16_Body32_Inequality() throws Exception {
		
		assertTrue("Checking HEAD16 versus BODY32 inequality",!CTPhantomType.HEAD16.equals(CTPhantomType.BODY32));
		assertFalse("Checking HEAD16 versus BODY32 content item hashCode inequality",CTPhantomType.getCodedSequenceItem(CTPhantomType.HEAD16).hashCode() == CTPhantomType.getCodedSequenceItem(CTPhantomType.BODY32).hashCode());
		assertFalse("Checking HEAD16 versus BODY32 content item inequality",CTPhantomType.getCodedSequenceItem(CTPhantomType.HEAD16).equals(CTPhantomType.getCodedSequenceItem(CTPhantomType.BODY32)));
	}
	
	public void TestCTPhantomType_SelectFromDescription() throws Exception {
	
		assertTrue("Checking select HEAD16",CTPhantomType.selectFromDescription("HEAD16").equals(CTPhantomType.HEAD16));
		assertTrue("Checking select BODY32",CTPhantomType.selectFromDescription("BODY32").equals(CTPhantomType.BODY32));
		assertTrue("Checking select HEAD",CTPhantomType.selectFromDescription("HEAD").equals(CTPhantomType.HEAD16));
		assertTrue("Checking select BODY",CTPhantomType.selectFromDescription("BODY").equals(CTPhantomType.BODY32));
		assertTrue("Checking select BLA finds nothing",CTPhantomType.selectFromDescription("BLA") == null);
	}
	
	public void TestCTPhantomType_SelectFromCode() throws Exception {
	
		assertTrue("Checking select HEAD16",CTPhantomType.selectFromCode(new CodedSequenceItem("113690","DCM","IEC Head Dosimetry Phantom")).equals(CTPhantomType.HEAD16));
		assertTrue("Checking select BODY32",CTPhantomType.selectFromCode(new CodedSequenceItem("113691","DCM","IEC Body Dosimetry Phantom")).equals(CTPhantomType.BODY32));
		assertTrue("Checking select BLA finds nothing",CTPhantomType.selectFromCode(new CodedSequenceItem("43643432","99BLA","Strange Phantom")) == null);
	}
	
}
