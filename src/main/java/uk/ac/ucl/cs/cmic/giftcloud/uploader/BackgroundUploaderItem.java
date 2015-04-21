package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;

class BackgroundUploaderItem extends BackgroundServicePendingItem {
    private final CallableUploader callableUploader;

    BackgroundUploaderItem(final CallableUploader callableUploader) {
        this.callableUploader = callableUploader;
    }

    public CallableUploader getCallableUploader() {
        return callableUploader;
    }
}
