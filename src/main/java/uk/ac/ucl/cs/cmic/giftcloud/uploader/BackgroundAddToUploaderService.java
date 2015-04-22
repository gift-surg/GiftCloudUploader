package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.util.Optional;

public class BackgroundAddToUploaderService extends BackgroundService<PendingUploadTask, PendingUploadTask> {

    private final GiftCloudServerFactory serverFactory;
    private final GiftCloudUploader uploader;

    public BackgroundAddToUploaderService(final PendingUploadTaskList pendingUploadList, final GiftCloudServerFactory serverFactory, final GiftCloudUploader uploader, final MultiUploadReporter reporter) {
        super(pendingUploadList, reporter);
        this.serverFactory = serverFactory;
        this.uploader = uploader;
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

        if (pendingUploadTask.getAppend()) {
            giftCloudServer.appendToGiftCloud(pendingUploadTask.getPaths(), projectName);
        } else {
            giftCloudServer.uploadToGiftCloud(pendingUploadTask.getPaths(), projectName);
        }
    }
}
