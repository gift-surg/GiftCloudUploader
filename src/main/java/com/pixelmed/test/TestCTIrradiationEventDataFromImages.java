/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import java.io.File;

import java.util.Locale;

import com.pixelmed.dicom.*;

import com.pixelmed.dose.CTAcquisitionParameters;
import com.pixelmed.dose.CTIrradiationEventDataFromImages;
import com.pixelmed.dose.CTScanType;

import junit.framework.*;

public class TestCTIrradiationEventDataFromImages extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCTIrradiationEventDataFromImages(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTIrradiationEventDataFromImages.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCTIrradiationEventDataFromImages");
		
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_SeriesNumber_OneAcquisitionTime"));
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_SeriesNumber_TwoAcquisitionTimes"));
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_AcquisitionNumber"));
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_Localizer"));
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_PitchFactorZero"));
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_Anatomy"));
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_AnatomyDifferent"));
		suite.addTest(new TestCTIrradiationEventDataFromImages("TestCTIrradiationEventDataFromImages_AnatomyCombined"));
		
		return suite;
	}
		
	protected void setUp() {
		Locale.setDefault(Locale.FRENCH);	// forces check that "," is not being used as decimal point in any double to string conversions
	}
	
	protected void tearDown() {
	}
	
	public void TestCTIrradiationEventDataFromImages_SeriesNumber_OneAcquisitionTime() throws Exception {

		double pitchFactor = 1;
		String expectPitchFactor = "1";
		String kvp = "140";
		String studyInstanceUID = "1.2.3.4";
		String seriesInstanceUID = "1.2.3.4.5";
		String seriesNumber = "56";
		String acquisitionTime = "100134";
		String imageTypeValue3 = "AXIAL";
		CTScanType expectScanType = CTScanType.UNKNOWN;		// i.e., not LOCALIZER or STATIONARY

		String sopInstanceUID1 = "1.2.3.4.5.6";
		String sliceLocation1 = "100.000";	// must include three decimal places else won't match :(
		String sliceLocationPrefix1 = "S";

		String sopInstanceUID2 = "1.2.3.4.5.7";
		String sliceLocation2 = "150.000";	// must include three decimal places else won't match :(
		String sliceLocationPrefix2 = "S";
		
		File testFile1 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile1.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new FloatDoubleAttribute     (TagFromName.SpiralPitchFactor); a.addValue(pitchFactor); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.KVP);               a.addValue(kvp); list.put(a); }
			{ Attribute a = new IntegerStringAttribute   (TagFromName.SeriesNumber);      a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new TimeAttribute            (TagFromName.AcquisitionTime);   a.addValue(acquisitionTime); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);  a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }			// otherwise won't have IrradiationEventUID and nothing will be returned
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);         a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);    a.addValue(sopInstanceUID1); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);       a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();

			{ Attribute a = new DecimalStringAttribute   (TagFromName.SliceLocation);     a.addValue(sliceLocation1); list.put(a); }
			
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }

			list.write(testFile1,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		File testFile2 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile2.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new FloatDoubleAttribute     (TagFromName.SpiralPitchFactor); a.addValue(pitchFactor); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.KVP);               a.addValue(kvp); list.put(a); }
			{ Attribute a = new IntegerStringAttribute   (TagFromName.SeriesNumber);      a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new TimeAttribute            (TagFromName.AcquisitionTime);   a.addValue(acquisitionTime); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);  a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }			// otherwise won't have IrradiationEventUID and nothing will be returned
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);         a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);    a.addValue(sopInstanceUID2); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);       a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();

			{ Attribute a = new DecimalStringAttribute   (TagFromName.SliceLocation);     a.addValue(sliceLocation2); list.put(a); }

			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }

			list.write(testFile2,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile1);
			eventDataFromImages.add(testFile2);
