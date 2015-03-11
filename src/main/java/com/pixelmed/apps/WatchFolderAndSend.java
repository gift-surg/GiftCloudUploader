/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.SetOfDicomFiles;

import com.pixelmed.display.DialogMessageLogger;
import com.pixelmed.display.SafeFileChooser;

import com.pixelmed.network.ApplicationEntity;
import com.pixelmed.network.ApplicationEntityConfigurationDialog;
import com.pixelmed.network.ApplicationEntityMap;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.MultipleInstanceTransferStatusHandlerWithFileName;
import com.pixelmed.network.NetworkApplicationInformation;
import com.pixelmed.network.PresentationAddress;
import com.pixelmed.network.StorageSOPClassSCU;

import com.pixelmed.utils.FileUtilities;
import com.pixelmed.utils.MessageLogger;
import com.pixelmed.utils.PrintStreamMessageLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import javax.swing.JFileChooser;

/**
 * <p>A class for watching a folder and sending any DICOM files that appear to a pre-configured network remote Storage SCP AE.</p>
 *
 * <p>The class has no public methods other than the constructor and a main method that is useful as a utility.
 *
 * <p>For example:</p>
 * <pre>
java -cp ./pixelmed.jar \
	com.pixelmed.apps.WatchFolderAndSend \
	watchthisfolder \
	graytoo 11112 GRAYTOO_DV_11112
 * </pre>
 * <p>or, with a GUI:</p>
 * <pre>
java -cp ./pixelmed.jar \
	com.pixelmed.apps.WatchFolderAndSend
 * </pre>
 *
 * @author	dclunie
 */
public class WatchFolderAndSend {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/WatchFolderAndSend.java,v 1.5 2013/02/21 00:06:44 dclunie Exp $";
	
	protected static int sleepTimeBetweenCheckingForFolderContent = 10000;	// ms
	protected static int intervalAfterLastModificationWithinWhichDoNotSendFileYet = 1000;		// ms
	
	protected static String propertiesFileName = ".com.pixelmed.apps.WatchFolderAndSend.properties";
	protected static String localnameForRemoteAE = "remote";

	protected static String propertiesFilePath = FileUtilities.makePathToFileInUsersHomeDirectory(propertiesFileName);
	
