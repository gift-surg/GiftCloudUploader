package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PixelDataAnonymiserFilterCache;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * The main dialog panel for the GIFT-Cloud Uploader application
 */
public class GiftCloudUploaderPanel extends JPanel {

    // User interface components
    private final StatusPanel statusPanel;
    private final JPanel srcDatabasePanel;
    private final QueryRetrieveDialog remoteQueryRetrieveDialog;

    // Callback to the controller for invoking actions
    private final GiftCloudUploaderController controller;

    private GiftCloudPropertiesFromApplication giftCloudProperties;
    // Error reporting interface
    private final GiftCloudReporterFromApplication reporter;

    public GiftCloudUploaderPanel(final JFrame dialog, final GiftCloudUploaderController controller, final TableModel tableModel, final PixelDataAnonymiserFilterCache filters, final GiftCloudPropertiesFromApplication giftCloudProperties, final ResourceBundle resourceBundle, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) throws DicomException, IOException {
        super();
        this.controller = controller;
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;

        new FileDrop(dialog, new FileDrop.Listener()
        {
            public void filesDropped(final java.io.File[] files) {
                controller.runImport(Arrays.asList(files), true, reporter);
            }
        });

        remoteQueryRetrieveDialog = new QueryRetrieveDialog(dialog, controller, resourceBundle);

        JPanel combinedPanel = new JPanel();

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        srcDatabasePanel = new JPanel();
        srcDatabasePanel.setLayout(new GridLayout(1, 1));
        srcDatabasePanel.add(scrollPane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

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

        JButton pixelDataButton = new JButton(resourceBundle.getString("pixelDataButtonLabelText"));
        pixelDataButton.setToolTipText(resourceBundle.getString("pixelDataButtonToolTipText"));
        buttonPanel.add(pixelDataButton);
        pixelDataButton.addActionListener(new ConfigurePixelDataAnonymisationActionListener());

        JButton closeButton = new JButton(resourceBundle.getString("closeButtonLabelText"));
        closeButton.setToolTipText(resourceBundle.getString("closeButtonToolTipText"));
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new CloseActionListener());

        // Restart listener button
//        JButton restartListenerButton = new JButton(resourceBundle.getString("restartListenerButtonLabelText"));
//        restartListenerButton.setToolTipText(resourceBundle.getString("restartListenerButtonToolTipText"));
//        buttonPanel.add(restartListenerButton);
//        restartListenerButton.addActionListener(new RestartListenerActionListener());

        statusPanel = new StatusPanel(controller, uploaderStatusModel);
        reporter.addProgressListener(statusPanel);

        {
            GridBagLayout combinedPanelLayout = new GridBagLayout();
            combinedPanel.setLayout(combinedPanelLayout);
            {
                GridBagConstraints statusBarPanelConstraints = new GridBagConstraints();
                statusBarPanelConstraints.gridx = 0;
                statusBarPanelConstraints.gridy = 0;
                statusBarPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                statusBarPanelConstraints.insets = new Insets(5, 5, 5, 5);
                combinedPanelLayout.setConstraints(statusPanel, statusBarPanelConstraints);
                combinedPanel.add(statusPanel);
            }
            {
                GridBagConstraints localBrowserPanesConstraints = new GridBagConstraints();
                localBrowserPanesConstraints.gridx = 0;
                localBrowserPanesConstraints.gridy = 1;
                localBrowserPanesConstraints.weightx = 1;
                localBrowserPanesConstraints.weighty = 1;
                localBrowserPanesConstraints.insets = new Insets(5, 5, 5, 5);
                localBrowserPanesConstraints.fill = GridBagConstraints.BOTH;
                combinedPanelLayout.setConstraints(srcDatabasePanel,localBrowserPanesConstraints);
                combinedPanel.add(srcDatabasePanel);
            }

            GridBagLayout mainPanelLayout = new GridBagLayout();
            setLayout(mainPanelLayout);

            {
                GridBagConstraints combinedPanelConstraints = new GridBagConstraints();
                combinedPanelConstraints.gridx = 0;
                combinedPanelConstraints.gridy = 0;
                combinedPanelConstraints.fill = GridBagConstraints.BOTH;
                combinedPanelConstraints.insets = new Insets(5, 5, 5, 5);
                mainPanelLayout.setConstraints(combinedPanel, combinedPanelConstraints);
                add(combinedPanel);
            }
            {
                GridBagConstraints separatorConstraint = new GridBagConstraints();
                separatorConstraint.gridx = 0;
                separatorConstraint.gridy = 1;
                separatorConstraint.weightx = 1.0;
                separatorConstraint.fill = GridBagConstraints.HORIZONTAL;
                separatorConstraint.gridwidth = GridBagConstraints.REMAINDER;
                JSeparator separator = new JSeparator();
                mainPanelLayout.setConstraints(separator,separatorConstraint);
                add(separator);
            }
            {
                GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
                buttonPanelConstraints.gridx = 0;
                buttonPanelConstraints.gridy = 2;
                buttonPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                buttonPanelConstraints.insets = new Insets(5, 5, 5, 5);
                mainPanelLayout.setConstraints(buttonPanel, buttonPanelConstraints);
                add(buttonPanel);
            }
        }
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
            reporter.showMessageToUser("Please set the PACS AE title before importing from PACS.");
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

    private class RestartListenerActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.restartDicomService();
        }
    }

    private class ConfigurePixelDataAnonymisationActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                controller.showPixelDataTemplateDialog();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    protected class CloseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            controller.hide();
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