//System.err.print(eventDataFromImages);

			// note order of slice locations in key ... most superior must come first else won't match
			String seriesNumberAndScanRangeAndStudyInstanceUIDKey = seriesNumber+"+"+sliceLocationPrefix2+sliceLocation2+"+"+sliceLocationPrefix1+sliceLocation1+"+"+studyInstanceUID;
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_SeriesNumber(): seriesNumberAndScanRangeAndStudyInstanceUIDKey = "+seriesNumberAndScanRangeAndStudyInstanceUIDKey);
			CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersBySeriesNumberScanRangeAndStudyInstanceUID(seriesNumberAndScanRangeAndStudyInstanceUIDKey);
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_SeriesNumber(): CTAcquisitionParameters = "+ap);
			assertEquals("Checking CTAcquisitionParameters SpiralPitchFactor",expectPitchFactor,ap.getPitchFactor());
			assertEquals("Checking CTAcquisitionParameters KVP",kvp,ap.getKVP());
			assertEquals("Checking CTAcquisitionParameters ScanType",expectScanType,ap.getScanType());
		}
	}
	
	
	public void TestCTIrradiationEventDataFromImages_SeriesNumber_TwoAcquisitionTimes() throws Exception {

		String acquisitionTimeA = "100134";
		String acquisitionTimeB = "100248";

		double pitchFactorA = 1;
		String expectPitchFactorA = "1";
		String kvpA = "140";
		CTScanType expectScanTypeA = CTScanType.UNKNOWN;		// i.e., not LOCALIZER or STATIONARY
		String protocolNameA = "Protocol A";

		double pitchFactorB = 1.5;
		String expectPitchFactorB = "1.5";
		String kvpB = "120";
		CTScanType expectScanTypeB = CTScanType.UNKNOWN;		// i.e., not LOCALIZER or STATIONARY
		String protocolNameB = "Protocol B";

		String studyInstanceUID = "1.2.3.4";
		String seriesInstanceUID = "1.2.3.4.5";
		String seriesNumber = "56";
		String imageTypeValue3 = "AXIAL";

		String sopInstanceUID1 = "1.2.3.4.5.6";
		String sliceLocation1 = "100.000";	// must include three decimal places else won't match :(
		String sliceLocationPrefix1 = "S";

		String sopInstanceUID2 = "1.2.3.4.5.7";
		String sliceLocation2 = "150.000";	// must include three decimal places else won't match :(
		String sliceLocationPrefix2 = "S";
		
		String sopInstanceUID3 = "1.2.3.4.5.8";
		String sliceLocation3 = "95.000";	// must include three decimal places else won't match :(
		String sliceLocationPrefix3 = "S";

		String sopInstanceUID4 = "1.2.3.4.5.9";
		String sliceLocation4 = "155.000";	// must include three decimal places else won't match :(
		String sliceLocationPrefix4 = "S";
		
		File testFile1 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile1.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new IntegerStringAttribute   (TagFromName.SeriesNumber);      a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);  a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }			// otherwise won't have IrradiationEventUID and nothing will be returned
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);         a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);    a.addValue(sopInstanceUID1); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);       a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();

			{ Attribute a = new DecimalStringAttribute   (TagFromName.SliceLocation);     a.addValue(sliceLocation1); list.put(a); }

			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }

			{ Attribute a = new TimeAttribute            (TagFromName.AcquisitionTime);   a.addValue(acquisitionTimeA); list.put(a); }
			{ Attribute a = new FloatDoubleAttribute     (TagFromName.SpiralPitchFactor); a.addValue(pitchFactorA); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.KVP);               a.addValue(kvpA); list.put(a); }
			{ Attribute a = new LongStringAttribute      (TagFromName.ProtocolName);      a.addValue(protocolNameA); list.put(a); }

			list.write(testFile1,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		File testFile2 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile2.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new IntegerStringAttribute   (TagFromName.SeriesNumber);      a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);  a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }			// otherwise won't have IrradiationEventUID and nothing will be returned
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);         a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);    a.addValue(sopInstanceUID2); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);       a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();

			{ Attribute a = new DecimalStringAttribute   (TagFromName.SliceLocation);     a.addValue(sliceLocation2); list.put(a); }

			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }

			{ Attribute a = new TimeAttribute            (TagFromName.AcquisitionTime);   a.addValue(acquisitionTimeA); list.put(a); }
			{ Attribute a = new FloatDoubleAttribute     (TagFromName.SpiralPitchFactor); a.addValue(pitchFactorA); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.KVP);               a.addValue(kvpA); list.put(a); }
			{ Attribute a = new LongStringAttribute      (TagFromName.ProtocolName);      a.addValue(protocolNameA); list.put(a); }

			list.write(testFile2,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		File testFile3 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile3.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new IntegerStringAttribute   (TagFromName.SeriesNumber);      a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);  a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }			// otherwise won't have IrradiationEventUID and nothing will be returned
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);         a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);    a.addValue(sopInstanceUID3); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);       a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();

			{ Attribute a = new DecimalStringAttribute   (TagFromName.SliceLocation);     a.addValue(sliceLocation3); list.put(a); }

			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }

			{ Attribute a = new TimeAttribute            (TagFromName.AcquisitionTime);   a.addValue(acquisitionTimeB); list.put(a); }
			{ Attribute a = new FloatDoubleAttribute     (TagFromName.SpiralPitchFactor); a.addValue(pitchFactorB); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.KVP);               a.addValue(kvpB); list.put(a); }
			{ Attribute a = new LongStringAttribute      (TagFromName.ProtocolName);      a.addValue(protocolNameB); list.put(a); }

			list.write(testFile3,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		File testFile4 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile4.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new IntegerStringAttribute   (TagFromName.SeriesNumber);      a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);  a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }			// otherwise won't have IrradiationEventUID and nothing will be returned
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);         a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);    a.addValue(sopInstanceUID4); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);       a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();

			{ Attribute a = new DecimalStringAttribute   (TagFromName.SliceLocation);     a.addValue(sliceLocation4); list.put(a); }

			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }

			{ Attribute a = new TimeAttribute            (TagFromName.AcquisitionTime);   a.addValue(acquisitionTimeB); list.put(a); }
			{ Attribute a = new FloatDoubleAttribute     (TagFromName.SpiralPitchFactor); a.addValue(pitchFactorB); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.KVP);               a.addValue(kvpB); list.put(a); }
			{ Attribute a = new LongStringAttribute      (TagFromName.ProtocolName);      a.addValue(protocolNameB); list.put(a); }

			list.write(testFile4,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile1);
			eventDataFromImages.add(testFile2);
			eventDataFromImages.add(testFile3);
			eventDataFromImages.add(testFile4);

			{
				// note order of slice locations in key ... most superior must come first else won't match
				String seriesNumberAndScanRangeAndStudyInstanceUIDKey = seriesNumber+"+"+sliceLocationPrefix2+sliceLocation2+"+"+sliceLocationPrefix1+sliceLocation1+"+"+studyInstanceUID;
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_SeriesNumber(): seriesNumberAndScanRangeAndStudyInstanceUIDKey = "+seriesNumberAndScanRangeAndStudyInstanceUIDKey);
				CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersBySeriesNumberScanRangeAndStudyInstanceUID(seriesNumberAndScanRangeAndStudyInstanceUIDKey);
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_SeriesNumber(): CTAcquisitionParameters = "+ap);
				assertEquals("Checking CTAcquisitionParameters SpiralPitchFactor",expectPitchFactorA,ap.getPitchFactor());
				assertEquals("Checking CTAcquisitionParameters KVP",kvpA,ap.getKVP());
				assertEquals("Checking CTAcquisitionParameters ScanType",expectScanTypeA,ap.getScanType());
				assertEquals("Checking CTAcquisitionParameters AcqusitionProtocol",protocolNameA,ap.getAcquisitionProtocol());
			}
			{
				String seriesNumberAndScanRangeAndStudyInstanceUIDKey = seriesNumber+"+"+sliceLocationPrefix4+sliceLocation4+"+"+sliceLocationPrefix3+sliceLocation3+"+"+studyInstanceUID;
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_SeriesNumber(): seriesNumberAndScanRangeAndStudyInstanceUIDKey = "+seriesNumberAndScanRangeAndStudyInstanceUIDKey);
				CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersBySeriesNumberScanRangeAndStudyInstanceUID(seriesNumberAndScanRangeAndStudyInstanceUIDKey);
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_SeriesNumber(): CTAcquisitionParameters = "+ap);
				assertEquals("Checking CTAcquisitionParameters SpiralPitchFactor",expectPitchFactorB,ap.getPitchFactor());
				assertEquals("Checking CTAcquisitionParameters KVP",kvpB,ap.getKVP());
				assertEquals("Checking CTAcquisitionParameters ScanType",expectScanTypeB,ap.getScanType());
				assertEquals("Checking CTAcquisitionParameters AcqusitionProtocol",protocolNameB,ap.getAcquisitionProtocol());
			}
		}
	}
	
	public void TestCTIrradiationEventDataFromImages_AcquisitionNumber() throws Exception {
		double pitchFactor = 1;
		String expectPitchFactor = "1";
		String kvp = "140";
		String studyInstanceUID = "1.2.3.4";
		String seriesInstanceUID = "1.2.3.4.5";
		String seriesNumber = "56";
		String acquisitionNumber = "43";
		String acquisitionTime = "100134";
		String imageTypeValue3 = "AXIAL";
		CTScanType expectScanType = CTScanType.UNKNOWN;		// i.e., not LOCALIZER or STATIONARY

		String sopInstanceUID = "1.2.3.4.5.6";
		String sliceLocation = "100.000";
		String sliceLocationPrefix = "S";
		
		File testFile = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new FloatDoubleAttribute     (TagFromName.SpiralPitchFactor); a.addValue(pitchFactor); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.KVP);               a.addValue(kvp); list.put(a); }
			{ Attribute a = new IntegerStringAttribute   (TagFromName.SeriesNumber);      a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new IntegerStringAttribute   (TagFromName.AcquisitionNumber); a.addValue(acquisitionNumber); list.put(a); }
			{ Attribute a = new TimeAttribute            (TagFromName.AcquisitionTime);   a.addValue(acquisitionTime); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID);  a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }			// otherwise won't have IrradiationEventUID and nothing will be returned
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);    a.addValue(sopInstanceUID); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);       a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ Attribute a = new DecimalStringAttribute   (TagFromName.SliceLocation);     a.addValue(sliceLocation); list.put(a); }				// even though scan range is not matched, need this else will be no irradiation event created
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);         a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile);

			String acquisitionNumberAndStudyInstanceUIDKey = acquisitionNumber+"+"+studyInstanceUID;
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_AcquisitionNumber(): acquisitionNumberAndStudyInstanceUIDKey = "+acquisitionNumberAndStudyInstanceUIDKey);
			CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersByAcquisitionNumberAndStudyInstanceUID(acquisitionNumberAndStudyInstanceUIDKey);
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_AcquisitionNumber(): CTAcquisitionParameters = "+ap);
			assertEquals("Checking CTAcquisitionParameters SpiralPitchFactor",expectPitchFactor,ap.getPitchFactor());
			assertEquals("Checking CTAcquisitionParameters KVP",kvp,ap.getKVP());
			assertEquals("Checking CTAcquisitionParameters ScanType",expectScanType,ap.getScanType());
		}
	}
	
	public void TestCTIrradiationEventDataFromImages_Localizer() throws Exception {

		String irradiationEventUID = "1.2.3.4";
		String sopInstanceUID = "1.2.3.4.5";
		String imageTypeValue3 = "LOCALIZER";
		CTScanType expectScanType = CTScanType.LOCALIZER;

		File testFile = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.IrradiationEventUID); a.addValue(irradiationEventUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);      a.addValue(sopInstanceUID); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);         a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);           a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }	// is not a valid orientation for a localizer, but otherwise will never get through
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile);
			CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersForIrradiationEvent(irradiationEventUID);
