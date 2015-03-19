package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.display.SafeProgressBarUpdaterThread;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel implements Progress {

    protected SafeProgressBarUpdaterThread progressBarUpdater;
    private JProgressBar progressBar;

    StatusPanel(JLabel statusBar) {
        GridBagLayout statusBarPanelLayout = new GridBagLayout();
        setLayout(statusBarPanelLayout);
        {
            GridBagConstraints statusBarConstraints = new GridBagConstraints();
            statusBarConstraints.weightx = 1;
            statusBarConstraints.fill = GridBagConstraints.BOTH;
            statusBarConstraints.anchor = GridBagConstraints.WEST;
            statusBarConstraints.gridwidth = GridBagConstraints.RELATIVE;
            statusBarPanelLayout.setConstraints(statusBar, statusBarConstraints);
            add(statusBar);
        }
        {
            progressBar = new JProgressBar();		// local not class scope; helps detect when being accessed other than through SafeProgressBarUpdaterThread
            progressBar.setStringPainted(false);
            GridBagConstraints progressBarConstraints = new GridBagConstraints();
            progressBarConstraints.weightx = 0.5;
            progressBarConstraints.fill = GridBagConstraints.BOTH;
            progressBarConstraints.anchor = GridBagConstraints.EAST;
            progressBarConstraints.gridwidth = GridBagConstraints.REMAINDER;
            statusBarPanelLayout.setConstraints(progressBar,progressBarConstraints);
            add(progressBar);

            progressBarUpdater = new SafeProgressBarUpdaterThread(progressBar);
        }
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
    public void updateProgressBar(int value, int maximum) {
        SafeProgressBarUpdaterThread.updateProgressBar(progressBarUpdater, value, maximum);
    }

    @Override
    public void updateProgressBar(int value) {
        SafeProgressBarUpdaterThread.updateProgressBar(progressBarUpdater, value);
    }


    // This is to deal with the Media Importer, which takes in an actual JProgressBar
    public JProgressBar getProgressBar() {
        return progressBar;
    }
}
