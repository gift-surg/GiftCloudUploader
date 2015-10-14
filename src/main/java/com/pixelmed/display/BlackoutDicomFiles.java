package com.pixelmed.display;

public class BlackoutDicomFiles {
    private final String[] dicomFiles;
    private int currentFileNumber;

    BlackoutDicomFiles(final String[] dicomFiles) {
        currentFileNumber = 0;
        this.dicomFiles = dicomFiles;
    }

    public boolean filesExist() {
        return dicomFiles != null;
    }

    public int getNumberOfFiles() {
        return dicomFiles != null ? dicomFiles.length : -1;
    }

    public boolean goToNext() {
        if (filesExist() && currentFileNumber < dicomFiles.length) {
            currentFileNumber++;
            return true;
        } else {
            return false;
        }
    }

    public boolean goToPrevious() {
        if (filesExist() && currentFileNumber > 0) {
            currentFileNumber--;
            return true;
        } else {
            return false;
        }
    }

    public int getCurrentFileNumber() {
        return currentFileNumber;
    }

    public String getCurrentFileName() {
        return dicomFiles[currentFileNumber];
    }
}
