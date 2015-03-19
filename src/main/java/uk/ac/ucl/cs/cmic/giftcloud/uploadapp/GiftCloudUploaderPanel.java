/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.DatabaseTreeBrowser;
import com.pixelmed.database.DatabaseTreeRecord;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.QueryTreeBrowser;
import com.pixelmed.query.QueryTreeModel;
import com.pixelmed.query.QueryTreeRecord;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * <p>This class is an application for importing or retrieving DICOM studies,
 * cleaning them (i.e., de-identifying them or replacing UIDs, etc.), and
 * sending them elsewhere.</p>
 * 
 * <p>It is configured by use of a properties file that resides in the user's
 * home directory in <code>.uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderPanel.properties</code>.</p>
 * 
 * <p>It supports import and network retrieval of uncompressed, deflate and bzip compressed,
 * and baseline JPEG compressed images (but not yet other encapsulated compressed pixel data).</p>
 * 
 * @author	dclunie
 */
public class GiftCloudUploaderPanel extends JPanel {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/GiftCloudUploaderPanel.java,v 1.63 2014/12/06 19:03:17 dclunie Exp $";

    private JComboBox<String> projectList;

    private List<QuerySelection> currentRemoteQuerySelectionList;

    private GiftCloudReporter reporter;
    private GiftCloudUploaderController controller;
    private GiftCloudPropertiesFromBridge giftCloudProperties = null;

    final private DicomNode dicomNode;


//	protected static String propertiesFileName  = ".uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderPanel.properties";
	


	protected static int textFieldLengthForQueryPatientName = 16;
	protected static int textFieldLengthForQueryPatientID = 10;
	protected static int textFieldLengthForQueryStudyDate = 8;
	protected static int textFieldLengthForQueryAccessionNumber = 10;

    protected static int textFieldLengthForGiftCloudServerUrl = 32;

//	protected static int textFieldLengthForReplacementPatientName = 16;
//	protected static int textFieldLengthForReplacementPatientID = 10;
//	protected static int textFieldLengthForReplacementAccessionNumber = 10;
//	protected static int textFieldLengthForModifyDates = 8;

	protected ResourceBundle resourceBundle;

	protected JPanel srcDatabasePanel;
//	protected JPanel dstDatabasePanel;
	protected JPanel remoteQueryRetrievePanel;
	
//	protected JCheckBox removeIdentityCheckBox;
//	protected JCheckBox removeDescriptionsCheckBox;
//	protected JCheckBox removeSeriesDescriptionsCheckBox;
//	protected JCheckBox removeProtocolNameCheckBox;
//	protected JCheckBox removeCharacteristicsCheckBox;
//	protected JCheckBox removeDeviceIdentityCheckBox;
//	protected JCheckBox removeInstitutionIdentityCheckBox;
//	protected JCheckBox cleanUIDsCheckBox;
//	protected JCheckBox removePrivateCheckBox;
//	protected JCheckBox addContributingEquipmentCheckBox;
//	protected JCheckBox removeClinicalTrialAttributesCheckBox;
//	protected JCheckBox zipExportCheckBox;
//	protected JCheckBox hierarchicalExportCheckBox;
//	protected JCheckBox acceptAnyTransferSyntaxCheckBox;

//	protected JCheckBox replacePatientNameCheckBox;
//	protected JCheckBox replacePatientIDCheckBox;
//	protected JCheckBox replaceAccessionNumberCheckBox;
//	protected JCheckBox modifyDatesCheckBox;
//
//	protected JTextField replacementPatientNameTextField;
//	protected JTextField replacementPatientIDTextField;
//	protected JTextField replacementAccessionNumberTextField;
//	protected JTextField modifyDatesTextField;

    protected JTextField giftCloudServerText;

	protected JTextField queryFilterPatientNameTextField;
	protected JTextField queryFilterPatientIDTextField;
	protected JTextField queryFilterStudyDateTextField;
	protected JTextField queryFilterAccessionNumberTextField;
	


    private final StatusPanel statusPanel;




    private GiftCloudDialogs giftCloudDialogs;
    private String buildDate;
    private JLabel statusBar;


//	private String showInputDialogToSelectNetworkTargetByLocalApplicationEntityName(String message,String title,String defaultSelection) {
//		String ae = defaultSelection;
//		if (dicomNode.isNetworkApplicationInformationValid()) {
//			Set localNamesOfRemoteAEs = dicomNode.getListOfLocalNamesOfApplicationEntities();
//			if (localNamesOfRemoteAEs != null) {
//				String sta[] = new String[localNamesOfRemoteAEs.size()];
//				int i=0;
//				Iterator it = localNamesOfRemoteAEs.iterator();
//				while (it.hasNext()) {
//					sta[i++]=(String)(it.next());
//				}
//                ae = giftCloudDialogs.getSelection(message, title, sta, ae);
//			}
//		}
//		return ae;
//	}
	
    public String getBuildDate() {
        return buildDate;
    }

    public JLabel getStatusBar() {
        return statusBar;
    }


	protected DatabaseTreeRecord[] currentSourceDatabaseSelections;
	protected Vector currentSourceFilePathSelections;

    public void addFile(final String fileName) {
        System.out.println("** CHANGE IN FILELIST **"); // ToDo
        srcDatabasePanel.removeAll();

        try {
            new OurSourceDatabaseTreeBrowser(dicomNode.getSrcDatabase(), srcDatabasePanel);

        } catch (DicomException e) {
            // ToDo
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Refresh source database browser failed: "+e));
//				e.printStackTrace(System.err);
            e.printStackTrace();
        }
        srcDatabasePanel.validate();
    }

