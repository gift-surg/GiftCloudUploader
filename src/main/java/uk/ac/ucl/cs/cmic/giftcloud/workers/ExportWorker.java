package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.dicom.*;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.utils.CopyStream;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
* Created by tom on 17/03/15.
*/
public class ExportWorker implements Runnable {
    private final Vector<String> destinationFilePathSelections;
    private final File exportDirectory;
private final boolean hierarchicalExport;
private final boolean zipExport;
private final GiftCloudReporterFromApplication reporter;
private String rootNameForDicomInstanceFilesOnInterchangeMedia = "DICOM"; //static
private String filePrefixForDicomInstanceFilesOnInterchangeMedia = "I";  //static
private String fileSuffixForDicomInstanceFilesOnInterchangeMedia = ""; //static
private String nameForDicomDirectoryOnInterchangeMedia = "DICOMDIR"; // static
private String exportedZipFileName = "export.zip"; // static


public ExportWorker(final Vector<String> destinationFilePathSelections, final File exportDirectory, final boolean hierarchicalExport, final boolean zipExport, final GiftCloudReporterFromApplication reporter) {
        this.destinationFilePathSelections = destinationFilePathSelections;
        this.exportDirectory = exportDirectory;
this.hierarchicalExport = hierarchicalExport;
this.zipExport = zipExport;
this.reporter = reporter;
}

    public void run() {
        reporter.setWaitCursor();
reporter.sendLn("Export started");
        try {
            int nFiles = destinationFilePathSelections.size();
reporter.updateProgressBar(0, nFiles + 1); // include DICOMDIR
            String exportFileNames[] = new String[nFiles];
            for (int j=0; j<nFiles; ++j) {
                String databaseFileName = destinationFilePathSelections.get(j);
                String exportRelativePathName = hierarchicalExport ? makeNewFullyQualifiedHierarchicalInstancePathName(databaseFileName) : makeNewFullyQualifiedInterchangeMediaInstancePathName(j);
                File exportFile = new File(exportDirectory,exportRelativePathName);
//					ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Exporting "+exportRelativePathName));
                reporter.updateStatusText("Exporting " + databaseFileName + " to " + exportFile.getCanonicalPath());
                exportFile.getParentFile().mkdirs();
                CopyStream.copy(new File(databaseFileName), exportFile);
                exportFileNames[j] = exportRelativePathName;
reporter.updateProgressBar(j + 1);
            }
reporter.updateStatusText("Exporting DICOMDIR");
            DicomDirectory dicomDirectory = new DicomDirectory(exportDirectory, exportFileNames);
dicomDirectory.write(new File(exportDirectory,nameForDicomDirectoryOnInterchangeMedia).getCanonicalPath());
reporter.updateProgressBar(nFiles + 1); // include DICOMDIR

            if (zipExport) {
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Zipping exported files"));
reporter.updateStatusText("Zipping exported files");
                File zipFile = new File(exportDirectory,exportedZipFileName);
                zipFile.delete();
                FileOutputStream fout = new FileOutputStream(zipFile);
                ZipOutputStream zout = new ZipOutputStream(fout);
                zout.setMethod(ZipOutputStream.DEFLATED);
                zout.setLevel(9);

reporter.updateProgressBar(0, nFiles + 1); // include DICOMDIR
                for (int j=0; j<nFiles; ++j) {
                    String exportRelativePathName = exportFileNames[j];
                    File inFile = new File(exportDirectory,exportRelativePathName);
                    ZipEntry zipEntry = new ZipEntry(exportRelativePathName);
                    //zipEntry.setMethod(ZipOutputStream.DEFLATED);
                    zout.putNextEntry(zipEntry);
                    FileInputStream in = new FileInputStream(inFile);
                    CopyStream.copy(in,zout);
                    zout.closeEntry();
                    in.close();
                    inFile.delete();
reporter.updateProgressBar(j + 1);
                }

                {
                    File inFile = new File(exportDirectory,nameForDicomDirectoryOnInterchangeMedia);
                    ZipEntry zipEntry = new ZipEntry(nameForDicomDirectoryOnInterchangeMedia);
                    zipEntry.setMethod(ZipOutputStream.DEFLATED);
                    zout.putNextEntry(zipEntry);
                    FileInputStream in = new FileInputStream(inFile);
                    CopyStream.copy(in,zout);
                    zout.closeEntry();
                    in.close();
                    inFile.delete();
reporter.updateProgressBar(nFiles + 1); // include DICOMDIR
                }
                zout.close();
                fout.close();
                new File(exportDirectory,rootNameForDicomInstanceFilesOnInterchangeMedia).delete();
            }

        } catch (Exception e) {
            ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Export failed: "+e));
            e.printStackTrace(System.err);
        }
reporter.updateStatusText("Done exporting to " + exportDirectory);
reporter.endProgressBar();
reporter.sendLn("Export complete");
        reporter.restoreCursor();
    }

protected String makeNewFullyQualifiedHierarchicalInstancePathName(String sourceFileName) throws DicomException, IOException {
AttributeList list = new AttributeList();
list.read(sourceFileName, TagFromName.PixelData);
String hierarchicalFileName = MoveDicomFilesIntoHierarchy.makeHierarchicalPathFromAttributes(list);
return new File(rootNameForDicomInstanceFilesOnInterchangeMedia,hierarchicalFileName).getPath();
}

protected String makeNewFullyQualifiedInterchangeMediaInstancePathName(int fileCount) throws IOException {
return new File(
rootNameForDicomInstanceFilesOnInterchangeMedia,
filePrefixForDicomInstanceFilesOnInterchangeMedia + Integer.toString(fileCount) + fileSuffixForDicomInstanceFilesOnInterchangeMedia)
.getPath();
}

}
