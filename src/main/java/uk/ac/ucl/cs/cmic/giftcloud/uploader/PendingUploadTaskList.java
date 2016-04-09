package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public void addFiles(final Optional<String> projectName, final FileImportRecord fileImportRecord) throws IOException {
        final List<String> fileNames = fileImportRecord.getFilenames();
        final PendingUploadTask task = fileImportRecord.getDeleteAfterUpload() == PendingUploadTask.DeleteAfterUpload.DELETE_AFTER_UPLOAD ? new PendingUploadTaskInstance(fileNames, projectName) : new PendingUploadTaskReference(fileNames, projectName);
        taskList.addNewTask(task);
        for (final String file : fileNames) {
            fileMap.put(file, task);
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
        PendingUploadTaskReference(final List<String> fileReferences, final Optional<String> projectName) {
            super(fileReferences, projectName, Append.APPEND, DeleteAfterUpload.DO_NOT_DELETE_AFTER_UPLOAD);
        }
    }

    private class PendingUploadTaskInstance extends PendingUploadTask {
        PendingUploadTaskInstance(final List<String> fileInstances, final Optional<String> projectName) {
            super(fileInstances, projectName, Append.APPEND, DeleteAfterUpload.DELETE_AFTER_UPLOAD);
        }
    }
}
