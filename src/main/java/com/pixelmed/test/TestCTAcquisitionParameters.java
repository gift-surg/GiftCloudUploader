/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.ContentItem;
import com.pixelmed.dose.*;

import junit.framework.*;

import java.util.Locale;

public class TestCTAcquisitionParameters extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCTAcquisitionParameters(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTAcquisitionParameters.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCTAcquisitionParameters");
		
		suite.addTest(new TestCTAcquisitionParameters("TestCTAcquisitionParametersConstructor_WithAllParameters"));
		suite.addTest(new TestCTAcquisitionParameters("TestCTAcquisitionParametersConstructor_Equality"));
		suite.addTest(new TestCTAcquisitionParameters("TestCTAcquisitionParametersConstructor_EqualityWithSomeNullValues"));
		suite.addTest(new TestCTAcquisitionParameters("TestCTAcquisitionParametersConstructor_Equality_WithTID10014ScanningLength"));
		suite.addTest(new TestCTAcquisitionParameters("TestCTAcquisitionParametersConstructor_WithAllParameters_CloneConstructor"));
		suite.addTest(new TestCTAcquisitionParameters("TestCTAcquisitionParametersConstructor_WithAllParameters_Merge"));
		
		return suite;
	}
	
	protected void setUp() {
		Locale.setDefault(Locale.FRENCH);	// forces check that "," is not being used as decimal point in any double to string conversions
	}
	
	protected void tearDown() {
	}
	
	public void TestCTAcquisitionParametersConstructor_WithAllParameters() throws Exception {
		
		String irradiationEventUID = "1.2.3.4";
		String exposureTimeInSeconds = "1";
		String scanningLengthInMM = "750";
		String lengthOfReconstructableVolumeInMM = "650";
		String exposedRangeInMM = "760";
		String topZLocationOfReconstructableVolume= "-50";
		String topZLocationOfReconstructableVolumeExpected= "I50";
		String bottomZLocationOfReconstructableVolume = "-700";
		String bottomZLocationOfReconstructableVolumeExpected = "I700";
		String topZLocationOfScanningLength = "0";
		String topZLocationOfScanningLengthExpected = "S0";
		String bottomZLocationOfScanningLength = "-750";
		String bottomZLocationOfScanningLengthExpected = "I750";
		String frameOfReferenceUID = "1.2.3.6";
		String nominalSingleCollimationWidthInMM = "0.625";
		String nominalTotalCollimationWidthInMM = "40";
		String pitchFactor = "0.984";
		String kvp = "120";
		String tubeCurrent = "397";
		String tubeCurrentMaximum = "433";
		String exposureTimePerRotation = "0.6";
		String anatomyCodeMeaning = "Chest, Abdomen and Pelvis";
		CodedSequenceItem anatomy = new CodedSequenceItem("R-FAB56","SRT",anatomyCodeMeaning);
		CTScanType scanType = CTScanType.HELICAL;
		String acquisitionProtocol = "5.90 CHEST+ABDOMEN+PELVIS";
		String comment = "Non-contrast thin";
		
		CTAcquisitionParameters ctAcquisitionParameters = new CTAcquisitionParameters(irradiationEventUID,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,lengthOfReconstructableVolumeInMM,exposedRangeInMM,topZLocationOfReconstructableVolume,bottomZLocationOfReconstructableVolume,topZLocationOfScanningLength,bottomZLocationOfScanningLength,frameOfReferenceUID,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		
		String expectToString = "\tIrradiationEventUID="+irradiationEventUID+"\tHelical\tAnatomy="+anatomy+"\tProtocol="+acquisitionProtocol
			+"\tScanningLength="+scanningLengthInMM+" mm ["+bottomZLocationOfScanningLengthExpected+"-"+topZLocationOfScanningLengthExpected+"]"
			+"\tLengthOfReconstructableVolume="+lengthOfReconstructableVolumeInMM+" mm ["+bottomZLocationOfReconstructableVolumeExpected+"-"+topZLocationOfReconstructableVolumeExpected+"]"
			+"\tExposedRange="+exposedRangeInMM+" mm"
			+"\tFrameOfReferenceUID="+frameOfReferenceUID
			+"\tCollimation single/total="+nominalSingleCollimationWidthInMM+"/"+nominalTotalCollimationWidthInMM+" mm"+"\tPitchFactor="+pitchFactor+":1"
			+"\tKVP="+kvp+" kVP"+"\tTubeCurrent/Max="+tubeCurrent+"/"+tubeCurrentMaximum+" mA"+"\tExposure time/per rotation="+exposureTimeInSeconds+"/"+exposureTimePerRotation+" s\tComment="+comment+"\n";
		
		String expectToStringPretty = "\tHelical\t"+anatomyCodeMeaning+"\t"+acquisitionProtocol
			+"\t"+scanningLengthInMM+" mm ["+bottomZLocationOfScanningLengthExpected+"-"+topZLocationOfScanningLengthExpected+"]"
			+"\t"+lengthOfReconstructableVolumeInMM+" mm ["+bottomZLocationOfReconstructableVolumeExpected+"-"+topZLocationOfReconstructableVolumeExpected+"]"
			+"\t"+exposedRangeInMM+" mm"
			+"\t"+nominalSingleCollimationWidthInMM+"/"+nominalTotalCollimationWidthInMM+" mm"+"\t"+pitchFactor+":1"
			+"\t"+kvp+" kVP"+"\t"+tubeCurrent+"/"+tubeCurrentMaximum+" mA"+"\t"+exposureTimeInSeconds+"/"+exposureTimePerRotation+" s\t"+comment+"\n";
		
		String expectGetHTMLHeader =
			  "<th>Type</th><th>Anatomy</th><th>Protocol</th><th>Scanning Length mm</th><th>Reconstructable Volume mm</th><th>Exposed Range mm</th><th>Collimation Single/Total mm</th><th>Pitch Factor</th><th>kVP</th><th>Tube Current Mean/Max mA</th><th>Exposure Time/Per Rotation s</th><th>Comment</th>"
			;
		
		String expectGetHTML =
			  "<td>Helical</td><td>"+anatomyCodeMeaning+"</td><td>"+acquisitionProtocol
			  +"</td><td>"+scanningLengthInMM+" ["+bottomZLocationOfScanningLengthExpected+"-"+topZLocationOfScanningLengthExpected+"]"
			  +"</td><td>"+lengthOfReconstructableVolumeInMM+" ["+bottomZLocationOfReconstructableVolumeExpected+"-"+topZLocationOfReconstructableVolumeExpected+"]"
			  +"</td><td>"+exposedRangeInMM
			  +"</td><td>"+nominalSingleCollimationWidthInMM+"/"+nominalTotalCollimationWidthInMM
			  +"</td><td>"+pitchFactor+":1"
			  +"</td><td>"+kvp
			  +"</td><td>"+tubeCurrent+"/"+tubeCurrentMaximum
			  +"</td><td>1/"+exposureTimePerRotation
			  +"</td><td>"+comment+"</td>"
			;

		assertEquals("Checking IrradiationEventUID equality",irradiationEventUID,ctAcquisitionParameters.getIrradiationEventUID());
		assertEquals("Checking ScanType equality",scanType,ctAcquisitionParameters.getScanType());
		assertEquals("Checking Anatomy equality",anatomy,ctAcquisitionParameters.getAnatomy());
		assertEquals("Checking AcquisitionProtocol equality",acquisitionProtocol,ctAcquisitionParameters.getAcquisitionProtocol());
		assertEquals("Checking Comment equality",comment,ctAcquisitionParameters.getComment());
		assertEquals("Checking ExposureTimeInSeconds equality",exposureTimeInSeconds,ctAcquisitionParameters.getExposureTimeInSeconds());
		assertEquals("Checking ScanningLengthInMM equality",scanningLengthInMM,ctAcquisitionParameters.getScanningLengthInMM());
		assertEquals("Checking LengthOfReconstructableVolumeInMM equality",lengthOfReconstructableVolumeInMM,ctAcquisitionParameters.getLengthOfReconstructableVolumeInMM());
		assertEquals("Checking ExposedRangeInMM equality",exposedRangeInMM,ctAcquisitionParameters.getExposedRangeInMM());
		assertEquals("Checking TopZLocationOfReconstructableVolume equality",topZLocationOfReconstructableVolume,ctAcquisitionParameters.getTopZLocationOfReconstructableVolume());
		assertEquals("Checking BottomZLocationOfReconstructableVolume equality",bottomZLocationOfReconstructableVolume,ctAcquisitionParameters.getBottomZLocationOfReconstructableVolume());
		assertEquals("Checking TopZLocationOfScanningLength equality",topZLocationOfScanningLength,ctAcquisitionParameters.getTopZLocationOfScanningLength());
		assertEquals("Checking BottomZLocationOfScanningLength equality",bottomZLocationOfScanningLength,ctAcquisitionParameters.getBottomZLocationOfScanningLength());
		assertEquals("Checking FrameOfReferenceUID equality",frameOfReferenceUID,ctAcquisitionParameters.getFrameOfReferenceUID());
		assertEquals("Checking NominalSingleCollimationWidthInMM equality",nominalSingleCollimationWidthInMM,ctAcquisitionParameters.getNominalSingleCollimationWidthInMM());
		assertEquals("Checking NominalTotalCollimationWidthInMM equality",nominalTotalCollimationWidthInMM,ctAcquisitionParameters.getNominalTotalCollimationWidthInMM());
		assertEquals("Checking PitchFactor equality",pitchFactor,ctAcquisitionParameters.getPitchFactor());
		assertEquals("Checking KVP equality",kvp,ctAcquisitionParameters.getKVP());
		assertEquals("Checking TubeCurrent equality",tubeCurrent,ctAcquisitionParameters.getTubeCurrent());
		assertEquals("Checking TubeCurrentMaximum equality",tubeCurrentMaximum,ctAcquisitionParameters.getTubeCurrentMaximum());
		assertEquals("Checking ExposureTimePerRotation equality",exposureTimePerRotation,ctAcquisitionParameters.getExposureTimePerRotation());
		
		assertEquals("Checking toString",expectToString,ctAcquisitionParameters.toString());
		assertEquals("Checking toString pretty",expectToStringPretty,ctAcquisitionParameters.toString(true));
		assertEquals("Checking getHTML header",expectGetHTMLHeader,ctAcquisitionParameters.getHTMLTableHeaderRowFragment());
		assertEquals("Checking getHTML",expectGetHTML,ctAcquisitionParameters.getHTMLTableRowFragment());
		
		// check round trip via SR ...
		
		ContentItem srFragment = ctAcquisitionParameters.getStructuredReportFragment(null);
		CTAcquisitionParameters ctAcquisitionParameters2 = new CTAcquisitionParameters(srFragment);

		assertEquals("Checking round trip ExposureTimeInSeconds equality",exposureTimeInSeconds,ctAcquisitionParameters2.getExposureTimeInSeconds());
		assertEquals("Checking round trip ScanningLengthInMM equality",scanningLengthInMM,ctAcquisitionParameters2.getScanningLengthInMM());
		assertEquals("Checking round trip ExposedRangeInMM equality",exposedRangeInMM,ctAcquisitionParameters2.getExposedRangeInMM());
		assertEquals("Checking round trip TopZLocationOfReconstructableVolume equality",topZLocationOfReconstructableVolume,ctAcquisitionParameters2.getTopZLocationOfReconstructableVolume());
		assertEquals("Checking round trip BottomZLocationOfReconstructableVolume equality",bottomZLocationOfReconstructableVolume,ctAcquisitionParameters2.getBottomZLocationOfReconstructableVolume());
		assertEquals("Checking round trip TopZLocationOfScanningLength equality",topZLocationOfScanningLength,ctAcquisitionParameters2.getTopZLocationOfScanningLength());
		assertEquals("Checking round trip BottomZLocationOfScanningLength equality",bottomZLocationOfScanningLength,ctAcquisitionParameters2.getBottomZLocationOfScanningLength());
		assertEquals("Checking round trip FrameOfReferenceUID equality",frameOfReferenceUID,ctAcquisitionParameters2.getFrameOfReferenceUID());
		assertEquals("Checking round trip NominalSingleCollimationWidthInMM equality",nominalSingleCollimationWidthInMM,ctAcquisitionParameters2.getNominalSingleCollimationWidthInMM());
		assertEquals("Checking round trip NominalTotalCollimationWidthInMM equality",nominalTotalCollimationWidthInMM,ctAcquisitionParameters2.getNominalTotalCollimationWidthInMM());
		assertEquals("Checking round trip PitchFactor equality",pitchFactor,ctAcquisitionParameters2.getPitchFactor());
		// do NOT expect ScanType or AcquisitionProtocol or Comment round trip equality, since though these are extracted from SR by constructor, they are inserted by CTDoseAcquisition, not CTAcquisitionParameters
		//assertEquals("Checking round trip ScanType equality",scanType,ctAcquisitionParameters2.getScanType());
		//assertEquals("Checking round trip AcquisitionProtocol equality",acquisitionProtocol,ctAcquisitionParameters2.getAcquisitionProtocol());
		//assertEquals("Checking round trip Comment equality",comment,ctAcquisitionParameters2.getComment());
	}
	
	public void TestCTAcquisitionParametersConstructor_Equality() throws Exception {
		
		String irradiationEventUID1 = "1.2.3.4";
		String irradiationEventUID2 = "1.2.3.5";
		String exposureTimeInSeconds = "1";
		String scanningLengthInMM = "750";
		String nominalSingleCollimationWidthInMM = "0.625";
		String nominalTotalCollimationWidthInMM = "40";
		String pitchFactor = "0.984";
		String kvp = "120";
		String tubeCurrent = "397";
		String tubeCurrentMaximum = "433";
		String exposureTimePerRotation = "0.6";
		String anatomyCodeMeaning = "Chest, Abdomen and Pelvis";
		CodedSequenceItem anatomy = new CodedSequenceItem("R-FAB56","SRT",anatomyCodeMeaning);
		CTScanType scanType = CTScanType.HELICAL;
		String acquisitionProtocol = "5.90 CHEST+ABDOMEN+PELVIS";
		String comment = "Non-contrast thin";
		
		CTAcquisitionParameters ctAcquisitionParameters1 = new CTAcquisitionParameters(irradiationEventUID1,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		CTAcquisitionParameters ctAcquisitionParameters2 = new CTAcquisitionParameters(irradiationEventUID1,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		CTAcquisitionParameters ctAcquisitionParameters3 = new CTAcquisitionParameters(irradiationEventUID2,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		
		assertTrue("Checking CTAcquisitionParameters equality with object value",                                     ctAcquisitionParameters1.equals                            (ctAcquisitionParameters2));
		assertTrue("Checking CTAcquisitionParameters equality with compared to value",                                ctAcquisitionParameters2.equals                            (ctAcquisitionParameters1));
		assertTrue("Checking CTAcquisitionParameters equality with object value apart from event UID",                ctAcquisitionParameters1.equalsApartFromIrradiationEventUID(ctAcquisitionParameters2));
		assertTrue("Checking CTAcquisitionParameters equality with compared to value apart from event UID",           ctAcquisitionParameters2.equalsApartFromIrradiationEventUID(ctAcquisitionParameters1));
		assertTrue("Checking CTAcquisitionParameters inequality with object value with different event UID",         !ctAcquisitionParameters1.equals                            (ctAcquisitionParameters3));
		assertTrue("Checking CTAcquisitionParameters inequality with compared to value with different event UID",    !ctAcquisitionParameters3.equals                            (ctAcquisitionParameters1));
		assertTrue("Checking CTAcquisitionParameters equality with object value apart from different event UID",      ctAcquisitionParameters1.equalsApartFromIrradiationEventUID(ctAcquisitionParameters3));
		assertTrue("Checking CTAcquisitionParameters equality with compared to value apart from different event UID", ctAcquisitionParameters3.equalsApartFromIrradiationEventUID(ctAcquisitionParameters1));
	}
	
	public void TestCTAcquisitionParametersConstructor_EqualityWithSomeNullValues() throws Exception {
		
		String irradiationEventUID1 = "1.2.3.4";
		String irradiationEventUID2 = "1.2.3.5";
		String exposureTimeInSeconds = "1";
		String scanningLengthInMM = "750";
		String nominalSingleCollimationWidthInMM = "0.625";
		String nominalTotalCollimationWidthInMM = "40";
		String pitchFactor = "0.984";
		String kvp = "120";
		String tubeCurrent = "397";
		String tubeCurrentMaximum = "433";
		String exposureTimePerRotation = "0.6";
		String anatomyCodeMeaning = "Chest, Abdomen and Pelvis";
		CodedSequenceItem anatomy = new CodedSequenceItem("R-FAB56","SRT",anatomyCodeMeaning);
		CTScanType scanType = CTScanType.HELICAL;
		String acquisitionProtocol = "5.90 CHEST+ABDOMEN+PELVIS";
		String comment = "Non-contrast thin";
		
		CTAcquisitionParameters ctAcquisitionParameters1 = new CTAcquisitionParameters(irradiationEventUID1,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,null/*exposureTimePerRotation*/);
		CTAcquisitionParameters ctAcquisitionParameters2 = new CTAcquisitionParameters(irradiationEventUID1,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,null/*exposureTimePerRotation*/);
		CTAcquisitionParameters ctAcquisitionParameters3 = new CTAcquisitionParameters(irradiationEventUID2,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,null/*exposureTimePerRotation*/);
		
		assertTrue("Checking CTAcquisitionParameters equality with object value",                                     ctAcquisitionParameters1.equals                            (ctAcquisitionParameters2));
		assertTrue("Checking CTAcquisitionParameters equality with compared to value",                                ctAcquisitionParameters2.equals                            (ctAcquisitionParameters1));
		assertTrue("Checking CTAcquisitionParameters equality with object value apart from event UID",                ctAcquisitionParameters1.equalsApartFromIrradiationEventUID(ctAcquisitionParameters2));
		assertTrue("Checking CTAcquisitionParameters equality with compared to value apart from event UID",           ctAcquisitionParameters2.equalsApartFromIrradiationEventUID(ctAcquisitionParameters1));
		assertTrue("Checking CTAcquisitionParameters inequality with object value with different event UID",         !ctAcquisitionParameters1.equals                            (ctAcquisitionParameters3));
		assertTrue("Checking CTAcquisitionParameters inequality with compared to value with different event UID",    !ctAcquisitionParameters3.equals                            (ctAcquisitionParameters1));
		assertTrue("Checking CTAcquisitionParameters equality with object value apart from different event UID",      ctAcquisitionParameters1.equalsApartFromIrradiationEventUID(ctAcquisitionParameters3));
		assertTrue("Checking CTAcquisitionParameters equality with compared to value apart from different event UID", ctAcquisitionParameters3.equalsApartFromIrradiationEventUID(ctAcquisitionParameters1));
		
		CTAcquisitionParameters ctAcquisitionParameters4 = new CTAcquisitionParameters(irradiationEventUID1,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		assertTrue("Checking CTAcquisitionParameters inequality with object value null and the other not",!ctAcquisitionParameters1.equals(ctAcquisitionParameters4));
		assertTrue("Checking CTAcquisitionParameters inequality with compared to value null and the other not",!ctAcquisitionParameters4.equals(ctAcquisitionParameters1));

		CTAcquisitionParameters ctAcquisitionParameters5 = new CTAcquisitionParameters(irradiationEventUID2,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		assertTrue("Checking CTAcquisitionParameters inequality with object value null and the other not",                                    !ctAcquisitionParameters1.equals                            (ctAcquisitionParameters5));
		assertTrue("Checking CTAcquisitionParameters inequality with compared to value null and the other not",                               !ctAcquisitionParameters5.equals                            (ctAcquisitionParameters1));
		assertTrue("Checking CTAcquisitionParameters inequality apart from different event UID with object value null and the other not",     !ctAcquisitionParameters1.equalsApartFromIrradiationEventUID(ctAcquisitionParameters5));
		assertTrue("Checking CTAcquisitionParameters inequality apart from different event UID with compared to value null and the other not",!ctAcquisitionParameters5.equalsApartFromIrradiationEventUID(ctAcquisitionParameters1));
	}
	
	public void TestCTAcquisitionParametersConstructor_Equality_WithTID10014ScanningLength() throws Exception {
		
		String irradiationEventUID1 = "1.2.3.4";
		String irradiationEventUID2 = "1.2.3.5";
		String exposureTimeInSeconds = "1";
		String scanningLengthInMM = "750";
		String lengthOfReconstructableVolumeInMM = "650";
		String exposedRangeInMM = "760";
		String topZLocationOfReconstructableVolume= "-50";
		String bottomZLocationOfReconstructableVolume = "-700";
		String topZLocationOfScanningLength = "0";
		String bottomZLocationOfScanningLength = "-750";
		String frameOfReferenceUID = "1.2.3.6";
		String nominalSingleCollimationWidthInMM = "0.625";
		String nominalTotalCollimationWidthInMM = "40";
		String pitchFactor = "0.984";
		String kvp = "120";
		String tubeCurrent = "397";
		String tubeCurrentMaximum = "433";
		String exposureTimePerRotation = "0.6";
		String anatomyCodeMeaning = "Chest, Abdomen and Pelvis";
		CodedSequenceItem anatomy = new CodedSequenceItem("R-FAB56","SRT",anatomyCodeMeaning);
		CTScanType scanType = CTScanType.HELICAL;
		String acquisitionProtocol = "5.90 CHEST+ABDOMEN+PELVIS";
		String comment = "Non-contrast thin";

		CTAcquisitionParameters ctAcquisitionParameters1 = new CTAcquisitionParameters(irradiationEventUID1,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,lengthOfReconstructableVolumeInMM,exposedRangeInMM,topZLocationOfReconstructableVolume,bottomZLocationOfReconstructableVolume,topZLocationOfScanningLength,bottomZLocationOfScanningLength,frameOfReferenceUID,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		CTAcquisitionParameters ctAcquisitionParameters2 = new CTAcquisitionParameters(irradiationEventUID1,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,lengthOfReconstructableVolumeInMM,exposedRangeInMM,topZLocationOfReconstructableVolume,bottomZLocationOfReconstructableVolume,topZLocationOfScanningLength,bottomZLocationOfScanningLength,frameOfReferenceUID,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		CTAcquisitionParameters ctAcquisitionParameters3 = new CTAcquisitionParameters(irradiationEventUID2,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,lengthOfReconstructableVolumeInMM,exposedRangeInMM,topZLocationOfReconstructableVolume,bottomZLocationOfReconstructableVolume,topZLocationOfScanningLength,bottomZLocationOfScanningLength,frameOfReferenceUID,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		
		assertTrue("Checking CTAcquisitionParameters equality",ctAcquisitionParameters1.equals(ctAcquisitionParameters2));
		assertTrue("Checking CTAcquisitionParameters equality apart from event UID",ctAcquisitionParameters1.equalsApartFromIrradiationEventUID(ctAcquisitionParameters2));
		assertTrue("Checking CTAcquisitionParameters inequality with different event UID",!ctAcquisitionParameters1.equals(ctAcquisitionParameters3));
		assertTrue("Checking CTAcquisitionParameters equality apart from different event UID",ctAcquisitionParameters1.equalsApartFromIrradiationEventUID(ctAcquisitionParameters3));
	}
	
	public void TestCTAcquisitionParametersConstructor_WithAllParameters_CloneConstructor() throws Exception {
		
		String irradiationEventUID = "1.2.3.4";
		String exposureTimeInSeconds = "1";
		String scanningLengthInMM = "750";
		String lengthOfReconstructableVolumeInMM = "650";
		String exposedRangeInMM = "760";
		String topZLocationOfReconstructableVolume= "-50";
		String topZLocationOfReconstructableVolumeExpected= "I50";
		String bottomZLocationOfReconstructableVolume = "-700";
		String bottomZLocationOfReconstructableVolumeExpected = "I700";
		String topZLocationOfScanningLength = "0";
		String topZLocationOfScanningLengthExpected = "S0";
		String bottomZLocationOfScanningLength = "-750";
		String bottomZLocationOfScanningLengthExpected = "I750";
		String frameOfReferenceUID = "1.2.3.6";
		String nominalSingleCollimationWidthInMM = "0.625";
		String nominalTotalCollimationWidthInMM = "40";
		String pitchFactor = "0.984";
		String kvp = "120";
		String tubeCurrent = "397";
		String tubeCurrentMaximum = "433";
		String exposureTimePerRotation = "0.6";
		String anatomyCodeMeaning = "Chest, Abdomen and Pelvis";
		CodedSequenceItem anatomy = new CodedSequenceItem("R-FAB56","SRT",anatomyCodeMeaning);
		CTScanType scanType = CTScanType.HELICAL;
		String acquisitionProtocol = "5.90 CHEST+ABDOMEN+PELVIS";
		String comment = "Non-contrast thin";
		
		CTAcquisitionParameters ctAcquisitionParameters1 = new CTAcquisitionParameters(irradiationEventUID,scanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,lengthOfReconstructableVolumeInMM,exposedRangeInMM,topZLocationOfReconstructableVolume,bottomZLocationOfReconstructableVolume,topZLocationOfScanningLength,bottomZLocationOfScanningLength,frameOfReferenceUID,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		
		assertEquals("Checking IrradiationEventUID equality",irradiationEventUID,ctAcquisitionParameters1.getIrradiationEventUID());
		assertEquals("Checking ScanType equality",scanType,ctAcquisitionParameters1.getScanType());
		assertEquals("Checking Anatomy equality",anatomy,ctAcquisitionParameters1.getAnatomy());
		assertEquals("Checking AcquisitionProtocol equality",acquisitionProtocol,ctAcquisitionParameters1.getAcquisitionProtocol());
		assertEquals("Checking ExposureTimeInSeconds equality",exposureTimeInSeconds,ctAcquisitionParameters1.getExposureTimeInSeconds());
		assertEquals("Checking ScanningLengthInMM equality",scanningLengthInMM,ctAcquisitionParameters1.getScanningLengthInMM());
		assertEquals("Checking LengthOfReconstructableVolumeInMM equality",lengthOfReconstructableVolumeInMM,ctAcquisitionParameters1.getLengthOfReconstructableVolumeInMM());
		assertEquals("Checking ExposedRangeInMM equality",exposedRangeInMM,ctAcquisitionParameters1.getExposedRangeInMM());
		assertEquals("Checking TopZLocationOfReconstructableVolume equality",topZLocationOfReconstructableVolume,ctAcquisitionParameters1.getTopZLocationOfReconstructableVolume());
		assertEquals("Checking BottomZLocationOfReconstructableVolume equality",bottomZLocationOfReconstructableVolume,ctAcquisitionParameters1.getBottomZLocationOfReconstructableVolume());
		assertEquals("Checking TopZLocationOfScanningLength equality",topZLocationOfScanningLength,ctAcquisitionParameters1.getTopZLocationOfScanningLength());
		assertEquals("Checking BottomZLocationOfScanningLength equality",bottomZLocationOfScanningLength,ctAcquisitionParameters1.getBottomZLocationOfScanningLength());
		assertEquals("Checking FrameOfReferenceUID equality",frameOfReferenceUID,ctAcquisitionParameters1.getFrameOfReferenceUID());
		assertEquals("Checking NominalSingleCollimationWidthInMM equality",nominalSingleCollimationWidthInMM,ctAcquisitionParameters1.getNominalSingleCollimationWidthInMM());
		assertEquals("Checking NominalTotalCollimationWidthInMM equality",nominalTotalCollimationWidthInMM,ctAcquisitionParameters1.getNominalTotalCollimationWidthInMM());
		assertEquals("Checking PitchFactor equality",pitchFactor,ctAcquisitionParameters1.getPitchFactor());
		assertEquals("Checking KVP equality",kvp,ctAcquisitionParameters1.getKVP());
		assertEquals("Checking TubeCurrent equality",tubeCurrent,ctAcquisitionParameters1.getTubeCurrent());
		assertEquals("Checking TubeCurrentMaximum equality",tubeCurrentMaximum,ctAcquisitionParameters1.getTubeCurrentMaximum());
		assertEquals("Checking ExposureTimePerRotation equality",exposureTimePerRotation,ctAcquisitionParameters1.getExposureTimePerRotation());
		assertEquals("Checking Comment equality",comment,ctAcquisitionParameters1.getComment());
		
		// check after cloning ...
		
		CTAcquisitionParameters ctAcquisitionParameters2 = new CTAcquisitionParameters(ctAcquisitionParameters1);
		
		assertEquals("Checking equality",ctAcquisitionParameters1,ctAcquisitionParameters2);
		
		assertEquals("Checking IrradiationEventUID equality",irradiationEventUID,ctAcquisitionParameters2.getIrradiationEventUID());
		assertEquals("Checking ScanType equality",scanType,ctAcquisitionParameters2.getScanType());
		assertEquals("Checking Anatomy equality",anatomy,ctAcquisitionParameters2.getAnatomy());
		assertEquals("Checking AcquisitionProtocol equality",acquisitionProtocol,ctAcquisitionParameters2.getAcquisitionProtocol());
		assertEquals("Checking ExposureTimeInSeconds equality",exposureTimeInSeconds,ctAcquisitionParameters2.getExposureTimeInSeconds());
		assertEquals("Checking ScanningLengthInMM equality",scanningLengthInMM,ctAcquisitionParameters2.getScanningLengthInMM());
		assertEquals("Checking LengthOfReconstructableVolumeInMM equality",lengthOfReconstructableVolumeInMM,ctAcquisitionParameters2.getLengthOfReconstructableVolumeInMM());
		assertEquals("Checking ExposedRangeInMM equality",exposedRangeInMM,ctAcquisitionParameters2.getExposedRangeInMM());
		assertEquals("Checking TopZLocationOfReconstructableVolume equality",topZLocationOfReconstructableVolume,ctAcquisitionParameters2.getTopZLocationOfReconstructableVolume());
		assertEquals("Checking BottomZLocationOfReconstructableVolume equality",bottomZLocationOfReconstructableVolume,ctAcquisitionParameters2.getBottomZLocationOfReconstructableVolume());
		assertEquals("Checking TopZLocationOfScanningLength equality",topZLocationOfScanningLength,ctAcquisitionParameters2.getTopZLocationOfScanningLength());
		assertEquals("Checking BottomZLocationOfScanningLength equality",bottomZLocationOfScanningLength,ctAcquisitionParameters2.getBottomZLocationOfScanningLength());
		assertEquals("Checking FrameOfReferenceUID equality",frameOfReferenceUID,ctAcquisitionParameters2.getFrameOfReferenceUID());
		assertEquals("Checking NominalSingleCollimationWidthInMM equality",nominalSingleCollimationWidthInMM,ctAcquisitionParameters2.getNominalSingleCollimationWidthInMM());
		assertEquals("Checking NominalTotalCollimationWidthInMM equality",nominalTotalCollimationWidthInMM,ctAcquisitionParameters2.getNominalTotalCollimationWidthInMM());
		assertEquals("Checking PitchFactor equality",pitchFactor,ctAcquisitionParameters2.getPitchFactor());
		assertEquals("Checking KVP equality",kvp,ctAcquisitionParameters2.getKVP());
		assertEquals("Checking TubeCurrent equality",tubeCurrent,ctAcquisitionParameters2.getTubeCurrent());
		assertEquals("Checking TubeCurrentMaximum equality",tubeCurrentMaximum,ctAcquisitionParameters2.getTubeCurrentMaximum());
		assertEquals("Checking ExposureTimePerRotation equality",exposureTimePerRotation,ctAcquisitionParameters2.getExposureTimePerRotation());
		assertEquals("Checking Comment equality",comment,ctAcquisitionParameters2.getComment());
		
	}
	
	public void TestCTAcquisitionParametersConstructor_WithAllParameters_Merge() throws Exception {
		
		String irradiationEventUID1 = "1.2.3.4";
		String exposureTimeInSeconds1 = "1";
		String scanningLengthInMM1 = "750";
		String lengthOfReconstructableVolumeInMM1 = "650";
		String exposedRangeInMM1 = "760";
		String topZLocationOfReconstructableVolume1 = "-50";
		String topZLocationOfReconstructableVolumeExpected1 = "I50";
		String bottomZLocationOfReconstructableVolume1 = "-700";
		String bottomZLocationOfReconstructableVolumeExpected1 = "I700";
		String topZLocationOfScanningLength1 = "0";
		String topZLocationOfScanningLengthExpected1 = "S0";
		String bottomZLocationOfScanningLength1 = "-750";
		String bottomZLocationOfScanningLengthExpected1 = "I750";
		String frameOfReferenceUID1 = "1.2.3.6";
		String nominalSingleCollimationWidthInMM1 = "0.625";
		String nominalTotalCollimationWidthInMM1 = "40";
		String pitchFactor1 = "0.984";
		String kvp1 = "120";
		String tubeCurrent1 = "397";
		String tubeCurrentMaximum1 = "433";
		String exposureTimePerRotation1 = "0.6";
		String anatomyCodeMeaning1 = "Chest, Abdomen and Pelvis";
		CodedSequenceItem anatomy1 = new CodedSequenceItem("R-FAB56","SRT",anatomyCodeMeaning1);
		CTScanType scanType1 = CTScanType.HELICAL;
		String acquisitionProtocol1 = "5.90 CHEST+ABDOMEN+PELVIS";
		String comment1 = "Non-contrast thin";
		
		CTAcquisitionParameters ctAcquisitionParameters1 = new CTAcquisitionParameters(irradiationEventUID1,scanType1,anatomy1,acquisitionProtocol1,comment1,exposureTimeInSeconds1,scanningLengthInMM1,lengthOfReconstructableVolumeInMM1,exposedRangeInMM1,topZLocationOfReconstructableVolume1,bottomZLocationOfReconstructableVolume1,topZLocationOfScanningLength1,bottomZLocationOfScanningLength1,frameOfReferenceUID1,nominalSingleCollimationWidthInMM1,nominalTotalCollimationWidthInMM1,pitchFactor1,kvp1,tubeCurrent1,tubeCurrentMaximum1,exposureTimePerRotation1);

		
		String irradiationEventUID2 = "1.2.3.4.1";
		String exposureTimeInSeconds2 = "2";
		String scanningLengthInMM2 = "790";
		String lengthOfReconstructableVolumeInMM2 = "645";
		String exposedRangeInMM2 = "760";
		String topZLocationOfReconstructableVolume2 = "-60";
		String topZLocationOfReconstructableVolumeExpected2 = "I60";
		String bottomZLocationOfReconstructableVolume2 = "-705";
		String bottomZLocationOfReconstructableVolumeExpected2 = "I705";
		String topZLocationOfScanningLength2 = "10";
		String topZLocationOfScanningLengthExpected2 = "S10";
		String bottomZLocationOfScanningLength2 = "-780";
		String bottomZLocationOfScanningLengthExpected2 = "I780";
		String frameOfReferenceUID2 = "1.2.3.7";
		String nominalSingleCollimationWidthInMM2 = "1.00";
		String nominalTotalCollimationWidthInMM2 = "40";
		String pitchFactor2 = "1.00";
		String kvp2 = "140";
		String tubeCurrent2 = "200";
		String tubeCurrentMaximum2 = "275";
		String exposureTimePerRotation2 = "0.8";
		String anatomyCodeMeaning2 = "Chest";
		CodedSequenceItem anatomy2 = new CodedSequenceItem("T-D3000","SRT",anatomyCodeMeaning2);
		CTScanType scanType2 = CTScanType.AXIAL;
		String acquisitionProtocol2 = "5.80 CHEST";
		String comment2 = " With contrast thick";

		CTAcquisitionParameters ctAcquisitionParameters2 = new CTAcquisitionParameters(irradiationEventUID2,scanType2,anatomy2,acquisitionProtocol2,comment2,exposureTimeInSeconds2,scanningLengthInMM2,lengthOfReconstructableVolumeInMM2,exposedRangeInMM2,topZLocationOfReconstructableVolume2,bottomZLocationOfReconstructableVolume2,topZLocationOfScanningLength2,bottomZLocationOfScanningLength2,frameOfReferenceUID2,nominalSingleCollimationWidthInMM2,nominalTotalCollimationWidthInMM2,pitchFactor2,kvp2,tubeCurrent2,tubeCurrentMaximum2,exposureTimePerRotation2);

		ctAcquisitionParameters1.merge(ctAcquisitionParameters2);

		assertEquals("Checking equality after merge",ctAcquisitionParameters1,ctAcquisitionParameters2);
		
		assertEquals("Checking IrradiationEventUID equality",irradiationEventUID2,ctAcquisitionParameters1.getIrradiationEventUID());
		assertEquals("Checking ScanType equality",scanType2,ctAcquisitionParameters1.getScanType());
		assertEquals("Checking Anatomy equality",anatomy2,ctAcquisitionParameters1.getAnatomy());
		assertEquals("Checking AcquisitionProtocol equality",acquisitionProtocol2,ctAcquisitionParameters1.getAcquisitionProtocol());
		assertEquals("Checking ExposureTimeInSeconds equality",exposureTimeInSeconds2,ctAcquisitionParameters1.getExposureTimeInSeconds());
		assertEquals("Checking ScanningLengthInMM equality",scanningLengthInMM2,ctAcquisitionParameters1.getScanningLengthInMM());
		assertEquals("Checking LengthOfReconstructableVolumeInMM equality",lengthOfReconstructableVolumeInMM2,ctAcquisitionParameters1.getLengthOfReconstructableVolumeInMM());
		assertEquals("Checking ExposedRangeInMM equality",exposedRangeInMM2,ctAcquisitionParameters1.getExposedRangeInMM());
		assertEquals("Checking TopZLocationOfReconstructableVolume equality",topZLocationOfReconstructableVolume2,ctAcquisitionParameters1.getTopZLocationOfReconstructableVolume());
		assertEquals("Checking BottomZLocationOfReconstructableVolume equality",bottomZLocationOfReconstructableVolume2,ctAcquisitionParameters1.getBottomZLocationOfReconstructableVolume());
		assertEquals("Checking TopZLocationOfScanningLength equality",topZLocationOfScanningLength2,ctAcquisitionParameters1.getTopZLocationOfScanningLength());
		assertEquals("Checking BottomZLocationOfScanningLength equality",bottomZLocationOfScanningLength2,ctAcquisitionParameters1.getBottomZLocationOfScanningLength());
		assertEquals("Checking FrameOfReferenceUID equality",frameOfReferenceUID2,ctAcquisitionParameters1.getFrameOfReferenceUID());
		assertEquals("Checking NominalSingleCollimationWidthInMM equality",nominalSingleCollimationWidthInMM2,ctAcquisitionParameters1.getNominalSingleCollimationWidthInMM());
		assertEquals("Checking NominalTotalCollimationWidthInMM equality",nominalTotalCollimationWidthInMM2,ctAcquisitionParameters1.getNominalTotalCollimationWidthInMM());
		assertEquals("Checking PitchFactor equality",pitchFactor2,ctAcquisitionParameters1.getPitchFactor());
		assertEquals("Checking KVP equality",kvp2,ctAcquisitionParameters1.getKVP());
		assertEquals("Checking TubeCurrent equality",tubeCurrent2,ctAcquisitionParameters1.getTubeCurrent());
		assertEquals("Checking TubeCurrentMaximum equality",tubeCurrentMaximum2,ctAcquisitionParameters1.getTubeCurrentMaximum());
		assertEquals("Checking ExposureTimePerRotation equality",exposureTimePerRotation2,ctAcquisitionParameters1.getExposureTimePerRotation());
		assertEquals("Checking Comment equality",comment2,ctAcquisitionParameters1.getComment());
		
	}
	
}
