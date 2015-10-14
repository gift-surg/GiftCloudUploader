package com.pixelmed.display;

public class BlackoutDicomFiles {
    private final String[] dicomFiles;

    BlackoutDicomFiles(final String[] dicomFiles) {

        this.dicomFiles = dicomFiles;
    }

    public boolean filesExist() {
        return dicomFiles != null;
    }

    public int getNumberOfFiles() {
        return dicomFiles != null ? dicomFiles.length : -1;
    }

    public String getFileName(int currentFileNumber) {
        return dicomFiles[currentFileNumber];
    }
}
