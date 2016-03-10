package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.query.QueryInformationModel;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.QueryRetrieveRemoteView;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;

public class QueryWorker implements Runnable {
    private QueryRetrieveRemoteView queryRetrieveRemoteView;
    private QueryInformationModel currentRemoteQueryInformationModel;
    AttributeList filter;
    private UploaderStatusModel uploaderStatusModel;
    private GiftCloudReporterFromApplication reporter;

    public QueryWorker(final QueryRetrieveRemoteView queryRetrieveRemoteView, final QueryInformationModel currentRemoteQueryInformationModel, AttributeList filter, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) {
        this.queryRetrieveRemoteView = queryRetrieveRemoteView;
        this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        this.filter=filter;
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;
    }

    public void run() {
        reporter.setWaitCursor();
        String calledAET = currentRemoteQueryInformationModel.getCalledAETitle();
        reporter.updateStatusText("Performing query on " + calledAET + " (" + calledAET + ")");
        try {
            queryRetrieveRemoteView.updateQueryPanel(currentRemoteQueryInformationModel, filter, currentRemoteQueryInformationModel);
            reporter.updateStatusText("Query to " + calledAET + ") complete");
            uploaderStatusModel.setUploadingStatusMessage("Query to " + calledAET + ") complete");
        } catch (GiftCloudException e) {
            uploaderStatusModel.setUploadingStatusMessage(e.getPithyMessage());
            reporter.reportErrorToUser("The PACS query failed. Please ensure the PACS settings are correct and that the PACS is running.", e);
        } catch (Exception e) {
            uploaderStatusModel.setUploadingStatusMessage("Query to " + calledAET + " failed due to" + e);
            reporter.reportErrorToUser("The PACS query failed. Please ensure the PACS settings are correct and that the PACS is running.", e);
        }
        reporter.restoreCursor();
    }
}

