/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.database.DatabaseInformationModel;

//import com.pixelmed.dicom.Attribute;
//import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
//import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.InformationEntity;
//import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.SOPClass;
//import com.pixelmed.dicom.TagFromName;
//import com.pixelmed.dicom.TransferSyntax;

import com.pixelmed.utils.CopyStream;
import com.pixelmed.utils.MessageLogger;
import com.pixelmed.utils.PrintStreamMessageLogger;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * <p>This class copies a set of DICOM files, if they match specified criteria.</p>
 *
 * @author	dclunie
 */
public class FindAndCopySelectedDicomFilesUsingDatabase {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/FindAndCopySelectedDicomFilesUsingDatabase.java,v 1.1 2014/06/26 20:54:58 dclunie Exp $";
	
	public FindAndCopySelectedDicomFilesUsingDatabase(DatabaseInformationModel databaseInformationModel,Set<String> sopClasses,String outputPath) {
		String filenameColumnKey = databaseInformationModel.getLocalFileNameColumnName(InformationEntity.INSTANCE);
		for (String sopClass : sopClasses) {
System.err.println("Doing SOP Class "+sopClass);
			try {
				ArrayList<TreeMap<String,String>> records = databaseInformationModel.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedKeyValue(InformationEntity.INSTANCE,"SOPCLASSUID",sopClass);
				for (TreeMap<String,String> record : records) {
					String srcfilename = record.get(filenameColumnKey);
					if (srcfilename != null && srcfilename.length() > 0) {
						String sopInstanceUID = record.get("SOPINSTANCEUID");
						if (sopInstanceUID != null && sopInstanceUID.length() > 0) {
							File outputFile = new File(outputPath,sopInstanceUID+".dcm");
System.err.println("Copying srcfilename \""+srcfilename+"\" to \""+outputFile+"\"");
							CopyStream.copy(new File(srcfilename),outputFile);
						}
						else {
							System.err.println("Cannot extract SOP Instance UID for \""+srcfilename+"\" to create output file name - ignoring");
						}
					}
					else {
						System.err.println("Record missing filename - ignoring");
					}
				}
			}
			catch (DicomException e) {
				e.printStackTrace(System.err);
			}
			catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}
		
	/**
	 * <p>Copy a set of DICOM files, if they match specified criteria.</p>
	 *
	 * <p>Does not actually check the Modality value in the file, but matches the SOP Class against what is returned from {@link com.pixelmed.dicom.SOPClass#getPlausibleStandardSOPClassUIDsForModality(String) SOPClass.getPlausibleStandardSOPClassUIDsForModality(String)}.</p>
	 *
	 * @param	arg	array of four strings - the class name of the database model, the fully qualified path of the database file prefix, the output path, and the SOP Class UID or Modality
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 4) {
				String databaseModelClassName = arg[0];
				String databaseFileName = arg[1];
		
				if (databaseModelClassName.indexOf('.') == -1) {					// not already fully qualified
					databaseModelClassName="com.pixelmed.database."+databaseModelClassName;
				}
//System.err.println("Class name = "+databaseModelClassName);

				//DatabaseInformationModel databaseInformationModel = new PatientStudySeriesConcatenationInstanceModel(makePathToFileInUsersHomeDirectory(dataBaseFileName));
				DatabaseInformationModel databaseInformationModel = null;
				try {
					Class classToUse = Thread.currentThread().getContextClassLoader().loadClass(databaseModelClassName);
					Class[] parameterTypes = { databaseFileName.getClass() };
					Constructor constructorToUse = classToUse.getConstructor(parameterTypes);
					Object[] args = { databaseFileName };
					databaseInformationModel = (DatabaseInformationModel)(constructorToUse.newInstance(args));
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					System.exit(0);
				}
				
				String requestedSOPClass = arg[3];
				String outputPath = arg[2];

				Set<String> sopClasses = new HashSet<String>();
				if (requestedSOPClass.startsWith("1")) {
//System.err.println("main(): importer.sopClasses.add = \""+arg[2]+"\"");
					sopClasses.add(requestedSOPClass);
				}
				else {
					for (String sopClass : SOPClass.getPlausibleStandardSOPClassUIDsForModality(requestedSOPClass)) {
//System.err.println("main(): importer.sopClasses.add = \""+sopClass+"\"");
						sopClasses.add(sopClass);
					}
				}
				new FindAndCopySelectedDicomFilesUsingDatabase(databaseInformationModel,sopClasses,outputPath);
			}
			else {
				System.err.println("Usage: java -cp ./pixelmed.jar com.pixelmed.apps.FindAndCopySelectedDicomFilesUsingDatabase databaseModelClassName databaseFileName dstdir sopclass|modality");
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
}


