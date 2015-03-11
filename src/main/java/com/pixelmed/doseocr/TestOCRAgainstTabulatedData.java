/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.doseocr;

import com.pixelmed.dose.*;

import junit.framework.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestOCRAgainstTabulatedData extends TestCase {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/doseocr/TestOCRAgainstTabulatedData.java,v 1.13 2013/02/01 13:53:20 dclunie Exp $";
	
	// constructor to support adding tests to suite ...
	
	public TestOCRAgainstTabulatedData(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCTScanType.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestOCRAgainstTabulatedData");
		
		suite.addTest(new TestOCRAgainstTabulatedData("testOCRAgainstTabulatedData_AllFiles"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private void recursiveDirectorySearch(Map<String,File> mapOfBaseNameToFile,File directory) {
		File[] files = directory.listFiles();
		for (int i=0; i<files.length; ++i) {
			if (files[i].isDirectory()) {
//System.err.println("recursiveDirectorySearch(): recursing into directory "+files[i]);
				recursiveDirectorySearch(mapOfBaseNameToFile,files[i]);
			}
			else {
				String baseName = files[i].getName();
				if (!baseName.startsWith(".")) {
//System.err.println("recursiveDirectorySearch(): adding file "+files[i]);
					mapOfBaseNameToFile.put(baseName,files[i]);
				}
			}
		}
	}
	
	public void testOCRAgainstTabulatedData_AllFiles() throws Exception {
		String pathToTestImages = "../../../testpaths/DoseFilesCopied";
		String pathToTestData = "./doseocrcheck.dat";
		String pathToOcrGlyphs = "./OCR_Glyphs_DoseScreen.xml";
		
		Map<String,File> mapOfBaseNameToFile = new HashMap<String,File>();
		recursiveDirectorySearch(mapOfBaseNameToFile,new File(pathToTestImages));
		
		//FILENAME	1.2.124.113532.10.8.8.91.20080701.165343.367049
		Pattern pFileName = Pattern.compile("FILENAME\t(.+)");
		
		//STUYDINSTANCEUID	1.2.124.113532.10.8.8.91.20080701.165343.367049
		Pattern pStudyInstanceUID = Pattern.compile("STUDYINSTANCEUID\t(.+)");
		
		//1	SCOUT	-	-	-	-	-	-	-
		//2	HELICAL	I	146.500	I	406.500	16.16	474.49	BODY32
		//200	AXIAL	I	84.250	I	84.250	13.39	13.40	BODY32
		Pattern pGEEvent = Pattern.compile("([0-9]+)[ \t]+([A-Z]+)[ \t]+([SI])[ \t]+([0-9]*[.][0-9]*)[ \t]+([SI])[ \t]+([0-9]*[.][0-9]*)[ \t]+([0-9]*[.][0-9]*)[ \t]+([0-9]*[.][0-9]*)[ \t]+(.*)[ \t]*");
		//2	- 6.68	265.00 BODY32
		Pattern pSiemensEvent = Pattern.compile("([0-9-]+)[ \t]+(TOPOGRAM|-)[ \t]+([0-9]*[.][0-9]*)[ \t]+([0-9]*[.][0-9]*)[ \t]+([A-Z0-9-]*).*");	// sometimes Siemens series (acquisition) numbers are hyphenated
		//TOTAL 2413.44
		Pattern pTotal = Pattern.compile("TOTAL[ \t]+([0-9]*[.][0-9]*)");

		CTDose ctDoseFromOCR = null;
		CTDose ctDoseFromData = null;
		String studyInstanceUID = null;
		BufferedReader r = new BufferedReader(new FileReader(pathToTestData));
		String line = null;
		while ((line=r.readLine()) != null) {
			line=line.toUpperCase(java.util.Locale.US);
			Matcher mFileName = pFileName.matcher(line);
			if (mFileName.matches()) {
				String fileName = mFileName.group(1);		// first group is not 0, which is the entire match
//System.err.println("Matched file "+fileName);
				File imageFile = mapOfBaseNameToFile.get(fileName);
System.err.println("Processing file "+imageFile);
				OCR ocr = new OCR(imageFile.getCanonicalPath(),pathToOcrGlyphs,null,0/*debugLevel*/);
//System.err.println(ocr);
				ctDoseFromOCR = OCR.getCTDoseFromOCROfDoseScreen(ocr,0/*debugLevel*/,null,false);
//System.err.println(ctDoseFromOCR);
				assertFalse("check parse returned something",ctDoseFromOCR == null);
				ctDoseFromData = new CTDose(ScopeOfDoseAccummulation.STUDY,"0.0.0.0",null,null,"");
			}
			else {
				Matcher mStudyInstanceUID = pStudyInstanceUID.matcher(line);
				if (mStudyInstanceUID.matches()) {
					studyInstanceUID = mStudyInstanceUID.group(1);		// first group is not 0, which is the entire match
//System.err.println("Matched studyInstanceUID "+studyInstanceUID);
				}
				else {
					Matcher mTotal = pTotal.matcher(line);
					if (mTotal.matches()) {
						String dlpTotal = mTotal.group(1);		// first group is not 0, which is the entire match
//System.err.println("Matched total "+dlpTotal);
						ctDoseFromData.setDLPTotal(dlpTotal);
					}
					else {
						Matcher mGEEvent = pGEEvent.matcher(line);
						if (mGEEvent.matches()) {
//System.err.println("Matched GE event");
							String series = mGEEvent.group(1);		// first group is not 0, which is the entire match
							String scanType = mGEEvent.group(2);
							String rangeFromSI = mGEEvent.group(3);
							String rangeFromLocation = mGEEvent.group(4);
							String rangeToSI = mGEEvent.group(5);
							String rangeToLocation = mGEEvent.group(6);
							String CTDIvol = mGEEvent.group(7);
							String DLP = mGEEvent.group(8);
							String phantom = mGEEvent.group(9).replaceAll("[ \t]+","").trim();
							CTScanType recognizedScanType = CTScanType.selectFromDescription(scanType);
							if (recognizedScanType != null && !recognizedScanType.equals(CTScanType.LOCALIZER)) {
//System.err.println("Adding GE acquisition");
								ctDoseFromData.addAcquisition(new CTDoseAcquisition(studyInstanceUID,true/*isSeries*/,series,recognizedScanType,new ScanRange(rangeFromSI,rangeFromLocation,rangeToSI,rangeToLocation),CTDIvol,DLP,CTPhantomType.selectFromDescription(phantom)));
							}
						}
						else {
//System.err.println("Checking for Siemens event in line "+line);
							Matcher mSiemensEvent = pSiemensEvent.matcher(line);
							if (mSiemensEvent.matches()) {
//System.err.println("Matched Siemens event");
								String series = mSiemensEvent.group(1);		// first group is not 0, which is the entire match
								String scanType = mSiemensEvent.group(2);
								String CTDIvol = mSiemensEvent.group(3);
								String DLP = mSiemensEvent.group(4);
								String phantom = mSiemensEvent.group(5);
								ctDoseFromData.addAcquisition(new CTDoseAcquisition(studyInstanceUID,false/*isSeries*/,series,CTScanType.selectFromDescription(scanType),null/*scan range*/,CTDIvol,DLP,CTPhantomType.selectFromDescription(phantom)));
							}
							else if(line.startsWith("ENDFILE")) {
System.err.println("From OCR:\n"+ctDoseFromOCR);
System.err.println("From Data:\n"+ctDoseFromData);
								assertEquals("Checking DLP Total",ctDoseFromData.getDLPTotal(),ctDoseFromOCR.getDLPTotal());
								assertEquals("Checking totalNumberOfIrradiationEvents",ctDoseFromData.getTotalNumberOfIrradiationEvents(),ctDoseFromOCR.getTotalNumberOfIrradiationEvents());
								int n = ctDoseFromData.getTotalNumberOfIrradiationEvents();
								for (int i=0; i<n; ++i) {
									CTDoseAcquisition acqFromOCR = ctDoseFromOCR.getAcquisition(i);
									CTDoseAcquisition acqFromData = ctDoseFromData.getAcquisition(i);
System.err.println("From OCR:\n"+acqFromOCR);
System.err.println("From Data:\n"+acqFromData);
									assertTrue("Checking CTDoseAcquisition "+i+" equality",acqFromOCR.equals(acqFromData));
								}
							}
						}
					}
				}
			}
		}
	}
}