    public void updateQueryPanel(final QueryInformationModel queryInformationModel, final AttributeList filter, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomException, IOException, DicomNetworkException {
        QueryTreeModel treeModel = queryInformationModel.performHierarchicalQuery(filter);
        new GiftCloudUploaderPanel.OurQueryTreeBrowser(queryInformationModel, treeModel, remoteQueryRetrievePanel, currentRemoteQueryInformationModel);
    }

    protected class OurSourceDatabaseTreeBrowser extends DatabaseTreeBrowser {
		public OurSourceDatabaseTreeBrowser(DatabaseInformationModel d,Container content) throws DicomException {
			super(d,content);
		}
		
		protected boolean doSomethingWithSelections(DatabaseTreeRecord[] selections) {
			currentSourceDatabaseSelections = selections;
			return false;	// still want to call doSomethingWithSelectedFiles()
		}
		
		protected void doSomethingWithSelectedFiles(Vector paths) {
			currentSourceFilePathSelections = paths;
		}
	}
	
//	protected DatabaseTreeRecord[] currentDestinationDatabaseSelections;
	protected Vector<String> currentDestinationFilePathSelections;

//	protected class OurDestinationDatabaseTreeBrowser extends DatabaseTreeBrowser {
//		public OurDestinationDatabaseTreeBrowser(DatabaseInformationModel d,Container content) throws DicomException {
//			super(d,content);
//		}
//
//		protected boolean doSomethingWithSelections(DatabaseTreeRecord[] selections) {
//			currentDestinationDatabaseSelections = selections;
//			return false;	// still want to call doSomethingWithSelectedFiles()
//		}
//
//		protected void doSomethingWithSelectedFiles(Vector paths) {
//			currentDestinationFilePathSelections = paths;
//		}
//	}
	
//	// very similar to code in DicomImageViewer and DoseUtility apart from logging and progress bar ... should refactor :(
//	protected void purgeFilesAndDatabaseInformation(DatabaseTreeRecord[] databaseSelections,MessageLogger logger,SafeProgressBarUpdaterThread progressBarUpdater,int done,int maximum) throws DicomException, IOException {
//		if (databaseSelections != null) {
//			for (DatabaseTreeRecord databaseSelection : databaseSelections) {
//				purgeFilesAndDatabaseInformation(databaseSelection,logger,progressBarUpdater,done,maximum);
//			}
//		}
//	}
	
//	protected void purgeFilesAndDatabaseInformation(DatabaseTreeRecord databaseSelection,MessageLogger logger,SafeProgressBarUpdaterThread progressBarUpdater,int done,int maximum) throws DicomException, IOException {
//		if (databaseSelection != null) {
//			SafeProgressBarUpdaterThread.updateProgressBar(progressBarUpdater,done,maximum);
//			InformationEntity ie = databaseSelection.getInformationEntity();
//			if (ie == null /* the root of the tree, i.e., everything */ || !ie.equals(InformationEntity.INSTANCE)) {
//				// Do it one study at a time, in the order in which the patients and studies are sorted in the tree
//				Enumeration children = databaseSelection.children();
//				if (children != null) {
//					maximum+=databaseSelection.getChildCount();
//					while (children.hasMoreElements()) {
//						DatabaseTreeRecord child = (DatabaseTreeRecord)(children.nextElement());
//						if (child != null) {
//							purgeFilesAndDatabaseInformation(child,logger,progressBarUpdater,done,maximum);
//							++done;
//						}
//					}
//				}
//				// AFTER we have processed all the children, if any, we can delete ourselves, unless we are the root
//				if (ie != null) {
//					logger.sendLn("Purging "+databaseSelection);
//					databaseSelection.removeFromParent();
//				}
//			}
//			else {
//				// Instance level ... may need to delete files
//				String fileName = databaseSelection.getLocalFileNameValue();
//				String fileReferenceType = databaseSelection.getLocalFileReferenceTypeValue();
//				if (fileReferenceType != null && fileReferenceType.equals(DatabaseInformationModel.FILE_COPIED)) {
//					try {
//						logger.sendLn("Deleting file "+fileName);
//						if (!new File(fileName).delete()) {
//							logger.sendLn("Failed to delete local copy of file "+fileName);
//						}
//					}
//					catch (Exception e) {
//						e.printStackTrace(System.err);
//						logger.sendLn("Failed to delete local copy of file "+fileName);
//					}
//				}
//                dicomNode.removeFileFromEasliestDatesIndex(fileName);
//				logger.sendLn("Purging "+databaseSelection);
//				databaseSelection.removeFromParent();
//			}
//		}
//	}

//	protected class PurgeWorker implements Runnable {
//		//PurgeWorker() {
//		//}
//
//		public void run() {
//			cursorChanger.setWaitCursor();
//			logger.sendLn("Purging started");
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Purging started"));
//			SafeProgressBarUpdaterThread.startProgressBar(progressBarUpdater);
//			try {
//				purgeFilesAndDatabaseInformation(currentSourceDatabaseSelections,logger,progressBarUpdater,0,1);
////				purgeFilesAndDatabaseInformation(currentDestinationDatabaseSelections,logger,progressBarUpdater,0,1);
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Purging failed: "+e));
//				e.printStackTrace(System.err);
//			}
//			srcDatabasePanel.removeAll();
//
//            // ToDo: Can a purge fire a DicomNode changed event to automatically repopulate the source panel?
//
////			dstDatabasePanel.removeAll();
//			try {
//				new OurSourceDatabaseTreeBrowser(dicomNode.getSrcDatabase(), srcDatabasePanel);
////				new OurDestinationDatabaseTreeBrowser(dstDatabase,dstDatabasePanel);
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Refresh source database browser failed: "+e));
//				e.printStackTrace(System.err);
//			}
//			srcDatabasePanel.validate();
//			SafeProgressBarUpdaterThread.endProgressBar(progressBarUpdater);
//			logger.sendLn("Purging complete");
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done purging"));
//			cursorChanger.restoreCursor();
//		}
//	}

//	protected class PurgeActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			try {
//				activeThread = new Thread(new PurgeWorker());
//				activeThread.start();
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Purging failed: "+e));
//				e.printStackTrace(System.err);
//			}
//		}
//	}
		
//	protected boolean copyFromOriginalToCleanedPerformingAction(Vector paths,Date earliestDateInSet,MessageLogger logger,SafeProgressBarUpdaterThread progressBarUpdater) throws DicomException, IOException {
//		boolean success = true;
//		if (paths != null) {
//			Date epochForDateModification = null;
////			if (modifyDatesCheckBox.isSelected()) {
////				try {
////					epochForDateModification = DateTimeAttribute.getDateFromFormattedString(modifyDatesTextField.getText().trim());		// assumes 0 time and UTC if not specified
////System.err.println("GiftCloudUploaderPanel.copyFromOriginalToCleanedPerformingAction(): epochForDateModification "+epochForDateModification);
////				}
////				catch (java.text.ParseException e) {
////					e.printStackTrace(System.err);
////					epochForDateModification = new Date(0);		// use system epoch if failed; better than to not modify them at all when requested to
////				}
////			}
//			SafeProgressBarUpdaterThread.updateProgressBar(progressBarUpdater,0,paths.size());
//			for (int j=0; j< paths.size(); ++j) {
//				String dicomFileName = (String)(paths.get(j));
//				if (dicomFileName != null) {
//					ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Cleaning "+dicomFileName));
//					try {
//						// do not log it yet ... wait till we have output file name
//						File file = new File(dicomFileName);
//						DicomInputStream i = new DicomInputStream(file);
//						AttributeList list = new AttributeList();
//						list.setDecompressPixelData(false);
//						list.read(i);
//						i.close();
//
//						list.removeGroupLengthAttributes();
//						// did not decompress, so do not need to change ImagePixelModule attributes or insert lossy compression history
//
//						String outputTransferSyntaxUID = null;
//						{
//							String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.TransferSyntaxUID);		// did not decompress it
//							// did not compress, so leave it alone unless Implicit VR, which we always want to convert to Explicit VR
//							outputTransferSyntaxUID = transferSyntaxUID.equals(TransferSyntax.ImplicitVRLittleEndian) ? TransferSyntax.ExplicitVRLittleEndian : transferSyntaxUID;
//						}
//						list.removeMetaInformationHeaderAttributes();
//
////						if (removeClinicalTrialAttributesCheckBox.isSelected()) {
////							ClinicalTrialsAttributes.removeClinicalTrialsAttributes(list);
////						}
////						if (removeIdentityCheckBox.isSelected()) {
////							ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
////								ClinicalTrialsAttributes.HandleUIDs.keep,
////								!removeDescriptionsCheckBox.isSelected(),
////								!removeSeriesDescriptionsCheckBox.isSelected(),
////								!removeProtocolNameCheckBox.isSelected(),
////								!removeCharacteristicsCheckBox.isSelected(),
////								!removeDeviceIdentityCheckBox.isSelected(),
////								!removeInstitutionIdentityCheckBox.isSelected(),
////								modifyDatesCheckBox.isSelected() ? ClinicalTrialsAttributes.HandleDates.modify : ClinicalTrialsAttributes.HandleDates.keep,epochForDateModification,earliestDateInSet);
////						}
////						if (replacePatientNameCheckBox.isSelected()) {
////							String newName = replacementPatientNameTextField.getText().trim();
////							{ AttributeTag tag = TagFromName.PatientName; list.remove(tag); Attribute a = new PersonNameAttribute(tag); a.addValue(newName); list.put(tag,a); }
////						}
////						if (replacePatientIDCheckBox.isSelected()) {
////							String newID = replacementPatientIDTextField.getText().trim();
////							{ AttributeTag tag = TagFromName.PatientID; list.remove(tag); Attribute a = new LongStringAttribute(tag); a.addValue(newID); list.put(tag,a); }
////						}
////						if (replaceAccessionNumberCheckBox.isSelected()) {
////							String newAccessionNumber = replacementAccessionNumberTextField.getText().trim();
////							{ AttributeTag tag = TagFromName.AccessionNumber; list.remove(tag); Attribute a = new ShortStringAttribute(tag); a.addValue(newAccessionNumber); list.put(tag,a); }
////						}
//
////						if (removePrivateCheckBox.isSelected()) {
//
//
//
//
//
////							list.removeUnsafePrivateAttributes();
////							{
////								Attribute a = list.get(TagFromName.DeidentificationMethod);
////								if (a != null) {
////									a.addValue("Unsafe private removed");
////								}
////							}
////							{
////								SequenceAttribute a = (SequenceAttribute)(list.get(TagFromName.DeidentificationMethodCodeSequence));
////								if (a != null) {
////									a.addItem(new CodedSequenceItem("113111","DCM","Retain Safe Private Option").getAttributeList());
////								}
////							}
////						}
////						else {
//							{
//								Attribute a = list.get(TagFromName.DeidentificationMethod);
//								if (a != null) {
//									a.addValue("All private retained");
//								}
//							}
//							{
//								SequenceAttribute a = (SequenceAttribute)(list.get(TagFromName.DeidentificationMethodCodeSequence));
//								if (a != null) {
//									a.addItem(new CodedSequenceItem("210002","99PMP","Retain all private elements").getAttributeList());
//								}
//							}
////						}
////						if (cleanUIDsCheckBox.isSelected()) {
////							ClinicalTrialsAttributes.remapUIDAttributes(list);
////							{
////								Attribute a = list.get(TagFromName.DeidentificationMethod);
////								if (a != null) {
////									a.addValue("UIDs remapped");
////								}
////							}
////							// remove the default Retain UIDs added by ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes() with the ClinicalTrialsAttributes.HandleUIDs.keep option
////							{
////								SequenceAttribute a = (SequenceAttribute)(list.get(TagFromName.DeidentificationMethodCodeSequence));
////								if (a != null) {
////									Iterator<SequenceItem> it = a.iterator();
////									while (it.hasNext()) {
////										SequenceItem item = it.next();
////										if (item != null) {
////											CodedSequenceItem testcsi = new CodedSequenceItem(item.getAttributeList());
////											if (testcsi != null) {
////												String cv = testcsi.getCodeValue();
////												String csd = testcsi.getCodingSchemeDesignator();
////												if (cv != null && cv.equals("113110") && csd != null && csd.equals("DCM")) {	// "Retain UIDs Option"
////													it.remove();
////												}
////											}
////										}
////									}
////								}
////							}
////							{
////								SequenceAttribute a = (SequenceAttribute)(list.get(TagFromName.DeidentificationMethodCodeSequence));
////								if (a != null) {
////									a.addItem(new CodedSequenceItem("210001","99PMP","Remap UIDs").getAttributeList());
////								}
////							}
////						}
////						if (addContributingEquipmentCheckBox.isSelected()) {
//							ClinicalTrialsAttributes.addContributingEquipmentSequence(list,
//								true,
//								new CodedSequenceItem("109104","DCM","De-identifying Equipment"),	// per CP 892
//								"TIG",  														// Manufacturer
//								"UCL",															// Institution Name
//								"CMIC",															// Institutional Department Name
//								null		,													// Institution Address
//								dicomNode.getOurCalledAETitle(),												// Station Name
//								"GIFT-Cloud Uploader",													// Manufacturer's Model Name
//								null,															// Device Serial Number
//								getBuildDate(),													// Software Version(s)
//								"Cleaned");
////						}
//						FileMetaInformation.addFileMetaInformation(list, outputTransferSyntaxUID, dicomNode.getOurCalledAETitle());
//						list.insertSuitableSpecificCharacterSetForAllStringValues();	// E.g., may have de-identified Kanji name and need new character set
//						File cleanedFile = File.createTempFile("clean",".dcm");
//						cleanedFile.deleteOnExit();
//						list.write(cleanedFile,outputTransferSyntaxUID,true/*useMeta*/,true/*useBufferedStream*/);
//						logger.sendLn("Cleaned "+dicomFileName+" into "+cleanedFile.getCanonicalPath());
//
//                        // ToDo: Removed insertion into destination
////						dstDatabase.insertObject(list,cleanedFile.getCanonicalPath(),DatabaseInformationModel.FILE_COPIED);
//					}
//					catch (Exception e) {
//						System.err.println("GiftCloudUploaderPanel.copyFromOriginalToCleanedPerformingAction(): while cleaning "+dicomFileName);
//						e.printStackTrace(System.err);
//						logger.sendLn("Cleaning failed for "+dicomFileName+" because "+e.toString());
//						success = false;
//					}
//				}
//				SafeProgressBarUpdaterThread.updateProgressBar(progressBarUpdater,j+1);
//			}
//		}
//		return success;
//	}


	
//	protected static Date findEarliestDate(Map<String,Date> earliestDatesIndexedBySourceFilePath,Vector<String> sourceFilePathSelections) {
//		Date earliestSoFar = null;
//		for (String path : sourceFilePathSelections) {
//			Date candidate = earliestDatesIndexedBySourceFilePath.get(path);
//			if (candidate != null && (earliestSoFar == null || candidate.before(earliestSoFar))) {
//				earliestSoFar = candidate;
//			}
//		}
//		return earliestSoFar;
//	}
//
//	protected class CleanWorker implements Runnable {
//		Vector sourceFilePathSelections;
//		DatabaseInformationModel dstDatabase;
//		JPanel dstDatabasePanel;
//		Map<String,Date> earliestDatesIndexedBySourceFilePath;
//
//		CleanWorker(Vector sourceFilePathSelections,DatabaseInformationModel dstDatabase,JPanel dstDatabasePanel,Map<String,Date> earliestDatesIndexedBySourceFilePath) {
//			this.sourceFilePathSelections=sourceFilePathSelections;
//			this.dstDatabase=dstDatabase;
//			this.dstDatabasePanel=dstDatabasePanel;
//			this.earliestDatesIndexedBySourceFilePath=earliestDatesIndexedBySourceFilePath;
//		}
//
//		public void run() {
//			cursorChanger.setWaitCursor();
//			logger.sendLn("Cleaning started");
//			SafeProgressBarUpdaterThread.startProgressBar(progressBarUpdater);
//			Date earliestDateInSet = findEarliestDate(earliestDatesIndexedBySourceFilePath,sourceFilePathSelections);
//			try {
//				if (!copyFromOriginalToCleanedPerformingAction(sourceFilePathSelections,earliestDateInSet,logger,progressBarUpdater)) {
//					ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Cleaning (partially) failed: "));
//				}
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Cleaning failed: "+e));
//				e.printStackTrace(System.err);
//			}
//			dstDatabasePanel.removeAll();
//			try {
//				new OurDestinationDatabaseTreeBrowser(dstDatabase,dstDatabasePanel);
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Refresh destination database browser failed: "+e));
//				e.printStackTrace(System.err);
//			}
//			dstDatabasePanel.validate();
//			SafeProgressBarUpdaterThread.endProgressBar(progressBarUpdater);
//			logger.sendLn("Cleaning complete");
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done cleaning"));
//			cursorChanger.restoreCursor();
//		}
//	}
//
//	protected class CleanActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			try {
//				activeThread = new Thread(new CleanWorker(currentSourceFilePathSelections,dstDatabasePanel,dstDatabasePanel,earliestDatesIndexedBySourceFilePath));
//				activeThread.start();
//
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Cleaned failed: "+e));
//				e.printStackTrace(System.err);
//			}
//		}
//	}
	

    protected class ImportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
                reporter.showMesageLogger();

                Optional<GiftCloudDialogs.SelectedPathAndFile> selectFileOrDirectory = giftCloudDialogs.selectFileOrDirectory(giftCloudProperties.getLastImportDirectory());

                if (selectFileOrDirectory.isPresent()) {
                    giftCloudProperties.setLastImportDirectory(selectFileOrDirectory.get().getSelectedPath());
                    String filePath = selectFileOrDirectory.get().getSelectedFile();
                    controller.runImport(filePath, statusPanel.getProgressBar());
                }
            } catch (Exception e) {
                reporter.updateProgress("Importing failed due to the following error: " + e);
                e.printStackTrace(System.err);
            }
        }
	}

    protected class ExportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (currentDestinationFilePathSelections != null && currentDestinationFilePathSelections.size() > 0) {

                try {
                    Optional<String> exportDirectory = giftCloudDialogs.selectDirectory(giftCloudProperties.getLastExportDirectory());

                    if (exportDirectory.isPresent()) {
                        giftCloudProperties.setLastExportDirectory(exportDirectory.get());
                        controller.export(exportDirectory.get(), currentDestinationFilePathSelections);

                    } // else the user cancelled
                } catch (Exception e) {
                    ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Export failed: "+e));
                    e.printStackTrace(System.err);
                }
			}
		}
	}

    protected class GiftCloudUploadActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.upload(currentSourceFilePathSelections);
        }
    }





