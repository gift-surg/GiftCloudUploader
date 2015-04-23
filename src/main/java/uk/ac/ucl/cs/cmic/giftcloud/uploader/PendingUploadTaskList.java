package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.io.FileUtils;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Maintains lists of files that are waiting to be uploaded
 */
public class PendingUploadTaskList {
    private final BackgroundBlockingQueueTaskList<PendingUploadTask> taskList;
    private final File pendingUploadFolder;
    private final UniqueFileMap<PendingUploadTask> fileMap = new UniqueFileMap<PendingUploadTask>();
    private MultiUploadReporter reporter;

    public PendingUploadTaskList(final GiftCloudProperties giftCloudProperties, final MultiUploadReporter reporter) {
        this.reporter = reporter;
        taskList = new BackgroundBlockingQueueTaskList<PendingUploadTask>();
        pendingUploadFolder = giftCloudProperties.getUploadFolder(reporter);
    }

    public void addFileReference(final String fileReference, final Optional<String> projectName) throws IOException {
        final PendingUploadTaskReference taskReference = new PendingUploadTaskReference(fileReference, projectName);
        taskList.add(taskReference);
        fileMap.put(fileReference, taskReference);
    }

    public void addFileInstance(final String fileInstance, final Optional<String> projectName) throws IOException {
        final PendingUploadTaskInstance taskInstance = new PendingUploadTaskInstance(fileInstance, projectName);
        taskList.add(taskInstance);
        fileMap.put(fileInstance, taskInstance);
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
            try {
                addFileInstance(file.getAbsolutePath(), emptyString);
            } catch (IOException e) {
                // If any files fails then we still try to add the rest
                e.printStackTrace();
            }
        }
    }

    public BackgroundServiceTaskList<PendingUploadTask, PendingUploadTask> getList() {
        return taskList;
    }

    public void notifySuccess(final FileCollection fileCollection) {
        for (final File file : fileCollection.getFiles()) {
            try {
                Optional<PendingUploadTask> task = fileMap.get(file);
                if (task.isPresent()) {
                    PendingUploadTask pendingTask = task.get();
                    if (pendingTask instanceof PendingUploadTaskInstance) {
                        // Delete file
                    } else if (pendingTask instanceof PendingUploadTaskReference) {

                    } else {
                        throw new RuntimeException("Unknown file type"); // probably change this to enum
                    }
                    fileMap.safeRemove(file);
                }
            } catch (IOException e) {
                // ToDo: log error
                e.printStackTrace();
                reporter.silentLogException(e, "The file " + file.getAbsolutePath() + " reported a successful upload, but could not be removed from the pending upload list because the canonical file name could not be determined.");
            }
        }
    }

    public void notifyFailure(final FileCollection fileCollection) {
        // ToDo: add to failure list
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
