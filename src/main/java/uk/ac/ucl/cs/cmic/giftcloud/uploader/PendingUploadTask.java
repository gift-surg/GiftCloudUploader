package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.Optional;
import java.util.Vector;

public class PendingUploadTask {

    private final Vector<String> paths;
    private final boolean append;
    private final Optional<String> projectName;

    public PendingUploadTask(final Vector<String> paths, final Optional<String> projectName, final boolean append) {
        this.paths = paths;
        this.projectName = projectName;
        this.append = append;
    }

    public PendingUploadTask(final String path, final Optional<String> projectName, final boolean append) {
        this.projectName = projectName;
        this.paths = new Vector<String>();
        paths.add(path);
        this.append = append;
    }

    public Vector<String> getPaths() {
        return paths;
    }

    public boolean getAppend() {
        return append;
    }

    public Optional<String> getProjectName() {
        return projectName;
    }
}
