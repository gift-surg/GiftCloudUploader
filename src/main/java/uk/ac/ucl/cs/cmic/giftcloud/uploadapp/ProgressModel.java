package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.Progress;

import java.util.ArrayList;
import java.util.List;

class ProgressModel {
    private final List<Progress> listeners = new ArrayList<Progress>();

    void addListener(final Progress progress) {
        listeners.add(progress);
    }

    public void startProgress() {
        for (Progress progress : listeners) {
            progress.startProgressBar();
        }
    }

    public void endProgress() {
        for (Progress progress : listeners) {
            progress.endProgressBar();
        }
    }

    public void startProgress(int i) {
        for (Progress progress : listeners) {
            progress.startProgressBar(i);
        }
    }

    public void updateProgressBar(int value) {
        for (Progress progress : listeners) {
            progress.updateProgressBar(value);
        }
    }

    public void updateProgressBar(int value, int maximum) {
        for (Progress progress : listeners) {
            progress.updateProgressBar(value, maximum);
        }
    }

}