//System.err.print("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_Localizer(): CTAcquisitionParameters = "+ap);
			assertEquals("Checking CTAcquisitionParameters ScanType",expectScanType,ap.getScanType());
		}
	}
	
	public void TestCTIrradiationEventDataFromImages_PitchFactorZero() throws Exception {

		double pitchFactor = 0;
		String expectPitchFactor = "0";
		String irradiationEventUID = "1.2.3.4";
		String sopInstanceUID = "1.2.3.4.5";
		String imageTypeValue3 = "AXIAL";
		CTScanType expectScanType = CTScanType.STATIONARY;

		File testFile = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new FloatDoubleAttribute(TagFromName.SpiralPitchFactor);        a.addValue(pitchFactor); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.IrradiationEventUID); a.addValue(irradiationEventUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);      a.addValue(sopInstanceUID); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);         a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ Attribute a = new CodeStringAttribute      (TagFromName.ImageType);           a.addValue("ORIGINAL"); a.addValue("PRIMARY"); a.addValue(imageTypeValue3); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }
//System.err.print("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_PitchFactorZero(): writing\n"+list);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile);
			CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersForIrradiationEvent(irradiationEventUID);
//System.err.print("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_PitchFactorZero(): CTAcquisitionParameters = "+ap);
			assertEquals("Checking CTAcquisitionParameters SpiralPitchFactor",expectPitchFactor,ap.getPitchFactor());
			assertEquals("Checking CTAcquisitionParameters ScanType stationary because of zero pitch",expectScanType,ap.getScanType());
		}
	}
	
	public void TestCTIrradiationEventDataFromImages_Anatomy() throws Exception {

		String irradiationEventUID = "1.2.3.4";
		String sopInstanceUID = "1.2.3.4.5";
		CodedSequenceItem anatomy = new CodedSequenceItem("R-FAB56","SRT","Chest, Abdomen and Pelvis");

		File testFile = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.IrradiationEventUID); a.addValue(irradiationEventUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);      a.addValue(sopInstanceUID); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);         a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.AnatomicRegionSequence); a.addItem(anatomy.getAttributeList()); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }
