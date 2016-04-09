package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.util.ArrayList;
import java.util.List;

public class PendingUploadTask {

    enum Append {
        APPEND,
        REPLACE
    }

    public enum DeleteAfterUpload {
        DELETE_AFTER_UPLOAD,
        DO_NOT_DELETE_AFTER_UPLOAD
    }

    private final DeleteAfterUpload deleteAfterUpload;
    private final List<String> paths;
    private final Append append;
    private final Optional<String> projectName;

    public PendingUploadTask(final List<String> paths, final Optional<String> projectName, final Append append, final DeleteAfterUpload deleteAfterUpload) {
        this.paths = paths;
        this.projectName = projectName;
        this.append = append;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public PendingUploadTask(final String path, final Optional<String> projectName, final Append append, final DeleteAfterUpload deleteAfterUpload) {
        this.projectName = projectName;
        this.paths = new ArrayList<String>();
        paths.add(path);
        this.append = append;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public List<String> getPaths() {
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
