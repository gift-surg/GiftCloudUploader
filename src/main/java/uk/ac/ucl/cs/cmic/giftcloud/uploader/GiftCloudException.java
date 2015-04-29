package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.io.IOException;
import java.util.Optional;

public class GiftCloudException extends IOException {
    private final GiftCloudUploaderError error;
    private final Optional<String> additionalMessage;

    public GiftCloudException(final GiftCloudUploaderError error) {
        super(error.getMessageWithErrorCode());
        this.error = error;
        additionalMessage = Optional.empty();
    }

    public GiftCloudException(final GiftCloudUploaderError error, final String additionalMessage) {
        super(error.getMessageWithErrorCode() + " " + additionalMessage);
        this.error = error;
        this.additionalMessage = Optional.of(additionalMessage);
    }
}
