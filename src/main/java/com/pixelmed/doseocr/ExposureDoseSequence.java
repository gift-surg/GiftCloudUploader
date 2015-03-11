/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.doseocr;

import com.pixelmed.anatproc.CTAnatomy;
import com.pixelmed.anatproc.DisplayableAnatomicConcept;
import com.pixelmed.dicom.*;
import com.pixelmed.dose.*;
import com.pixelmed.utils.FloatFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A class to extract Exposure Dose Sequence and related attributes from Philips modality dose report screen saves.</p>
 *
  * @author	dclunie
 */

public class ExposureDoseSequence {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/doseocr/ExposureDoseSequence.java,v 1.30 2013/02/01 13:53:20 dclunie Exp $";
	
	private static AttributeTag privateDLPTag = new AttributeTag(0x00e1,0x1021);

	public static String getValuesFromCommentsOnRadiationDose(AttributeList list,Map<String,String> DLPFromCommentsOnRadiationDoseIndexedBySeriesNumber,Map<String,String> CTDIVolFromCommentsOnRadiationDoseIndexedBySeriesNumber,int debugLevel) throws IOException {
if (debugLevel > 0) System.err.println("ExposureDoseSequence.getValuesFromCommentsOnRadiationDose():");
		String totalDLPFromCommentsOnRadiationDose = "";
		String commentsOnRadiationDose = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CommentsOnRadiationDose).toUpperCase(java.util.Locale.US).trim();
		if (commentsOnRadiationDose.length() > 0) {
			//Series #2 OS Average CTDIvol=23.3 DLP=899.8
			Pattern pSeries = Pattern.compile("[ \t]*SERIES #[ \t]*([0-9]+)[ \t]*.*CTDIVOL[ \t]*=[ \t]*([0-9]*[.][0-9]*)[ \t]+DLP[ \t]*=[ \t]*([0-9]*[.][0-9]*).*");
			//EVENT=3 DLP=950.10
			Pattern pEvent = Pattern.compile("[ \t]*EVENT=[ \t]*([0-9]+)[ \t]*DLP[ \t]*=[ \t]*([0-9]*[.][0-9]*).*");
			//Total DLP=1510.1
			Pattern pTotal = Pattern.compile("[ \t]*TOTAL[ \t]*DLP=[ \t]*([0-9]*[.][0-9]*).*");
			BufferedReader r = new BufferedReader(new StringReader(commentsOnRadiationDose.toString()));
			String line = null;
			while ((line=r.readLine()) != null) {
				line=line.toUpperCase(java.util.Locale.US);
if (debugLevel > 0) System.err.println(line);
				if (line.contains("SERIES")) {			// Philips
					Matcher m = pSeries.matcher(line);
					if (m.matches()) {
if (debugLevel > 0) System.err.println("matches");
						int groupCount = m.groupCount();
if (debugLevel > 0) System.err.println("groupCount = "+groupCount);
						if (groupCount >= 3) {
							String series = m.group(1);		// first group is not 0, which is the entire match
							String CTDIVol = m.group(2);		// first group is not 0, which is the entire match
							String DLP = m.group(3);		// first group is not 0, which is the entire match
if (debugLevel > 0) System.err.println("series = "+series+", CTDIVol = "+CTDIVol+", DLP = "+DLP);
							if (series.length() > 0) {
								CTDIVolFromCommentsOnRadiationDoseIndexedBySeriesNumber.put(series,CTDIVol);
								DLPFromCommentsOnRadiationDoseIndexedBySeriesNumber.put(series,DLP);
							}
						}
					}
				}
				if (line.contains("EVENT")) {			// GE
					Matcher m = pEvent.matcher(line);
					if (m.matches()) {
if (debugLevel > 0) System.err.println("matches");
						int groupCount = m.groupCount();
if (debugLevel > 0) System.err.println("groupCount = "+groupCount);
						if (groupCount >= 2) {
							String series = m.group(1);		// first group is not 0, which is the entire match
							String DLP = m.group(2);		// first group is not 0, which is the entire match
if (debugLevel > 0) System.err.println("series = "+series+", DLP = "+DLP);
							if (series.length() > 0) {
								DLPFromCommentsOnRadiationDoseIndexedBySeriesNumber.put(series,DLP);
							}
						}
					}
				}
				else if (line.contains("TOTAL")) {
					Matcher m = pTotal.matcher(line);
					if (m.matches()) {
if (debugLevel > 0) System.err.println("matches");
						int groupCount = m.groupCount();
if (debugLevel > 0) System.err.println("groupCount = "+groupCount);
						if (groupCount >= 1) {
							totalDLPFromCommentsOnRadiationDose = m.group(1);		// first group is not 0, which is the entire match
if (debugLevel > 0) System.err.println("totalDLPFromCommentsOnRadiationDose = "+totalDLPFromCommentsOnRadiationDose);
						}
					}
				}
			}
		}
		return totalDLPFromCommentsOnRadiationDose;
	}
	
	public static String getPhilipsPrivateDLPValue(AttributeList list) throws DicomException {
		String DLP = "";
		Attribute aDLP = list.get(privateDLPTag);	// should check private creator is ELSCINT1, and also moved private group :(
		if (aDLP instanceof UnknownAttribute) {
			byte[] bytes = aDLP.getByteValues();
//System.err.println(com.pixelmed.utils.HexDump.dump(bytes));
			DLP = new String(bytes);
//System.err.println("DLP extracted from private UN attribute = "+DLP);
			// precision is excessive ... reduce it
			DLP = FloatFormatter.toString(new Double(DLP).doubleValue(),Locale.US);
		}
		else {
			DLP = Attribute.getSingleStringValueOrEmptyString(list,privateDLPTag);
		}
		return DLP;
	}

	public static boolean isPossiblyPhilipsDoseScreenSeries(String manufacturer,String modality,String seriesNumber,String seriesDescription) {
		return (manufacturer == null || manufacturer.length() == 0 || manufacturer.toUpperCase(java.util.Locale.US).contains("PHILIPS")) && modality != null && modality.equals("CT") && seriesDescription != null && seriesDescription.toLowerCase(java.util.Locale.US).trim().equals("dose info");
	}
	
	public static boolean isPossiblyPhilipsDoseScreenSeries(AttributeList list) {
		return isPossiblyPhilipsDoseScreenSeries(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.Manufacturer),Attribute.getSingleStringValueOrEmptyString(list,TagFromName.Modality),null,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesDescription));
	}
	
	public static boolean isPossiblyPhilipsDoseScreenInstance(String manufacturer,String sopClassUID,String imageType) {
		// no need for trim() since using contains()
		return (manufacturer == null || manufacturer.length() == 0 || manufacturer.toUpperCase(java.util.Locale.US).contains("PHILIPS")) && imageType != null && (imageType.contains("DOSE_INFO") || imageType.contains("DOSE-INFO") || imageType.contains("LOCALIZER"));	// hyphenated form is illegal but sometimes seen (e.g. in query responses - (000575))
	}
	
	public static boolean isPossiblyPhilipsDoseScreenInstance(AttributeList list) {
		return isPossiblyPhilipsDoseScreenInstance(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.Manufacturer),null,Attribute.getDelimitedStringValuesOrDefault(list,TagFromName.ImageType,""));
	}
	
	public static boolean isPhilipsDoseScreenInstance(AttributeList list) {
		String imageType = Attribute.getDelimitedStringValuesOrDefault(list,TagFromName.ImageType,"").trim();
		return Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesDescription).toLowerCase(java.util.Locale.US).trim().equals("dose info")
		    && isPossiblyPhilipsDoseScreenInstance(list)
			&& list.get(TagFromName.ExposureDoseSequence) != null;	// this is necessary to distinguish localizers that pretend to be dose info but don't have the information present (and may be mixed in the same series as a real one)
	}
	
	public static CTDose getCTDoseFromExposureDoseSequence(AttributeList list,int debugLevel,CTIrradiationEventDataFromImages eventDataFromImages,boolean buildSR) throws IOException, DicomException {
if (debugLevel > 0) System.err.println("ExposureDoseSequence.getCTDoseFromExposureDoseSequence():");
		String studyInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyInstanceUID);
		String studyDescription = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDescription);
		String startDateTime = null;
		String endDateTime = null;
		if (eventDataFromImages != null) {
			startDateTime = eventDataFromImages.getOverallEarliestAcquisitionDateTimeForStudy(studyInstanceUID);
			endDateTime = eventDataFromImages.getOverallLatestAcquisitionDateTimeForStudy(studyInstanceUID);
		}
		if (startDateTime == null || startDateTime.trim().length() == 0 && list != null) {
			startDateTime = Attribute.getSingleStringValueOrNull(list,TagFromName.StudyDate);
			if (startDateTime != null && startDateTime.length() == 8) {
				startDateTime = startDateTime + Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyTime);
			}
		}
		CTDose ctDose = new CTDose(ScopeOfDoseAccummulation.STUDY,studyInstanceUID,startDateTime,endDateTime,studyDescription);
		ctDose.setSourceOfDoseInformation(SourceOfDoseInformation.COPIED_FROM_IMAGE_ATTRIBUTES);
		
		CodedSequenceItem defaultAnatomy = null;
		{
			DisplayableAnatomicConcept anatomyConcept = CTAnatomy.findAnatomicConcept(list);
			if (anatomyConcept != null) {
				defaultAnatomy = anatomyConcept.getCodedSequenceItem();
			}
		}
		
		String defaultProtocolName = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ProtocolName);
		
		String defaultComment = null;	// do NOT want to use top level Series Description, since it describes the localizer or dose screen, not the acquisition
		
		// should do not need to parse text in CommentsOnRadiationDose, since everything in individual attributes
		// but get it just in case, since sometimes part is missing from (older) headers ...
		
		Map<String,String> DLPFromCommentsOnRadiationDoseIndexedBySeriesNumber = new HashMap<String,String>();
		Map<String,String> CTDIVolFromCommentsOnRadiationDoseIndexedBySeriesNumber = new HashMap<String,String>();
		String totalDLPFromCommentsOnRadiationDose = getValuesFromCommentsOnRadiationDose(list,DLPFromCommentsOnRadiationDoseIndexedBySeriesNumber,CTDIVolFromCommentsOnRadiationDoseIndexedBySeriesNumber,debugLevel);

		{
			String totalDLP = getPhilipsPrivateDLPValue(list);
			if (totalDLP == null || totalDLP.length() == 0) {
if (debugLevel > 0) System.err.println("Cannot get total DLP from private attribute - using value parsed from CommentsOnRadiationDose if present");
				totalDLP = totalDLPFromCommentsOnRadiationDose;
			}
			ctDose.setDLPTotal(totalDLP);
		}
				
		Attribute aExposureDoseSequence = list.get(TagFromName.ExposureDoseSequence);
		if (aExposureDoseSequence != null && aExposureDoseSequence instanceof SequenceAttribute) {
			//UIDGenerator u = new UIDGenerator();
		
			int seriesCounter = 1;	// use this in case SeriesNumber is missing (it is present for Philips, but not GE); may not be the true series number, but matches the events parsed from the comments
			Iterator ieds = ((SequenceAttribute)aExposureDoseSequence).iterator();
			while (ieds.hasNext()) {
				SequenceItem item = (SequenceItem)(ieds.next());
				if (item != null) {
					AttributeList itemList = item.getAttributeList();
if (debugLevel > 1) System.err.print(itemList);

	// Philips ...				
	// (0x0008,0x002a) DT Acquisition Date Time 	 VR=<DT>   VL=<0x0016>  <20090806081005.000000 > 
	// (0x0008,0x103e) LO Series Description 	 VR=<LO>   VL=<0x000c>  <70sec delay > 
	// (0x0018,0x0000) UL Group Length 	 VR=<UL>   VL=<0x0004>  [0x000000f4] 
	// (0x0018,0x0060) DS KVP 	 VR=<DS>   VL=<0x0004>  <120 > 
	// (0x0018,0x1150) IS Exposure Time 	 VR=<IS>   VL=<0x0006>  <15598 > 
	// (0x0018,0x115a) CS Radiation Mode 	 VR=<CS>   VL=<0x000a>  <CONTINUOUS> 
	// (0x0018,0x1160) SH Filter Type 	 VR=<SH>   VL=<0x0010>  <WEDGE_PREPATIENT> 
	// (0x0018,0x1302) IS Scan Length 	 VR=<IS>   VL=<0x0004>  <525 > 
	// (0x0018,0x7050) CS Filter Material 	 VR=<CS>   VL=<0x0016>  <TEFLON\TITANIUM 1_2MM > 
	// (0x0018,0x8151) DS X-Ray Tube Current in uA 	 VR=<DS>   VL=<0x000a>  <118989.614> 
	// (0x0018,0x9302) CS Acquisition Type 	 VR=<CS>   VL=<0x0006>  <SPIRAL> 
	// (0x0018,0x9306) FD Single Collimation Width 	 VR=<FD>   VL=<0x0008>  {0.625} 
	// (0x0018,0x9307) FD Total Collimation Width 	 VR=<FD>   VL=<0x0008>  {40} 
	// (0x0018,0x9311) FD Spiral Pitch Factor 	 VR=<FD>   VL=<0x0000>  {} 
	// (0x0018,0x9324) FD Estimated Dose Saving 	 VR=<FD>   VL=<0x0008>  {15.6102} 
	// (0x0018,0x9345) FD CTDIvol 	 VR=<FD>   VL=<0x0008>  {7.4263} 
	// (0x0020,0x0000) UL Group Length 	 VR=<UL>   VL=<0x0004>  [0x0000001a] 
	// (0x0020,0x0011) IS Series Number 	 VR=<IS>   VL=<0x0002>  <6 > 
	// (0x0020,0x1041) DS Slice Location 	 VR=<DS>   VL=<0x0008>  <-1659.0 > 
	// (0x00e1,0x0000) UL Group Length 	 VR=<UL>   VL=<0x0004>  [0x00000026] 
	// (0x00e1,0x0010) LO PrivateCreator 	 VR=<LO>   VL=<0x0008>  <ELSCINT1> 
	// (0x00e1,0x1021) DS ? 	 VR=<DS>   VL=<0x000a>  <434.382979> 
	
	// GE ...
	// (0x0018,0x0015) CS Body Part Examined 	 VR=<CS>   VL=<0x0006>  <CHEST > 
	// (0x0018,0x0060) DS KVP 	 VR=<DS>   VL=<0x0004>  <120 > 
	// (0x0018,0x1150) IS Exposure Time 	 VR=<IS>   VL=<0x0004>  <5530> 
	// (0x0018,0x8151) DS X-Ray Tube Current in uA 	 VR=<DS>   VL=<0x000a>  <432299.99 > 
	// (0x0018,0x9302) CS Acquisition Type 	 VR=<CS>   VL=<0x0006>  <SPIRAL> 
	// (0x0018,0x9306) FD Single Collimation Width 	 VR=<FD>   VL=<0x0008>  {0.62} 
	// (0x0018,0x9307) FD Total Collimation Width 	 VR=<FD>   VL=<0x0008>  {160} 
	// (0x0018,0x9311) FD Spiral Pitch Factor 	 VR=<FD>   VL=<0x0008>  {0.98} 
	// (0x0018,0x9345) FD CTDIvol 	 VR=<FD>   VL=<0x0008>  {24.33} 
	// (0x0018,0x9346) SQ CTDI Phantom Type Code Sequence 	 VR=<SQ>   VL=<0xffffffff>  
	//   (0x0008,0x0100) SH Code Value 	 VR=<SH>   VL=<0x0006>  <113691> 
	//   (0x0008,0x0102) SH Coding Scheme Designator 	 VR=<SH>   VL=<0x0004>  <DCM > 
	//   (0x0008,0x0104) LO Code Meaning 	 VR=<LO>   VL=<0x001a>  <IEC Body Dosimetry Phantom> 


					String acquisitionDateTime = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.AcquisitionDateTime);
					String seriesDescription = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.SeriesDescription);
					String protocolName = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.ProtocolName);
					String kvp = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.KVP);
					double exposureTimeInMilliSeconds = Attribute.getSingleDoubleValueOrDefault(itemList,TagFromName.ExposureTime,0d);
					String radiationMode = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.RadiationMode);
					String filterType = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.FilterType);
					String scanLength = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.ScanLength);
					double tubeCurrentInuA = Attribute.getSingleDoubleValueOrDefault(itemList,TagFromName.XRayTubeCurrentInuA,0d);
					String acquisitionType = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.AcquisitionType);
					String singleCollimationWidth = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.SingleCollimationWidth);
					String totalCollimationWidth = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.TotalCollimationWidth);
					String spiralPitchFactor = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.SpiralPitchFactor);
					String estimatedDoseSaving = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.EstimatedDoseSaving);
					String CTDIvol = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.CTDIvol);
					String seriesNumber = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.SeriesNumber);
					String sliceLocation = Attribute.getSingleStringValueOrEmptyString(itemList,TagFromName.SliceLocation);
					String DLP = getPhilipsPrivateDLPValue(itemList);
					
					// don't use value of 0 or it will be copied literally into SRs rather than omitted ...
					String exposureTimeInSeconds = exposureTimeInMilliSeconds > 0 ? FloatFormatter.toString(new Double(exposureTimeInMilliSeconds).doubleValue()/1000,Locale.US) : null;
					String tubeCurrentInmA = tubeCurrentInuA > 0 ? FloatFormatter.toString(new Double(tubeCurrentInuA).doubleValue()/1000,Locale.US) : null;
					
					String useSeriesNumber = seriesNumber;
					if (useSeriesNumber.length() == 0) {
						useSeriesNumber = Integer.toString(seriesCounter);
					}
					
					if (useSeriesNumber.length() > 0) {
						if (CTDIvol.length() == 0) {
if (debugLevel > 0) System.err.println("Cannot get per series CTDIvol from private attribute - using value parsed from CommentsOnRadiationDose if present");
							CTDIvol = CTDIVolFromCommentsOnRadiationDoseIndexedBySeriesNumber.get(useSeriesNumber);
							if (CTDIvol == null) {
								CTDIvol = "";
							}
						}
						if (DLP.length() == 0) {
if (debugLevel > 0) System.err.println("Cannot get per series DLP from private attribute - using value parsed from CommentsOnRadiationDose if present");
							DLP = DLPFromCommentsOnRadiationDoseIndexedBySeriesNumber.get(useSeriesNumber);
							if (DLP == null) {
								DLP = "";
							}
						}
					}
					
					// don't use value of 0 (e.g, from ExposureDoseSequence for localizer, or it will be copied literally into SRs rather than omitted ...
					if (CTDIvol.equals("0")) {
						CTDIvol = "";
					}
					if (DLP.equals("0")) {
						DLP = "";
					}
					
					CodedSequenceItem useAnatomy = defaultAnatomy;
					{
						DisplayableAnatomicConcept anatomyConcept = CTAnatomy.findAnatomicConcept(itemList);	// will us BodyPartExamined from GE
if (debugLevel > 0) System.err.println("anatomyConcept = "+anatomyConcept);
						if (anatomyConcept != null) {
							useAnatomy = anatomyConcept.getCodedSequenceItem();
						}
					}
					
					CTPhantomType usePhantom = null;
					{
						CodedSequenceItem csiCTDIPhantomTypeCodeSequence = CodedSequenceItem.getSingleCodedSequenceItemOrNull(itemList,TagFromName.CTDIPhantomTypeCodeSequence);
						if (csiCTDIPhantomTypeCodeSequence != null) {
							usePhantom = CTPhantomType.selectFromCode(csiCTDIPhantomTypeCodeSequence);
if (debugLevel > 0) System.err.println("usePhantom = "+usePhantom);
						}
					}
					
					String useProtocolName = defaultProtocolName;
					if (protocolName.length() > 0) {
						useProtocolName = protocolName;
					}
					
					String useComment = defaultComment;
					if (seriesDescription.length() > 0) {
						useComment = seriesDescription;
					}
					
