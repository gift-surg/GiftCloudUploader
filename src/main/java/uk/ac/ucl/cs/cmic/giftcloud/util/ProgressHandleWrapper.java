package uk.ac.ucl.cs.cmic.giftcloud.util;

import org.netbeans.spi.wizard.ResultProgressHandle;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;

import java.awt.*;

public class ProgressHandleWrapper implements ResultProgressHandle {

    private boolean isRunning = false;
    private final Progress progress;

    public ProgressHandleWrapper(final Progress progress) {
        this.progress = progress;
    }

    @Override
    public void setProgress(int currentStep, int totalSteps) {
        progress.updateProgressBar(currentStep, totalSteps);
        isRunning = true;
    }

    @Override
    public void setProgress(String description, int currentStep, int totalSteps) {
        progress.updateStatusText(description);
        progress.updateProgressBar(currentStep, totalSteps);
        isRunning = true;
    }

    @Override
    public void setBusy(String description) {
        progress.startProgressBar();
        progress.updateStatusText(description);
        isRunning = true;
    }

    @Override
    public void finished(Object result) {
        progress.endProgressBar();
        isRunning = false;
    }

    @Override
    public void failed(String message, boolean canNavigateBack) {
        isRunning = false;
    }

    @Override
    public void addProgressComponents(Container panel) {

    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
