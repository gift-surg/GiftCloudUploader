package com.pixelmed.display;

public class BlackoutDicomFiles {
    private final String[] dicomFiles;

    public BlackoutDicomFiles(final String[] dicomFiles) {
        this.dicomFiles = dicomFiles;
    }

    public String getCurrentFileName() {
        return dicomFiles[0];
    }
}
