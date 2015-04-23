package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.io.FileUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.File;
import java.util.Iterator;
import java.util.Optional;

/**
 * Maintains lists of files that are waiting to be uploaded
 */
public class PendingUploadTaskList extends BackgroundBlockingQueueTaskList<PendingUploadTask> {
    private final File pendingUploadFolder;

    public PendingUploadTaskList(final GiftCloudProperties giftCloudProperties, final MultiUploadReporter reporter) {
        pendingUploadFolder = giftCloudProperties.getUploadFolder(reporter);
    }

    public void addFileReference(final String fileReference, final Optional<String> projectName) {
        add(new PendingUploadTaskReference(fileReference, projectName));
    }

    public void addFileInstance(final String fileInstance, final Optional<String> projectName) {
        add(new PendingUploadTaskInstance(fileInstance, projectName));
    }

    public File getPendingUploadFolder() {
        return pendingUploadFolder;
    }

    public void addExistingFiles() {
        final File directory = pendingUploadFolder;

        Iterator fileIterator = FileUtils.iterateFiles(directory, null, true);

        final Optional<String> emptyString = Optional.empty();
        while (fileIterator.hasNext()) {
            File file = (File) fileIterator.next();
            addFileInstance(file.getAbsolutePath(), emptyString);
        }
    }


    private class PendingUploadTaskReference extends PendingUploadTask {
        PendingUploadTaskReference(final String fileReference, final Optional<String> projectName) {
            super(fileReference, projectName, true);
        }
    }

    private class PendingUploadTaskInstance extends PendingUploadTask {
        PendingUploadTaskInstance(final String fileInstance, final Optional<String> projectName) {
            super(fileInstance, projectName, true);
        }
    }
}