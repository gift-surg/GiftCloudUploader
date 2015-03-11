/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.TransferSyntax;

import com.pixelmed.network.Association;
import com.pixelmed.network.AssociationStatusHandler;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.PresentationContext;
import com.pixelmed.network.ReceivedObjectHandler;
import com.pixelmed.network.StorageSOPClassSCPDispatcher;
import com.pixelmed.network.StorageSOPClassSCU;
import com.pixelmed.network.TransferSyntaxSelectionPolicy;
import com.pixelmed.network.UnencapsulatedExplicitStoreFindMoveGetPresentationContextSelectionPolicy;

import java.io.File;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import junit.framework.*;

public class TestCStore extends TestCase {

	protected static final int    waitIntervalWhenSleeping = 10;	// in ms
	protected static final int    port = 11119;
	protected static final String scpAET = "TESTSTORESCP";
	protected static final String scuAET = "TESTSTORESCU";

	// constructor to support adding tests to suite ...
	
	public TestCStore(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCStore.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCStore");
		
		suite.addTest(new TestCStore("TestCStore_SendOneNotOfSetToDefaultSCPPresentationContextSelectionPolicySuccessfully"));
		suite.addTest(new TestCStore("TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicySuccessfully"));
		suite.addTest(new TestCStore("TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicyUnsuccessfully_RLE"));
		suite.addTest(new TestCStore("TestCStore_SendOneOfSetToLastRecognizedSCPPresentationContextSelectionPolicySuccessfully_RLE"));
		suite.addTest(new TestCStore("TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicyUnsuccessfully_MPEG"));
		suite.addTest(new TestCStore("TestCStore_SendOneOfSetToLastRecognizedSCPPresentationContextSelectionPolicySuccessfully_MPEG"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	protected volatile String lastReceivedDicomFileName;
	
	protected class OurReceivedObjectHandler extends ReceivedObjectHandler {
		public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle) throws DicomNetworkException, DicomException, IOException {
//System.err.println("Received: "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax);
			lastReceivedDicomFileName = dicomFileName;
		}
	}

	protected volatile boolean associationReleased;
	
	private class OurAssociationStatusHandler extends AssociationStatusHandler {
		public void sendAssociationReleaseIndication(Association a) throws DicomNetworkException, DicomException, IOException {
			if (a != null) {
//System.err.println("Association "+a.getAssociationNumber()+" from "+a.getCallingAETitle()+" released");
			}
			associationReleased = true;
		}
	}

	private class OurLastRecognizedPresentationContextSelectionPolicy extends UnencapsulatedExplicitStoreFindMoveGetPresentationContextSelectionPolicy {
		OurLastRecognizedPresentationContextSelectionPolicy() {
			super();
			transferSyntaxSelectionPolicy = new OurLastRecognizedTransferSyntaxSelectionPolicy();
		}
	}
	
	private class OurLastRecognizedTransferSyntaxSelectionPolicy extends TransferSyntaxSelectionPolicy {
		public LinkedList applyTransferSyntaxSelectionPolicy(LinkedList presentationContexts,int associationNumber,int debugLevel) {
			ListIterator pcsi = presentationContexts.listIterator();
			while (pcsi.hasNext()) {
				PresentationContext pc = (PresentationContext)(pcsi.next());
				String lastRecognized = null;
				List tsuids = pc.getTransferSyntaxUIDs();
				ListIterator tsuidsi = tsuids.listIterator();
				while (tsuidsi.hasNext()) {
					String transferSyntaxUID=(String)(tsuidsi.next());
					if (new TransferSyntax(transferSyntaxUID).isRecognized()) lastRecognized = transferSyntaxUID;
				}
				// discard old list and make a new one ...
				pc.newTransferSyntaxUIDs();
				if (lastRecognized != null) {
					pc.addTransferSyntaxUID(lastRecognized);
				}
				else {
					pc.setResultReason((byte)4);				// transfer syntaxes not supported (provider rejection)
				}
			}
			return presentationContexts;
		}
	}
	
	public void TestCStore_SendOneNotOfSetToDefaultSCPPresentationContextSelectionPolicySuccessfully() throws Exception {
System.err.println("TestCStore_SendOneNotOfSetToDefaultSCPPresentationContextSelectionPolicySuccessfully():");
		File savedImagesFolder = new File("./receivedfiles");
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			null/*queryResponseGeneratorFactory*/,
			null/*retrieveResponseGeneratorFactory*/,
			null/*networkApplicationInformation*/,
			null/*presentationContextSelectionPolicy*/,
			false/*secureTransport*/,
			0/*debugLevel*/); 
			
		Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
		storageSOPClassSCPDispatcherThread.start();
		while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		
		new StorageSOPClassSCU("localhost",port,scpAET,scuAET,"testnetworkfile_scevrle.dcm",null/*affectedSOPClass*/,null/*affectedSOPInstance*/,0/*compressionLevel*/,0/*debugLevel*/);
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}
	}
	
	public void TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicySuccessfully() throws Exception {
System.err.println("TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicySuccessfully():");
		File savedImagesFolder = new File("./receivedfiles");
			
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			null/*queryResponseGeneratorFactory*/,
			null/*retrieveResponseGeneratorFactory*/,
			null/*networkApplicationInformation*/,
			null/*presentationContextSelectionPolicy*/,
			false/*secureTransport*/,
			0/*debugLevel*/); 
			
		Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
		storageSOPClassSCPDispatcherThread.start();
		while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		
		SetOfDicomFiles fileset = new SetOfDicomFiles();
		fileset.add("testnetworkfile_scevrle.dcm");
		
