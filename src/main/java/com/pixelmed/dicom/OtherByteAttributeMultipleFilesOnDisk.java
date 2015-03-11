/* Copyright (c) 2001-2015, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

import com.pixelmed.utils.CopyStream;

/**
 * <p>A concrete class specializing {@link com.pixelmed.dicom.Attribute Attribute} for
 * Other Byte (OB) attributes whose values are not memory resident but rather are stored in multiple files on disk.</p>
 *
 * @see com.pixelmed.dicom.Attribute
 * @see com.pixelmed.dicom.AttributeFactory
 * @see com.pixelmed.dicom.AttributeList
 *
 * @author	dclunie
 */
public class OtherByteAttributeMultipleFilesOnDisk extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherByteAttributeMultipleFilesOnDisk.java,v 1.4 2015/01/03 22:59:40 dclunie Exp $";
	
	protected File[] files;
	protected long[] byteOffsets;
	protected long[] lengths;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherByteAttributeMultipleFilesOnDisk(AttributeTag t) {
		super(t);
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	files		the input files
	 * @param	byteOffsets	the byte offsets in the files of the start of the data, one entry for each file, or null if 0 for all files
	 * @param	lengths		the lengths in the files from the the start of the data, one entry for each file, or null if the remaining file length after the byteOffset, if any
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherByteAttributeMultipleFilesOnDisk(AttributeTag t,File[] files,long[] byteOffsets,long[] lengths) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(files,byteOffsets,lengths);
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	fileNames	the input files
	 * @param	byteOffsets	the byte offsets in the files of the start of the data, one entry for each file, or null if 0 for all files
	 * @param	lengths		the lengths in the files from the the start of the data, one entry for each file, or null if the remaining file length after the byteOffset, if any
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherByteAttributeMultipleFilesOnDisk(AttributeTag t,String[] fileNames,long[] byteOffsets,long[] lengths) throws IOException, DicomException {
		super(t);
		File[] files = new File[fileNames.length];
		for (int i=0; i<fileNames.length; ++i) {
			files[i] = new File(fileNames[i]);
		}
		doCommonConstructorStuff(files,byteOffsets,lengths);
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	files		the input files
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherByteAttributeMultipleFilesOnDisk(AttributeTag t,File[] files) throws IOException, DicomException {
		this(t,files,null,null);
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	fileNames	the input files
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherByteAttributeMultipleFilesOnDisk(AttributeTag t,String[] fileNames) throws IOException, DicomException {
		this(t,fileNames,null,null);
	}

	/**
	 * @param	files		the input files
	 * @param	byteOffsets	the byte offsets in the files of the start of the data, one entry for each file, or null if 0 for all files
	 * @param	lengths		the lengths in the files from the the start of the data, one entry for each file, or null if the remaining file length after the byteOffset, if any
	 * @throws	IOException
	 * @throws	DicomException
	 */
	private void doCommonConstructorStuff(File[] files,long[] byteOffsets,long[] lengths) throws IOException {
		this.files = files;
		if (byteOffsets == null) {
			this.byteOffsets = new long[files.length];
		}
		else {
			this.byteOffsets = byteOffsets;
		}
		if (lengths == null) {
			this.lengths = new long[files.length];
		}
		else {
			this.lengths = lengths;
		}
	
		valueLength=0;
		for (int i=0; i<files.length; ++i) {
			long length = 0;
			if (lengths == null) {
				length = files[i].length();
//System.err.println("OtherByteAttributeMultipleFilesOnDisk.doCommonConstructorStuff(): files["+i+"] = "+files[i].getCanonicalPath()+" length() = "+length);
				if (byteOffsets != null) {
					length -= byteOffsets[i];
				}
				this.lengths[i] = length;
			}
			else {
				length = lengths[i];
			}
			valueLength += length;
		}
//System.err.println("OtherByteAttributeMultipleFilesOnDisk.doCommonConstructorStuff(): valueLength = "+valueLength);
	}

	/***/
	public long getPaddedVL() {
		long vl = getVL();
		if (vl%2 != 0) ++vl;
		return vl;
	}
	
	/**
	 * @param	o
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		if (valueLength > 0) {
			for (int i=0; i<files.length; ++i) {
				File file = files[i];
				long byteOffset = byteOffsets[i];
				long length = lengths[i];
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				CopyStream.skipInsistently(in,byteOffset);
				CopyStream.copy(in,o,length);
				in.close();
			}
			long npad = getPaddedVL() - valueLength;
			while (npad-- > 0) o.write(0x00);
		}
	}
	
	/***/
	public String toString(DicomDictionary dictionary) {
		StringBuffer str = new StringBuffer();
		str.append(super.toString(dictionary));
		str.append(" []");		// i.e. don't really dump values ... too many
		return str.toString();
	}

	/**
	 */
	public void removeValues() {
		files=null;
		byteOffsets=null;
		lengths=null;
		valueMultiplicity=0;
		valueLength=0;
	}

	/**
	 * <p>Get the value representation of this attribute (OB).</p>
	 *
	 * @return	'O','B' in ASCII as a two byte array; see {@link com.pixelmed.dicom.ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OB; }
}

