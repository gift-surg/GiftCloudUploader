/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.UUIDBasedOID;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import junit.framework.*;

public class TestUUIDBasedOID extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestUUIDBasedOID(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestUUIDBasedOID.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestUUIDBasedOID");
		
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_parseUUIDFromOID"));
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_OIDConstructor"));
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_createOIDFromUUIDCanonicalHexString"));
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_roundTripFromUUID_Test1"));
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_roundTripFromUUID_Test2"));
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_getByteArrayInNetworkByteOrderFromUUID"));
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_Deterministic"));
		suite.addTest(new TestUUIDBasedOID("TestUUIDBasedOID_Uniqueness"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestUUIDBasedOID_parseUUIDFromOID() throws Exception {
		String testOIDString = "2.25.329800735698586629295641978511506172918";
		String testCanonicalHexString = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";	// "http://www.digipedia.pl/man/uuid.3ossp.html"
		
		UUID uuidFromOID = UUIDBasedOID.parseUUIDFromOID(testOIDString);
		//System.err.println(uuidFromOID);	// 
		UUID uuidFromCanonicalHexString = UUID.fromString(testCanonicalHexString);
		assertEquals("Checking parsing UUID from OID via static method",uuidFromCanonicalHexString,uuidFromOID);
	}
	
	public void TestUUIDBasedOID_OIDConstructor() throws Exception {
		String testOIDString = "2.25.329800735698586629295641978511506172918";
		String testCanonicalHexString = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";	// "http://www.digipedia.pl/man/uuid.3ossp.html"
		
		UUIDBasedOID oid = new UUIDBasedOID(testOIDString);
		UUID uuidFromOID = oid.getUUID();
		//System.err.println(uuidFromOID);
		UUID uuidFromCanonicalHexString = UUID.fromString(testCanonicalHexString);
		assertEquals("Checking parsing UUID from OID via constructor",uuidFromCanonicalHexString,uuidFromOID);

		String oidString = oid.getOID();
		assertEquals("Checking round trip from string constructor of OID",testOIDString,oidString);
	}

	public void TestUUIDBasedOID_createOIDFromUUIDCanonicalHexString() throws Exception {
		String testOIDString = "2.25.329800735698586629295641978511506172918";
		String testCanonicalHexString = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";	// "http://www.digipedia.pl/man/uuid.3ossp.html"
		
		String oidFromCanonicalHexString = UUIDBasedOID.createOIDFromUUIDCanonicalHexString(testCanonicalHexString);
		//System.err.println(oidFromCanonicalHexString);
		assertEquals("Checking creating OID given UUID via static method",testOIDString,oidFromCanonicalHexString);
	}


	public void TestUUIDBasedOID_roundTripFromUUID_Test1() throws Exception {
		String testCanonicalHexString = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";	// "http://www.digipedia.pl/man/uuid.3ossp.html"
					
		String oidFromCanonicalHexString = UUIDBasedOID.createOIDFromUUIDCanonicalHexString(testCanonicalHexString);
		//System.err.println(oidFromCanonicalHexString);
		String uuidString = UUIDBasedOID.parseUUIDFromOID(oidFromCanonicalHexString).toString();
		//System.err.println(uuidString);
			
		assertEquals("Checking round trip given UUID",testCanonicalHexString,uuidString);
	}

	public void TestUUIDBasedOID_roundTripFromUUID_Test2() throws Exception {
		String testCanonicalHexString = "0b7827e3-35f0-46c0-a2e9-eaf4dadc899b";
		
		String oidFromCanonicalHexString = UUIDBasedOID.createOIDFromUUIDCanonicalHexString(testCanonicalHexString);
		//System.err.println(oidFromCanonicalHexString);
		String uuidString = UUIDBasedOID.parseUUIDFromOID(oidFromCanonicalHexString).toString();
		//System.err.println(uuidString);
			
		assertEquals("Checking round trip given UUID",testCanonicalHexString,uuidString);
	}

	public void TestUUIDBasedOID_getByteArrayInNetworkByteOrderFromUUID() throws Exception {
		
		UUID uuid = new UUID(0xffeeddccbbaa9988l,0x7766554433221100l);
		byte[] expectBytes = { (byte)0xff, (byte)0xee, (byte)0xdd, (byte)0xcc, (byte)0xbb, (byte)0xaa, (byte)0x99, (byte)0x88, (byte)0x77, (byte)0x66, (byte)0x55, (byte)0x44, (byte)0x33, (byte)0x22, (byte)0x11, (byte)0x00 };

		byte[] b = UUIDBasedOID.getByteArrayInNetworkByteOrderFromUUID(uuid);
		assertTrue("Checking getByteArrayInNetworkByteOrderFromUUID() ",java.util.Arrays.equals(expectBytes,b));
	}

	public void TestUUIDBasedOID_Deterministic() throws Exception {
		String testHashInputString = "Hello World";
		byte[] testHashInputBytes = testHashInputString.getBytes("UTF8");

		UUID namespace = UUID.randomUUID();
		UUIDBasedOID oid1 = new UUIDBasedOID(namespace,testHashInputBytes);
		UUIDBasedOID oid2 = new UUIDBasedOID(namespace,testHashInputBytes);
		//System.err.println("TestUUIDBasedOID_Deterministic OID is "+oid1.getOID());
		
		assertEquals("Checking Type 3 UUID hash based OID is deterministic ",oid1.getOID(),oid2.getOID());
	}

	public void TestUUIDBasedOID_Uniqueness() throws Exception {
			int count = 1000;
			// Check are all unique
			boolean uniquenessCheck = true;
			boolean lengthCheck = true;
			Set<String> set = new HashSet<String>();
			for (int i=0; i<count; ++i) {
				UUIDBasedOID oid = new UUIDBasedOID();
				//uuidFromOID = oid.getUUID();
				//System.err.println(uuidFromOID);
				String oidString = oid.getOID();
				//System.err.println(oidString);
				int length = oidString.length();
				//System.err.println(oidString+" (length = "+length+")");
				if (set.contains(oidString)) {
					System.err.println("Error - not unique - \""+oidString+"\"");
					uniquenessCheck = false;
				}
				if (length > 64) {
					System.err.println("Error - too long - \""+oidString+"\" (length = "+length+")");
					lengthCheck = false;
				}
			}
			assertTrue("Were all unique",uniquenessCheck);
			assertTrue("Were all within length limit",lengthCheck);
	}
	
}	
