package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.Optional;
import java.util.Vector;

public class PendingUploadTask {

    enum Append {
        APPEND,
        REPLACE
    }

    enum DeleteAfterUpload {
        DELETE_AFTER_UPLOAD,
        DO_NOT_DELETE_AFTER_UPLOAD
    }

    private final DeleteAfterUpload deleteAfterUpload;
    private final Vector<String> paths;
    private final Append append;
    private final Optional<String> projectName;

    public PendingUploadTask(final Vector<String> paths, final Optional<String> projectName, final Append append, final DeleteAfterUpload deleteAfterUpload) {
        this.paths = paths;
        this.projectName = projectName;
        this.append = append;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public PendingUploadTask(final String path, final Optional<String> projectName, final Append append, final DeleteAfterUpload deleteAfterUpload) {
        this.projectName = projectName;
        this.paths = new Vector<String>();
        paths.add(path);
        this.append = append;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public Vector<String> getPaths() {
        return paths;
    }

    public boolean shouldAppend() {
        return append == Append.APPEND;
    }

    public DeleteAfterUpload getDeleteAfterUpload() {
        return deleteAfterUpload;
    }

    public Optional<String> getProjectName() {
        return projectName;
    }
}
