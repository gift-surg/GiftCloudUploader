/*
 * NewSubjectDialog
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.google.common.base.Strings;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.Subject;
import uk.ac.ucl.cs.cmic.giftcloud.data.SubjectInformation;
import org.nrg.xnat.Labels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;

public class NewSubjectDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final String TITLE = "Create new subject";	// TODO: localize
	private static final String CREATE_LABEL = "Create";	// TODO: localize
	private static final String CANCEL_LABEL = "Cancel";	// TODO: localize
	private static final String ID_LABEL = "Subject label:";	// TODO: localize
	private static final int VALUE_WIDTH = 24;
	private static final String EMPTY_MESSAGE = " ";
	private static final String EMPTY_LABEL_MESSAGE = "Label cannot be empty.";
	private static final String INVALID_LABEL_MESSAGE = "Label may contain only letters, digits, hyphen (-) and underscore (_).";
	private static final String EXISTING_SUBJECT_MESSAGE = "Subject %s already exists.";

	private final Logger logger = LoggerFactory.getLogger(NewSubjectDialog.class);
	private final SelectSubjectPage page;
	private final RestServerHelper restServerHelper;
	private final Project project;
	private JPanel contents = null;


	public NewSubjectDialog(final SelectSubjectPage page, final RestServerHelper restServerHelper, final Project project) {
		super(UIUtils.findParentFrame(page), TITLE, true);
		setLocationRelativeTo(getOwner());
		this.page = page;
		this.restServerHelper = restServerHelper;
		this.project = project;
		setContentPane(getContents());
		pack();
	}

	private void doCreateSubject(final String label) {
		final Callable<Subject> doCreate = new Callable<Subject>() {
			public Subject call() throws SubjectInformation.UploadSubjectException {
				final SubjectInformation subjectInfo = new SubjectInformation(restServerHelper, project);
				subjectInfo.setLabel(label);
				try {
					final Subject subject = subjectInfo.uploadTo();
					project.addSubject(subject);
					page.refreshSubjectList(subject);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() { NewSubjectDialog.this.dispose(); }
					});
					return subject;
				} catch (SubjectInformation.UploadSubjectException e) {
					logger.error("Unable to create subject " + label, e);
					JOptionPane.showMessageDialog(NewSubjectDialog.this,
							"There was a problem uploading the new subject " + label
							+ ": " + e.getMessage(),
							"Subject creation failed",
							JOptionPane.ERROR_MESSAGE);
					throw e;
				}
			}
		};
		project.submit(doCreate);
	}

	private final JPanel getContents() {
		if (null == contents) {
			contents = new JPanel(new GridBagLayout());

			final JLabel idLabel = makeLabel(ID_LABEL);
			final JLabel idMessage = makeMessage();
			final JTextField idTF = makeTextField();

			final JButton cancelButton = new JButton(CANCEL_LABEL);
			final JButton createButton = new JButton(CREATE_LABEL);

			idMessage.setForeground(Color.RED);

			idTF.getDocument().addDocumentListener(new DocumentListener() {
				private void handle(final DocumentEvent ev) {
					final String text = idTF.getText();
					if (Strings.isNullOrEmpty(text)) {
						idMessage.setText(EMPTY_LABEL_MESSAGE);
						createButton.setEnabled(false);
					} else if (!Labels.isValidLabel(text)) {
						idMessage.setText(INVALID_LABEL_MESSAGE);
						createButton.setEnabled(false);
					} else {
						try {
							if (project.hasSubject(text)) {
								idMessage.setText(String.format(EXISTING_SUBJECT_MESSAGE, text));
								createButton.setEnabled(false);
							} else {
								idMessage.setText(EMPTY_MESSAGE);
								createButton.setEnabled(true);
							}
						} catch (Throwable e) {
							logger.error("unable to check for subject " + text + " in " + project, e);
							idMessage.setText(EMPTY_MESSAGE);
							createButton.setEnabled(true);
						}
					}
				}

				public void removeUpdate(DocumentEvent e) { handle(e); }
				public void insertUpdate(DocumentEvent e) { handle(e); }
				public void changedUpdate(DocumentEvent e) { handle(e); }
			});

			// Start with empty label text, so create button must be disabled
			idMessage.setText(EMPTY_LABEL_MESSAGE);
			createButton.setEnabled(false);

			cancelButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent ev) {
					NewSubjectDialog.this.dispose();
				}
			});

			createButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent ev) {
                    String label = idTF.getText();
                    if (Labels.isValidLabel(label)) {
                        createButton.setEnabled(false);
                        cancelButton.setEnabled(false);
                        doCreateSubject(label);
                    } else {
                        // This should actually already be true, but some impls, e.g. Mac OS X, allow you to click button anyways.
                        idMessage.setText(INVALID_LABEL_MESSAGE);
                        createButton.setEnabled(false);
                    }
				}
			});

			contents.add(idLabel, makeLabelConstraints(0));
			contents.add(idTF, makeValueConstraints(0));
			contents.add(idMessage, makeMessageConstraints(1));
			contents.add(cancelButton, makeButtonConstraints(1, 2));
			contents.add(createButton, makeButtonConstraints(2, 2));
		}
		return contents;
	}

	private final JLabel makeLabel(final String text) {
		final JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setHorizontalTextPosition(SwingConstants.RIGHT);
		return label;
	}

	private final GridBagConstraints makeLabelConstraints(final int row) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.insets = new Insets(4, 8, 0, 0);
		return gbc;
	}

	private final JTextField makeTextField() {
        JTextField text = new JTextField(VALUE_WIDTH);
        text.setMinimumSize(text.getPreferredSize());
        return text;
	}

	private final GridBagConstraints makeValueConstraints(final int row) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.gridy = row;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}

	private final GridBagConstraints makeButtonConstraints(final int col, final int row) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = col;
		gbc.gridy = row;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}

	private final JLabel makeMessage() {
		final JLabel label = new JLabel(" ");
		label.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		return label;
	}

	private final GridBagConstraints makeMessageConstraints(final int row) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.gridy = row;
		return gbc;
	}
}
