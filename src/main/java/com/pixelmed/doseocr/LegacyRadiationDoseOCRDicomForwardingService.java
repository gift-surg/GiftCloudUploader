/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.doseocr;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.TagFromName;

import com.pixelmed.dose.CTDose;

import com.pixelmed.doseocr.ExposureDoseSequence;
import com.pixelmed.doseocr.OCR;

import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.ReceivedObjectHandler;
import com.pixelmed.network.StorageSOPClassSCPDispatcher;
import com.pixelmed.network.StorageSOPClassSCU;

//import com.pixelmed.utils.FileUtilities;

import java.io.BufferedInputStream;
//import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>A class to wait for incoming dose screen images, perform OCR to create Radiation Dose SRs and send RDSRs to a pre-configured DICOM destination.</p>
 *
 * <p>The class has no public methods other than the constructor and a main method that is useful as a utility.</p>
 *
 * @author	dclunie
 */
public class LegacyRadiationDoseOCRDicomForwardingService {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/doseocr/LegacyRadiationDoseOCRDicomForwardingService.java,v 1.5 2014/09/09 20:34:09 dclunie Exp $";
	
	protected static long TIMEOUT_BEFORE_PROCESSING_SERIES_MS = 10*60*1000l;// 10 minutes
	protected static long TIMEOUT_BEFORE_CHECKING_FOR_WORK_MS = 10*1000l;	// 10 secs
	
	protected int debugLevel;
	protected int networkDebugLevel;
	
	protected String theirHost;
	protected int theirPort;
	protected String theirAETitle;
	
	protected String ourAETitle;
	
	protected class Series {
		String seriesInstanceUID;
		long lastReceivedTime;
		List<String> fileNames = new LinkedList<String>();
		int numberWanted;
		
		Series(String seriesInstanceUID,int numberOfSeriesRelatedInstances) {
			this.seriesInstanceUID = seriesInstanceUID;
			numberWanted = numberOfSeriesRelatedInstances;
		}
		
		void addFile(String receivedFileName,long receivedTime) {
			fileNames.add(receivedFileName);
			lastReceivedTime = receivedTime;
		}
		
		boolean isReadyToProcess() {
			long timeSinceLast = System.currentTimeMillis() - lastReceivedTime;
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.Series.isReadyToProcess(): System.currentTimeMillis() - lastReceivedTime = "+timeSinceLast);
			return (numberWanted > 0 && numberWanted == fileNames.size())
			    || (timeSinceLast > TIMEOUT_BEFORE_PROCESSING_SERIES_MS);
		}
	}

	protected class SeriesQueue {
		Map<String,Series> queuedMultiPageInstancesIndexedBySeriesInstanceUID = new HashMap<String,Series>();
		
		SeriesQueue() {
			new java.util.Timer().schedule(new java.util.TimerTask() { public void run() { synchronized (SeriesQueue.this) { SeriesQueue.this.notify(); } } },TIMEOUT_BEFORE_CHECKING_FOR_WORK_MS,TIMEOUT_BEFORE_CHECKING_FOR_WORK_MS);
		}
	
		synchronized void addFile(String seriesInstanceUID,String receivedFileName,long receivedTime,int numberOfSeriesRelatedInstances) {
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.SeriesQueue.addFile(): SeriesInstanceUID "+seriesInstanceUID);
			Series series = queuedMultiPageInstancesIndexedBySeriesInstanceUID.get(seriesInstanceUID);
			if (series == null) {
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.SeriesQueue.addFile(): SeriesInstanceUID "+seriesInstanceUID+" first instance");
				series = new Series(seriesInstanceUID,numberOfSeriesRelatedInstances);
				queuedMultiPageInstancesIndexedBySeriesInstanceUID.put(seriesInstanceUID,series);
			}
			series.addFile(receivedFileName,receivedTime);
			notify();
		}

		synchronized Series getWork() throws InterruptedException {
			while (true) {
				for (String seriesInstanceUID : queuedMultiPageInstancesIndexedBySeriesInstanceUID.keySet()) {	// may be empty
					Series series = queuedMultiPageInstancesIndexedBySeriesInstanceUID.get(seriesInstanceUID);
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.SeriesQueue.getWork(): checking series is ready "+seriesInstanceUID);
					if (series != null && series.isReadyToProcess()) {
						queuedMultiPageInstancesIndexedBySeriesInstanceUID.remove(seriesInstanceUID);
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.SeriesQueue.getWork(): series is ready "+seriesInstanceUID);
						return series;
					}
				}
				wait();	// will block here until something received or woken by timer task to check again
			}
		}
	}

