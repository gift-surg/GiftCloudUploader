/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dose.*;

import junit.framework.*;

import com.pixelmed.dicom.*;

import java.util.Locale;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestCTDose extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCTDose(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTDose.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCTDose");
		
		suite.addTest(new TestCTDose("testCTDoseConstructor_WithAllParameters"));
		suite.addTest(new TestCTDose("testCTDoseConstructor_WithAllParametersAndThreeAcquisitionsInTwoSeries"));
		suite.addTest(new TestCTDose("testCTDoseConstructor_WithOneAcquisitionsAndNoTotalDLP"));
		suite.addTest(new TestCTDose("testCTDoseConstructor_PropagationOfDescriptionToAttributeList_IfNoStudyDescriptionInCompositeInstanceContext"));
		suite.addTest(new TestCTDose("testCTDoseConstructor_PropagationOfDescriptionToAttributeList_DoesNotOverwriteIfStudyDescriptionAlreadyInCompositeInstanceContext"));
		suite.addTest(new TestCTDose("testCTDoseConstructor_WithSeparateHeadAndBodyTotalDLPConstructor"));
		suite.addTest(new TestCTDose("testCTDoseConstructor_WithSeparateHeadAndBodyTotalDLPSet"));
		suite.addTest(new TestCTDose("testCTDoseConstructor_WithSeparateHeadAndBodyAcquisitions"));
		
		return suite;
	}
	
	private CommonDoseObserverContext observerContext;
	private CompositeInstanceContext compositeInstanceContext;
	
	private String deviceUID = "1.2.3.4";
	private String deviceName = "station1";
	private String manufacturer = "Acme";
	private String modelName = "Scanner";
	private String serialNumber = "72349236741";
	private String location = "Suite1";
	
	private String operatorName = "Smith^John";
	private String operatorID = "26354781234";
	private String physicianName = "Jones^Mary";
	private String physicianID = "23491234234";
	private String idIssuer = "99BLA";
	private String organization = "St. Elsewhere's";

	private String patientName = "Smith^Mary";
	private String patientID = "3764913624";
	private String patientBirthDate = "19600101";
	private String patientAge = "041Y";
	private String patientWeight = "68";
	private String patientSize = "1.55";
	private String patientSex = "F";
	private String studyID = "612386812";
	private String seriesNumber = "12";
	private String instanceNumber = "38";
	private String referringPhysicianName = "Jones^Harriet";
	private String studyDate = "20010203";
	private String studyTime = "043000";
	
	private UIDGenerator u = new UIDGenerator("9999");
	private String sopInstanceUID;
	private String seriesInstanceUID;
	private String studyInstanceUID;
	
	protected void setUp() {
		Locale.setDefault(Locale.FRENCH);	// forces check that "," is not being used as decimal point in any double to string conversions

		observerContext = new CommonDoseObserverContext(deviceUID,deviceName,manufacturer,modelName,serialNumber,location,operatorName,operatorID,physicianName,physicianID,idIssuer,organization);

		try {
			UIDGenerator u = new UIDGenerator("9999");
			sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
			seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
			studyInstanceUID = u.getNewStudyInstanceUID(studyID);
			AttributeList list = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
			{ Attribute a = new AgeStringAttribute(TagFromName.PatientAge); a.addValue(patientAge); list.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(patientWeight); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(patientSize); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); /*a.addValue(manufacturer);*/ list.put(a); }
			//{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(modelName); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
//System.err.println("TestCTDose.setUp(): compositeInstanceContext.getAttributeList() =\n"+list);
			compositeInstanceContext = new CompositeInstanceContext(list,true/*forSR*/);
		}
		catch (DicomException e) {
		}
	}
	
	protected void tearDown() {
	}
	
	public void testCTDoseConstructor_WithAllParameters() throws Exception {

		String dlpTotal = "1299.58";
		int totalNumberOfIrradiationEvents = 4;
		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		String startDateTime = "20010203043000+0000";
		String endDateTime   = "20010203043500+0000";
		String description   = "CAP";
		
		String expectToStringDetail = "Dose\tPatient ID=3764913624\tName=Smith^Mary\tSex=F\tDOB=19600101\tAge=041Y\tWeight=68 kg\tHeight=1.55 m\tAccession=\tStart="+startDateTime+"\tEnd="+endDateTime+"\tModality=CT\tDescription="+description+"\tScope=Study\tUID="+scopeUID+"\tEvents="+Integer.toString(totalNumberOfIrradiationEvents)+"\tDLP Total="+dlpTotal+" mGy.cm\n";
		String expectToStringNoDetail = "Dose\tPatient ID=3764913624\tName=Smith^Mary\tSex=F\tDOB=19600101\tAge=041Y\tWeight=68 kg\tHeight=1.55 m\tAccession=\tStart="+startDateTime+"\tModality=CT\tDescription="+description+"\tDLP Total="+dlpTotal+" mGy.cm\n";
		
		CTDose ctDose = new CTDose(dlpTotal,totalNumberOfIrradiationEvents,ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		
		//ctDose.setObserverContext(observerContext);
		ctDose.setCompositeInstanceContext(compositeInstanceContext);

		assertEquals("Checking DLP Total",dlpTotal,ctDose.getDLPTotal());
		assertEquals("Checking totalNumberOfIrradiationEvents",totalNumberOfIrradiationEvents,ctDose.getTotalNumberOfIrradiationEvents());
		assertEquals("Checking ScopeOfDoseAccummulation equality",ScopeOfDoseAccummulation.STUDY,ctDose.getScopeOfDoseAccummulation());
		assertEquals("Checking ScopeOfDoseAccummulation string equality","Study",ctDose.getScopeOfDoseAccummulation().toString());
		assertEquals("Checking scopeUID string equality",scopeUID,ctDose.getScopeUID());
		assertEquals("Checking startDateTime string equality",startDateTime,ctDose.getStartDateTime());
		assertEquals("Checking endDateTime string equality",endDateTime,ctDose.getEndDateTime());
		assertEquals("Checking description string equality",description,ctDose.getDescription());
		
		assertEquals("Checking toString default",expectToStringDetail,ctDose.toString());
		assertEquals("Checking toString detail",expectToStringDetail,ctDose.toString(true,false));
		assertEquals("Checking toString no detail",expectToStringNoDetail,ctDose.toString(false,false));

		assertFalse("Checking SR is not null",ctDose.getStructuredReport() == null);
	}
	
	public void testCTDoseConstructor_WithAllParametersAndThreeAcquisitionsInTwoSeries() throws Exception {

		String[] ctdiVolArray        = { "20.23",   "20.23",  "20.23"  };
		String[] dlpArray            = { "1362.24", "107.73", "172.99" };
		String[] scanningLengthArray = { "673.38",  "53.25",  "85.51"  };	// i.e., DLP/CTDIvol*10
		String commonPhantomTypeString = "BODY32";
		ScanRange[] scanRangeArray = { new ScanRange("S", "14.250","I","635.750"), new ScanRange("I","635.250","I","665.250"), new ScanRange("S", "14.250","S", "84.250") };
		
		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		CTDoseAcquisition acq0 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"2",CTScanType.HELICAL,scanRangeArray[0],ctdiVolArray[0],dlpArray[0],CTPhantomType.selectFromDescription(commonPhantomTypeString));
		CTDoseAcquisition acq1 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"2",CTScanType.HELICAL,scanRangeArray[1],ctdiVolArray[1],dlpArray[1],CTPhantomType.selectFromDescription(commonPhantomTypeString));
		CTDoseAcquisition acq2 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"3",CTScanType.HELICAL,scanRangeArray[2],ctdiVolArray[2],dlpArray[2],CTPhantomType.selectFromDescription(commonPhantomTypeString));

		String dlpTotal = "1642.96";
		int totalNumberOfIrradiationEvents = 3;
		String startDateTime = "20010203043000+0000";
		String startDateTimeFormatted = "2001/02/03 04:30:00";
		String endDateTime   = "20010203043500+0000";
		String endDateTimeFormatted   = "2001/02/03 04:35:00";
		String description   = "CAP";
	
		String expectToStringDetail =
			  "Dose\tPatient ID=\tName=\tSex=\tDOB=\tAge=\tWeight= kg\tHeight= m\tAccession=\tStart="+startDateTime+"\tEnd="+endDateTime+"\tModality=CT\tDescription="+description+"\tScope=Study\tUID="+scopeUID+"\tEvents="+Integer.toString(totalNumberOfIrradiationEvents)+"\tDLP Total="+dlpTotal+" (BODY32) mGy.cm\n"
			+ "\tSeries=2\tHelical\tRange=S14.250-I635.750 mm\tCTDIvol="+ctdiVolArray[0]+" mGy\tDLP="+dlpArray[0]+" mGy.cm\tPhantom=BODY32\n"
			+ "\tSeries=2\tHelical\tRange=I635.250-I665.250 mm\tCTDIvol="+ctdiVolArray[1]+" mGy\tDLP="+dlpArray[1]+" mGy.cm\tPhantom=BODY32\n"
			+ "\tSeries=3\tHelical\tRange=S14.250-S84.250 mm\tCTDIvol="+ctdiVolArray[2]+" mGy\tDLP="+dlpArray[2]+" mGy.cm\tPhantom=BODY32\n"
			;
		
		String expectToStringNoDetail =
			  "Dose\tPatient ID=\tName=\tSex=\tDOB=\tAge=\tWeight= kg\tHeight= m\tAccession=\tStart="+startDateTime+"\tModality=CT\tDescription="+description+"\tDLP Total="+dlpTotal+" (BODY32) mGy.cm\n";
		
		String expectToStringNoDetailPretty =
			  "Dose\tPatient ID=\tName=\tSex=\tDOB=\tAge=\tWeight= kg\tHeight= m\tAccession=\t"+startDateTimeFormatted+"\tCT\t"+description+"\tDLP Total="+dlpTotal+" (BODY32) mGy.cm\n";
		
	
		String expectGetHTMLNoDetail =
			  "<tr><td></td><td></td><td></td><td></td><td align=right></td><td align=right></td><td align=right></td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>CAP</td><td align=right>1642.96 (BODY32)</td><td align=right></td><td align=right></td><td></td><td></td><td>MOD</td></tr>\n"
			;
	
		String expectGetHTMLDetail =
			  "<tr><th>ID</th><th>Name</th><th>Sex</th><th>DOB</th><th>Age</th><th>Weight kg</th><th>Height m</th><th>Accession</th><th>Date</th><th>Modality</th><th>Description</th><th>DLP Total mGy.cm</th><th>DLP HEAD16 mGy.cm</th><th>DLP BODY32 mGy.cm</th><th>Manufacturer</th><th>Model</th><th>From</th></tr>\n"
			+ "<tr><td></td><td></td><td></td><td></td><td align=right></td><td align=right></td><td align=right></td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>CAP</td><td align=right>1642.96 (BODY32)</td><td align=right></td><td align=right></td><td></td><td></td><td>MOD</td></tr>\n"
			+ "<tr><td colspan=2></td><td colspan=15><table><tr><th>Number</th><th>Type</th><th>Range mm</th><th>CTDIvol mGy</th><th>DLP mGy.cm</th><th>Phantom</th></tr>\n"
			+ "<tr><td>Series=2</td><td>Helical</td><td>S14.250-I635.750</td><td>"+ctdiVolArray[0]+"</td><td>"+dlpArray[0]+"</td><td>BODY32</td></tr>\n"
			+ "<tr><td>Series=2</td><td>Helical</td><td>I635.250-I665.250</td><td>"+ctdiVolArray[1]+"</td><td>"+dlpArray[1]+"</td><td>BODY32</td></tr>\n"
			+ "<tr><td>Series=3</td><td>Helical</td><td>S14.250-S84.250</td><td>"+ctdiVolArray[2]+"</td><td>"+dlpArray[2]+"</td><td>BODY32</td></tr>\n"
			+ "</table></td></tr>\n"
			;
		
		CTDose ctDose = new CTDose(ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		ctDose.addAcquisition(acq0);
		ctDose.addAcquisition(acq1);
		ctDose.addAcquisition(acq2);
		ctDose.setDLPTotal(dlpTotal);
		
		assertEquals("Checking DLP Total",dlpTotal,ctDose.getDLPTotal());
		assertTrue("Checking DLP Total as double",Double.parseDouble(dlpTotal) == Double.parseDouble(ctDose.getDLPTotal()));
		assertEquals("Checking totalNumberOfIrradiationEvents",totalNumberOfIrradiationEvents,ctDose.getTotalNumberOfIrradiationEvents());
		assertTrue("Checking ScopeOfDoseAccummulation equality",ScopeOfDoseAccummulation.STUDY.equals(ctDose.getScopeOfDoseAccummulation()));
		assertEquals("Checking ScopeOfDoseAccummulation string equality","Study",ctDose.getScopeOfDoseAccummulation().toString());
		assertEquals("Checking scopeUID string equality",scopeUID,ctDose.getScopeUID());
		assertEquals("Checking startDateTime string equality",startDateTime,ctDose.getStartDateTime());
		assertEquals("Checking endDateTime string equality",endDateTime,ctDose.getEndDateTime());
		assertEquals("Checking description string equality",description,ctDose.getDescription());

		assertEquals("Checking number of acquisitions",totalNumberOfIrradiationEvents,ctDose.getNumberOfAcquisitions());
		assertTrue("Checking CTDoseAcquisition 1 equality",acq0.equals(ctDose.getAcquisition(0)));
		assertTrue("Checking CTDoseAcquisition 2 equality",acq1.equals(ctDose.getAcquisition(1)));
		assertTrue("Checking CTDoseAcquisition 3 equality",acq2.equals(ctDose.getAcquisition(2)));

		assertTrue("Checking DLP Total as double with total from acquisitions",ctDose.specifiedDLPTotalMatchesDLPTotalFromAcquisitions());

		assertEquals("Checking toString default",expectToStringDetail,ctDose.toString());
		assertEquals("Checking toString detail",expectToStringDetail,ctDose.toString(true,false));
		assertEquals("Checking toString no detail",expectToStringNoDetail,ctDose.toString(false,false));
		assertEquals("Checking toString no detail pretty",expectToStringNoDetailPretty,ctDose.toString(false,true));
		assertEquals("Checking getHTML no detail",expectGetHTMLNoDetail,ctDose.getHTMLTableRow(false));
		assertEquals("Checking getHTML detail",expectGetHTMLDetail,ctDose.getHTMLTableRow(true));
		
		StructuredReport sr = ctDose.getStructuredReport();
		assertFalse("Checking SR is not null",sr == null);
		AttributeList list = ctDose.getAttributeList();
		assertFalse("Checking SR AttributeList is not null",list == null);
		org.w3c.dom.Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
		assertFalse("Checking SR document is not null",srDocument == null);
//System.err.println("srDocument =\n");
//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);

		XPathFactory xpf = XPathFactory.newInstance();
		
		assertEquals("Checking document title","X-Ray Radiation Dose Report",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/concept/@cm",srDocument));
		assertEquals("Checking procedure reported","Computed Tomography X-Ray",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='121058']/value/@cm",srDocument));
		assertEquals("Checking diagnostic intent","Diagnostic Intent",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='121058']/code[concept/@cv='G-C0E8']/value/@cm",srDocument));

		assertEquals("Checking Start of X-Ray Irradiation",startDateTime,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/datetime[concept/@cv='113809']/value",srDocument));
		assertEquals("Checking End of X-Ray Irradiation",endDateTime,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/datetime[concept/@cv='113810']/value",srDocument));

		assertEquals("Checking scope of accumulation","Study",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='113705']/value/@cm",srDocument));
		assertEquals("Checking scope of accumulation UIDREF",scopeUID,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='113705']/uidref/value",srDocument));

		// CONTAINER: (113811,DCM,"CT Accumulated Dose Data")
		assertEquals("Checking Total Number of Irradiation Events",Integer.toString(totalNumberOfIrradiationEvents),xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113812']/value",srDocument));
		assertEquals("Checking Total Number of Irradiation Events units","events",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113812']/units/@cm",srDocument));
		assertEquals("Checking CT Dose Length Product Total",dlpTotal,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/value",srDocument));
		assertEquals("Checking CT Dose Length Product Total units","mGy.cm",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/units/@cm",srDocument));

		// CONTAINER: (113819,DCM,"CT Acquisition")

		NodeList acquisitions = (NodeList)(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']",srDocument,XPathConstants.NODESET));
		assertTrue("CT Acquisitions not null",acquisitions != null);
		assertTrue("CT Acquisitions count",acquisitions.getLength() == totalNumberOfIrradiationEvents);
		
		for (int i=0; i<acquisitions.getLength(); ++i) {
			Node acquisitionNode = acquisitions.item(i);
//System.err.println("acquisitionNode["+i+"] = "+XMLRepresentationOfStructuredReportObjectFactory.toString(acquisitionNode,0/*indent*/));
//System.err.println("acquisitionNode["+i+"] evaluate = "+xpf.newXPath().evaluate("./code[concept/@cv='113820']/value/@cm",acquisitionNode));
			assertEquals("Checking Target Region","Entire body",xpf.newXPath().evaluate("./code[concept/@cv='123014']/value/@cm",acquisitionNode));
			assertEquals("Checking CT Acquisition Type","Spiral Acquisition",xpf.newXPath().evaluate("./code[concept/@cv='113820']/value/@cm",acquisitionNode));

			// CT Acquisition Parameters ... even though no CTAcquisitionParameters were set (to emulate screen without slice images), scanning length should be provided from values in CTDoseAcquisition
			assertEquals("Checking Scanning Length",scanningLengthArray[i],xpf.newXPath().evaluate("./container[concept/@cv='113822']/num[concept/@cv='113825']/value",acquisitionNode));
			assertEquals("Checking Scanning Length units","mm",            xpf.newXPath().evaluate("./container[concept/@cv='113822']/num[concept/@cv='113825']/units/@cm",acquisitionNode));

			// CT Dose
			assertEquals("Checking Mean CTDIvol",ctdiVolArray[i],xpf.newXPath().evaluate("./container[concept/@cv='113829']/num[concept/@cv='113830']/value",acquisitionNode));
			assertEquals("Checking Mean CTDIvol units","mGy",xpf.newXPath().evaluate("./container[concept/@cv='113829']/num[concept/@cv='113830']/units/@cm",acquisitionNode));
			assertEquals("Checking CTDIw Phantom Type","IEC Body Dosimetry Phantom",xpf.newXPath().evaluate("./container[concept/@cv='113829']/code[concept/@cv='113835']/value/@cm",acquisitionNode));
			assertEquals("Checking DLP",dlpArray[i],xpf.newXPath().evaluate("./container[concept/@cv='113829']/num[concept/@cv='113838']/value",acquisitionNode));
			assertEquals("Checking DLP units","mGy.cm",xpf.newXPath().evaluate("./container[concept/@cv='113829']/num[concept/@cv='113838']/units/@cm",acquisitionNode));
		}
	}
	
	public void testCTDoseConstructor_WithOneAcquisitionsAndNoTotalDLP() throws Exception {

		SourceOfDoseInformation source = SourceOfDoseInformation.DERIVED_FROM_HUMAN_READABLE_REPORTS;
		
		String dlp =  "1362.24";
		String ctdiVol = "20.23";
		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		CTDoseAcquisition acq0 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"2",CTScanType.HELICAL,new ScanRange("S", "14.250","I","635.750"),ctdiVol,dlp,CTPhantomType.selectFromDescription("BODY32"));
		
		String irradiationEventUID = "1.2.3.4";
		CTScanType ctScanType = CTScanType.HELICAL;
		CodedSequenceItem anatomy = new CodedSequenceItem("R-FAB56","SRT","Chest, Abdomen and Pelvis");
		String acquisitionProtocol = "5.90 CHEST+ABDOMEN+PELVIS";
		String comment = "Non-contrast thin";
		String exposureTimeInSeconds = "1";
		String kvp = "120";
		String tubeCurrent = "397";
		String tubeCurrentMaximum = "433";
		String exposureTimePerRotation = "0.6";
		String nominalSingleCollimationWidthInMM = "0.625";
		String nominalTotalCollimationWidthInMM = "40";
		String pitchFactor = "0.984";
		
		String topZLocationOfReconstructableVolume = "14.5625";			// i.e., S14.250  + nominalSingleCollimationWidthInMM/2
		String topZLocationOfReconstructableVolumeExpected = "S14.5625";
		String bottomZLocationOfReconstructableVolume = "-636.0625";	// i.e., I635.750 - nominalSingleCollimationWidthInMM/2
		String bottomZLocationOfReconstructableVolumeExpected = "I636.0625";
		String lengthOfReconstructableVolumeInMM = "650.625";
		String frameOfReferenceUID = "1.2.124.113532.172.16.11.4.3651756012387523857235";

		CTAcquisitionParameters acqparam0 = new CTAcquisitionParameters(irradiationEventUID,ctScanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,
			null/*scanningLength*/,lengthOfReconstructableVolumeInMM,null/*exposedRangeInMM*/,topZLocationOfReconstructableVolume,bottomZLocationOfReconstructableVolume,null/*topZLocationOfScanningLength*/,null/*bottomZLocationOfScanningLength*/,frameOfReferenceUID,
			nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		acqparam0.deriveScanningLengthFromDLPAndCTDIVolIfGreater(dlp,ctdiVol);
		String scanningLength = "673.38";	// 1362.24/20.23*10;
		
		CTDoseCheckDetails dcd0 = new CTDoseCheckDetails(
			true/*alertDLPValueConfigured*/,
			"1000"/*alertDLPValue*/,
			true/*alertCTDIvolValueConfigured*/,
			"150"/*alertCTDIvolValue*/,
			"1200"/*alertAccumulatedDLPForwardEstimate*/,
			null/*alertAccumulatedCTDIvolForwardEstimate*/,
			"very naughty"/*alertReasonForProceeding*/,
			observerContext.getPersonParticipantAdministering()/*PersonParticipant alertPerson*/,
			true/*notificationDLPValueConfigured*/,
			"500"/*notificationDLPValue*/,
			true/*notificationCTDIvolValueConfigured*/,
			"120"/*notificationCTDIvolValue*/,
			null/*notificationDLPForwardEstimate*/,
			"125"/*notificationCTDIvolForwardEstimate*/,
			"slightly naughty"/*notificationReasonForProceeding*/,
			observerContext.getPersonParticipantAdministering()/*PersonParticipant notificationPerson*/);

		String dlpTotalFromEvents = dlp;

		String dlpTotal = null;
		int totalNumberOfIrradiationEvents = 1;
		String startDateTime = "20010203043000+0000";
		String endDateTime   = "20010203043500+0000";
		String description   = "CAP";
		
		String expectGetHTMLNoDetail =
			  "<tr><td>3764913624</td><td>Smith^Mary</td><td>F</td><td>19600101</td><td align=right>041Y</td><td align=right>68</td><td align=right>1.55</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>CAP</td><td align=right>1362.24 (BODY32)</td><td align=right></td><td align=right></td><td>"+manufacturer+"</td><td>"+modelName+"</td><td>OCR</td></tr>\n"
			;
	
		String expectGetHTMLDetail =
			  "<tr><th>ID</th><th>Name</th><th>Sex</th><th>DOB</th><th>Age</th><th>Weight kg</th><th>Height m</th><th>Accession</th><th>Date</th><th>Modality</th><th>Description</th><th>DLP Total mGy.cm</th><th>DLP HEAD16 mGy.cm</th><th>DLP BODY32 mGy.cm</th><th>Manufacturer</th><th>Model</th><th>From</th></tr>\n"
			+ "<tr><td>3764913624</td><td>Smith^Mary</td><td>F</td><td>19600101</td><td align=right>041Y</td><td align=right>68</td><td align=right>1.55</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>CAP</td><td align=right>1362.24 (BODY32)</td><td align=right></td><td align=right></td><td>"+manufacturer+"</td><td>"+modelName+"</td><td>OCR</td></tr>\n"
			+ "<tr><td colspan=2></td><td colspan=15><table><tr><th>Number</th><th>Type</th><th>Range mm</th><th>CTDIvol mGy</th><th>DLP mGy.cm</th><th>Phantom</th><th>Type</th><th>Anatomy</th><th>Protocol</th><th>Scanning Length mm</th><th>Reconstructable Volume mm</th><th>Exposed Range mm</th><th>Collimation Single/Total mm</th><th>Pitch Factor</th><th>kVP</th><th>Tube Current Mean/Max mA</th><th>Exposure Time/Per Rotation s</th><th>Comment</th></tr>\n"
			+ "<tr><td>Series=2</td><td>Helical</td><td>S14.250-I635.750</td><td>20.23</td><td>1362.24</td><td>BODY32</td><td>Helical</td><td>Chest, Abdomen and Pelvis</td><td>"+acquisitionProtocol+"</td><td>673.38</td><td>650.625 [I636.0625-S14.5625]</td><td></td><td>0.625/40</td><td>0.984:1</td><td>120</td><td>397/433</td><td>1/0.6</td><td>"+comment+"</td></tr>\n"
			+ "</table></td></tr>\n"
			;
		
		CTDose ctDose = new CTDose(ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		acq0.setAcquisitionParameters(acqparam0);
		acq0.setDoseCheckDetails(dcd0);
		ctDose.addAcquisition(acq0);
		ctDose.setDLPTotal(dlpTotal);
		
		ctDose.setObserverContext(observerContext);
		ctDose.setCompositeInstanceContext(compositeInstanceContext);
		
		assertEquals("Checking SourceOfDoseInformation default",SourceOfDoseInformation.AUTOMATED_DATA_COLLECTION,ctDose.getSourceOfDoseInformation());
		ctDose.setSourceOfDoseInformation(source);
		assertEquals("Checking SourceOfDoseInformation is set",source,ctDose.getSourceOfDoseInformation());
		
		assertEquals("Checking null DLP Total",dlpTotal,ctDose.getDLPTotal());
		assertFalse("Checking null DLP Total does not match total from acquisitions",ctDose.specifiedDLPTotalMatchesDLPTotalFromAcquisitions());
		assertEquals("Checking DLP total from acquisitions",dlpTotalFromEvents,ctDose.getDLPTotalFromAcquisitions());
		
		assertEquals("Checking getHTML no detail",expectGetHTMLNoDetail,ctDose.getHTMLTableRow(false));
		assertEquals("Checking getHTML detail",expectGetHTMLDetail,ctDose.getHTMLTableRow(true));

		StructuredReport sr = ctDose.getStructuredReport();
//System.err.print("sr =\n"+sr);
		assertFalse("Checking SR is not null",sr == null);
		AttributeList list = ctDose.getAttributeList();
		assertFalse("Checking SR AttributeList is not null",list == null);
		org.w3c.dom.Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
		assertFalse("Checking SR document is not null",srDocument == null);
//System.err.println("srDocument =\n");
//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		
		XPathFactory xpf = XPathFactory.newInstance();
		
		assertEquals("Checking document title","X-Ray Radiation Dose Report",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/concept/@cm",srDocument));
		assertEquals("Checking procedure reported","Computed Tomography X-Ray",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='121058']/value/@cm",srDocument));
		assertEquals("Checking diagnostic intent","Diagnostic Intent",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='121058']/code[concept/@cv='G-C0E8']/value/@cm",srDocument));

		assertEquals("Checking observer type","Device",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='121005']/value/@cm",srDocument));
		assertEquals("Checking Device Observer UID",deviceUID,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/uidref[concept/@cv='121012']/value",srDocument));
		assertEquals("Checking Device Observer Name",deviceName,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/text[concept/@cv='121013']/value",srDocument));
		assertEquals("Checking Device Observer Manufacturer",manufacturer,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/text[concept/@cv='121014']/value",srDocument));
		assertEquals("Checking Device Observer Model Name",modelName,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/text[concept/@cv='121015']/value",srDocument));
		assertEquals("Checking Device Observer Serial Number",serialNumber,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/text[concept/@cv='121016']/value",srDocument));
		assertEquals("Checking Device Observer Physical Location During Observation",location,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/text[concept/@cv='121017']/value",srDocument));

		assertEquals("Checking Start of X-Ray Irradiation",startDateTime,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/datetime[concept/@cv='113809']/value",srDocument));
		assertEquals("Checking End of X-Ray Irradiation",endDateTime,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/datetime[concept/@cv='113810']/value",srDocument));

		assertEquals("Checking scope of accumulation","Study",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='113705']/value/@cm",srDocument));
		assertEquals("Checking scope of accumulation UIDREF",scopeUID,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='113705']/uidref/value",srDocument));

		// CONTAINER: (113811,DCM,"CT Accumulated Dose Data")
		assertEquals("Checking Total Number of Irradiation Events","1",           xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113812']/value",srDocument));
		assertEquals("Checking Total Number of Irradiation Events units","events",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113812']/units/@cm",srDocument));
		assertEquals("Checking CT Dose Length Product Total",dlpTotalFromEvents,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/value",srDocument));
		assertEquals("Checking CT Dose Length Product Total units","mGy.cm",     xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/units/@cm",srDocument));

		// CONTAINER: (113819,DCM,"CT Acquisition")
		assertEquals("Checking Acquisition Protocol",acquisitionProtocol,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/text[concept/@cv='125203']/value",srDocument));
		assertEquals("Checking Target Region","Chest, Abdomen and Pelvis",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/code[concept/@cv='123014']/value/@cm",srDocument));
		assertEquals("Checking CT Acquisition Type","Spiral Acquisition",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/code[concept/@cv='113820']/value/@cm",srDocument));
		assertEquals("Checking Irradiation Event UID",irradiationEventUID,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/uidref[concept/@cv='113769']/value",srDocument));

		assertEquals("Checking ExposureTime",exposureTimeInSeconds,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113824']/value",srDocument));
		assertEquals("Checking ExposureTime units","s",            xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113824']/units/@cm",srDocument));
		assertEquals("Checking Scanning Length",scanningLength,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113825']/value",srDocument));
		assertEquals("Checking Scanning Length units","mm"    ,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113825']/units/@cm",srDocument));

		assertEquals("Checking Length of Reconstructable Volume",lengthOfReconstructableVolumeInMM,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113893']/value",srDocument));
		assertEquals("Checking Length of Reconstructable Volume units","mm",                       xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113893']/units/@cm",srDocument));
		assertEquals("Checking Top Z Location of Reconstructable Volume",topZLocationOfReconstructableVolume,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113895']/value",srDocument));
		assertEquals("Checking Top Z Location of Reconstructable Volume units","mm" ,                        xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113895']/units/@cm",srDocument));
		assertEquals("Checking Bottom Z Location of Reconstructable Volume",bottomZLocationOfReconstructableVolume,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113896']/value",srDocument));
		assertEquals("Checking Bottom Z Location of Reconstructable Volume units","mm",                            xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113896']/units/@cm",srDocument));
		assertEquals("Checking Frame of Reference UID",frameOfReferenceUID,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/uidref[concept/@cv='112227']/value",srDocument));
		assertEquals("Checking Nominal Single Collimation Width",nominalSingleCollimationWidthInMM,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113826']/value",srDocument));
		assertEquals("Checking Nominal Single Collimation Width units","mm"                       ,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113826']/units/@cm",srDocument));
		assertEquals("Checking Nominal Total Collimation Width",nominalTotalCollimationWidthInMM,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113827']/value",srDocument));
		assertEquals("Checking Nominal Total Collimation Width units","mm"                      ,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113827']/units/@cm",srDocument));
		assertEquals("Checking Pitch Factor",pitchFactor  ,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113828']/value",srDocument));
		assertEquals("Checking Pitch Factor units","ratio",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113828']/units/@cm",srDocument));

		assertEquals("Checking Number of X-Ray Sources","1",                  xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113823']/value",srDocument));
		assertEquals("Checking Number of X-Ray Sources units","X-Ray sources",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/num[concept/@cv='113823']/units/@cm",srDocument));
		assertEquals("Checking Identification Number of the X-Ray Source","1",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/text[concept/@cv='113832']/value",srDocument));
		assertEquals("Checking KVP",kvp,       xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113733']/value",srDocument));
		assertEquals("Checking KVP units","kV",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113733']/units/@cm",srDocument));
		assertEquals("Checking Maximum X-Ray Tube Current",tubeCurrentMaximum,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113833']/value",srDocument));
		assertEquals("Checking Maximum X-Ray Tube Current units","mA", xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113833']/units/@cm",srDocument));
		assertEquals("Checking X-Ray Tube Current",tubeCurrent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113734']/value",srDocument));
		assertEquals("Checking X-Ray Tube Current units","mA", xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113734']/units/@cm",srDocument));
		assertEquals("Checking Exposure Time per Rotation",exposureTimePerRotation,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113834']/value",srDocument));
		assertEquals("Checking Exposure Time per Rotation","s",                    xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113822']/container[concept/@cv='113831']/num[concept/@cv='113834']/units/@cm",srDocument));
		
		assertEquals("Checking Comment",comment,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/text[concept/@cv='121106']/value",srDocument));

		assertEquals("Checking Mean CTDIvol",ctdiVol,    xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113829']/num[concept/@cv='113830']/value",srDocument));
		assertEquals("Checking Mean CTDIvol units","mGy",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113829']/num[concept/@cv='113830']/units/@cm",srDocument));
		assertEquals("Checking CTDIw Phantom Type","IEC Body Dosimetry Phantom",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113829']/code[concept/@cv='113835']/value/@cm",srDocument));
		assertEquals("Checking DLP",dlp,          xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113829']/num[concept/@cv='113838']/value",srDocument));
		assertEquals("Checking DLP units","mGy.cm",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/container[concept/@cv='113829']/num[concept/@cv='113838']/units/@cm",srDocument));

		assertEquals("Checking Source of Dose Information","Derived From Human-Readable Reports",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/code[concept/@cv='113854']/value/@cm",srDocument));

		assertEquals("Checking Device Role in Procedure","Irradiating Device",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/code[concept/@cv='113876']/value/@cm",srDocument));
		assertEquals("Checking Device Manufacturer",manufacturer,             xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/code[concept/@cv='113876']/text[concept/@cv='113878']/value",srDocument));
		assertEquals("Checking Device Model Name",modelName,                  xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/code[concept/@cv='113876']/text[concept/@cv='113879']/value",srDocument));
		assertEquals("Checking Device Serial Number",serialNumber,            xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/code[concept/@cv='113876']/text[concept/@cv='113880']/value",srDocument));
		assertEquals("Checking Device Observer UID",deviceUID,                xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113819']/code[concept/@cv='113876']/uidref[concept/@cv='121012']/value",srDocument));
		
		String expectValidationString = 
		 "Found XRayRadiationDoseSR IOD\n"
		+"Found Root Template TID_10011 (CTRadiationDose)\n"
		+"Root Template Validation Complete\n"
		+"Warning: 1.11.2.1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/CODE (113835,DCM,\"CTDIw Phantom Type\"): Content Item not in template\n"
		+"IOD validation complete\n"
		;
		com.pixelmed.validate.DicomSRValidator validator = new com.pixelmed.validate.DicomSRValidator();
		//validator.setOptionDescribeChecking(true);
System.err.println("validate =\n"+validator.validate(list));
		assertEquals("Checking validation from StructuredReport Document",expectValidationString,validator.validate(list));
		
		assertEquals("Checking PatientName",patientName,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientName));
		assertEquals("Checking PatientID",patientID,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientID));
		assertEquals("Checking PatientBirthDate",patientBirthDate,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientBirthDate));
		assertEquals("Checking PatientSex",patientSex,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientSex));
		assertEquals("Checking PatientAge",patientAge,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge));
		assertEquals("Checking PatientWeight",patientWeight,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientWeight));
		assertEquals("Checking PatientSize",patientSize,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientSize));

		assertEquals("Checking StudyInstanceUID",studyInstanceUID,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyInstanceUID));
		assertEquals("Checking StudyID",studyID,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyID));
		assertEquals("Checking ReferringPhysicianName",referringPhysicianName,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ReferringPhysicianName));
		assertEquals("Checking AccessionNumber","",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.AccessionNumber));
		assertEquals("Checking StudyDate",studyDate,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDate));
		assertEquals("Checking StudyTime",studyTime,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyTime));
		
		assertEquals("Checking SeriesInstanceUID",seriesInstanceUID,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesInstanceUID));
		assertEquals("Checking SeriesNumber",seriesNumber,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesNumber));

		assertEquals("Checking SOPInstanceUID",sopInstanceUID,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPInstanceUID));
		assertEquals("Checking InstanceNumber",instanceNumber,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.InstanceNumber));
		
		// check round trip of creating new CTDose from previously generated SR alone
		{
			String sr1AsString = sr.toString();
			CTDose ctDose2 = new CTDose(sr);
//System.err.println("ctDose2 =\n"+ctDose2);
			StructuredReport sr2 = ctDose2.getStructuredReport(true/*rebuild*/);
			String sr2AsString = sr2.toString();
//System.err.println("sr2AsString =\n"+sr2AsString);
			assertEquals("Checking round trip SR (from SR alone) as string",sr1AsString,sr2AsString);
			String sr1AttributeListAsString = list.toString();
			AttributeList list2 = ctDose2.getAttributeList();
			String sr2AttributeListAsString = list.toString();
//System.err.println("sr2AttributeListAsString =\n"+sr2AttributeListAsString);
			assertEquals("Checking round trip SR AttributeList (from SR alone) as string",sr1AttributeListAsString,sr2AttributeListAsString);
		}
		
		// check round trip of creating new CTDose from previously generated SR and AttributeList
		{
			String sr1AsString = sr.toString();
			CTDose ctDose2 = new CTDose(sr,list);
//System.err.println("ctDose2 =\n"+ctDose2);
			StructuredReport sr2 = ctDose2.getStructuredReport(true/*rebuild*/);
			String sr2AsString = sr2.toString();
//System.err.println("sr2AsString =\n"+sr2AsString);
			assertEquals("Checking round trip SR (from SR and AttributeList) as string",sr1AsString,sr2AsString);
			String sr1AttributeListAsString = list.toString();
			AttributeList list2 = ctDose2.getAttributeList();
			String sr2AttributeListAsString = list.toString();
//System.err.println("sr2AttributeListAsString =\n"+sr2AttributeListAsString);
			assertEquals("Checking round trip SR AttributeList (from SR and AttributeList) as as string",sr1AttributeListAsString,sr2AttributeListAsString);
		}
		
		// check round trip of creating new CTDose from previously generated AttributeList alone
		{
			String sr1AsString = sr.toString();
			CTDose ctDose2 = new CTDose(list);
//System.err.println("ctDose2 =\n"+ctDose2);
			StructuredReport sr2 = ctDose2.getStructuredReport(true/*rebuild*/);
			String sr2AsString = sr2.toString();
//System.err.println("sr2AsString =\n"+sr2AsString);
			assertEquals("Checking round trip SR (from AttributeList alone) as string",sr1AsString,sr2AsString);
			String sr1AttributeListAsString = list.toString();
			AttributeList list2 = ctDose2.getAttributeList();
			String sr2AttributeListAsString = list.toString();
//System.err.println("sr2AttributeListAsString =\n"+sr2AttributeListAsString);
			assertEquals("Checking round trip SR AttributeList (from AttributeList alone) as as string",sr1AttributeListAsString,sr2AttributeListAsString);
		}
	}
	
	
	public void testCTDoseConstructor_PropagationOfDescriptionToAttributeList_IfNoStudyDescriptionInCompositeInstanceContext() throws Exception {

		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		String startDateTime = "20010203043000+0000";
		String endDateTime   = "20010203043500+0000";
		String description   = "Wanted";
		
		CTDose ctDose = new CTDose(ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		ctDose.setCompositeInstanceContext(compositeInstanceContext);
		
		assertEquals("Checking description is set correctly",description,ctDose.getDescription());
		AttributeList list = ctDose.getAttributeList();
		String studyDescription = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDescription);
		assertEquals("Checking description is copied into StudyDescription",description,studyDescription);
	}
	
	
	public void testCTDoseConstructor_PropagationOfDescriptionToAttributeList_DoesNotOverwriteIfStudyDescriptionAlreadyInCompositeInstanceContext() throws Exception {

		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		String startDateTime = "20010203043000+0000";
		String endDateTime   = "20010203043500+0000";
		String description   = "Replacement";
		String studyDescription = "Existing";
		
		CTDose ctDose = new CTDose(ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		
		{ Attribute a = new LongStringAttribute(TagFromName.StudyDescription); a.addValue(studyDescription); compositeInstanceContext.put(a); }
		ctDose.setCompositeInstanceContext(compositeInstanceContext);
		
		assertEquals("Checking description is set correctly",description,ctDose.getDescription());
		AttributeList list = ctDose.getAttributeList();
		String gotStudyDescription = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDescription);
		assertEquals("Checking description does not overwrite StudyDescription",studyDescription,gotStudyDescription);
	}

	public void testCTDoseConstructor_WithSeparateHeadAndBodyTotalDLPConstructor() throws Exception {

		String dlpSubTotalHead = "844.90";
		String dlpSubTotalBody = "622.70";
		String dlpTotal = "1045.15";	// = 844.90/2 + 622.70
		int totalNumberOfIrradiationEvents = 0;
		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		String startDateTime = "20010203043000+0000";
		String endDateTime   = "20010203043500+0000";
		String description   = "HeadAndCAP";
		
		String expectToStringDetail = "Dose\tPatient ID="+patientID+"\tName="+patientName+"\tSex="+patientSex+"\tDOB="+patientBirthDate+"\tAge="+patientAge+"\tWeight="+patientWeight+" kg\tHeight="+patientSize+" m\tAccession=\tStart="+startDateTime+"\tEnd="+endDateTime+"\tModality=CT\tDescription="+description+"\tScope=Study\tUID="+scopeUID+"\tEvents="+Integer.toString(totalNumberOfIrradiationEvents)+"\tDLP Total="+dlpTotal+" (BODY32) (HEAD16 "+dlpSubTotalHead+") (BODY32 "+dlpSubTotalBody+") mGy.cm\n";
		String expectToStringNoDetail = "Dose\tPatient ID="+patientID+"\tName="+patientName+"\tSex="+patientSex+"\tDOB="+patientBirthDate+"\tAge="+patientAge+"\tWeight="+patientWeight+" kg\tHeight="+patientSize+" m\tAccession=\tStart="+startDateTime+"\tModality=CT\tDescription="+description+"\tDLP Total="+dlpTotal+" (BODY32) (HEAD16 "+dlpSubTotalHead+") (BODY32 "+dlpSubTotalBody+") mGy.cm\n";
		
		String expectGetHTMLNoDetail =
			  "<tr><td>"+patientID+"</td><td>"+patientName+"</td><td>"+patientSex+"</td><td>"+patientBirthDate+"</td><td align=right>"+patientAge+"</td><td align=right>"+patientWeight+"</td><td align=right>"+patientSize+"</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>"+description+"</td><td align=right>"+dlpTotal+" (BODY32)</td><td align=right>"+dlpSubTotalHead+"</td><td align=right>"+dlpSubTotalBody+"</td><td>"+manufacturer+"</td><td>"+modelName+"</td><td>MOD</td></tr>\n"
			;
	
		String expectGetHTMLDetail =
			  "<tr><th>ID</th><th>Name</th><th>Sex</th><th>DOB</th><th>Age</th><th>Weight kg</th><th>Height m</th><th>Accession</th><th>Date</th><th>Modality</th><th>Description</th><th>DLP Total mGy.cm</th><th>DLP HEAD16 mGy.cm</th><th>DLP BODY32 mGy.cm</th><th>Manufacturer</th><th>Model</th><th>From</th></tr>\n"
			+ "<tr><td>"+patientID+"</td><td>"+patientName+"</td><td>"+patientSex+"</td><td>"+patientBirthDate+"</td><td align=right>"+patientAge+"</td><td align=right>"+patientWeight+"</td><td align=right>"+patientSize+"</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>"+description+"</td><td align=right>"+dlpTotal+" (BODY32)</td><td align=right>"+dlpSubTotalHead+"</td><td align=right>"+dlpSubTotalBody+"</td><td>"+manufacturer+"</td><td>"+modelName+"</td><td>MOD</td></tr>\n"
			;

		CTDose ctDose = new CTDose(dlpSubTotalHead,dlpSubTotalBody,totalNumberOfIrradiationEvents,ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		
		ctDose.setObserverContext(observerContext);		// else SR roundtrip will not match
		ctDose.setCompositeInstanceContext(compositeInstanceContext);

		assertEquals("Checking DLP Total",dlpTotal,ctDose.getDLPTotal());
		assertEquals("Checking DLP Total Head",dlpSubTotalHead,ctDose.getDLPSubTotalHead());
		assertEquals("Checking DLP Total Body",dlpSubTotalBody,ctDose.getDLPSubTotalBody());
		
		assertEquals("Checking toString default",expectToStringDetail,ctDose.toString());
		assertEquals("Checking toString detail",expectToStringDetail,ctDose.toString(true,false));
		assertEquals("Checking toString no detail",expectToStringNoDetail,ctDose.toString(false,false));
		assertEquals("Checking getHTML no detail",expectGetHTMLNoDetail,ctDose.getHTMLTableRow(false));
		assertEquals("Checking getHTML detail",expectGetHTMLDetail,ctDose.getHTMLTableRow(true));

		StructuredReport sr = ctDose.getStructuredReport();
//System.err.print("sr =\n"+sr);
		assertFalse("Checking SR is not null",sr == null);
		AttributeList list = ctDose.getAttributeList();
		assertFalse("Checking SR AttributeList is not null",list == null);
		org.w3c.dom.Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
		assertFalse("Checking SR document is not null",srDocument == null);
//System.err.println("srDocument =\n");
//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();
		// CONTAINER: (113811,DCM,"CT Accumulated Dose Data")

		assertEquals("Checking CT Dose Length Product Total",dlpTotal,                xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701' and concept/@csd='DCM']/container[concept/@cv='113811' and concept/@csd='DCM']/num[concept/@cv='113813' and concept/@csd='DCM']/value",srDocument));
		//assertEquals("Checking CT Dose Length Product Total",dlpTotal,                xpf.newXPath().evaluate("//num[concept/@cv='113813']/value",srDocument));
		//assertEquals("Checking CT Dose Length Product Total",dlpTotal,              ((ContentItemFactory.NumericContentItem)((ContentItem)sr.getRoot()).getNamedChild("DCM","113811").getNamedChild("DCM","113813")).getNumericValue());

  		assertEquals("Checking CT Dose Length Product Total units","mGy.cm",          xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701' and concept/@csd='DCM']/container[concept/@cv='113811' and concept/@csd='DCM']/num[concept/@cv='113813' and concept/@csd='DCM']/units/@cm",srDocument));
		assertEquals("Checking CTDIw Phantom Type","IEC Body Dosimetry Phantom",      xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701' and concept/@csd='DCM']/container[concept/@cv='113811' and concept/@csd='DCM']/num[concept/@cv='113813' and concept/@csd='DCM']/code[concept/@cv='113835' and concept/@csd='DCM']/value/@cm",srDocument));
		assertEquals("Checking CT Dose Length Product Head Sub-Total",dlpSubTotalHead,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701' and concept/@csd='DCM']/container[concept/@cv='113811' and concept/@csd='DCM']/num[concept/@cv='113813' and concept/@csd='DCM']/num [concept/@cv='220005' and code/concept/@cv='113835' and code/concept/@csd='DCM' and code/value/@cv='113690' and code/value/@csd='DCM']/value",srDocument));
		assertEquals("Checking CT Dose Length Product Body Sub-Total",dlpSubTotalBody,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701' and concept/@csd='DCM']/container[concept/@cv='113811' and concept/@csd='DCM']/num[concept/@cv='113813' and concept/@csd='DCM']/num [concept/@cv='220005' and code/concept/@cv='113835' and code/concept/@csd='DCM' and code/value/@cv='113691' and code/value/@csd='DCM']/value",srDocument));
		
		String expectValidationString = 
		 "Found XRayRadiationDoseSR IOD\n"
		+"Found Root Template TID_10011 (CTRadiationDose)\n"
		+"Warning: Template 10012 CTAccumulatedDoseData/[Row 1] CONTAINER (113811,DCM,\"CT Accumulated Dose Data\"): /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\"): value of 0 for Total Number of Irradiation Events is not illegal, but is unlikely to be correct\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\"): within 1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\"): Missing required content item\n"
		+"Root Template Validation Complete\n"
		+"Warning: 1.11.2.1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/CODE (113835,DCM,\"CTDIw Phantom Type\"): Content Item not in template\n"
		+"Warning: 1.11.2.2: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\"): Content Item not in template\n"
		+"Warning: 1.11.2.2.1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\")/CODE (113835,DCM,\"CTDIw Phantom Type\"): Content Item not in template\n"
		+"Warning: 1.11.2.3: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\"): Content Item not in template\n"
		+"Warning: 1.11.2.3.1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\")/CODE (113835,DCM,\"CTDIw Phantom Type\"): Content Item not in template\n"
		+"IOD validation complete\n"
		;
		com.pixelmed.validate.DicomSRValidator validator = new com.pixelmed.validate.DicomSRValidator();
		//validator.setOptionDescribeChecking(true);
System.err.println("validate =\n"+validator.validate(list));
		assertEquals("Checking validation from StructuredReport Document",expectValidationString,validator.validate(list));

		
		// check round trip of creating new CTDose from previously generated SR alone (to make sure DLP sub-totals are read)
		{
			String sr1AsString = sr.toString();
			CTDose ctDose2 = new CTDose(sr);
//System.err.println("ctDose2 =\n"+ctDose2);
			StructuredReport sr2 = ctDose2.getStructuredReport(true/*rebuild*/);
			String sr2AsString = sr2.toString();
//System.err.println("sr2AsString =\n"+sr2AsString);
			assertEquals("Checking round trip SR (from SR alone) as string",sr1AsString,sr2AsString);
			String sr1AttributeListAsString = list.toString();
			AttributeList list2 = ctDose2.getAttributeList();
			String sr2AttributeListAsString = list.toString();
//System.err.println("sr2AttributeListAsString =\n"+sr2AttributeListAsString);
			assertEquals("Checking round trip SR AttributeList (from SR alone) as string",sr1AttributeListAsString,sr2AttributeListAsString);
		}
	}

	public void testCTDoseConstructor_WithSeparateHeadAndBodyTotalDLPSet() throws Exception {
		
		String dlpSubTotalHead = "844.90";
		String dlpSubTotalBody = "622.70";
		String dlpTotal = "1045.15";	// = 844.90/2 + 622.70
		int totalNumberOfIrradiationEvents = 0;
		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		String startDateTime = "20010203043000+0000";
		String endDateTime   = "20010203043500+0000";
		String description   = "HeadAndCAP";
		
		String expectToStringDetail = "Dose\tPatient ID=3764913624\tName=Smith^Mary\tSex=F\tDOB=19600101\tAge=041Y\tWeight=68 kg\tHeight=1.55 m\tAccession=\tStart="+startDateTime+"\tEnd="+endDateTime+"\tModality=CT\tDescription="+description+"\tScope=Study\tUID="+scopeUID+"\tEvents="+Integer.toString(totalNumberOfIrradiationEvents)+"\tDLP Total="+dlpTotal+" (BODY32) (HEAD16 "+dlpSubTotalHead+") (BODY32 "+dlpSubTotalBody+") mGy.cm\n";
		String expectToStringNoDetail = "Dose\tPatient ID=3764913624\tName=Smith^Mary\tSex=F\tDOB=19600101\tAge=041Y\tWeight=68 kg\tHeight=1.55 m\tAccession=\tStart="+startDateTime+"\tModality=CT\tDescription="+description+"\tDLP Total="+dlpTotal+" (BODY32) (HEAD16 "+dlpSubTotalHead+") (BODY32 "+dlpSubTotalBody+") mGy.cm\n";
		
		String expectGetHTMLNoDetail =
			  "<tr><td>"+patientID+"</td><td>"+patientName+"</td><td>"+patientSex+"</td><td>"+patientBirthDate+"</td><td align=right>"+patientAge+"</td><td align=right>"+patientWeight+"</td><td align=right>"+patientSize+"</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>"+description+"</td><td align=right>"+dlpTotal+" (BODY32)</td><td align=right>"+dlpSubTotalHead+"</td><td align=right>"+dlpSubTotalBody+"</td><td></td><td></td><td>MOD</td></tr>\n"
			;
	
		String expectGetHTMLDetail =
			  "<tr><th>ID</th><th>Name</th><th>Sex</th><th>DOB</th><th>Age</th><th>Weight kg</th><th>Height m</th><th>Accession</th><th>Date</th><th>Modality</th><th>Description</th><th>DLP Total mGy.cm</th><th>DLP HEAD16 mGy.cm</th><th>DLP BODY32 mGy.cm</th><th>Manufacturer</th><th>Model</th><th>From</th></tr>\n"
			+ "<tr><td>"+patientID+"</td><td>"+patientName+"</td><td>"+patientSex+"</td><td>"+patientBirthDate+"</td><td align=right>"+patientAge+"</td><td align=right>"+patientWeight+"</td><td align=right>"+patientSize+"</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>"+description+"</td><td align=right>"+dlpTotal+" (BODY32)</td><td align=right>"+dlpSubTotalHead+"</td><td align=right>"+dlpSubTotalBody+"</td><td></td><td></td><td>MOD</td></tr>\n"
			;
		
		CTDose ctDose = new CTDose(ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		ctDose.setDLPTotal(dlpSubTotalHead,dlpSubTotalBody);

		//ctDose.setObserverContext(observerContext);
		ctDose.setCompositeInstanceContext(compositeInstanceContext);

		assertEquals("Checking DLP Total",dlpTotal,ctDose.getDLPTotal());
		assertEquals("Checking DLP Total Head",dlpSubTotalHead,ctDose.getDLPSubTotalHead());
		assertEquals("Checking DLP Total Body",dlpSubTotalBody,ctDose.getDLPSubTotalBody());
		
		assertEquals("Checking toString default",expectToStringDetail,ctDose.toString());
		assertEquals("Checking toString detail",expectToStringDetail,ctDose.toString(true,false));
		assertEquals("Checking toString no detail",expectToStringNoDetail,ctDose.toString(false,false));
		assertEquals("Checking getHTML no detail",expectGetHTMLNoDetail,ctDose.getHTMLTableRow(false));
		assertEquals("Checking getHTML detail",expectGetHTMLDetail,ctDose.getHTMLTableRow(true));

		assertFalse("Checking SR is not null",ctDose.getStructuredReport() == null);
	}
	

	public void testCTDoseConstructor_WithSeparateHeadAndBodyAcquisitions() throws Exception {

		String[] ctdiVolArray        = { "14.80",   "18.60",   "16.80",   "18.20"  };
		String[] dlpArray            = { "318.60",  "516.10",  "462.30",  "649.80" };
		String[] scanningLengthArray = { "21.53",   "27.75",   "27.52",   "35.70"  };	// i.e., DLP/CTDIvol*10
		String[] phantomTypeArray    = { "HEAD16",  "HEAD16",  "BODY32",  "BODY32" };
		String[] scanTypeArray       = { "Axial",   "Helical", "Helical", "Helical" };
		
		String scopeUID = "1.2.124.113532.172.16.11.4.20090807.144612.3424396";
		CTDoseAcquisition acq0 = new CTDoseAcquisition(scopeUID,false/*isSeries*/,null,CTScanType.selectFromDescription(scanTypeArray[0]),null/*scanRangeArray*/,ctdiVolArray[0],dlpArray[0],CTPhantomType.selectFromDescription(phantomTypeArray[0]));
		CTDoseAcquisition acq1 = new CTDoseAcquisition(scopeUID,false/*isSeries*/,null,CTScanType.selectFromDescription(scanTypeArray[1]),null/*scanRangeArray*/,ctdiVolArray[1],dlpArray[1],CTPhantomType.selectFromDescription(phantomTypeArray[1]));
		CTDoseAcquisition acq2 = new CTDoseAcquisition(scopeUID,false/*isSeries*/,null,CTScanType.selectFromDescription(scanTypeArray[2]),null/*scanRangeArray*/,ctdiVolArray[2],dlpArray[2],CTPhantomType.selectFromDescription(phantomTypeArray[2]));
		CTDoseAcquisition acq3 = new CTDoseAcquisition(scopeUID,false/*isSeries*/,null,CTScanType.selectFromDescription(scanTypeArray[3]),null/*scanRangeArray*/,ctdiVolArray[3],dlpArray[3],CTPhantomType.selectFromDescription(phantomTypeArray[3]));

		String dlpSubTotalHead = "834.70";
		String dlpSubTotalBody = "1112.10";
		String dlpTotal = "1529.45";	// = 834.70/2 + 1112.10
		int totalNumberOfIrradiationEvents = 4;
		String startDateTime = "20010203043000+0000";
		String endDateTime   = "20010203043500+0000";
		String description   = "HeadAndCAP";
		
		String expectToStringDetail = "Dose\tPatient ID="+patientID+"\tName="+patientName+"\tSex="+patientSex+"\tDOB="+patientBirthDate+"\tAge="+patientAge+"\tWeight="+patientWeight+" kg\tHeight="+patientSize+" m\tAccession=\tStart="+startDateTime+"\tEnd="+endDateTime+"\tModality=CT\tDescription="+description+"\tScope=Study\tUID="+scopeUID+"\tEvents="+Integer.toString(totalNumberOfIrradiationEvents)+"\tDLP Total="+dlpTotal+" (BODY32) (HEAD16 "+dlpSubTotalHead+") (BODY32 "+dlpSubTotalBody+") mGy.cm\n"
			+ "\tAcq=null\t"+scanTypeArray[0]+"\tRange=null mm\tCTDIvol="+ctdiVolArray[0]+" mGy\tDLP="+dlpArray[0]+" mGy.cm\tPhantom="+phantomTypeArray[0]+"\n"
			+ "\tAcq=null\t"+scanTypeArray[1]+"\tRange=null mm\tCTDIvol="+ctdiVolArray[1]+" mGy\tDLP="+dlpArray[1]+" mGy.cm\tPhantom="+phantomTypeArray[1]+"\n"
			+ "\tAcq=null\t"+scanTypeArray[2]+"\tRange=null mm\tCTDIvol="+ctdiVolArray[2]+" mGy\tDLP="+dlpArray[2]+" mGy.cm\tPhantom="+phantomTypeArray[2]+"\n"
			+ "\tAcq=null\t"+scanTypeArray[3]+"\tRange=null mm\tCTDIvol="+ctdiVolArray[3]+" mGy\tDLP="+dlpArray[3]+" mGy.cm\tPhantom="+phantomTypeArray[3]+"\n"
			;

		String expectToStringNoDetail = "Dose\tPatient ID="+patientID+"\tName="+patientName+"\tSex="+patientSex+"\tDOB="+patientBirthDate+"\tAge="+patientAge+"\tWeight="+patientWeight+" kg\tHeight="+patientSize+" m\tAccession=\tStart="+startDateTime+"\tModality=CT\tDescription="+description+"\tDLP Total="+dlpTotal+" (BODY32) (HEAD16 "+dlpSubTotalHead+") (BODY32 "+dlpSubTotalBody+") mGy.cm\n";
		
		String expectGetHTMLNoDetail =
			  "<tr><td>"+patientID+"</td><td>"+patientName+"</td><td>"+patientSex+"</td><td>"+patientBirthDate+"</td><td align=right>"+patientAge+"</td><td align=right>"+patientWeight+"</td><td align=right>"+patientSize+"</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>"+description+"</td><td align=right>"+dlpTotal+" (BODY32)</td><td align=right>"+dlpSubTotalHead+"</td><td align=right>"+dlpSubTotalBody+"</td><td>"+manufacturer+"</td><td>"+modelName+"</td><td>MOD</td></tr>\n"
			;
	
		String expectGetHTMLDetail =
			  "<tr><th>ID</th><th>Name</th><th>Sex</th><th>DOB</th><th>Age</th><th>Weight kg</th><th>Height m</th><th>Accession</th><th>Date</th><th>Modality</th><th>Description</th><th>DLP Total mGy.cm</th><th>DLP HEAD16 mGy.cm</th><th>DLP BODY32 mGy.cm</th><th>Manufacturer</th><th>Model</th><th>From</th></tr>\n"
			+ "<tr><td>"+patientID+"</td><td>"+patientName+"</td><td>"+patientSex+"</td><td>"+patientBirthDate+"</td><td align=right>"+patientAge+"</td><td align=right>"+patientWeight+"</td><td align=right>"+patientSize+"</td><td></td><td>2001/02/03 04:30:00</td><td>CT</td><td>"+description+"</td><td align=right>"+dlpTotal+" (BODY32)</td><td align=right>"+dlpSubTotalHead+"</td><td align=right>"+dlpSubTotalBody+"</td><td>"+manufacturer+"</td><td>"+modelName+"</td><td>MOD</td></tr>\n"
			+ "<tr><td colspan=2></td><td colspan=15><table><tr><th>Number</th><th>Type</th><th>Range mm</th><th>CTDIvol mGy</th><th>DLP mGy.cm</th><th>Phantom</th></tr>\n"
			+ "<tr><td></td><td>"+scanTypeArray[0]+"</td><td></td><td>"+ctdiVolArray[0]+"</td><td>"+dlpArray[0]+"</td><td>"+phantomTypeArray[0]+"</td></tr>\n"
			+ "<tr><td></td><td>"+scanTypeArray[1]+"</td><td></td><td>"+ctdiVolArray[1]+"</td><td>"+dlpArray[1]+"</td><td>"+phantomTypeArray[1]+"</td></tr>\n"
			+ "<tr><td></td><td>"+scanTypeArray[2]+"</td><td></td><td>"+ctdiVolArray[2]+"</td><td>"+dlpArray[2]+"</td><td>"+phantomTypeArray[2]+"</td></tr>\n"
			+ "<tr><td></td><td>"+scanTypeArray[3]+"</td><td></td><td>"+ctdiVolArray[3]+"</td><td>"+dlpArray[3]+"</td><td>"+phantomTypeArray[3]+"</td></tr>\n"
			+ "</table></td></tr>\n"
			;

		//CTDose ctDose = new CTDose(dlpSubTotalHead,dlpSubTotalBody,totalNumberOfIrradiationEvents,ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		CTDose ctDose = new CTDose(ScopeOfDoseAccummulation.STUDY,scopeUID,startDateTime,endDateTime,description);
		ctDose.setDLPTotal(dlpSubTotalHead,dlpSubTotalBody);
		ctDose.addAcquisition(acq0);
		ctDose.addAcquisition(acq1);
		ctDose.addAcquisition(acq2);
		ctDose.addAcquisition(acq3);
		
		ctDose.setObserverContext(observerContext);		// else SR roundtrip will not match
		ctDose.setCompositeInstanceContext(compositeInstanceContext);

		assertEquals("Checking DLP Total",dlpTotal,ctDose.getDLPTotal());
		assertEquals("Checking DLP Total (to use)",dlpTotal,ctDose.getDLPTotalToUse());
		assertEquals("Checking DLP Total (CombinedFromHeadAndBodyPhantomValues)",dlpTotal,ctDose.getDLPTotalCombinedFromHeadAndBodyPhantomValues());
		assertEquals("Checking DLP Total (from acquisitions)",dlpTotal,ctDose.getDLPTotalFromAcquisitions());
		
		assertEquals("Checking DLP Total Head",dlpSubTotalHead,ctDose.getDLPSubTotalHead());
		assertEquals("Checking DLP Total Body",dlpSubTotalBody,ctDose.getDLPSubTotalBody());
		
		assertEquals("Checking toString default",expectToStringDetail,ctDose.toString());
		assertEquals("Checking toString detail",expectToStringDetail,ctDose.toString(true,false));
		assertEquals("Checking toString no detail",expectToStringNoDetail,ctDose.toString(false,false));
		assertEquals("Checking getHTML no detail",expectGetHTMLNoDetail,ctDose.getHTMLTableRow(false));
		assertEquals("Checking getHTML detail",expectGetHTMLDetail,ctDose.getHTMLTableRow(true));

		StructuredReport sr = ctDose.getStructuredReport();
//System.err.print("sr =\n"+sr);
		assertFalse("Checking SR is not null",sr == null);
		AttributeList list = ctDose.getAttributeList();
		assertFalse("Checking SR AttributeList is not null",list == null);
		org.w3c.dom.Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
		assertFalse("Checking SR document is not null",srDocument == null);
//System.err.println("srDocument =\n");
//XMLRepresentationOfStructuredReportObjectFactory.write(System.err,srDocument);
		XPathFactory xpf = XPathFactory.newInstance();
		// CONTAINER: (113811,DCM,"CT Accumulated Dose Data")
		assertEquals("Checking CT Dose Length Product Total",dlpTotal,                xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/value",srDocument));
		assertEquals("Checking CT Dose Length Product Total units","mGy.cm",          xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/units/@cm",srDocument));
		assertEquals("Checking CTDIw Phantom Type","IEC Body Dosimetry Phantom",      xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/code[concept/@cv='113835']/value/@cm",srDocument));
		assertEquals("Checking CT Dose Length Product Head Sub-Total",dlpSubTotalHead,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/num[concept/@cv='220005' and code/concept/@cv='113835' and code/value/@cv='113690']/value",srDocument));
		assertEquals("Checking CT Dose Length Product Body Sub-Total",dlpSubTotalBody,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='113701']/container[concept/@cv='113811']/num[concept/@cv='113813']/num[concept/@cv='220005' and code/concept/@cv='113835' and code/value/@cv='113691']/value",srDocument));
		
		String expectValidationString = 
		 "Found XRayRadiationDoseSR IOD\n"
		+"Found Root Template TID_10011 (CTRadiationDose)\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 8] NUM (113824,DCM,\"Exposure Time\"): within 1.12.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 10] NUM (113826,DCM,\"Nominal Single Collimation Width\"): within 1.12.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 11] NUM (113827,DCM,\"Nominal Total Collimation Width\"): within 1.12.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 12] NUM (113828,DCM,\"Pitch Factor\"): within 1.12.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing conditional content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 13] NUM (113823,DCM,\"Number of X-Ray Sources\"): within 1.12.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 14] CONTAINER (113831,DCM,\"CT X-Ray Source Parameters\"): within 1.12.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 8] NUM (113824,DCM,\"Exposure Time\"): within 1.13.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 10] NUM (113826,DCM,\"Nominal Single Collimation Width\"): within 1.13.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 11] NUM (113827,DCM,\"Nominal Total Collimation Width\"): within 1.13.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 12] NUM (113828,DCM,\"Pitch Factor\"): within 1.13.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing conditional content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 13] NUM (113823,DCM,\"Number of X-Ray Sources\"): within 1.13.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 14] CONTAINER (113831,DCM,\"CT X-Ray Source Parameters\"): within 1.13.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 8] NUM (113824,DCM,\"Exposure Time\"): within 1.14.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 10] NUM (113826,DCM,\"Nominal Single Collimation Width\"): within 1.14.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 11] NUM (113827,DCM,\"Nominal Total Collimation Width\"): within 1.14.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 12] NUM (113828,DCM,\"Pitch Factor\"): within 1.14.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing conditional content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 13] NUM (113823,DCM,\"Number of X-Ray Sources\"): within 1.14.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 14] CONTAINER (113831,DCM,\"CT X-Ray Source Parameters\"): within 1.14.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 8] NUM (113824,DCM,\"Exposure Time\"): within 1.15.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 10] NUM (113826,DCM,\"Nominal Single Collimation Width\"): within 1.15.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 11] NUM (113827,DCM,\"Nominal Total Collimation Width\"): within 1.15.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 12] NUM (113828,DCM,\"Pitch Factor\"): within 1.15.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing conditional content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 13] NUM (113823,DCM,\"Number of X-Ray Sources\"): within 1.15.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Error: Template 10013 CTIrradiationEventData/[Row 1] CONTAINER (113819,DCM,\"CT Acquisition\")/[Row 7] CONTAINER (113822,DCM,\"CT Acquisition Parameters\")/[Row 14] CONTAINER (113831,DCM,\"CT X-Ray Source Parameters\"): within 1.15.4: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113819,DCM,\"CT Acquisition\")/CONTAINER (113822,DCM,\"CT Acquisition Parameters\"): Missing required content item\n"
		+"Root Template Validation Complete\n"
		+"Warning: 1.11.2.1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/CODE (113835,DCM,\"CTDIw Phantom Type\"): Content Item not in template\n"
		+"Warning: 1.11.2.2: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\"): Content Item not in template\n"
		+"Warning: 1.11.2.2.1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\")/CODE (113835,DCM,\"CTDIw Phantom Type\"): Content Item not in template\n"
		+"Warning: 1.11.2.3: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\"): Content Item not in template\n"
		+"Warning: 1.11.2.3.1: /CONTAINER (113701,DCM,\"X-Ray Radiation Dose Report\")/CONTAINER (113811,DCM,\"CT Accumulated Dose Data\")/NUM (113813,DCM,\"CT Dose Length Product Total\")/NUM (220005,99PMP,\"CT Dose Length Product Sub-Total\")/CODE (113835,DCM,\"CTDIw Phantom Type\"): Content Item not in template\n"
		+"IOD validation complete\n"
		;
		com.pixelmed.validate.DicomSRValidator validator = new com.pixelmed.validate.DicomSRValidator();
		//validator.setOptionDescribeChecking(true);
System.err.println("validate =\n"+validator.validate(list));
		assertEquals("Checking validation from StructuredReport Document",expectValidationString,validator.validate(list));

		
		// check round trip of creating new CTDose from previously generated SR alone (to make sure DLP sub-totals are read)
		{
			String sr1AsString = sr.toString();
			CTDose ctDose2 = new CTDose(sr);
//System.err.println("ctDose2 =\n"+ctDose2);
			StructuredReport sr2 = ctDose2.getStructuredReport(true/*rebuild*/);
			String sr2AsString = sr2.toString();
//System.err.println("sr2AsString =\n"+sr2AsString);
			assertEquals("Checking round trip SR (from SR alone) as string",sr1AsString,sr2AsString);
			String sr1AttributeListAsString = list.toString();
			AttributeList list2 = ctDose2.getAttributeList();
			String sr2AttributeListAsString = list.toString();
//System.err.println("sr2AttributeListAsString =\n"+sr2AttributeListAsString);
			assertEquals("Checking round trip SR AttributeList (from SR alone) as string",sr1AttributeListAsString,sr2AttributeListAsString);
		}
	}

}
