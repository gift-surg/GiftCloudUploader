/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.FileUtilities;
import com.pixelmed.utils.MessageLogger;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>This class is designed to support the importation of DICOM files from
 * interchange media (such as CDs and DVDs).</p>
 * 
 * <p>It supports locating a DICOMDIR file and iterating through the list of
 * referenced files.</p>
 * 
 * <p>The actual work (e.g. to import the file into a database or similar) is
 * performed by the implementation of the {@link MediaImporter#doSomethingWithDicomFileOnMedia(String) doSomethingWithDicomFileOnMedia}
 * method in a sub-class of this class.</p>
 *
 * @see com.pixelmed.database.DatabaseMediaImporter
 * 
 * @author	dclunie
 */
public class MediaImporter {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/MediaImporter.java,v 1.14 2014/11/26 14:50:49 dclunie Exp $";

	protected String mediaDirectoryPath;
	protected MessageLogger logger;
	protected JProgressBar progressBar;
	
	/**
	 * @param	s	message to log
	 */
	protected void logLn(String s) {
		if (logger != null) {
			logger.sendLn(s);
		}
		//System.err.println(s);
	}
		
	/**
	 * <p>Construct an importer that will looked for files in the system default path.</p>
	 *
	 * @param	logger			where to send status updates as files are read (may be null for no logging)
	 */
	public MediaImporter(MessageLogger logger) {
		mediaDirectoryPath=null;
		this.logger=logger;
	}

	/**
	 * <p>Construct an importer that will looked for files in the specified path.</p>
	 *
	 * @param	mediaDirectoryPath	where to begin looking for the DICOMDIR and DICOM files
	 * @param	logger			where to send status updates as files are read (may be null for no logging)
	 */
	public MediaImporter(String mediaDirectoryPath,MessageLogger logger) {
		this.mediaDirectoryPath=mediaDirectoryPath;
		this.logger=logger;
	}

    /**
	 * <p>Check for valid information, and that the file is not compressed or not a suitable storage object for import.</p>
	 *
	 * @param	sopClassUID
	 * @param	transferSyntaxUID
	 */
	protected boolean isOKToImport(String sopClassUID,String transferSyntaxUID) {
		return sopClassUID != null
		    && (SOPClass.isImageStorage(sopClassUID) || (SOPClass.isNonImageStorage(sopClassUID) && ! SOPClass.isDirectory(sopClassUID)))
		    && transferSyntaxUID != null
		    && (transferSyntaxUID.equals(TransferSyntax.ImplicitVRLittleEndian)
		     || transferSyntaxUID.equals(TransferSyntax.ExplicitVRLittleEndian)
		     || transferSyntaxUID.equals(TransferSyntax.ExplicitVRBigEndian));
	}

	// copied from SynchronizeFromRemoteSCP ... should refactor :(
	protected static class OurReadTerminationStrategy implements AttributeList.ReadTerminationStrategy {
		public boolean terminate(AttributeList attributeList,AttributeTag tag,long byteOffset) {
			return tag.getGroup() > 0x0008;
		}
	}
	
	protected final static AttributeList.ReadTerminationStrategy terminateAfterIdentifyingGroup = new OurReadTerminationStrategy();
	
	/**
	 * <p>Read a DICOMDIR file, and then import any DICOM files that it references.</p>
	 *
	 * <p>How errors are handled during the importation of the referenced files
	 * depends on the implementation of {@link MediaImporter#doSomethingWithDicomFileOnMedia(String) doSomethingWithDicomFileOnMedia}
	 * in the sub-class. Any such errors will not interrupt the execution of this method (i.e., will not prevent the
	 * importation of the remaining files).</p>
	 *
	 * @param	pathName		the path name to a DICOMDIR file or folder containing a DICOMDIR file
	 *
	 * @throws		IOException		thrown if the DICOMDIR file (but not any referenced files) cannot be opened or read
	 * @throws		DicomException		thrown if the DICOMDIR file cannot be parsed
	 */
	public void importDicomFiles(String pathName, final Progress progress) throws IOException, DicomException {
		if (pathName != null) {
			File path = new File(pathName);
			File dicomdirFile = null;		// look for DICOMDIR here or in root folder of here, with various case permutations
			if (path != null && path.exists()) {
				if (path.isFile() && path.getName().toUpperCase(java.util.Locale.US).equals("DICOMDIR")) {
					dicomdirFile=path;
				}
				else if (path.isDirectory()) {
					File tryFile = new File(path,"DICOMDIR");
					if (tryFile != null && tryFile.exists()) {
						dicomdirFile=tryFile;
					}
					else {
						tryFile = new File(path,"Dicomdir");
						if (tryFile != null && tryFile.exists()) {
							dicomdirFile=tryFile;
						}
						else {
							tryFile = new File(path,"dicomdir");
							if (tryFile != null && tryFile.exists()) {
								dicomdirFile=tryFile;
							}
							// else give up
						}
					}
				}
			}
			if (dicomdirFile != null) {
				logLn("Found DICOMDIR at: "+dicomdirFile);
				DicomInputStream i = new DicomInputStream(new BufferedInputStream(new FileInputStream(dicomdirFile)));
				AttributeList list = new AttributeList();
				list.read(i);
				i.close();
				DicomDirectory dicomDirectory = new DicomDirectory(list);
				HashMap allDicomFiles = dicomDirectory.findAllContainedReferencedFileNamesAndTheirRecords(dicomdirFile.getParentFile().getPath());
				if (progress != null) {
                    progress.updateProgressBar(0, allDicomFiles.size());
				}
				int count = 0;
				Iterator it = allDicomFiles.keySet().iterator();
				while (it.hasNext()) {
					String mediaFileName = (String)it.next();
					if (mediaFileName != null) {
						boolean goodToGo = false;
						String sopClassUID = "";
						String transferSyntaxUID = "";
						DicomDirectoryRecord record = (DicomDirectoryRecord)(allDicomFiles.get(mediaFileName));
						if (record != null) {
							AttributeList rlist = ((DicomDirectoryRecord)record).getAttributeList();
							if (rlist != null) {
								sopClassUID = Attribute.getSingleStringValueOrNull(rlist,TagFromName.ReferencedSOPClassUIDInFile);
								transferSyntaxUID = Attribute.getSingleStringValueOrNull(rlist,TagFromName.ReferencedTransferSyntaxUIDInFile);
								if (sopClassUID == null || transferSyntaxUID == null) {
									// the directory record is invalid; these should be present
									// don't give up though ... try reading the meta-information header ...
									try {
										DicomInputStream di = new DicomInputStream(new BufferedInputStream(new FileInputStream(mediaFileName)));
										if (di.haveMetaHeader()) {
											AttributeList dlist = new AttributeList();
											dlist.readOnlyMetaInformationHeader(di);
											// Don't replace them unless they were null; they might be missing in the meta header !
											if (sopClassUID == null) {
												sopClassUID = Attribute.getSingleStringValueOrNull(dlist,TagFromName.MediaStorageSOPClassUID);
											}
											if (transferSyntaxUID == null) {
												transferSyntaxUID = Attribute.getSingleStringValueOrNull(dlist,TagFromName.TransferSyntaxUID);
											}
										}
										di.close();
									}
									catch (Exception e) {
										// ignore the error ... will fail on null sopClassUID or transferSyntaxUID
									}
								}
								if (isOKToImport(sopClassUID,transferSyntaxUID)) {
									goodToGo=true;
								}
								else {
									logLn("Is a DICOM file but bad meta-header, not a storage object, or is compressed: "
										+mediaFileName+" SOP Class="+sopClassUID+", Transfer Syntax="+transferSyntaxUID);
								}
							}
						}
						if (goodToGo) {
							//logLn("Is a suitable DICOMDIR referenced file: "+mediaFileName);
							doSomethingWithDicomFileOnMedia(mediaFileName,transferSyntaxUID,sopClassUID);
						}
						else {
							//logLn("Not a suitable DICOMDIR referenced file: "+mediaFileName);
							doSomethingWithUnwantedFileOnMedia(mediaFileName,transferSyntaxUID,sopClassUID);
						}
					}
					++count;
					if (progress != null) {
                        progress.updateProgressBar(count);
					}
				}
			}
			else {
				ArrayList listOfAllFiles = FileUtilities.listFilesRecursively(path);
				if (progress != null) {
                    progress.updateProgressBar(0, listOfAllFiles.size());
				}
				int count = 0;
				Iterator it = listOfAllFiles.iterator();
				while (it.hasNext()) {
					File mediaFile = (File)it.next();
					if (mediaFile != null) {
						// It might or might not be a DICOM file ... only way to tell is to try it
						try {
							String sopClassUID = "";
							String transferSyntaxUID = "";
							DicomInputStream i = new DicomInputStream(new BufferedInputStream(new FileInputStream(mediaFile)));
							boolean goodToGo = false;
							if (i.haveMetaHeader()) {
								AttributeList list = new AttributeList();
								list.readOnlyMetaInformationHeader(i);
								sopClassUID = Attribute.getSingleStringValueOrNull(list,TagFromName.MediaStorageSOPClassUID);
								transferSyntaxUID = Attribute.getSingleStringValueOrNull(list,TagFromName.TransferSyntaxUID);
								if (isOKToImport(sopClassUID,transferSyntaxUID)) {
									goodToGo=true;
								}
								else {
									logLn("Is a DICOM file but bad meta-header, not a storage object, or is compressed with a scheme that is not supported: "
										+mediaFile+" SOP Class="+sopClassUID+", Transfer Syntax="+transferSyntaxUID);
								}
							}
							else {
								// no meta information header ... assume default Transfer Syntax and try to read SOP Class IOD ...
								logLn("No meta information header, so guessing is a DICOM file in implicit VR and attempting to get SOP Class UID: "+mediaFile);
								AttributeList list = new AttributeList();
								list.read(i,terminateAfterIdentifyingGroup);
								sopClassUID = Attribute.getSingleStringValueOrNull(list,TagFromName.SOPClassUID);
								transferSyntaxUID = TransferSyntax.ImplicitVRLittleEndian;
								if (isOKToImport(sopClassUID,transferSyntaxUID)) {
									goodToGo=true;
								}
								else {
									logLn("Is not a DICOM file, or not a storage object, or is compressed with a scheme that is not supported: "
										+mediaFile+" SOP Class="+sopClassUID+", Transfer Syntax="+transferSyntaxUID);
								}
							}
							i.close();	// do this BEFORE calling the handler, just in case
							if (goodToGo) {
								//logLn("Is a DICOM file that is wanted: "+mediaFile);
								doSomethingWithDicomFileOnMedia(mediaFile.getPath(),transferSyntaxUID,sopClassUID);
							}
							else {
								//logLn("Not a DICOM PS 3.10 file or not one that is wanted: "+mediaFile);
								doSomethingWithUnwantedFileOnMedia(mediaFile.getPath(),transferSyntaxUID,sopClassUID);
							}
						}
						catch (Exception e) {
							//logLn("Not a DICOM file: "+mediaFile);
							doSomethingWithUnwantedFileOnMedia(mediaFile.getPath(),"","");
						}
					}
					++count;
					if (progress != null) {
                        progress.updateProgressBar(count);
					}
				}
			}
		}
		logLn("Media import complete");
	}
	
	/**
	 * <p>Do something with the unwanted (possibly DICOM file) that has been encountered.</p>
	 *
	 * <p>This method needs to be implemented in a sub-class to do anything useful.
	 * The default method does nothing.</p>
	 *
	 * <p>"Unwanted" files are those that are not DICOM files or DICOM files for which {@link MediaImporter#isOKToImport(String,String) isOKToImport(String sopClassUID,String transferSyntaxUID)} returns false.</p>
	 *
	 * <p>This method does not define any exceptions and hence must handle any
	 * errors locally.</p>
	 *
	 * @param	mediaFileName		the fully qualified path name to a DICOM file
	 * @param	transferSyntaxUID	the Transfer Syntax of the Data Set if a DICOM file, from the DICOMDIR or Meta Information Header
	 * @param	sopClassUID			the SOP Class of the Data Set if a DICOM file, from the DICOMDIR or Meta Information Header
	 */
	protected void doSomethingWithUnwantedFileOnMedia(String mediaFileName,String transferSyntaxUID,String sopClassUID) {
		logLn("Not a DICOM file, not a DICOM PS 3.10 file or not one that is wanted: "+mediaFileName);
	}
	
	/**
	 * <p>Do something with the referenced DICOM file that has been encountered.</p>
	 *
	 * <p>This method may be implemented in a sub-class to do something useful that requires knowledge of the Transfer Syntax or SOP Class.
	 * The default method calls the simpler method {@link MediaImporter#doSomethingWithDicomFileOnMedia(String) doSomethingWithDicomFileOnMedia(String mediaFileName)}.</p>
	 *
	 * <p>"Wanted" files are those that are DICOM files for which {@link MediaImporter#isOKToImport(String,String) isOKToImport(String sopClassUID,String transferSyntaxUID)} returns true.</p>
	 *
	 * <p>This method does not define any exceptions and hence must handle any
	 * errors locally.</p>
	 *
	 * @param	mediaFileName		the fully qualified path name to a DICOM file
	 * @param	transferSyntaxUID	the Transfer Syntax of the Data Set in the DICOM file, from the DICOMDIR or Meta Information Header
	 * @param	sopClassUID			the SOP Class of the Data Set in the DICOM file, from the DICOMDIR or Meta Information Header
	 */
	protected void doSomethingWithDicomFileOnMedia(String mediaFileName,String transferSyntaxUID,String sopClassUID) {
		doSomethingWithDicomFileOnMedia(mediaFileName);
	}
	
	/**
	 * <p>Do something with the referenced DICOM file that has been encountered.</p>
	 *
	 * <p>This method needs to be implemented in a sub-class to do anything useful,
	 * unless {@link MediaImporter#doSomethingWithDicomFileOnMedia(String,String,String) doSomethingWithDicomFileOnMedia(String mediaFileName,String transferSyntaxUID,String sopClassUID)} has been overridden instead.
	 * The default method does nothing.</p>
	 *
	 * <p>"Wanted" files are those that are DICOM files for which {@link MediaImporter#isOKToImport(String,String) isOKToImport(String sopClassUID,String transferSyntaxUID)} returns true.</p>
	 *
	 * <p>This method does not define any exceptions and hence must handle any
	 * errors locally.</p>
	 *
	 * @param	mediaFileName	the fully qualified path name to a DICOM file
	 */
	protected void doSomethingWithDicomFileOnMedia(String mediaFileName) {
		//logLn("MediaImporter.doSomethingWithDicomFile(): "+mediaFileName);
		logLn("Is a DICOM PS3.10 file that is wanted: "+mediaFileName);
	}
	
	/**
	 * @return	the directory last used to perform an import
	 */
	public String getDirectory() { return mediaDirectoryPath; }

}


