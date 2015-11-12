package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;
import java.util.ResourceBundle;

/**
 * Shows a dialog for setting the properties of the GIFT-Cloud Uplaoder app
 */
public class GiftCloudConfigurationDialog {

    private static int textFieldLengthForGiftCloudServerUrl = 32;

    private final GiftCloudUploaderController controller;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private ProjectListModel projectListModel;
    private ResourceBundle resourceBundle;
    private final GiftCloudDialogs giftCloudDialogs;
    private GiftCloudReporter reporter;
    private final JDialog dialog;

    private final JComboBox<String> projectList;
    private final JTextField giftCloudServerText;
    private final JTextField giftCloudUsernameText;
    private final JPasswordField giftCloudPasswordText;
    private final JTextField listeningAETitleField;
    private final JTextField listeningPortField;
    private final JTextField patientListExportFolderField;
    private final JTextField subjectPrefixField;
    private final JPasswordField patientListSpreadsheetPasswordField;
    private final JTextField remoteAETitleField;
    private final JTextField remoteAEHostName;
    private final JTextField remoteAEPortField;
    private final JLabel projectListWaitingLabel;

    private final Component componentToCenterDialogOver;
    private final TemporaryProjectListModel temporaryDropDownListModel;
    private DropDownListModel.EnabledListener<Boolean> projectListEnabledListener = null;

    private boolean isDisposed = false;

