package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.io.Files;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.MediaImporter;
import com.tomdoel.mpg2dcm.EndoscopicXmlToDicomConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Parses through a directory structure adding files to the database
 */
public class XmlFileImporter {
    private final MediaImporter mediaImporter;

    public XmlFileImporter(final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final GiftCloudReporterFromApplication reporter) {

        // Any DICOM files created from the XML import will be temporary; therefore we must import as copy not reference
        mediaImporter = new UploaderMediaImporter(acceptAnyTransferSyntax, giftCloudUploader, false, reporter);
    }

    public boolean importFiles(final File fileOrDirectory, final Progress progress) throws IOException, DicomException {
        boolean anyFiles = false;

        // Check if we are importing a file or directory. For a directory we import all files recursively
        if (fileOrDirectory.isDirectory()) {
            Iterator it = FileUtils.iterateFiles(fileOrDirectory, new String[]{"xml"}, true);
            while (it.hasNext()) {
                final File nextXmlFile = (File) it.next();
                anyFiles = importXmlFile(progress, nextXmlFile) || anyFiles;
            }
        } else {
            if (fileOrDirectory.isFile() && FilenameUtils.isExtension(fileOrDirectory.getName(), "xml")) {
                anyFiles = importXmlFile(progress, fileOrDirectory) || anyFiles;
            }
        }
        return anyFiles;
    }

    private boolean importXmlFile(Progress progress, File nextXmlFile) {
        try {
            // For each XML file we will attempt to convert it into a DICOM file in a temporary directory. If this succeeds then we import that temporary directory
            final File dicomOutputPath = Files.createTempDir();
            EndoscopicXmlToDicomConverter.convert(nextXmlFile, dicomOutputPath.getCanonicalPath());
            return mediaImporter.importDicomFileOrPath(dicomOutputPath, progress);
        } catch (Throwable t) {
            return false;
        }
    }

}
