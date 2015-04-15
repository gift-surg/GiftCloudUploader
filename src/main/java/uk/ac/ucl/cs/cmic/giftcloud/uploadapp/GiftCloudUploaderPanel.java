/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.DatabaseTreeBrowser;
import com.pixelmed.database.DatabaseTreeRecord;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.QueryTreeBrowser;
import com.pixelmed.query.QueryTreeModel;
import com.pixelmed.query.QueryTreeRecord;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

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

    private static int textFieldLengthForQueryPatientName = 16;
    private static int textFieldLengthForQueryPatientID = 10;
    private static int textFieldLengthForQueryStudyDate = 8;
    private static int textFieldLengthForQueryAccessionNumber = 10;
    private static int textFieldLengthForGiftCloudServerUrl = 32;

    // User interface components
    private final StatusPanel statusPanel;
    private final JComboBox<String> projectList;
    private final JPanel srcDatabasePanel;
    private final JPanel remoteQueryRetrievePanel;
    private final JTextField giftCloudServerText;
    private final JTextField queryFilterPatientNameTextField;
    private final JTextField queryFilterPatientIDTextField;
    private final JTextField queryFilterStudyDateTextField;
    private final JTextField queryFilterAccessionNumberTextField;

    // Callback to the controller for invoking actions
    private final GiftCloudUploaderController controller;

    // Models for data selections by the user
    private List<QuerySelection> currentRemoteQuerySelectionList;
    private Vector<String> currentSourceFilePathSelections;

    // Error reporting interface
    private final GiftCloudReporter reporter;

    public GiftCloudUploaderPanel(final GiftCloudUploaderController controller, final ComboBoxModel<String> projectListModel, final DatabaseInformationModel srcDatabase, final GiftCloudPropertiesFromApplication giftCloudProperties, final ResourceBundle resourceBundle, final GiftCloudDialogs giftCloudDialogs, final String buildDate, final JLabel statusBar, final GiftCloudReporter reporter) throws DicomException, IOException {
        super();
        this.controller = controller;
        this.reporter = reporter;

        srcDatabasePanel = new JPanel();
        remoteQueryRetrievePanel = new JPanel();

        srcDatabasePanel.setLayout(new GridLayout(1,1));
        remoteQueryRetrievePanel.setLayout(new GridLayout(1,1));

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

        JButton queryButton = new JButton(resourceBundle.getString("queryButtonLabelText"));
        queryButton.setToolTipText(resourceBundle.getString("queryButtonToolTipText"));
        buttonPanel.add(queryButton);
        queryButton.addActionListener(new QueryActionListener());

        JButton retrieveButton = new JButton(resourceBundle.getString("retrieveButtonLabelText"));
        retrieveButton.setToolTipText(resourceBundle.getString("retrieveButtonToolTipText"));
        buttonPanel.add(retrieveButton);
        retrieveButton.addActionListener(new RetrieveActionListener());

        JButton importButton = new JButton(resourceBundle.getString("importButtonLabelText"));
        importButton.setToolTipText(resourceBundle.getString("importButtonToolTipText"));
        buttonPanel.add(importButton);
        importButton.addActionListener(new ImportActionListener());

        JButton giftCloudUploadButton = new JButton(resourceBundle.getString("giftCloudUploadButtonLabelText"));
        giftCloudUploadButton.setToolTipText(resourceBundle.getString("giftCloudUploadButtonToolTipText"));
        buttonPanel.add(giftCloudUploadButton);
        giftCloudUploadButton.addActionListener(new GiftCloudUploadActionListener());

        JPanel queryFilterTextEntryPanel = new JPanel();
        queryFilterTextEntryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        queryFilterTextEntryPanel.setBorder(panelBorder);

        JLabel queryIntroduction = new JLabel(resourceBundle.getString("queryIntroductionLabelText"));
        queryFilterTextEntryPanel.add(queryIntroduction);

        JLabel queryFilterPatientNameLabel = new JLabel(resourceBundle.getString("queryPatientNameLabelText"));
        queryFilterPatientNameLabel.setToolTipText(resourceBundle.getString("queryPatientNameToolTipText"));
        queryFilterTextEntryPanel.add(queryFilterPatientNameLabel);
        queryFilterPatientNameTextField = new JTextField("",textFieldLengthForQueryPatientName);
        queryFilterTextEntryPanel.add(queryFilterPatientNameTextField);

        JLabel queryFilterPatientIDLabel = new JLabel(resourceBundle.getString("queryPatientIDLabelText"));
        queryFilterPatientIDLabel.setToolTipText(resourceBundle.getString("queryPatientIDToolTipText"));
        queryFilterTextEntryPanel.add(queryFilterPatientIDLabel);
        queryFilterPatientIDTextField = new JTextField("",textFieldLengthForQueryPatientID);
        queryFilterTextEntryPanel.add(queryFilterPatientIDTextField);

        JLabel queryFilterStudyDateLabel = new JLabel(resourceBundle.getString("queryStudyDateLabelText"));
        queryFilterStudyDateLabel.setToolTipText(resourceBundle.getString("queryStudyDateToolTipText"));
        queryFilterTextEntryPanel.add(queryFilterStudyDateLabel);
        queryFilterStudyDateTextField = new JTextField("",textFieldLengthForQueryStudyDate);
        queryFilterTextEntryPanel.add(queryFilterStudyDateTextField);

        JLabel queryFilterAccessionNumberLabel = new JLabel(resourceBundle.getString("queryAccessionNumberLabelText"));
        queryFilterAccessionNumberLabel.setToolTipText(resourceBundle.getString("queryAccessionNumberToolTipText"));
        queryFilterTextEntryPanel.add(queryFilterAccessionNumberLabel);
        queryFilterAccessionNumberTextField = new JTextField("",textFieldLengthForQueryAccessionNumber);
        queryFilterTextEntryPanel.add(queryFilterAccessionNumberTextField);



        JPanel newTextEntryPanel = new JPanel();
        newTextEntryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        newTextEntryPanel.setBorder(panelBorder);

        projectList = new JComboBox<String>();
        projectList.setEditable(false);
        projectList.setToolTipText(resourceBundle.getString("giftCloudProjectTooltip"));

        JLabel projectListLabel = new JLabel(resourceBundle.getString("giftCloudProjectLabelText"));
        newTextEntryPanel.add(projectListLabel);
        newTextEntryPanel.add(projectList);

        JLabel giftCloudServerLabel = new JLabel(resourceBundle.getString("giftCloudServerText"));
        giftCloudServerLabel.setToolTipText(resourceBundle.getString("giftCloudServerTextToolTipText"));


        giftCloudServerText = new AutoSaveTextField(giftCloudProperties.getGiftCloudUrl(), textFieldLengthForGiftCloudServerUrl) {
            @Override
            void autoSave() {
                giftCloudProperties.setGiftCloudUrl(getText());
            }
        };

        newTextEntryPanel.add(giftCloudServerLabel);
        newTextEntryPanel.add(giftCloudServerText);

        statusPanel = new StatusPanel(statusBar);
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
                GridBagConstraints queryFilterTextEntryPanelConstraints = new GridBagConstraints();
                queryFilterTextEntryPanelConstraints.gridx = 0;
                queryFilterTextEntryPanelConstraints.gridy = 2;
                queryFilterTextEntryPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                mainPanelLayout.setConstraints(queryFilterTextEntryPanel,queryFilterTextEntryPanelConstraints);
                add(queryFilterTextEntryPanel);
            }
            {
                GridBagConstraints newTextEntryPanelConstraints = new GridBagConstraints();
                newTextEntryPanelConstraints.gridx = 0;
                newTextEntryPanelConstraints.gridy = 3;
                newTextEntryPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                mainPanelLayout.setConstraints(newTextEntryPanel,newTextEntryPanelConstraints);
                add(newTextEntryPanel);
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

    // Called when the database model has changed
    public void rebuildFileList(final DatabaseInformationModel srcDatabase) {
        srcDatabasePanel.removeAll();

        try {
            new OurSourceDatabaseTreeBrowser(srcDatabase, srcDatabasePanel);

        } catch (DicomException e) {
            // ToDo
            reporter.updateProgress("Refresh of the file database failed: " + e);
            e.printStackTrace();
        }
        srcDatabasePanel.validate();
    }

    public void updateQueryPanel(final QueryInformationModel queryInformationModel, final AttributeList filter, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomException, IOException, DicomNetworkException {
        QueryTreeModel treeModel = queryInformationModel.performHierarchicalQuery(filter);
        new GiftCloudUploaderPanel.OurQueryTreeBrowser(queryInformationModel, treeModel, remoteQueryRetrievePanel, currentRemoteQueryInformationModel);
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

    private class QueryActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

            final QueryParams queryParams = new QueryParams();

            String patientName = queryFilterPatientNameTextField.getText().trim();
            if (patientName != null && patientName.length() > 0) {
                queryParams.setPatientName(patientName);
            }

            String patientID = queryFilterPatientIDTextField.getText().trim();
            if (patientID != null && patientID.length() > 0) {
                queryParams.setPatientId(patientID);
            }

            String accessionNumber = queryFilterAccessionNumberTextField.getText().trim();
            if (accessionNumber != null && accessionNumber.length() > 0) {
                queryParams.setAccessionNumber(accessionNumber);
            }

            String studyDate = queryFilterStudyDateTextField.getText().trim();
            if (studyDate != null && studyDate.length() > 0) {
                queryParams.setStudyDate(studyDate);
            }

            remoteQueryRetrievePanel.removeAll();

            controller.query(queryParams);

			remoteQueryRetrievePanel.validate();
		}
	}

    private class RetrieveActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
            controller.retrieve(currentRemoteQuerySelectionList);
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

    private class OurQueryTreeBrowser extends QueryTreeBrowser {
        private QueryInformationModel currentRemoteQueryInformationModel;

        /**
         * @param	q
         * @param	m
         * @param	content
         * @throws	DicomException
         */
        OurQueryTreeBrowser(QueryInformationModel q,QueryTreeModel m,Container content, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomException {
            super(q,m,content);
            this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        }

        /***/
        protected TreeSelectionListener buildTreeSelectionListenerToDoSomethingWithSelectedLevel() {
            return new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent tse) {

                    // Store all the selected paths
                    QueryTreeRecord[] records = getSelectionPaths();
                    List<QuerySelection> remoteQuerySelectionList = new ArrayList<QuerySelection>();
                    if (records != null) {
                        for (QueryTreeRecord record : records) {
                            remoteQuerySelectionList.add(new QuerySelection(record, currentRemoteQueryInformationModel));
                        }
                    }
                    currentRemoteQuerySelectionList = remoteQuerySelectionList;
                }
            };
        }
    }
}
