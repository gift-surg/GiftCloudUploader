package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class BackgroundUploader extends BackgroundService<CallableUploader, Future<Set<String>>> {

    /**
     * When re-starting the service and the previous thread has been signalled to stop but has not yet completed, this
     * is how long we wait before just going ahead and creating a new thread anyway
     */
    private static final long MAXIMUM_THREAD_COMPLETION_WAIT_MS = 10000;

    final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList;
    private BackgroundUploadOutcomeCallback outcomeCallback;
    private UploaderStatusModel uploaderStatusModel;


    public BackgroundUploader(final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList, final BackgroundUploadOutcomeCallback outcomeCallback, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporter reporter) {
        super(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundCompletionServiceTaskList, MAXIMUM_THREAD_COMPLETION_WAIT_MS, reporter);

        this.backgroundCompletionServiceTaskList = backgroundCompletionServiceTaskList;
        this.outcomeCallback = outcomeCallback;
        this.uploaderStatusModel = uploaderStatusModel;
    }

    public void addUploader(final CallableUploader uploader) {
        backgroundCompletionServiceTaskList.addNewTask(uploader);
    }

    @Override
    protected void processItem(final Future<Set<String>> futureResult) throws Exception {
        final Set<String> result = futureResult.get();
    }

    @Override
    protected void notifySuccess(final BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>> taskWrapper) {
        final FileCollection fileCollection = taskWrapper.getTask().getFileCollection();

        // Alert the caller of the uploading success
        outcomeCallback.fileUploadSuccess(fileCollection);

        // Update the status for any listeners
        final int numUploads = fileCollection.getFileCount();
        String message;
        if (numUploads < 1) {
            message = "No files uploaded";
        } else if (numUploads == 1) {
            message = "File " + fileCollection.getFiles().iterator().next().getName() + " uploaded successfully";
        } else {
            message = numUploads + " files uploaded successfully. First file: " + fileCollection.getFiles().iterator().next().getName();
        }
        uploaderStatusModel.setUploadingStatusMessage(message);
    }

    @Override
    protected void notifyFailure(final BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>> taskWrapper) {
        final FileCollection fileCollection = taskWrapper.getTask().getFileCollection();

        // Alert the caller of the uploading failure
        outcomeCallback.fileUploadFailure(fileCollection);

        // Update the status for any listeners
        String message;
        final int numUploads = fileCollection.getFileCount();
        if (numUploads == 1) {
            message = "Failed to upload file " + fileCollection.getFiles().iterator().next();
        } else if (numUploads > 1) {
            message = "Failed to upload files " + fileCollection.getFiles().iterator().next();
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

    public interface BackgroundUploadOutcomeCallback {
        void fileUploadSuccess(final FileCollection fileCollection);
        void fileUploadFailure(final FileCollection fileCollection);

    }
}