if (debugLevel > 0) System.err.println("acquisitionType = "+acquisitionType);
					CTScanType useScanType = CTScanType.selectFromDescription(acquisitionType);
if (debugLevel > 0) System.err.println("useScanType = "+useScanType);
					// sometimes acquisitionType is missing, yet valid information is present, and there is (some) information provided for localizers
					{
						CTDoseAcquisition acq = new CTDoseAcquisition(studyInstanceUID,
							true/*isSeries*/,seriesNumber/* NOT counter in useSeriesNumber - doesn't match actual series in dose screen for GE */,
							useScanType,null/*only start scan range information*/,CTDIvol,DLP,usePhantom);

						String useScanningLength = null;
						String useLengthOfReconstructableVolume = null;
						if (useScanType.equals(CTScanType.LOCALIZER)) {
							useScanningLength = scanLength;
						}
						else {
							useScanningLength = scanLength;					// needs to be populated and NOT overridden by DLP-derived value if the latter is less, i.e., there is a gap for sequenced scans
							useLengthOfReconstructableVolume = scanLength;	// scanLength is from edge to edge not center to center; hence do not need to add slice thickness, etc.
						}
						CTAcquisitionParameters ap = new CTAcquisitionParameters(
							null/*u.getAnotherNewUID()*//*irradiationEventUID*/,
							useScanType,
							useAnatomy,
							useProtocolName,
							useComment,
							exposureTimeInSeconds,
							useScanningLength,
							useLengthOfReconstructableVolume,
							null/*exposedRangeInMM*/,
							null/*topZLocationOfReconstructableVolume*/,null/*bottomZLocationOfReconstructableVolume*/,null/*topZLocationOfScanningLength*/,null/*bottomZLocationOfScanningLength*/,
							null/*frameOfReferenceUID*/,
							singleCollimationWidth,totalCollimationWidth,spiralPitchFactor,
							kvp,tubeCurrentInmA,null/*no info about maximum tube current, cannot assume constant and do not want to override what is found scanning reconstructed slice headers*/,
							null/*exposureTimePerRotation*/);
						
if (debugLevel > 0) System.err.println("CTDIvol = "+CTDIvol);
if (debugLevel > 0) System.err.println("DLP = "+DLP);
						if (CTDIvol.length() > 0 && DLP.length() > 0) {
							// override Scan Length, which does not include overranging, if the DLP and CTDIvol informaiton is present (i.e., other than a localizer)
							// in order to match DICOM RDSR definition for sequenced scans if there is any skip, only override if greater (otherwise would be too small)
							ap.deriveScanningLengthFromDLPAndCTDIVolIfGreater(DLP,CTDIvol);
						}
						acq.setAcquisitionParameters(ap);
						ctDose.addAcquisition(acq);
					}
				}
				++seriesCounter;
			}
		}
		
		//if (eventDataFromImages != null) {
		//	for (int ai = 0; ai<ctDose.getNumberOfAcquisitions(); ++ai) {
		//		CTDoseAcquisition acq = ctDose.getAcquisition(ai);
		//		ScanRange scanRange = acq.getScanRange();
		//		// This will work as long as there are not more than one series with the same number and scan range :(
		//		String key = acq.getSeriesNumber()
		//				+"+"+scanRange.getStartDirection()+scanRange.getStartLocation()
		//				+"+"+scanRange.getEndDirection()+scanRange.getEndLocation();
		//		CTAcquisitionParameters ap = eventDataFromImages.getAcquisitionParameters(key);
		//		if (ap != null) {
		//			ap.deriveScanningLengthFromDLPAndCTDIVol(acq.getDLP(),acq.getCTDIvol());
		//			acq.setAcquisitionParameters(ap);
		//		}
		//	}
		//}
		
		if (buildSR) {
			GenerateRadiationDoseStructuredReport.createContextForNewRadiationDoseStructuredReportFromExistingInstance(list,ctDose,eventDataFromImages);
		}

		return ctDose;
	}
	
	/**
	 * <p>Extract the CT dose information from the Exposure Dose Sequence in a screen save or localizer image, correlate it with any acquired CT slice images.</p>
	 *
	 * @param	arg		an array of 1 to 4 strings - the file name of the dose screen save image (or "-" if to search for dose screen amongst acquired images),
	 *					then optionally the path to a DICOMDIR or folder containing acquired CT slice images (or "-" if none and more arguments)
	 *					then optionally the name of Dose SR file to write  (or "-" if none and more arguments)
	 *					then optionally the debug level
	 */
	public static final void main(String arg[]) {
		try {
			String screenFilename            = arg.length > 0  && !arg[0].equals("-") ? arg[0] : null;
			String acquiredImagesPath        = arg.length > 1  && !arg[1].equals("-") ? arg[1] : null;
			String srOutputFilename          = arg.length > 2 && !arg[2].equals("-") ? arg[2] : null;
			int    debugLevel                = arg.length > 3 ? Integer.parseInt(arg[3]) : -1;
//System.err.println("debugLevel = "+debugLevel);
		
			CTIrradiationEventDataFromImages eventDataFromImages = null;
			if (acquiredImagesPath != null) {
				eventDataFromImages = new CTIrradiationEventDataFromImages(acquiredImagesPath);
System.err.print(eventDataFromImages);
				if (screenFilename == null) {
					screenFilename = eventDataFromImages.getDoseScreenOrStructuredReportFilenames(true/*includeScreen*/,false/*includeSR*/).get(0);
				}
			}

			AttributeList list = new AttributeList();
			list.read(screenFilename);
//System.err.print(list);
			CTDose ctDose = getCTDoseFromExposureDoseSequence(list,debugLevel,eventDataFromImages,srOutputFilename != null);
System.err.print(ctDose.toString(true,true));
			if (!ctDose.specifiedDLPTotalMatchesDLPTotalFromAcquisitions()) {
				System.err.println("############ specified DLP total ("+ctDose.getDLPTotal()+") does not match DLP total from acquisitions ("+ctDose.getDLPTotalFromAcquisitions()+")");
			}
			
			if (srOutputFilename != null) {
				ctDose.write(srOutputFilename,null,ExposureDoseSequence.class.getCanonicalName());
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}

