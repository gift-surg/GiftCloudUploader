/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import org.w3c.dom.Document;

import javax.xml.xpath.XPathFactory;

import junit.framework.*;

import java.util.Locale;

public class TestSpatialCoordinates3DContentItemLocaleEffect extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSpatialCoordinates3DContentItemLocaleEffect(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSpatialCoordinates3DContentItemLocaleEffect.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSpatialCoordinates3DContentItemLocaleEffect");
		
		suite.addTest(new TestSpatialCoordinates3DContentItemLocaleEffect("TestSpatialCoordinates3DContentItemLocaleEffect_AllAvailableLocales"));
		
		return suite;
	}
	
	ContentItemFactory cf;
		
	protected void setUp() {
		cf = new ContentItemFactory();
	}
	
	protected void tearDown() {
	}
	
	public void TestSpatialCoordinates3DContentItemLocaleEffect_AllAvailableLocales() throws Exception {
		String cvConcept = "111010";
		String csdConcept = "DCM";
		String cmConcept = "Center";
		
		String referencedFrameOfReferenceUID = "1.2.3.4";
		
		float x = -39.76f;
		float y = 7.384f;
		float z = 42.13f;
		float[] graphicData = { x, y, z };
		
		String expectString = "CONTAINS: SCOORD3D: Center = POINT (-39.759998321533,7.38399982452393,42.1300010681152) (FoR 1.2.3.4)";
				
		Locale[] localesToTest = Locale.getAvailableLocales();
		for (Locale l : localesToTest) {
//System.err.println("Testing effect on SpatialCoordinatesContentItem toString() method with default locale "+l);
			Locale.setDefault(l);
			
			ContentItem root = cf.new ContainerContentItem(
				null/*no parent since root*/,null/*no relationshipType since root*/,
				null/*no root concept*/,
				true/*continuityOfContentIsSeparate*/,
				"","");

			ContentItemFactory.SpatialCoordinates3DContentItem ci = cf.makeSpatialCoordinates3DContentItem(root,"CONTAINS",
				new CodedSequenceItem(cvConcept,csdConcept,cmConcept),
				"POINT",
				graphicData,
				referencedFrameOfReferenceUID);
	
//System.err.println("ci = "+ci.toString());
			assertEquals(l.toString(),expectString,ci.toString());
			
			String justcoords = ci.toString().replace("CONTAINS: SCOORD3D: Center = POINT (","").replace(" (FoR 1.2.3.4)","").replace(")","");
			String[] xyz = justcoords.split(",");
			assertEquals(l.toString()+": x in content item",x,Float.valueOf(xyz[0]).floatValue());
			assertEquals(l.toString()+": y in content item",y,Float.valueOf(xyz[1]).floatValue());
			assertEquals(l.toString()+": z in content item",z,Float.valueOf(xyz[2]).floatValue());

			StructuredReport sr = new StructuredReport(root);
//System.err.println(sr);
			
			// NB. XPath matching against the XML document fails if the default Locale is Turkish ("tr" or "tr_TR")
			// if any toLowerCase(java.util.Locale.US) method calls used to create the document use the default Locale instead of an explicit non-Turkish Locale like Locale.US
			Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr);
//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);

			XPathFactory xpf = XPathFactory.newInstance();
			
//System.err.println("/DicomStructuredReport/DicomStructuredReportContent: "+xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent",srDocument));
//System.err.println("/DicomStructuredReport/DicomStructuredReportContent/container: "+xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container",srDocument));
//System.err.println("/DicomStructuredReport/DicomStructuredReportContent/container/scoord/point: "+xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container/scoord3d/point",srDocument));

			assertEquals(l.toString()+": x in XML",x,Float.valueOf(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container/scoord3d/point/x",srDocument)).floatValue());
			assertEquals(l.toString()+": y in XML",y,Float.valueOf(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container/scoord3d/point/y",srDocument)).floatValue());
			assertEquals(l.toString()+": z in XML",z,Float.valueOf(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container/scoord3d/point/z",srDocument)).floatValue());
		
			{
				// test XML round trip
				AttributeList roundTripList = new XMLRepresentationOfStructuredReportObjectFactory().getAttributeList(srDocument);
				StructuredReport roundTripSR = new StructuredReport(roundTripList);
				ContentItem roundTripRoot = (ContentItem)(roundTripSR.getRoot());
				ContentItemFactory.SpatialCoordinates3DContentItem roundTripCoords = (ContentItemFactory.SpatialCoordinates3DContentItem)(roundTripRoot.getChildAt(0));
//System.err.println("roundTripCoords = "+roundTripCoords);
				assertEquals("Round trip XML SCOORD3D content item",ci.toString(),roundTripCoords.toString());
			}
		}
	}
	
}
