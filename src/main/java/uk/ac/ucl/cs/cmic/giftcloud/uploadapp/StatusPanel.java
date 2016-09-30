/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.VersionAndConstants;
import com.pixelmed.display.SafeProgressBarUpdaterThread;
import com.pixelmed.display.StatusBarManager;
import uk.ac.ucl.cs.cmic.giftcloud.util.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StatusPanel extends JPanel implements Progress, UploaderStatusModel.StatusListener {

    private final SafeProgressBarUpdaterThread progressBarUpdater;
    private final JProgressBar progressBar;
    private final JLabel statusBar;
    private final JLabel uploaderServiceStatusText;
    private final JLabel importerServiceStatusText;
    private final StatusBarManager statusBarManager;		// maintain a strong reference else weak reference to listener gets nulled when garbage collected
    private boolean isCancelled = false;
    private UploaderGuiController controller;

    StatusPanel(final UploaderGuiController controller, final UploaderStatusModel uploaderStatusModel) {
        this.controller = controller;
        statusBarManager = new StatusBarManager(getBuildDate()+" "+getReleaseString());
        statusBar = getStatusBar();
        GridBagLayout statusBarPanelLayout = new GridBagLayout();
        setLayout(statusBarPanelLayout);
        {
            importerServiceStatusText = new JLabel("GIFT-Cloud Uploader image importer service");
            GridBagConstraints uploaderServiceStatusBarConstraints = new GridBagConstraints();
            uploaderServiceStatusBarConstraints.gridx = 0;
            uploaderServiceStatusBarConstraints.gridy = 0;
            uploaderServiceStatusBarConstraints.weightx = 1;
            uploaderServiceStatusBarConstraints.fill = GridBagConstraints.HORIZONTAL;
            uploaderServiceStatusBarConstraints.anchor = GridBagConstraints.WEST;
            uploaderServiceStatusBarConstraints.gridwidth = GridBagConstraints.RELATIVE;
            add(importerServiceStatusText, uploaderServiceStatusBarConstraints);
        }

        {
            uploaderServiceStatusText = new JLabel("GIFT-Cloud Uploader uploader service");
            GridBagConstraints uploaderServiceStatusBarConstraints = new GridBagConstraints();
            uploaderServiceStatusBarConstraints.gridx = 0;
            uploaderServiceStatusBarConstraints.gridy = 1;
            uploaderServiceStatusBarConstraints.weightx = 1;
            uploaderServiceStatusBarConstraints.fill = GridBagConstraints.HORIZONTAL;
            uploaderServiceStatusBarConstraints.anchor = GridBagConstraints.WEST;
            uploaderServiceStatusBarConstraints.gridwidth = GridBagConstraints.RELATIVE;
            add(uploaderServiceStatusText, uploaderServiceStatusBarConstraints);
        }
        {
            GridBagConstraints statusBarConstraints = new GridBagConstraints();
            statusBarConstraints.gridx = 0;
            statusBarConstraints.gridy = 2;
            statusBarConstraints.weightx = 1;
            statusBarConstraints.fill = GridBagConstraints.HORIZONTAL;
            statusBarConstraints.anchor = GridBagConstraints.WEST;
            statusBarConstraints.gridwidth = GridBagConstraints.RELATIVE;
//            add(statusBar, statusBarConstraints);
        }
        {
            progressBar = new JProgressBar();		// local not class scope; helps detect when being accessed other than through SafeProgressBarUpdaterThread
            progressBar.setStringPainted(false);
            GridBagConstraints progressBarConstraints = new GridBagConstraints();
//            progressBarConstraints.gridx = 0;
            progressBarConstraints.gridy = 2;
            progressBarConstraints.weightx = 0.5;
            progressBarConstraints.fill = GridBagConstraints.HORIZONTAL;
            progressBarConstraints.anchor = GridBagConstraints.EAST;
            progressBarConstraints.gridwidth = GridBagConstraints.REMAINDER;
//            add(progressBar, progressBarConstraints);

            progressBarUpdater = new SafeProgressBarUpdaterThread(progressBar);
        }
//        {
//            JButton cancelButton = new JButton("Cancel");
//            cancelButton.setToolTipText("Cancel current task");
//            add(cancelButton);
//            cancelButton.addActionListener(new CancelActionListener());
//
//        }

        uploaderStatusModel.addListener(this);
    }

    @Override
    public void startProgressBar() {
        SafeProgressBarUpdaterThread.startProgressBar(progressBarUpdater);
    }

    @Override
    public void startProgressBar(int maximum) {
        SafeProgressBarUpdaterThread.startProgressBar(progressBarUpdater, maximum);
    }

    @Override
    public void endProgressBar() {
        SafeProgressBarUpdaterThread.endProgressBar(progressBarUpdater);
    }

    @Override
    public void updateStatusText(String progressText) {
        statusBar.setText(progressText);
        SafeProgressBarUpdaterThread.updateStatusText(progressBarUpdater, progressText);
    }

    @Override
    public boolean isCancelled() {
        return getAndResetCancellation();
    }

    @Override
    public void updateProgressBar(int value, int maximum) {
        SafeProgressBarUpdaterThread.updateProgressBar(progressBarUpdater, value, maximum);
    }

    @Override
    public void updateProgressBar(int value) {
        SafeProgressBarUpdaterThread.updateProgressBar(progressBarUpdater, value);
    }


    /**
     * <p>Get the release string for this application.</p>
     *
     * @return	 the release string
     */
    protected static String getReleaseString() {
        return VersionAndConstants.releaseString;
    }

    /**
     * <p>Get the date the package was built.</p>
     *
     * @return	 the build date
     */
    protected static String getBuildDate() {
        return VersionAndConstants.getBuildDate();
    }

    /**
     * <p>Setup a StatusBarManager and return its StatusBar.</p>
     *
     * <p>The initial string in the StatusBar is composed of the build date and release string.</p>
     *
     * @return	 the StatusBar
     */
    protected JLabel getStatusBar() {
        return statusBarManager.getStatusBar();
    }

    private synchronized boolean getAndResetCancellation() {
        boolean returnValue = isCancelled;
        isCancelled = false;
        return returnValue;
    }

    private synchronized void Cancel() {
        isCancelled = true;
    }

    @Override
    public void uploaderStatusMessageChanged(final String newMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                uploaderServiceStatusText.setText(newMessage);
            }
        });
    }

    @Override
    public void importerStatusMessageChanged(final String newMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                importerServiceStatusText.setText(newMessage);
            }
        });
    }

    protected class CancelActionListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            Cancel();
        }
    }

}
