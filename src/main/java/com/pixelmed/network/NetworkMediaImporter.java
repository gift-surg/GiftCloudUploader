/* Copyright (c) 2001-2006, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.SetOfDicomFiles;

import com.pixelmed.utils.MessageLogger;
import com.pixelmed.utils.PrintStreamMessageLogger;

/**
 * <p>This class is designed to support the importation of DICOM files from
 * interchange media (such as CDs and DVDs) and their transfer over the
 * network as C-STORE requests to a specified AE.</p>
 * 
 * @see com.pixelmed.dicom.MediaImporter
 * @see com.pixelmed.database.DatabaseMediaImporter
 * 
 * @author	dclunie
 */
public class NetworkMediaImporter extends MediaImporter {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/NetworkMediaImporter.java,v 1.2 2006/10/19 20:09:15 dclunie Exp $";

	/***/
	protected SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();

	protected class OurMultipleInstanceTransferStatusHandler extends MultipleInstanceTransferStatusHandler {
		protected MessageLogger logger;
		
		public OurMultipleInstanceTransferStatusHandler(MessageLogger logger) {
			this.logger = logger;
		}
		
		public void updateStatus(int nRemaining,int nCompleted,int nFailed,int nWarning,String sopInstanceUID) {
//System.err.println("Sent "+sopInstanceUID);
			if (logger != null) {
				logger.sendLn("Transferred "+sopInstanceUID);
			}
		}
	}

	public NetworkMediaImporter(String hostname,int port,String calledAETitle,String callingAETitle,
			String pathName,MessageLogger logger,int debugLevel) {
		super(logger);
		try {
			importDicomFiles(pathName);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
//System.err.println(setOfDicomFiles);
		if (setOfDicomFiles.isEmpty()) {
				logger.sendLn("Finished ... nothing to transfer");
		}
		else {
			if (logger != null) {
				logger.sendLn("Starting network transfer ...");
			}
			new StorageSOPClassSCU(hostname,port,calledAETitle,callingAETitle,
				setOfDicomFiles,
				0/*compressionLevel*/,
				(logger == null ? null : new OurMultipleInstanceTransferStatusHandler(logger)),
				null/*moveOriginatorApplicationEntityTitle*/,0/*moveOriginatorMessageID*/,
				debugLevel);
			if (logger != null) {
				logger.sendLn("Finished import and transfer");
			}
		}
	}

	/**
	 * <p>Adds the specified file name and its characteristics to the list to be transferred.</p>
	 *
	 * <p>If any errors are encountered during this process, the exceptions
	 * are caught, logged to stderr, and the file will not be transferred.</p>
	 *
	 * <p>Note that the actual transfer is performed later once the characteristics
	 * of all the files to be transferred has been ascertained.</p>
	 *
	 * @param	mediaFileName	the fully qualified path name to a DICOM file
	 */
	protected void doSomethingWithDicomFileOnMedia(String mediaFileName) {
//System.err.println("NetworkMediaImporter:doSomethingWithDicomFile(): "+mediaFileName);
//System.err.println("Importing "+mediaFileName);
		try {
			setOfDicomFiles.add(mediaFileName);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * <p>Import DICOM files and send to the specified AE as C-STORE requests.</p>
	 *
	 * @param	arg	array of five strings - their hostname, their port, their AE Title, our AE Title,
	 *			and the path to the media or folder containing the files to import and send
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 5) {
				String           hostname=arg[0];
				int                  port=Integer.parseInt(arg[1]);
				String      calledAETitle=arg[2];
				String     callingAETitle=arg[3];
				String           pathName=arg[4];
				MessageLogger logger = new PrintStreamMessageLogger(System.err);
				//MessageLogger logger = null;
				new NetworkMediaImporter(hostname,port,calledAETitle,callingAETitle,pathName,logger,0/*debugLevel*/);
			}
			else {
				throw new Exception("Argument list must be 5 values");
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
}



