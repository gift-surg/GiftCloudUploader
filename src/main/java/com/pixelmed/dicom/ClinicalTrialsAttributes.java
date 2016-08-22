/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.DateUtilities;

import java.util.*;

/**
 * <p>An abstract class of static methods to support removing identifying attributes and adding
 * Clinical Trials Patient, Study and Series Modules attributes.</p>
 *
 * <p>UID attributes are handled specially, in that they may be kept, removed or remapped. Remapping
 * means that any UID that is not standard (e.g., not a SOP Class, etc.) will be replaced consistently
 * with another generated UID, such that when that UID is encountered again, the same replacement
 * value will be used. The replacement mapping persists within the invocation of the JVM until it is explciitly
 * flushed. A different JVM invocation will replace the UIDs with different values. Therefore, multiple
 * instances that need to be remapped consistently must be cleaned within the same invocation.</p>
 *
 * <p>Note that this map could grow quite large and consumes resources in memory, and hence in a server
 * application should be flushed at appropriate intervals using the appropriate method.</p>
 *
 * @author	dclunie
 */
abstract public class ClinicalTrialsAttributes {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/ClinicalTrialsAttributes.java,v 1.70 2014/09/09 20:34:09 dclunie Exp $";
	
	protected static final String defaultValueForMissingNonZeroLengthStrings = "NONE";
	protected static final String defaultValueForMissingPossiblyZeroLengthStrings = "";
	
	protected static Map mapOfOriginalToReplacementUIDs = null;
	protected static UIDGenerator uidGenerator = null;	

	private ClinicalTrialsAttributes() {}

