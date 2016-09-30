/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;

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