		new StorageSOPClassSCU("localhost",port,scpAET,scuAET,fileset,0/*compressionLevel*/,null/*multipleInstanceTransferStatusHandler*/,0/*debugLevel*/);
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}
	}
	
	public void TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicyUnsuccessfully_RLE() throws Exception {
System.err.println("TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicyUnsuccessfully_RLE():");
		File savedImagesFolder = new File("./receivedfiles");
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			null/*queryResponseGeneratorFactory*/,
			null/*retrieveResponseGeneratorFactory*/,
			null/*networkApplicationInformation*/,
			null/*presentationContextSelectionPolicy*/,
			false/*secureTransport*/,
			0/*debugLevel*/); 
			
		Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
		storageSOPClassSCPDispatcherThread.start();
		while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		
		SetOfDicomFiles fileset = new SetOfDicomFiles();
		fileset.add("testnetworkfile_usrle.dcm");
		
		
		StorageSOPClassSCU storageSOPClassSCU = new StorageSOPClassSCU("localhost",port,scpAET,scuAET,fileset,0/*compressionLevel*/,null/*multipleInstanceTransferStatusHandler*/,0/*debugLevel*/);
		if (storageSOPClassSCU.encounteredTrappedExceptions()) {
			// as expected ... no Presentation Context for RLE
		}
		else {
			fail("new StorageSOPClassSCU() should have thrown a DicomNetworkException for No presentation context for Abstract Syntax");
			while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
				Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
			}

			while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
				Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
			}
		}
		
		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);			// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}
	}

	public void TestCStore_SendOneOfSetToLastRecognizedSCPPresentationContextSelectionPolicySuccessfully_RLE() throws Exception {
System.err.println("TestCStore_SendOneOfSetToLastRecognizedSCPPresentationContextSelectionPolicySuccessfully_RLE():");
		File savedImagesFolder = new File("./receivedfiles");
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			null/*queryResponseGeneratorFactory*/,
			null/*retrieveResponseGeneratorFactory*/,
			null/*networkApplicationInformation*/,
			new OurLastRecognizedPresentationContextSelectionPolicy(),
			false/*secureTransport*/,
			0/*debugLevel*/); 
			
		Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
		storageSOPClassSCPDispatcherThread.start();
		while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		
		SetOfDicomFiles fileset = new SetOfDicomFiles();
		fileset.add("testnetworkfile_scevrle.dcm");
		
		new StorageSOPClassSCU("localhost",port,scpAET,scuAET,fileset,0/*compressionLevel*/,null/*multipleInstanceTransferStatusHandler*/,0/*debugLevel*/);
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}
	}
	
	public void TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicyUnsuccessfully_MPEG() throws Exception {
System.err.println("TestCStore_SendOneOfSetToDefaultSCPPresentationContextSelectionPolicyUnsuccessfully_MPEG():");
		File savedImagesFolder = new File("./receivedfiles");
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			null/*queryResponseGeneratorFactory*/,
			null/*retrieveResponseGeneratorFactory*/,
			null/*networkApplicationInformation*/,
			null/*presentationContextSelectionPolicy*/,
			false/*secureTransport*/,
			0/*debugLevel*/); 
			
		Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
		storageSOPClassSCPDispatcherThread.start();
		while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		
		SetOfDicomFiles fileset = new SetOfDicomFiles();
		fileset.add("testnetworkfile_endompeg.dcm");
		
		
		StorageSOPClassSCU storageSOPClassSCU = new StorageSOPClassSCU("localhost",port,scpAET,scuAET,fileset,0/*compressionLevel*/,null/*multipleInstanceTransferStatusHandler*/,0/*debugLevel*/);
		if (storageSOPClassSCU.encounteredTrappedExceptions()) {
			// as expected ... no Presentation Context for RLE
		}
		else {
			fail("new StorageSOPClassSCU() should have thrown a DicomNetworkException for No presentation context for Abstract Syntax");
			while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
				Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
			}

			while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
				Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
			}
		}
		
		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);			// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}
	}

	public void TestCStore_SendOneOfSetToLastRecognizedSCPPresentationContextSelectionPolicySuccessfully_MPEG() throws Exception {
System.err.println("TestCStore_SendOneOfSetToLastRecognizedSCPPresentationContextSelectionPolicySuccessfully_MPEG():");
		File savedImagesFolder = new File("./receivedfiles");
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			null/*queryResponseGeneratorFactory*/,
			null/*retrieveResponseGeneratorFactory*/,
			null/*networkApplicationInformation*/,
			new OurLastRecognizedPresentationContextSelectionPolicy(),
			false/*secureTransport*/,
			0/*debugLevel*/); 
			
		Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
		storageSOPClassSCPDispatcherThread.start();
		while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		
		SetOfDicomFiles fileset = new SetOfDicomFiles();
		fileset.add("testnetworkfile_endompeg.dcm");
		
		new StorageSOPClassSCU("localhost",port,scpAET,scuAET,fileset,0/*compressionLevel*/,null/*multipleInstanceTransferStatusHandler*/,0/*debugLevel*/);
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}
	}
}

