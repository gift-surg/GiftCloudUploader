/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>An abstract class specializing {@link com.pixelmed.dicom.Attribute Attribute} for
 * Other (OB or OW) attributes whose values are not memory resident.</p>
 *
 * <p>Used as a base class for {@link com.pixelmed.dicom.OtherByteAttributeOnDisk OtherByteAttributeOnDisk} and {@link com.pixelmed.dicom.OtherWordAttributeOnDisk OtherWordAttributeOnDisk}.</p>
 *
 * @see com.pixelmed.dicom.Attribute
 * @see com.pixelmed.dicom.AttributeFactory
 * @see com.pixelmed.dicom.AttributeList
 * @see com.pixelmed.dicom.OtherByteAttributeOnDisk
 * @see com.pixelmed.dicom.OtherWordAttributeOnDisk
 *
 * @author	dclunie
 */
public abstract class OtherAttributeOnDisk extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherAttributeOnDisk.java,v 1.2 2014/09/09 20:34:09 dclunie Exp $";

	protected long byteOffset;
	protected File file;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherAttributeOnDisk(AttributeTag t) {
		super(t);
		byteOffset = 0;
		file = null;
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @param	byteOffset	the byte offset in the input stream of the start of the data
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public OtherAttributeOnDisk(AttributeTag t,long vl,DicomInputStream i,long byteOffset) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl,i,byteOffset);
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @param	byteOffset	the byte offset in the input stream of the start of the data
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public OtherAttributeOnDisk(AttributeTag t,Long vl,DicomInputStream i,Long byteOffset) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl.longValue(),i,byteOffset.longValue());
	}

	/**
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @param	byteOffset	the byte offset in the input stream of the start of the data
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	private void doCommonConstructorStuff(long vl,DicomInputStream i,long byteOffset) throws IOException, DicomException {
		valueLength=vl;
		this.byteOffset=byteOffset;
		file=i.getFile();
		if (file == null) {
				throw new DicomException("Cannot have an OtherAttributeOnDisk without a file available in the DicomInputStream");
		}

		if (vl > 0) {
			try {
				i.skipInsistently(vl);
			}
			catch (IOException e) {
				throw new DicomException("Failed to skip value (length "+vl+" dec) in "+ValueRepresentation.getAsString(getVR())+" attribute "+getTag());
			}
		}
	}
	
	public String toString(DicomDictionary dictionary) {
		StringBuffer str = new StringBuffer();
		str.append(super.toString(dictionary));
		str.append(" []");		// i.e. don't really dump values ... too many
		return str.toString();
	}

	/**
	 * @return		the offset from the start of the object in bytes
	 */
	public long getByteOffset() { return byteOffset; }

	/**
	 * @return		the file containing the data
	 */
	public File getFile() { return file; }

	/**
	 * <p>Change the file containing the data, for example if it has been renamed.</p>
	 *
	 * @param	file	the new file containing the data
	 */
	public void setFile(File file) { this.file = file; }

	public void removeValues() {
		file=null;
		byteOffset=0;
		valueMultiplicity=0;
		valueLength=0;
	}

}

