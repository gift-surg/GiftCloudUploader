package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.NetworkApplicationProperties;
import com.pixelmed.network.PresentationAddress;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.StudyRootQueryInformationModel;
import uk.ac.ucl.cs.cmic.giftcloud.workers.QueryWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.RetrieveWorker;

import java.util.List;

public class QueryRetrieveHelper {

    private GiftCloudPropertiesFromApplication giftCloudProperties;
    private DicomNode dicomNode;
    private GiftCloudReporter reporter;

    QueryRetrieveHelper(final GiftCloudPropertiesFromApplication giftCloudProperties, final DicomNode dicomNode, final GiftCloudReporter reporter) {
        this.giftCloudProperties = giftCloudProperties;
        this.dicomNode = dicomNode;
        this.reporter = reporter;
    }

    private QueryInformationModel currentRemoteQueryInformationModel;

    public void retrieve(final List<QuerySelection> currentRemoteQuerySelectionList) {
        Thread activeThread = new Thread(new RetrieveWorker(currentRemoteQuerySelectionList, currentRemoteQueryInformationModel, reporter));
        activeThread.start();
        // ToDo: Cache active thread so we can provide a cancel option
    }

    public void query(final GiftCloudUploaderPanel giftCloudUploaderPanel, final QueryParams queryParams) {
        //new QueryRetrieveDialog("GiftCloudUploaderPanel Query",400,512);
        String ae = giftCloudProperties.getCurrentlySelectedQueryTargetAE();
        if (ae != null) {
            setCurrentRemoteQueryInformationModel(ae);
            if (currentRemoteQueryInformationModel == null) {
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Cannot query "+ae));
            }
            else {
                try {
                    AttributeList filter = queryParams.build();
                    Thread activeThread = new Thread(new QueryWorker(giftCloudUploaderPanel, currentRemoteQueryInformationModel, filter, dicomNode, reporter));
                    activeThread.start();
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                    reporter.updateStatusText("Query to " + ae + " failed");
//                        ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Query to "+ae+" failed"));
                }
            }
        }
    }


    void setCurrentRemoteQueryInformationModel(String remoteAEForQuery) {
        currentRemoteQueryInformationModel=null;
        String stringForTitle="";
        if (remoteAEForQuery != null && remoteAEForQuery.length() > 0 && giftCloudProperties.areNetworkPropertiesValid() && dicomNode.isNetworkApplicationInformationValid()) {
            try {
                String              queryCallingAETitle = giftCloudProperties.getCallingAETitle();
                String               queryCalledAETitle = dicomNode.getApplicationEntityTitleFromLocalName(remoteAEForQuery);
                PresentationAddress presentationAddress = dicomNode.getPresentationAddress(queryCalledAETitle);

                if (presentationAddress == null) {
                    throw new Exception("For remote query AE <"+remoteAEForQuery+">, presentationAddress cannot be determined");
                }

                String                        queryHost = presentationAddress.getHostname();
                int			      queryPort = presentationAddress.getPort();
                String                       queryModel = dicomNode.getQueryModel(queryCalledAETitle); //    networkApplicationInformation.getApplicationEntityMap().getQueryModel(queryCalledAETitle);
                int                     queryDebugLevel = giftCloudProperties.getQueryDebugLevel();

                if (NetworkApplicationProperties.isStudyRootQueryModel(queryModel) || queryModel == null) {
                    currentRemoteQueryInformationModel=new StudyRootQueryInformationModel(queryHost,queryPort,queryCalledAETitle,queryCallingAETitle,queryDebugLevel);
                    stringForTitle=":"+remoteAEForQuery;
                }
                else {
                    throw new Exception("For remote query AE <"+remoteAEForQuery+">, query model "+queryModel+" not supported");
                }
            }
            catch (Exception e) {		// if an AE's property has no value, or model not supported
                e.printStackTrace(System.err);
            }
        }
    }

}