//	protected class OurMultipleInstanceTransferStatusHandler extends MultipleInstanceTransferStatusHandlerWithFileName {
//		int nFiles;
//		MessageLogger logger;
//		StatusPanel statusPanel;
//
//		OurMultipleInstanceTransferStatusHandler(int nFiles,MessageLogger logger, StatusPanel statusPanel) {
//			this.nFiles=nFiles;
//			this.logger=logger;
//			this.statusPanel=statusPanel;
//		}
//
//		public void updateStatus(int nRemaining,int nCompleted,int nFailed,int nWarning,String sopInstanceUID,String fileName,boolean success) {
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Remaining "+nRemaining+", completed "+nCompleted+", failed "+nFailed+", warning "+nWarning));
//            statusPanel.updateProgressBar(nFiles - nRemaining);
//			if (logger != null) {
//				logger.sendLn((success ? "Sent " : "Failed to send ")+fileName);
//			}
//		}
//	}

//	protected class SendWorker implements Runnable {
//		String hostname;
//		int port;
//		String calledAETitle;
//		String callingAETitle;
//		SetOfDicomFiles setOfDicomFiles;
//
//		SendWorker(String hostname,int port,String calledAETitle,String callingAETitle,SetOfDicomFiles setOfDicomFiles) {
//			this.hostname=hostname;
//			this.port=port;
//			this.calledAETitle=calledAETitle;
//			this.callingAETitle=callingAETitle;
//			this.setOfDicomFiles=setOfDicomFiles;
//		}
//
//		public void run() {
//			reporter.setWaitCursor();
//            reporter.sendLn("Send starting");
//			try {
//				int nFiles = setOfDicomFiles.size();
//                statusPanel.updateProgressBar(0, nFiles);
//				new StorageSOPClassSCU(hostname,port,calledAETitle,callingAETitle,setOfDicomFiles,0/*compressionLevel*/,
//					new OurMultipleInstanceTransferStatusHandler(nFiles,reporter,statusPanel),
//					0/*debugLevel*/);
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Send failed: "+e));
//                reporter.sendLn("Send failed");
//				e.printStackTrace(System.err);
//			}
//            statusPanel.endProgressBar();
//            reporter.sendLn("Send complete");
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done sending to "+calledAETitle));
//			reporter.restoreCursor();
//		}
//	}

