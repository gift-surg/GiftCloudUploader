package uk.ac.ucl.cs.cmic.giftcloud.uploader;

/**
 * An unchecked exception wrapper for GiftCloudExceptions. This is possibly the least bad way to deal with leaky abstractions, where a standard interface does not allow implementations to throw exceptions
 */
public class GiftCloudUncheckedException extends Error {
        private final GiftCloudException wrappedException;

        public GiftCloudUncheckedException(final GiftCloudException source) {
            this.wrappedException = source;
        }

        public String toString() {
            return wrappedException.toString();
        }

        public GiftCloudException getWrappedException() {
            return wrappedException;
        }
}
