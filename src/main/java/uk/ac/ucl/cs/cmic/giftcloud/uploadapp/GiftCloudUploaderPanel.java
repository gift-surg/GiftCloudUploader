package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.DatabaseTreeBrowser;
import com.pixelmed.database.DatabaseTreeRecord;
import com.pixelmed.dicom.DicomException;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 *
 */
public class GiftCloudUploaderPanel extends JPanel {

    // User interface components
    private final StatusPanel statusPanel;
    private final JPanel srcDatabasePanel;
    private final QueryRetrieveDialog remoteQueryRetrieveDialog;

    // Callback to the controller for invoking actions
    private final GiftCloudUploaderController controller;

    // Models for data selections by the user
    private Vector<String> currentSourceFilePathSelections;

    private GiftCloudPropertiesFromApplication giftCloudProperties;
    // Error reporting interface
    private final GiftCloudReporterFromApplication reporter;

    public GiftCloudUploaderPanel(final JFrame dialog, final GiftCloudUploaderController controller, final DatabaseInformationModel srcDatabase, final GiftCloudPropertiesFromApplication giftCloudProperties, final ResourceBundle resourceBundle, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) throws DicomException, IOException {
        super();
        this.controller = controller;
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;

        remoteQueryRetrieveDialog = new QueryRetrieveDialog(dialog, controller, resourceBundle);

        srcDatabasePanel = new JPanel();
        srcDatabasePanel.setLayout(new GridLayout(1, 1));
        new OurSourceDatabaseTreeBrowser(srcDatabase, srcDatabasePanel);

        Border panelBorder = BorderFactory.createEtchedBorder();

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

        JButton importPacsButton = new JButton(resourceBundle.getString("importPacsButtonLabelText"));
        importPacsButton.setToolTipText(resourceBundle.getString("importPacsButtonToolTipText"));
        buttonPanel.add(importPacsButton);
        importPacsButton.addActionListener(new ImportPacsActionListener());

        JButton exportButton = new JButton(resourceBundle.getString("exportButtonLabelText"));
        exportButton.setToolTipText(resourceBundle.getString("exportButtonToolTipText"));
        buttonPanel.add(exportButton);
        exportButton.addActionListener(new ExportActionListener());

        // Restart listener button
//        JButton restartListenerButton = new JButton(resourceBundle.getString("restartListenerButtonLabelText"));
//        restartListenerButton.setToolTipText(resourceBundle.getString("restartListenerButtonToolTipText"));
//        buttonPanel.add(restartListenerButton);
//        restartListenerButton.addActionListener(new RestartListenerActionListener());


        // Refresh button
//        JButton refreshButton = new JButton(resourceBundle.getString("refreshButtonLabelText"));
//        refreshButton.setToolTipText(resourceBundle.getString("refreshButtonToolTipText"));
//        buttonPanel.add(refreshButton);
//        refreshButton.addActionListener(new RefreshActionListener());


        statusPanel = new StatusPanel(controller, uploaderStatusModel);
        reporter.addProgressListener(statusPanel);

        {
            GridBagLayout mainPanelLayout = new GridBagLayout();
            setLayout(mainPanelLayout);
            {
                GridBagConstraints localBrowserPanesConstraints = new GridBagConstraints();
                localBrowserPanesConstraints.gridx = 0;
                localBrowserPanesConstraints.gridy = 0;
                localBrowserPanesConstraints.weightx = 1;
                localBrowserPanesConstraints.weighty = 1;
                localBrowserPanesConstraints.fill = GridBagConstraints.BOTH;
                mainPanelLayout.setConstraints(srcDatabasePanel,localBrowserPanesConstraints);
                add(srcDatabasePanel);
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
                GridBagConstraints statusBarPanelConstraints = new GridBagConstraints();
                statusBarPanelConstraints.gridx = 0;
                statusBarPanelConstraints.gridy = 2;
                statusBarPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                mainPanelLayout.setConstraints(statusPanel, statusBarPanelConstraints);
                add(statusPanel);
            }
        }
    }

    // Called when the database model has changed
    public void rebuildFileList(final DatabaseInformationModel srcDatabase) {
        srcDatabasePanel.removeAll();

        try {
            new OurSourceDatabaseTreeBrowser(srcDatabase, srcDatabasePanel);

        } catch (DicomException e) {
            reporter.error("Refresh of the file database failed: " + e.getLocalizedMessage(), e);
        }
        srcDatabasePanel.validate();
    }

    public QueryRetrieveRemoteView getQueryRetrieveRemoteView() {
        return remoteQueryRetrieveDialog.getQueryRetrieveRemoteView();
    }

    public void showQueryRetrieveDialog() {
        final Optional<String> queryHost = giftCloudProperties.getPacsHostName();
        if (!queryHost.isPresent() || StringUtils.isBlank(queryHost.get())) {
            reporter.showMessageToUser("Please set the PACS host name before importing from PACS.");
            controller.showConfigureDialog(false);
            return;
        }
        final Optional<String> queryCalledAETitle = giftCloudProperties.getPacsAeTitle();
        if (!queryCalledAETitle.isPresent() || StringUtils.isBlank(queryCalledAETitle.get())) {
            reporter.showMessageToUser("Please set the PACS AE title before performing importing from PACS.");
            controller.showConfigureDialog(false);
            return;
        }

        remoteQueryRetrieveDialog.setVisible(true);
    }

    private class ImportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
            controller.selectAndImport();
        }
	}

    private class ImportPacsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            showQueryRetrieveDialog();
        }
    }

    private class ExportActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.selectAndExport(currentSourceFilePathSelections);
        }
    }

    private class RestartListenerActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.restartDicomService();
        }
    }

    private class RefreshActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.refreshFileList();
        }
    }

    private class GiftCloudUploadActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.upload(currentSourceFilePathSelections);
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

    private class ConfigureActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                controller.showConfigureDialog(false);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