//	protected class SendActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			if (currentDestinationFilePathSelections != null && currentDestinationFilePathSelections.size() > 0) {
//                String ae = giftCloudProperties.getPropertyCurrentlySelectedStorageTargetAE();
//				ae = showInputDialogToSelectNetworkTargetByLocalApplicationEntityName("Select destination","Send ...",ae);
//				if (ae != null && giftCloudProperties.areNetworkPropertiesValid()) {
//					try {
//						String                   callingAETitle = giftCloudProperties.getCallingAETitle();
//						String                    calledAETitle = dicomNode.getCalledAETitle(ae);
//						PresentationAddress presentationAddress = dicomNode.getPresentationAddress(calledAETitle);
//						String                         hostname = presentationAddress.getHostname();
//						int                                port = presentationAddress.getPort();
//
//						SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles(currentDestinationFilePathSelections);
//						new Thread(new SendWorker(hostname,port,calledAETitle,callingAETitle,setOfDicomFiles)).start();
//					}
//					catch (Exception e) {
//						e.printStackTrace(System.err);
//					}
//				}
//				// else user cancelled operation in JOptionPane.showInputDialog() so gracefully do nothing
//			}
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done sending."));
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done sending."));
//		}
//	}
	
//	protected class OurDicomImageBlackout extends DicomImageBlackout {
//
//		OurDicomImageBlackout(String title,String dicomFileNames[],int burnedinflag,String ourAETitle) {
//			super(title,dicomFileNames,null,burnedinflag);
//			statusNotificationHandler = new ApplicationStatusChangeEventNotificationHandler();
//			this.ourAETitle=ourAETitle;
//		}
//
//		public class ApplicationStatusChangeEventNotificationHandler extends StatusNotificationHandler {
//			public void notify(int status,String message,Throwable t) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Blackout "+message));
//                reporter.sendLn("Blackout "+message);
//				System.err.println("DicomImageBlackout.DefaultStatusNotificationHandler.notify(): status = "+status);
//				System.err.println("DicomImageBlackout.DefaultStatusNotificationHandler.notify(): message = "+message);
//				if (t != null) {
//					t.printStackTrace(System.err);
//				}
//			}
//		}
//	}
//
//	protected class BlackoutActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			cursorChanger.setWaitCursor();
//			logger.sendLn("Blackout starting");
//			if (currentDestinationFilePathSelections != null && currentDestinationFilePathSelections.size() > 0) {
//				{
//					try {
//						int nFiles = currentDestinationFilePathSelections.size();
//						String fileNames[] = new String[nFiles];
//						for (int j=0; j< nFiles; ++j) {
//							fileNames[j] = (String)(currentDestinationFilePathSelections.get(j));
//						}
//						new OurDicomImageBlackout("Dicom Image Blackout",fileNames,DicomImageBlackout.BurnedInAnnotationFlagAction.ADD_AS_NO_IF_SAVED, dicomNode.getOurCalledAETitle());
//					}
//					catch (Exception e) {
//						e.printStackTrace(System.err);
//					}
//				}
//			}
//			// don't need to send StatusChangeEvent("Blackout complete.") ... DicomImageBlackout already does something similar
//			// DicomImageBlackout sends its own completion message to log, so do not need another one
//			cursorChanger.restoreCursor();
//		}
//	}

    protected class OurQueryTreeBrowser extends QueryTreeBrowser {
        private QueryInformationModel currentRemoteQueryInformationModel;

        /**
		 * @param	q
		 * @param	m
		 * @param	content
		 * @throws	DicomException
		 */
		OurQueryTreeBrowser(QueryInformationModel q,QueryTreeModel m,Container content, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomException {
			super(q,m,content);
            this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        }

		/***/
		protected TreeSelectionListener buildTreeSelectionListenerToDoSomethingWithSelectedLevel() {
			return new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent tse) {

                    // Store all the selected paths
                    QueryTreeRecord[] records = getSelectionPaths();
                    List<QuerySelection> remoteQuerySelectionList = new ArrayList<QuerySelection>();
                    if (records != null) {
                        for (QueryTreeRecord record : records) {
                            remoteQuerySelectionList.add(new QuerySelection(record, currentRemoteQueryInformationModel));
                        }
                    }
                    currentRemoteQuerySelectionList = remoteQuerySelectionList;
				}
			};
		}
	}

    protected class QueryActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

            final QueryParams queryParams = new QueryParams();

            String patientName = queryFilterPatientNameTextField.getText().trim();
            if (patientName != null && patientName.length() > 0) {
                queryParams.setPatientName(patientName);
            }

            String patientID = queryFilterPatientIDTextField.getText().trim();
            if (patientID != null && patientID.length() > 0) {
                queryParams.setPatientId(patientID);
            }

            String accessionNumber = queryFilterAccessionNumberTextField.getText().trim();
            if (accessionNumber != null && accessionNumber.length() > 0) {
                queryParams.setAccessionNumber(accessionNumber);
            }

            String studyDate = queryFilterStudyDateTextField.getText().trim();
            if (studyDate != null && studyDate.length() > 0) {
                queryParams.setStudyDate(studyDate);
            }