//System.err.print("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_Anatomy(): writing\n"+list);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile);
			CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersForIrradiationEvent(irradiationEventUID);
//System.err.print("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_Anatomy(): CTAcquisitionParameters = "+ap);
			assertEquals("Checking CTAcquisitionParameters anatomy",anatomy,ap.getAnatomy());
		}
	}
	
	public void TestCTIrradiationEventDataFromImages_AnatomyDifferent() throws Exception {

		String irradiationEventUID = "1.2.3.4";

		String sopInstanceUID1 = "1.2.3.4.5.6";
		CodedSequenceItem anatomy1 = new CodedSequenceItem("R-FAB56","SRT","Chest, Abdomen and Pelvis");

		String sopInstanceUID2 = "1.2.3.4.5.7";
		CodedSequenceItem anatomy2 = new CodedSequenceItem("T-D8810","SRT","Thumb");
		
		File testFile1 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile1.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.IrradiationEventUID); a.addValue(irradiationEventUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);      a.addValue(sopInstanceUID1); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);         a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.AnatomicRegionSequence); a.addItem(anatomy1.getAttributeList()); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }
			list.write(testFile1,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		File testFile2 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile2.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.IrradiationEventUID); a.addValue(irradiationEventUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);      a.addValue(sopInstanceUID2); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);         a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.AnatomicRegionSequence); a.addItem(anatomy2.getAttributeList()); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }
			list.write(testFile2,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile1);
			eventDataFromImages.add(testFile2);
			CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersForIrradiationEvent(irradiationEventUID);
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_AnatomyDifferent(): CTAcquisitionParameters = "+ap);
			assertTrue("Checking CTAcquisitionParameters anatomy empty",ap.getAnatomy() == null);
		}
	}
	
	public void TestCTIrradiationEventDataFromImages_AnatomyCombined() throws Exception {

		String irradiationEventUID = "1.2.3.4";

		String sopInstanceUID1 = "1.2.3.4.5.6";
		CodedSequenceItem anatomy1 = new CodedSequenceItem("T-D3000","SRT","Chest");

		String sopInstanceUID2 = "1.2.3.4.5.7";
		CodedSequenceItem anatomy2 = new CodedSequenceItem("T-D4000","SRT","Abdomen");
		
		CodedSequenceItem expectedAnatomy = new CodedSequenceItem("R-FAB55","SRT","Chest and Abdomen");

		File testFile1 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile1.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.IrradiationEventUID); a.addValue(irradiationEventUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);      a.addValue(sopInstanceUID1); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);         a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.AnatomicRegionSequence); a.addItem(anatomy1.getAttributeList()); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }
			list.write(testFile1,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		File testFile2 = File.createTempFile("TestCTIrradiationEventDataFromImages",".tmp");
		testFile2.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.IrradiationEventUID); a.addValue(irradiationEventUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID);      a.addValue(sopInstanceUID2); list.put(a); }			// otherwise DicomFileUtilities.isDicomOrAcrNemaFile() used in CTIrradiationEventDataFromImages will not recognize in IVRLE TS
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID);         a.addValue(SOPClass.CTImageStorage); list.put(a); }	// otherwise will be ignored by CTIrradiationEventDataFromImages();
			{ SequenceAttribute a = new SequenceAttribute(TagFromName.AnatomicRegionSequence); a.addItem(anatomy2.getAttributeList()); list.put(a); }
			{ Attribute a = new DecimalStringAttribute   (TagFromName.ImageOrientationPatient); a.addValue(1d); a.addValue(0d); a.addValue(0d); a.addValue(0d); a.addValue(1d); a.addValue(0d); list.put(a); }
			list.write(testFile2,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			CTIrradiationEventDataFromImages eventDataFromImages = new CTIrradiationEventDataFromImages();
			eventDataFromImages.add(testFile1);
			eventDataFromImages.add(testFile2);
			CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParametersForIrradiationEvent(irradiationEventUID);
//System.err.println("TestCTIrradiationEventDataFromImages.TestCTIrradiationEventDataFromImages_AnatomyDifferent(): CTAcquisitionParameters = "+ap);
			assertEquals("Checking CTAcquisitionParameters anatomy combined",expectedAnatomy,ap.getAnatomy());
		}
	}
	
}
