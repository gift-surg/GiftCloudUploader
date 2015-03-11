/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.ContentItem;
import com.pixelmed.dose.*;

import junit.framework.*;

import java.util.Locale;

public class TestCTDoseAcquisition extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCTDoseAcquisition(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTDoseAcquisition.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCTDoseAcquisition");
		
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_Helical"));
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_Axial"));
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_AxialOnePosition"));
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_WithAcquisitionParameterChildren"));
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_StationaryBasedOnZeroPitchInAcquisitionParameterChildren"));
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_Equality"));
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_MatchForMerge"));
		suite.addTest(new TestCTDoseAcquisition("TestCTDoseAcquisitionConstructor_WithAllParameters_WithEntireAcquisitionParameterMerge"));

		return suite;
	}
	
	protected void setUp() {
		Locale.setDefault(Locale.FRENCH);	// forces check that "," is not being used as decimal point in any double to string conversions
	}
	
	protected void tearDown() {
	}
	
	public void TestCTDoseAcquisitionConstructor_WithAllParameters_Helical() throws Exception {
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"1",CTScanType.HELICAL,new ScanRange("I","12.750","I","602.750"),"15.51","948.89",CTPhantomType.selectFromDescription("BODY32"));
		
		String expectToString = "\tSeries=1\tHelical\tRange=I12.750-I602.750 mm\tCTDIvol=15.51 mGy\tDLP=948.89 mGy.cm\tPhantom=BODY32\n";
		String expectToStringPretty = "\tSeries=1\tHelical\tI12.750-I602.750 mm\t15.51 mGy\t948.89 mGy.cm\tBODY32\n";
		
		String expectGetHTMLHeader =
			  "<tr><th>Number</th><th>Type</th><th>Range mm</th><th>CTDIvol mGy</th><th>DLP mGy.cm</th><th>Phantom</th></tr>\n"
			;
		
		String expectGetHTML =
			  "<tr><td>Series=1</td><td>Helical</td><td>I12.750-I602.750</td><td>15.51</td><td>948.89</td><td>BODY32</td></tr>\n"
			;
		
		assertEquals("Checking SeriesNumber equality","1",ctDoseAcquisition.getSeriesOrAcquisitionNumber());
		assertEquals("Checking CTScanType equality",CTScanType.HELICAL,ctDoseAcquisition.getScanType());
		assertEquals("Checking CTScanType string equality","Helical",ctDoseAcquisition.getScanType().toString());
		assertTrue("Checking ScanRange equality",new ScanRange("I","12.750","I","602.750").equals(ctDoseAcquisition.getScanRange()));
		assertEquals("Checking CTDIvol equality","15.51",ctDoseAcquisition.getCTDIvol());
		assertEquals("Checking DLP equality","948.89",ctDoseAcquisition.getDLP());
		assertTrue("Checking CTPhantomType equality",CTPhantomType.BODY32.equals(ctDoseAcquisition.getPhantomType()));
		
		assertFalse("Checking specified DLP does not equal DLP computed from range and CTDIvol due to overscan",ctDoseAcquisition.specifiedDLPMatchesDLPFromRangeAndCTDIvol());
		
		assertEquals("Checking toString",expectToString,ctDoseAcquisition.toString());
		assertEquals("Checking toString pretty",expectToStringPretty,ctDoseAcquisition.toString(true));
		assertEquals("Checking getHTML header",expectGetHTMLHeader,ctDoseAcquisition.getHTMLTableHeaderRow());
		assertEquals("Checking getHTML",expectGetHTML,ctDoseAcquisition.getHTMLTableRow());
		
		// check round trip via SR ...
		
		ContentItem srFragment = ctDoseAcquisition.getStructuredReportFragment(null);
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,srFragment);

		assertEquals("Checking round trip CTScanType equality",CTScanType.HELICAL,ctDoseAcquisition2.getScanType());
		assertEquals("Checking round trip CTDIvol equality","15.51",ctDoseAcquisition2.getCTDIvol());
		assertEquals("Checking round trip DLP equality","948.89",ctDoseAcquisition2.getDLP());
		assertTrue("Checking round trip CTPhantomType equality",CTPhantomType.BODY32.equals(ctDoseAcquisition2.getPhantomType()));
	}
	
	public void TestCTDoseAcquisitionConstructor_WithAllParameters_Axial() throws Exception {
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"2",CTScanType.AXIAL,new ScanRange("I","24.500","S","33.000"),"234.67","1408.00",CTPhantomType.selectFromDescription("HEAD16"));
		
		String expectToString = "\tSeries=2\tAxial\tRange=I24.500-S33.000 mm\tCTDIvol=234.67 mGy\tDLP=1408.00 mGy.cm\tPhantom=HEAD16\n";
		String expectToStringPretty = "\tSeries=2\tAxial\tI24.500-S33.000 mm\t234.67 mGy\t1408.00 mGy.cm\tHEAD16\n";
		
		assertEquals("Checking SeriesNumber equality","2",ctDoseAcquisition.getSeriesOrAcquisitionNumber());
		assertEquals("Checking CTScanType equality",CTScanType.AXIAL,ctDoseAcquisition.getScanType());
		assertEquals("Checking CTScanType string equality","Axial",ctDoseAcquisition.getScanType().toString());
		assertTrue("Checking ScanRange equality",new ScanRange("I","24.500","S","33.000").equals(ctDoseAcquisition.getScanRange()));
		assertEquals("Checking CTDIvol equality","234.67",ctDoseAcquisition.getCTDIvol());
		assertEquals("Checking DLP equality","1408.00",ctDoseAcquisition.getDLP());
		assertTrue("Checking CTPhantomType equality",CTPhantomType.HEAD16.equals(ctDoseAcquisition.getPhantomType()));
		
		assertFalse("Checking specified DLP does not equal DLP computed from range and CTDIvol due to slice thickness or spacing",ctDoseAcquisition.specifiedDLPMatchesDLPFromRangeAndCTDIvol());
		
		assertEquals("Checking toString",expectToString,ctDoseAcquisition.toString());
		assertEquals("Checking toString pretty",expectToStringPretty,ctDoseAcquisition.toString(true));
		
		// check round trip via SR ...
		
		ContentItem srFragment = ctDoseAcquisition.getStructuredReportFragment(null);
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,srFragment);

		assertEquals("Checking round trip CTScanType equality",CTScanType.AXIAL,ctDoseAcquisition2.getScanType());
		assertEquals("Checking round trip CTDIvol equality","234.67",ctDoseAcquisition2.getCTDIvol());
		assertEquals("Checking round trip DLP equality","1408.00",ctDoseAcquisition2.getDLP());
		assertTrue("Checking round trip CTPhantomType equality",CTPhantomType.HEAD16.equals(ctDoseAcquisition2.getPhantomType()));
	}
	
	public void TestCTDoseAcquisitionConstructor_WithAllParameters_AxialOnePosition() throws Exception {
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"200",CTScanType.AXIAL,new ScanRange("I","254.500","I","254.500"),"46.29","23.10",CTPhantomType.selectFromDescription("BODY32"));
		
		String expectToString = "\tSeries=200\tAxial\tRange=I254.500-I254.500 mm\tCTDIvol=46.29 mGy\tDLP=23.10 mGy.cm\tPhantom=BODY32\n";
		String expectToStringPretty = "\tSeries=200\tAxial\tI254.500-I254.500 mm\t46.29 mGy\t23.10 mGy.cm\tBODY32\n";
		
		assertEquals("Checking SeriesNumber equality","200",ctDoseAcquisition.getSeriesOrAcquisitionNumber());
		assertEquals("Checking CTScanType equality",CTScanType.AXIAL,ctDoseAcquisition.getScanType());
		assertEquals("Checking CTScanType string equality","Axial",ctDoseAcquisition.getScanType().toString());
		assertTrue("Checking ScanRange equality",new ScanRange("I","254.500","I","254.500").equals(ctDoseAcquisition.getScanRange()));
		assertEquals("Checking CTDIvol equality","46.29",ctDoseAcquisition.getCTDIvol());
		assertEquals("Checking DLP equality","23.10",ctDoseAcquisition.getDLP());
		assertTrue("Checking CTPhantomType equality",CTPhantomType.BODY32.equals(ctDoseAcquisition.getPhantomType()));
		
		assertFalse("Checking specified DLP does not equal DLP computed from range and CTDIvol due to slice thickness",ctDoseAcquisition.specifiedDLPMatchesDLPFromRangeAndCTDIvol());
		
		assertEquals("Checking toString",expectToString,ctDoseAcquisition.toString());
		assertEquals("Checking toString pretty",expectToStringPretty,ctDoseAcquisition.toString(true));
		
		// check round trip via SR ...
		
		ContentItem srFragment = ctDoseAcquisition.getStructuredReportFragment(null);
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,srFragment);

		assertEquals("Checking round trip CTScanType equality",CTScanType.AXIAL,ctDoseAcquisition2.getScanType());
		assertEquals("Checking round trip CTDIvol equality","46.29",ctDoseAcquisition2.getCTDIvol());
		assertEquals("Checking round trip DLP equality","23.10",ctDoseAcquisition2.getDLP());
		assertTrue("Checking round trip CTPhantomType equality",CTPhantomType.BODY32.equals(ctDoseAcquisition2.getPhantomType()));
	}
	
	public void TestCTDoseAcquisitionConstructor_WithAllParameters_WithAcquisitionParameterChildren() throws Exception {

		String seriesNumber = "2";
		CTScanType ctScanType = CTScanType.HELICAL;
		ScanRange scanRange = new ScanRange("S", "14.250","I","635.750");
		String ctdiVol = "20.23";
		String dlp =  "1362.24";
		CTPhantomType ctPhantomType = CTPhantomType.selectFromDescription("BODY32");
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition = new CTDoseAcquisition(scopeUID,true/*isSeries*/,seriesNumber,ctScanType,scanRange,ctdiVol,dlp,ctPhantomType);
		
		String irradiationEventUID = "1.2.3.4";
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

		String scanningLengthInMM = "673.38";	// 1362.24/20.23*10

		String expectGetHTMLHeader =
			  "<tr><th>Number</th><th>Type</th><th>Range mm</th><th>CTDIvol mGy</th><th>DLP mGy.cm</th><th>Phantom</th><th>Type</th><th>Anatomy</th><th>Protocol</th><th>Scanning Length mm</th><th>Reconstructable Volume mm</th><th>Exposed Range mm</th><th>Collimation Single/Total mm</th><th>Pitch Factor</th><th>kVP</th><th>Tube Current Mean/Max mA</th><th>Exposure Time/Per Rotation s</th><th>Comment</th></tr>\n"
			;
		
		String expectGetHTML =
			  "<tr><td>Series=2</td><td>Helical</td><td>S14.250-I635.750</td><td>20.23</td><td>1362.24</td><td>BODY32</td><td>Helical</td><td>Chest, Abdomen and Pelvis</td><td>"+acquisitionProtocol+"</td><td>673.38</td><td></td><td></td><td>0.625/40</td><td>0.984:1</td><td>120</td><td>397/433</td><td>1/0.6</td><td>"+comment+"</td></tr>\n"
			;
		CTAcquisitionParameters ctAcquisitionParameters = new CTAcquisitionParameters(irradiationEventUID,ctScanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,null,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		ctAcquisitionParameters.deriveScanningLengthFromDLPAndCTDIVolIfGreater(dlp,ctdiVol);
		ctDoseAcquisition.setAcquisitionParameters(ctAcquisitionParameters);
		
		assertEquals("Checking getHTML header",expectGetHTMLHeader,ctDoseAcquisition.getHTMLTableHeaderRow());
		assertEquals("Checking getHTML",expectGetHTML,ctDoseAcquisition.getHTMLTableRow());

		assertEquals("Checking SeriesNumber equality",seriesNumber,ctDoseAcquisition.getSeriesOrAcquisitionNumber());
		assertEquals("Checking CTScanType equality",ctScanType,ctDoseAcquisition.getScanType());
		assertTrue("Checking ScanRange equality",scanRange.equals(ctDoseAcquisition.getScanRange()));
		assertEquals("Checking CTDIvol equality",ctdiVol,ctDoseAcquisition.getCTDIvol());
		assertEquals("Checking DLP equality",dlp,ctDoseAcquisition.getDLP());
		assertTrue("Checking CTPhantomType equality",ctPhantomType.equals(ctDoseAcquisition.getPhantomType()));
		
		assertFalse("Checking specified DLP does not equal DLP computed from range and CTDIvol due to slice thickness",ctDoseAcquisition.specifiedDLPMatchesDLPFromRangeAndCTDIvol());

		// check round trip via SR ...
		
		ContentItem srFragment = ctDoseAcquisition.getStructuredReportFragment(null);
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,srFragment);
		CTAcquisitionParameters ctAcquisitionParameters2 = ctDoseAcquisition2.getAcquisitionParameters();

		assertEquals("Checking round trip CTScanType equality",ctScanType,ctDoseAcquisition2.getScanType());
		assertEquals("Checking round trip CTDIvol equality",ctdiVol,ctDoseAcquisition2.getCTDIvol());
		assertEquals("Checking round trip DLP equality",dlp,ctDoseAcquisition2.getDLP());
		assertTrue("Checking round trip CTPhantomType equality",ctPhantomType.equals(ctDoseAcquisition2.getPhantomType()));

		assertEquals("Checking round trip IrradiationEventUID equality",irradiationEventUID,ctAcquisitionParameters2.getIrradiationEventUID());
		assertEquals("Checking round trip Anatomy equality",anatomy,ctAcquisitionParameters2.getAnatomy());

		assertEquals("Checking round trip ExposureTimeInSeconds equality",exposureTimeInSeconds,ctAcquisitionParameters2.getExposureTimeInSeconds());
		assertEquals("Checking round trip ScanningLengthInMM equality",scanningLengthInMM,ctAcquisitionParameters2.getScanningLengthInMM());
		assertEquals("Checking round trip NominalSingleCollimationWidthInMM equality",nominalSingleCollimationWidthInMM,ctAcquisitionParameters2.getNominalSingleCollimationWidthInMM());
		assertEquals("Checking round trip NominalTotalCollimationWidthInMM equality",nominalTotalCollimationWidthInMM,ctAcquisitionParameters2.getNominalTotalCollimationWidthInMM());
		assertEquals("Checking round trip PitchFactor equality",pitchFactor,ctAcquisitionParameters2.getPitchFactor());

		assertEquals("Checking KVP equality",kvp,ctAcquisitionParameters2.getKVP());
		assertEquals("Checking TubeCurrent equality",tubeCurrent,ctAcquisitionParameters2.getTubeCurrent());
		assertEquals("Checking TubeCurrentMaximum equality",tubeCurrentMaximum,ctAcquisitionParameters2.getTubeCurrentMaximum());
		assertEquals("Checking ExposureTimePerRotation equality",exposureTimePerRotation,ctAcquisitionParameters2.getExposureTimePerRotation());
	}
	
	public void TestCTDoseAcquisitionConstructor_WithAllParameters_StationaryBasedOnZeroPitchInAcquisitionParameterChildren() throws Exception {

		String seriesNumber = "2";
		CTScanType ctScanType = CTScanType.STATIONARY;
		//ScanRange scanRange = new ScanRange("S", "14.250","S","14.250");
		String ctdiVol = "20.23";
		String dlp =  "1362.24";
		CTPhantomType ctPhantomType = CTPhantomType.selectFromDescription("BODY32");
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition = new CTDoseAcquisition(scopeUID,true/*isSeries*/,seriesNumber,CTScanType.UNKNOWN,null/*scanRange*/,ctdiVol,dlp,ctPhantomType);
		
		String irradiationEventUID = "1.2.3.4";
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
		String pitchFactor = "0";

		String scanningLengthInMM = "673.38";	// 1362.24/20.23*10

		CTAcquisitionParameters ctAcquisitionParameters = new CTAcquisitionParameters(irradiationEventUID,ctScanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,null,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		ctAcquisitionParameters.deriveScanningLengthFromDLPAndCTDIVolIfGreater(dlp,ctdiVol);
		ctDoseAcquisition.setAcquisitionParameters(ctAcquisitionParameters);		// this should cause the ScanType to change
		
		assertEquals("Checking SeriesNumber equality",seriesNumber,ctDoseAcquisition.getSeriesOrAcquisitionNumber());
		assertEquals("Checking CTScanType equality",ctScanType,ctDoseAcquisition.getScanType());
		//assertTrue("Checking ScanRange equality",scanRange.equals(ctDoseAcquisition.getScanRange()));
		assertEquals("Checking CTDIvol equality",ctdiVol,ctDoseAcquisition.getCTDIvol());
		assertEquals("Checking DLP equality",dlp,ctDoseAcquisition.getDLP());
		assertTrue("Checking CTPhantomType equality",ctPhantomType.equals(ctDoseAcquisition.getPhantomType()));
		
		assertFalse("Checking specified DLP does not equal DLP computed from range and CTDIvol due to slice thickness",ctDoseAcquisition.specifiedDLPMatchesDLPFromRangeAndCTDIvol());

		// check round trip via SR ...
		
		ContentItem srFragment = ctDoseAcquisition.getStructuredReportFragment(null);
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,srFragment);
		CTAcquisitionParameters ctAcquisitionParameters2 = ctDoseAcquisition2.getAcquisitionParameters();

		assertEquals("Checking round trip CTScanType equality",ctScanType,ctDoseAcquisition2.getScanType());
		assertEquals("Checking round trip CTDIvol equality",ctdiVol,ctDoseAcquisition2.getCTDIvol());
		assertEquals("Checking round trip DLP equality",dlp,ctDoseAcquisition2.getDLP());
		assertTrue("Checking round trip CTPhantomType equality",ctPhantomType.equals(ctDoseAcquisition2.getPhantomType()));

		assertEquals("Checking round trip IrradiationEventUID equality",irradiationEventUID,ctAcquisitionParameters2.getIrradiationEventUID());
		assertEquals("Checking round trip Anatomy equality",anatomy,ctAcquisitionParameters2.getAnatomy());

		assertEquals("Checking round trip ExposureTimeInSeconds equality",exposureTimeInSeconds,ctAcquisitionParameters2.getExposureTimeInSeconds());
		assertEquals("Checking round trip ScanningLengthInMM equality",scanningLengthInMM,ctAcquisitionParameters2.getScanningLengthInMM());
		assertEquals("Checking round trip NominalSingleCollimationWidthInMM equality",nominalSingleCollimationWidthInMM,ctAcquisitionParameters2.getNominalSingleCollimationWidthInMM());
		assertEquals("Checking round trip NominalTotalCollimationWidthInMM equality",nominalTotalCollimationWidthInMM,ctAcquisitionParameters2.getNominalTotalCollimationWidthInMM());
		assertTrue  ("Checking round trip PitchFactor is absent",ctAcquisitionParameters2.getPitchFactor() == null);

		assertEquals("Checking KVP equality",kvp,ctAcquisitionParameters2.getKVP());
		assertEquals("Checking TubeCurrent equality",tubeCurrent,ctAcquisitionParameters2.getTubeCurrent());
		assertEquals("Checking TubeCurrentMaximum equality",tubeCurrentMaximum,ctAcquisitionParameters2.getTubeCurrentMaximum());
		assertEquals("Checking ExposureTimePerRotation equality",exposureTimePerRotation,ctAcquisitionParameters2.getExposureTimePerRotation());
	}
	
	public void TestCTDoseAcquisitionConstructor_WithAllParameters_Equality() throws Exception {
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition1 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"1",CTScanType.HELICAL,new ScanRange("I","12.750","I","602.750"),"15.51","948.89",CTPhantomType.selectFromDescription("BODY32"));
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"1",CTScanType.HELICAL,new ScanRange("I","12.750","I","602.750"),"15.51","948.89",CTPhantomType.selectFromDescription("BODY32"));
		
		assertEquals("Checking equality 1 versus 2",ctDoseAcquisition1,ctDoseAcquisition2);
		assertEquals("Checking equality 2 versus 1",ctDoseAcquisition2,ctDoseAcquisition1);
	}
	
	public void TestCTDoseAcquisitionConstructor_WithAllParameters_MatchForMerge() throws Exception {
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition1 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"1",CTScanType.HELICAL,new ScanRange("I","12.750","I","602.750"),"15.51","948.89",CTPhantomType.selectFromDescription("BODY32"));
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,"1",CTScanType.HELICAL,new ScanRange("I","12.750","I","602.750"),"15.51","948.89",CTPhantomType.selectFromDescription("BODY32"));
		
		assertTrue("Checking match 1 versus 2",ctDoseAcquisition1.matchForMerge(ctDoseAcquisition2));
		assertTrue("Checking match 2 versus 1",ctDoseAcquisition2.matchForMerge(ctDoseAcquisition1));
	}

	public void TestCTDoseAcquisitionConstructor_WithAllParameters_WithEntireAcquisitionParameterMerge() throws Exception {

		String seriesNumber = "2";
		CTScanType ctScanType = CTScanType.HELICAL;
		ScanRange scanRange = new ScanRange("S", "14.250","I","635.750");
		String ctdiVol = "20.23";
		String dlp =  "1362.24";
		String largerDLPForScanningLengthOverride =  "9999";
		CTPhantomType ctPhantomType = CTPhantomType.selectFromDescription("BODY32");
		String scopeUID = "1.2.3.4.5";
		CTDoseAcquisition ctDoseAcquisition1 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,seriesNumber,ctScanType,scanRange,ctdiVol,dlp,ctPhantomType);
		CTDoseAcquisition ctDoseAcquisition2 = new CTDoseAcquisition(scopeUID,true/*isSeries*/,seriesNumber,ctScanType,scanRange,ctdiVol,dlp,ctPhantomType);
		
		String irradiationEventUID = "1.2.3.4";
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

		String scanningLengthInMM = "673.38";	// 1362.24/20.23*10
		String scanningLengthInMMFromLargerDLP = "4942.66";	// 9999/20.23*10

		CTAcquisitionParameters ctAcquisitionParameters2 = new CTAcquisitionParameters(irradiationEventUID,ctScanType,anatomy,acquisitionProtocol,comment,exposureTimeInSeconds,scanningLengthInMM,nominalSingleCollimationWidthInMM,nominalTotalCollimationWidthInMM,pitchFactor,kvp,tubeCurrent,tubeCurrentMaximum,exposureTimePerRotation);
		ctDoseAcquisition2.setAcquisitionParameters(ctAcquisitionParameters2);
		
		assertTrue("Checking ctDoseAcquisition1 initially has no CTAcquisitionParameters",ctDoseAcquisition1.getAcquisitionParameters() == null);
		ctDoseAcquisition1.merge(ctDoseAcquisition2);
		assertEquals("Checking equality of CTAcquisitionParameters after merge",ctAcquisitionParameters2,ctDoseAcquisition1.getAcquisitionParameters());
		
		assertEquals("Checking initial ScanningLengthInMM equality in merged CTAcquisitionParameters",scanningLengthInMM,ctDoseAcquisition1.getAcquisitionParameters().getScanningLengthInMM());
		ctDoseAcquisition2.getAcquisitionParameters().deriveScanningLengthFromDLPAndCTDIVolIfGreater(largerDLPForScanningLengthOverride,ctdiVol);	// change in original ... should NOT be propagated to merged value
		assertEquals("Checking ScanningLengthInMM has been changed",scanningLengthInMMFromLargerDLP,ctDoseAcquisition2.getAcquisitionParameters().getScanningLengthInMM());
		assertEquals("Checking cloned ScanningLengthInMM equality in merged CTAcquisitionParameters has not been changed",scanningLengthInMM,ctDoseAcquisition1.getAcquisitionParameters().getScanningLengthInMM());
	}
}
