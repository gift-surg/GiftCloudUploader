package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.Vector;

/**
 * Describes a set of one or more files to be imported
 */
public abstract class FileImportRecord {

    protected Vector<String> fileNames;
    protected PendingUploadTask.DeleteAfterUpload deleteAfterUpload;

    protected FileImportRecord(Vector<String> fileNames, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload) {

        this.fileNames = fileNames;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public Vector<String> getFilenames() {
        return fileNames;
    }

    public PendingUploadTask.DeleteAfterUpload getDeleteAfterUpload() {
        return deleteAfterUpload;
    }
}

