/*
 * SessionReviewPanel
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.google.common.base.Joiner;
import org.dcm4che2.data.Tag;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.Series;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.Study;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.util.Utils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public final class SessionReviewPanel extends JPanel {

    public static final int MAX_TAG = Collections.max(new ArrayList<Integer>() {{
        add(Tag.SeriesDescription);
        add(Tag.SeriesNumber);
    }});

	private static final long serialVersionUID = 1L;
	private final Session _session;

	public SessionReviewPanel(final Session session) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		_session = session;

		// for now, only allow selection of series from DICOM
		if (session instanceof Study) {
			add(createScanDetails(session));
		}

		add(createSessionSummary(session));

		setVisible(true);
	}

	private static JPanel createScanDetails(final Session session) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new JLabel("<html><b>Scan Details</b></html>"));
		panel.add(buildScansTable((Study) session));
		panel.add(new JLabel("Note: Unchecked scans will not be uploaded."));

		panel.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));

		return panel;
	}

	private static JPanel createSessionSummary(final Session session) {
		
		Date sessionDate = session.getDateTime();  //this is here for the ECAT case where we need to prompt the user for a timezone.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if(session.getTimeZone() != null){
			sdf.setTimeZone(session.getTimeZone());
		}
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(new JLabel("<html><b>Session Summary</b></html>"));
		panel.add(new JLabel(session.getFormat() + " _session " + session.getID()));
		panel.add(new JLabel("Accession: " + session.getAccession()));
		// panel.add(new JLabel("Date/time: " + _session.getDateTime()));
		panel.add(new JLabel("Description: " + session.getDescription()));
		if(sessionDate != null){
			panel.add(new JLabel("Date: " + sdf.format(sessionDate)));
		} else {
			panel.add(new JLabel("Date: Unknown" ));
		}
		panel.add(buildModalitiesLabel(session));
		panel.add(new JLabel(describeScans(session)));

		return panel;
	}

	private static JLabel buildModalitiesLabel(final Session session) {
		final Set<String> modalities = session.getModalities();
		if (1 == modalities.size()) {
			return new JLabel("Modality: " + modalities.iterator().next());
		} else {
			final Joiner joiner = Joiner.on(',');
			final StringBuilder sb = new StringBuilder("Modalities: ");
			joiner.appendTo(sb, modalities);
			return new JLabel(sb.toString());
		}
	}

	private static JScrollPane buildScansTable(final Study study){
		final ScansTableModel tableModel = new ScansTableModel(study);
		final JTable table = new JTable(tableModel);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter. SortKey>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        table.setRowSorter(sorter);
        table.setIntercellSpacing(new Dimension(10, 0));

        final JScrollPane scrollPane = new JScrollPane(table);
		final int width = table.getPreferredSize().width;
		final int height = table.getRowCount() * table.getRowHeight();
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(1).setPreferredWidth(20);
        table.getColumnModel().getColumn(2).setPreferredWidth(width - 120);
        table.getColumnModel().getColumn(3).setPreferredWidth(40);
        table.getColumnModel().getColumn(4).setPreferredWidth(40);
		table.setPreferredScrollableViewportSize(new Dimension(width, height));
		return scrollPane;
	}

	public Session getSession() { return _session; }

	public static String describeScans(final Session session) {
		final StringBuilder sb = new StringBuilder();
		final int scans = session.getScanCount();
		sb.append(scans).append(1 == scans ? " scan in " : " scans in ");
		final int files = session.getFileCount();
		sb.append(files).append(1 == files ? " file" : " files");
		sb.append(" (");
		Utils.showNearestUnits(sb, session.getSize(), "B");
		sb.append(")");
		return sb.toString();
	}

	static class ScansTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 3558061608731992249L;
		private static final int SELECT_COLUMN = 0;
		private static final String[] COLUMNS = new String[] { "Upload", "#", "Scan Type", "File Count" , "Size (bytes)"};
		private static final Class<?>[] COLUMN_TYPES = new Class[] {Boolean.class, Integer.class, String.class, Integer.class, Long.class};

		public ScansTableModel(final Study study) {
			super(COLUMNS, 0);


			// use a list so the TableModelListener has random-access ability on the series 
			final List<Series> seriesList = new ArrayList<Series>();

            for (final Series series : study.getSeries()) {
                seriesList.add(series);
                String description = series.getSampleObject().getString(Tag.SeriesDescription);
                if (description == null) {
                    description = "<Empty series description>";
                }
                addRow(new Object[] { series.isUploadAllowed(),
                       series.get(Tag.SeriesNumber),
                       description,
                       series.getFileCount(),
                       Utils.showNearestUnits(new StringBuilder(), series.getSize(), "B").toString() });
            }

			addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					final int row = e.getFirstRow();
					seriesList.get(row).setUploadAllowed((Boolean) getValueAt(row, SELECT_COLUMN));
				}
			});
		}

		@Override
		public Class<?> getColumnClass(final int col) {
			return COLUMN_TYPES[col];
		}

		@Override
		public boolean isCellEditable(final int row, final int col) {
			// user can only click the usable checkbox in the first column
			return col == SELECT_COLUMN;
		}
	}

}