    GiftCloudConfigurationDialog(final Component owner, final GiftCloudUploaderController controller, final GiftCloudPropertiesFromApplication giftCloudProperties, final ProjectListModel projectListModel, final ResourceBundle resourceBundle, final GiftCloudDialogs giftCloudDialogs, final GiftCloudReporter reporter) {
        this.controller = controller;
        this.giftCloudProperties = giftCloudProperties;
        this.projectListModel = projectListModel;
        this.resourceBundle = resourceBundle;
        this.giftCloudDialogs = giftCloudDialogs;
        this.reporter = reporter;
        temporaryDropDownListModel = new TemporaryProjectListModel(projectListModel, giftCloudProperties.getLastProject());
        componentToCenterDialogOver = owner;

        dialog = new JDialog();
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(componentToCenterDialogOver);	// without this, appears at TLHC rather then center of parent or screen
        dialog.setTitle(resourceBundle.getString("configurationDialogTitle"));

        Border panelBorder = BorderFactory.createEtchedBorder();

        final GridBagConstraints sectionTitleConstraints = new GridBagConstraints();
        sectionTitleConstraints.gridx = 0;
        sectionTitleConstraints.gridy = 1;
        sectionTitleConstraints.gridwidth = 2;
        sectionTitleConstraints.weightx = 1;
        sectionTitleConstraints.weighty = 1;
        sectionTitleConstraints.anchor = GridBagConstraints.CENTER;
        sectionTitleConstraints.fill = GridBagConstraints.HORIZONTAL;

        final GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.gridwidth = 1;
        labelConstraints.weightx = 1;
        labelConstraints.weighty = 1;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.fill = GridBagConstraints.NONE;

        final GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 1;
        inputConstraints.gridy = 0;
        inputConstraints.gridwidth = 1;
        inputConstraints.weightx = 1;
        inputConstraints.weighty = 1;
        inputConstraints.anchor = GridBagConstraints.LINE_END;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;

        GridBagConstraints separatorConstraint = new GridBagConstraints();
        separatorConstraint.weightx = 1.0;
        separatorConstraint.fill = GridBagConstraints.HORIZONTAL;
        separatorConstraint.gridwidth = GridBagConstraints.REMAINDER;

        // The panel containing the GIFT-Cloud server configuration
        final JPanel giftCloudServerPanel = new JPanel();
        {
            GridBagLayout projectUploadlayout = new GridBagLayout();
            giftCloudServerPanel.setLayout(projectUploadlayout);
            JLabel serverPanelLabel = new JLabel(resourceBundle.getString("configPanelServerConfig"), SwingConstants.CENTER);
            giftCloudServerPanel.add(serverPanelLabel, sectionTitleConstraints);

            // GIFT-Cloud server URL
            {
                labelConstraints.gridwidth = 1;
                labelConstraints.gridy = 2;
                final JLabel giftCloudServerLabel = new JLabel(resourceBundle.getString("giftCloudServerText"), SwingConstants.RIGHT);
                giftCloudServerLabel.setToolTipText(resourceBundle.getString("giftCloudServerTextToolTipText"));
                giftCloudServerPanel.add(giftCloudServerLabel, labelConstraints);

                giftCloudServerText = new AutoFocusTextField(giftCloudProperties.getGiftCloudUrl().orElse(""), textFieldLengthForGiftCloudServerUrl);
                inputConstraints.gridy = 2;
                giftCloudServerPanel.add(giftCloudServerText, inputConstraints);
            }

            // GIFT-Cloud username
            {
                labelConstraints.gridy = 3;
                final JLabel giftCloudUserNameLabel = new JLabel(resourceBundle.getString("giftCloudUsername"), SwingConstants.RIGHT);
                giftCloudUserNameLabel.setToolTipText(resourceBundle.getString("giftCloudUsernameToolTipText"));
                giftCloudServerPanel.add(giftCloudUserNameLabel, labelConstraints);

                final Optional<String> serverUrl = giftCloudProperties.getLastUserName();
                final String initialServerText = serverUrl.isPresent() ? serverUrl.get() : "";
                giftCloudUsernameText = new AutoFocusTextField(initialServerText);
                inputConstraints.gridy = 3;
                giftCloudServerPanel.add(giftCloudUsernameText, inputConstraints);
            }

            // GIFT-Cloud password
            {
                labelConstraints.gridy = 4;
                final JLabel giftCloudPasswordLabel = new JLabel(resourceBundle.getString("giftCloudPassword"), SwingConstants.RIGHT);
                giftCloudPasswordLabel.setToolTipText(resourceBundle.getString("giftCloudPasswordToolTipText"));
                giftCloudServerPanel.add(giftCloudPasswordLabel, labelConstraints);

                final Optional<char[]> password = giftCloudProperties.getLastPassword();
                final char[] initialPassword = password.isPresent() ? password.get() : "".toCharArray();
                giftCloudPasswordText = new JPasswordField(new String(initialPassword), 16); // Shouldn't create a String but there's no other way to initialize the password field
                inputConstraints.gridy = 4;
                giftCloudServerPanel.add(giftCloudPasswordText, inputConstraints);
            }

            // Project list
            {
                labelConstraints.gridy = 5;
                JLabel projectListLabel = new JLabel(resourceBundle.getString("giftCloudProjectLabelText"), SwingConstants.RIGHT);
                giftCloudServerPanel.add(projectListLabel, labelConstraints);

                inputConstraints.gridy = 5;
                projectList = new JComboBox<String>();
                projectList.setEditable(false);
                projectList.setToolTipText(resourceBundle.getString("giftCloudProjectTooltip"));
                giftCloudServerPanel.add(projectList, inputConstraints);

                labelConstraints.gridx = 1;
                projectListWaitingLabel = new JLabel(resourceBundle.getString("giftCloudProjectWaitingLabelText"), SwingConstants.RIGHT);
                giftCloudServerPanel.add(projectListWaitingLabel, labelConstraints);
                labelConstraints.gridx = 0;
            }

            // Subject prefix
            {
                labelConstraints.gridy = 6;
                JLabel subjectPrefixLabel = new JLabel(resourceBundle.getString("configPanelListenerSubjectPrefix"), SwingConstants.RIGHT);
                subjectPrefixLabel.setToolTipText(resourceBundle.getString("configPanelListenerSubjectPrefixTooltip"));
                giftCloudServerPanel.add(subjectPrefixLabel, labelConstraints);

                inputConstraints.gridy = 6;
                final Optional<String> subjectPrefixText = giftCloudProperties.getSubjectPrefix();
                subjectPrefixField = new AutoFocusTextField(subjectPrefixText.orElse(""));
                giftCloudServerPanel.add(subjectPrefixField, inputConstraints);
            }
        }

        // Local Dicom node configuration
        final JPanel listenerPanel = new JPanel();
        {
            GridBagLayout listenerPanellayout = new GridBagLayout();
            listenerPanel.setLayout(listenerPanellayout);
            JSeparator separator = new JSeparator();
            listenerPanel.add(separator, separatorConstraint);

            JLabel listenerPanelLabel = new JLabel(resourceBundle.getString("configPanelListenerConfig"), SwingConstants.CENTER);
            listenerPanel.add(listenerPanelLabel, sectionTitleConstraints);

            {
                labelConstraints.gridy = 2;
                JLabel listeningAETitleJLabel = new JLabel(resourceBundle.getString("configPanelListenerAe"), SwingConstants.RIGHT);
                listeningAETitleJLabel.setToolTipText(resourceBundle.getString("configPanelListenerAeToolTip"));
                listenerPanellayout.setConstraints(listeningAETitleJLabel, labelConstraints);
                listenerPanel.add(listeningAETitleJLabel);

                inputConstraints.gridy = 2;
                final String listeningAETitleInitialText = giftCloudProperties.getListenerAETitle();
                listeningAETitleField = new AutoFocusTextField(listeningAETitleInitialText);
                listenerPanellayout.setConstraints(listeningAETitleField, inputConstraints);
                listenerPanel.add(listeningAETitleField);
            }
            {
                labelConstraints.gridy = 3;
                JLabel listeningPortJLabel = new JLabel(resourceBundle.getString("configPanelListenerPort"), SwingConstants.RIGHT);
                listeningPortJLabel.setToolTipText(resourceBundle.getString("configPanelListenerPortToolTip"));
                listenerPanellayout.setConstraints(listeningPortJLabel, labelConstraints);
                listenerPanel.add(listeningPortJLabel);

                inputConstraints.gridy = 3;
                final int port = giftCloudProperties.getListeningPort();
                final String portValue = Integer.toString(port);
                listeningPortField = new AutoFocusTextField(portValue);
                listenerPanellayout.setConstraints(listeningPortField, inputConstraints);
                listenerPanel.add(listeningPortField);
            }
            {
                labelConstraints.gridy = 4;
                JLabel patientListExportFolderLabel = new JLabel(resourceBundle.getString("configPanelListenerPatientListExportFolder"), SwingConstants.RIGHT);
                patientListExportFolderLabel.setToolTipText(resourceBundle.getString("configPanelListenerPatientListExportFolderTooltip"));
                listenerPanellayout.setConstraints(patientListExportFolderLabel, labelConstraints);
                listenerPanel.add(patientListExportFolderLabel);

                inputConstraints.gridy = 4;
                final Optional<String> patientListExportFolder = giftCloudProperties.getPatientListExportFolder();
                patientListExportFolderField = new AutoFocusTextField(patientListExportFolder.orElse(""));
                listenerPanellayout.setConstraints(patientListExportFolderField, inputConstraints);
                listenerPanel.add(patientListExportFolderField);
            }

            // Patient list spreadsheet password
            {
                labelConstraints.gridy = 5;
                final JLabel patientListSpreadsheetPasswordLabel = new JLabel(resourceBundle.getString("configPanelListenerPatientListSpreadhsheetPassword"), SwingConstants.RIGHT);
                patientListSpreadsheetPasswordLabel.setToolTipText(resourceBundle.getString("configPanelListenerPatientListSpreadhsheetPasswordTooltip"));
                listenerPanel.add(patientListSpreadsheetPasswordLabel, labelConstraints);

                final Optional<char[]> password = giftCloudProperties.getPatientListPassword();
                final char[] initialPassword = password.isPresent() ? password.get() : "".toCharArray();
                patientListSpreadsheetPasswordField = new JPasswordField(new String(initialPassword), 16); // Shouldn't create a String but there's no other way to initialize the password field
                inputConstraints.gridy = 5;
                listenerPanel.add(patientListSpreadsheetPasswordField, inputConstraints);
            }
        }


        // Remote PACS configuration
        final JPanel remoteAEPanel = new JPanel();

        {
            GridBagLayout pacsPanellayout = new GridBagLayout();
            remoteAEPanel.setLayout(pacsPanellayout);

            JSeparator separator = new JSeparator();
            remoteAEPanel.add(separator, separatorConstraint);

            JLabel remotePanelLabel = new JLabel(resourceBundle.getString("pacsPanelListenerConfig"), SwingConstants.CENTER);
            remoteAEPanel.add(remotePanelLabel, sectionTitleConstraints);

            {
                labelConstraints.gridy = 2;
                JLabel remoteAeTitleLabel = new JLabel(resourceBundle.getString("configPanelPacsAeTitle"), SwingConstants.RIGHT);
                remoteAeTitleLabel.setToolTipText(resourceBundle.getString("configPanelPacsAeTitleTooltip"));
                remoteAEPanel.add(remoteAeTitleLabel, labelConstraints);

                final Optional<String> pacsAeTitle = giftCloudProperties.getPacsAeTitle();
                remoteAETitleField = new AutoFocusTextField(pacsAeTitle.isPresent() ? pacsAeTitle.get() : "");
                inputConstraints.gridy = 2;
                remoteAEPanel.add(remoteAETitleField, inputConstraints);
            }

            {
                labelConstraints.gridy = 3;
                JLabel remoteAeHostLabel = new JLabel(resourceBundle.getString("configPanelPacsHostname"), SwingConstants.RIGHT);
                remoteAeHostLabel.setToolTipText(resourceBundle.getString("configPanelPacsHostnameTooltip"));
                remoteAEPanel.add(remoteAeHostLabel, labelConstraints);

                remoteAEHostName = new AutoFocusTextField(giftCloudProperties.getPacsHostName().orElse(""));
                inputConstraints.gridy = 3;
                remoteAEPanel.add(remoteAEHostName, inputConstraints);
            }

            {
                labelConstraints.gridy = 4;
                JLabel remoteAeTitleLabel = new JLabel(resourceBundle.getString("configPanelPacsPort"), SwingConstants.RIGHT);
                remoteAeTitleLabel.setToolTipText(resourceBundle.getString("configPanelPacsPortTooltip"));
                remoteAEPanel.add(remoteAeTitleLabel, labelConstraints);

                remoteAEPortField = new AutoFocusTextField(Integer.toString(giftCloudProperties.getPacsPort()));
                inputConstraints.gridy = 4;
                remoteAEPanel.add(remoteAEPortField, inputConstraints);
            }
        }

        // The panel containing the cancel and apply buttons
        JPanel closeButtonPanel = new JPanel();
        {
            closeButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            closeButtonPanel.setBorder(panelBorder);

            JButton cancelButton = new JButton(resourceBundle.getString("cancelSettingsButtonLabelText"));
            cancelButton.setToolTipText(resourceBundle.getString("cancelSettingsButtonToolTipText"));
            closeButtonPanel.add(cancelButton);
            cancelButton.addActionListener(new CancelActionListener());

            JButton applyButton = new JButton(resourceBundle.getString("applySettingsButtonLabelText"));
            applyButton.setToolTipText(resourceBundle.getString("applySettingsButtonToolTipText"));
            closeButtonPanel.add(applyButton);
            applyButton.addActionListener(new ApplyActionListener());

            JButton closeButton = new JButton(resourceBundle.getString("closeSettingsButtonLabelText"));
            closeButton.setToolTipText(resourceBundle.getString("closeSettingsButtonToolTipText"));
            closeButtonPanel.add(closeButton);
            closeButton.addActionListener(new CloseActionListener());
        }


        // The main panel of the configuration dialog
        JPanel configPanel = new JPanel();
        {
            final GridBagLayout configPanelLayout = new GridBagLayout();
            configPanel.setLayout(configPanelLayout);
            {
                final GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.weightx = 1;
                constraints.weighty = 1;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                configPanelLayout.setConstraints(giftCloudServerPanel, constraints);
                configPanel.add(giftCloudServerPanel);
            }
            {
                final GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 1;
                constraints.weightx = 1;
                constraints.weighty = 1;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                configPanelLayout.setConstraints(listenerPanel, constraints);
                configPanel.add(listenerPanel);
            }
            {
                final GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 2;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                configPanelLayout.setConstraints(remoteAEPanel, constraints);
                configPanel.add(remoteAEPanel);
            }
            {
                final GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 3;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                configPanelLayout.setConstraints(closeButtonPanel, constraints);
                configPanel.add(closeButtonPanel);
            }
        }

        projectList.setModel(temporaryDropDownListModel);
        showProjectList(projectListModel.isEnabled());

        // Create a listener to enable/disable the project list when it is set from the server.
        // The reason for this is that the project list is set after logging into the server, which can happen asynchronously after property changes have been applied.
        // If the server was configured in the dialog and apply clicked, it might take a few seconds for the project list to be updated, and we want it to become available when this happens
        projectListEnabledListener = new DropDownListModel.EnabledListener<Boolean>() {
            @Override
            public void statusChanged(final Boolean visibility) {
                showProjectList(projectListModel.isEnabled());
            }
        };

        projectListModel.addListener(projectListEnabledListener);

        GridBagLayout layout = new GridBagLayout();
        dialog.setLayout(layout);
        Container content = dialog.getContentPane();
        content.add(configPanel);
        dialog.pack();
        dialog.setVisible(true);
        dialog.pack();
    }

