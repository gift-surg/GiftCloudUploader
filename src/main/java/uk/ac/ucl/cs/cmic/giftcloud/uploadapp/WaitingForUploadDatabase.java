package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.FileImportRecord;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PendingUploadTask;
import uk.ac.ucl.cs.cmic.giftcloud.util.ConsecutiveThreadExecutor;

import javax.swing.table.TableModel;
import java.io.File;
import java.util.*;

/**
 * Class to store references to files that are waiting for upload. This class includes the functionality to delete files after upload, if required
 *
 * In order to not delay execution of the calling thread, the database is modified using a sequential execution thread
 */
public class WaitingForUploadDatabase extends Observable {

    private final List<DatabaseItem> databaseItems = new ArrayList<DatabaseItem>();
    private final Map<String, DatabaseItem> fileNameToDatabaseItemMap = new HashMap<String, DatabaseItem>();
    private final UploadStatusTableModelAggregator tableModelUpdater;
    private final ConsecutiveThreadExecutor consecutiveThreadExecutor = new ConsecutiveThreadExecutor();

    public WaitingForUploadDatabase(final int delayBetweenUpdates) {
        tableModelUpdater = new UploadStatusTableModelAggregator(delayBetweenUpdates);
    }

    /**
     * Adds files to the in-memory database and table model
     * @param fileImportRecord
     */
    public void addFiles(final FileImportRecord fileImportRecord) {
        consecutiveThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {

                // Update in-memory database
                final String groupId = fileImportRecord.getSeriesIdentifier();
                final List<String> fileUids = new ArrayList<String>();
                for (final String fileName : fileImportRecord.getFilenames()) {
                    final DatabaseItem databaseItem = new DatabaseItem(fileName, groupId, fileImportRecord.getDeleteAfterUpload());
                    databaseItems.add(databaseItem);
                    fileNameToDatabaseItemMap.put(fileName, databaseItem);
                    fileUids.add(databaseItem.getUuid());
                }

                // Update table model
                final String name = fileImportRecord.getVisibleName();
                final String modality = fileImportRecord.getModality();
                final String date = fileImportRecord.getDate();
                tableModelUpdater.notifyFilesAdded(groupId, date, modality, name, fileUids);
            }
        });
    }

    /**
     * Removes files from the in-memory database, update the table model, and delete files if necessary
     * @param fileName
     */
    public void removeAndDeleteCopies(final String fileName) {
        consecutiveThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                // Update in-memory database and delete files if necessary
                final List<DatabaseItem> foundItems = new ArrayList<DatabaseItem>();
                for (final DatabaseItem item : databaseItems) {
                    if (item.fileName.equals(fileName)) {
                        foundItems.add(item);
                    }
                }
                for (final DatabaseItem item : foundItems) {
                    databaseItems.remove(item);
                    if (item.deleteAfterUpload == PendingUploadTask.DeleteAfterUpload.DELETE_AFTER_UPLOAD) {
                        final File file = new File(item.fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }

                // Update table model
                if (fileNameToDatabaseItemMap.containsKey(fileName)) {
                    final DatabaseItem item = fileNameToDatabaseItemMap.get(fileName);
                    tableModelUpdater.notifyFileComplete(item.getGroupId(), item.getUuid());
                }
            }
        });
    }

    /**
     * @return a TableModel representing the current status of files to upload
     */
    public TableModel getTableModel() {
        return tableModelUpdater.getTableModel();
    }

    private class DatabaseItem {
        private final String fileName;
        private String groupId;
        private final PendingUploadTask.DeleteAfterUpload deleteAfterUpload;
        private final String uuid = UUID.randomUUID().toString();

        DatabaseItem(final String fileName, String groupId, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload) {
            this.fileName = fileName;
            this.groupId = groupId;
            this.deleteAfterUpload = deleteAfterUpload;
        }

        public String getUuid() {
            return uuid;
        }

        public String getGroupId() {
            return groupId;
        }
    }
}
