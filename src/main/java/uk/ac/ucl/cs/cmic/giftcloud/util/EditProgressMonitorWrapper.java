package uk.ac.ucl.cs.cmic.giftcloud.util;

import org.nrg.util.EditProgressMonitor;

public class EditProgressMonitorWrapper implements EditProgressMonitor {
    private final Progress progress;
    private Optional<Integer> maximum = Optional.empty();

    public EditProgressMonitorWrapper(final Progress progress) {
        this.progress = progress;
        reset();
    }

    @Override
    public void setMinimum(int min) {
    }

    @Override
    public void setMaximum(int max) {
        maximum = Optional.of(max);
    }

    @Override
    public void setProgress(int current) {
        if (maximum.isPresent()) {
            progress.updateProgressBar(current, maximum.get());
        } else {
            progress.updateProgressBar(current);
        }
    }

    @Override
    public void setNote(String note) {
        progress.updateStatusText(note);
    }

    @Override
    public void close() {
        progress.endProgressBar();
        reset();
    }

    @Override
    public boolean isCanceled() {
        return progress.isCancelled();
    }

    private void reset() {
        maximum = Optional.empty();
    }
}
