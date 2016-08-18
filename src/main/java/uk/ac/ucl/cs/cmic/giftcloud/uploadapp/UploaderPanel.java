package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * The main dialog panel for the GIFT-Cloud Uploader application
 */
public class UploaderPanel {

    // User interface components
    private final JFrame parentFrame;
    private JPanel basePanel;
    private StatusPanel statusPanel;
    private JPanel srcDatabasePanel;

    // Callback to the controller for invoking actions
    private final UploaderGuiController controller;

    private TableModel tableModel;
    private ResourceBundle resourceBundle;
    private UploaderStatusModel uploaderStatusModel;
    private final GiftCloudReporterFromApplication reporter;

    public UploaderPanel(final MainFrame mainFrame, final UploaderGuiController controller, final TableModel tableModel, final ResourceBundle resourceBundle, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) throws InvocationTargetException, InterruptedException {
        this.controller = controller;
        this.tableModel = tableModel;
        this.resourceBundle = resourceBundle;
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;
        parentFrame = mainFrame.getParent();

        GiftCloudUtils.runNowOnEdt(new Runnable() {
            @Override
            public void run() {
                createPanel();
                mainFrame.addMainPanel(basePanel);
            }
        });
    }

    private void createPanel() {
        basePanel = new JPanel();

        new FileDrop(parentFrame, new FileDrop.Listener()
        {
            public void filesDropped(final java.io.File[] files) {
                controller.runImport(Arrays.asList(files), true, reporter);
            }
        });

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
            basePanel.setLayout(mainPanelLayout);

            {
                GridBagConstraints combinedPanelConstraints = new GridBagConstraints();
                combinedPanelConstraints.gridx = 0;
                combinedPanelConstraints.gridy = 0;
                combinedPanelConstraints.fill = GridBagConstraints.BOTH;
                combinedPanelConstraints.insets = new Insets(5, 5, 5, 5);
                mainPanelLayout.setConstraints(combinedPanel, combinedPanelConstraints);
                basePanel.add(combinedPanel);
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
                basePanel.add(separator);
            }
            {
                GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
                buttonPanelConstraints.gridx = 0;
                buttonPanelConstraints.gridy = 2;
                buttonPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                buttonPanelConstraints.insets = new Insets(5, 5, 5, 5);
                mainPanelLayout.setConstraints(buttonPanel, buttonPanelConstraints);
                basePanel.add(buttonPanel);
            }
        }
    }

    private class ImportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
            controller.selectAndImport();
        }
	}

    private class ImportPacsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.importFromPacs();
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
