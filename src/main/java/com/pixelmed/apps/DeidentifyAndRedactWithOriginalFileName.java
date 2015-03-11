/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;

import java.io.File;
import java.io.IOException;

public class DeidentifyAndRedactWithOriginalFileName extends DeidentifyAndRedact {

	/**
	 * <p>Make a suitable file name to use for a deidentified and redacted input file.</p>
	 *
	 * <p>Uses the inputFileName without any trailing ".dcm" suffix plus "_Anon.dcm" in the outputFolderName (ignoring the sopInstanceUID).</p>
	 *
	 * <p>Does NOT use the full hierarchy of the inputFileName, only the base file name and does NOT check whether or not the generated file name already exists,
	 * so may cause any existing or duplicate base file name to be silently overwritten.</p>
	 *
	 * <p>Overrides the default method in the parent class.</p>
	 *
	 * @param		outputFolderName	where to store all the processed output files
	 * @param		inputFileName		the path to search for DICOM files
	 * @param		sopInstanceUID		the SOP Instance UID of the output file
	 * @exception	IOException			if a filename cannot be constructed
	 */
	protected String makeOutputFileName(String outputFolderName,String inputFileName,String sopInstanceUID) throws IOException {
		// ignore sopInstanceUID
		return new File(outputFolderName,new File(inputFileName).getName().replaceFirst("[.]dcm$","")+"_Anon.dcm").getCanonicalPath();
	}

	public DeidentifyAndRedactWithOriginalFileName(String inputPathName,String outputFolderName,String redactionControlFileName,boolean decompress,boolean keepAllPrivate,AttributeList replacementAttributes) throws DicomException, Exception, IOException {
		super(inputPathName,outputFolderName,redactionControlFileName,decompress,keepAllPrivate,replacementAttributes);
	}
	
	public DeidentifyAndRedactWithOriginalFileName(String inputPathName,String outputFolderName,String redactionControlFileName,boolean decompress,boolean keepAllPrivate) throws DicomException, Exception, IOException {
		super(inputPathName,outputFolderName,redactionControlFileName,decompress,keepAllPrivate);
	}

	public static void main(String arg[]) {
		try {
			if (arg.length >= 3) {
				int startReplacements = 3;
				boolean decompress = false;
				if (arg.length >= 4 && arg.length %2 == 0) {	// optional, but may be followed by replacement kewyord/value pairs, so check for even length too
					startReplacements = 4;
					decompress = arg[3].trim().toUpperCase().equals("DECOMPRESS");
				}
//System.err.println("DeidentifyAndRedact.main(): decompress = "+decompress);
				AttributeList replacementAttributes = null;
				if (arg.length > 4) {
//System.err.println("DeidentifyAndRedact.main(): have replacement attributes");
					replacementAttributes = AttributeList.makeAttributeListFromKeywordAndValuePairs(arg,startReplacements,arg.length-startReplacements);
//System.err.print("DeidentifyAndRedact.main(): the replacement attributes are:"+replacementAttributes);
				}
long startTime = System.currentTimeMillis();
				new DeidentifyAndRedactWithOriginalFileName(arg[0],arg[1],arg[2],decompress,true/*keepAllPrivate*/,replacementAttributes);
long currentTime = System.currentTimeMillis();
System.err.println("DeidentifyAndRedact entire set took = "+(currentTime-startTime)+" ms");
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: DeidentifyAndRedactWithOriginalFileName inputPath outputFile redactionControlFile [BLOCK|DECOMPRESS] [keyword value]*");
				System.exit(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

