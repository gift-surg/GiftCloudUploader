/*
 * SelectSubjectPage
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.json.JSONException;
import org.netbeans.spi.wizard.WizardPage;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class SelectSubjectPage extends WizardPage {
    private static final long serialVersionUID = 1L;

    public static final String PRODUCT_NAME = "subject";
    private static final String STEP_DESCRIPTION = "Select subject";
    private static final String LONG_DESCRIPTION = "Select the subject for the session to be uploaded";

    private final Logger logger = LoggerFactory.getLogger(SelectSubjectPage.class);
    private final JList list;
    private final DefaultListModel listModel = new DefaultListModel();
    private final Dimension dimension;
    private UploadSelector uploadSelector;
    private MultiUploadReporter reporter;
    private final RestServerHelper restServerHelper;

    public static String getDescription() {
        return STEP_DESCRIPTION;
    }

    public SelectSubjectPage(final RestServerHelper restServerHelper, final Dimension dimension, final UploadSelector uploadSelector, final MultiUploadReporter reporter) {
        this.restServerHelper = restServerHelper;
        this.dimension = dimension;
        this.uploadSelector = uploadSelector;
        this.reporter = reporter;
        list = new JList(listModel);
        list.setName(PRODUCT_NAME);
        setLongDescription(LONG_DESCRIPTION);
    }

    /*
      * (non-Javadoc)
      * @see org.netbeans.spi.wizard.WizardPage#recycle()
      */
    protected void recycle() {
        removeAll();
        refreshSubjectList();
        validate();
    }

    /*
      * (non-Javadoc)
      * @see org.netbeans.spi.wizard.WizardPage#renderingPage()
      */
    protected void renderingPage() {
        setBusy(true);
        final JScrollPane subject_list = new JScrollPane(list);
        if (null != dimension) {
            subject_list.setPreferredSize(dimension);
        }
        add(subject_list);
        if (uploadSelector.isProjectSet()) {
            refreshSubjectList();
            final JButton newSubject = new JButton("Create new subject");

            final JDialog newSubjectDialog = new NewSubjectDialog(this, restServerHelper, uploadSelector.getProject());
            newSubject.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newSubjectDialog.setVisible(true);
                }
            });
            newSubject.setEnabled(true);
            add(newSubject);
        }
        setBusy(false);
    }

    /*
      * (non-Javadoc)
      * @see org.netbeans.spi.wizard.WizardPage#validateContents(java.awt.Component, java.lang.Object)
      */
    protected String validateContents(final Component component, final Object o) {
        return uploadSelector.isSubjectSet() ? "" : null;
    }

    /**
     * Refreshes the list of subjects in the current project.
     *
     * @param selection item in the subjects list to be selected after refresh
     */
    void refreshSubjectList(final Object selection) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final Project project = uploadSelector.getProject();
        uploadSelector.clearSubject();
        final Callable<Object> doRefresh = new Callable<Object>() {
            public Object call() throws IOException, JSONException {

                final Collection<Subject> subjectList = getListOfSubjects(project);

                listModel.removeAllElements();
                for (final Subject subject : subjectList) {
                    listModel.addElement(subject);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        selectSubject(selection);
                    }
                });
                return selection;
            }
        };
        project.submit(doRefresh);
        setCursor(Cursor.getDefaultCursor());
    }



    private final Collection<Subject> getListOfSubjects(final Project project) throws IOException {
        for (; ; ) {
            try {
                return project.getSubjects();
            } catch (InterruptedException retry) {
                logger.info("subject retrieval interrupted, retrying", retry);
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    if (reporter.askRetry(SelectSubjectPage.this, "Network error", "Unable to contact server")) {
                        logger.error("error getting subject list; retrying at user request", cause);
                    } else {
                        throw new IOException(cause);
                    }

                } else if (cause instanceof JSONException) {
                    if (reporter.askRetry(SelectSubjectPage.this, "Server error", "Received invalid response from server")) {
                        logger.error("error getting subject list; retrying at user request", cause);
                    } else {
                        throw new IOException(cause); // Shouldn't this be JSONException?
                    }

                } else {
                    logger.error("error getting subject list for " + project, cause);
                    logger.info("will retry in 1000 ms");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                        JOptionPane.showMessageDialog(null, "Got an exception: " + ignore.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
                    }
                }
            }
        }
    }

    /**
     * Refreshes the list of subjects in the current project, re-selecting
     * the current selection after refresh is complete.
     */
    void refreshSubjectList() {
        refreshSubjectList(uploadSelector.getSubject());
    }

    private void selectSubject(Object selection) {
        if (listModel.contains(selection)) {
            list.setSelectedValue(selection, true);
        }
    }
}
