/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.util.ArrayList;

/**
 * Model class for status messages that are reported to the user. Threadsafe implementation to allow updates from multiple uploading threads
 */
public class UploaderStatusModel {
    private final java.util.List<StatusListener> listeners = new ArrayList<StatusListener>();
    private Optional<String> uploadingStatusMessage = Optional.empty();
    private Optional<String> importingStatusMessage = Optional.empty();

    public synchronized void addListener(final StatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void setUploadingStatusMessage(final String newStatusMessage, final Throwable throwable) {
        final String extendedMessage = throwable instanceof GiftCloudException ? ((GiftCloudException)throwable).getPithyMessage() : throwable.getLocalizedMessage();
        setUploadingStatusMessage(newStatusMessage + ". " + extendedMessage);
    }

    public synchronized void setUploadingStatusMessage(final String newStatusMessage) {
        if (!uploadingStatusMessage.isPresent() || !uploadingStatusMessage.get().equals(newStatusMessage)) {
            uploadingStatusMessage = Optional.of(newStatusMessage);
            notifyUploadingStatusMessageChanged();
        }
    }

    private synchronized void notifyUploadingStatusMessageChanged() {
        if (uploadingStatusMessage.isPresent()) {
            for (final StatusListener listener : listeners) {
                listener.uploaderStatusMessageChanged(uploadingStatusMessage.get());
            }
        }
    }

    public synchronized void setImportingStatusMessage(final String newStatusMessage, final Throwable throwable) {
        final String extendedMessage = throwable instanceof GiftCloudException ? ((GiftCloudException)throwable).getPithyMessage() : throwable.getLocalizedMessage();
        setImportingStatusMessage(newStatusMessage + ". " + extendedMessage);
    }

    public synchronized void setImportingStatusMessage(final String newStatusMessage) {
        if (!importingStatusMessage.isPresent() || !importingStatusMessage.get().equals(newStatusMessage)) {
            importingStatusMessage = Optional.of(newStatusMessage);
            notifyImportingStatusMessageChanged();
        }
    }

    private synchronized void notifyImportingStatusMessageChanged() {
        if (importingStatusMessage.isPresent()) {
            for (final StatusListener listener : listeners) {
                listener.importerStatusMessageChanged(importingStatusMessage.get());
            }
        }
    }

    public interface StatusListener {
        void uploaderStatusMessageChanged(final String newMessage);
        void importerStatusMessageChanged(String s);
    }
}
