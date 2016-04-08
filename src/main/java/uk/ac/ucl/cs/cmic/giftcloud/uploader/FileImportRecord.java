package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.Vector;

/**
 * Describes a set of one or more files to be imported
 */
public abstract class FileImportRecord {

    protected final Vector<String> fileNames;
    private final String date;
    protected final PendingUploadTask.DeleteAfterUpload deleteAfterUpload;

    protected FileImportRecord(final Vector<String> fileNames, final String date, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload) {

        this.fileNames = fileNames;
        this.date = date;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public Vector<String> getFilenames() {
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

    public abstract String getVisibleName();

    public abstract String getModality();

}

