package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;

import java.io.IOException;

/**
 * Parses through a directory structure adding files to the database
 */
public class DicomFileImporter {
    private final UploaderMediaImporter mediaImporter;

    public DicomFileImporter(final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final GiftCloudReporterFromApplication reporter) {
        mediaImporter = new UploaderMediaImporter(reporter, acceptAnyTransferSyntax, giftCloudUploader, importAsReference);
    }

    public boolean importFiles(String pathName, final Progress progress) throws IOException, DicomException {
        return mediaImporter.importDicomFiles(pathName, progress);
    }
}
