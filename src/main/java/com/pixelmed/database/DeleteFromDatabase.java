/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.database;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.InformationEntity;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * <p>This class provides methods for removing entries from a database, all its children and any associated
 * files that were copied into the database (rather than referenced).</p>
 *
 * @author	dclunie
 */
public class DeleteFromDatabase {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/database/DeleteFromDatabase.java,v 1.3 2014/09/09 20:34:09 dclunie Exp $";
	
	public static void deleteRecordChildrenAndFilesByUniqueKey(DatabaseInformationModel d,String ieName,String keyValue) throws DicomException {
		InformationEntity ie = InformationEntity.fromString(ieName);	// already handles upper or lower case
		deleteRecordChildrenAndFilesByUniqueKey(d,ie,keyValue);
	}

	public static void deleteRecordChildrenAndFilesByFilename(final DatabaseInformationModel databaseInformationModel, final String fileName) throws DicomException {
		String sopInstanceUid = findPrimaryKeyForFilename(databaseInformationModel, fileName);
		deleteRecordChildrenAndFilesByUniqueKey(databaseInformationModel, InformationEntity.INSTANCE, sopInstanceUid);
	}

	/**
	 * <p>Remove the database entry, all its children and any copied files.</p>
	 *
	 * @param	d
	 * @param	ie
	 * @param	keyValue					for the PATIENT level, the unique key is the PatientID, otherwise it is the InstanceUID of the entity
	 * @throws	DicomException
	 */
	public static void deleteRecordChildrenAndFilesByUniqueKey(DatabaseInformationModel d,InformationEntity ie,String keyValue) throws DicomException {
		if (ie != null) {
			if (d != null) {
			
				// really should consider adding PATIENT to DatabaseInformationModel.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedUID()
				// instead of having it return null for patient level since PatientID is not a true "UID", and that would better generalize the model
				// rather than hardwiting it here (and/or rename the method to ...WithSpecifiedUniqueKey() or similar) :(
				String keyColumnName = ie.equals(InformationEntity.PATIENT) ? "PATIENTID" : d.getUIDColumnNameForInformationEntity(ie);
				String localPrimaryKeyColumnName = d.getLocalPrimaryKeyColumnName(ie);
								
//System.err.println("Query database for "+ie+" "+keyColumnName+" "+keyValue);
				List<Map<String,String>> results = d.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedKeyValue(ie,keyColumnName,keyValue);
//System.err.println("Results = "+results);
				if (results != null && results.size() > 0) {
					for (Map<String,String> result : results) {
						String localPrimaryKeyValue = result.get(localPrimaryKeyColumnName);
System.err.println("Deleting "+ie+" "+localPrimaryKeyValue+" "+keyValue);
						deleteRecordChildrenAndFilesByLocalPrimaryKey(d,ie,localPrimaryKeyValue);
						
					}
				}
				// else do nothing and success already false
			}
			else {
				throw new DicomException("No database");
			}
		}
		else {
			throw new DicomException("Unrecognized Information Entity");
		}
	}
	

	public static String findPrimaryKeyForFilename(DatabaseInformationModel d, final String fileName) throws DicomException {
		Map<String,String> result = d.findAllAttributeValuesForSelectedFilename(fileName);
		return result.get("SOPINSTANCEUID");
	}