	protected SeriesQueue seriesQueue = new SeriesQueue();
	
	protected class SeriesProcessor implements Runnable {
		
		SeriesProcessor() {
		}
		
		public void run() {
			try {
				while (true) {
					// Retrieve some work; block if the queue is empty
					Series series = seriesQueue.getWork();
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.SeriesProcessor.run(): SeriesInstanceUID "+series.seriesInstanceUID+" is ready");
					OCR ocr = new OCR(series.fileNames,0/*debugLevel*/);
//System.err.print(ocr);
					CTDose ctDose = ocr.getCTDoseFromOCROfDoseScreen(ocr,debugLevel,null/*eventDataFromImages*/,true/*buildSR*/);
					if (ctDose != null) {
						sendSRFile(ctDose);
					}
					for (String f : series.fileNames) {
						try {
							if (!new File(f).delete()) {
								throw new DicomException("Failed to delete queued file that we have extracted from "+f);
							}
						}
						catch (Exception e) {
							e.printStackTrace(System.err);
						}
					}
				}
			}
			catch (InterruptedException e) {
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	protected void sendSRFile(CTDose ctDose) {
		try {
			File ctDoseSRFile = File.createTempFile("ocrrdsr",".dcm");
			String ctDoseSRFileName = ctDoseSRFile.getCanonicalPath();
			try {
				AttributeList ctDoseList = ctDose.getAttributeList();
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.sendSRFile(): adding our own newly created SR file = "+ctDoseSRFileName);
				ctDose.write(ctDoseSRFileName,ourAETitle,this.getClass().getCanonicalName());	// has side effect of updating list returned by ctDose.getAttributeList(); uncool :(
				new StorageSOPClassSCU(theirHost,theirPort,theirAETitle,ourAETitle,ctDoseSRFileName,
					Attribute.getSingleStringValueOrNull(ctDoseList,TagFromName.SOPClassUID),
					Attribute.getSingleStringValueOrNull(ctDoseList,TagFromName.SOPInstanceUID),
					0/*compressionLevel*/,
					networkDebugLevel);
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
			}
			if (ctDoseSRFile != null) {
				try {
					if (!ctDoseSRFile.delete()) {
						throw new DicomException("Failed to delete RDSR file that we created "+ctDoseSRFileName);
					}
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	protected class ReceivedFileProcessor implements Runnable {
		String receivedFileName;
		AttributeList list;
		
		ReceivedFileProcessor(String receivedFileName) {
			this.receivedFileName = receivedFileName;
		}
		
		public void run() {
			try {
if (debugLevel > 1) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.ReceivedFileProcessor.run(): receivedFileName = "+receivedFileName);
				long receivedTime = System.currentTimeMillis();
				FileInputStream fis = new FileInputStream(receivedFileName);
				DicomInputStream i = new DicomInputStream(new BufferedInputStream(fis));
				AttributeList list = new AttributeList();
				list.read(i);
				i.close();
				fis.close();
				
				{
					CTDose ctDose = null;
					if (OCR.isDoseScreenInstance(list)) {
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.ReceivedFileProcessor.run(): isDoseScreenInstance");
						int numberOfSeriesRelatedInstances = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfSeriesRelatedInstances,-1);
						if (numberOfSeriesRelatedInstances == 1) {
							OCR ocr = new OCR(list,0/*debugLevel*/);
//System.err.print(ocr);
							ctDose = ocr.getCTDoseFromOCROfDoseScreen(ocr,debugLevel,null/*eventDataFromImages*/,true/*buildSR*/);
						}
						else {
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.ReceivedFileProcessor.run(): numberOfSeriesRelatedInstances = "+numberOfSeriesRelatedInstances);
							String seriesInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesInstanceUID);
							if (seriesInstanceUID.length() > 0) {
								seriesQueue.addFile(seriesInstanceUID,receivedFileName,receivedTime,numberOfSeriesRelatedInstances);
								receivedFileName = null;	// to suppress deletion until queue processed
							}
						}
					}
					else if (ExposureDoseSequence.isPhilipsDoseScreenInstance(list)) {
if (debugLevel > 0) System.err.println("LegacyRadiationDoseOCRDicomForwardingService.ReceivedFileProcessor.run(): isPhilipsDoseScreenInstance");
						ctDose = ExposureDoseSequence.getCTDoseFromExposureDoseSequence(list,debugLevel,null/*eventDataFromImages*/,true/*buildSR*/);
					}
					if (ctDose != null) {
						sendSRFile(ctDose);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
			}
			if (receivedFileName != null) {
				try {
					if (!new File(receivedFileName).delete()) {
						throw new DicomException("Failed to delete received file that we have successfully extracted from "+receivedFileName);
					}
				}
				catch  (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}
	
	/**
	 *
	 */
	protected class OurReceivedObjectHandler extends ReceivedObjectHandler {
		/**
		 * @param	dicomFileName
		 * @param	transferSyntax
		 * @param	callingAETitle
		 * @throws	IOException
		 * @throws	DicomException
		 * @throws	DicomNetworkException
		 */
		public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle)
				throws DicomNetworkException, DicomException, IOException {
			if (dicomFileName != null) {
if (debugLevel > 0) System.err.println("Received: "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax);
				try {
					new Thread(new ReceivedFileProcessor(dicomFileName)).start();		// on separate thread, else will block and the C-STORE response will be delayed
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}

		}
	}
	
	/**
	 * <p>Wait for incoming dose screen images, perform OCR to create Radiation Dose SRs and send RDSRs to specified DICOM destination.</p>
	 *
	 * @param	ourPort
	 * @param	ourAETitle
	 * @param	theirHost
	 * @param	theirPort
	 * @param	theirAETitle
	 * @param	savedImagesFolder
	 * @param	debugLevel
	 */
	public LegacyRadiationDoseOCRDicomForwardingService(int ourPort,String ourAETitle,String theirHost,int theirPort,String theirAETitle,File savedImagesFolder,int debugLevel) throws IOException {
		this(ourPort,ourAETitle,theirHost,theirPort,theirAETitle,savedImagesFolder,debugLevel,debugLevel);
	}
	
	/**
	 * <p>Wait for incoming dose screen images, perform OCR to create Radiation Dose SRs and send RDSRs to specified DICOM destination.</p>
	 *
	 * @param	ourPort
	 * @param	ourAETitle
	 * @param	theirHost
	 * @param	theirPort
	 * @param	theirAETitle
	 * @param	savedImagesFolder
	 * @param	debugLevel
	 * @param	networkDebugLevel
	 */
	public LegacyRadiationDoseOCRDicomForwardingService(int ourPort,String ourAETitle,String theirHost,int theirPort,String theirAETitle,File savedImagesFolder,int debugLevel,int networkDebugLevel) throws IOException {
		this.ourAETitle        = ourAETitle;
		this.theirHost         = theirHost;
		this.theirPort         = theirPort;
		this.theirAETitle      = theirAETitle;
		this.debugLevel        = debugLevel;
		this.networkDebugLevel = networkDebugLevel;
		// Start up DICOM association listener in background for receiving images  ...
if (debugLevel > 1) System.err.println("Starting up DICOM association listener ...");
		new Thread(new StorageSOPClassSCPDispatcher(ourPort,ourAETitle,savedImagesFolder,new OurReceivedObjectHandler(),networkDebugLevel)).start();
		new Thread(new SeriesProcessor()).start();
	}

	/**
	 * <p>Wait for incoming dose screen images, perform OCR to create Radiation Dose SRs and send RDSRs to specified DICOM destination.</p>
	 *
	 * @param	arg	array of six or strings - our port, our AE Title, their hostname, their port, their AE Title,
	 *			and the debugging level and optionally a network debugging level (if absent defaults to the master debugging level)
	 */
	public static void main(String arg[]) {
		try {
			int ourPort;
			String ourAETitle;
			String theirHost;
			int theirPort;
			String theirAETitle;
			int debugLevel;
			int networkDebugLevel;
			if (arg.length >= 6) {
				        ourPort=Integer.parseInt(arg[0]);
				    ourAETitle=arg[1];
				     theirHost=arg[2];
				     theirPort=Integer.parseInt(arg[3]);
				  theirAETitle=arg[4];
				    debugLevel=Integer.parseInt(arg[5]);
			 networkDebugLevel=arg.length >= 7 ? Integer.parseInt(arg[6]) : debugLevel;
			}
			else {
				throw new Exception("Argument list must be 6 or 7 values");
			}
			File savedImagesFolder = new File(System.getProperty("java.io.tmpdir"));
			new LegacyRadiationDoseOCRDicomForwardingService(ourPort,ourAETitle,theirHost,theirPort,theirAETitle,savedImagesFolder,debugLevel,networkDebugLevel);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
}

