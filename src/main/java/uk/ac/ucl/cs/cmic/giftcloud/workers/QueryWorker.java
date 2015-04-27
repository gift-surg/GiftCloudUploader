package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.query.QueryInformationModel;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.DicomNode;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderPanel;

public class QueryWorker implements Runnable {
    private GiftCloudUploaderPanel giftCloudUploaderPanel;
    private QueryInformationModel currentRemoteQueryInformationModel;
    AttributeList filter;
    private DicomNode dicomNode;
    private GiftCloudReporter reporter;

    public QueryWorker(GiftCloudUploaderPanel giftCloudUploaderPanel, final QueryInformationModel currentRemoteQueryInformationModel, AttributeList filter, final DicomNode dicomNode, final GiftCloudReporter reporter) {
        this.giftCloudUploaderPanel = giftCloudUploaderPanel;
        this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        this.filter=filter;
        this.dicomNode = dicomNode;
        this.reporter = reporter;
    }

    public void run() {
        reporter.setWaitCursor();
        String calledAET = currentRemoteQueryInformationModel.getCalledAETitle();
        String localName = dicomNode.getLocalNameFromApplicationEntityTitle(calledAET); //networkApplicationInformation.getLocalNameFromApplicationEntityTitle(calledAET);
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Performing query on "+localName));
        reporter.updateStatusText("Performing query on " + localName + " (" + calledAET + ")");
        try {
            giftCloudUploaderPanel.updateQueryPanel(currentRemoteQueryInformationModel, filter, currentRemoteQueryInformationModel);
            reporter.updateStatusText("Done querying " + localName);
//                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done querying "+localName));
        } catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Query to "+localName+" failed "+e));
            reporter.updateStatusText("Query to " + localName + " (" + calledAET + ") failed due to" + e);
            e.printStackTrace(System.err);
        }
        reporter.updateStatusText("Query to " + localName + " (" + calledAET + ") complete");
//			ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Done querying  "+localName));
        reporter.restoreCursor();
    }
}

