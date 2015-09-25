package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudAutoUploader;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class BackgroundAddToUploaderService extends BackgroundService<PendingUploadTask, PendingUploadTask> {

    /**
     * When re-starting the service and the previous thread has been signalled to stop but has not yet completed, this
     * is how long we wait before just going ahead and creating a new thread anyway
     */
    private static final long MAXIMUM_THREAD_COMPLETION_WAIT_MS = 1000;

    private final GiftCloudServerFactory serverFactory;
    private final GiftCloudUploader uploader;
    private final GiftCloudAutoUploader autoUploader;
    private final UploaderStatusModel uploaderStatusModel;

    public BackgroundAddToUploaderService(final PendingUploadTaskList pendingUploadList, final GiftCloudServerFactory serverFactory, final GiftCloudUploader uploader, final GiftCloudAutoUploader autoUploader, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporter reporter) {
        super(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, pendingUploadList.getList(), MAXIMUM_THREAD_COMPLETION_WAIT_MS, reporter);
        this.serverFactory = serverFactory;
        this.uploader = uploader;
        this.autoUploader = autoUploader;
        this.uploaderStatusModel = uploaderStatusModel;
    }

    @Override
    protected void processItem(PendingUploadTask pendingUploadTask) throws Exception {

        final Vector<String> paths = pendingUploadTask.getPaths();
        if (paths.size() > 0) {
            uploaderStatusModel.setImportingStatusMessage("Adding file to upload queue:" + paths.get(0));
        }

        final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();
        // Allow user to log in again if they have previously cancelled a login dialog
        giftCloudServer.resetCancellation();

        final String projectName;
        final Optional<String> projectNameOptional = pendingUploadTask.getProjectName();
        if (projectNameOptional.isPresent()) {
            projectName = projectNameOptional.get();
        } else {
            projectName = uploader.getProjectName(giftCloudServer);
        }


        if (pendingUploadTask.shouldAppend()) {
            autoUploader.appendToGiftCloud(giftCloudServer, pendingUploadTask.getPaths(), projectName);
        } else {
            autoUploader.uploadToGiftCloud(giftCloudServer, pendingUploadTask.getPaths(), projectName);
        }
    }

    @Override
    protected void notifySuccess(BackgroundServiceTaskWrapper<PendingUploadTask, PendingUploadTask> taskWrapper) {
    }

    @Override
    protected void notifyFailure(BackgroundServiceTaskWrapper<PendingUploadTask, PendingUploadTask> taskWrapper) {
        final Vector<String> fileCollection = taskWrapper.getTask().getPaths();

        // Update the status for any listeners
        String message;
        final int numUploads = fileCollection.size();
        if (numUploads == 1) {
            message = "Failed to upload file " + fileCollection.get(0);
        } else if (numUploads > 1) {
            message = "Failed to upload files " + fileCollection.get(0);
        } else {
            message = "Failed to upload files";
        }
        List<BackgroundServiceErrorRecord.ErrorRecordItem> errorList = taskWrapper.getErrorRecord().getErrorList();
        final int numFailures = errorList.size();
        if (numFailures > 0) {
            final Throwable throwable = errorList.get(0).getException();
            uploaderStatusModel.setUploadingStatusMessage(message, throwable);
        } else {
            uploaderStatusModel.setUploadingStatusMessage(message);
        }
    }

    @Override
    protected void doPreprocessing() {
        super.doPreprocessing();
        try {
            uploaderStatusModel.setUploadingStatusMessage("Trying to connect to GIFT-Cloud");
            serverFactory.getGiftCloudServer().tryAuthentication();
            uploaderStatusModel.setUploadingStatusMessage("Connected to GIFT-Cloud. Ready to upload.");
        } catch (IOException e) {
            uploaderStatusModel.setUploadingStatusMessage("Cannot upload", e);
        }
    }

    @Override
    protected void doPostprocessing() {
        super.doPostprocessing();
        uploaderStatusModel.setUploadingStatusMessage("Uploader is paused");
    }
}
