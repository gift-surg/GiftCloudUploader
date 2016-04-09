/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.FileUtilities;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;

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
 * performed by the implementation of the {@link MediaImporter#doSomethingWithDicomFileOnMedia(String, AttributeList) doSomethingWithDicomFileOnMedia}
 * method in a sub-class of this class.</p>
 *
 *
 * @author	dclunie
 */
public class MediaImporter {
	protected GiftCloudReporterFromApplication reporter;

	/**
	 * <p>Construct an importer that will looked for files in the system default path.</p>
	 *
	 * @param	reporter			where to send status updates as files are read (may be null for no logging)
	 */
	public MediaImporter(GiftCloudReporterFromApplication reporter) {
		this.reporter = reporter;
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
	 * <p>Imports a single DICOM file, a DICOMDIR file, or recursively imports a directory.</p>
	 *
	 * <p>How errors are handled during the importation of the referenced files
	 * depends on the implementation of {@link MediaImporter#doSomethingWithDicomFileOnMedia(String, AttributeList) doSomethingWithDicomFileOnMedia}
	 * in the sub-class. Any such errors will not interrupt the execution of this method (i.e., will not prevent the
	 * importation of the remaining files).</p>
	 *
	 * @param	path		the path name to a DICOMDIR file or folder containing a DICOMDIR file
	 *
	 * @throws		IOException		thrown if the DICOMDIR file (but not any referenced files) cannot be opened or read
	 * @throws		DicomException		thrown if the DICOMDIR file cannot be parsed
	 */
	public boolean importDicomFileOrPath(File path, final Progress progress) throws IOException, DicomException {
		if (path == null) {
			return false;
		}

		if (path.isDirectory()) {
			return importNonDicomDirFilesRecursively(path, progress);
		} else {
			return importFile(path, progress);
		}
	}

	private boolean importFile(File path, Progress progress) throws IOException, DicomException {
		if (isDicomDir(path)) {
            return importDicomDirFile(path, progress);
        } else {
            return importNonDicomdirFile(path);
        }
	}

	private boolean importNonDicomDirFilesRecursively(File path, Progress progress) {
		boolean anyFiles = false;
		ArrayList<File> listOfAllFiles = FileUtilities.listFilesRecursively(path);
		if (progress != null) {
			progress.updateProgressBar(0, listOfAllFiles.size());
        }
		int count = 0;
		for (File mediaFile : listOfAllFiles) {
            if (mediaFile != null) {
                anyFiles = importNonDicomdirFile(mediaFile) || anyFiles;
            }
            ++count;
            if (progress != null) {
                progress.updateProgressBar(count);
            }
        }
		return anyFiles;
	}

	private boolean importDicomDirFile(File dicomdirFile, Progress progress) throws IOException, DicomException {
		boolean anyFiles = false;
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
                        }
                    }
                }
                if (goodToGo) {
                    doSomethingWithDicomFileOnMedia(mediaFileName, list);
                    anyFiles = true;
                }
                else {
                    doSomethingWithUnwantedFileOnMedia(mediaFileName);
                }
            }
            ++count;
            if (progress != null) {
				progress.updateProgressBar(count);
            }
        }
		return anyFiles;
	}

	private boolean isDicomDir(final File pathOrFile) {
		return (pathOrFile != null && pathOrFile.exists() && pathOrFile.isFile() && pathOrFile.getName().toUpperCase(java.util.Locale.US).equals("DICOMDIR"));
	}

	private boolean importNonDicomdirFile(File mediaFile) {
		if (mediaFile == null || isDicomDir((mediaFile))) {
			return false;
		}
		boolean fileImported = false;

		// It might or might not be a DICOM file ... only way to tell is to try it
		try {
			String sopClassUID;
			String transferSyntaxUID;
			DicomInputStream i = new DicomInputStream(new BufferedInputStream(new FileInputStream(mediaFile)));

			AttributeList list = new AttributeList();
			if (i.haveMetaHeader()) {
				list.readOnlyMetaInformationHeader(i);
				sopClassUID = Attribute.getSingleStringValueOrNull(list, TagFromName.MediaStorageSOPClassUID);
				transferSyntaxUID = Attribute.getSingleStringValueOrNull(list, TagFromName.TransferSyntaxUID);
			}
			else {
				// no meta information header, so assume implicit vr and default Transfer Syntax and try to read SOP Class IOD ...
				list.read(i, terminateAfterIdentifyingGroup);
				sopClassUID = Attribute.getSingleStringValueOrNull(list, TagFromName.SOPClassUID);
				transferSyntaxUID = TransferSyntax.ImplicitVRLittleEndian;
			}

			i.close();	// do this before calling the handler, just in case

			if (isOKToImport(sopClassUID,transferSyntaxUID)) {
				doSomethingWithDicomFileOnMedia(mediaFile.getPath(), list);
				fileImported = true;
			} else {
				// either is not a DICOM file, has bad meta-header, not a storage object, or is compressed with a scheme that is not supported
				//logLn("Not a DICOM PS 3.10 file or not one that is wanted: "+mediaFile);
				doSomethingWithUnwantedFileOnMedia(mediaFile.getPath());
			}

		}
		catch (Exception e) {
			// Most likely not a DICOM file
			doSomethingWithUnwantedFileOnMedia(mediaFile.getPath());
		}
		return fileImported;
	}

	/**
	 * <p>Do something with the referenced DICOM file that has been encountered.</p>
	 *
	 * <p>This method needs to be implemented in a sub-class to do anything useful,
	 * The default method does nothing.</p>
	 *
	 * <p>"Wanted" files are those that are DICOM files for which {@link MediaImporter#isOKToImport(String,String) isOKToImport(String sopClassUID,String transferSyntaxUID)} returns true.</p>
	 *
	 * <p>This method does not define any exceptions and hence must handle any
	 * errors locally.</p>
	 *
	 * @param    mediaFileName    the fully qualified path name to a DICOM file
	 * @param list
	 */
	protected void doSomethingWithDicomFileOnMedia(String mediaFileName, AttributeList list) {
//		reporter.sendLn("Is a DICOM PS3.10 file that is wanted: " + mediaFileName);
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
	 */
	protected void doSomethingWithUnwantedFileOnMedia(String mediaFileName) {
//		reporter.sendLn("Not a DICOM file, not a DICOM PS 3.10 file or not one that is wanted: "+mediaFileName);
	}

}