//			ae = showInputDialogToSelectNetworkTargetByLocalApplicationEntityName("Select remote system","Query ...",ae);
            remoteQueryRetrievePanel.removeAll();

            controller.query(queryParams);






			remoteQueryRetrievePanel.validate();
		}
	}


    protected class RetrieveActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
            controller.retrieve(currentRemoteQuerySelectionList);
		}
	}

//	protected class LogActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			if (logger instanceof DialogMessageLogger) {
//				((DialogMessageLogger)logger).setVisible(true);
//			}
//		}
//	}

	protected class ConfigureActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
                controller.showConfigureDialog();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
//	Thread activeThread;
	
//	protected class CancelActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			try {
//				if (activeThread != null) {
//					activeThread.interrupt();
//				}
//			} catch (Exception e) {
//				e.printStackTrace(System.err);
//			}
//		}
//	}
//
//	protected class EarliestYearActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			try {
//				if (earliestDatesIndexedBySourceFilePath != null && currentSourceFilePathSelections != null) {
//					Date earliestDateInSet = findEarliestDate(earliestDatesIndexedBySourceFilePath,currentSourceFilePathSelections);
////System.err.println("GiftCloudUploaderPanel.EarliestYearActionListener.actionPerformed(): earliestDateInSet = "+earliestDateInSet);
//					String newYear = DateTimeAttribute.getFormattedString(earliestDateInSet,TimeZone.getTimeZone("GMT")).substring(0,4);
//					modifyDatesTextField.setText(newYear+"0101");
//				}
//			} catch (Exception e) {
//				e.printStackTrace(System.err);
//			}
//		}
//	}
//
//	protected class RandomYearActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			int newYear = (int)(Math.random()*100 + 1970);
//			modifyDatesTextField.setText(Integer.toString(newYear)+"0101");
//		}
//	}
//
//	protected class DefaultYearActionListener implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			modifyDatesTextField.setText(resourceBundle.getString("defaultModifyDatesEpoch"));
//		}
//	}


    public GiftCloudUploaderPanel(final GiftCloudUploaderController controller, final DicomNode dicomNode, final GiftCloudBridge giftCloudBridge, final GiftCloudPropertiesFromBridge giftCloudProperties, final ResourceBundle resourceBundle, final GiftCloudDialogs giftCloudDialogs, final String buildDate, final JLabel statusBar, final GiftCloudReporter reporter) throws DicomException, IOException {
		super();
        this.controller = controller;
        this.giftCloudProperties = giftCloudProperties;
        this.resourceBundle = resourceBundle;
        this.giftCloudDialogs = giftCloudDialogs;
//        this.mainFrame = mainFrame;
        this.buildDate = buildDate;
        this.statusBar = statusBar;
        this.dicomNode = dicomNode;
        this.reporter = reporter;

//		resourceBundle = ResourceBundle.getBundle(resourceBundleName);
//		setTitle(resourceBundle.getString("applicationTitle"));







        srcDatabasePanel = new JPanel();
//		dstDatabasePanel = new JPanel();
		remoteQueryRetrievePanel = new JPanel();

		srcDatabasePanel.setLayout(new GridLayout(1,1));
//		dstDatabasePanel.setLayout(new GridLayout(1,1));
		remoteQueryRetrievePanel.setLayout(new GridLayout(1,1));

        new OurSourceDatabaseTreeBrowser(dicomNode.getSrcDatabase(),srcDatabasePanel);
//		DatabaseTreeBrowser srcDatabaseTreeBrowser = new OurSourceDatabaseTreeBrowser(dicomNode.getSrcDatabase(),srcDatabasePanel);
//		DatabaseTreeBrowser dstDatabaseTreeBrowser = new OurDestinationDatabaseTreeBrowser(dstDatabase,dstDatabasePanel);

		Border panelBorder = BorderFactory.createEtchedBorder();

//		JSplitPane pairOfLocalDatabaseBrowserPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,srcDatabasePanel,dstDatabasePanel);
//		pairOfLocalDatabaseBrowserPanes.setOneTouchExpandable(true);
//		pairOfLocalDatabaseBrowserPanes.setResizeWeight(0.5);
		
//		JSplitPane remoteAndLocalBrowserPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,remoteQueryRetrievePanel,pairOfLocalDatabaseBrowserPanes);
        JSplitPane remoteAndLocalBrowserPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,remoteQueryRetrievePanel,srcDatabasePanel);
		remoteAndLocalBrowserPanes.setOneTouchExpandable(true);
		remoteAndLocalBrowserPanes.setResizeWeight(0.5);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBorder(panelBorder);
		
		JButton configureButton = new JButton(resourceBundle.getString("configureButtonLabelText"));
		configureButton.setToolTipText(resourceBundle.getString("configureButtonToolTipText"));
		buttonPanel.add(configureButton);
		configureButton.addActionListener(new ConfigureActionListener());
