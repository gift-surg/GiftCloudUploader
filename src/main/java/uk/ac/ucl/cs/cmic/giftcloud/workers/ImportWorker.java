package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.utils.CapabilitiesAvailable;
import com.pixelmed.utils.MessageLogger;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.DicomNode;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;

public class ImportWorker implements Runnable {
    private DicomNode dicomNode;
    private GiftCloudUploader giftCloudUploader;
    private boolean importAsReference;
    private GiftCloudReporterFromApplication reporter;
    private MediaImporter importer;
    private String pathName;
    private Progress progress;

    public ImportWorker(final DicomNode dicomNode, String pathName, final Progress progress, final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final GiftCloudReporterFromApplication reporter) {
        this.dicomNode = dicomNode;
        this.giftCloudUploader = giftCloudUploader;
        this.importAsReference = importAsReference;
        this.reporter = reporter;
        this.progress = progress;
        importer = new OurMediaImporter(reporter, acceptAnyTransferSyntax);
        this.pathName=pathName;
    }

    public void run() {
        reporter.sendLn("Import starting");
        reporter.startProgressBar();

        try {
            importer.importDicomFiles(pathName, progress);
        } catch (Exception e) {
            reporter.updateStatusText("Importing failed: " + e);
            e.printStackTrace(System.err);
        }

        reporter.endProgressBar();
        reporter.updateStatusText("Done importing");
        // importer sends its own completion message to log, so do not need another one
    }


    protected class OurMediaImporter extends MediaImporter {
        boolean acceptAnyTransferSyntax;

        public OurMediaImporter(MessageLogger logger, boolean acceptAnyTransferSyntax) {
            super(logger);
            this.acceptAnyTransferSyntax = acceptAnyTransferSyntax;
        }

        protected void doSomethingWithDicomFileOnMedia(String mediaFileName) {
            try {
                logger.sendLn("Importing DICOM file: " + mediaFileName);

                if (importAsReference) {
                    dicomNode.importFileIntoDatabase(mediaFileName, DatabaseInformationModel.FILE_REFERENCED);
                    giftCloudUploader.addFileReference(mediaFileName);
                } else {
                    dicomNode.importFileIntoDatabase(mediaFileName, DatabaseInformationModel.FILE_COPIED);
                    giftCloudUploader.addFileInstance(mediaFileName);
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        protected void doSomethingWithUnwantedFileOnMedia(String mediaFileName) {
        }

        protected boolean canUseBzip = CapabilitiesAvailable.haveBzip2Support();

        // override base class isOKToImport(), which rejects unsupported compressed transfer syntaxes

        protected boolean isOKToImport(String sopClassUID,String transferSyntaxUID) {
            return sopClassUID != null
                    && (SOPClass.isImageStorage(sopClassUID) || (SOPClass.isNonImageStorage(sopClassUID) && ! SOPClass.isDirectory(sopClassUID)))
                    && transferSyntaxUID != null
                    && ((acceptAnyTransferSyntax && new TransferSyntax(transferSyntaxUID).isRecognized())
                    || transferSyntaxUID.equals(TransferSyntax.ImplicitVRLittleEndian)
                    || transferSyntaxUID.equals(TransferSyntax.ExplicitVRLittleEndian)
                    || transferSyntaxUID.equals(TransferSyntax.ExplicitVRBigEndian)
                    || transferSyntaxUID.equals(TransferSyntax.DeflatedExplicitVRLittleEndian)
                    || (transferSyntaxUID.equals(TransferSyntax.DeflatedExplicitVRLittleEndian) && canUseBzip)
                    || transferSyntaxUID.equals(TransferSyntax.RLE)
                    || transferSyntaxUID.equals(TransferSyntax.JPEGBaseline)
                    || CapabilitiesAvailable.haveJPEGLosslessCodec() && (transferSyntaxUID.equals(TransferSyntax.JPEGLossless) || transferSyntaxUID.equals(TransferSyntax.JPEGLosslessSV1))
                    || CapabilitiesAvailable.haveJPEG2000Part1Codec() && (transferSyntaxUID.equals(TransferSyntax.JPEG2000) || transferSyntaxUID.equals(TransferSyntax.JPEG2000Lossless))
                    || CapabilitiesAvailable.haveJPEGLSCodec() && (transferSyntaxUID.equals(TransferSyntax.JPEGLS) || transferSyntaxUID.equals(TransferSyntax.JPEGNLS))
            );
        }
    }

}
