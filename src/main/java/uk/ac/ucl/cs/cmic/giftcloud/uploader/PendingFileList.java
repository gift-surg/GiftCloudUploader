package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.io.FileUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PendingFileList {
    private final List<PendingFile> pendingFileList = new ArrayList<PendingFile>();
    private final File pendingUploadFolder;

    public PendingFileList(final GiftCloudProperties giftCloudProperties, final MultiUploadReporter reporter) {
        pendingUploadFolder = giftCloudProperties.getUploadFolder(reporter);

        addExistingFiles(pendingUploadFolder);
    }

    private void addExistingFiles(final File directory) {

        Iterator fileIterator = FileUtils.iterateFiles(directory, null, true);

        while (fileIterator.hasNext()) {
            File file = (File) fileIterator.next();
            addFileInstance(file.getAbsolutePath());
        }
    }

    public void addFileReference(final String fileReference) {
        pendingFileList.add(new PendingFileReference(fileReference));
    }

    public void addFileInstance(final String fileInstance) {
        pendingFileList.add(new PendingFileInstance(fileInstance));
    }

    public File getPendingUploadFolder() {
        return pendingUploadFolder;
    }

    private class PendingFileReference implements PendingFile {
        private final String fileReference;

        PendingFileReference(final String fileReference) {
            this.fileReference = fileReference;
        }
    }

    private class PendingFileInstance implements PendingFile {
        private final String fileInstance;

        PendingFileInstance(final String fileInstance) {
            this.fileInstance = fileInstance;
        }
    }
}