//
//		JButton logButton = new JButton(resourceBundle.getString("logButtonLabelText"));
//		logButton.setToolTipText(resourceBundle.getString("logButtonToolTipText"));
//		buttonPanel.add(logButton);
//		logButton.addActionListener(new LogActionListener());
		
		JButton queryButton = new JButton(resourceBundle.getString("queryButtonLabelText"));
		queryButton.setToolTipText(resourceBundle.getString("queryButtonToolTipText"));
		buttonPanel.add(queryButton);
		queryButton.addActionListener(new QueryActionListener());
		
		JButton retrieveButton = new JButton(resourceBundle.getString("retrieveButtonLabelText"));
		retrieveButton.setToolTipText(resourceBundle.getString("retrieveButtonToolTipText"));
		buttonPanel.add(retrieveButton);
		retrieveButton.addActionListener(new RetrieveActionListener());
		
		JButton importButton = new JButton(resourceBundle.getString("importButtonLabelText"));
		importButton.setToolTipText(resourceBundle.getString("importButtonToolTipText"));
		buttonPanel.add(importButton);
		importButton.addActionListener(new ImportActionListener());
		
//		JButton cleanButton = new JButton(resourceBundle.getString("cleanButtonLabelText"));
//		cleanButton.setToolTipText(resourceBundle.getString("cleanButtonToolTipText"));
//		buttonPanel.add(cleanButton);
//		cleanButton.addActionListener(new CleanActionListener());
//
//		JButton blackoutButton = new JButton(resourceBundle.getString("blackoutButtonLabelText"));
//		blackoutButton.setToolTipText(resourceBundle.getString("blackoutButtonToolTipText"));
//		buttonPanel.add(blackoutButton);
//		blackoutButton.addActionListener(new BlackoutActionListener());
		
		JButton exportButton = new JButton(resourceBundle.getString("exportButtonLabelText"));
		exportButton.setToolTipText(resourceBundle.getString("exportButtonToolTipText"));
		buttonPanel.add(exportButton);
		exportButton.addActionListener(new ExportActionListener());

        JButton giftCloudUploadButton = new JButton(resourceBundle.getString("giftCloudUploadButtonLabelText"));
        giftCloudUploadButton.setToolTipText(resourceBundle.getString("giftCloudUploadButtonToolTipText"));
        buttonPanel.add(giftCloudUploadButton);
        giftCloudUploadButton.addActionListener(new GiftCloudUploadActionListener());

//		JButton sendButton = new JButton(resourceBundle.getString("sendButtonLabelText"));
//		sendButton.setToolTipText(resourceBundle.getString("sendButtonToolTipText"));
//		buttonPanel.add(sendButton);
//		sendButton.addActionListener(new SendActionListener());
//
//		JButton purgeButton = new JButton(resourceBundle.getString("purgeButtonLabelText"));
//		purgeButton.setToolTipText(resourceBundle.getString("purgeButtonToolTipText"));
//		buttonPanel.add(purgeButton);
//		purgeButton.addActionListener(new PurgeActionListener());
		
		//JButton cancelButton = new JButton(resourceBundle.getString("cancelButtonLabelText"));
		//cancelButton.setToolTipText(resourceBundle.getString("cancelButtonToolTipText"));
		//buttonPanel.add(cancelButton);
		//cancelButton.addActionListener(new CancelActionListener());
		
		JPanel queryFilterTextEntryPanel = new JPanel();
		queryFilterTextEntryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		queryFilterTextEntryPanel.setBorder(panelBorder);

		JLabel queryIntroduction = new JLabel(resourceBundle.getString("queryIntroductionLabelText"));
		queryFilterTextEntryPanel.add(queryIntroduction);

		JLabel queryFilterPatientNameLabel = new JLabel(resourceBundle.getString("queryPatientNameLabelText"));
		queryFilterPatientNameLabel.setToolTipText(resourceBundle.getString("queryPatientNameToolTipText"));
		queryFilterTextEntryPanel.add(queryFilterPatientNameLabel);
		queryFilterPatientNameTextField = new JTextField("",textFieldLengthForQueryPatientName);
		queryFilterTextEntryPanel.add(queryFilterPatientNameTextField);
		
		JLabel queryFilterPatientIDLabel = new JLabel(resourceBundle.getString("queryPatientIDLabelText"));
		queryFilterPatientIDLabel.setToolTipText(resourceBundle.getString("queryPatientIDToolTipText"));
		queryFilterTextEntryPanel.add(queryFilterPatientIDLabel);
		queryFilterPatientIDTextField = new JTextField("",textFieldLengthForQueryPatientID);
		queryFilterTextEntryPanel.add(queryFilterPatientIDTextField);
		
		JLabel queryFilterStudyDateLabel = new JLabel(resourceBundle.getString("queryStudyDateLabelText"));
		queryFilterStudyDateLabel.setToolTipText(resourceBundle.getString("queryStudyDateToolTipText"));
		queryFilterTextEntryPanel.add(queryFilterStudyDateLabel);
		queryFilterStudyDateTextField = new JTextField("",textFieldLengthForQueryStudyDate);
		queryFilterTextEntryPanel.add(queryFilterStudyDateTextField);
		
		JLabel queryFilterAccessionNumberLabel = new JLabel(resourceBundle.getString("queryAccessionNumberLabelText"));
		queryFilterAccessionNumberLabel.setToolTipText(resourceBundle.getString("queryAccessionNumberToolTipText"));
		queryFilterTextEntryPanel.add(queryFilterAccessionNumberLabel);
		queryFilterAccessionNumberTextField = new JTextField("",textFieldLengthForQueryAccessionNumber);
		queryFilterTextEntryPanel.add(queryFilterAccessionNumberTextField);



		JPanel newTextEntryPanel = new JPanel();
		newTextEntryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		newTextEntryPanel.setBorder(panelBorder);

