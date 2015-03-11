/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import org.w3c.dom.Document;

import javax.xml.xpath.XPathFactory;

import junit.framework.*;

import java.util.Locale;

public class TestSpatialCoordinatesContentItemLocaleEffect extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSpatialCoordinatesContentItemLocaleEffect(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSpatialCoordinatesContentItemLocaleEffect.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSpatialCoordinatesContentItemLocaleEffect");
		
		suite.addTest(new TestSpatialCoordinatesContentItemLocaleEffect("TestSpatialCoordinatesContentItemLocaleEffect_AllAvailableLocales"));
		
		return suite;
	}
	
	ContentItemFactory cf;
		
	protected void setUp() {
		cf = new ContentItemFactory();
	}
	
	protected void tearDown() {
	}
	
	public void TestSpatialCoordinatesContentItemLocaleEffect_AllAvailableLocales() throws Exception {
		String cvConcept = "111010";
		String csdConcept = "DCM";
		String cmConcept = "Center";
		
		float x = -39.76f;
		float y = 7.384f;
		float[] graphicData = { x, y };
		
		String expectString = "CONTAINS: SCOORD: Center = POINT (-39.759998321533,7.38399982452393)";
				
		Locale[] localesToTest = Locale.getAvailableLocales();
		for (Locale l : localesToTest) {
//System.err.println("Testing effect on SpatialCoordinatesContentItem toString() method with default locale "+l);
			Locale.setDefault(l);
			
			ContentItem root = cf.new ContainerContentItem(
				null/*no parent since root*/,null/*no relationshipType since root*/,
				null/*no root concept*/,
				true/*continuityOfContentIsSeparate*/,
				"","");

			ContentItemFactory.SpatialCoordinatesContentItem ci = cf.makeSpatialCoordinatesContentItem(root,"CONTAINS",
				new CodedSequenceItem(cvConcept,csdConcept,cmConcept),
				"POINT",
				graphicData);
	
//System.err.println("ci = "+ci.toString());
			assertEquals(l.toString(),expectString,ci.toString());
			
			String justcoords = ci.toString().replace("CONTAINS: SCOORD: Center = POINT (","").replace(")","");
			String[] xy = justcoords.split(",");
			assertEquals(l.toString()+": x in content item",x,Float.valueOf(xy[0]).floatValue());
			assertEquals(l.toString()+": y in content item",y,Float.valueOf(xy[1]).floatValue());

			StructuredReport sr = new StructuredReport(root);
//System.err.println(sr);
			
			// NB. XPath matching against the XML document fails if the default Locale is Turkish ("tr" or "tr_TR")
			// if any toLowerCase(java.util.Locale.US) method calls used to create the document use the default Locale instead of an explicit non-Turkish Locale like Locale.US
			Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr);
//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);

			XPathFactory xpf = XPathFactory.newInstance();
			
//System.err.println("/DicomStructuredReport/DicomStructuredReportContent: "+xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent",srDocument));
//System.err.println("/DicomStructuredReport/DicomStructuredReportContent/container: "+xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container",srDocument));
//System.err.println("/DicomStructuredReport/DicomStructuredReportContent/container/scoord/point: "+xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container/scoord/point",srDocument));

			assertEquals(l.toString()+": x in XML",x,Float.valueOf(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container/scoord/point/x",srDocument)).floatValue());
			assertEquals(l.toString()+": y in XML",y,Float.valueOf(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container/scoord/point/y",srDocument)).floatValue());
			
			{
				// test XML round trip
				AttributeList roundTripList = new XMLRepresentationOfStructuredReportObjectFactory().getAttributeList(srDocument);
				StructuredReport roundTripSR = new StructuredReport(roundTripList);
				ContentItem roundTripRoot = (ContentItem)(roundTripSR.getRoot());
				ContentItemFactory.SpatialCoordinatesContentItem roundTripCoords = (ContentItemFactory.SpatialCoordinatesContentItem)(roundTripRoot.getChildAt(0));
//System.err.println("roundTripCoords = "+roundTripCoords);
				assertEquals("Round trip XML SCOORD content item",ci.toString(),roundTripCoords.toString());
			}
		}
	}
	
}
