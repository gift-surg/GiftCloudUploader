/*
 * ConfirmSessionDateDialog
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.toedter.calendar.JDateChooser;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

public class ConfirmSessionDateDialog extends JDialog {
    private static final Logger _log = LoggerFactory.getLogger(ConfirmSessionDateDialog.class);

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 140;
	private static final String TITLE = "Confirm Session Date";	// TODO: localize
	private static final String CONFIRM_LABEL = "Confirm";	    // TODO: localize
	private static final String CANCEL_LABEL = "Cancel";        // TODO: localize
	private static final String DATE_LABEL = "Date:";	        // TODO: localize
    private static final String MISMATCHED_DATES_MESSAGE =
            "<html><p>The date you submitted earlier in the upload process</p>" +
                  "<p>doesn't match the date of the selected session. Please</p>" +
                  "<p>re-enter the session date to confirm that this is the</p>" +
                  "<p> session that you want to upload.</p></html>";
    private static final String MATCHED_DATES_MESSAGE =
            "<html><p>The date you've entered matches the date of the</p>" +
                  "<p>selected session. Click <b>" + CONFIRM_LABEL + "</b> to proceed. You can</p>" +
                  "<p>also click <b>" + CANCEL_LABEL + "</b> to return to the upload applet if</p>" +
                  "<p>you believe you've selected the wrong session.</p></html>";

    private final Dimension _size;
    private final Date _date;
    private boolean _confirm;

    public static boolean showDialog(final Component page, final Date sessionDate, final Date confirmDate) {
        return showDialog(page, sessionDate, confirmDate, new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public static boolean showDialog(final Component page, final Date sessionDate, final Date confirmDate, Dimension size) {
        ConfirmSessionDateDialog dialog = new ConfirmSessionDateDialog(page, sessionDate, confirmDate, size);
        dialog.setVisible(true);
        return dialog._confirm;
    }

    private ConfirmSessionDateDialog(final Component page, final Date date, final Date confirmDate, Dimension size) {
		super(UIUtils.findParentFrame(page), TITLE, true);
        _date = date;
        _size = size;
		setLocationRelativeTo(getOwner());
		setContentPane(getContents(confirmDate));
		pack();
        _log.debug("Created dialog to confirm session date of: " + _date);
	}

	private JPanel getContents(Date confirmDate) {
        final JPanel contents = new JPanel(new GridBagLayout()) {{ setPreferredSize(_size); }};
        final JLabel label = makeLabel(DATE_LABEL);
        final JLabel message = makeMessage();

        final JButton confirmButton = new JButton(CONFIRM_LABEL) {{
            setEnabled(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent ev) {
                    if (_confirm) {
                        ConfirmSessionDateDialog.this.dispose();
                    }
                }
            });
        }};
        final JButton cancelButton = new JButton(CANCEL_LABEL) {{
            setEnabled(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent ev) {
                    _confirm = false;
                    ConfirmSessionDateDialog.this.dispose();
                }
            });
        }};

        final JDateChooser dateChooser = new JDateChooser(confirmDate, UIUtils.DEFAULT_DATE_FORMAT, JDateChooserTextFieldDataEditor.getInstance()) {{
            setMinimumSize(getPreferredSize());
            addPropertyChangeListener("date", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    _confirm = DateUtils.isSameDay(_date, getDate());
                    message.setForeground(_confirm ? Color.BLACK : Color.RED);
                    message.setText(_confirm ? MATCHED_DATES_MESSAGE : MISMATCHED_DATES_MESSAGE);
                    confirmButton.setEnabled(_confirm);
                }
            });
        }};

        contents.add(label, makeLabelConstraints(0));
        contents.add(dateChooser, makeValueConstraints(0));
        contents.add(message, makeMessageConstraints(1));
        contents.add(cancelButton, makeButtonConstraints(1, 2));
        contents.add(confirmButton, makeButtonConstraints(2, 2));

		return contents;
	}

	private JLabel makeLabel(final String text) {
		final JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setHorizontalTextPosition(SwingConstants.RIGHT);
		return label;
	}

    private JLabel makeMessage() {
        final JLabel label = new JLabel(MISMATCHED_DATES_MESSAGE);
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setForeground(Color.RED);
        return label;
    }

    private GridBagConstraints makeLabelConstraints(final int row) {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(4, 8, 0, 0);
        return gbc;
    }

	private GridBagConstraints makeValueConstraints(final int row) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.gridy = row;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}

	private GridBagConstraints makeButtonConstraints(final int col, final int row) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = col;
		gbc.gridy = row;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}

	private GridBagConstraints makeMessageConstraints(final int row) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.gridy = row;
        gbc.ipadx = 10;
        gbc.ipady = 10;
		return gbc;
	}

    public static void main(String[] a) {
        Date date = new Date();
        boolean confirm = ConfirmSessionDateDialog.showDialog(new JFrame(), date, DateUtils.addDays(date, -2));
        System.exit(confirm ? 0 : -1);
    }
}
