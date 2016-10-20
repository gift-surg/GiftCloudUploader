/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Some parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

 ============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.ConsecutiveThreadExecutor;

import javax.swing.table.TableModel;
import java.io.File;
import java.util.*;

/**
 * Class to store references to files that are waiting for upload. This class includes the functionality to delete files after upload, if required
 *
 * In order to not delay execution of the calling thread, the database is modified using a sequential execution thread
 */
class WaitingForUploadDatabase extends Observable {

    private final List<DatabaseItem> databaseItems = new ArrayList<DatabaseItem>();
    private final UploadStatusTableModelAggregator tableModelUpdater;
    private final ConsecutiveThreadExecutor consecutiveThreadExecutor = new ConsecutiveThreadExecutor();

    WaitingForUploadDatabase(final int delayBetweenUpdates) {
        tableModelUpdater = new UploadStatusTableModelAggregator(delayBetweenUpdates);
    }

    /**
     * Adds files to the in-memory database and table model
     * @param fileImportRecord
     */
    void addFiles(final FileImportRecord fileImportRecord) {
        consecutiveThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {

                // Update in-memory database
                final String groupId = fileImportRecord.getSeriesIdentifier();
                final List<String> fileUids = new ArrayList<String>();
                for (final String fileName : fileImportRecord.getFilenames()) {
                    final DatabaseItem databaseItem = new DatabaseItem(fileName, groupId, fileImportRecord.getDeleteAfterUpload());
                    databaseItems.add(databaseItem);
                    fileUids.add(databaseItem.getUuid());
                }

                // Update table model
                final String name = fileImportRecord.getVisibleName();
                final String id = fileImportRecord.getPatientId();
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
    void removeAndDeleteCopies(final String fileName) {
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

                if (!foundItems.isEmpty()) {
                    final DatabaseItem itemToRemove = foundItems.get(0);

                    // Remove the first item with a matching filename. Normally there will be only one, but if data
                    // are sent twice we could end up with duplicate references. We only remove one reference because
                    // the duplicate entries will be in the pending queue or might even be in the process of uploading,
                    // so deleting would cause unpredictable behaviour.
                    databaseItems.remove(itemToRemove);

                    // Only delete the file if all references are gone. There will only be multiple references to the
                    // same file if a duplicate file is added before the original has finished uploading. We will allow
                    // the file to be uploaded again by letting the uploading requests be honoured. The alternative
                    // would be to suppress an upload request if the file already exists; but that would prevent the
                    // triggering of an upload again if it had failed the first time. This behaviour might lead to
                    // duplicate unnecessary file uploads, but it will behave in a well-defined way
                    if (foundItems.size() == 1) {
                        if (itemToRemove.deleteAfterUpload == PendingUploadTask.DeleteAfterUpload.DELETE_AFTER_UPLOAD) {
                            final File file = new File(itemToRemove.fileName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }

                    // Update table model
                    tableModelUpdater.notifyFileComplete(itemToRemove.getGroupId(), itemToRemove.getUuid());
                }
            }
        });
    }

    /**
     * @return a TableModel representing the current status of files to upload
     */
    TableModel getTableModel() {
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
