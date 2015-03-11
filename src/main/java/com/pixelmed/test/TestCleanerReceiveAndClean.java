/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.ReceivedObjectHandler;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

//import sun.awt.AppContext;

public class TestCleanerReceiveAndClean extends TestCase {

	protected static final String ourCallingAET = "TESTCALLINGAET";								// really doesn't matter (what we as SCU use when we send files to GiftCloudUploaderPanel)
	protected static final String localNameOfUnitTestSCP = "UNITTEST";							// must already be configured in .uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderPanel.properties
	protected static final String savedImagesFolderName = "./tmp/TestCleanerReceiveAndClean";	// must already exist; is not cleaned up
	protected static final String existingTestFileToBeCleaned = "testcleanerfile.dcm";			// must already exist
	protected static final int    sendingDebugLevel = 0;										// in case we want any trace during send of test file to GiftCloudUploaderPanel
	protected static final int    receivingCleanedDebugLevel = 0;								// in case we want any trace during receipt of cleaned file from GiftCloudUploaderPanel
	
	protected static final int    waitIntervalWhenSleeping = 10;								// in ms
	
	// constructor to support adding tests to suite ...
	
	public TestCleanerReceiveAndClean(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCleanerReceiveAndClean.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCleanerReceiveAndClean");
		
		suite.addTest(new TestCleanerReceiveAndClean("TestCleanerReceiveAndClean_ReceiveAndCleanWithDefaults"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	
	protected class ExpectedResult {
		AttributeTag tag;
		boolean handleAsUID;
		boolean cleaned;
		String originalValue;
		String cleanedValueIfCleaned;
		
		public ExpectedResult(AttributeTag tag,boolean handleAsUID,boolean cleaned,String originalValue,String cleanedValueIfCleaned) {
			this.tag = tag;
			this.handleAsUID = handleAsUID;
			this.cleaned = cleaned;
			this.originalValue = originalValue;
			this.cleanedValueIfCleaned = cleanedValueIfCleaned;
		}
		
		public void checkIsAsExpected(AttributeList list) {
			if (handleAsUID) {
				if (cleaned) {
					assertTrue("Cleaned "+list.getDictionary().getFullNameFromTag(tag),!originalValue.equals(Attribute.getSingleStringValueOrNull(list,tag)));
				}
				else {
					assertEquals("Not Cleaned "+list.getDictionary().getFullNameFromTag(tag),originalValue,Attribute.getSingleStringValueOrNull(list,tag));
				}
			}
			else {
				assertEquals((cleaned ? "" : "Not ")+"Cleaned "+list.getDictionary().getFullNameFromTag(tag),(cleaned ? cleanedValueIfCleaned : originalValue),Attribute.getSingleStringValueOrNull(list,tag));
			}
		}
		
	};
	
	protected void checkAllAreAsExpected(AttributeList list) {
		for (ExpectedResult e : expectedResults) {
			e.checkIsAsExpected(list);
		}
	}
	
	ExpectedResult[] expectedResults = {
		new ExpectedResult(TagFromName.PatientName            , false, true , "Test^OriginalName"          , "NAME^NONE"),
		new ExpectedResult(TagFromName.PatientID              , false, true , "TESTORIGINALID"             , "NOID"),
		new ExpectedResult(TagFromName.AccessionNumber        , false, true , "TESTORIGINALNUM"            , null/*i.e.,removed*/),
		new ExpectedResult(TagFromName.StudyID                , false, true , "TESTORIGINALID"             , null/*i.e.,removed*/),
		new ExpectedResult(TagFromName.InstitutionName        , false, false, "Original Institution"       , ""),
		new ExpectedResult(TagFromName.DeviceSerialNumber     , false, false, "Original Serial Number"     , ""),
		new ExpectedResult(TagFromName.PatientWeight          , false, false, "57.5"                       , ""),
		new ExpectedResult(TagFromName.PatientSize            , false, false, "175"                        , ""),
		new ExpectedResult(TagFromName.PatientSex             , false, false, "F"                          , ""),
		new ExpectedResult(TagFromName.PatientAge             , false, false, "041Y"                       , ""),
		new ExpectedResult(TagFromName.PatientBirthDate       , false, true , "19700214"                   , null/*i.e.,removed*/),	// NB. birth date removed even when other patient characteristics retained
		new ExpectedResult(TagFromName.StudyDescription       , false, false, "Original Study Description" , ""),
		new ExpectedResult(TagFromName.SeriesDescription      , false, false, "Original Series Description", ""),
		new ExpectedResult(TagFromName.ImageComments          , false, false, "Original Image Comment"     , ""),
		new ExpectedResult(TagFromName.SOPInstanceUID         , true , true , "1.3.6.1.4.1.5962.1.1.0.0.0.1298308902.23788.0", null/*irrelevant for UID*/),
		new ExpectedResult(TagFromName.SeriesInstanceUID      , true , true , "1.3.6.1.4.1.5962.1.3.0.0.1298308902.23788.0"  , null/*irrelevant for UID*/),
		new ExpectedResult(TagFromName.StudyInstanceUID       , true , true , "1.3.6.1.4.1.5962.1.2.0.1298308902.23788.0"    , null/*irrelevant for UID*/),
		new ExpectedResult(TagFromName.ClinicalTrialProtocolID, false, false, "TESTORIGINALID"             , ""),
		new ExpectedResult(new AttributeTag(0x0029,0x0010)    , false, true , "SIEMENS MED DISPLAY"        , null),
		new ExpectedResult(new AttributeTag(0x0029,0x1099)    , false, true , "UnsafeOriginalPrivateValue" , null),
		new ExpectedResult(new AttributeTag(0x01E1,0x0010)    , false, false, "ELSCINT1"                   , null),
		new ExpectedResult(new AttributeTag(0x01E1,0x1026)    , false, false, "SAFEPHANTOMTYPE"            , null),
	};

	static protected String removeIdentityLabelText = "Remove all unreplaced identities";
	static protected String removeDescriptionsLabelText = "Remove descriptions";
	static protected String removeSeriesDescriptionsLabelText = "Remove series description";
	static protected String removeCharacteristicsLabelText = "Remove patient characteristics";
	static protected String removeDeviceIdentityLabelText = "Remove device identifiers";
	static protected String removeInstitutionIdentityLabelText = "Remove institution identifiers";
	static protected String cleanUIDsLabelText = "Replace all UIDs";
	static protected String removePrivateLabelText = "Remove unsafe private attributes";
	static protected String addContributingEquipmentLabelText = "Add contributing equipment";
	static protected String removeClinicalTrialAttributesLabelText = "Remove clinical trial attributes";
	static protected String zipExportLabelText = "Zip exported files";
	static protected String hierarchicalExportLabelText = "Hierarchical names in export";
	static protected String acceptAnyTransferSyntaxLabelText = "Accept any Transfer Syntax";

//	protected volatile GiftCloudUploaderPanel application;
	
	protected volatile String lastReceivedDicomFileName;
	
	protected class OurReceivedObjectHandler extends ReceivedObjectHandler {
		public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle) throws DicomNetworkException, DicomException, IOException {
//System.err.println("Received: "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax);
			lastReceivedDicomFileName = dicomFileName;
		}
	}

//	public void TestCleanerReceiveAndClean_ReceiveAndCleanWithDefaults() throws Exception {
//		//GiftCloudUploaderPanel.main(new String[] {});
//		//ThreadGroup applicationThreadGroup = new ThreadGroup("DicomCleanerThreadGroup");
//		//AppContext applicationContext =  new AppContext(applicationThreadGroup);
//		//Thread applicationThread = new Thread(applicationThreadGroup,"DicomCleanerThread") {
//		Thread applicationThread = new Thread() {
//			public void run() {
//				try {
//					application=new GiftCloudUploaderPanel();
////System.err.println("applicationThread.run(): application = "+application);
//				}
//				catch (Exception e) {
//					e.printStackTrace(System.err);
//				}
//			}
//		};
//		applicationThread.start();
//		while (application == null) Thread.currentThread().sleep(waitIntervalWhenSleeping);	// NB. must be declared volatile for this to work reliably
////System.err.println("Have application, proceeeding ...");
////System.err.println("application = "+application);
//
//		UserInterfaceUtilities.registerWindowOpeningAndClosingListenerToTrackDialogs();
//
//		{
//			String host =                  application.getPropertyInsistently("Dicom.RemoteAEs."+localNameOfUnitTestSCP+".HostNameOrIPAddress");
//			int    port = Integer.parseInt(application.getPropertyInsistently("Dicom.RemoteAEs."+localNameOfUnitTestSCP+".Port"));
//			String aet  =                  application.getPropertyInsistently("Dicom.RemoteAEs."+localNameOfUnitTestSCP+".CalledAETitle");
//
//			// probably won't get to these assertions since getPropertyInsistently() will throw exception if not found
//			assertTrue("Have receiving "+localNameOfUnitTestSCP+" hostname",host != null && host.length() > 0);
//			assertTrue("Have receiving "+localNameOfUnitTestSCP+" port"    ,port > 0);
//			assertTrue("Have receiving "+localNameOfUnitTestSCP+" AET"     ,aet != null && aet.length() > 0);
//
//			StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
//				port,
//				aet,
//				new File(savedImagesFolderName),
//				StoredFilePathStrategy.BYSOPINSTANCEUIDINSINGLEFOLDER,
//				new OurReceivedObjectHandler(),
//				receivingCleanedDebugLevel);
//			Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
//			storageSOPClassSCPDispatcherThread.start();
//			while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
//				Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
//			}
//			while (!storageSOPClassSCPDispatcher.isReady()) {
//				Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
//			}
//		}
//
////System.err.println("java.awt.EventQueue.isDispatchThread() ="+java.awt.EventQueue.isDispatchThread());
//
//		// wait till user interface appears ... this probably isn't necessary given the wait on the constructor in the thread returning, but just in case ...
//		while (Frame.getFrames().length == 0) Thread.currentThread().sleep(waitIntervalWhenSleeping);
////System.err.println("Have UI, proceeeding ...");
//		while (UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeIdentityLabelText).length == 0) Thread.currentThread().sleep(waitIntervalWhenSleeping);
////System.err.println("Have checkboxes, proceeeding ...");
//
////UserInterfaceUtilities.dumpAllFramesAndChildren(System.err);
//
//		assertTrue(removeIdentityLabelText               +" is not selected by default",   ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeIdentityLabelText)[0]).isSelected());
//		assertTrue(removeDescriptionsLabelText           +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeDescriptionsLabelText)[0]).isSelected());
//		assertTrue(removeSeriesDescriptionsLabelText     +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeSeriesDescriptionsLabelText)[0]).isSelected());
//		assertTrue(removeCharacteristicsLabelText        +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeCharacteristicsLabelText)[0]).isSelected());
//		assertTrue(cleanUIDsLabelText                    +" is     selected by default",   ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",cleanUIDsLabelText)[0]).isSelected());
//		assertTrue(removePrivateLabelText                +" is     selected by default",   ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removePrivateLabelText)[0]).isSelected());
//		assertTrue(removeDeviceIdentityLabelText         +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeDeviceIdentityLabelText)[0]).isSelected());
//		assertTrue(removeInstitutionIdentityLabelText    +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeInstitutionIdentityLabelText)[0]).isSelected());
//		assertTrue(removeClinicalTrialAttributesLabelText+" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",removeClinicalTrialAttributesLabelText)[0]).isSelected());
//		assertTrue(addContributingEquipmentLabelText     +" is     selected by default",   ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",addContributingEquipmentLabelText)[0]).isSelected());
//		assertTrue(zipExportLabelText                    +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",zipExportLabelText)[0]).isSelected());
//		assertTrue(hierarchicalExportLabelText           +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",hierarchicalExportLabelText)[0]).isSelected());
//		assertTrue(acceptAnyTransferSyntaxLabelText      +" is not selected by default", ! ((JCheckBox)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JCheckBox",acceptAnyTransferSyntaxLabelText)[0]).isSelected());
//
//		{
//			SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();
//			setOfDicomFiles.add(existingTestFileToBeCleaned);
//			String    theirHost = "localhost";
//			int       theirPort = Integer.parseInt(application.getPropertyInsistently("Dicom.ListeningPort"));
//			String theirAETitle = application.getPropertyInsistently("Dicom.CalledAETitle");
//			new StorageSOPClassSCU(theirHost,theirPort,theirAETitle,ourCallingAET,setOfDicomFiles,0/*compressionLevel*/,null,null,0,sendingDebugLevel);
//		}
//
//		//Thread.currentThread().sleep(1000);
//
////System.err.println("Trying to identify original tree ...");
//		JTreeWithAdditionalKeyStrokeActions original = null;
//		// rather than sleeping for an arbitrary fixed interval, wait until the result appears in the (new) tree
//		while (original == null || original.getRowCount() < 2) {
//			Thread.currentThread().sleep(waitIntervalWhenSleeping);
//			// need to fetch trees again, since the one in the cleaned panel will actually be a new object after import (don't ask)
//			Component[] databaseTrees = UserInterfaceUtilities.findComponentsOfClass("com.pixelmed.utils.JTreeWithAdditionalKeyStrokeActions");
////System.err.println("databaseTrees.length = "+databaseTrees.length);
//			if (databaseTrees.length > 0) {	// in case we catch it in transition
//				original = (JTreeWithAdditionalKeyStrokeActions)(databaseTrees[0]);
////System.err.println("Original.getRowCount() = "+original.getRowCount());
//			}
//		}
////System.err.println("Original tree: "+original.getModel());
//
//		original.setSelectionRow(1);
//		((JButton)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JButton","Clean")[0]).doClick();
//
//		JTreeWithAdditionalKeyStrokeActions cleaned = null;
//		// rather than sleeping for an arbitrary fixed interval, wait until the result appears in the (new) tree
//		while (cleaned == null || cleaned.getRowCount() < 2) {
//			Thread.currentThread().sleep(waitIntervalWhenSleeping);
//			// need to fetch trees again, since the one in the cleaned panel will actually be a new object after import (don't ask)
//			Component[] databaseTrees = UserInterfaceUtilities.findComponentsOfClass("com.pixelmed.utils.JTreeWithAdditionalKeyStrokeActions");
//			if (databaseTrees.length > 1) {	// in case we catch it in transition
//				cleaned = (JTreeWithAdditionalKeyStrokeActions)(databaseTrees[1]);
//			}
//		}
////System.err.println("Cleaned tree: "+cleaned.getModel());
//
//		cleaned.setSelectionRow(1);
//
//		{
//			// Need to click the button in another thread, otherwise blocks because of the modal dialog
//			Thread thread = new Thread() {
//				public void run() {
//					try {
//						//((JButton)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JButton","Export")[0]).doClick();
//						((JButton)UserInterfaceUtilities.findComponentsOfClassWithTextValue("javax.swing.JButton","Send")[0]).doClick();
//					}
//					catch (Exception e) {
//						e.printStackTrace(System.err);
//					}
//				}
//			};
//			thread.start();
//			while (thread.getState() != Thread.State.RUNNABLE) Thread.currentThread().sleep(waitIntervalWhenSleeping);	// Otherwise may get stuck waiting forever below
//		}
//
//		{
//			JDialog dialog = null;
//			//while ((dialog=UserInterfaceUtilities.getOpenDialogByTitle("Open")) == null) Thread.currentThread().sleep(waitIntervalWhenSleeping);
//			while ((dialog=UserInterfaceUtilities.getOpenDialogByTitle("Send ...")) == null) Thread.currentThread().sleep(waitIntervalWhenSleeping);
////System.err.println("Have dialog: "+dialog);
////UserInterfaceUtilities.dumpComponentAndChildren(dialog,System.err);
//
//			// assume default selection of target is OK ... THIS MAY NOT WORK IF JMDNS HAS FOUND MORE AE'S, so do not activate jmdns Makefile (000615) :(
//			// really should do better than make this assumption :(
//			((JButton)UserInterfaceUtilities.findComponentsOfClassWithTextValue(dialog,"javax.swing.JButton","OK")[0]).doClick();
//		}
//
//		while (lastReceivedDicomFileName == null) {
////System.err.println("Waiting for lastReceivedDicomFileName");
//			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
//		}
//
//		{
////System.err.println("Reading "+lastReceivedDicomFileName);
//			AttributeList list = new AttributeList();
//			list.read(lastReceivedDicomFileName);
////System.err.print(list);
//			checkAllAreAsExpected(list);
//		}
//
////System.err.println("Done checking.");
//		//while (true);
//	}
}
