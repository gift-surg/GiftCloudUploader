package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.network.ApplicationEntityConfigurationDialog;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Shows a dialog for setting the properties of the GIFT-Cloud Uplaoder app
 */
public class GiftCloudConfigurationDialog {

    private static int textFieldLengthForGiftCloudServerUrl = 32;

    private final GiftCloudUploaderController controller;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private ComboBoxModel<String> projectListModel;
    private final DicomNode dicomNode;
    private final GiftCloudDialogs giftCloudDialogs;
    private final JDialog dialog;

    private final JComboBox<String> projectList;
    private final JTextField giftCloudServerText;
    private final JTextField giftCloudUsernameText;
    private final JPasswordField giftCloudPasswordText;
    private JTextField listeningAETitleField;
    private JTextField listeningPortField;
    private JTextField patientListExportFolderField;
    private JTextField remoteAETitleField;
    private JTextField remoteAEHostName;
    private JTextField remoteAEPortField;

    private final Component componentToCenterDialogOver;
    private final TemporaryProjectListModel temporaryDropDownListModel;

    private boolean isDisposed = false;

    GiftCloudConfigurationDialog(final Dialog owner, final GiftCloudUploaderController controller, final GiftCloudPropertiesFromApplication giftCloudProperties, final ComboBoxModel<String> projectListModel, final DicomNode dicomNode, final ResourceBundle resourceBundle, final GiftCloudDialogs giftCloudDialogs) {
        this.controller = controller;
        this.giftCloudProperties = giftCloudProperties;
        this.projectListModel = projectListModel;
        this.dicomNode = dicomNode;
        this.giftCloudDialogs = giftCloudDialogs;
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
                JLabel giftCloudServerLabel = new JLabel(resourceBundle.getString("giftCloudServerText"), SwingConstants.RIGHT);
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

        GridBagLayout layout = new GridBagLayout();
        dialog.setLayout(layout);
        Container content = dialog.getContentPane();
        content.add(configPanel);
        dialog.pack();
        dialog.setVisible(true);
        dialog.pack();
    }

    public boolean isVisible() {
        return dialog.isDisplayable() && !isDisposed;
    }

    private class CloseActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (checkProperties()) {
                applyProperties();
                closeDialog();
            }
            // Otherwise force the user to fix the problem
        }
    }

    private class CancelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            closeDialog();
        }
    }

    private class ApplyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (checkProperties()) {
                applyProperties();
            }
            // Otherwise force the user to fix the problem
        }
    }

    private boolean checkProperties() {
        boolean propertiesOk = true;

        {
            String listeningAETitle = listeningAETitleField.getText();
            if (!StringUtils.isBlank(listeningAETitle) && !ApplicationEntityConfigurationDialog.isValidAETitle(listeningAETitle)) {
                propertiesOk = false;
                listeningAETitleField.setText("\\\\\\BAD\\\\\\");        // use backslash character here (which is illegal in AE's) to make sure this field is edited
            }
        }
        {
            int listeningPort = 0;
            try {
                listeningPort = Integer.parseInt(listeningPortField.getText());
                if (listeningPort < 1024) {
                    propertiesOk = false;
                    listeningPortField.setText("want >= 1024");
                }
            } catch (NumberFormatException e) {
                propertiesOk = false;
                listeningPortField.setText("\\\\\\BAD\\\\\\");
            }
        }
        {
            int remotePort = 0;
            try {
                remotePort = Integer.parseInt(remoteAEPortField.getText());
                if (remotePort < 1024) {
                    propertiesOk = false;
                    remoteAEPortField.setText("want >= 1024");
                }
            } catch (NumberFormatException e) {
                propertiesOk = false;
                remoteAEPortField.setText("\\\\\\BAD\\\\\\");
            }
        }

        {
            String remoteAETitle = remoteAETitleField.getText();
            if (!StringUtils.isBlank(remoteAETitle) && !ApplicationEntityConfigurationDialog.isValidAETitle(remoteAETitle)) {
                propertiesOk = false;
                remoteAETitleField.setText("\\\\\\BAD\\\\\\");        // use backslash character here (which is illegal in AE's) to make sure this field is edited
            }
        }
        return propertiesOk;
    }

    private void applyProperties() {

        // Extract out all the new values (this makes the code a little clearer below)
        final int newListeningPortValue = Integer.parseInt(listeningPortField.getText());
        final String newListeningAeTitle = listeningAETitleField.getText();
        final String newGiftCloudUrl = giftCloudServerText.getText();
        final String newGiftCloudUserName = giftCloudUsernameText.getText();
        final char[] newGiftCloudPassword = giftCloudPasswordText.getPassword();
        final String newPatientListExportFolder = patientListExportFolderField.getText();
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

        // Change the properties (must be done after we access the current values to check for changes)
        giftCloudProperties.setListeningPort(newListeningPortValue);
        giftCloudProperties.setListenerAETitle(newListeningAeTitle);
        giftCloudProperties.setPatientListExportFolder(newPatientListExportFolder);
        giftCloudProperties.setGiftCloudUrl(newGiftCloudUrl);
        giftCloudProperties.setLastUserName(newGiftCloudUserName);
        giftCloudProperties.setLastPassword(newGiftCloudPassword);
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

        try {
            giftCloudProperties.storeProperties("Saved from GiftCloudConfigurationDialog");
        } catch (IOException e) {

            // ToDo:
            e.printStackTrace();
        }


        if (restartDicomNode || restartUploader) {
            Cursor was = dialog.getCursor();
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (restartDicomNode) {
                controller.restartDicomService();
            }
            if (restartUploader) {
                controller.restartUploader();
            }

            dialog.setCursor(was);
        }

    }

    private void closeDialog() {
        isDisposed = true;
        dialog.dispose();
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