    protected static void addType1LongStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value == null || value.length() == 0) {
			value=defaultValueForMissingNonZeroLengthStrings;
		}
		Attribute a = new LongStringAttribute(t,specificCharacterSet);
		a.addValue(value);
		list.put(t,a);
	}

	protected static void addType2LongStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value == null) {
			value=defaultValueForMissingPossiblyZeroLengthStrings;
		}
		Attribute a = new LongStringAttribute(t,specificCharacterSet);
		a.addValue(value);
		list.put(t,a);
	}

	protected static void addType3ShortTextAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value != null) {
			Attribute a = new ShortTextAttribute(t,specificCharacterSet);
			a.addValue(value);
			list.put(t,a);
		}
	}

	protected static void addType3ShortStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value != null) {
			Attribute a = new ShortStringAttribute(t,specificCharacterSet);
			a.addValue(value);
			list.put(t,a);
		}
	}

	protected static void addType3LongStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value != null) {
			Attribute a = new LongStringAttribute(t,specificCharacterSet);
			a.addValue(value);
			list.put(t,a);
		}
	}

	protected static void addType3DateTimeAttribute(AttributeList list,AttributeTag t,String value) throws DicomException {
		if (value != null) {
			Attribute a = new DateTimeAttribute(t);
			a.addValue(value);
			list.put(t,a);
		}
	}

	/**
	 * <p>Add the Patient's Age derived from the Patient's Birth Date and study-related date.</p>
	 *
	 * <p>Does nothing if no value for Patient's Birth Date.</p>
	 *
	 * <p>Uses the Study, Series, Acquisition or Content Date in that order if present, else does nothing.</p>
	 *
	 * @param	list	the list of attributes in which to find the dob and date and to which to add the age
	 */
	public static void addAgeDerivedFromBirthDateAndStudyRelatedDate(AttributeList list) {
		String dob = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientBirthDate);
		if (dob.length() > 0) {
			String useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDate);
			if (useAsCurrentDateForAge.length() == 0) {
				useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesDate);
			}
			if (useAsCurrentDateForAge.length() == 0) {
				useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.AcquisitionDate);
			}
			if (useAsCurrentDateForAge.length() == 0) {
				useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ContentDate);
			}
			if (useAsCurrentDateForAge.length() > 0) {
				try {
					String age = DateUtilities.getAgeBetweenAsDICOMAgeString(dob,useAsCurrentDateForAge);
					if (age != null && age.length() > 0) {
						Attribute aPatientAge = new AgeStringAttribute(TagFromName.PatientAge);
						aPatientAge.addValue(age);
						list.put(aPatientAge);
					}
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * <p>Retains any existing items in Contributing Equipment Sequence.</p>
	 *
	 * <p>Uses <code>("109104","DCM","De-identifying Equipment")</code> for the Purpose of Reference.</p>
	 *
	 * <p>Uses <code>"Deidentified"</code> for the Contribution Description.</p>
	 *
	 * <p>Uses the current date and time for the Contribution DateTime.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion) throws DicomException {
		addContributingEquipmentSequence(list,true,new CodedSequenceItem("109104","DCM","De-identifying Equipment"),	// per CP 892
			manufacturer,institutionName,institutionalDepartmentName,institutionAddress,stationName,manufacturerModelName,deviceSerialNumber,softwareVersion,
			"Deidentified");
	}

	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	retainExistingItems				if true, retain any existing items in Contributing Equipment Sequence, otherwise remove them
	 * @param	purposeOfReferenceCodeSequence	the purpose of reference
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @param	contributionDescription			the contribution description
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,boolean retainExistingItems,
			CodedSequenceItem purposeOfReferenceCodeSequence,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion,
			String contributionDescription) throws DicomException {
		addContributingEquipmentSequence(list,true,purposeOfReferenceCodeSequence,
			manufacturer,institutionName,institutionalDepartmentName,institutionAddress,stationName,manufacturerModelName,deviceSerialNumber,softwareVersion,
			contributionDescription,DateTimeAttribute.getFormattedStringDefaultTimeZone(new java.util.Date()),
			null,null);
	}

	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	retainExistingItems				if true, retain any existing items in Contributing Equipment Sequence, otherwise remove them
	 * @param	purposeOfReferenceCodeSequence	the purpose of reference
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @param	contributionDescription			the contribution description
	 * @param	contributionDateTime			the contribution datetime
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,boolean retainExistingItems,
			CodedSequenceItem purposeOfReferenceCodeSequence,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion,
			String contributionDescription,
			String contributionDateTime) throws DicomException {
		addContributingEquipmentSequence(list,true,purposeOfReferenceCodeSequence,
			manufacturer,institutionName,institutionalDepartmentName,institutionAddress,stationName,manufacturerModelName,deviceSerialNumber,softwareVersion,
			contributionDescription,contributionDateTime,
			null,null);
	}
	
	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	retainExistingItems				if true, retain any existing items in Contributing Equipment Sequence, otherwise remove them
	 * @param	purposeOfReferenceCodeSequence	the purpose of reference
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @param	contributionDescription			the contribution description
	 * @param	contributionDateTime			the contribution datetime
	 * @param	operatorNames					an array of Strings of one or more operator's names, or null if not to be added
	 * @param	operatorIdentifications			an array of {@link com.pixelmed.dicom.PersonIdentification PersonIdentification}, or null if not to be added
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,boolean retainExistingItems,
			CodedSequenceItem purposeOfReferenceCodeSequence,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion,
			String contributionDescription,
			String contributionDateTime,
			String[] operatorNames,
			PersonIdentification[] operatorIdentifications) throws DicomException {

		Attribute aSpecificCharacterSet = list.get(TagFromName.SpecificCharacterSet);
		SpecificCharacterSet specificCharacterSet = aSpecificCharacterSet == null ? null : new SpecificCharacterSet(aSpecificCharacterSet.getStringValues());

		AttributeList newItemList = new AttributeList();
		
		if (purposeOfReferenceCodeSequence != null) {
			SequenceAttribute aPurposeOfReferenceCodeSequence = new SequenceAttribute(TagFromName.PurposeOfReferenceCodeSequence);
			aPurposeOfReferenceCodeSequence.addItem(purposeOfReferenceCodeSequence.getAttributeList());
			newItemList.put(aPurposeOfReferenceCodeSequence);
		}
		addType3LongStringAttribute (newItemList,TagFromName.Manufacturer,manufacturer,specificCharacterSet);
		addType3LongStringAttribute (newItemList,TagFromName.InstitutionName,institutionName,specificCharacterSet);
		addType3LongStringAttribute (newItemList,TagFromName.InstitutionalDepartmentName,institutionalDepartmentName,specificCharacterSet);
		addType3ShortTextAttribute  (newItemList,TagFromName.InstitutionAddress,institutionAddress,specificCharacterSet);
		addType3ShortStringAttribute(newItemList,TagFromName.StationName,stationName,specificCharacterSet);
		addType3LongStringAttribute (newItemList,TagFromName.ManufacturerModelName,manufacturerModelName,specificCharacterSet);
		addType3LongStringAttribute (newItemList,TagFromName.DeviceSerialNumber,deviceSerialNumber,specificCharacterSet);
		addType3LongStringAttribute (newItemList,TagFromName.SoftwareVersions,softwareVersion,specificCharacterSet);
		addType3ShortTextAttribute  (newItemList,TagFromName.ContributionDescription,contributionDescription,specificCharacterSet);
		addType3DateTimeAttribute   (newItemList,TagFromName.ContributionDateTime,contributionDateTime);
		
		if (operatorNames != null && operatorNames.length > 0) {
			Attribute aOperatorName = new PersonNameAttribute(TagFromName.OperatorsName);
			for (int i=0; i<operatorNames.length; ++i) {
				aOperatorName.addValue(operatorNames[i]);
			}
			newItemList.put(aOperatorName);
		}
		
		if (operatorIdentifications != null && operatorIdentifications.length > 0) {
			SequenceAttribute aOperatorIdentificationSequence = new SequenceAttribute(TagFromName.OperatorIdentificationSequence);
			for (int i=0; i<operatorIdentifications.length; ++i) {
				PersonIdentification operator = operatorIdentifications[i];
				if (operator != null) {
					aOperatorIdentificationSequence.addItem(new SequenceItem(operator.getAttributeList()));
				}
			}
			newItemList.put(aOperatorIdentificationSequence);
		}
		
		SequenceAttribute aContributingEquipmentSequence = null;
		if (retainExistingItems) {
			aContributingEquipmentSequence = (SequenceAttribute)list.get(TagFromName.ContributingEquipmentSequence);	// may be absent
		}
		if (aContributingEquipmentSequence == null) {
			aContributingEquipmentSequence = new SequenceAttribute(TagFromName.ContributingEquipmentSequence);
		}
		aContributingEquipmentSequence.addItem(newItemList);
		list.remove(TagFromName.ContributingEquipmentSequence);
		list.put(aContributingEquipmentSequence);
	}
	
	/**
	 * <p>Remove the attributes of the Clinical Trials Patient, Study and Series Modules, from a list of attributes.</p>
	 *
	 * @param	list	the list of attributes from which to remove the attributes
	 */
	public static void removeClinicalTrialsAttributes(AttributeList list) {
		list.remove(TagFromName.ClinicalTrialSponsorName);
		list.remove(TagFromName.ClinicalTrialProtocolID);
		list.remove(TagFromName.ClinicalTrialProtocolName);
		list.remove(TagFromName.ClinicalTrialSiteID);
		list.remove(TagFromName.ClinicalTrialSiteName);
		list.remove(TagFromName.ClinicalTrialSubjectID);
		list.remove(TagFromName.ClinicalTrialSubjectReadingID);
		list.remove(TagFromName.ClinicalTrialTimePointID);
		list.remove(TagFromName.ClinicalTrialTimePointDescription);
		list.remove(TagFromName.ClinicalTrialCoordinatingCenterName);
	}

	/**
	 * <p>Is a private tag safe.</p>
	 *
	 * <p>Safe private attributes are all those that are known not to contain individually identifiable information.</p>
	 *
	 * <p>Private creators are always considered safe.</p>
	 *
	 * @param	tag		the tag in question
	 * @param	list	the list in which the tag is contained from which the private creator can be extracted
	 * @return			true if safe
	 */
	public static boolean isSafePrivateAttribute(AttributeTag tag,AttributeList list) {
//System.err.println("ClinicalTrialsAttributes.isSafePrivateAttribute(): checking "+tag);
		boolean safe = false;
		if (tag.isPrivateCreator()) {
			safe = true;		// keep all creators, since may need them, and are harmless (and need them to check real private tags later)
		}
		else {
			String creator = list.getPrivateCreatorString(tag);
			safe = isSafePrivateAttribute(creator,tag);
		}
//System.err.println("ClinicalTrialsAttributes.isSafePrivateAttribute(): safe="+safe);
		return safe;
	}
	
	/**
	 * <p>Is a private tag safe.</p>
	 *
	 * <p>Safe private attributes are all those that are known not to contain individually identifiable information.</p>
	 *
	 * <p>Private creators are always considered safe, though there is no point in calling this method for private creator tags ... use AttributeTag.isPrivateCreator() instead.</p>
	 *
	 * @param	creator	the private creator of the block containing the tag
	 * @param	tag		the tag in question
	 * @return			true if safe
	 */
	public static boolean isSafePrivateAttribute(String creator,AttributeTag tag) {
//System.err.println("ClinicalTrialsAttributes.isSafePrivateAttribute(): checking "+tag+" creator "+creator);
		boolean safe = false;
		if (tag.isPrivateCreator()) {
			safe = true;
		}
		else {
			int group = tag.getGroup();
			int element = tag.getElement();
			int elementInBlock = element & 0x00ff;
			if (group == 0x7053 && creator.equals("Philips PET Private Group")) {
				if      (elementInBlock == 0x0000) {	// DS	SUV Factor - Multiplying stored pixel values by Rescale Slope then this factor results in SUVbw in g/l
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// DS	Activity Concentration Factor - Multiplying stored pixel values by Rescale Slope then this factor results in MBq/ml.
					safe = true;
				}
			}
			else if (group == 0x2005 && (creator.equals("Philips MR Imaging DD 001") || creator.equals("PHILIPS MR IMAGING DD 001"))) {
				if      (elementInBlock == 0x000d) {	// FL	Scale Intercept
					safe = true;
				}
				else if (elementInBlock == 0x000e) {	// FL	Scale Slope
					safe = true;
				}
			}
			else if (group == 0x00E1 && creator.equals("ELSCINT1")) {
				if      (elementInBlock == 0x0021) {	// DS	DLP
					safe = true;
				}
			}
			else if (group == 0x01E1 && creator.equals("ELSCINT1")) {
				if      (elementInBlock == 0x0026) {	// CS	Phantom Type
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// DS	Acquisition Duration
					safe = true;
				}
			}
			else if (group == 0x01F1 && creator.equals("ELSCINT1")) {
				if      (elementInBlock == 0x0001) {	// CS	Acquisition Type
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// DS	Table Velocity
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// DS	Pitch
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// DS	Rotation Time
					safe = true;
				}
			}
			else if (group == 0x0019 && creator.equals("GEMS_ACQU_01")) {
				if      (elementInBlock == 0x0023) {	// DS	Table Speed [mm/rotation]
					safe = true;
				}
				else if (elementInBlock == 0x0024) {	// DS	Mid Scan Time [sec]
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// DS	Rotation Speed (Gantry Period)
					safe = true;
				}
				else if (elementInBlock == 0x009e) {	// LO	Internal Pulse Sequence Name
					safe = true;
				}
			}
			else if (group == 0x0025 && creator.equals("GEMS_SERS_01")) {
				if      (elementInBlock == 0x0007) {	// SL	Images In Series
					safe = true;
				}
			}
			else if (group == 0x0043 && creator.equals("GEMS_PARM_01")) {
				if      (elementInBlock == 0x0027) {	// SH	Scan Pitch Ratio in the form "n.nnn:1"
					safe = true;
				}
				else if (elementInBlock == 0x006f) {	// DS	Scanner Table Entry + Gradient Coil Selected (VM is 3 or 4)
					safe = true;
				}
			}
			else if (group == 0x0045 && creator.equals("GEMS_HELIOS_01")) {
				if      (elementInBlock == 0x0001) {	// SS	Number of Macro Rows in Detector
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// FL	Macro width at ISO Center
					safe = true;
				}
			}
			else if (group == 0x0903 && creator.equals("GEIIS PACS")) {
				if      (elementInBlock == 0x0010) {	// US	Reject Image Flag
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// US	Significant Flag
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// US	Confidential Flag
					safe = true;
				}
			}
			else if ((group == 0x7e01 || group == 0x7f01) && creator.equals("HOLOGIC, Inc.")) {
				if      (elementInBlock == 0x0001) {	// LO	Codec Version
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// SH	Codec Content Type
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// SQ	High Resolution Data Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// SQ	Low Resolution Data Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// OB	Codec Content
					safe = true;
				}
			}
			else if (group == 0x0019 && creator.equals("LORAD Selenia")) {
				if      (elementInBlock == 0x0006) {	// LO	Paddle ID
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// SH	Paddle Position
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	Collimation Size
					safe = true;
				}
				else if (elementInBlock == 0x0016) {	// DS	Paddle Angle
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// LO	Paddle ID Description
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// SH	Paddle Position Description
					safe = true;
				}
				else if (elementInBlock == 0x0028) {	// LO	Collimation Size Description
					safe = true;
				}
				else if (elementInBlock == 0x0029) {	// LO	AEC User Density Scale Factor Description
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// US	AEC User Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0031) {	// US	AEC System Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0032) {	// US	AEC Calculated mAs
					safe = true;
				}
				else if (elementInBlock == 0x0033) {	// US	AEC Auto Pixel 1
					safe = true;
				}
				else if (elementInBlock == 0x0034) {	// US	AEC Auto Pixel 2
					safe = true;
				}
				else if (elementInBlock == 0x0035) {	// US	AEC Sensor
					safe = true;
				}
				else if (elementInBlock == 0x0037) {	// LO	NPT Mode
					safe = true;
				}
				else if (elementInBlock == 0x0040) {	// DS	Skin Edge
					safe = true;
				}
				else if (elementInBlock == 0x0041) {	// DS	Exposure Index
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// DS	Display Minimum OD
					safe = true;
				}
				else if (elementInBlock == 0x0051) {	// DS	Dispaly Maximum OD
					safe = true;
				}
				else if (elementInBlock == 0x0052) {	// IS	Display Minimum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0053) {	// IS	Display Maximum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0060) {	// LT	Geometry Calibration
					safe = true;
				}
				else if (elementInBlock == 0x0070) {	// LO	Frame of Reference ID
					safe = true;
				}
				else if (elementInBlock == 0x0071) {	// CS	Paired Position
					safe = true;
				}
				else if (elementInBlock == 0x0080) {	// SH	Detector Image Offset
					safe = true;
				}
				else if (elementInBlock == 0x0090) {	// DS	Conventional Tomo Angle
					safe = true;
				}
			}
			else if (group == 0x0019 && creator.equals("HOLOGIC, Inc.")) {
				if      (elementInBlock == 0x0006) {	// LO	Paddle ID
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// SH	Paddle Position
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	Collimation Size
					safe = true;
				}
				else if (elementInBlock == 0x0016) {	// DS	Paddle Angle
					safe = true;
				}
				else if (elementInBlock == 0x0025) {	// SH	? but always observed to be safe string like "NORMAL"
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// LO	Paddle ID Description
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// SH	Paddle Position Description
					safe = true;
				}
				else if (elementInBlock == 0x0028) {	// LO	Collimation Size Description
					safe = true;
				}
				else if (elementInBlock == 0x0029) {	// LO	AEC User Density Scale Factor Description
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// US	AEC User Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0031) {	// US	AEC System Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0032) {	// US	AEC Calculated mAs
					safe = true;
				}
				else if (elementInBlock == 0x0033) {	// US	AEC Auto Pixel 1
					safe = true;
				}
				else if (elementInBlock == 0x0034) {	// US	AEC Auto Pixel 2
					safe = true;
				}
				else if (elementInBlock == 0x0035) {	// US	AEC Sensor
					safe = true;
				}
				else if (elementInBlock == 0x0037) {	// LO	NPT Mode
					safe = true;
				}
				else if (elementInBlock == 0x0040) {	// DS	Skin Edge
					safe = true;
				}
				else if (elementInBlock == 0x0041) {	// DS	Exposure Index
					safe = true;
				}
				else if (elementInBlock == 0x0042) {	// IS	Exposure Index Target
					safe = true;
				}
				else if (elementInBlock == 0x0043) {	// DS	Short Index Ratio
					safe = true;
				}
				else if (elementInBlock == 0x0044) {	// DS	Scout kVp
					safe = true;
				}
				else if (elementInBlock == 0x0045) {	// IS	Scout mA
					safe = true;
				}
				else if (elementInBlock == 0x0046) {	// IS	Scout mAs
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// DS	Display Minimum OD
					safe = true;
				}
				else if (elementInBlock == 0x0051) {	// DS	Dispaly Maximum OD
					safe = true;
				}
				else if (elementInBlock == 0x0052) {	// IS	Display Minimum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0053) {	// IS	Display Maximum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0060) {	// LT	Geometry Calibration
					safe = true;
				}
				else if (elementInBlock == 0x0061) {	// OB	3D IP Parameters
					safe = true;
				}
				else if (elementInBlock == 0x0062) {	// LO	2D IP Parameters
					safe = true;
				}
				else if (elementInBlock == 0x0070) {	// LO	Frame of Reference ID
					safe = true;
				}
				else if (elementInBlock == 0x0071) {	// CS	Paired Position
					safe = true;
				}
				else if (elementInBlock == 0x0080) {	// SH	Detector Image Offset
					safe = true;
				}
				else if (elementInBlock == 0x0085) {	// SH	Image Source
					safe = true;
				}
				else if (elementInBlock == 0x0087) {	// LO	Marker Text (this seems to be safe, since fixed string like LCC, not operator entered free text)
					safe = true;
				}
				else if (elementInBlock == 0x0089) {	// DS	Marker Location
					safe = true;
				}
				else if (elementInBlock == 0x008A) {	// SQ	Marker Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0090) {	// DS	Conventional Tomo Angle
					safe = true;
				}
				else if (elementInBlock == 0x0097) {	// SH	Markers Burned Into Image
					safe = true;
				}
				else if (elementInBlock == 0x0098) {	// LO	Grid Line Correction
					safe = true;
				}
			}
			else if (group == 0x0099 && creator.equals("NQHeader")) {
				if      (elementInBlock == 0x0001) {	// UI	Version
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// SS	ReturnCode
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// LT	ReturnMessage
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// FL	MI
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// SH	Units
					safe = true;
				}
				else if (elementInBlock == 0x0021) {	// FL	ICV
					safe = true;
				}
			}
			else if (group == 0x0199 && creator.equals("NQLeft")) {
				if (elementInBlock >= 0x0001 && elementInBlock <= 0x003a) {
					safe = true;
				}
			}
			else if (group == 0x0299 && creator.equals("NQRight")) {
				if (elementInBlock >= 0x0001 && elementInBlock <= 0x003a) {
					safe = true;
				}
			}
		}
		return safe;
	}
	
	/**
	 * <p>Is a private tag a safe sequence VR that needs to be read as SQ if UN.</p>
	 *
	 *
	 * @param	creator	the private creator of the block containing the tag
	 * @param	tag		the tag in question
	 * @return			true if a safe SQ private tag
	 */
	public static boolean isSafePrivateSequenceAttribute(String creator,AttributeTag tag) {
//System.err.println("ClinicalTrialsAttributes.isSafePrivateSequenceAttribute(): checking "+tag+" creator "+creator);
		boolean safeSequence = false;
		if (!tag.isPrivateCreator()) {
			int group = tag.getGroup();
			int element = tag.getElement();
			int elementInBlock = element & 0x00ff;
			// do NOT include (0x7e01/0x7f01,0x0010/0x0011) High/Low Resolution Data Sequence,
			// since if supplied in UN VR will be IVRLE not EVRLE encoded and parsing them as SQ will fail
			// and we have no need to recurse into them checking for unsafe private elements anyway
			if (group == 0x0019 && creator.equals("HOLOGIC, Inc.")) {
				if (elementInBlock == 0x008A) {	// SQ	Marker Sequence
					safeSequence = true;
				}
			}
		}
		return safeSequence;
	}
	
	/**
	 * <p>Flush (remove all entries in) the map of original UIDs to replacement UIDs.</p>
	 */
	public static void flushMapOfUIDs() {
		mapOfOriginalToReplacementUIDs = null;
	}
	
	public class HandleUIDs {
		public static final int keep = 0;
		public static final int remove = 1;
		public static final int remap = 2;
	}

	/**
	 * <p>Remap UID attributes in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void remapUIDAttributes(AttributeList list) throws DicomException {
		removeOrRemapUIDAttributes(list,HandleUIDs.remap);
	}
	
	/**
	 * <p>Remove UID attributes in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeUIDAttributes(AttributeList list) throws DicomException {
		removeOrRemapUIDAttributes(list,HandleUIDs.remove);
	}
	
	/**
	 * <p>Remove or remap UID attributes in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	handleUIDs	remove or remap the UIDs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	protected static void removeOrRemapUIDAttributes(AttributeList list,int handleUIDs) throws DicomException {
		// iterate through list to remove all UIDs, and recursively iterate through any sequences ...
		LinkedList forRemovalOrRemapping = null;
		Iterator i = list.values().iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				Iterator items = a.iterator();
				if (items != null) {
					while (items.hasNext()) {
						SequenceItem item = (SequenceItem)(items.next());
						if (item != null) {
							AttributeList itemAttributeList = item.getAttributeList();
							if (itemAttributeList != null) {
								removeOrRemapUIDAttributes(itemAttributeList,handleUIDs);
							}
						}
					}
				}
			}
			else if (handleUIDs != HandleUIDs.keep && o instanceof UniqueIdentifierAttribute) {
				// remove all UIDs except those that are not instance-related
				UniqueIdentifierAttribute a = (UniqueIdentifierAttribute)o;
				AttributeTag tag = a.getTag();
//if (tag.equals(TagFromName.SOPInstanceUID)) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): encountered SOP Instance UID"); }
				if (UniqueIdentifierAttribute.isTransient(tag)) {
					if (forRemovalOrRemapping == null) {
						forRemovalOrRemapping = new LinkedList();
					}
					forRemovalOrRemapping.add(tag);
//if (tag.equals(TagFromName.SOPInstanceUID)) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): added SOP Instance UID to list"); }
				}
			}
		}
		if (forRemovalOrRemapping != null) {
			Iterator i2 = forRemovalOrRemapping.iterator();
			while (i2.hasNext()) {
				AttributeTag tag = (AttributeTag)(i2.next());
				if (handleUIDs == HandleUIDs.remove) {
					list.remove(tag);
				}
				else if (handleUIDs == HandleUIDs.remap) {
					String originalUIDValue = Attribute.getSingleStringValueOrNull(list,tag);
//if (tag.equals(TagFromName.SOPInstanceUID)) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): requesting replacement of SOP Instance UID "+originalUIDValue); }
					if (originalUIDValue != null) {
						String replacementUIDValue = null;
						if (mapOfOriginalToReplacementUIDs == null) {
							mapOfOriginalToReplacementUIDs = new HashMap();
						}
						replacementUIDValue = (String)(mapOfOriginalToReplacementUIDs.get(originalUIDValue));
						if (replacementUIDValue == null) {
							if (uidGenerator == null) {
								uidGenerator = new UIDGenerator();
							}
							replacementUIDValue = uidGenerator.getAnotherNewUID();
							mapOfOriginalToReplacementUIDs.put(originalUIDValue,replacementUIDValue);
						}
						assert replacementUIDValue != null;
						list.remove(tag);
						Attribute a = new UniqueIdentifierAttribute(tag);
						a.addValue(replacementUIDValue);
						list.put(tag,a);
//if (tag.equals(TagFromName.SOPInstanceUID)) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): replacing SOP Instance UID "+originalUIDValue+" with "+replacementUIDValue); }
					}
					else {
						// we have a problem ... just remove it to be safe
						list.remove(tag);
					}
				}
			}
		}
	}
	
	public class HandleDates {
		public static final int keep = 0;
		public static final int remove = 1;
		public static final int modify = 2;
	}
	
	protected static AttributeTag getTagOfTimeAttributeCorrespondingToDateAttribute(AttributeTag dateTag) {
		AttributeTag timeTag = null;
		if (dateTag != null) {
			DicomDictionary dictionary = AttributeList.getDictionary();
			String dateName = dictionary.getNameFromTag(dateTag);
			if (dateName != null) {
				String timeName = dateName.replace("Date","Time");
				if (!timeName.equals(dateName)) {
					timeTag = dictionary.getTagFromName(timeName);
				}
			}
		}		
		return timeTag;
	}
	
	protected static boolean isValidCandidateForEarliestDateTime(Date candidate) {
		return candidate != null && candidate.getTime() >= 0;	// exclude ridiculous dates that are obvious dummy values (e.g., Applicare Presentation States with Annotation Creation Date of 18991230)
	}
	
	/**
	 * <p>Get the earliest patient event related date and time.</p>
	 *
	 * <p>Ignores equipment related dates like calibration, patient related dates like birth date, and non-patient instance related dates like effective and information issue.</p>
	 *
	 * @param	list							the list of attributes from the top level data set
	 * @param	earliestSoFar					for recursion
	 * @param	dateToUseForUnaccompaniedTimes	the date to use for time attributes that don't have a date sibling
	 * @return									the earliest date and time
	 */
	protected static Date findEarliestDateTime(AttributeList list,Date earliestSoFar,String dateToUseForUnaccompaniedTimes) {
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Start with earliestSoFar "+earliestSoFar);
		// iterate through list to process all Date, Time and DateTime attributes, and recursively iterate through any sequences ...
		Iterator<Attribute> i = list.values().iterator();
		while (i.hasNext()) {
			Attribute o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				Iterator items = a.iterator();
				if (items != null) {
					while (items.hasNext()) {
						SequenceItem item = (SequenceItem)(items.next());
						if (item != null) {
							AttributeList itemAttributeList = item.getAttributeList();
							if (itemAttributeList != null) {
								Date candidate = findEarliestDateTime(itemAttributeList,earliestSoFar,dateToUseForUnaccompaniedTimes);
								if (isValidCandidateForEarliestDateTime(candidate) && (earliestSoFar == null || candidate.before(earliestSoFar))) {
									earliestSoFar = candidate;
								}
							}
						}
					}
				}
			}
			else {
				String dateString = null;
				AttributeTag tag = o.getTag();
				if (o instanceof DateTimeAttribute
				   && !tag.equals(TagFromName.HangingProtocolCreationDateTime)		// exclude non-event related dates
				   && !tag.equals(TagFromName.EffectiveDateTime)
				   && !tag.equals(TagFromName.InformationIssueDateTime)
				) {
					dateString = ((DateTimeAttribute)o).getSingleStringValueOrEmptyString();
				}
				else if (o instanceof DateAttribute
						&& !tag.equals(TagFromName.PatientBirthDate)				// exclude non-event related dates
						&& !tag.equals(TagFromName.LastMenstrualDate)				// is sort of event related, but not desirable as the earliest
						&& !tag.equals(TagFromName.DateOfLastCalibration)
						&& !tag.equals(TagFromName.DateOfLastDetectorCalibration)
					) {
					dateString = ((DateAttribute)o).getSingleStringValueOrEmptyString();
					if (dateString.length() == 8) {
						AttributeTag timeTag = getTagOfTimeAttributeCorrespondingToDateAttribute(tag);
						if (timeTag != null) {
							String timeString = Attribute.getSingleStringValueOrEmptyString(list,timeTag);
							if (timeString.length() > 0) {
								dateString = dateString + timeString;
							}
						}
					}
					else {
						dateString = null;
					}
				}
				else if (o instanceof TimeAttribute && o.getVL() > 0) {
					// need to handle time only attributes that have values and are relative to some other date (such as AcquisitionDate (NM) or SeriesDate (PET) or unspecified)
					// very important that this list match exactly what is used in removeOrRemapDateAndTimeAttributes, otherwise risk crossing midnight
					if (tag.equals(TagFromName.RadiopharmaceuticalStartTime)
					 || tag.equals(TagFromName.RadiopharmaceuticalStopTime)
					 || tag.equals(TagFromName.InterventionDrugStartTime)
					 || tag.equals(TagFromName.InterventionDrugStopTime)
					 || tag.equals(TagFromName.ContrastBolusStartTime)
					 || tag.equals(TagFromName.ContrastBolusStopTime)) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): "+o);
						if (dateToUseForUnaccompaniedTimes != null && dateToUseForUnaccompaniedTimes.length() == 8) {
							dateString = dateToUseForUnaccompaniedTimes + o.getSingleStringValueOrEmptyString();
						}
					}
				}
				
				if (dateString != null && dateString.length() >= 8) {
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Attribute "+o);
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Checking dateString "+dateString);
					try {
						Date candidate = DateTimeAttribute.getDateFromFormattedString(dateString);
						if (isValidCandidateForEarliestDateTime(candidate) && (earliestSoFar == null || candidate.before(earliestSoFar))) {
							earliestSoFar = candidate;
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Checking earliestSoFar updated to "+earliestSoFar);
						}
					}
					catch (java.text.ParseException Exception) {
					}
				}
			}
		}
		return earliestSoFar;
	}

	/**
	 * <p>Get the earliest patient event related date and time.</p>
	 *
	 * <p>Ignores equipment related dates like calibration, patient related dates like birth date, and non-patient instance related dates like effective and information issue.</p>
	 *
	 * @param	list							the list of attributes from the top level data set
	 * @return									the earliest date and time
	 */
	public static Date findEarliestDateTime(AttributeList list) {
		String dateToUseForUnaccompaniedTimes = getDateToUseForUnaccompaniedTimes(list);
		return findEarliestDateTime(list,null/*earliestSoFar*/,dateToUseForUnaccompaniedTimes);
	}
	
	protected static Date getDateOffsetByEarliestMovedToEpoch(Date existingDate,Date epochForDateModification,Date earliest) {
		long existingTime = existingDate.getTime();
		long epochTime = epochForDateModification.getTime();
		long earliestTime = earliest.getTime();
		long newTime = existingTime - earliestTime + epochTime;
		Date newDate = new Date(newTime);
		return newDate;
	}
	
	/**
	 * <p>Get the date to use for time attributes that don't have a date sibling and which may be nested in sequences.</p>
	 *
	 * <p>E.g., to use with RadiopharmaceuticalStartTime.</p>
	 *
	 * @param	list	the list of attributes from the top level data set
	 * @return			a date to use, or an empty string if none
	 */
	protected static String getDateToUseForUnaccompaniedTimes(AttributeList list) {
		String dateString = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.AcquisitionDate);
		if (dateString.length() == 0) {
			dateString = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ContentDate);
		}
		if (dateString.length() == 0) {
			dateString = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesDate);
		}
		if (dateString.length() == 0) {
			dateString = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDate);
		}
