package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.Vector;

public class PendingUploadResult extends BackgroundServiceResult<PendingUploadItem> {
    private final Vector<String> fileList;

    PendingUploadResult(final PendingUploadItem pendingItem, final Vector<String> fileList, final BackgroundServiceErrorRecord errorRecord) {
        super(pendingItem, errorRecord);
        this.fileList = fileList;
    }

    public Vector<String> getFileList() {
        return fileList;
    }
}
