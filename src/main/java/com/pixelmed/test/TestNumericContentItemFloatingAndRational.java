/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import com.pixelmed.utils.Base64;

import junit.framework.*;

import java.util.Locale;

import javax.xml.xpath.XPathFactory;

public class TestNumericContentItemFloatingAndRational extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestNumericContentItemFloatingAndRational(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestNumericContentItemFloatingAndRational.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestNumericContentItemFloatingAndRational");
				
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_StringOnlyFromAttributeListConstructor_NonSR"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_FloatingFromAttributeListConstructor_nonSR"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_StringOnlyConstructor_nonSR"));
		
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_StringOnlyFromAttributeListConstructor"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_FloatingFromAttributeListConstructor"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_RationalFromAttributeListConstructor"));
		
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_StringOnlyConstructor"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_FloatingFromDoubleConstructor"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_RationalAndFloatingFromDoubleConstructor"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_RationalAndFloatingFromDoubleConstructorDivideByZero"));

		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_DoubleConstructor_MinimalSubNormal"));
		suite.addTest(new TestNumericContentItemFloatingAndRational("TestNumericContentItemFloatingAndRational_DoubleConstructor_SubNormalAllMantissaBitsSet"));
		

		return suite;
	}
	
	ContentItemFactory cf;
		
	protected void setUp() {
		cf = new ContentItemFactory();
	}
	
	protected void tearDown() {
	}
	
	public void TestNumericContentItemFloatingAndRational_StringOnlyFromAttributeListConstructor_NonSR() throws Exception {
		String stringValue = "1";
	
		AttributeList list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("NUMERIC"); list.put(a); }
		
		{ Attribute a = new DecimalStringAttribute(TagFromName.NumericValue); a.addValue(stringValue); list.put(a); }

		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = (ContentItemFactory.NumericContentItem)(cif.getNewContentItem(null/*parent*/,list));
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_StringOnlyFromAttributeListConstructor() throws Exception {
		String stringValue = "1";
	
		AttributeList list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("NUM"); list.put(a); }
		SequenceAttribute aMeasuredValueSequence = new SequenceAttribute(TagFromName.MeasuredValueSequence);
		list.put(aMeasuredValueSequence);
		{
			AttributeList mvsiList = new AttributeList();
			aMeasuredValueSequence.addItem(mvsiList);
			
			{ Attribute a = new DecimalStringAttribute(TagFromName.NumericValue); a.addValue(stringValue); mvsiList.put(a); }
		}
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = (ContentItemFactory.NumericContentItem)(cif.getNewContentItem(null/*parent*/,list));
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument));
		assertEquals("numerator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci2.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci2.hasRationalValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_FloatingFromAttributeListConstructor() throws Exception {
		String stringValue = "1";
		double doubleValue = 1d;
	
		AttributeList list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("NUM"); list.put(a); }
		SequenceAttribute aMeasuredValueSequence = new SequenceAttribute(TagFromName.MeasuredValueSequence);
		list.put(aMeasuredValueSequence);
		{
			AttributeList mvsiList = new AttributeList();
			aMeasuredValueSequence.addItem(mvsiList);
			
			{ Attribute a = new DecimalStringAttribute(TagFromName.NumericValue); a.addValue(stringValue); mvsiList.put(a); }
			{ Attribute a = new FloatDoubleAttribute(TagFromName.FloatingPointValue); a.addValue(doubleValue); mvsiList.put(a); }
		}
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = (ContentItemFactory.NumericContentItem)(cif.getNewContentItem(null/*parent*/,list));
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("hasFloatingPointValue",nci.hasFloatingPointValue());
		assertEquals("getFloatingPointValue",doubleValue,nci.getFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double",doubleValue,Double.parseDouble(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument)));
		assertEquals("numerator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("hasFloatingPointValue",nci2.hasFloatingPointValue());
		assertEquals("getFloatingPointValue",doubleValue,nci2.getFloatingPointValue());
		assertTrue("!hasRationalValue",!nci2.hasRationalValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_FloatingFromAttributeListConstructor_nonSR() throws Exception {
		String stringValue = "1";
		double doubleValue = 1d;
	
		AttributeList list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("NUMERIC"); list.put(a); }
		
		{ Attribute a = new DecimalStringAttribute(TagFromName.NumericValue); a.addValue(stringValue); list.put(a); }
		{ Attribute a = new FloatDoubleAttribute(TagFromName.FloatingPointValue); a.addValue(doubleValue); list.put(a); }

		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = (ContentItemFactory.NumericContentItem)(cif.getNewContentItem(null/*parent*/,list));
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("hasFloatingPointValue",nci.hasFloatingPointValue());
		assertEquals("getFloatingPointValue",doubleValue,nci.getFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_RationalFromAttributeListConstructor() throws Exception {
		String stringValue = ".333333333333333";	// 16 chars
		int numerator = 1;
		long denominator = 3;
	
		AttributeList list = new AttributeList();
		{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("NUM"); list.put(a); }
		SequenceAttribute aMeasuredValueSequence = new SequenceAttribute(TagFromName.MeasuredValueSequence);
		list.put(aMeasuredValueSequence);
		{
			AttributeList mvsiList = new AttributeList();
			aMeasuredValueSequence.addItem(mvsiList);
			
			{ Attribute a = new DecimalStringAttribute(TagFromName.NumericValue); a.addValue(stringValue); mvsiList.put(a); }
			{ Attribute a = new SignedLongAttribute(TagFromName.RationalNumeratorValue); a.addValue(numerator); mvsiList.put(a); }
			{ Attribute a = new UnsignedLongAttribute(TagFromName.RationalDenominatorValue); a.addValue(denominator); mvsiList.put(a); }
		}
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = (ContentItemFactory.NumericContentItem)(cif.getNewContentItem(null/*parent*/,list));
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci.hasFloatingPointValue());
		assertTrue("hasRationalValue",nci.hasRationalValue());
		assertEquals("getRationalNumeratorValue",numerator,nci.getRationalNumeratorValue());
		assertEquals("getRationalDenominatorValue",denominator,nci.getRationalDenominatorValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument));
		assertEquals("numerator","1",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","3",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci2.hasFloatingPointValue());
		assertTrue("hasRationalValue",nci2.hasRationalValue());
		assertEquals("getRationalNumeratorValue",numerator,nci2.getRationalNumeratorValue());
		assertEquals("getRationalDenominatorValue",denominator,nci2.getRationalDenominatorValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_StringOnlyConstructor_nonSR() throws Exception {
		String stringValue = "1";
	
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = cif.makeNumericContentItem(null/*parent*/,true/*isNotSR*/,null/*relationshipType*/,null/*conceptName*/,stringValue,null/*units*/,null/*qualifier*/);
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
		
		{
			AttributeList list = nci.getAttributeList();
			assertEquals("get NumericValue from AttributeList",stringValue,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.NumericValue));
			assertTrue("no RationalNumeratorValue from AttributeList",list.get(TagFromName.RationalNumeratorValue) == null);
			assertTrue("no RationalDenominatorValue from AttributeList",list.get(TagFromName.RationalDenominatorValue) == null);
			assertTrue("no NumericValueQualifierCodeSequence from AttributeList",list.get(TagFromName.NumericValueQualifierCodeSequence) == null);
		}
	}
	
	public void TestNumericContentItemFloatingAndRational_StringOnlyConstructor() throws Exception {
		String stringValue = "1";
	
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = cif.makeNumericContentItem(null/*parent*/,null/*relationshipType*/,null/*conceptName*/,stringValue,null/*units*/,null/*qualifier*/);
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);

		{
			AttributeList list = nci.getAttributeList();
			SequenceAttribute mvs=(SequenceAttribute)(list.get(TagFromName.MeasuredValueSequence));
			assertTrue("MeasuredValueSequence present",mvs != null);
			assertTrue("MeasuredValueSequence has exactly one item",mvs.getNumberOfItems() == 1);
			AttributeList mvl = mvs.getItem(0).getAttributeList();
			
			assertEquals("get NumericValue from AttributeList",stringValue,Attribute.getSingleStringValueOrEmptyString(mvl,TagFromName.NumericValue));
			assertTrue("no FloatingPointValue from AttributeList",mvl.get(TagFromName.FloatingPointValue) == null);
			assertTrue("no RationalNumeratorValue from AttributeList",mvl.get(TagFromName.RationalNumeratorValue) == null);
			assertTrue("no RationalDenominatorValue from AttributeList",mvl.get(TagFromName.RationalDenominatorValue) == null);
			assertTrue("no NumericValueQualifierCodeSequence from AttributeList",mvl.get(TagFromName.NumericValueQualifierCodeSequence) == null);
		}
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument));
		assertEquals("numerator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci2.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci2.hasRationalValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_FloatingFromDoubleConstructor() throws Exception {
		String stringValue = ".333333333333333";	// 16 chars
		double doubleValue = 1d/3;
	
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = cif.makeNumericContentItem(null/*parent*/,null/*relationshipType*/,null/*conceptName*/,doubleValue,null/*units*/);
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("hasFloatingPointValue",nci.hasFloatingPointValue());
		assertEquals("getFloatingPointValue",doubleValue,nci.getFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);

		{
			AttributeList list = nci.getAttributeList();
			SequenceAttribute mvs=(SequenceAttribute)(list.get(TagFromName.MeasuredValueSequence));
			assertTrue("MeasuredValueSequence present",mvs != null);
			assertTrue("MeasuredValueSequence has exactly one item",mvs.getNumberOfItems() == 1);
			AttributeList mvl = mvs.getItem(0).getAttributeList();
			
			assertEquals("get NumericValue from AttributeList",stringValue,Attribute.getSingleStringValueOrEmptyString(mvl,TagFromName.NumericValue));
			assertEquals("get FloatingPointValue from AttributeList",doubleValue,mvl.get(TagFromName.FloatingPointValue).getDoubleValues()[0]);
			assertTrue("no RationalNumeratorValue from AttributeList",mvl.get(TagFromName.RationalNumeratorValue) == null);
			assertTrue("no RationalDenominatorValue from AttributeList",mvl.get(TagFromName.RationalDenominatorValue) == null);
			assertTrue("no NumericValueQualifierCodeSequence from AttributeList",mvl.get(TagFromName.NumericValueQualifierCodeSequence) == null);
		}
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double",doubleValue,Double.parseDouble(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument)));
		assertEquals("numerator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("hasFloatingPointValue",nci2.hasFloatingPointValue());
		assertEquals("getFloatingPointValue",doubleValue,nci2.getFloatingPointValue());
		assertTrue("!hasRationalValue",!nci2.hasRationalValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_RationalAndFloatingFromDoubleConstructor() throws Exception {
		String stringValue = ".333333333333333";	// 16 chars
		int numerator = 1;
		long denominator = 3;
		double doubleValue = ((double)numerator)/denominator;
	
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = cif.makeNumericContentItem(null/*parent*/,null/*relationshipType*/,null/*conceptName*/,numerator,denominator,null/*units*/);
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("hasFloatingPointValue",nci.hasFloatingPointValue());
		assertEquals("getFloatingPointValue",doubleValue,nci.getFloatingPointValue());
		assertTrue("hasRationalValue",nci.hasRationalValue());
		assertEquals("getRationalNumeratorValue",numerator,nci.getRationalNumeratorValue());
		assertEquals("getRationalDenominatorValue",denominator,nci.getRationalDenominatorValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double",doubleValue,Double.parseDouble(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument)));
		assertEquals("numerator","1",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","3",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("hasFloatingPointValue",nci2.hasFloatingPointValue());
		assertEquals("getFloatingPointValue",doubleValue,nci2.getFloatingPointValue());
		assertTrue("hasRationalValue",nci2.hasRationalValue());
		assertEquals("getRationalNumeratorValue",numerator,nci2.getRationalNumeratorValue());
		assertEquals("getRationalDenominatorValue",denominator,nci2.getRationalDenominatorValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}
	
	public void TestNumericContentItemFloatingAndRational_RationalAndFloatingFromDoubleConstructorDivideByZero() throws Exception {
		String stringValue = "";
		int numerator = 1;
		long denominator = 0;
	
		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = cif.makeNumericContentItem(null/*parent*/,null/*relationshipType*/,null/*conceptName*/,numerator,denominator,null/*units*/);
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertEquals("getQualifier",new CodedSequenceItem("114003","DCM","Divide by zero"),nci.getQualifier());
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument));
		assertEquals("numerator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","Divide by zero",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci2.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci2.hasRationalValue());
		assertEquals("getQualifier",new CodedSequenceItem("114003","DCM","Divide by zero"),nci2.getQualifier());
	}

	public void TestNumericContentItemFloatingAndRational_DoubleConstructor_MinimalSubNormal() throws Exception {
		String stringValue = ".49E-323";
		double doubleValue = Double.longBitsToDouble(0x0000000000000001l);

		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = cif.makeNumericContentItem(null/*parent*/,null/*relationshipType*/,null/*conceptName*/,doubleValue,null/*units*/);
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument));
		assertEquals("numerator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("!hasFloatingPointValue",!nci2.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci2.hasRationalValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}

	public void TestNumericContentItemFloatingAndRational_DoubleConstructor_SubNormalAllMantissaBitsSet() throws Exception {
		String stringValue = ".2225073859E-307";	// rounded from actual ".2225073858507201E-307" to fit in 16 chars
		double doubleValue = Double.longBitsToDouble(0x000fffffffffffffl);

		ContentItemFactory cif = new ContentItemFactory();
		ContentItemFactory.NumericContentItem nci = cif.makeNumericContentItem(null/*parent*/,null/*relationshipType*/,null/*conceptName*/,doubleValue,null/*units*/);
		
		assertEquals("getNumericValue",stringValue,nci.getNumericValue());
		assertTrue("hasFloatingPointValue",nci.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci.hasRationalValue());
		assertTrue("getQualifier",nci.getQualifier() == null);
		
		StructuredReport sr = new StructuredReport(nci);
		XMLRepresentationOfStructuredReportObjectFactory xmf = new XMLRepresentationOfStructuredReportObjectFactory();
		org.w3c.dom.Document srDocument = xmf.getDocument(sr,null/*list*/);
		//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();

		assertEquals("numeric value",stringValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/value",srDocument));
		assertEquals("double",doubleValue,Double.parseDouble(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/double",srDocument)));
		assertEquals("numerator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/numerator",srDocument));
		assertEquals("denominator","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/denominator",srDocument));
		assertEquals("qualifier","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/num/qualifier/@cm",srDocument));
		
		StructuredReport sr2 = xmf.getStructuredReport(srDocument);
		ContentItemFactory.NumericContentItem nci2 = (ContentItemFactory.NumericContentItem)(sr2.getRoot());
		
		assertEquals("getNumericValue",stringValue,nci2.getNumericValue());
		assertTrue("hasFloatingPointValue",nci2.hasFloatingPointValue());
		assertTrue("!hasRationalValue",!nci2.hasRationalValue());
		assertTrue("getQualifier",nci2.getQualifier() == null);
	}
	
}
