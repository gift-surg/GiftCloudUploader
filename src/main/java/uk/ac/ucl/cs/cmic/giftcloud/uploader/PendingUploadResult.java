package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.Vector;

public class PendingUploadResult extends BackgroundServiceResult<PendingUploadItem, Vector<String>> {
    PendingUploadResult(final PendingUploadItem pendingItem, final Vector<String> result, final BackgroundServiceErrorRecord errorRecord) {
        super(pendingItem, result, errorRecord);
    }
}
