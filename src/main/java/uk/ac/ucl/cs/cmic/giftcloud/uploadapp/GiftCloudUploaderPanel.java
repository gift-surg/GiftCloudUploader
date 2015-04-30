/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.DatabaseTreeBrowser;
import com.pixelmed.database.DatabaseTreeRecord;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.query.QueryInformationModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * <p>This class is an application for importing or retrieving DICOM studies,
 * cleaning them (i.e., de-identifying them or replacing UIDs, etc.), and
 * sending them elsewhere.</p>
 * 
 * <p>It is configured by use of a properties file that resides in the user's
 * home directory in <code>.uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderPanel.properties</code>.</p>
 * 
 * <p>It supports import and network retrieval of uncompressed, deflate and bzip compressed,
 * and baseline JPEG compressed images (but not yet other encapsulated compressed pixel data).</p>
 * 
 * @author	dclunie
 */
public class GiftCloudUploaderPanel extends JPanel {

    private static int textFieldLengthForGiftCloudServerUrl = 32;

    // User interface components
    private final StatusPanel statusPanel;
    private final JComboBox<String> projectList;
    private final JPanel srcDatabasePanel;
    private final JTextField giftCloudServerText;
    private final QueryRetrievePanel remoteQueryRetrievePanel;

    // Callback to the controller for invoking actions
    private final GiftCloudUploaderController controller;

    // Models for data selections by the user
    private Vector<String> currentSourceFilePathSelections;

    // Error reporting interface
    private final GiftCloudReporter reporter;

    public GiftCloudUploaderPanel(final GiftCloudUploaderController controller, final ComboBoxModel<String> projectListModel, final DatabaseInformationModel srcDatabase, final GiftCloudPropertiesFromApplication giftCloudProperties, final ResourceBundle resourceBundle, final GiftCloudReporter reporter) throws DicomException, IOException {
        super();
        this.controller = controller;
        this.reporter = reporter;

        remoteQueryRetrievePanel = new QueryRetrievePanel(controller, resourceBundle);

        srcDatabasePanel = new JPanel();
        srcDatabasePanel.setLayout(new GridLayout(1, 1));
        new OurSourceDatabaseTreeBrowser(srcDatabase, srcDatabasePanel);

        Border panelBorder = BorderFactory.createEtchedBorder();

        JSplitPane remoteAndLocalBrowserPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,remoteQueryRetrievePanel,srcDatabasePanel);
        remoteAndLocalBrowserPanes.setOneTouchExpandable(true);
        remoteAndLocalBrowserPanes.setResizeWeight(0.5);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(panelBorder);

        JButton configureButton = new JButton(resourceBundle.getString("configureButtonLabelText"));
        configureButton.setToolTipText(resourceBundle.getString("configureButtonToolTipText"));
        buttonPanel.add(configureButton);
        configureButton.addActionListener(new ConfigureActionListener());

        JButton importButton = new JButton(resourceBundle.getString("importButtonLabelText"));
        importButton.setToolTipText(resourceBundle.getString("importButtonToolTipText"));
        buttonPanel.add(importButton);
        importButton.addActionListener(new ImportActionListener());

        JButton giftCloudUploadButton = new JButton(resourceBundle.getString("giftCloudUploadButtonLabelText"));
        giftCloudUploadButton.setToolTipText(resourceBundle.getString("giftCloudUploadButtonToolTipText"));
        buttonPanel.add(giftCloudUploadButton);
        giftCloudUploadButton.addActionListener(new GiftCloudUploadActionListener());

        JPanel projectUploadPanel = new JPanel();
        projectUploadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        projectUploadPanel.setBorder(panelBorder);

        projectList = new JComboBox<String>();
        projectList.setEditable(false);
        projectList.setToolTipText(resourceBundle.getString("giftCloudProjectTooltip"));

        JLabel projectListLabel = new JLabel(resourceBundle.getString("giftCloudProjectLabelText"));
        projectUploadPanel.add(projectListLabel);
        projectUploadPanel.add(projectList);

