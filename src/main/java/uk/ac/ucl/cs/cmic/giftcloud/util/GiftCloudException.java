package uk.ac.ucl.cs.cmic.giftcloud.util;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;

public class GiftCloudException extends IOException {
    private final GiftCloudUploaderError error;
    private final Optional<String> additionalMessage;
    private final String pithyMessage;
    private final boolean allowRetry;

    public GiftCloudException(final GiftCloudUploaderError error) {
        super(error.getMessageWithErrorCode());
        this.error = error;
        this.pithyMessage = error.getPithyMessage();
        this.additionalMessage = Optional.empty();
        this.allowRetry = error.allowRetry();
    }

    public GiftCloudException(final GiftCloudUploaderError error, final Throwable cause) {
        super(error.getMessageWithErrorCode(), cause);
        this.error = error;
        this.pithyMessage = error.getPithyMessage();
        this.additionalMessage = Optional.empty();
        this.allowRetry = error.allowRetry();
    }

    public GiftCloudException(final GiftCloudUploaderError error, final String additionalMessage) {
        super(error.getMessageWithErrorCode() + " " + additionalMessage);
        this.error = error;
        this.pithyMessage = error.getPithyMessage();
        this.additionalMessage = Optional.of(additionalMessage);
        this.allowRetry = error.allowRetry();
    }

    public String getPithyMessage() {
        return pithyMessage;
    }

    public boolean allowRetry() {
        return allowRetry;
    }
}
