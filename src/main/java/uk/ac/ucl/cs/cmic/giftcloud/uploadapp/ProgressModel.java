package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.util.Progress;

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

    public void endProgressBar() {
        for (Progress progress : listeners) {
            progress.endProgressBar();
        }
    }

    public void startProgress(int maximum) {
        for (Progress progress : listeners) {
            progress.startProgressBar(maximum);
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

    public void updateProgressText(final String text) {
        for (Progress progress : listeners) {
            progress.updateStatusText(text);
        }
    }

    public boolean isCancelled() {
        boolean cancelled = false;

        // Note that we do not early out of the cancellation checks. This is because we want to give each progress listener a chance to reset its cancellation value
        for (Progress progress : listeners) {
            cancelled = cancelled || progress.isCancelled();
        }
        return cancelled;
    }
}