//System.err.println("ClinicalTrialsAttributes.getDateToUseForUnaccompaniedTimes(): using  "+dateString);
		return dateString;
	}

	/**
	 * <p>Remove or remap Date, Time and DateTime attributes (other than Patient Birth) in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list							the list of attributes to be cleaned up
	 * @param	handleDates						keep, remove or modify dates and times
	 * @param	epochForDateModification		used if handleDates is modify dates, otherwise null
	 * @param	earliest						used to determine the offset from the epoch to remove from all dates and times - MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @param	dateToUseForUnaccompaniedTimes	used for time attributes that don't have a date sibling and which may be nested in sequences, e.g., RadiopharmaceuticalStartTime
	 * @throws	DicomException	if error in DICOM encoding
	 */
	protected static void removeOrRemapDateAndTimeAttributes(AttributeList list,int handleDates,Date epochForDateModification,Date earliest,String dateToUseForUnaccompaniedTimes) throws DicomException {
		AttributeList replacementList = new AttributeList();			// to prevent concurrent modification
		List<AttributeTag> removeList = new LinkedList<AttributeTag>();	// to prevent concurrent modification
		// iterate through list to process all Date, Time and DateTime attributes, and recursively iterate through any sequences ...
		Iterator<Attribute> i = list.values().iterator();
		while (i.hasNext()) {
			Attribute o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				Iterator items = a.iterator();
				if (items != null) {
					while (items.hasNext()) {
						SequenceItem item = (SequenceItem)(items.next());
						if (item != null) {
							AttributeList itemAttributeList = item.getAttributeList();
							if (itemAttributeList != null) {
								removeOrRemapDateAndTimeAttributes(itemAttributeList,handleDates,epochForDateModification,earliest,dateToUseForUnaccompaniedTimes);
							}
						}
					}
				}
			}
			else if (handleDates == HandleDates.remove) {
				if (o instanceof DateAttribute || o instanceof TimeAttribute || o instanceof DateTimeAttribute) {
					AttributeTag tag = o.getTag();
					if (tag.equals(TagFromName.StudyDate) || tag.equals(TagFromName.ContentDate)) {			// PS 3.15 profile Z (Content is actually Z/D :( )
						{ Attribute a = new DateAttribute(tag); replacementList.put(a); }
					}
					else if (tag.equals(TagFromName.StudyTime) || tag.equals(TagFromName.ContentTime)) {	// PS 3.15 profile Z (Content is actually Z/D :( )
						{ Attribute a = new TimeAttribute(tag); replacementList.put(a); }
					}
					else if (tag.equals(TagFromName.PatientBirthDate) || tag.equals(TagFromName.PatientBirthTime)) {
						// do nothing ... handled seperately
					}
					else {																					// PS 3.15 profile X (Series Date, Time are actually X/D ... just assume X :( )
						removeList.add(tag);
					}
				}
			}
			else if (handleDates == HandleDates.modify) {
				AttributeTag tag = o.getTag();
				if (o instanceof DateTimeAttribute) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): Doing DT "+o);
					String dateString = o.getSingleStringValueOrEmptyString();
					if (dateString.length() > 0) {
						try {
							Date existingDate = DateTimeAttribute.getDateFromFormattedString(dateString);
							if (existingDate != null) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): was DateTime "+dateString+" ("+existingDate+")");
								Date newDate = getDateOffsetByEarliestMovedToEpoch(existingDate,epochForDateModification,earliest);
								String newDateString = DateTimeAttribute.getFormattedString(newDate,TimeZone.getTimeZone("GMT"));
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): now "+newDateString+" ("+newDate+")");
								{ Attribute a = new DateTimeAttribute(tag); a.addValue(newDateString); replacementList.put(a); }
							}
							else {
								// problem ... set to zero length
								{ Attribute a = new DateTimeAttribute(tag); replacementList.put(a); }
							}
						}
						catch (java.text.ParseException Exception) {
							// problem ... set to zero length
							{ Attribute a = new DateTimeAttribute(tag); replacementList.put(a); }
						}
					}
					// else leave zero length DT alone
				}
				else if (o instanceof DateAttribute && !tag.equals(TagFromName.PatientBirthDate)) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): Doing DA "+o);
					String dateString = o.getSingleStringValueOrEmptyString();
					if (dateString.length() == 8) {
						AttributeTag timeTag = getTagOfTimeAttributeCorrespondingToDateAttribute(((DateAttribute)o).getTag());
						if (timeTag != null) {
							String timeString = Attribute.getSingleStringValueOrDefault(list,timeTag,"000000");	// important to have default, since still need to do date even if no time
							if (timeString.length() > 0) {
								dateString = dateString + timeString;
								try {
									Date existingDate = DateTimeAttribute.getDateFromFormattedString(dateString);
									if (existingDate != null) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): was Date and Time "+dateString+" ("+existingDate+")");
										Date newDate = getDateOffsetByEarliestMovedToEpoch(existingDate,epochForDateModification,earliest);
										String newDateString = DateTimeAttribute.getFormattedString(newDate,TimeZone.getTimeZone("GMT"),false/*tzSuffix*/);
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): now "+newDateString+" ("+newDate+")");
										{ Attribute a = new DateAttribute(tag);     a.addValue(newDateString.substring(0,8)); replacementList.put(a); }
										{ Attribute a = new TimeAttribute(timeTag); a.addValue(newDateString.substring(8)); replacementList.put(a); }
									}
								}
								catch (java.text.ParseException Exception) {
									// problem ... set to zero length
									{ Attribute a = new DateAttribute(tag);     replacementList.put(a); }
									{ Attribute a = new TimeAttribute(timeTag); replacementList.put(a); }
								}
							}
						}
					}
				}
				else if (o instanceof TimeAttribute && o.getVL() > 0) {
					// need to handle time only attributes that have values and are relative to some other date (such as AcquisitionDate (NM) or SeriesDate (PET) or unspecified)
					// very important that this list match exactly what is used in findEarliestDateTime, otherwise risk crossing midnight
					if (tag.equals(TagFromName.RadiopharmaceuticalStartTime)
					 || tag.equals(TagFromName.RadiopharmaceuticalStopTime)
					 || tag.equals(TagFromName.InterventionDrugStartTime)
					 || tag.equals(TagFromName.InterventionDrugStopTime)
					 || tag.equals(TagFromName.ContrastBolusStartTime)
					 || tag.equals(TagFromName.ContrastBolusStopTime)) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): Doing TM "+o);
						if (dateToUseForUnaccompaniedTimes.length() == 8) {
							String dateString = dateToUseForUnaccompaniedTimes + o.getSingleStringValueOrEmptyString();
							try {
								Date existingDate = DateTimeAttribute.getDateFromFormattedString(dateString);
								if (existingDate != null) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): was Time only special handling "+dateString+" ("+existingDate+")");
									Date newDate = getDateOffsetByEarliestMovedToEpoch(existingDate,epochForDateModification,earliest);
									String newDateString = DateTimeAttribute.getFormattedString(newDate,TimeZone.getTimeZone("GMT"),false/*tzSuffix*/);
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): now "+newDateString+" ("+newDate+")");
									{ Attribute a = new TimeAttribute(tag); a.addValue(newDateString.substring(8)); replacementList.put(a); }
								}
							}
							catch (java.text.ParseException Exception) {
								// problem ... leave it alone - not ideal, but not identity leak, so no need to zero it or remove it
							}
						}
						// else leave it alone - not ideal, but not identity leak, so no need to zero it or remove it
					}
				}
			}
		}
		for (AttributeTag t : removeList) {
			list.remove(t);
		}
		list.putAll(replacementList);
	}

	/**
	 * <p>Deidentify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @deprecated	retained for compatibility with previous releases; does NOT keep patient characteristics (such as might be needed for PET SUV calculations); use {@link #removeOrNullIdentifyingAttributes(AttributeList,boolean,boolean,boolean) removeOrNullIdentifyingAttributes()} instead
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	keepUIDs	if true, keep the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,boolean keepUIDs,boolean keepDescriptors) throws DicomException {
		removeOrNullIdentifyingAttributes(list,keepUIDs,keepDescriptors,false/*keepPatientCharacteristics*/);
	}

	/**
	 * <p>Deidentify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	keepUIDs	if true, keep the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,boolean keepUIDs,boolean keepDescriptors,boolean keepPatientCharacteristics) throws DicomException {
		removeOrNullIdentifyingAttributes(list,keepUIDs ? HandleUIDs.keep : HandleUIDs.remove,keepDescriptors,keepPatientCharacteristics);
	}
	
	/**
	 * <p>Deidentify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	handleUIDs	keep, remove or remap the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepPatientCharacteristics) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,false/*keepSeriesDescriptors*/,keepPatientCharacteristics,false/*keepDeviceIdentity*/,false/*keepInstitutionIdentity*/);
	}

	/**
	 * <p>Deidentify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	handleUIDs	keep, remove or remap the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors	if true, keep the series description even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity	if true, keep device identity
	 * @param	keepInstitutionIdentity	if true, keep device identity
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,HandleDates.keep,null/*epochForDateModification*/,null/*earliestDateInSet*/);
	}
	
	/**
	 * <p>Deidentify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Handles dates and times as requested (other than Patient Birth), including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep device identity
	 * @param	handleDates					keep, remove or modify dates and times
	 * @param	epochForDateModification	the epoch to which to move the earliest date, used if handleDates is modify dates, otherwise null
	 * @param	earliestDateInSet			the known earliest date to move to the specified epoch, used if handleDates is modify dates, otherwise null; if null, the earliest in the supplied list is used; MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,int handleDates,Date epochForDateModification,Date earliestDateInSet) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,false/*keepProtocolName*/,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,HandleDates.keep,null/*epochForDateModification*/,null/*earliestDateInSet*/);
	}
	
	/**
	 * <p>Deidentify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Handles dates and times as requested (other than Patient Birth), including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepProtocolName			if true, keep protocol name even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep device identity
	 * @param	handleDates					keep, remove or modify dates and times
	 * @param	epochForDateModification	the epoch to which to move the earliest date, used if handleDates is modify dates, otherwise null
	 * @param	earliestDateInSet			the known earliest date to move to the specified epoch, used if handleDates is modify dates, otherwise null; if null, the earliest in the supplied list is used; MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,int handleDates,Date epochForDateModification,Date earliestDateInSet) throws DicomException {
		removeOrNullIdentifyingAttributesRecursively(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity);
		
		if (handleUIDs != HandleUIDs.keep) {
			removeOrRemapUIDAttributes(list,handleUIDs);
		}
		
		if (handleDates != HandleDates.keep) {
			String dateToUseForUnaccompaniedTimes = getDateToUseForUnaccompaniedTimes(list);
			if (earliestDateInSet == null) {
				earliestDateInSet = findEarliestDateTime(list,null/*earliestSoFar*/,dateToUseForUnaccompaniedTimes);
			}
			removeOrRemapDateAndTimeAttributes(list,handleDates,epochForDateModification,earliestDateInSet,dateToUseForUnaccompaniedTimes);
		}
		
		{ AttributeTag tag = TagFromName.PatientIdentityRemoved; list.remove(tag); Attribute a = new CodeStringAttribute(tag); a.addValue("YES"); list.put(tag,a); }
		{
			AttributeTag tag = TagFromName.DeidentificationMethod;
			Attribute a = list.get(tag);
			if (a == null) {
				a = new LongStringAttribute(tag);
				list.put(tag,a);
			}
			a.addValue("Deidentified");
			a.addValue("Descriptors " + (keepDescriptors ? "retained" : ("removed" + (keepSeriesDescriptors ? " except series" : "") + (keepProtocolName ? " except protocol" : ""))));
			a.addValue("Patient Characteristics " + (keepPatientCharacteristics ? "retained" : "removed"));
			a.addValue("Device identity " + (keepDeviceIdentity ? "retained" : "removed"));
			a.addValue("Institution identity " + (keepInstitutionIdentity ? "retained" : "removed"));
			if (handleUIDs != HandleUIDs.keep) {
				a.addValue("UIDs " + (handleUIDs == HandleUIDs.remap ? "remapped" : "removed"));
			}
			a.addValue("Dates " + (handleDates == HandleDates.keep ? "retained" : (handleDates == HandleDates.modify ? "modified" : "removed")));
		}
		{
			AttributeTag tag = TagFromName.DeidentificationMethodCodeSequence;
			SequenceAttribute a = (SequenceAttribute)(list.get(tag));
			if (a == null) {
				a = new SequenceAttribute(tag);
				list.put(tag,a);
			}
			a.addItem(new CodedSequenceItem("113100","DCM","Basic Application Confidentiality Profile").getAttributeList());
			
			if (keepDescriptors) {
				a.addItem(new CodedSequenceItem("210005","99PMP","Retain all descriptors unchanged").getAttributeList());
			}
			else {
				if (keepSeriesDescriptors && keepProtocolName) {
					a.addItem(new CodedSequenceItem("210008","99PMP","Remove all descriptors except Series Description & Protocol Name").getAttributeList());
				}
				else if (keepProtocolName) {
					a.addItem(new CodedSequenceItem("210009","99PMP","Remove all descriptors except Protocol Name").getAttributeList());
				}
				else if (keepSeriesDescriptors) {
					a.addItem(new CodedSequenceItem("210003","99PMP","Remove all descriptors except Series Description").getAttributeList());
				}
				else {
					a.addItem(new CodedSequenceItem("210004","99PMP","Remove all descriptors").getAttributeList());
				}
			}
			
			if (keepPatientCharacteristics) {
				a.addItem(new CodedSequenceItem("113108","DCM","Retain Patient Characteristics Option").getAttributeList());
			}
			
			if (keepDeviceIdentity) {
				a.addItem(new CodedSequenceItem("113109","DCM","Retain Device Identity Option").getAttributeList());
			}

			if (keepInstitutionIdentity) {
				a.addItem(new CodedSequenceItem("210006","99PMP","Retain institution identity").getAttributeList());
			}

			if (handleUIDs == HandleUIDs.keep) {
				a.addItem(new CodedSequenceItem("113110","DCM","Retain UIDs Option").getAttributeList());
			}
			else if (handleUIDs == HandleUIDs.remap) {
				a.addItem(new CodedSequenceItem("210001","99PMP","Remap UIDs").getAttributeList());
			}
			else if (handleUIDs == HandleUIDs.remove) {
				a.addItem(new CodedSequenceItem("210007","99PMP","Remove UIDs").getAttributeList());
			}

			if (handleDates == HandleDates.keep) {
				a.addItem(new CodedSequenceItem("113106","DCM","Retain Longitudinal Temporal Information Full Dates Option").getAttributeList());
			}
			else if (handleDates == HandleDates.modify) {
				a.addItem(new CodedSequenceItem("113107","DCM","Retain Longitudinal Temporal Information Modified Dates Option").getAttributeList());
			}
		}
	}

	
	/**
	 * <p>Deidentify a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * <p>Does not process UIDs, but does remove sequences that would be invalidated by removing UIDs, e.g., Source Image Sequence and Referenced Image Sequence. If necessary caller should use {@link ClinicalTrialsAttributes#removeOrRemapUIDAttributes(AttributeList,int) removeOrRemapUIDAttributes()}.</p>
	 *
	 * <p>Does not process dates and times (other than Patient Birth). If necessary caller should use {@link ClinicalTrialsAttributes#removeOrRemapDateAndTimeAttributes(AttributeList,int,Date,Date,String) removeOrRemapDateAndTimeAttributes()}.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepProtocolName			if true, keep protocol name even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep device identity
	 * @throws	DicomException	if error in DICOM encoding
	 */
	protected static void removeOrNullIdentifyingAttributesRecursively(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity) throws DicomException {
		// use the list from the Basic Application Level Confidentiality Profile in PS 3.15 2003, as updated per draft of Sup 142
	
		if (!keepDescriptors) {
			list.remove(TagFromName.StudyDescription);
			if (!keepSeriesDescriptors) {
				list.remove(TagFromName.SeriesDescription);
			}
			if (!keepProtocolName) {
				list.remove(TagFromName.ProtocolName);
			}
		}

		list.replaceWithZeroLengthIfPresent(TagFromName.AccessionNumber);
		
		if (!keepInstitutionIdentity) {
			list.remove(TagFromName.InstitutionName);
			list.remove(TagFromName.InstitutionAddress);
			list.remove(TagFromName.InstitutionalDepartmentName);
		}
		
		list.replaceWithZeroLengthIfPresent(TagFromName.ReferringPhysicianName);
		list.remove(TagFromName.ReferringPhysicianAddress);
		list.remove(TagFromName.ReferringPhysicianTelephoneNumbers);
		list.remove(TagFromName.PhysiciansOfRecord);
		list.remove(TagFromName.PerformingPhysicianName);
		list.remove(TagFromName.NameOfPhysiciansReadingStudy);
		list.remove(TagFromName.RequestingPhysician);		// not in IOD; from Detached Study Mx; seen in Philips CT, ADAC NM
		
		if (keepPatientCharacteristics && Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientAge).length() == 0) {
			addAgeDerivedFromBirthDateAndStudyRelatedDate(list);	// need to do this BEFORE replacing PatientBirthDate with zero length, obviously
		}

		list.remove(TagFromName.OperatorsName);
		list.remove(TagFromName.AdmittingDiagnosesDescription);
		list.remove(TagFromName.DerivationDescription);
		list.replaceWithZeroLengthIfPresent(TagFromName.PatientName);
		list.replaceWithZeroLengthIfPresent(TagFromName.PatientID);
		list.replaceWithZeroLengthIfPresent(TagFromName.PatientBirthDate);
		list.remove(TagFromName.PatientBirthTime);
		list.remove(TagFromName.OtherPatientIDs);
		list.remove(TagFromName.OtherPatientIDsSequence);
		list.remove(TagFromName.OtherPatientNames);

		if (!keepPatientCharacteristics) {
			list.replaceWithZeroLengthIfPresent(TagFromName.PatientSex);
			list.remove(TagFromName.PatientAge);
			list.remove(TagFromName.PatientSize);
			list.remove(TagFromName.PatientWeight);
			list.remove(TagFromName.EthnicGroup);
			list.remove(TagFromName.PregnancyStatus);		// not in IOD; from Detached Patient Mx
			list.remove(TagFromName.SmokingStatus);			// not in IOD; from Detached Patient Mx
			list.replaceWithZeroLengthIfPresent(TagFromName.PatientSexNeutered);
			list.remove(TagFromName.SpecialNeeds);
		}
		
		list.remove(TagFromName.MedicalRecordLocator);
		list.remove(TagFromName.Occupation);
		list.remove(TagFromName.AdditionalPatientHistory);
		list.remove(TagFromName.PatientComments);

		if (!keepDeviceIdentity) {
			list.remove(TagFromName.StationName);
			list.replaceWithValueIfPresent(TagFromName.DeviceSerialNumber,"SN000000");
			list.remove(TagFromName.DeviceUID);
			list.remove(TagFromName.PlateID);
			list.remove(TagFromName.GantryID);
			list.remove(TagFromName.CassetteID);
			list.remove(TagFromName.GeneratorID);
			list.replaceWithValueIfPresent(TagFromName.DetectorID,"DET00000");
			list.remove(TagFromName.PerformedStationAETitle);
			list.remove(TagFromName.PerformedStationGeographicLocationCodeSequence);
			list.remove(TagFromName.PerformedStationName);
			list.remove(TagFromName.PerformedStationNameCodeSequence);
			list.remove(TagFromName.ScheduledProcedureStepLocation);
			list.remove(TagFromName.ScheduledStationAETitle);
			list.remove(TagFromName.ScheduledStationGeographicLocationCodeSequence);
			list.remove(TagFromName.ScheduledStationName);
			list.remove(TagFromName.ScheduledStationNameCodeSequence);
			list.remove(TagFromName.ScheduledStudyLocation);
			list.remove(TagFromName.ScheduledStudyLocationAETitle);
			list.remove(TagFromName.SourceSerialNumber);
		}
		
		list.replaceWithZeroLengthIfPresent(TagFromName.StudyID);
		list.remove(TagFromName.RequestAttributesSequence);
		
		// remove all issuers, whether in composite IODs or not
		list.remove(TagFromName.IssuerOfAccessionNumberSequence);
		list.remove(TagFromName.IssuerOfPatientID);
		list.remove(TagFromName.IssuerOfPatientIDQualifiersSequence);
		list.remove(TagFromName.StudyIDIssuer);
		list.remove(TagFromName.IssuerOfAdmissionID);
		list.remove(TagFromName.IssuerOfAdmissionIDSequence);
		list.remove(TagFromName.IssuerOfServiceEpisodeID);
		list.remove(TagFromName.IssuerOfServiceEpisodeIDSequence);
		list.remove(TagFromName.ResultsIDIssuer);
		list.remove(TagFromName.InterpretationIDIssuer);
		
		list.remove(TagFromName.StudyStatusID);			// not in IOD; from Detached Study Mx; seen in Philips CT
		list.remove(TagFromName.StudyPriorityID);		// not in IOD; from Detached Study Mx; seen in Philips CT
		list.remove(TagFromName.CurrentPatientLocation);	// not in IOD; from Detached Study Mx; seen in Philips CT
		
		list.remove(TagFromName.PatientAddress);					// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.MilitaryRank);						// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.BranchOfService);					// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.PatientBirthName);					// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.PatientMotherBirthName);				// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.ConfidentialityConstraintOnPatientDataDescription);	// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.PatientInsurancePlanCodeSequence);			// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.PatientPrimaryLanguageCodeSequence);			// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.CountryOfResidence);					// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.RegionOfResidence);					// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.PatientTelephoneNumbers);				// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.PatientReligiousPreference);				// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.MedicalAlerts);						// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.Allergies);							// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.LastMenstrualDate);					// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.SpecialNeeds);						// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.PatientState);						// not in IOD; from Detached Patient Mx
		list.remove(TagFromName.AdmissionID);						// not in IOD
		list.remove(TagFromName.AdmittingDate);						// not in IOD
		list.remove(TagFromName.AdmittingTime);						// not in IOD

		if (!keepDescriptors) {
			list.remove(TagFromName.ImageComments);
		}

		// ContentSequence
		
		// ContentIdentificationMacro
		
		list.replaceWithZeroLengthIfPresent(TagFromName.ContentCreatorName);
		list.remove(TagFromName.ContentCreatorIdentificationCodeSequence);

		// others that it would seem necessary to remove ...
		
		list.remove(TagFromName.ReferencedPatientSequence);
		list.remove(TagFromName.ReferringPhysicianIdentificationSequence);
		list.remove(TagFromName.PhysiciansOfRecordIdentificationSequence);
		list.remove(TagFromName.PhysiciansReadingStudyIdentificationSequence);
		list.remove(TagFromName.ReferencedStudySequence);
		list.remove(TagFromName.AdmittingDiagnosesCodeSequence);
		list.remove(TagFromName.PerformingPhysicianIdentificationSequence);
		list.remove(TagFromName.OperatorIdentificationSequence);
		list.remove(TagFromName.PerformedProcedureStepID);
		list.remove(TagFromName.DataSetTrailingPadding);
		
		list.remove(TagFromName.ActualHumanPerformersSequence);
		list.remove(TagFromName.AddressTrial);
		list.remove(TagFromName.Arbitrary);
		list.remove(TagFromName.AuthorObserverSequence);
		list.remove(TagFromName.ContributionDescription);
		list.remove(TagFromName.CurrentObserverTrial);
		list.remove(TagFromName.CustodialOrganizationSequence);
		list.remove(TagFromName.DistributionAddress);
		list.remove(TagFromName.DistributionName);
		list.replaceWithZeroLengthIfPresent(TagFromName.FillerOrderNumberImagingServiceRequest);
		list.remove(TagFromName.HumanPerformerName);
		list.remove(TagFromName.HumanPerformerOrganization);
		list.remove(TagFromName.IconImageSequence);
		list.remove(TagFromName.IdentifyingComments);
		list.remove(TagFromName.InsurancePlanIdentification);
		list.remove(TagFromName.IntendedRecipientsOfResultsIdentificationSequence);
		list.remove(TagFromName.InterpretationApproverSequence);
		list.remove(TagFromName.InterpretationAuthor);
		list.remove(TagFromName.InterpretationRecorder);
		list.remove(TagFromName.InterpretationTranscriber);
		list.remove(TagFromName.ModifyingDeviceID);
		list.remove(TagFromName.ModifyingDeviceManufacturer);
		list.remove(TagFromName.NamesOfIntendedRecipientsOfResults);
		list.remove(TagFromName.OrderCallbackPhoneNumber);
		list.remove(TagFromName.OrderEnteredBy);
		list.remove(TagFromName.OrderEntererLocation);
		list.remove(TagFromName.ParticipantSequence);
		list.remove(TagFromName.PerformedLocation);
		list.remove(TagFromName.PersonAddress);
		list.remove(TagFromName.PersonIdentificationCodeSequence);
		list.remove(TagFromName.PersonName);
		list.remove(TagFromName.PersonTelephoneNumbers);
		list.remove(TagFromName.PhysicianApprovingInterpretation);
		list.replaceWithZeroLengthIfPresent(TagFromName.PlacerOrderNumberImagingServiceRequest);
		list.remove(TagFromName.PreMedication);
		list.remove(TagFromName.ReferencedPatientAliasSequence);
		list.remove(TagFromName.RequestedProcedureLocation);
		list.remove(TagFromName.RequestedProcedureID);
		list.remove(TagFromName.RequestingService);
		list.remove(TagFromName.ResponsibleOrganization);
		list.remove(TagFromName.ResponsiblePerson);
		list.remove(TagFromName.ResultsDistributionListSequence);
		list.remove(TagFromName.ScheduledHumanPerformersSequence);
		list.remove(TagFromName.ScheduledPatientInstitutionResidence);
		list.remove(TagFromName.ScheduledPerformingPhysicianIdentificationSequence);
		list.remove(TagFromName.ScheduledPerformingPhysicianName);
		list.remove(TagFromName.ServiceEpisodeID);
		list.remove(TagFromName.TelephoneNumberTrial);
		list.remove(TagFromName.TextComments);
		list.remove(TagFromName.TextString);
		list.remove(TagFromName.TopicAuthor);
		list.remove(TagFromName.TopicKeywords);
		list.remove(TagFromName.TopicSubject);
		list.remove(TagFromName.TopicTitle);
		list.remove(TagFromName.VerbalSourceTrial);
		list.remove(TagFromName.VerbalSourceIdentifierCodeSequenceTrial);
		list.replaceWithZeroLengthIfPresent(TagFromName.VerifyingObserverIdentificationCodeSequence);
		list.replaceWithValueIfPresent(TagFromName.VerifyingObserverName,"Observer^Deidentified");
		// Do nothing with VerifyingObserverSequence ... we handle the attributes inside it individually
		list.remove(TagFromName.VerifyingOrganization);


		if (!keepDescriptors) {
			list.remove(TagFromName.PerformedProcedureStepDescription);
			list.remove(TagFromName.CommentsOnThePerformedProcedureStep);
			list.remove(TagFromName.AcquisitionComments);
			list.remove(TagFromName.ReasonForStudy);		// not in IOD; from Detached Study Mx; seen in Philips CT
			list.remove(TagFromName.RequestedProcedureDescription);	// not in IOD; from Detached Study Mx; seen in Philips CT
			list.remove(TagFromName.StudyComments);			// not in IOD; from Detached Study Mx; seen in Philips CT
			list.replaceWithZeroLengthIfPresent(TagFromName.AcquisitionDeviceProcessingDescription);
			list.remove(TagFromName.DischargeDiagnosisDescription);
			list.remove(TagFromName.ImagePresentationComments);
			list.remove(TagFromName.ImagingServiceRequestComments);
			list.remove(TagFromName.Impressions);
			list.remove(TagFromName.InterpretationDiagnosisDescription);
			list.remove(TagFromName.InterpretationText);
			list.remove(TagFromName.OverlayComments);
			list.remove(TagFromName.ReasonForTheImagingServiceRequest);
			list.remove(TagFromName.RequestedContrastAgent);
			list.remove(TagFromName.RequestedProcedureComments);
			list.remove(TagFromName.ResultsComments);
			list.remove(TagFromName.ScheduledProcedureStepDescription);
			list.remove(TagFromName.ServiceEpisodeDescription);
			list.remove(TagFromName.VisitComments);
		}

		if (handleUIDs == HandleUIDs.remove) {
			// these are not UI VR, and hence don't get taken care of later,
			// but if left, would be made invalid when Instance UIDs were removed and
			// incomplete items left
			list.remove(TagFromName.ReferencedImageSequence);
			list.remove(TagFromName.SourceImageSequence);
			list.remove(TagFromName.ReferencedPerformedProcedureStepSequence);
		}

		Iterator i = list.values().iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				if (!a.getTag().equals(TagFromName.ContentSequence)) {	// i.e. do NOT descend into SR content tree
					Iterator items = a.iterator();
					if (items != null) {
						while (items.hasNext()) {
							SequenceItem item = (SequenceItem)(items.next());
							if (item != null) {
								AttributeList itemAttributeList = item.getAttributeList();
//System.err.println("Recursed into item of "+a);
								if (itemAttributeList != null) {
									removeOrNullIdentifyingAttributesRecursively(itemAttributeList,handleUIDs,keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity);
								}
							}
						}
					}
				}
			}
		}
	}

}

