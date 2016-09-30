/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.dicom.*;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.QueryTreeRecord;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.QuerySelection;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;

import java.util.Enumeration;
import java.util.List;

public class RetrieveWorker implements Runnable {
    private QueryInformationModel currentRemoteQueryInformationModel;
    private UploaderStatusModel uploaderStatusModel;
    private GiftCloudReporterFromApplication reporter;
    private List<QuerySelection> currentRemoteQuerySelectionList;

    public RetrieveWorker(final List<QuerySelection> currentRemoteQuerySelectionList, final QueryInformationModel currentRemoteQueryInformationModel, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) {
        this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;
        this.currentRemoteQuerySelectionList = currentRemoteQuerySelectionList;
    }

    public void run() {
        reporter.setWaitCursor();

        final List<QuerySelection> queryList = currentRemoteQuerySelectionList;
        for (QuerySelection currentQuerySelection : queryList) {
            retrieve(currentQuerySelection);
        }

        reporter.restoreCursor();
    }

    private void retrieve(final QuerySelection currentQuerySelection) {
        if (currentQuerySelection.getCurrentRemoteQuerySelectionLevel() == null) {	// they have selected the root of the tree
            QueryTreeRecord parent = currentQuerySelection.getCurrentRemoteQuerySelectionQueryTreeRecord();
            if (parent != null) {
                reporter.updateStatusText("Retrieving everything from " + currentQuerySelection.getCurrentRemoteQuerySelectionRetrieveAE());
                Enumeration children = parent.children();
                if (children != null) {
                    int nChildren = parent.getChildCount();
                    reporter.startProgressBar(nChildren);
                    int doneCount = 0;
                    while (children.hasMoreElements()) {
                        QueryTreeRecord r = (QueryTreeRecord)(children.nextElement());
                        if (r != null) {
                            QuerySelection currentRemoteQuerySelection = new QuerySelection(r, currentRemoteQueryInformationModel);
                            reporter.updateStatusText("Retrieving " + currentRemoteQuerySelection.getCurrentRemoteQuerySelectionLevel() + " " + currentRemoteQuerySelection.getCurrentRemoteQuerySelectionUniqueKey().getSingleStringValueOrEmptyString() + " from " + currentQuerySelection.getCurrentRemoteQuerySelectionRetrieveAE());
                            performRetrieve(currentRemoteQuerySelection.getCurrentRemoteQuerySelectionUniqueKeys(), currentRemoteQuerySelection.getCurrentRemoteQuerySelectionLevel(), currentQuerySelection.getCurrentRemoteQuerySelectionRetrieveAE());
                            reporter.updateProgressBar(++doneCount);
                        }
                    }
                    reporter.endProgressBar();
                }
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done sending retrieval request"));
            }
        }
        else {
//            reporter.updateProgress("Retrieving "+currentQuerySelection.getCurrentRemoteQuerySelectionLevel()+" "+currentQuerySelection.getCurrentRemoteQuerySelectionUniqueKey().getSingleStringValueOrEmptyString()+" from "+localName);
//                reporter.sendLn("Request retrieval of "+currentQuerySelection.getCurrentRemoteQuerySelectionLevel()+" "+currentQuerySelection.getCurrentRemoteQuerySelectionUniqueKey().getSingleStringValueOrEmptyString()+" from "+localName+" ("+currentQuerySelection.getCurrentRemoteQuerySelectionRetrieveAE()+")");
            reporter.startProgressBar(1);
            performRetrieve(currentQuerySelection.getCurrentRemoteQuerySelectionUniqueKeys(), currentQuerySelection.getCurrentRemoteQuerySelectionLevel(), currentQuerySelection.getCurrentRemoteQuerySelectionRetrieveAE());

            reporter.updateStatusText("Done sending retrieval request");
            reporter.endProgressBar();
        }
    }


    private void performRetrieve(AttributeList uniqueKeys,String selectionLevel,String retrieveAE) {
        try {
            uploaderStatusModel.setImportingStatusMessage("Retrieving files...");
            AttributeList identifier = new AttributeList();
            if (uniqueKeys != null) {
                identifier.putAll(uniqueKeys);
                { AttributeTag t = TagFromName.QueryRetrieveLevel; Attribute a = new CodeStringAttribute(t); a.addValue(selectionLevel); identifier.put(t,a); }
                currentRemoteQueryInformationModel.performHierarchicalMoveFrom(identifier,retrieveAE);
                uploaderStatusModel.setImportingStatusMessage("Files have been retrieved and added to the upload queue.");
            }
            // else do nothing, since no unique key to specify what to retrieve
        } catch (Exception e) {
            uploaderStatusModel.setImportingStatusMessage("The files could not be retrieved. Please ensure the PACS settings are correct");
            reporter.silentLogException(e, "Retrieve operation failed");
        }
    }
}
