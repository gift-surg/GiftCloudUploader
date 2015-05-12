package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.query.QueryInformationModel;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.DicomNode;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.QueryRetrieveRemoteView;

public class QueryWorker implements Runnable {
    private QueryRetrieveRemoteView queryRetrieveRemoteView;
    private QueryInformationModel currentRemoteQueryInformationModel;
    AttributeList filter;
    private DicomNode dicomNode;
    private GiftCloudReporterFromApplication reporter;

    public QueryWorker(final QueryRetrieveRemoteView queryRetrieveRemoteView, final QueryInformationModel currentRemoteQueryInformationModel, AttributeList filter, final DicomNode dicomNode, final GiftCloudReporterFromApplication reporter) {
        this.queryRetrieveRemoteView = queryRetrieveRemoteView;
        this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        this.filter=filter;
        this.dicomNode = dicomNode;
        this.reporter = reporter;
    }

    public void run() {
        reporter.setWaitCursor();
        String calledAET = currentRemoteQueryInformationModel.getCalledAETitle();
        reporter.updateStatusText("Performing query on " + calledAET + " (" + calledAET + ")");
        try {
            queryRetrieveRemoteView.updateQueryPanel(currentRemoteQueryInformationModel, filter, currentRemoteQueryInformationModel);
            reporter.updateStatusText("Done querying " + calledAET);
        } catch (Exception e) {
            reporter.updateStatusText("Query to " + calledAET + " failed due to" + e);
            e.printStackTrace(System.err);
        }
        reporter.updateStatusText("Query to " + calledAET + ") complete");
        reporter.restoreCursor();
    }
}