        JLabel giftCloudServerLabel = new JLabel(resourceBundle.getString("giftCloudServerText"));
        giftCloudServerLabel.setToolTipText(resourceBundle.getString("giftCloudServerTextToolTipText"));


        giftCloudServerText = new AutoSaveTextField(giftCloudProperties.getGiftCloudUrl(), textFieldLengthForGiftCloudServerUrl) {
            @Override
            void autoSave() {
                giftCloudProperties.setGiftCloudUrl(getText());
            }
        };

        projectUploadPanel.add(giftCloudServerLabel);
        projectUploadPanel.add(giftCloudServerText);

        statusPanel = new StatusPanel();
        reporter.addProgressListener(statusPanel);

        {
            GridBagLayout mainPanelLayout = new GridBagLayout();
            setLayout(mainPanelLayout);
            {
                GridBagConstraints remoteAndLocalBrowserPanesConstraints = new GridBagConstraints();
                remoteAndLocalBrowserPanesConstraints.gridx = 0;
                remoteAndLocalBrowserPanesConstraints.gridy = 0;
                remoteAndLocalBrowserPanesConstraints.weightx = 1;
                remoteAndLocalBrowserPanesConstraints.weighty = 1;
                remoteAndLocalBrowserPanesConstraints.fill = GridBagConstraints.BOTH;
                mainPanelLayout.setConstraints(remoteAndLocalBrowserPanes,remoteAndLocalBrowserPanesConstraints);
                add(remoteAndLocalBrowserPanes);
            }
            {
                GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
                buttonPanelConstraints.gridx = 0;
                buttonPanelConstraints.gridy = 1;
                buttonPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                mainPanelLayout.setConstraints(buttonPanel,buttonPanelConstraints);
                add(buttonPanel);
            }
            {
                GridBagConstraints projectUploadPanelConstraints = new GridBagConstraints();
                projectUploadPanelConstraints.gridx = 0;
                projectUploadPanelConstraints.gridy = 3;
                projectUploadPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                mainPanelLayout.setConstraints(projectUploadPanel,projectUploadPanelConstraints);
                add(projectUploadPanel);
            }
            {
                GridBagConstraints statusBarPanelConstraints = new GridBagConstraints();
                statusBarPanelConstraints.gridx = 0;
                statusBarPanelConstraints.gridy = 6;
                statusBarPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                mainPanelLayout.setConstraints(statusPanel, statusBarPanelConstraints);
                add(statusPanel);
            }
        }

        projectList.setModel(projectListModel);
    }

    public QueryRetrieveRemoteView getQueryRetrievePanel() {
        return remoteQueryRetrievePanel.getQueryRetrievePanel();
    }

    // Called when the database model has changed
    public void rebuildFileList(final DatabaseInformationModel srcDatabase) {
        srcDatabasePanel.removeAll();

        try {
            new OurSourceDatabaseTreeBrowser(srcDatabase, srcDatabasePanel);

        } catch (DicomException e) {
            // ToDo
            reporter.updateStatusText("Refresh of the file database failed: " + e);
            e.printStackTrace();
        }
        srcDatabasePanel.validate();
    }

    public void updateQueryPanel(final QueryInformationModel queryInformationModel, final AttributeList filter, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomException, IOException, DicomNetworkException {
        remoteQueryRetrievePanel.updateQueryPanel(queryInformationModel, filter, currentRemoteQueryInformationModel);
    }

    private class ImportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
            controller.selectAndImport();
        }
	}

    private class GiftCloudUploadActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.upload(currentSourceFilePathSelections);
        }
    }

	private class ConfigureActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
                controller.showConfigureDialog();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

    private class OurSourceDatabaseTreeBrowser extends DatabaseTreeBrowser {
        public OurSourceDatabaseTreeBrowser(DatabaseInformationModel d,Container content) throws DicomException {
            super(d,content);
        }

        protected boolean doSomethingWithSelections(DatabaseTreeRecord[] selections) {
            return false;	// still want to call doSomethingWithSelectedFiles()
        }

        protected void doSomethingWithSelectedFiles(Vector<String> paths) {
            currentSourceFilePathSelections = paths;
        }
    }

}
