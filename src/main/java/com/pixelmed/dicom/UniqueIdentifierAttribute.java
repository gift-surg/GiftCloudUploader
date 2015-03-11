/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>A concrete class specializing {@link com.pixelmed.dicom.Attribute Attribute} for
 * Unique Identifier (UI) attributes.</p>
 *
 * <p>Though an instance of this class may be created
 * using its constructors, there is also a factory class, {@link com.pixelmed.dicom.AttributeFactory AttributeFactory}.</p>
 *
 * @see com.pixelmed.dicom.Attribute
 * @see com.pixelmed.dicom.AttributeFactory
 * @see com.pixelmed.dicom.AttributeList
 *
 * @author	dclunie
 */
public class UniqueIdentifierAttribute extends StringAttribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/UniqueIdentifierAttribute.java,v 1.15 2014/09/09 20:34:09 dclunie Exp $";

	protected static final int MAX_LENGTH_SINGLE_VALUE = 64;
	
	public final int getMaximumLengthOfSingleValue() { return MAX_LENGTH_SINGLE_VALUE; }

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public UniqueIdentifierAttribute(AttributeTag t) {
		super(t);
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public UniqueIdentifierAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
		super(t,vl,i);
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public UniqueIdentifierAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
		super(t,vl.longValue(),i);
	}

	/**
	 * <p>Get the value representation of this attribute (UI).</p>
	 *
	 * @return	'U','I' in ASCII as a two byte array; see {@link com.pixelmed.dicom.ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.UI; }

	/**
	 * <p>Get the appropriate (0X00) byte for padding UIDS to an even length.</p>
	 *
	 * @return	the byte pad value appropriate to the VR
	 */
	protected byte getPadByte() { return 0x00; }
	
	// grep 'VR="UI"' ~/work/dicom3tools/libsrc/standard/elmdict/dicom3.tpl | awk '{print $1 " " $5}' | sed -e 's/Keyword="//' -e 's/"//g' | sort +1 | egrep '(TransferSyntax|SOPClass|Private|CodingScheme)' | awk '{print $2}'

	static public boolean isSOPClassRelated(AttributeTag t) {
		return t.equals(TagFromName.SOPClassUID)
			|| t.equals(TagFromName.AffectedSOPClassUID)
			|| t.equals(TagFromName.MediaStorageSOPClassUID)
			|| t.equals(TagFromName.OriginalSpecializedSOPClassUID)
			|| t.equals(TagFromName.ReferencedRelatedGeneralSOPClassUIDInFile)
			|| t.equals(TagFromName.ReferencedSOPClassUID)
			|| t.equals(TagFromName.ReferencedSOPClassUIDInFile)
			|| t.equals(TagFromName.RelatedGeneralSOPClassUID)
			|| t.equals(TagFromName.RequestedSOPClassUID)
			|| t.equals(TagFromName.RelatedGeneralSOPClassUID)
			|| t.equals(TagFromName.SOPClassesInStudy)
			|| t.equals(TagFromName.SOPClassesSupported);
	}
	
	static public boolean isTransferSyntaxRelated(AttributeTag t) {
		return t.equals(TagFromName.TransferSyntaxUID)
			|| t.equals(TagFromName.EncryptedContentTransferSyntaxUID)
			|| t.equals(TagFromName.MACCalculationTransferSyntaxUID)
			|| t.equals(TagFromName.ReferencedTransferSyntaxUIDInFile);
	}
	
	static public boolean isCodingSchemeRelated(AttributeTag t) {
		return t.equals(TagFromName.CodingSchemeUID)
			|| t.equals(TagFromName.ContextGroupExtensionCreatorUID);
	}
	
	static public boolean isPrivateRelated(AttributeTag t) {
		return t.equals(TagFromName.PrivateInformationCreatorUID)
			|| t.equals(TagFromName.PrivateRecordUID);
	}

	static public boolean isTransient(AttributeTag t) {
		return !isSOPClassRelated(t)
			&& !isTransferSyntaxRelated(t)
			&& !isCodingSchemeRelated(t)
			&& !isPrivateRelated(t);
	}

	//protected final boolean allowRepairOfIncorrectLength() { return true; }				// moot, since we do not check this in repairValues(), i.e., hard-coded
	
	//protected final boolean allowRepairOfInvalidCharacterReplacement() { return false; }	// moot, since we do not check this in repairValues(), i.e., hard-coded

	public final boolean isCharacterInValueValid(int c) throws DicomException {
		return c < 0x7f /* ASCII only to limit Character.isXX() tests */ && (Character.isDigit(c) || c == '.');
	}
	
	public boolean repairValues() throws DicomException {
		if (!isValid()) {
			flushCachedCopies();
			originalByteValues=null;
			if (originalValues != null && originalValues.length > 0) {
				// removing padding is the best we can do without loosing the meaning of the value ... may still be invalid :(
				// do not just use ArrayCopyUtilities.copyStringArrayRemovingLeadingAndTrailingPadding(originalValues), since it only handle space character
				for (int i=0; i<originalValues.length; ++i) {
					String v = originalValues[i];
					if (v != null && v.length() > 0) {
						originalValues[i] = v.trim();
					}
				}
			}
		}
		return isValid();
	}
}

