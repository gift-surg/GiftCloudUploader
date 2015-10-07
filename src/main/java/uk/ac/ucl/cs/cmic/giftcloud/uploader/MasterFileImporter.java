package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;

import java.io.IOException;

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

    public boolean importFiles(String pathName, final Progress progress) throws IOException, DicomException {
        boolean anyXmlFiles = xmlFileImporter.importFiles(pathName, progress);
        boolean anyDicomFiles = dicomFileImporter.importFiles(pathName, progress);
        return anyXmlFiles || anyDicomFiles;
    }
}