	/**
	 * <p>Remove the database entry, all its children and any copied files.</p>
	 *
	 * @param	d
	 * @param	ie
	 * @param	localPrimaryKeyValue
	 * @throws	DicomException			if the databaseInformationModel or ie are invalid
	 */
	public static void deleteRecordChildrenAndFilesByLocalPrimaryKey(DatabaseInformationModel d,InformationEntity ie,String localPrimaryKeyValue) throws DicomException {
		InformationEntity thisIe = ie;
		if (thisIe != null) {
			if (d != null) {
				String parentPrimaryKeyValue = null;
				if (localPrimaryKeyValue != null && localPrimaryKeyValue.length() > 0) {
					Map<String,String> result = d.findAllAttributeValuesForSelectedRecord(thisIe,localPrimaryKeyValue);

					// If the model supports concatenations but this series has no concatentations, then the result might be empty, so check for the parent type
					if (result == null || result.size() == 0) {
						InformationEntity parentIE = d.getParentTypeForChild(thisIe,false/*concatenation*/);
						if (parentIE != null) {
							thisIe = parentIE;
							result = d.findAllAttributeValuesForSelectedRecord(parentIE, localPrimaryKeyValue);
						}
					}
					if (result != null && result.size() > 0) {
						parentPrimaryKeyValue = result.get(d.getLocalParentReferenceColumnName(thisIe));
					}

					if (result != null) {
						if (thisIe.equals(InformationEntity.INSTANCE)) {
							// delete any referenced files
							String fileName = result.get(d.getLocalFileNameColumnName(thisIe));
							String fileReferenceType = result.get(d.getLocalFileReferenceTypeColumnName(thisIe));
							if (fileReferenceType != null && fileReferenceType.equals(DatabaseInformationModel.FILE_COPIED)) {
								try {
									if (!new File(fileName).delete()) {
										System.err.println("Failed to delete local copy of file "+fileName);
									}
								}
								catch (Exception e) {
									e.printStackTrace(System.err);
									System.err.println("Failed to delete local copy of file "+fileName);
								}
							}
						}
					}
				
					// delete any children first
					InformationEntity childIE = d.getChildTypeForParent(thisIe,true/*concatenation*/);
					if (childIE != null) {
						String childLocalPrimaryKeyColumnName = d.getLocalPrimaryKeyColumnName(childIE);
						List<Map<String,String>> childResults = d.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedParent(childIE,localPrimaryKeyValue);
						if (childResults == null || childResults.size() == 0) {
							// could be because model supports concatenations but this series has no concatentations, so check for instance children of series ...
							childIE = d.getChildTypeForParent(thisIe,false/*concatenation*/);
							childLocalPrimaryKeyColumnName = d.getLocalPrimaryKeyColumnName(childIE);
							childResults = d.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedParent(childIE,localPrimaryKeyValue);
						}
						if (childResults != null && childResults.size() > 0) {
							for (Map<String,String> childResult : childResults) {
								String childLocalPrimaryKeyValue = childResult.get(childLocalPrimaryKeyColumnName);
								deleteRecordChildrenAndFilesByLocalPrimaryKey(d,childIE,childLocalPrimaryKeyValue);
							}
						}
					}

					// now delete ourselves ...
					d.deleteRecord(thisIe,localPrimaryKeyValue);

					// Delete the parent key if it does not contain any children
					if (parentPrimaryKeyValue != null) {
						final InformationEntity parentIE = d.getParentTypeForChild(thisIe, true/*concatenation*/);
						if (parentIE != null) {
							List<Map<String,String>> siblings = d.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedParent(thisIe, parentPrimaryKeyValue);
							if (siblings == null || siblings.size() == 0) {
								deleteRecordChildrenAndFilesByLocalPrimaryKey(d, parentIE, parentPrimaryKeyValue);
							}
						}
					}
				}
				else {
					throw new DicomException("Missing local primary key");
				}
			}
			else {
				throw new DicomException("No database");
			}
		}
		else {
			throw new DicomException("Unrecognized Information Entity");
		}
	}



	/**
	 * <p>Remove the database entry, all its children and any copied files.</p>
	 *
	 * <p>For the PATIENT level, the unique key is the PatientID, otherwise it is the InstanceUID of the entity.</p>
	 *
	 * @param	arg	four arguments, the class name of the model, the (full) path of the database file prefix, the level of the entity to remove and the unique key of the entity
	 */
	public static void main(String arg[]) {
		if (arg.length == 4) {
			String databaseModelClassName = arg[0];
			String databaseFileName = arg[1];
		
			if (databaseModelClassName.indexOf('.') == -1) {					// not already fully qualified
				databaseModelClassName="com.pixelmed.database."+databaseModelClassName;
			}
//System.err.println("Class name = "+databaseModelClassName);

			try {
				DatabaseInformationModel databaseInformationModel = new com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel(databaseFileName);
				//DatabaseInformationModel databaseInformationModel = null;
				//Class classToUse = Thread.currentThread().getContextClassLoader().loadClass(databaseModelClassName);
				//Class[] parameterTypes = { databaseFileName.getClass() };
				//Constructor constructorToUse = classToUse.getConstructor(parameterTypes);
				//Object[] args = { databaseFileName };
				//databaseInformationModel = (DatabaseInformationModel)(constructorToUse.newInstance(args));

				if (databaseInformationModel != null) {
					//{
					//	List everything = databaseInformationModel.findAllAttributeValuesForAllRecordsForThisInformationEntity(InformationEntity.PATIENT);
					//	System.err.println("everything.size() = "+everything.size());
					//}
					deleteRecordChildrenAndFilesByUniqueKey(databaseInformationModel,arg[2],arg[3]);
					databaseInformationModel.close();	// this is really important ... will not persist everything unless we do this
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				System.exit(0);
			}
			
		}
		else {
			System.err.println("Usage: java com.pixelmed.database.DeleteFromDatabase databaseModelClassName databaseFilePathPrefix databaseFileName path(s)");
		}
	}
}

