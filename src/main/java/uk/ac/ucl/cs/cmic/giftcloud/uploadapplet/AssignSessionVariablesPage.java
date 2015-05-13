/*
 * AssignSessionVariablesPage
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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.nrg.dcm.SOPModel;
import org.nrg.dcm.edit.MultipleInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.data.*;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.DicomSessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.SessionVariableValue;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.PETTracerRetriever;
import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;
import uk.ac.ucl.cs.cmic.giftcloud.util.PrearchiveCode;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class AssignSessionVariablesPage extends WizardPage implements SessionVariableConsumer, PropertyChangeListener {
    private static final long serialVersionUID = -2786166512597463435L;

    public static final String PRODUCT_NAME = "*session-variables*";

    private static final String STEP_DESCRIPTION = "Enter session details";
    private static final String LONG_DESCRIPTION = "Review session information and enter session details if applicable";
    private static final String DUP_SESSION_ID_TITLE = "Session ID Already Exists";
    private static final String DUP_SESSION_ID_MESSAGE_WITH_OVERWRITE =
            "<html><p>The session ID you've specified is already present in your project.</p>" +
                    "<p>Would you like to overwrite the existing session, append to the existing</p>" +
                    "<p>session, or provide a new session ID?</p></html>";
    private static final String[] DUP_SESSION_ID_OPTIONS_WITH_OVERWRITE = {"Overwrite existing", "Append to existing", "Provide new session ID"};
    private static final String DUP_SESSION_ID_MESSAGE =
            "<html><p>The session ID you've specified is already present in your project.</p>" +
                    "<p>Would you like to append to the existing session, or provide a</p>" +
                    "new session ID?</p></html>";
    private static final String[] DUP_SESSION_ID_OPTIONS = {"Append to existing", "Provide new session ID"};
    private static final String DUP_SESSION_ID_WO_APPEND_MESSAGE =
            "<html><p>The session ID you've specified is already present in your project.</p>" +
                    "<p>This project does not allow merging into existing sessions. You can</p>" +
                    "specify a new session ID or consult the project manager.</p></html>";
    private static final String NULL_SESSION_DATE_TITLE = "Session Has No Associated Date";
    private static final String NULL_SESSION_DATE_MESSAGE =
            "<html><p>The session you selected has no date associated with it, but you specified a date" +
                    "<p>for the session. This can happen when data has been anonymized or when scan data has been" +
                    "<p>corrupted. Please verify that you've selected the correct session for upload. If not," +
                    "<p>click the <b>Prev</b> button to return to the previous screen to select a different" +
                    "<p>session or click <b>Next</b> or <b>Finish</b> to continue with the existing session.</p></html>";
    private static final String INVALID_SESSION_DATE_TITLE = "Invalid Session Date/Time";
    private static final String INVALID_SESSION_DATE_MESSAGE =
            "<html><p>The date and/or time you indicated for your scan session does</p>" +
                    "<p>not match the date of the selected session. Please re-check</p>" +
                    "<p>the date for your new session and start over or click <b>Prev</b></p>" +
                    "<p>and select another session that matches the indicated date.</p></html>";
    private static final String UNEXPECTED_MODALITY_TITLE = "Unexpected Modality";
    private static final String UNEXPECTED_MODALITY_MESSAGE =
            "<html><p>The expected modality does not match that of the session.</p>" +
                    "<p>Please re-check the modality of your new session and start</p>" +
                    "<p>over or click <b>Prev</b> and select another session that matches</p>" +
                    "<p>the indicated modality.</p></html>";

    private final Logger logger = LoggerFactory.getLogger(AssignSessionVariablesPage.class);
    private final Set<SessionVariable> invalid = Sets.newLinkedHashSet();
    private Project project;
    private Subject subject;
    private Session session;
    private Date sessionDate;
    private SessionVariable sessionLabel;
    private Date confirmedDate;
    private boolean unsetDate = false;
    private boolean isAutoArchiving = false;
    private boolean warnOnDupeSessionLabels = true;
    private boolean allowOverwriteOnDupeSessionLabels = false;
    private boolean allowAppendOnDupeSessionLabels = true;
    private SessionVariable tracer = null;

    private static final GridBagConstraints SPANNING = new GridBagConstraints() {
        private static final long serialVersionUID = 5114328188210435952L;

        {
            gridx = 0;
            gridwidth = 2;
            insets = new Insets(8, 0, 0, 0);
        }
    };

    private UploadSelector uploadSelector;

    //If d1's hours and minutes are both == 0 we assume we're testing
    //to see if both dates occurred on the same day.  If there is data
    //in the hours and minutes, we return true if both dates are within
    // a 61 minute window of each other.
    private boolean isSessionDateOk(Date d1, Date session, TimeZone sessionTimeZone) {
        // if no date was passed into the applet, confirmedDate has already been set to equal sessionDate
        if (unsetDate) {
            return true;
        }
        else{
            Calendar cal = Calendar.getInstance();
            cal.setTime(d1);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            if (hour == 0 && minute == 0) {
                return DateUtils.isSameDay(d1, session);
            } else {
                //check the 61 minute window
                if (sessionTimeZone != null) {
                    //if the session has a time zone, we have to deal with conversion...
                    Calendar sessionCal = Calendar.getInstance();
                    sessionCal.setTimeZone(sessionTimeZone);
                    sessionCal.setTime(session);
                    int sessionHour = sessionCal.get(Calendar.HOUR_OF_DAY);
                    //make sure the date is the same day (within 24 hours) and assure the hour is within 1 hour.
                    return Math.abs(d1.getTime() - session.getTime()) < 86400000 && Math.abs(hour - sessionHour) < 2;
                } else {
                    return Math.abs(d1.getTime() - session.getTime()) < 3660000;
                }
            }
        }
    }

    /**
     * Gets the description of this step for the wizard framework.
     *
     * @return The step description.
     */
    public static String getDescription() {
        return STEP_DESCRIPTION;
    }

    /**
     * Default constructor.
     */
    public AssignSessionVariablesPage(UploadSelector uploadSelector) {
        this.uploadSelector = uploadSelector;
        setLayout(new BorderLayout());
        setLongDescription(LONG_DESCRIPTION);
    }

    /**
     * Implementation of the method.
     *
     * @see uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariableConsumer#update(uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable, boolean)
     */
    @Override
    public void update(final SessionVariable v, final boolean isValidValue) {
        logger.trace("{} updated to {}", v, isValidValue ? "valid" : "invalid");
        if (isValidValue) {
            invalid.remove(v);
        } else {
            invalid.add(v);
            setProblem(v.getValueMessage());
        }
        setProblem(validateContents(null, null));
    }

    /**
     * Verifies whether or not user wants to proceed in the case of a verification date mismatch.
     *
     * @param stepName The name of the current step.
     * @param settings Any settings for the current step.
     * @param wizard   The current wizard.
     * @return Returns <b>WizardPanelNavResult.REMAIN_ON_PAGE</b> if the user doesn't confirm, <b>WizardPanelNavResult.PROCEED</b> otherwise.
     */
    @Override
    public WizardPanelNavResult allowNext(String stepName, @SuppressWarnings("rawtypes") Map settings, Wizard wizard) {
        return isOkToProceed() ? WizardPanelNavResult.PROCEED : WizardPanelNavResult.REMAIN_ON_PAGE;
    }

    /**
     * Verifies whether or not user wants to proceed in the case of a verification date mismatch.
     *
     * @param stepName The name of the current step.
     * @param settings Any settings for the current step.
     * @param wizard   The current wizard.
     * @return Returns <b>WizardPanelNavResult.REMAIN_ON_PAGE</b> if the user doesn't confirm, <b>WizardPanelNavResult.PROCEED</b> otherwise.
     */
    @Override
    public WizardPanelNavResult allowFinish(String stepName, @SuppressWarnings("rawtypes") Map settings, Wizard wizard) {
        return isOkToProceed() ? WizardPanelNavResult.PROCEED : WizardPanelNavResult.REMAIN_ON_PAGE;
    }

    /**
     * Implements the {@link PropertyChangeListener#propertyChange(PropertyChangeEvent)} method. This is used to
     * prompt the wizard framework to take notice when something has changed on the page.
     *
     * @param event The property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        userInputReceived(this, event);
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#recycle()
     */
    @Override
    protected void recycle() {
        removeAll();
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#renderingPage()
     */
    @Override
    protected void renderingPage() {
        initialize();

        final String expectedModality = (String) this.getWizardData(MultiUploadParameters.EXPECTED_MODALITY_LABEL);
        final Set<String> modalities = session.getModalities();

        if (StringUtils.isNotBlank(expectedModality)) {
            if (("PT".equalsIgnoreCase(expectedModality) || "PET".equalsIgnoreCase(expectedModality)) && (!(modalities.contains("PT") || modalities.contains("PET")))) {
                    JOptionPane.showMessageDialog(this, UNEXPECTED_MODALITY_MESSAGE, UNEXPECTED_MODALITY_TITLE, JOptionPane.WARNING_MESSAGE);
            } else if (!modalities.contains(expectedModality)) {
                    JOptionPane.showMessageDialog(this, UNEXPECTED_MODALITY_MESSAGE, UNEXPECTED_MODALITY_TITLE, JOptionPane.WARNING_MESSAGE);
                }
            }
        if (sessionDate == null && confirmedDate != null) {
            JOptionPane.showMessageDialog(this, NULL_SESSION_DATE_MESSAGE, NULL_SESSION_DATE_TITLE, JOptionPane.WARNING_MESSAGE);
        } else if (!isSessionDateOk(confirmedDate, sessionDate, session.getTimeZone())) {
            JOptionPane.showMessageDialog(this, INVALID_SESSION_DATE_MESSAGE, INVALID_SESSION_DATE_TITLE, JOptionPane.ERROR_MESSAGE);
        } else {
            add(addContent(new JPanel(new GridBagLayout())), BorderLayout.CENTER);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#validateContents(java.awt.Component, java.lang.Object)
     */
    @Override
    protected String validateContents(final Component component, final Object event) {
        if (!isSessionDateOk(confirmedDate, sessionDate, session.getTimeZone())) {
            // We have to go through this whole rigamarole to deal with the Java Date class's inadequacies and general crappiness.
            // The year being set by dcm4che seems to absolute, i.e. setting 2012 when it really means 2012. The nerve. Instead,
            // according to Java, it should be setting the offset from 1900, i.e. 112, when it means 2012. But we get what we get.
            // So, check for a year greater than 1900 and, if found, offset by -1900 and hope like crazy that that's the right thing.
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(confirmedDate);
            //            int year = calendar.get(Calendar.YEAR);
            //            if (year > 1900) {
            //                calendar.set(Calendar.YEAR, year - 1900);
            //            }
            return "Click the Previous button and select a session with scan date of " + FORMATTER.format(calendar.getTime());
        }

        // If verifyDate is empty, we won't allow user to proceed, but we won't do any other validation.
        if (component != null && component instanceof JTextField) {
            String candidate = sessionLabel != null ? sessionLabel.getValue() : ((JTextField) component).getText();
            boolean stopForDuplicateSessionId = stopForDuplicateSessionId(candidate, false);
            if (stopForDuplicateSessionId) {
                return "You must either select another session, change the session ID, or indicate how you want to handle the duplicate session ID for your auto-archiving project.";
            }
        }
        final SortedSet<String> names = Sets.newTreeSet();
        for (final SessionVariable v : invalid) {
            names.add(v.getName());
        }
        if (names.size() > 0) {
            final StringBuilder buffer = new StringBuilder("Some fields have invalid values: ");
            final Joiner joiner = Joiner.on(", ");
            return joiner.appendTo(buffer, names).toString();
        }
        return null;
    }

    /**
     * Initializes all incoming data from the wizard's data map.
     */
    private void initialize() {
        project = uploadSelector.getProject();
        subject = uploadSelector.getSubject();
        session = uploadSelector.getSession();
        sessionDate = session.getDateTime();
        sessionLabel = (SessionVariable) getWizardData(SessionVariableNames.SESSION_LABEL);
        if (getWizardDataMap().containsKey(SessionVariableNames.WARN_ON_DUPE_SESSION_LABELS)) {
            warnOnDupeSessionLabels = Boolean.parseBoolean(((AssignedSessionVariable) getWizardData(SessionVariableNames.WARN_ON_DUPE_SESSION_LABELS)).getValue());
        }
        if (getWizardDataMap().containsKey(SessionVariableNames.ALLOW_OVERWRITE_ON_DUPE_SESSION_LABELS)) {
            allowOverwriteOnDupeSessionLabels = Boolean.parseBoolean(((AssignedSessionVariable) getWizardData(SessionVariableNames.ALLOW_OVERWRITE_ON_DUPE_SESSION_LABELS)).getValue());
        }
        if (getWizardDataMap().containsKey(SessionVariableNames.ALLOW_APPEND_ON_DUPE_SESSION_LABELS)) {
            allowAppendOnDupeSessionLabels = Boolean.parseBoolean(((AssignedSessionVariable) getWizardData(SessionVariableNames.ALLOW_APPEND_ON_DUPE_SESSION_LABELS)).getValue());
        }

        if (uploadSelector.isDateSet()) {
            confirmedDate = uploadSelector.getDate();

        } else if (uploadSelector.getDateFromSession()) {
            // So if it's no session date, there's a good chance the session date is null. If so, initialize both
            // of the dates so that they're matching and we can pass through the date-check logic cleanly.
            if (sessionDate == null) {
                sessionDate = new Date();
            }
            confirmedDate = sessionDate;
            unsetDate = true;
        }
    }

    private JPanel addContent(final JPanel panel) {
        logger.trace("adding content");

        try {
            isAutoArchiving = project.getPrearchiveCode() != PrearchiveCode.Manual;
        } catch (Exception exception) {
            logger.warn("Error when retrieving project prearchive code", exception);
        }

        final Map<String, SessionVariable> predefs = Maps.newLinkedHashMap();
        predefs.put(SessionVariableNames.PROJECT, new AssignedSessionVariable(SessionVariableNames.PROJECT, project.toString()).fixValue());
        predefs.put(SessionVariableNames.SUBJECT, new AssignedSessionVariable(SessionVariableNames.SUBJECT, subject.toString()).fixValue());
        SessionVariable vSession = (SessionVariable) getWizardData(SessionVariableNames.PREDEF_SESSION);
        if (null != vSession) {
            predefs.put(SessionVariableNames.SESSION_LABEL, vSession);
        }

        final LinkedList<SessionVariable> vars = Lists.newLinkedList(session.getVariables(uploadSelector.getProject(), uploadSelector.getSession()));
        putWizardData(PRODUCT_NAME, vars);
        logger.trace("initialized session variables: {}", vars);

        final Set<String> modalities = session.getModalities();

        // modality = PET indicates an ECAT data set, so obviously that's PET.
        final String leadModality = modalities.contains("PET") ? "PET" : SOPModel.getLeadModality(modalities);

        // Strip project and subject out of the List: these are immutable and we'll
        // display them separately.  Also check whether session has been defined;
        // we'll use the existing variable if so, or add a new one if not.
        SessionVariable modalityLabel = null;
        for (final ListIterator<SessionVariable> i = vars.listIterator(); i.hasNext(); ) {
            final SessionVariable v = i.next();
            final String name = v.getName();
            if (SessionVariableNames.SESSION_LABEL.equals(name)) {
                vSession = v;
            } // no else clause: session might also be a predef and we want normal predef handling

            if (predefs.containsKey(name)) {
                final SessionVariable predef = predefs.get(name);
                logger.trace("found predefined variable {} in script", v);
                i.remove();
                try {
                    v.fixValue(predef.getValue());
                } catch (SessionVariable.InvalidValueException e) {
                    throw new RuntimeException(e);
                }
                predefs.put(v.getName(), v);
            } else if (SessionVariableNames.MODALITY_LABEL.equals(name)) {
                modalityLabel = v;   // Process this later
            } else if (SessionVariableNames.TRACER.equals(name)) {
                logger.trace("found session variable {}, will shadow", v);
                // tracer is special and PET-only
                final String field = v.getExportField();
                if (!Strings.isNullOrEmpty(field) && !SessionVariableNames.TRACER_PATH.equals(field)) {
                    logger.error("script variable {} has unexpected export path {}, replacing with " + SessionVariableNames.TRACER, v, field);
                }
                i.remove();
                tracer = v;  // Process this later
            }
        }

        // If this is a PET study, allow the user to specify a tracer.
        final boolean hasPETNoLabel = !predefs.containsKey(SessionVariableNames.SESSION_LABEL) && (modalities.contains("PET") || modalities.contains("PT"));
        if (hasPETNoLabel) {
            logger.trace("Found PET in session without predefined label");
            final Set<String> tracers = Sets.newLinkedHashSet();
            try {
                tracers.addAll(project.getPETTracers());
            } catch (Throwable t) {
                logger.error("error retrieving PET tracers", t);
                tracers.clear();
                tracers.addAll(PETTracerRetriever.getDefaultTracers());
            }
            if (tracer == null) {
                tracer = new EnumeratedSessionVariable(SessionVariableNames.TRACER, SessionVariableNames.TRACER_PATH, tracers, true, true);
            } else {
                ((EnumeratedSessionVariable) tracer).setItems(tracers);
            }
        }

        if (null != modalityLabel) {
            if (modalityLabel instanceof DicomSessionVariable) {
                final DicomSessionVariable dsv = (DicomSessionVariable) modalityLabel;
                try {
                    if (null == tracer) {
                        logger.trace("setting (hidden) modality label from lead modality {}", leadModality);
                        dsv.setInitialValue(new org.nrg.dcm.edit.ConstantValue(leadModality));
                    } else {
                        logger.trace("setting (hidden) modality label from tracer variable {}", tracer);
                        dsv.setInitialValue(new SessionVariableValue(tracer));
                        tracer.addListener(dsv);
                    }
                    dsv.setIsHidden(true);
                } catch (MultipleInitializationException exception) {
                    logger.debug("Got MultipleInitializationException", exception);
                }
            }
        }

        final ExcludingValueValidator excludeExistingSessions = getSessionExcluder(project, true);

        logger.trace("starting upload for {}", predefs);

        // If no initial value was provided for session, construct one in the format
        // {subject}_{modality}{index}, where index is the smallest positive integer
        // that results in a session label not already defined in this project.
        final IndexedDependentSessionVariable defaultSessionLabel;

        if (hasPETNoLabel) {
            // use the tracer instead of the modality string for PET
            assert null != tracer;
            logger.trace("building indexed tracer default session label from {}", tracer);
            defaultSessionLabel = new IndexedDependentSessionVariable(SessionVariableNames.SESSION_LABEL, tracer,
                    subject + "_%s%d", excludeExistingSessions);
        } else {
            logger.trace("building indexed modality default session label from {}", leadModality);
            defaultSessionLabel = new IndexedDependentSessionVariable(SessionVariableNames.SESSION_LABEL, predefs.get(SessionVariableNames.SUBJECT), "%s_" + leadModality + "%d", excludeExistingSessions);
        }
        if (null == vSession) {
            vars.addFirst(vSession = defaultSessionLabel);
        } else {
            if (vSession.isMutable() && !LabelValueValidator.getInstance().isValid(vSession.getValue())) {
                // The existing session variable doesn't have a useful initial value,
                // so shadow it with the default label format.
                final SessionVariable scriptSession = vSession;
                try {
                    vSession.setValue(defaultSessionLabel.getValue());
                } catch (SessionVariable.InvalidValueException e) {
                    logger.error("unable to set shadowed session variable", e);
                }

                defaultSessionLabel.addShadow(vSession);
                vSession = defaultSessionLabel;
                for (final ListIterator<SessionVariable> li = vars.listIterator(); li.hasNext(); ) {
                    if (scriptSession == li.next()) {
                        li.set(vSession);
                        break;
                    }
                }
            }
        }
        if (vSession.isMutable()) {
            // This handles JIRA XNAT-985: upon returning to the page, a new tracer control is created, but the session
            // identifier control still has a reference to the old tracer control, breaking the dependency between the controls.
            // We also have to check for tracer != null to keep from overwriting other dependency types, especially when
            // it uses the subject as the dependency.
            if (tracer != null && vSession instanceof IndexedDependentSessionVariable) {
                IndexedDependentSessionVariable indexed = (IndexedDependentSessionVariable) vSession;
                if (indexed.getDependency() == null || !indexed.getDependency().equals(tracer)) {
                    indexed.setDependency(tracer);
                }
            }

            vSession.addValidator(LabelValueValidator.getInstance());
        }

        if (hasPETNoLabel) {
            assert null != tracer;
            if (!vars.contains(tracer)) {
                vars.addFirst(tracer);
            }
        }

        // Session name, like project and subject, must be available in the wizard params.
        putWizardData(vSession.getName(), vSession);

        addSessionIdentifiers(panel, predefs, vars);

        return panel;
    }

    /**
     * Indicates whether a duplicate session ID has been identified for an auto-archived project and if the user has
     * indicated how to handle it.
     *
     * @return <b>false</b> if the project is not set to auto-archive, the selected session ID is not a duplicate of an
     *         existing session ID, or the user has indicated whether the duplicate session should be appended to or
     *         overwrite the existing session with the duplicate ID. In the case where a duplicate session ID is found
     *         for an auto-archiving project and the user selects <b>Cancel</b> on the option dialog, this method
     *         returns <b>true</b>.
     */
    private boolean stopForDuplicateSessionId(String candidate, boolean showDialog) {
        // Notify about duplicate session IDs when warning flag is set or the project is auto-archived.
        if (warnOnDupeSessionLabels || isAutoArchiving) {
            try {
                Map<String, String> labels = project.getSessionLabels();
                if (StringUtils.isNotBlank(candidate) && labels.containsKey(candidate)) {
                    if (showDialog) {
                        if (allowOverwriteOnDupeSessionLabels) {
                            int selected = JOptionPane.showOptionDialog(this, DUP_SESSION_ID_MESSAGE_WITH_OVERWRITE, DUP_SESSION_ID_TITLE, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, DUP_SESSION_ID_OPTIONS_WITH_OVERWRITE, DUP_SESSION_ID_OPTIONS_WITH_OVERWRITE[2]);
                            switch (selected) {
                                case JOptionPane.YES_OPTION:
                                    putWizardData(Project.AUTO_ARCHIVE, AutoArchive.Overwrite);
                                    break;
                                case JOptionPane.NO_OPTION:
                                    putWizardData(Project.AUTO_ARCHIVE, AutoArchive.Append);
                                    break;
                                case JOptionPane.CLOSED_OPTION:
                                case JOptionPane.CANCEL_OPTION:
                                    return true;
                            }
                        } else if (allowAppendOnDupeSessionLabels) {
                            int selected = JOptionPane.showOptionDialog(this, DUP_SESSION_ID_MESSAGE, DUP_SESSION_ID_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, DUP_SESSION_ID_OPTIONS, DUP_SESSION_ID_OPTIONS[1]);
                            switch (selected) {
                                case JOptionPane.YES_OPTION:
                                    putWizardData(Project.AUTO_ARCHIVE, AutoArchive.Append);
                                    break;
                                case JOptionPane.NO_OPTION:
                                case JOptionPane.CLOSED_OPTION:
                                    return true;
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, DUP_SESSION_ID_WO_APPEND_MESSAGE, DUP_SESSION_ID_TITLE, JOptionPane.WARNING_MESSAGE);
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            } catch (Exception exception) {
                // ToDo: Deal correctly with user cancellation
                logger.warn("Error when retrieving project session labels for project", exception);
            }
        }

        return false;
    }

    private void addSessionIdentifiers(JPanel panel, Map<String, SessionVariable> predefs, LinkedList<SessionVariable> vars) {
        for (final SessionVariable v : predefs.values()) {
            panel.add(new JLabel(v.getDescription()), VariableAssignmentManager.labelConstraint);
            panel.add(new JLabel(v.getValue()), VariableAssignmentManager.valueConstraint);
        }

        if (vars.size() > 0) {
            panel.add(new JLabel("<html><b>Set session identifiers:</b></html>"), SPANNING);
            panel.add(new JLabel(), VariableAssignmentManager.messageConstraint);
        }
        new VariableAssignmentManager(panel, vars, this);
    }

    private ExcludingValueValidator getSessionExcluder(final Project project, final boolean isStrict) {
        Set<String> labels;
        try {
            labels = project.getSessionLabels().keySet();
        } catch (Exception e) {
            // ToDo: Deal correctly with user cancellation
            labels = Collections.emptySet();
        }
        return new ExcludingValueValidator(labels, "Project already contains a session named %s.", isStrict);
    }

    private boolean isOkToProceed() {
        // If our dates are bad, we can't go forward.
        if (!(sessionDate != null && isSessionDateOk(confirmedDate, sessionDate, session.getTimeZone()))) {
            return false;
        }

        // If we don't have a value for the session label at all, we can't go forward.
        SessionVariable userSessionLabelContainer = ((SessionVariable) getWizardData("session"));
        final String userSessionLabel = userSessionLabelContainer.getValue();
        if (sessionLabel == null && StringUtils.isBlank(userSessionLabel)) {
            return false;
        }

        if (!LabelValueValidator.getInstance().isValid(userSessionLabel)) {
            return false;
        }

        // If we have a duplicate session label we have to check to see if they've
        // selected an appropriate autoarchive setting. If so, we can let them proceed.
        final boolean stop = stopForDuplicateSessionId(userSessionLabel, true);
        AutoArchive autoArchive = null;
        if (stop) {
            autoArchive = (AutoArchive) getWizardData(Project.AUTO_ARCHIVE);
        }
        return !stop || autoArchive != null;
    }

    private static final DateFormat FORMATTER = new SimpleDateFormat("M/d/yyyy HH:mm");
}
