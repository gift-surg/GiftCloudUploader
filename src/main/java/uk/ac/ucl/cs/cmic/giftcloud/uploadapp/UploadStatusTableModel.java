package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.FileStatusGrouper;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UploadStatusTableModel implements TableModel {

    private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private final FileStatusGrouper fileStatusGrouper;

    public static final List<String> columnTitles = Arrays.asList("Date", "Modality", "Name", "Files", "Status");
    public static final int DATE_COLUMN = 0;
    public static final int MODALITY_COLUMN = 1;
    public static final int DESCRIPTION_COLUMN = 2;
    public static final int NUM_FILES_COLUMN = 3;
    public static final int STATUS_COLUMN = 4;

    public UploadStatusTableModel(final FileStatusGrouper fileStatusGrouper) {
        this.fileStatusGrouper = fileStatusGrouper;
    }

    public void notifyListeners(final TableModelEvent tableModelEvent) {
        for (final TableModelListener listener : listeners) {
            listener.tableChanged(tableModelEvent);
        }
    }

    @Override
    public int getRowCount() {
        return fileStatusGrouper.numGroups();
    }

    @Override
    public int getColumnCount() {
        return columnTitles.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnTitles.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case UploadStatusTableModel.DATE_COLUMN:
                return fileStatusGrouper.getDate(rowIndex);
            case UploadStatusTableModel.MODALITY_COLUMN:
                return fileStatusGrouper.getModality(rowIndex);
            case UploadStatusTableModel.DESCRIPTION_COLUMN:
                return fileStatusGrouper.getDescription(rowIndex);
            case UploadStatusTableModel.NUM_FILES_COLUMN:
                return fileStatusGrouper.getFileNumbers(rowIndex);
            case UploadStatusTableModel.STATUS_COLUMN:
                return fileStatusGrouper.getStatus(rowIndex);
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
}
