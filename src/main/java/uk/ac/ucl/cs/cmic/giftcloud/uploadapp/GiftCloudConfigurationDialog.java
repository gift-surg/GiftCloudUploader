package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.network.DicomNetworkException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Shows a dialog for setting the properties of the GIFT-Cloud Uplaoder app
 */
public class GiftCloudConfigurationDialog extends JDialog {

    private static int textFieldLengthForGiftCloudServerUrl = 32;

    private final GiftCloudUploaderController controller;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private DicomNode dicomNode;
    private GiftCloudDialogs giftCloudDialogs;
    private final JComboBox<String> projectList;
    private final JTextField giftCloudServerText;

    GiftCloudConfigurationDialog(final Dialog owner, final GiftCloudUploaderController controller, final GiftCloudPropertiesFromApplication giftCloudProperties, final ComboBoxModel<String> projectListModel, final DicomNode dicomNode, final ResourceBundle resourceBundle, final GiftCloudDialogs giftCloudDialogs) {
        super(owner);
        this.controller = controller;
        this.giftCloudProperties = giftCloudProperties;
        this.dicomNode = dicomNode;
        this.giftCloudDialogs = giftCloudDialogs;

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setTitle(resourceBundle.getString("configurationDialogTitle"));

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        Border panelBorder = BorderFactory.createEtchedBorder();

        {
            JPanel projectUploadPanel = new JPanel();
            GridBagLayout projectUploadlayout = new GridBagLayout();
            projectUploadPanel.setLayout(projectUploadlayout);

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

            // GIFT-Cloud server URL

            labelConstraints.gridy = 0;
            JLabel giftCloudServerLabel = new JLabel(resourceBundle.getString("giftCloudServerText"));
            giftCloudServerLabel.setToolTipText(resourceBundle.getString("giftCloudServerTextToolTipText"));
            projectUploadlayout.setConstraints(giftCloudServerLabel, labelConstraints);
            projectUploadPanel.add(giftCloudServerLabel);

            giftCloudServerText = new AutoSaveTextField(giftCloudProperties.getGiftCloudUrl(), textFieldLengthForGiftCloudServerUrl) {
                @Override
                void autoSave() {
                    giftCloudProperties.setGiftCloudUrl(getText());
                }
            };
            inputConstraints.gridy = 0;
            projectUploadlayout.setConstraints(giftCloudServerText, inputConstraints);
            projectUploadPanel.add(giftCloudServerText);

            // Project list

            labelConstraints.gridy = 1;
            JLabel projectListLabel = new JLabel(resourceBundle.getString("giftCloudProjectLabelText"));
            projectUploadlayout.setConstraints(projectListLabel, labelConstraints);
            projectUploadPanel.add(projectListLabel);

            inputConstraints.gridy = 1;
            projectList = new JComboBox<String>();
            projectList.setEditable(false);
            projectList.setToolTipText(resourceBundle.getString("giftCloudProjectTooltip"));
            projectUploadlayout.setConstraints(projectList, inputConstraints);
            projectUploadPanel.add(projectList);



            GridBagConstraints projectPanelConstraints = new GridBagConstraints();
            projectPanelConstraints.gridx = 0;
            projectPanelConstraints.gridy = 0;
            projectPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            layout.setConstraints(projectUploadPanel, projectPanelConstraints);
            add(projectUploadPanel);
        }

        {
            JPanel networkPanel = new JPanel();
            networkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            JButton configureButton = new JButton(resourceBundle.getString("pacsConfigureButtonLabelText"));
            configureButton.setToolTipText(resourceBundle.getString("pacsConfigureButtonToolTipText"));
            configureButton.addActionListener(new PacsConfigureActionListener());

            networkPanel.add(configureButton);

            GridBagConstraints networkPanelConstraints = new GridBagConstraints();
            networkPanelConstraints.gridx = 0;
            networkPanelConstraints.gridy = 1;
            networkPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            layout.setConstraints(networkPanel, networkPanelConstraints);
            add(networkPanel);
        }

        {
            JPanel closeButtonPanel = new JPanel();
            closeButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            closeButtonPanel.setBorder(panelBorder);

            JButton closeButton = new JButton(resourceBundle.getString("closeSettingsButtonLabelText"));
            closeButton.setToolTipText(resourceBundle.getString("closeSettingsButtonToolTipText"));
            closeButtonPanel.add(closeButton);
            closeButton.addActionListener(new CloseActionListener());

            GridBagConstraints closePanelConstraints = new GridBagConstraints();
            closePanelConstraints.gridx = 0;
            closePanelConstraints.gridy = 2;
            closePanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            layout.setConstraints(closeButtonPanel, closePanelConstraints);
            add(closeButtonPanel);
        }

        projectList.setModel(projectListModel);

        pack();
    }

    public void showPacsConfigureDialog() throws IOException, DicomNode.DicomNodeStartException {
        dicomNode.shutdownStorageSCP();
        try {
            new NetworkApplicationConfigurationDialog(this, dicomNode.getNetworkApplicationInformation(), giftCloudProperties, giftCloudDialogs);
        } catch (DicomNetworkException e) {
            throw new IOException("Failed to create configuration dialog due to error: " + e.getCause());
        }
        // should now save properties to file
        giftCloudProperties.updatePropertiesWithNetworkProperties();
        giftCloudProperties.storeProperties("Edited and saved from user interface");
        dicomNode.activateStorageSCP();
    }


    private class PacsConfigureActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                showPacsConfigureDialog();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private class CloseActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            setVisible(false);
        }
    }
}