//		JLabel replacementIntroduction = new JLabel(resourceBundle.getString("giftCloudProductName"));
//		newTextEntryPanel.add(replacementIntroduction);

        projectList = new JComboBox<String>();
        projectList.setEditable(false);
		projectList.setToolTipText(resourceBundle.getString("giftCloudProjectTooltip"));


//		JComboBox projectListComboBox = new JComboBox();
//		replaceAccessionNumberCheckBox.setSelected(true);
//		newTextEntryPanel.add(replaceAccessionNumberCheckBox);
		JLabel projectListLabel = new JLabel(resourceBundle.getString("giftCloudProjectLabelText"));
		newTextEntryPanel.add(projectListLabel);
        newTextEntryPanel.add(projectList);

        JLabel giftCloudServerLabel = new JLabel(resourceBundle.getString("giftCloudServerText"));
        giftCloudServerLabel.setToolTipText(resourceBundle.getString("giftCloudServerTextToolTipText"));


        giftCloudServerText = new AutoSaveTextField(giftCloudProperties.getGiftCloudUrl(), textFieldLengthForGiftCloudServerUrl) {
            @Override
            void autoSave() {
                System.out.println("&&&&& focus action");
                giftCloudProperties.setGiftCloudUrl(getText());
            }
        };

        newTextEntryPanel.add(giftCloudServerLabel);
        newTextEntryPanel.add(giftCloudServerText);


//		replacePatientNameCheckBox = new JCheckBox(resourceBundle.getString("replacementPatientNameLabelText"));
//		replacePatientNameCheckBox.setSelected(true);
//		replacePatientNameCheckBox.setToolTipText(resourceBundle.getString("replacementPatientNameToolTipText"));
//		newTextEntryPanel.add(replacePatientNameCheckBox);
//		replacementPatientNameTextField = new JTextField(resourceBundle.getString("defaultReplacementPatientName"),textFieldLengthForReplacementPatientName);
//		newTextEntryPanel.add(replacementPatientNameTextField);
//
//		replacePatientIDCheckBox = new JCheckBox(resourceBundle.getString("replacementPatientIDLabelText"));
//		replacePatientIDCheckBox.setSelected(true);
//		replacePatientIDCheckBox.setToolTipText(resourceBundle.getString("replacementPatientIDToolTipText"));
//		newTextEntryPanel.add(replacePatientIDCheckBox);
//		replacementPatientIDTextField = new JTextField(resourceBundle.getString("defaultReplacementPatientID"),textFieldLengthForReplacementPatientID);
//		newTextEntryPanel.add(replacementPatientIDTextField);
//
//		replaceAccessionNumberCheckBox = new JCheckBox(resourceBundle.getString("replacementAccessionNumberLabelText"));
//		replaceAccessionNumberCheckBox.setSelected(true);
//		replaceAccessionNumberCheckBox.setToolTipText(resourceBundle.getString("replacementAccessionNumberToolTipText"));
//		newTextEntryPanel.add(replaceAccessionNumberCheckBox);
//		replacementAccessionNumberTextField = new JTextField(resourceBundle.getString("defaultReplacementAccessionNumber"),textFieldLengthForReplacementAccessionNumber);
//		newTextEntryPanel.add(replacementAccessionNumberTextField);
//
//
//		JPanel modifyDatesPanel = new JPanel();
//		modifyDatesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		modifyDatesPanel.setBorder(panelBorder);
//
//		JLabel modifyDatesIntroduction = new JLabel(resourceBundle.getString("modifyDatesIntroductionLabelText"));
//		modifyDatesPanel.add(modifyDatesIntroduction);
//
//		modifyDatesCheckBox = new JCheckBox(resourceBundle.getString("modifyDatesLabelText"));
//		modifyDatesCheckBox.setSelected(false);
//		modifyDatesCheckBox.setToolTipText(resourceBundle.getString("modifyDatesToolTipText"));
//		modifyDatesPanel.add(modifyDatesCheckBox);
//		modifyDatesTextField = new JTextField(resourceBundle.getString("defaultModifyDatesEpoch"),textFieldLengthForModifyDates);
//		modifyDatesPanel.add(modifyDatesTextField);
//
//		JButton earliestYearButton = new JButton(resourceBundle.getString("earliestYearButtonLabelText"));
//		earliestYearButton.setToolTipText(resourceBundle.getString("earliestYearButtonToolTipText"));
//		modifyDatesPanel.add(earliestYearButton);
//		earliestYearButton.addActionListener(new EarliestYearActionListener());
//
//		JButton randomYearButton = new JButton(resourceBundle.getString("randomYearButtonLabelText"));
//		randomYearButton.setToolTipText(resourceBundle.getString("randomYearButtonToolTipText"));
//		modifyDatesPanel.add(randomYearButton);
//		randomYearButton.addActionListener(new RandomYearActionListener());
//
//		JButton defaultYearButton = new JButton(resourceBundle.getString("defaultYearButtonLabelText"));
//		defaultYearButton.setToolTipText(resourceBundle.getString("defaultYearButtonToolTipText"));
//		modifyDatesPanel.add(defaultYearButton);
//		defaultYearButton.addActionListener(new DefaultYearActionListener());
//
//
//		JPanel checkBoxPanel = new JPanel();
//		checkBoxPanel.setLayout(new GridLayout(0,4));	// number of rows is ignored if number of columns is not 0
//		checkBoxPanel.setBorder(panelBorder);
//
//		removeIdentityCheckBox = new JCheckBox(resourceBundle.getString("removeIdentityLabelText"));
//		removeIdentityCheckBox.setSelected(true);
//		checkBoxPanel.add(removeIdentityCheckBox);
//
//		removeDescriptionsCheckBox = new JCheckBox(resourceBundle.getString("removeDescriptionsLabelText"));
//		removeDescriptionsCheckBox.setSelected(false);
//		checkBoxPanel.add(removeDescriptionsCheckBox);
//
//		removeSeriesDescriptionsCheckBox = new JCheckBox(resourceBundle.getString("removeSeriesDescriptionsLabelText"));
//		removeSeriesDescriptionsCheckBox.setSelected(false);
//		checkBoxPanel.add(removeSeriesDescriptionsCheckBox);
//
//		removeProtocolNameCheckBox = new JCheckBox(resourceBundle.getString("removeProtocolNameLabelText"));
//		removeProtocolNameCheckBox.setSelected(false);
//		checkBoxPanel.add(removeProtocolNameCheckBox);
//
//		removeCharacteristicsCheckBox = new JCheckBox(resourceBundle.getString("removeCharacteristicsLabelText"));
//		removeCharacteristicsCheckBox.setSelected(false);
//		checkBoxPanel.add(removeCharacteristicsCheckBox);
//
//		cleanUIDsCheckBox = new JCheckBox(resourceBundle.getString("cleanUIDsLabelText"));
//		cleanUIDsCheckBox.setSelected(true);
//		checkBoxPanel.add(cleanUIDsCheckBox);
//
//		removePrivateCheckBox = new JCheckBox(resourceBundle.getString("removePrivateLabelText"));
//		removePrivateCheckBox.setSelected(true);
//		checkBoxPanel.add(removePrivateCheckBox);
//
//		removeDeviceIdentityCheckBox = new JCheckBox(resourceBundle.getString("removeDeviceIdentityLabelText"));
//		removeDeviceIdentityCheckBox.setSelected(false);
//		checkBoxPanel.add(removeDeviceIdentityCheckBox);
//
//		removeInstitutionIdentityCheckBox = new JCheckBox(resourceBundle.getString("removeInstitutionIdentityLabelText"));
//		removeInstitutionIdentityCheckBox.setSelected(false);
//		checkBoxPanel.add(removeInstitutionIdentityCheckBox);
//
//		removeClinicalTrialAttributesCheckBox = new JCheckBox(resourceBundle.getString("removeClinicalTrialAttributesLabelText"));
//		removeClinicalTrialAttributesCheckBox.setSelected(false);
//		checkBoxPanel.add(removeClinicalTrialAttributesCheckBox);
//
//		addContributingEquipmentCheckBox = new JCheckBox(resourceBundle.getString("addContributingEquipmentLabelText"));
//		addContributingEquipmentCheckBox.setSelected(true);
//		checkBoxPanel.add(addContributingEquipmentCheckBox);
//
//		zipExportCheckBox = new JCheckBox(resourceBundle.getString("zipExportLabelText"));
//		zipExportCheckBox.setSelected(false);
//		checkBoxPanel.add(zipExportCheckBox);
//
//		hierarchicalExportCheckBox = new JCheckBox(resourceBundle.getString("hierarchicalExportLabelText"));
//		hierarchicalExportCheckBox.setSelected(false);
//		hierarchicalExportCheckBox.setToolTipText(resourceBundle.getString("hierarchicalExportToolTipText"));
//		checkBoxPanel.add(hierarchicalExportCheckBox);
//
//		acceptAnyTransferSyntaxCheckBox = new JCheckBox(resourceBundle.getString("acceptAnyTransferSyntaxLabelText"));
//		hierarchicalExportCheckBox.setSelected(false);
//		acceptAnyTransferSyntaxCheckBox.setToolTipText(resourceBundle.getString("acceptAnyTransferSyntaxToolTipText"));
//		checkBoxPanel.add(acceptAnyTransferSyntaxCheckBox);
				
		statusPanel = new StatusPanel(getStatusBar());
        reporter.addProgressListener(statusPanel);

