/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/



package uk.ac.ucl.cs.cmic.giftcloud.util;


import java.io.Closeable;
import java.io.IOException;

public abstract class CloseableResource<ReturnType, ResourceType extends Closeable> {

    protected abstract ReturnType run() throws IOException;
    protected ResourceType resource = null;

    public ReturnType tryWithResource() throws IOException {
        IOException storedRunException = null;
        ReturnType returnValue = null;

        try {
            returnValue = run();

        } catch (IOException runException) {
            storedRunException = runException;

        } finally {
            try {
                close();
            } catch (IOException closeException) {
                if (storedRunException == null) {
                    throw closeException;
                } else {
                    throw storedRunException;
                }
            }
            if (storedRunException != null) {
                throw storedRunException;
            }
        }

        return returnValue;
    }

    private void close() throws IOException {
        if (resource != null) {
            resource.close();
        }
    }
}