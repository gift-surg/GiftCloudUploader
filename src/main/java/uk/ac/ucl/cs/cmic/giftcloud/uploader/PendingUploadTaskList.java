package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Maintains lists of files that are waiting to be uploaded
 */
public class PendingUploadTaskList {
    private final BackgroundBlockingQueueTaskList<PendingUploadTask> taskList;
    private final UniqueFileMap<PendingUploadTask> fileMap = new UniqueFileMap<PendingUploadTask>();
    private GiftCloudReporter reporter;
    private final List<FileCollection> failures = new ArrayList<FileCollection>();

    public PendingUploadTaskList(final GiftCloudReporter reporter) {
        this.reporter = reporter;
        taskList = new BackgroundBlockingQueueTaskList<PendingUploadTask>();
    }

    public void addFileReference(final Vector<String> fileReferences, final Optional<String> projectName) throws IOException {
        final PendingUploadTaskReference taskReference = new PendingUploadTaskReference(fileReferences, projectName);
        taskList.addNewTask(taskReference);
        for (final String fileReference : fileReferences) {
            fileMap.put(fileReference, taskReference);
        }
    }

    public void addFileInstance(final Vector<String> fileInstances, final Optional<String> projectName) throws IOException {
        final PendingUploadTaskInstance taskInstance = new PendingUploadTaskInstance(fileInstances, projectName);
        taskList.addNewTask(taskInstance);
        for (final String fileInstance : fileInstances) {
            fileMap.put(fileInstance, taskInstance);
        }
    }

    public BackgroundServiceTaskList<PendingUploadTask, PendingUploadTask> getList() {
        return taskList;
    }

    /**
     * Called after a file or set of files have been successfully uploaded to the server. Files stored in the local
     * "waiting for upload" folder will be deleted.
     * @param file
     */
    public void processFileAfterUpload(final File file) {
        try {
            final Optional<PendingUploadTask> task = fileMap.get(file);
            if (task.isPresent()) {
                fileMap.safeRemove(file);
            }
        } catch (IOException e) {
            reporter.silentLogException(e, "The file " + file.getAbsolutePath() + " reported a successful upload, but could not be removed from the pending upload list because the canonical file name could not be determined. Error:" + e.getLocalizedMessage());
        }
    }

    public void fileUploadSuccess(FileCollection fileCollection) {
        for (final File file : fileCollection.getFiles()) {
            processFileAfterUpload(file);
        }
    }

    public void fileUploadFailure(FileCollection fileCollection) {
        failures.add(fileCollection);
    }

    private class PendingUploadTaskReference extends PendingUploadTask {
        PendingUploadTaskReference(final Vector<String> fileReferences, final Optional<String> projectName) {
            super(fileReferences, projectName, Append.APPEND, DeleteAfterUpload.DO_NOT_DELETE_AFTER_UPLOAD);
        }
    }

    private class PendingUploadTaskInstance extends PendingUploadTask {
        PendingUploadTaskInstance(final Vector<String> fileInstances, final Optional<String> projectName) {
            super(fileInstances, projectName, Append.APPEND, DeleteAfterUpload.DELETE_AFTER_UPLOAD);
        }
    }
}
