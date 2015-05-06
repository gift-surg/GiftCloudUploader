package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.NetworkApplicationProperties;
import com.pixelmed.network.PresentationAddress;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.StudyRootQueryInformationModel;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploaderError;
import uk.ac.ucl.cs.cmic.giftcloud.workers.QueryWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.RetrieveWorker;

import java.util.List;
import java.util.Optional;

public class QueryRetrieveController {

    private QueryRetrieveRemoteView queryRetrieveRemoteView;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final DicomNode dicomNode;
    private final GiftCloudReporterFromApplication reporter;
    private Optional<QueryInformationModel> currentRemoteQueryInformationModel = Optional.empty();
    private Thread activeThread = null;

    QueryRetrieveController(final QueryRetrieveRemoteView queryRetrieveRemoteView, final GiftCloudPropertiesFromApplication giftCloudProperties, final DicomNode dicomNode, final GiftCloudReporterFromApplication reporter) {
        this.queryRetrieveRemoteView = queryRetrieveRemoteView;
        this.giftCloudProperties = giftCloudProperties;
        this.dicomNode = dicomNode;
        this.reporter = reporter;
    }

    public boolean isRunning() {
        return (activeThread != null && activeThread.isAlive());
    }

    public synchronized void retrieve(final List<QuerySelection> currentRemoteQuerySelectionList) throws GiftCloudException {
        // Report an error if a previous thread has not yet completed
        if (activeThread != null && activeThread.isAlive()) {
            throw new GiftCloudException(GiftCloudUploaderError.QUERY_RETRIEVE_STILL_IN_PROGRESS);
        }

        if (currentRemoteQueryInformationModel.isPresent()) {
            Thread activeThread = new Thread(new RetrieveWorker(currentRemoteQuerySelectionList, currentRemoteQueryInformationModel.get(), reporter));
            activeThread.start();
        } else {
            throw new GiftCloudException(GiftCloudUploaderError.NO_QUERY_OR_QUERY_FAILED);
        }
    }

    public synchronized void query(final QueryParams queryParams) throws GiftCloudException, DicomException {

        // Report an error if a previous thread has not yet completed
        if (activeThread != null && activeThread.isAlive()) {
            throw new GiftCloudException(GiftCloudUploaderError.QUERY_RETRIEVE_STILL_IN_PROGRESS);
        }

        currentRemoteQueryInformationModel = Optional.of(createRemoteQueryInformationModel());
        queryRetrieveRemoteView.removeAll();
        queryRetrieveRemoteView.validate();

        AttributeList filter = queryParams.build();
        Thread activeThread = new Thread(new QueryWorker(queryRetrieveRemoteView, currentRemoteQueryInformationModel.get(), filter, dicomNode, reporter));
        activeThread.start();
    }

    QueryInformationModel createRemoteQueryInformationModel() throws GiftCloudException {
        final String remoteAEForQuery = giftCloudProperties.getCurrentlySelectedQueryTargetAE();
        if (StringUtils.isBlank(remoteAEForQuery)) {
            throw new GiftCloudException(GiftCloudUploaderError.EMPTY_AE);
        }

        if (giftCloudProperties.areNetworkPropertiesValid() && dicomNode.isNetworkApplicationInformationValid()) {
            final String queryCallingAETitle = giftCloudProperties.getCallingAETitle();
            final String queryCalledAETitle = dicomNode.getApplicationEntityTitleFromLocalName(remoteAEForQuery);
            final PresentationAddress presentationAddress = dicomNode.getPresentationAddress(queryCalledAETitle);

            if (presentationAddress == null) {
                throw new GiftCloudException(GiftCloudUploaderError.QUERY_CANNOT_DETERMINE_PRESENTATION_ADDRESS, "Cannot determine presentation address for remote query AE:" + remoteAEForQuery);
            }

            final String queryHost = presentationAddress.getHostname();
            final int queryPort = presentationAddress.getPort();
            final String queryModel = dicomNode.getQueryModel(queryCalledAETitle);
            final int queryDebugLevel = giftCloudProperties.getQueryDebugLevel();

            if (NetworkApplicationProperties.isStudyRootQueryModel(queryModel) || queryModel == null) {
                return new StudyRootQueryInformationModel(queryHost,queryPort,queryCalledAETitle,queryCallingAETitle,queryDebugLevel);
            } else {
                throw new GiftCloudException(GiftCloudUploaderError.QUERY_MODEL_NOT_SUPPORTED, "The query model is not supported for remote query AE:" + remoteAEForQuery);
            }
        } else {
            throw new GiftCloudException(GiftCloudUploaderError.NETWORK_PROPERTIES_INVALID);
        }
    }

    public void waitForCompletion(final long maxWaitTimeMs) {
        if (activeThread != null) {
            try {
                activeThread.join(maxWaitTimeMs);
            } catch (InterruptedException e) {
            }
        }
    }
}
