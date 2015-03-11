/*
 * SelectSessionPage
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.google.common.collect.Lists;
import org.json.JSONException;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.MasterTrawler;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class SelectSessionPage extends WizardPage implements SelectSessionAction.SessionSelectionListener {
    private static final long serialVersionUID = 1L;

    public static final String PRODUCT_NAME = "*session*";
    private static final String STEP_DESCRIPTION = "Verify selected session";
    private static final String LONG_DESCRIPTION = "Review and verify selected session information";

    private SessionReviewPanel _current;
    private RestServerHelper restServerHelper;
    private final FileSelector fileSelector;
    private final UploadSelector uploadSelector;

    private SeriesImportFilterApplicatorRetriever _filter = null;

    public static String getDescription() {
        return STEP_DESCRIPTION;
    }

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private Future<List<Session>> sessionLister = null;

    public SelectSessionPage(final RestServerHelper restServerHelper, final Optional<String> projectName, final FileSelector fileSelector, final UploadSelector uploadSelector) throws IOException, JSONException {
        setLayout(new BorderLayout());
        setLongDescription(LONG_DESCRIPTION);
        this.restServerHelper = restServerHelper;
        this.fileSelector = fileSelector;
        this.uploadSelector = uploadSelector;
        if (projectName.isPresent()) {
            _filter = new SeriesImportFilterApplicatorRetriever(restServerHelper, projectName);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#allowBack(java.lang.String, java.util.Map, org.netbeans.spi.wizard.Wizard)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public WizardPanelNavResult allowBack(final String stepName, final Map settings, final Wizard wizard) {
        if (null != sessionLister) {
            sessionLister.cancel(true);
            sessionLister = null;
        }
        removeAll();
        return super.allowBack(stepName, settings, wizard);
    }

    /**
     * Controls whether the user should be allowed to click the Next button. The primary condition for this is that the
     * user selected a specific session from a multi-session list or selected a folder containing only a single session.
     * If the user selected a folder containing no sessions or hasn't selected a session from a multi-session list, the
     * user is not allowed to proceed.
     *
     * @param stepName The name of the current wizard step.
     * @param settings Any settings for the wizard.
     * @param wizard   The wizard reference object.
     * @return {@link WizardPanelNavResult#PROCEED} if the user has selected a valid session
     */
    @Override
    public WizardPanelNavResult allowNext(String stepName, Map settings, Wizard wizard) {
        return _current == null ? WizardPanelNavResult.REMAIN_ON_PAGE : WizardPanelNavResult.PROCEED;
    }

    /*
     * (non-Javadoc)
     * @see SelectSessionAction.SessionSelectionListener#setSelectedSession(Session)
     */
    @Override
    public void setSelectedSession(final Session session) {
        setProblem(null);
        uploadSelector.setSession(session);
    }

    private class SessionLister implements Callable<List<Session>> {
        private final Logger logger = LoggerFactory.getLogger(SessionLister.class);
        private final Container container;
        private final Collection<File> files;

        SessionLister(final Container container, final Collection<File> files) {
            this.container = container;
            this.files = Lists.newArrayList(files);
            logger.trace("initializing SessionLister from {}", files);
        }

        SessionLister(final Container container, final File[] files) {
            this(container, Arrays.asList(files));
        }

        @Override
        public List<Session> call() {
            if (_filter == null) {
                try {
                    if (!uploadSelector.isProjectSet()) {
                        final Optional<String> emptyProject = Optional.empty();
                        _filter = new SeriesImportFilterApplicatorRetriever(restServerHelper, emptyProject);
                    } else {
                        _filter = new SeriesImportFilterApplicatorRetriever(restServerHelper, Optional.of(uploadSelector.getProject().toString()));
                    }
                } catch (Exception exception) {
                    throw new RuntimeException("Error encountered retrieving series import filters", exception);
                }
            }

            setBusy(true);
            final SwingProgressMonitor progress = new SwingProgressMonitor(container, "Finding data files", "searching", 0, files.size());
            final List<Session> sessions = new MasterTrawler(progress, files, _filter).call();
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        final Component component;
                        final String instructions;
                        switch (sessions.size()) {
                            case 0:
                                instructions = null;
                                component = null;
                                break;
                            case 1:
                                instructions = "Please confirm the scans to be included:";
                                final Session single = sessions.iterator().next();
                                component = _current = new SessionReviewPanel(single);
                                putWizardData(PRODUCT_NAME, single);
                                break;
                            default:
                                instructions = "Multiple sessions: Select one and confirm the scans to be included:";
                                final JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                                final List<SessionReviewPanel> panels = new ArrayList<SessionReviewPanel>(sessions.size());
                                final JComboBox<String> comboBox = new JComboBox<String>();
                                comboBox.addItem("(Select a session to upload...)");
                                _current = null;
                                for (Session session : sessions) {
                                    comboBox.addItem(session.toString());
                                    panels.add(new SessionReviewPanel(session));
                                }
                                comboBox.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent event) {
                                        int index;
                                        if (_current == null) {
                                            index = comboBox.getSelectedIndex() - 1;
                                            comboBox.removeItemAt(0);
                                        } else {
                                            index = comboBox.getSelectedIndex();
                                        }
                                        _current = panels.get(index);
                                        main.setBottomComponent(new JScrollPane(_current));
                                        SelectSessionPage.this.setSelectedSession(_current.getSession());
                                    }
                                });

                                try {
                                    main.setTopComponent(comboBox);
                                    main.setBottomComponent(new JScrollPane(new JLabel() {{
                                        setFont(getFont().deriveFont(Font.ITALIC));
                                        setHorizontalAlignment(JLabel.LEFT);
                                        setVerticalAlignment(JLabel.TOP);
                                        setText("Select a session to see the details of the session and included scans.");
                                    }}));
                                } catch (Exception exception) {
                                    UIUtils.handleAppletError(SelectSessionPage.this, exception);
                                }
                                component = main;
                                setProblem("Select a session");
                        }

                        if (null == component) {
                            setProblem("No sessions available: select a different directory.");
                        } else {
                            container.add(new JLabel(instructions), BorderLayout.PAGE_START);
                            container.add(component, BorderLayout.CENTER);
                            container.validate();
                        }
                        setBusy(false);
                    }
                });
            } catch (InterruptedException exception) {
                UIUtils.handleAppletError(SelectSessionPage.this, exception);
            } catch (InvocationTargetException exception) {
                UIUtils.handleAppletError(SelectSessionPage.this, exception);
            }
            setBusy(false);
            return sessions;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#recycle()
     */
    @Override
    protected void recycle() {
        removeAll();
        sessionLister = null;
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#renderingPage()
     */
    @Override
    protected void renderingPage() {
        if (null == sessionLister) {
            final File[] files, selected = fileSelector.getSelectedFiles();
            if (0 == selected.length) {
                files = new File[]{fileSelector.getCurrentDirectory()};
            } else {
                files = selected;
            }
            sessionLister = executor.submit(new SessionLister(this, files));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#validateContents(java.awt.Component, java.lang.Object)
     */
    @Override
    protected String validateContents(final Component component, final Object event) {
        if (!uploadSelector.isSessionSet()) {
            return null != sessionLister && sessionLister.isDone() ? "Select a session" : "Searching for sessions...";
        } else {
            if (uploadSelector.getSession().getFileCount() > 0) {
                return null;
            } else {
                return "Directory contains no DICOM files; try another directory.";
            }
        }
    }
}
