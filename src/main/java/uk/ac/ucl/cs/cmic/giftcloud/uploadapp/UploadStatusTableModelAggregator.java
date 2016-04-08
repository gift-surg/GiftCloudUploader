package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.FileStatusGrouper;
import uk.ac.ucl.cs.cmic.giftcloud.util.ConsecutiveThreadExecutor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static javax.swing.event.TableModelEvent.UPDATE;

/**
 * Aggregates status updates to an {@link UploadStatusTableModel} to prevent the table model from being updated too frequently.
 */
public class UploadStatusTableModelAggregator {

    private final BlockingQueue<Runnable> statusUpdateEvents = new LinkedBlockingQueue<Runnable>();
    private volatile boolean isRunning = false;
    private final ConsecutiveThreadExecutor consecutiveThreadExecutor = new ConsecutiveThreadExecutor();
    private final FileStatusGrouper fileStatusGrouper = new FileStatusGrouper();
    private final int delayBetweenUpdates;
    private final UploadStatusTableModel tableModel = new UploadStatusTableModel(fileStatusGrouper);

    /**
     * Creates a new aggregator that will fire updates with a specified minimum frequency
     * @param delayBetweenUpdates the minimum delay between successive update events
     */
    public UploadStatusTableModelAggregator(final int delayBetweenUpdates) {
        this.delayBetweenUpdates = delayBetweenUpdates;
    }

    /**
     * Add a notification of a group of files added
     * @param groupId the unique identifier for the group to which all the files belong
     * @param description the visible name for the file group
     * @param fileUids a list of unique identifiers
     */
    public void notifyFilesAdded(final String groupId, final String date, final String modality, final String description, final List<String> fileUids) {
        addEvent(new Runnable() {
            @Override
            public void run() {
                fileStatusGrouper.addFiles(groupId, date, modality, description, fileUids);
            }
        });
    }

    /**
     * Add a notification that a file has completed uploading
     *
     * @param groupId the unique identifier for the group to which the file belongs
     * @param fileUid the unique file identifier matching the identifier previously added using {@link #notifyFilesAdded}
     */
    public void notifyFileComplete(final String groupId, final String fileUid) {
        addEvent(new Runnable() {
            @Override
            public void run() {
                fileStatusGrouper.fileDone(groupId, fileUid);
            }
        });
    }

    /**
     * @return a {@link TableModel} view of this status data
     */
    public TableModel getTableModel() {
        return tableModel;
    }


    private final Runnable processStatusUpdateEvents = new Runnable() {
        // EDT runnable for updating the table model
        public void run() {
            Runnable event;

            fileStatusGrouper.resetChanged();

            while ((event = getNextEvent()) != null) {
                event.run();
            }

            // Fire a single event to update listeners, aggregated from all the events
            if (fileStatusGrouper.getAnyGroupsAddedOrRemoved()) {
                tableModel.notifyListeners(new TableModelEvent(tableModel));
            } else {
                final int minRowChanged = fileStatusGrouper.getMinGroupChanged();
                final int maxRowChanged = fileStatusGrouper.getMaxGroupChanged();
                tableModel.notifyListeners(new TableModelEvent(tableModel, minRowChanged, maxRowChanged, TableModelEvent.ALL_COLUMNS, UPDATE));
            }
        }
    };

    private synchronized Runnable getNextEvent() {
        final Runnable event = statusUpdateEvents.poll();

        // If the queue is empty here this means the loop will certainly terminate, so we set the variable within this synchronized block so that a new loop will be started the next time an event is added
        if (event == null) {
            isRunning = false;
        }
        return event;
    }

    /**
     * Adds an event to the queue of events waiting to be processed
     * @param event the GroupStatusUpdateEvent describing the update
     */
    private synchronized void addEvent(final Runnable event) {

        // Add this update to the queue
        statusUpdateEvents.add(event);

        // The isRunning variable means that either the EDT thread is running or is already scheduled to run, in which case we need do nothing - the synchronized block ensures that our new event will definitely be processed by the current thread
        if (!isRunning) {
            // We set isRunning to true to indicate that we are scheduling an EDT thread to be run
            isRunning = true;

            // Instead of running directly, we invoke via a ConsecutiveThreadExecutor. This ensures that any previous thread has completed which allows for a guaranteed delay
            consecutiveThreadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(processStatusUpdateEvents);
                    try {
                        Thread.sleep(delayBetweenUpdates);
                    } catch (InterruptedException e) {
                    }
                }
            });
        }
    }
}
