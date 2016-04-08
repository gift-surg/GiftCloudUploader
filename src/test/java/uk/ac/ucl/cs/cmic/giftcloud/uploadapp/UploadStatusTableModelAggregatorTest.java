package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import junit.framework.Assert;
import org.junit.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

public class UploadStatusTableModelAggregatorTest {
    @Test
    public void notifyFilesAdded() throws Exception {
        int delay = 10;
        UploadStatusTableModelAggregator aggregator = new UploadStatusTableModelAggregator(delay);
        TableModel tableModel = aggregator.getTableModel();
        TableListener tableModelListener = new TableListener();
        tableModel.addTableModelListener(tableModelListener);

        // Adding a new group will trigger update for all rows
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        addFiles(aggregator, "G1", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/2)");

        // Adding to same group will trigger update for just that group
        tableModelListener.clearAndSetExpectations(0, 0);
        addFiles(aggregator, "G1", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/4)");

        // Adding a new group will trigger update for all rows
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        addFiles(aggregator, "G2", 2);
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        addFiles(aggregator, "G3", 2);
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        addFiles(aggregator, "G4", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(1, 1), "(0/2)");
        Assert.assertEquals(tableModel.getValueAt(2, 1), "(0/2)");
        Assert.assertEquals(tableModel.getValueAt(3, 1), "(0/2)");

        // Adding to same group will trigger update for just that group
        tableModelListener.clearAndSetExpectations(1, 2);
        addFiles(aggregator, "G2", 2);
        addFiles(aggregator, "G3", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(1, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(2, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(3, 1), "(0/2)");

        tableModelListener.clearAndSetExpectations(0, 0);
        addFiles(aggregator, "G1", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/6)");
        Assert.assertEquals(tableModel.getValueAt(1, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(2, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(3, 1), "(0/2)");

        tableModelListener.clearAndSetExpectations(3, 3);
        addFiles(aggregator, "G4", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/6)");
        Assert.assertEquals(tableModel.getValueAt(1, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(2, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(3, 1), "(0/4)");

        tableModelListener.clearAndSetExpectations(0, 3);
        addFiles(aggregator, "G1", 2);
        addFiles(aggregator, "G4", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/8)");
        Assert.assertEquals(tableModel.getValueAt(1, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(2, 1), "(0/4)");
        Assert.assertEquals(tableModel.getValueAt(3, 1), "(0/6)");
    }


    @Test
    public void addingDuplicateFiles() throws Exception {

        // Test what happens if we add files that are already on the queue - we expect them to be uploaded twice, although the important thing we are testing is that uploading the same file twice does not cause an error
        int delay = 10;
        UploadStatusTableModelAggregator aggregator = new UploadStatusTableModelAggregator(delay);
        TableModel tableModel = aggregator.getTableModel();
        TableListener tableModelListener = new TableListener();
        tableModel.addTableModelListener(tableModelListener);

        // Adding a new group will trigger update for all rows
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        aggregator.notifyFilesAdded("G1", "descr1", new ArrayList<String>() {{ add("FILE1"); add("FILE2");}});
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, 0);
        aggregator.notifyFilesAdded("G1", "descr1", new ArrayList<String>() {{ add("FILE1"); add("FILE2");}});
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, 0);
        addFiles(aggregator, "G1", 2);
        tableModelListener.waitForCompletion();

        // Adding to same group will trigger update for just that group
        tableModelListener.clearAndSetExpectations(0, 0);
        aggregator.notifyFilesAdded("G1", "descr1", new ArrayList<String>() {{ add("FILE1"); add("FILE2");}});
        aggregator.notifyFilesAdded("G1", "descr1", new ArrayList<String>() {{ add("FILE1"); add("FILE2");}});
        tableModelListener.waitForCompletion();
    }

    @Test
    public void notifyFileComplete() throws Exception {
        int delay = 10;
        UploadStatusTableModelAggregator aggregator = new UploadStatusTableModelAggregator(delay);
        TableModel tableModel = aggregator.getTableModel();
        TableListener tableModelListener = new TableListener();
        tableModel.addTableModelListener(tableModelListener);

        // Adding a new group will trigger update for all rows
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        aggregator.notifyFilesAdded("G1", "D1" , new ArrayList<String>() {{ add("F11"); add("F12"); }} );
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        aggregator.notifyFilesAdded("G2", "D2" , new ArrayList<String>() {{ add("F21"); add("F22"); }} );
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        aggregator.notifyFilesAdded("G3", "D3" , new ArrayList<String>() {{ add("F31"); add("F32"); }} );
        tableModelListener.waitForCompletion();
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        aggregator.notifyFilesAdded("G4", "D4" , new ArrayList<String>() {{ add("F41"); add("F42"); }} );
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/2)");
        Assert.assertEquals(tableModel.getValueAt(1, 1), "(0/2)");
        Assert.assertEquals(tableModel.getValueAt(2, 1), "(0/2)");
        Assert.assertEquals(tableModel.getValueAt(3, 1), "(0/2)");

        tableModelListener.clearAndSetExpectations(0, 3);
        aggregator.notifyFileComplete("G1", "F12");
        aggregator.notifyFileComplete("G3", "F32");
        aggregator.notifyFileComplete("G3", "F31");
        aggregator.notifyFileComplete("G4", "F41");
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(1/2)");
        Assert.assertEquals(tableModel.getValueAt(1, 1), "(0/2)");
        Assert.assertEquals(tableModel.getValueAt(2, 1), "(2/2)");
        Assert.assertEquals(tableModel.getValueAt(3, 1), "(1/2)");
    }

    @Test
    public void getTableModel() throws Exception {
        // Test that we get a valid TableModel that is updated

        int delay = 10;
        UploadStatusTableModelAggregator aggregator = new UploadStatusTableModelAggregator(delay);
        TableModel tableModel = aggregator.getTableModel();
        TableListener tableModelListener = new TableListener();
        tableModel.addTableModelListener(tableModelListener);

        // Adding a new group will trigger update for all rows
        tableModelListener.clearAndSetExpectations(0, Integer.MAX_VALUE);
        addFiles(aggregator, "G1", 2);
        tableModelListener.waitForCompletion();
        Assert.assertEquals(tableModel.getValueAt(0, 1), "(0/2)");
    }

    private static void addFiles(final UploadStatusTableModelAggregator aggregator, String groupName, final int numFilesToAdd) {
        aggregator.notifyFilesAdded(groupName, "Descrip:" + groupName, new ArrayList<String>() {{ for (int index = 0; index < numFilesToAdd; index++) { add(UUID.randomUUID().toString());}}});
    }

    private class TableListener implements TableModelListener {
        private int expectedFirstRowChanged = -1;
        private int expectedLastRowChanged = -1;
        private int firstRowChanged = -1;
        private int lastRowChanged = -1;
        private int columns = -1;
        private boolean expectationHit = false;
        private CountDownLatch endLatch;

        @Override
        public void tableChanged(TableModelEvent e) {
            if (firstRowChanged == -1) {
                firstRowChanged = e.getFirstRow();
            } else {
                firstRowChanged = min(firstRowChanged, e.getFirstRow());
            }
            if (lastRowChanged == -1) {
                lastRowChanged = e.getLastRow();
            } else {
                lastRowChanged = min(lastRowChanged, e.getLastRow());
            }
            columns = e.getColumn();
            if (!expectationHit) {
                if (firstRowChanged == expectedFirstRowChanged && lastRowChanged == expectedLastRowChanged) {
                    endLatch.countDown();
                    expectationHit = true;
                }
            }
        }

        public void clear() {
            firstRowChanged = -1;
            lastRowChanged = -1;
            columns = -1;
        }

        public void clearAndSetExpectations(int expectedFirstRowChanged, int expectedLastRowChanged) {
            clear();
            expectationHit = false;
            endLatch = new CountDownLatch(1);
            this.expectedFirstRowChanged = expectedFirstRowChanged;
            this.expectedLastRowChanged = expectedLastRowChanged;
        }

        public void waitForCompletion() {
            try {
                // If it takes longer than 1 second then we assume the thread expectations are not fulfilled, so this means the test has failed
                boolean ok = endLatch.await(1000, TimeUnit.MILLISECONDS);
                Assert.assertTrue(ok);
            } catch (InterruptedException e) {
                Assert.fail();
            }
        }
    }
}