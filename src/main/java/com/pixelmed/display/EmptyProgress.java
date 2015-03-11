package com.pixelmed.display;

import org.netbeans.spi.wizard.ResultProgressHandle;

import java.awt.*;

public class EmptyProgress implements ResultProgressHandle {

    private boolean isRunning = false;

    @Override
    public void setProgress(int currentStep, int totalSteps) {
        isRunning = true;
    }

    @Override
    public void setProgress(String description, int currentStep, int totalSteps) {
        isRunning = true;
    }

    @Override
    public void setBusy(String description) {
        isRunning = true;
    }

    @Override
    public void finished(Object result) {
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