//		JPanel mainPanel = new JPanel();
		{
			GridBagLayout mainPanelLayout = new GridBagLayout();
			setLayout(mainPanelLayout);
			{
				GridBagConstraints remoteAndLocalBrowserPanesConstraints = new GridBagConstraints();
				remoteAndLocalBrowserPanesConstraints.gridx = 0;
				remoteAndLocalBrowserPanesConstraints.gridy = 0;
				remoteAndLocalBrowserPanesConstraints.weightx = 1;
				remoteAndLocalBrowserPanesConstraints.weighty = 1;
				remoteAndLocalBrowserPanesConstraints.fill = GridBagConstraints.BOTH;
				mainPanelLayout.setConstraints(remoteAndLocalBrowserPanes,remoteAndLocalBrowserPanesConstraints);
				add(remoteAndLocalBrowserPanes);
			}
			{
				GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
				buttonPanelConstraints.gridx = 0;
				buttonPanelConstraints.gridy = 1;
				buttonPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
				mainPanelLayout.setConstraints(buttonPanel,buttonPanelConstraints);
				add(buttonPanel);
			}
			{
				GridBagConstraints queryFilterTextEntryPanelConstraints = new GridBagConstraints();
				queryFilterTextEntryPanelConstraints.gridx = 0;
				queryFilterTextEntryPanelConstraints.gridy = 2;
				queryFilterTextEntryPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
				mainPanelLayout.setConstraints(queryFilterTextEntryPanel,queryFilterTextEntryPanelConstraints);
				add(queryFilterTextEntryPanel);
			}
			{
				GridBagConstraints newTextEntryPanelConstraints = new GridBagConstraints();
				newTextEntryPanelConstraints.gridx = 0;
				newTextEntryPanelConstraints.gridy = 3;
				newTextEntryPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
				mainPanelLayout.setConstraints(newTextEntryPanel,newTextEntryPanelConstraints);
				add(newTextEntryPanel);
			}
//			{
//				GridBagConstraints modifyDatesPanelConstraints = new GridBagConstraints();
//				modifyDatesPanelConstraints.gridx = 0;
//				modifyDatesPanelConstraints.gridy = 4;
//				modifyDatesPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
//				mainPanelLayout.setConstraints(modifyDatesPanel,modifyDatesPanelConstraints);
//				add(modifyDatesPanel);
//			}
//			{
//				GridBagConstraints checkBoxPanelConstraints = new GridBagConstraints();
//				checkBoxPanelConstraints.gridx = 0;
//				checkBoxPanelConstraints.gridy = 5;
//				checkBoxPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
//				mainPanelLayout.setConstraints(checkBoxPanel,checkBoxPanelConstraints);
//				add(checkBoxPanel);
//			}
			{
				GridBagConstraints statusBarPanelConstraints = new GridBagConstraints();
				statusBarPanelConstraints.gridx = 0;
				statusBarPanelConstraints.gridy = 6;
				statusBarPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
				mainPanelLayout.setConstraints(statusPanel, statusBarPanelConstraints);
				add(statusPanel);
			}
		}
//		Container content = getContentPane();
//		content.add(mainPanel);
//		pack();
//		setVisible(true);




        // The model for the list of projects is managed by the GiftCloudBridge
        // ToDo: Deal with null giftCloudBridge
        projectList.setModel(giftCloudBridge.getProjectListModel());
	}


}
