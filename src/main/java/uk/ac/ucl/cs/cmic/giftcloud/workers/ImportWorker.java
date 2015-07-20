package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.utils.CapabilitiesAvailable;
import com.pixelmed.utils.MessageLogger;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.LocalWaitingForUploadDatabase;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;

public class ImportWorker implements Runnable {
    private LocalWaitingForUploadDatabase uploadDatabase;
    private GiftCloudUploader giftCloudUploader;
    private boolean importAsReference;
    private UploaderStatusModel uploaderStatusModel;
    private GiftCloudReporterFromApplication reporter;
    private MediaImporter importer;
    private String pathName;
    private Progress progress;

    public ImportWorker(final LocalWaitingForUploadDatabase uploadDatabase, String pathName, final Progress progress, final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) {
        this.uploadDatabase = uploadDatabase;
        this.giftCloudUploader = giftCloudUploader;
        this.importAsReference = importAsReference;
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;
        this.progress = progress;
        importer = new OurMediaImporter(reporter, acceptAnyTransferSyntax);
        this.pathName=pathName;
    }

    public void run() {
        uploaderStatusModel.setImportingStatusMessage("Importing files...");
        reporter.startProgressBar();
        boolean anyFiles = false;

        try {
            anyFiles = importer.importDicomFiles(pathName, progress);
        } catch (Exception e) {
            uploaderStatusModel.setImportingStatusMessage("Failure when importing files" , e);
            reporter.silentLogException(e, "Failure when importing files");
        }

        reporter.endProgressBar();
        final String statusMessage = anyFiles ? "Files have been imported and are ready for upload" : "Ready";
        uploaderStatusModel.setImportingStatusMessage(statusMessage);
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
                    giftCloudUploader.importFile(mediaFileName, DatabaseInformationModel.FILE_REFERENCED);
                } else {
                    giftCloudUploader.importFile(mediaFileName, DatabaseInformationModel.FILE_COPIED);
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
