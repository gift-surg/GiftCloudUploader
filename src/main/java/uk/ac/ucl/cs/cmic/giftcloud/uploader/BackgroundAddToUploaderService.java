package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudAutoUploader;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.util.Optional;

public class BackgroundAddToUploaderService extends BackgroundService<PendingUploadTask, PendingUploadTask> {

    private final GiftCloudServerFactory serverFactory;
    private final GiftCloudUploader uploader;
    private final GiftCloudAutoUploader autoUploader;

    public BackgroundAddToUploaderService(final PendingUploadTaskList pendingUploadList, final GiftCloudServerFactory serverFactory, final GiftCloudUploader uploader, final GiftCloudAutoUploader autoUploader, final MultiUploadReporter reporter) {
        super(pendingUploadList.getList(), reporter);
        this.serverFactory = serverFactory;
        this.uploader = uploader;
        this.autoUploader = autoUploader;
    }

    @Override
    protected void processItem(PendingUploadTask pendingUploadTask) throws Exception {
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

    }
}