    private void showProjectList(final boolean enabled) {
        projectList.setEnabled(enabled);
        projectList.setVisible(enabled);
        projectListWaitingLabel.setVisible(!enabled);
    }

    public boolean isVisible() {
        return dialog.isDisplayable() && !isDisposed;
    }

    private class CloseActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (checkAndApplyProperties()) {
                closeDialog();
            }
        }
    }

    private class CancelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            closeDialog();
        }
    }

    private class ApplyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            checkAndApplyProperties();
        }
    }

    private boolean checkAndApplyProperties() {
        final List<String> problems = checkProperties();

        if (problems.isEmpty()) {
            applyProperties();
            return true;
        } else {
            final StringBuilder builder = new StringBuilder();
            for (final String string : problems) {
                builder.append(" - ");
                builder.append(string);
                builder.append("<br>");
            }
            giftCloudDialogs.showError("Please correct the following problems with the settings:", Optional.of(builder.toString()));
            return false;
        }
    }

    private List<String> checkProperties() {
        final List<String> problems = new ArrayList<String>();

        {
            final String listeningAETitle = listeningAETitleField.getText();

            if (StringUtils.isBlank(listeningAETitle)) {
                problems.add(resourceBundle.getString("configPanelListenerAeNotSet"));
            } else if (!GiftCloudConfigurationDialog.isValidAETitle(listeningAETitle)) {
                problems.add(resourceBundle.getString("configPanelListenerAeError"));
            }
        }
        {
            try {
                final int listeningPort = Integer.parseInt(listeningPortField.getText());
                if (listeningPort < 1024) {
                    problems.add(resourceBundle.getString("configPanelListenerPortSmallError"));
                }
            } catch (NumberFormatException e) {
                problems.add(resourceBundle.getString("configPanelListenerPortError"));
            }
        }
        {
            try {
                final int remotePort = Integer.parseInt(remoteAEPortField.getText());
                if (remotePort < 1024) {
                    problems.add(resourceBundle.getString("configPanelPacsPortSmallError"));
                }
            } catch (NumberFormatException e) {
                problems.add(resourceBundle.getString("configPanelPacsPortError"));
            }
        }

        {
            final String remoteAETitle = remoteAETitleField.getText();
            if (!StringUtils.isBlank(remoteAETitle) && !GiftCloudConfigurationDialog.isValidAETitle(remoteAETitle)) {
                problems.add(resourceBundle.getString("configPanelPacsAeError"));
            }
        }

        {
            final String serverText = giftCloudServerText.getText();
            if (!StringUtils.isBlank(serverText)) {
                try {
                    final URL giftCloudUrl = new URL(serverText);
                } catch (MalformedURLException e) {
                    problems.add(resourceBundle.getString("giftCloudServerTextError"));
                }
            }
        }

        {
            final String patientListExportFolder = patientListExportFolderField.getText();
            if (!StringUtils.isBlank(patientListExportFolder)) {
                try {
                    if (!GiftCloudUtils.createDirectoryIfNotExisting(new File(patientListExportFolder))) {
                        problems.add(resourceBundle.getString("configPanelListenerPatientListExportFolderCreationError"));
                    } else {
                        if (!GiftCloudUtils.isDirectoryWritable(patientListExportFolder)) {
                            problems.add(resourceBundle.getString("configPanelListenerPatientListExportFolderError"));
                        }
                    }
                } catch (Throwable t) {
                    problems.add(resourceBundle.getString("configPanelListenerPatientListExportFolderError"));
                }

            }
        }
        return problems;
    }

    private void applyProperties() {

        // Extract out all the new values (this makes the code a little clearer below)
        final int newListeningPortValue = Integer.parseInt(listeningPortField.getText());
        final String newListeningAeTitle = listeningAETitleField.getText();
        final String newGiftCloudUrl = giftCloudServerText.getText();
        final String newGiftCloudUserName = giftCloudUsernameText.getText();
        final char[] newGiftCloudPassword = giftCloudPasswordText.getPassword();
        final char[] newPatientListPassword = patientListSpreadsheetPasswordField.getPassword();
        final String newPatientListExportFolder = patientListExportFolderField.getText();
        final String newSubjectPrefix = subjectPrefixField.getText();
        final int newPacsPort = Integer.parseInt(remoteAEPortField.getText());
        final String newPacsAeTitle = remoteAETitleField.getText();
        final String newPacsHostName = remoteAEHostName.getText();

        // Determine whether to restart the listener service based on changes to the listener or PACS properties
        final boolean restartDicomNode =
                giftCloudProperties.getListeningPort() != newListeningPortValue ||
                !newListeningAeTitle.equals(giftCloudProperties.getListenerAETitle()) ||
                giftCloudProperties.getPacsPort() != newPacsPort ||
                !newPacsAeTitle.equals(giftCloudProperties.getPacsAeTitle().orElse("")) ||
                !newPacsHostName.equals(giftCloudProperties.getPacsHostName().orElse(""));

        final boolean restartUploader =
                !newGiftCloudUrl.equals(giftCloudProperties.getGiftCloudUrl().orElse("")) ||
                !newGiftCloudUserName.equals(giftCloudProperties.getLastUserName().orElse("")) ||
                !newGiftCloudPassword.equals(giftCloudProperties.getLastPassword().orElse("".toCharArray())) ||
                !temporaryDropDownListModel.getSelectedItem().equals(projectListModel.getSelectedItem());

        final boolean forcePatientListExport = StringUtils.isNotBlank(newPatientListExportFolder) &&
                (!giftCloudProperties.getPatientListExportFolder().isPresent() ||
                        !newPatientListExportFolder.equals(giftCloudProperties.getPatientListExportFolder().get()) ||
                        !newPatientListPassword.equals(giftCloudProperties.getPatientListPassword().orElse("".toCharArray())));

        // Change the properties (must be done after we access the current values to check for changes)
        giftCloudProperties.setListeningPort(newListeningPortValue);
        giftCloudProperties.setListenerAETitle(newListeningAeTitle);
        giftCloudProperties.setPatientListExportFolder(newPatientListExportFolder);
        giftCloudProperties.setGiftCloudUrl(newGiftCloudUrl);
        giftCloudProperties.setLastUserName(newGiftCloudUserName);
        giftCloudProperties.setLastPassword(newGiftCloudPassword);
        giftCloudProperties.setPatientListPassword(newPatientListPassword);
        giftCloudProperties.setSubjectPrefix(newSubjectPrefix);
        giftCloudProperties.setPacsPort(newPacsPort);
        giftCloudProperties.setPacsAeTitle(newPacsAeTitle);
        giftCloudProperties.setPacsHostName(newPacsHostName);

        // We only update the last project with a valid value. A null value may indicate the project list hasn't been
        // set because the server login details are incorrect or some communication error
        final Object selectedProjectItem = temporaryDropDownListModel.getSelectedItem();
        if (selectedProjectItem instanceof String) {
            final String newProject = (String)selectedProjectItem;
            if (StringUtils.isNotBlank(newProject)) {
                giftCloudProperties.setLastProject(newProject);
            }
        }

        giftCloudProperties.save();

        if (restartDicomNode || restartUploader || forcePatientListExport) {
            Cursor was = dialog.getCursor();
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            if (restartDicomNode) {
                controller.restartDicomService();
            }
            if (restartUploader) {
                controller.invalidateServerAndRestartUploader();
            }
            if (forcePatientListExport) {
                controller.exportPatientList();
            }

            dialog.setCursor(was);
        }

    }

    private void closeDialog() {
        projectListModel.removeListener(projectListEnabledListener);
        projectListEnabledListener = null;
        isDisposed = true;
        dialog.dispose();
    }

    public static boolean isValidAETitle(String aet) {
        // Per PS 3.5: Default Character Repertoire excluding character code 5CH (the BACKSLASH “\” in ISO-IR 6), and control characters LF, FF, CR and ESC. 16 bytes maximum
        boolean good = true;
        if (aet == null) {
            good = false;
        }
        else if (aet.length() == 0) {
            good = false;
        }
        else if (aet.length() > 16) {
            good = false;
        }
        else if (aet.trim().length() == 0) {		// all whitespace is illegal
            good = false;
        }
        else if (aet.contains("\\")) {
            good = false;
        }
        else {
            int l = aet.length();
            for (int i=0; i<l; ++i) {
                int codePoint = aet.codePointAt(i);
                try {
                    Character.UnicodeBlock codeBlock = Character.UnicodeBlock.of(codePoint);
                    if (codeBlock != Character.UnicodeBlock.BASIC_LATIN) {
                        good = false;
                    }
                    else if (Character.isISOControl(codePoint)) {
                        good = false;
                    }
                }
                catch (IllegalArgumentException e) {	// if not a valid code point
                    good = false;
                }
            }
        }
        return good;
    }

    class AutoFocusTextField extends JTextField {

        AutoFocusTextField(String text, int columns) {
            super(text, columns);
            addFocusCallback();
        }

        AutoFocusTextField(final String initialText) {
            super(initialText);
            addFocusCallback();
        }

        private void addFocusCallback() {
            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent event) {
                    JTextComponent textComponent = (JTextComponent) (event.getSource());
                    textComponent.selectAll();
                }
            });
        }
    }

}
