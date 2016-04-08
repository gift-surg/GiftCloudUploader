package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.FileStatusGrouper;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static org.mockito.Mockito.*;

public class UploadStatusTableModelTest {
    private UploadStatusTableModel tableModel;
    private FileStatusGrouper mockGrouper;
    private TableModelListener listener = mock(TableModelListener.class);

    @Before
    public void setUp() throws Exception {
        mockGrouper = mock(FileStatusGrouper.class);
        tableModel = new UploadStatusTableModel(mockGrouper);
        tableModel.addTableModelListener(listener);
        when(mockGrouper.numGroups()).thenReturn(7);
    }

    @Test
    public void notifyListeners() throws Exception {
        TableModelEvent event = mock(TableModelEvent.class);
        tableModel.notifyListeners(event);
        verify(listener).tableChanged(event);
    }

    @Test
    public void getRowCount() throws Exception {
        Assert.assertEquals(tableModel.getRowCount(), 7);
    }

    @Test
    public void getColumnCount() throws Exception {
        Assert.assertEquals(tableModel.getColumnCount(), UploadStatusTableModel.columnTitles.size());
    }

    @Test
    public void getColumnName() throws Exception {
        Assert.assertEquals(tableModel.getColumnName(0), UploadStatusTableModel.columnTitles.get(0));
        Assert.assertEquals(tableModel.getColumnName(1), UploadStatusTableModel.columnTitles.get(1));
        Assert.assertEquals(tableModel.getColumnName(2), UploadStatusTableModel.columnTitles.get(2));
        Assert.assertEquals(tableModel.getColumnName(3), UploadStatusTableModel.columnTitles.get(3));
        Assert.assertEquals(tableModel.getColumnName(4), UploadStatusTableModel.columnTitles.get(4));
    }

    @Test
    public void getColumnClass() throws Exception {
        Assert.assertEquals(tableModel.getColumnClass(5), String.class);
    }

    @Test
    public void isCellEditable() throws Exception {
        Assert.assertFalse(tableModel.isCellEditable(5,5));
    }

    @Test
    public void getValueAt() throws Exception {
        when(mockGrouper.getDate(0)).thenReturn("Date0");
        when(mockGrouper.getDate(1)).thenReturn("Date1");
        when(mockGrouper.getDescription(0)).thenReturn("Description0");
        when(mockGrouper.getDescription(1)).thenReturn("Description1");
        when(mockGrouper.getFileNumbers(0)).thenReturn("(1/1)");
        when(mockGrouper.getFileNumbers(1)).thenReturn("(2/2)");

        Assert.assertEquals(tableModel.getValueAt(0, UploadStatusTableModel.DATE_COLUMN), "Date0");
        Assert.assertEquals(tableModel.getValueAt(1, UploadStatusTableModel.DATE_COLUMN), "Date1");
    }

    @Test
    public void setValueAt() throws Exception {
        // This method is not used
    }

    @Test
    public void addTableModelListener() throws Exception {
        TableModelListener listener2 = mock(TableModelListener.class);
        TableModelEvent event = mock(TableModelEvent.class);
        tableModel.addTableModelListener(listener2);
        tableModel.notifyListeners(event);
        verify(listener2).tableChanged(event);
    }

    @Test
    public void removeTableModelListener() throws Exception {
        TableModelListener listener2 = mock(TableModelListener.class);
        TableModelEvent event = mock(TableModelEvent.class);
        tableModel.addTableModelListener(listener2);
        tableModel.notifyListeners(event);
        verify(listener).tableChanged(event);
        verify(listener2).tableChanged(event);

        TableModelEvent event2 = mock(TableModelEvent.class);
        tableModel.removeTableModelListener(listener);
        tableModel.notifyListeners(event2);
        verify(listener, never()).tableChanged(event2);
        verify(listener2).tableChanged(event);

        TableModelEvent event3 = mock(TableModelEvent.class);
        tableModel.removeTableModelListener(listener2);
        tableModel.notifyListeners(event3);
        verify(listener, never()).tableChanged(event3);
        verify(listener2, never()).tableChanged(event3);
    }
}