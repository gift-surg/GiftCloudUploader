/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSUVTransformValues extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSUVTransformValues(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSUVTransformValues.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSUVTransformValues");
		
		suite.addTest(new TestSUVTransformValues("TestSUVTransformValues_BQML"));
		suite.addTest(new TestSUVTransformValues("TestSUVTransformValues_BQMLWithTimezoneOffset"));
		suite.addTest(new TestSUVTransformValues("TestSUVTransformValues_PhilipsSUVScaleFactor"));
		suite.addTest(new TestSUVTransformValues("TestSUVTransformValues_GMLUnitsWithoutSUVType"));
		suite.addTest(new TestSUVTransformValues("TestSUVTransformValues_DeriveScanDateTimeFromHalfLifeAcquisitionDateTimeFrameReferenceTimeAndActualFrameDuration"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSUVTransformValues_BQML() throws Exception {
	
		// test values based on DRO object, with weight amended to make SUVbw == SUVlbm ...

		int storedPixelValue = 7972;
		double expectedSUVbw = 1.0d;
		double expectedSUVbsa = 0.3806d;
		double expectedSUVlbm = 1.0d;
		double expectedSUVibw = 2.7726d;
		double precisionErrorTolerated = 1e-4d;

		AttributeList list = new AttributeList();
		{
			AttributeList risList = new AttributeList();
			
			{ Attribute a = new TimeAttribute(TagFromName.RadiopharmaceuticalStartTime); a.addValue("130100"); risList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RadionuclideTotalDose); a.addValue(370000000d); risList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RadionuclideHalfLife); a.addValue(6586d); risList.put(a); }
			
			SequenceAttribute aRadiopharmaceuticalInformationSequence = new SequenceAttribute(TagFromName.RadiopharmaceuticalInformationSequence);
			SequenceItem risItem = new SequenceItem(risList);
			aRadiopharmaceuticalInformationSequence.addItem(risItem);
			list.put(aRadiopharmaceuticalInformationSequence);
		}

		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue("M"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(2.898275349237887d); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(70d); list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.PETImageStorage); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.CorrectedImage); a.addValue("ATTN"); a.addValue("DECY"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Units); a.addValue("BQML"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.DecayCorrection); a.addValue("START"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue(0); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue(0.453901487278775d); list.put(a); }

		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue("20111031"); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue("20111031"); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue("140100"); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue("140100"); list.put(a); }
		
		SUVTransform t = new SUVTransform(list);
		SUVTransform.SingleSUVTransform ts = t.getSingleSUVTransform(1);
		
		assertTrue("isValidSUVbw",ts.isValidSUVbw());
		assertTrue("getSUVbwValue",Math.abs(ts.getSUVbwValue(storedPixelValue) - expectedSUVbw) < precisionErrorTolerated);

		assertTrue("isValidSUVbsa",ts.isValidSUVbsa());
		assertTrue("getSUVbsaValue",Math.abs(ts.getSUVbsaValue(storedPixelValue) - expectedSUVbsa) < precisionErrorTolerated);

		assertTrue("isValidSUVbw",ts.isValidSUVlbm());
		assertTrue("getSUVlbmValue",Math.abs(ts.getSUVlbmValue(storedPixelValue) - expectedSUVlbm) < precisionErrorTolerated);

		assertTrue("isValidSUVibw",ts.isValidSUVibw());
		assertTrue("getSUVibwValue",Math.abs(ts.getSUVibwValue(storedPixelValue) - expectedSUVibw) < precisionErrorTolerated);
	}
	
	
	public void TestSUVTransformValues_BQMLWithTimezoneOffset() throws Exception {
	
		// test values based on DRO object, with weight amended to make SUVbw == SUVlbm ...

		int storedPixelValue = 7972;
		double expectedSUVbw = 1.0d;
		double expectedSUVbsa = 0.3806d;
		double expectedSUVlbm = 1.0d;
		double expectedSUVibw = 2.7726d;
		double precisionErrorTolerated = 1e-4d;

		AttributeList list = new AttributeList();
		{
			AttributeList risList = new AttributeList();
			
			{ Attribute a = new TimeAttribute(TagFromName.RadiopharmaceuticalStartTime); a.addValue("130100"); risList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RadionuclideTotalDose); a.addValue(370000000d); risList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RadionuclideHalfLife); a.addValue(6586d); risList.put(a); }
			
			SequenceAttribute aRadiopharmaceuticalInformationSequence = new SequenceAttribute(TagFromName.RadiopharmaceuticalInformationSequence);
			SequenceItem risItem = new SequenceItem(risList);
			aRadiopharmaceuticalInformationSequence.addItem(risItem);
			list.put(aRadiopharmaceuticalInformationSequence);
		}

		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue("M"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(2.898275349237887d); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(70d); list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.PETImageStorage); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.CorrectedImage); a.addValue("ATTN"); a.addValue("DECY"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Units); a.addValue("BQML"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.DecayCorrection); a.addValue("START"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue(0); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue(0.453901487278775d); list.put(a); }

		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue("20111031"); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue("20111031"); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue("140100"); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue("140100"); list.put(a); }
		
		{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC); a.addValue("-0700"); list.put(a); }
		
		SUVTransform t = new SUVTransform(list);
		SUVTransform.SingleSUVTransform ts = t.getSingleSUVTransform(1);
		
		assertTrue("isValidSUVbw",ts.isValidSUVbw());
		assertTrue("getSUVbwValue",Math.abs(ts.getSUVbwValue(storedPixelValue) - expectedSUVbw) < precisionErrorTolerated);

		assertTrue("isValidSUVbsa",ts.isValidSUVbsa());
		assertTrue("getSUVbsaValue",Math.abs(ts.getSUVbsaValue(storedPixelValue) - expectedSUVbsa) < precisionErrorTolerated);

		assertTrue("isValidSUVbw",ts.isValidSUVlbm());
		assertTrue("getSUVlbmValue",Math.abs(ts.getSUVlbmValue(storedPixelValue) - expectedSUVlbm) < precisionErrorTolerated);

		assertTrue("isValidSUVibw",ts.isValidSUVibw());
		assertTrue("getSUVibwValue",Math.abs(ts.getSUVibwValue(storedPixelValue) - expectedSUVibw) < precisionErrorTolerated);
	}
	
	public void TestSUVTransformValues_PhilipsSUVScaleFactor() throws Exception {
	
		// test values based on DRO object, with weight amended to make SUVbw == SUVlbm ...

		int storedPixelValue = 7972;
		double expectedSUVbw = 1.0d;
		double expectedSUVbsa = 0.3806d;
		double expectedSUVlbm = 1.0d;
		double expectedSUVibw = 2.7726d;
		double precisionErrorTolerated = 1e-4d;

		AttributeList list = new AttributeList();

		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue("M"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(2.898275349237887d); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(70d); list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.PETImageStorage); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.CorrectedImage); a.addValue("ATTN"); a.addValue("DECY"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Units); a.addValue("CNTS"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue(0); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue(0.453901487278775d); list.put(a); }

		{ Attribute a = new LongStringAttribute(new AttributeTag(0x7053,0x0010)); a.addValue("Philips PET Private Group"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(new AttributeTag(0x7053,0x1000)); a.addValue(2.7634E-4); list.put(a); }
		
		SUVTransform t = new SUVTransform(list);
		SUVTransform.SingleSUVTransform ts = t.getSingleSUVTransform(1);
		
		assertTrue("isValidSUVbw",ts.isValidSUVbw());
		assertTrue("getSUVbwValue",Math.abs(ts.getSUVbwValue(storedPixelValue) - expectedSUVbw) < precisionErrorTolerated);

		assertTrue("isValidSUVbsa",ts.isValidSUVbsa());
		assertTrue("getSUVbsaValue",Math.abs(ts.getSUVbsaValue(storedPixelValue) - expectedSUVbsa) < precisionErrorTolerated);

		assertTrue("isValidSUVbw",ts.isValidSUVlbm());
		assertTrue("getSUVlbmValue",Math.abs(ts.getSUVlbmValue(storedPixelValue) - expectedSUVlbm) < precisionErrorTolerated);

		assertTrue("isValidSUVibw",ts.isValidSUVibw());
		assertTrue("getSUVibwValue",Math.abs(ts.getSUVibwValue(storedPixelValue) - expectedSUVibw) < precisionErrorTolerated);
	}
	
	public void TestSUVTransformValues_GMLUnitsWithoutSUVType() throws Exception {
	
		// test values based on DRO object, with weight amended to make SUVbw == SUVlbm ...

		int storedPixelValue = 7972;
		double expectedSUVbw = 1.0d;
		double expectedSUVbsa = 0.3806d;
		double expectedSUVlbm = 1.0d;
		double expectedSUVibw = 2.7726d;
		double precisionErrorTolerated = 1e-4d;

		AttributeList list = new AttributeList();

		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue("M"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(2.898275349237887d); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(70d); list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.PETImageStorage); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.CorrectedImage); a.addValue("ATTN"); a.addValue("DECY"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Units); a.addValue("GML"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue(0); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue(1.2543E-4d); list.put(a); }

		SUVTransform t = new SUVTransform(list);
		SUVTransform.SingleSUVTransform ts = t.getSingleSUVTransform(1);
		
		assertTrue("isValidSUVbw",ts.isValidSUVbw());
		assertTrue("getSUVbwValue",Math.abs(ts.getSUVbwValue(storedPixelValue) - expectedSUVbw) < precisionErrorTolerated);

		assertTrue("isValidSUVbsa",ts.isValidSUVbsa());
		assertTrue("getSUVbsaValue",Math.abs(ts.getSUVbsaValue(storedPixelValue) - expectedSUVbsa) < precisionErrorTolerated);

		assertTrue("isValidSUVbw",ts.isValidSUVlbm());
		assertTrue("getSUVlbmValue",Math.abs(ts.getSUVlbmValue(storedPixelValue) - expectedSUVlbm) < precisionErrorTolerated);

		assertTrue("isValidSUVibw",ts.isValidSUVibw());
		assertTrue("getSUVibwValue",Math.abs(ts.getSUVibwValue(storedPixelValue) - expectedSUVibw) < precisionErrorTolerated);
	}

	public void TestSUVTransformValues_DeriveScanDateTimeFromHalfLifeAcquisitionDateTimeFrameReferenceTimeAndActualFrameDuration() throws Exception {
		long expectSeriesTime = DateTimeAttribute.getTimeInMilliSecondsSinceEpoch("20120928121310.062000");
		
		AttributeList list = new AttributeList();
		{
			AttributeList risList = new AttributeList();
			
			{ Attribute a = new TimeAttribute(TagFromName.RadiopharmaceuticalStartTime); a.addValue("103600"); risList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RadionuclideTotalDose); a.addValue(338549987.79297); risList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RadionuclideHalfLife); a.addValue(6586.2); risList.put(a); }
			
			SequenceAttribute aRadiopharmaceuticalInformationSequence = new SequenceAttribute(TagFromName.RadiopharmaceuticalInformationSequence);
			SequenceItem risItem = new SequenceItem(risList);
			aRadiopharmaceuticalInformationSequence.addItem(risItem);
			list.put(aRadiopharmaceuticalInformationSequence);
		}

		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue("M"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(2.898275349237887); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(70d); list.put(a); }

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.PETImageStorage); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.CorrectedImage); a.addValue("ATTN"); a.addValue("DECY"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Units); a.addValue("BQML"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.DecayCorrection); a.addValue("START"); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue(0); list.put(a); }
		{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue(5.02139); list.put(a); }

		{ Attribute a = new DateAttribute(TagFromName.SeriesDate);      a.addValue("20120928"); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue("20120928"); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);      a.addValue("121310.062000"); list.put(a); }
		
		{ Attribute a = new IntegerStringAttribute(TagFromName.ActualFrameDuration); a.addValue("120000"); list.put(a); }
		
		String[] acquisitionTimes    = { "121310.062000",   "121513.671000",  "121717.468000",  "121921.296000",  "122125.281000",  "122328.984000"  };
		String[] frameReferenceTimes = { "59936.854672596", "182936.8546726", "306936.8546726", "430936.8546726", "554936.8546726", "677936.8546726" };
		
		for (int i=0; i<frameReferenceTimes.length; ++ i) {
//System.err.println("Doing ["+i+"] "+acquisitionTimes[i]+" "+frameReferenceTimes[i]);
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime);             a.addValue(acquisitionTimes[i]);    list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.FrameReferenceTime); a.addValue(frameReferenceTimes[i]); list.put(a); }
			SUVTransform t = new SUVTransform(list);
			SUVTransform.SingleSUVTransform ts = t.getSingleSUVTransform(1);
			long derivedScanDateTime = SUVTransform.deriveScanDateTimeFromHalfLifeAcquisitionDateTimeFrameReferenceTimeAndActualFrameDuration(list);
//System.err.println("For ["+i+"] "+acquisitionTimes[i]+" "+frameReferenceTimes[i]+" derivedScanDateTime = "+derivedScanDateTime+" compared to expectSeriesTime "+expectSeriesTime+" difference is "+((derivedScanDateTime-expectSeriesTime)/1000)+" secs");
			assertTrue("derivedScanDateTime == expectSeriesTime in seconds",((derivedScanDateTime-expectSeriesTime)/1000) == 0);	// may not match down to the millisecond, unfortunately
		}

	}
	
}
