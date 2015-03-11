package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import java.io.File;
import java.util.Collection;

public interface FileCollection {
    int getFileCount();

    Collection<File> getFiles();

    long getSize();
}
