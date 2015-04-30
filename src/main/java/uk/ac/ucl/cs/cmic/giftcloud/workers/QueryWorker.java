package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.query.QueryInformationModel;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.DicomNode;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.QueryRetrieveRemoteView;

public class QueryWorker implements Runnable {
    private QueryRetrieveRemoteView queryRetrievePanel;
    private QueryInformationModel currentRemoteQueryInformationModel;
    AttributeList filter;
    private DicomNode dicomNode;
    private GiftCloudReporter reporter;

    public QueryWorker(final QueryRetrieveRemoteView queryRetrievePanel, final QueryInformationModel currentRemoteQueryInformationModel, AttributeList filter, final DicomNode dicomNode, final GiftCloudReporter reporter) {
        this.queryRetrievePanel = queryRetrievePanel;
        this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        this.filter=filter;
        this.dicomNode = dicomNode;
        this.reporter = reporter;
    }

    public void run() {
        reporter.setWaitCursor();
        String calledAET = currentRemoteQueryInformationModel.getCalledAETitle();
        String localName = dicomNode.getLocalNameFromApplicationEntityTitle(calledAET); //networkApplicationInformation.getLocalNameFromApplicationEntityTitle(calledAET);
        reporter.updateStatusText("Performing query on " + localName + " (" + calledAET + ")");
        try {
            queryRetrievePanel.updateQueryPanel(currentRemoteQueryInformationModel, filter, currentRemoteQueryInformationModel);
            reporter.updateStatusText("Done querying " + localName);
        } catch (Exception e) {
            reporter.updateStatusText("Query to " + localName + " (" + calledAET + ") failed due to" + e);
            e.printStackTrace(System.err);
        }
        reporter.updateStatusText("Query to " + localName + " (" + calledAET + ") complete");
        reporter.restoreCursor();
    }
}

