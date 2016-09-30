/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.List;

/**
 * Describes a set of one or more files to be imported
 */
public abstract class FileImportRecord {

    protected final List<String> fileNames;
    private final String date;
    protected final PendingUploadTask.DeleteAfterUpload deleteAfterUpload;

    protected FileImportRecord(final List<String> fileNames, final String date, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload) {

        this.fileNames = fileNames;
        this.date = date;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public List<String> getFilenames() {
        return fileNames;
    }

    public String getDate() {
        return date;
    }

    public PendingUploadTask.DeleteAfterUpload getDeleteAfterUpload() {
        return deleteAfterUpload;
    }

    /**
     * @return a unique identifier for the series to which this image belongs. Images are NOT guaranteed to be grouped if the necessary metadata does not exist
     */
    public abstract String getSeriesIdentifier();

    public abstract String getPatientId();

    public abstract String getVisibleName();

    public abstract String getModality();

}

