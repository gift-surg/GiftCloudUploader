package uk.ac.ucl.cs.cmic.giftcloud.restserver;

public class UploadResult {

    private final boolean success;

    UploadResult(final boolean success) {
        this.success = success;
    }

    boolean isSuccess() {
        return success;
    }
}
