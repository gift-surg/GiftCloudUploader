package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Parses through a directory structure adding files to the database
 */
public class MasterFileImporter {
    private final XmlFileImporter xmlFileImporter;
    private final DicomFileImporter dicomFileImporter;

    public MasterFileImporter(final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final GiftCloudReporterFromApplication reporter) {
        xmlFileImporter = new XmlFileImporter(acceptAnyTransferSyntax, giftCloudUploader, reporter);
        dicomFileImporter = new DicomFileImporter(acceptAnyTransferSyntax, giftCloudUploader, importAsReference, reporter);
    }

    public boolean importFiles(final List<File> fileList, final Progress progress) throws IOException, DicomException {
        boolean anyFiles = false;
        int count = 0;
        if (progress != null) {
            progress.updateProgressBar(0, fileList.size());
        }

        for (File mediaFile : fileList) {
            anyFiles = importFileOrDirectory(mediaFile, progress) || anyFiles;
            ++count;
            if (progress != null) {
                progress.updateProgressBar(count);
            }
        }
        return anyFiles;
    }

    public boolean importFileOrDirectory(final File fileOrDirectory, final Progress progress) throws IOException, DicomException {
        boolean anyXmlFiles = xmlFileImporter.importFiles(fileOrDirectory, progress);
        boolean anyDicomFiles = dicomFileImporter.importFiles(fileOrDirectory, progress);
        return anyXmlFiles || anyDicomFiles;
    }
}
