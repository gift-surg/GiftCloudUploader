package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.NetworkApplicationProperties;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.StudyRootQueryInformationModel;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploaderError;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.workers.QueryWorker;
import uk.ac.ucl.cs.cmic.giftcloud.workers.RetrieveWorker;

import java.util.List;
import java.util.Optional;

public class QueryRetrieveController {

    private QueryRetrieveRemoteView queryRetrieveRemoteView;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final DicomNode dicomNode;
    private UploaderStatusModel uploaderStatusModel;
    private final GiftCloudReporterFromApplication reporter;
    private Optional<QueryInformationModel> currentRemoteQueryInformationModel = Optional.empty();
    private Thread activeThread = null;

    QueryRetrieveController(final QueryRetrieveRemoteView queryRetrieveRemoteView, final GiftCloudPropertiesFromApplication giftCloudProperties, final DicomNode dicomNode, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) {
        this.queryRetrieveRemoteView = queryRetrieveRemoteView;
        this.giftCloudProperties = giftCloudProperties;
        this.dicomNode = dicomNode;
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;

        // Add a shutdown hook for graceful exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                cleanup(giftCloudProperties.getShutdownTimeoutMs());
            }
        });
    }

    private void cleanup(final long maxWaitTimeMs) {
        waitForCompletion(maxWaitTimeMs);
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
        Thread activeThread = new Thread(new QueryWorker(queryRetrieveRemoteView, currentRemoteQueryInformationModel.get(), filter, dicomNode, uploaderStatusModel, reporter));
        activeThread.start();
    }

    QueryInformationModel createRemoteQueryInformationModel() throws GiftCloudException {
        final String queryAETitle = giftCloudProperties.getListenerAETitle();
        final Optional<String> queryCalledAETitle = giftCloudProperties.getPacsAeTitle();
        final Optional<String> queryHost = giftCloudProperties.getPacsHostName();
        final int queryPort = giftCloudProperties.getPacsPort();
        final Optional<String> queryModel = giftCloudProperties.getPacsQueryModel();
        final int queryDebugLevel = giftCloudProperties.getQueryDebugLevel();

        if (!queryHost.isPresent() || StringUtils.isBlank(queryHost.get())) {
            throw new GiftCloudException(GiftCloudUploaderError.QUERY_NO_HOST, "Please set the PACS host name in the settings before performing a query.");
        }

        if (!queryCalledAETitle.isPresent() || StringUtils.isBlank(queryCalledAETitle.get())) {
            throw new GiftCloudException(GiftCloudUploaderError.QUERY_NO_CALLED_AE_TITLE, "Please set the PACS AE title in the settings before performing a query.");
        }

        if (!queryModel.isPresent() || NetworkApplicationProperties.isStudyRootQueryModel(queryModel.get())) {
            return new StudyRootQueryInformationModel(queryHost.get(), queryPort, queryCalledAETitle.get(), queryAETitle, queryDebugLevel);
        } else {
            throw new GiftCloudException(GiftCloudUploaderError.QUERY_MODEL_NOT_SUPPORTED, "The query model is not supported for remote query AE:" + queryCalledAETitle.get());
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
