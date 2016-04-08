package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.FileImportRecord;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PendingUploadTask;

import javax.swing.table.TableModel;
import java.io.File;
import java.util.UUID;
import java.util.Vector;

public class WaitingForUploadDatabaseTest {
    private WaitingForUploadDatabase database;
    private TableModel tableModel;
    private TableListener tableModelListener;

    @Before
    public void setUp() throws Exception {
        database = new WaitingForUploadDatabase(10);
        tableModel = database.getTableModel();
        tableModelListener = new TableListener();
        tableModel.addTableModelListener(tableModelListener);
    }

    @Test
    public void combinedTest() throws Exception {
        FileImportRecord record1 = new MockImportRecord("SERIES1", "NAME1", "MR", "DATE1", 10, false);
        FileImportRecord record2 = new MockImportRecord("SERIES2", "NAME2", "US", "DATE2", 9, false);
        FileImportRecord record3 = new MockImportRecord("SERIES2", "NAME2", "US", "DATE3", 5, false);

        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        database.addFiles(record1);
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        database.addFiles(record2);
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(1, 1);
        database.addFiles(record3);
        tableModelListener.waitForCompletion();

        tableModelListener.clearAndSetExpectations(0, 0);
        database.removeAndDeleteCopies(record1.getFilenames().get(0));
        tableModelListener.waitForCompletion();

        tableModelListener.clearAndSetExpectations(1, 1);
        database.removeAndDeleteCopies(record2.getFilenames().get(2));
        tableModelListener.waitForCompletion();

        tableModelListener.clearAndSetExpectations(0, 1);
        database.removeAndDeleteCopies(record1.getFilenames().get(5));
        database.removeAndDeleteCopies(record2.getFilenames().get(8));
        database.removeAndDeleteCopies(record3.getFilenames().get(0));
        tableModelListener.waitForCompletion();
    }

    @Test
    public void realFileDeleteTest() throws Exception {

        // Test with real file to ensure it is deleted
        File tempFile = File.createTempFile("TestFile", ".dcm");
        Assert.assertTrue(tempFile.exists());
        Vector<String> fileNames = new Vector<String>();
        fileNames.add(tempFile.getCanonicalPath());
        FileImportRecord record1 = new MockImportRecord("SERIES1", "NAME1", "MR", "DATE1", fileNames, true);
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        database.addFiles(record1);
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, 0);
        database.removeAndDeleteCopies(tempFile.getCanonicalPath());
        tableModelListener.waitForCompletion();
        Assert.assertFalse(tempFile.exists());
    }

    @Test
    public void realFileNoDeleteTest() throws Exception {

        // Test with real file to ensure it is deleted
        File tempFile = File.createTempFile("TestFile", ".dcm");
        Assert.assertTrue(tempFile.exists());
        Vector<String> fileNames = new Vector<String>();
        fileNames.add(tempFile.getCanonicalPath());
        FileImportRecord record1 = new MockImportRecord("SERIES1", "NAME1", "MR", "DATE1", fileNames, false);
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        database.addFiles(record1);
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, 0);
        database.removeAndDeleteCopies(tempFile.getCanonicalPath());
        tableModelListener.waitForCompletion();
        Assert.assertTrue(tempFile.exists());
        tempFile.delete();
    }

    private class MockImportRecord extends FileImportRecord {

        private String seriesId;
        private String name;
        private String modality;

        MockImportRecord(String seriesId, String name, String modality, final String date, final int numFiles, boolean deleteAfterUpload) {
            super(new Vector<String>() {{ for (int index = 0; index < numFiles; index++) { add(UUID.randomUUID().toString()); } }}, date, deleteAfterUpload ? PendingUploadTask.DeleteAfterUpload.DELETE_AFTER_UPLOAD : PendingUploadTask.DeleteAfterUpload.DO_NOT_DELETE_AFTER_UPLOAD);
            this.seriesId = seriesId;
            this.name = name;
            this.modality = modality;
        }

        MockImportRecord(String seriesId, String name, String modality, final String date, final Vector<String> realFileNames, boolean deleteAfterUpload) {
            super(realFileNames, date, deleteAfterUpload ? PendingUploadTask.DeleteAfterUpload.DELETE_AFTER_UPLOAD : PendingUploadTask.DeleteAfterUpload.DO_NOT_DELETE_AFTER_UPLOAD);
            this.seriesId = seriesId;
            this.name = name;
            this.modality = modality;
        }
        @Override
        public String getSeriesIdentifier() {
            return seriesId;
        }

        @Override
        public String getVisibleName() {
            return name;
        }

        @Override
        public String getModality() {
            return modality;
        }
    }
}