	protected static ApplicationEntity getPropertiesEditInDialogAndSave(int debugLevel) {
		ApplicationEntity ae = null;
		Properties properties = new Properties(/*defaultProperties*/);
		try {
			File propertiesFile = new File(propertiesFilePath);
			if (propertiesFile.exists()) {
				FileInputStream in = new FileInputStream(propertiesFile);
				properties.load(in);
				in.close();
if (debugLevel > 0) System.err.println("WatchFolderAndSend().getPropertiesEditInDialogAndSave(): got existing properties "+properties);
				NetworkApplicationInformation nai = new NetworkApplicationInformation(properties);
				String aet = nai.getApplicationEntityTitleFromLocalName(localnameForRemoteAE);
				ApplicationEntityMap aemap = nai.getApplicationEntityMap();
				if (aet != null && aemap != null) {
					ae = new ApplicationEntity(aet);
					PresentationAddress pa = aemap.getPresentationAddress(aet);
					if (pa != null) {
						ae.setPresentationAddress(pa);
					}
if (debugLevel > 0) System.err.println("WatchFolderAndSend().getPropertiesEditInDialogAndSave(): extracted AE "+ae);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
		ae = ae == null
			? new ApplicationEntityConfigurationDialog(null/*Component parent*/,localnameForRemoteAE)
			: new ApplicationEntityConfigurationDialog(null/*Component parent*/,localnameForRemoteAE,ae);
		try {
			NetworkApplicationInformation nai = new NetworkApplicationInformation();
			nai.add(localnameForRemoteAE,ae);
			properties = nai.getProperties(properties);
if (debugLevel > 0) System.err.println("WatchFolderAndSend().getPropertiesEditInDialogAndSave(): saving revised properties "+properties);
			FileOutputStream out = new FileOutputStream(propertiesFilePath);
			properties.store(out,"Reconfigured from dialog");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return ae;
	}
	
	protected class OurMultipleInstanceTransferStatusHandlerWithFileName extends MultipleInstanceTransferStatusHandlerWithFileName {
		int verbosityLevel;
		int debugLevel;
		MessageLogger logger;
		
		OurMultipleInstanceTransferStatusHandlerWithFileName(int verbosityLevel,int debugLevel,MessageLogger logger) {
			this.verbosityLevel = verbosityLevel;
			this.debugLevel = debugLevel;
			this.logger = logger;
		}
		
		public  void updateStatus(int nRemaining,int nCompleted,int nFailed,int nWarning,String sopInstanceUID,String pathName,boolean success) {
			File file = new File(pathName);
			String fileName = file.getName();
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Send of "+fileName+" "+(success ? "succeeded" : "failed")); }
			if (success) {
				if (file.exists() && file.isFile()) {
if (debugLevel > 0) System.err.println("WatchFolderAndSend(): removing "+fileName);
					if (file.delete()) {
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Removed "+fileName); }
					}
					else {
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Failed to remove "+fileName); }
					}
				}
			}
			else {
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Leaving "+fileName); }
			}
		}
	}

	/**
	 * <p>Watch a folder and send any DICOM files that appear to a network remote Storage SCP AE.</p>
	 *
	 * <p>Removes files only after successful send attempt, and leaves them and retries if connection or send fails.</p>
	 *
	 * <p>Tries not to send files that are still being modified.</p>
	 *
	 * <p>Does NOT RECURSE into sub-folders, only processes file in supplied directory itself.</p>
	 *
	 * @param	watchedFolderName
	 * @param	remoteHost
	 * @param	remotePort
	 * @param	remoteAE
	 * @param	localAE
	 * @param	verbosityLevel
	 * @param	debugLevel
	 * @param	logger
	 */
	public WatchFolderAndSend(String watchedFolderName,
				String remoteHost,int remotePort,String remoteAE,
				String localAE,int verbosityLevel,int debugLevel,
				MessageLogger logger)
			throws DicomException, DicomNetworkException, IOException, InterruptedException {
		if (watchedFolderName != null) {
			File watchedFolder = new File(watchedFolderName);
			while (watchedFolder.exists() && watchedFolder.isDirectory()) {
if (debugLevel > 0) System.err.println("WatchFolderAndSend(): watched folder exists");
				SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();
				File[] files = watchedFolder.listFiles();
				if (files != null && files.length > 0) {
					for (int i=0; i<files.length; ++i) {
						File file = files[i];
						if (file.exists() && file.isFile()) {
							String fileName = file.getName();
							long lastModified = file.lastModified();
							long currentTime = System.currentTimeMillis();
if (debugLevel > 0) System.err.println("WatchFolderAndSend(): lastModified = "+lastModified);
if (debugLevel > 0) System.err.println("WatchFolderAndSend(): currentTime  = "+currentTime);
							if ((currentTime - lastModified) > intervalAfterLastModificationWithinWhichDoNotSendFileYet) {
								if (DicomFileUtilities.isDicomOrAcrNemaFile(file)) {
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Queueing "+fileName); }
									setOfDicomFiles.add(file);
								}
								else {
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Skipping non-DICOM file "+fileName); }
								}
							}
							else {
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Skipping file still being modified "+fileName); }
							}
						}
					}
					if (setOfDicomFiles != null & setOfDicomFiles.size() > 0) {
						if (new StorageSOPClassSCU(remoteHost,remotePort,remoteAE,localAE,setOfDicomFiles,0,
								new OurMultipleInstanceTransferStatusHandlerWithFileName(verbosityLevel,debugLevel,logger),
								null,0,debugLevel).encounteredTrappedExceptions()) {
if (verbosityLevel > 0 && logger != null) { logger.sendLn("Sending queued files problem - connection or association failure ?"); }
						}
					}
					// removal is done in OurMultipleInstanceTransferStatusHandlerWithFileName()
				}
if (debugLevel > 0) System.err.println("WatchFolderAndSend(): sleeping for "+sleepTimeBetweenCheckingForFolderContent+" mS");
				Thread.currentThread().sleep(sleepTimeBetweenCheckingForFolderContent);
			}
		}
	}

	/**
	 * <p>Watch a folder and send any DICOM files that appear to a network remote Storage SCP AE.</p>
	 *
	 * @param	arg		none if parameters are to be requested through a graphical interface, otherwise an array of 4 to 7 strings - the fully qualified path of the watched folder,
	 *					the remote hostname, remote port, and remote AE Title and optionally our AE Title, a verbosity level, and an integer debug level
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 0 || (arg.length >= 4 && arg.length <= 7)) {
				String watchedFolderName = null;
				String remoteHost = null;
				int remotePort = 0;
				String remoteAE = null;
				String localAE = "US";
				int verbosityLevel = 0;
				int debugLevel =  0;
				MessageLogger logger = null;
				if (arg.length == 0) {
					SafeFileChooser.SafeFileChooserThread fileChooserThread = new SafeFileChooser.SafeFileChooserThread(JFileChooser.DIRECTORIES_ONLY,null,"Select Watched Folder ...");
					java.awt.EventQueue.invokeAndWait(fileChooserThread);
					watchedFolderName=fileChooserThread.getSelectedFileName();
					if (watchedFolderName == null) {
						System.exit(0);
					}
					ApplicationEntity ae = getPropertiesEditInDialogAndSave(debugLevel);
					remoteHost = ae.getPresentationAddress().getHostname();
					remotePort = ae.getPresentationAddress().getPort();
					remoteAE   = ae.getDicomAETitle();
					verbosityLevel = 1;
					logger = new DialogMessageLogger("WatchFolderAndSend Log",512,384,true/*exitApplicationOnClose*/,true/*visible*/);
				}
				else {
					watchedFolderName = arg[0];
					remoteHost = arg[1];
					remotePort = Integer.parseInt(arg[2]);
					remoteAE = arg[3];
					if (arg.length > 4) { localAE = arg[4]; }
					if (arg.length > 5) { verbosityLevel = Integer.parseInt(arg[5]); }
					if (arg.length > 6) { debugLevel = Integer.parseInt(arg[6]); }
					logger = new PrintStreamMessageLogger(System.err);
				}
				File watchedFolder = new File(watchedFolderName);
				new WatchFolderAndSend(watchedFolderName,remoteHost,remotePort,remoteAE,localAE,verbosityLevel,debugLevel,logger);
			}
			else {
				System.err.println("Usage: java -cp ./pixelmed.jar com.pixelmed.apps.WatchFolderAndSend [watchedfolder remoteHost remotePort remoteAET [ourAET [verbositylevel [debuglevel]]]]");
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
}




