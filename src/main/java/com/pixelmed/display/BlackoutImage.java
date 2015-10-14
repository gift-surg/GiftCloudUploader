package com.pixelmed.display;

import java.io.File;

public class BlackoutImage {
    private String currentFileName;

    public BlackoutImage(File currentFile) {
        currentFileName = currentFile.getAbsolutePath();        // set to what we actually used, used for later save
    }

    public String getCurrentFileName() {
        return currentFileName;
    }
}
