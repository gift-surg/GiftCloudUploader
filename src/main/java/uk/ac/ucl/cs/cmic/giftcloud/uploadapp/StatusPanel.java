package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.VersionAndConstants;
import com.pixelmed.display.SafeProgressBarUpdaterThread;
import com.pixelmed.display.StatusBarManager;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StatusPanel extends JPanel implements Progress {

    private final SafeProgressBarUpdaterThread progressBarUpdater;
    private final JProgressBar progressBar;
    private final JLabel statusBar;
    private final StatusBarManager statusBarManager;		// maintain a strong reference else weak reference to listener gets nulled when garbage collected
    private boolean isCancelled = false;
    private GiftCloudUploaderController controller;

    StatusPanel(final GiftCloudUploaderController controller) {
        this.controller = controller;
        statusBarManager = new StatusBarManager(getBuildDate()+" "+getReleaseString());
        statusBar = getStatusBar();
        GridBagLayout statusBarPanelLayout = new GridBagLayout();
        setLayout(statusBarPanelLayout);
        {
            GridBagConstraints statusBarConstraints = new GridBagConstraints();
            statusBarConstraints.weightx = 1;
            statusBarConstraints.fill = GridBagConstraints.BOTH;
            statusBarConstraints.anchor = GridBagConstraints.WEST;
            statusBarConstraints.gridwidth = GridBagConstraints.RELATIVE;
            add(statusBar, statusBarConstraints);
        }
        {
            progressBar = new JProgressBar();		// local not class scope; helps detect when being accessed other than through SafeProgressBarUpdaterThread
            progressBar.setStringPainted(false);
            GridBagConstraints progressBarConstraints = new GridBagConstraints();
            progressBarConstraints.weightx = 0.5;
            progressBarConstraints.fill = GridBagConstraints.BOTH;
            progressBarConstraints.anchor = GridBagConstraints.EAST;
            progressBarConstraints.gridwidth = GridBagConstraints.REMAINDER;
            add(progressBar, progressBarConstraints);

            progressBarUpdater = new SafeProgressBarUpdaterThread(progressBar);
        }
        {
            JButton closeButton = new JButton("Close");
            closeButton.setToolTipText("Close the uploader window");
            add(closeButton);
            GridBagConstraints closeButtonConstraints = new GridBagConstraints();
            closeButtonConstraints.weightx = 1;
            closeButtonConstraints.fill = GridBagConstraints.BOTH;
            closeButtonConstraints.anchor = GridBagConstraints.EAST;
            closeButtonConstraints.gridwidth = GridBagConstraints.RELATIVE;
            closeButton.addActionListener(new CloseActionListener());

        }
//        {
//            JButton cancelButton = new JButton("Cancel");
//            cancelButton.setToolTipText("Cancel current task");
//            add(cancelButton);
//            cancelButton.addActionListener(new CancelActionListener());
//
//        }
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

    protected class CancelActionListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            Cancel();
        }
    }
    protected class CloseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            controller.hide();
        }
    }

}
