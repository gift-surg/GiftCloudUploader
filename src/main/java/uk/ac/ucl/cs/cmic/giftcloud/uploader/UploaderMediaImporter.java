package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.utils.CapabilitiesAvailable;
import com.pixelmed.utils.MessageLogger;

class UploaderMediaImporter extends MediaImporter {
    boolean acceptAnyTransferSyntax;
    private GiftCloudUploader giftCloudUploader;
    private boolean importAsReference;

    public UploaderMediaImporter(final MessageLogger logger, boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference) {
        super(logger);
        this.acceptAnyTransferSyntax = acceptAnyTransferSyntax;
        this.giftCloudUploader = giftCloudUploader;
        this.importAsReference = importAsReference;
    }

    protected void doSomethingWithDicomFileOnMedia(String mediaFileName) {
        try {
            logger.sendLn("Importing DICOM file: " + mediaFileName);

            if (importAsReference) {
                giftCloudUploader.importFile(mediaFileName, DatabaseInformationModel.FILE_REFERENCED);
            } else {
                giftCloudUploader.importFile(mediaFileName, DatabaseInformationModel.FILE_COPIED);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected void doSomethingWithUnwantedFileOnMedia(String mediaFileName) {
    }

    protected boolean canUseBzip = CapabilitiesAvailable.haveBzip2Support();

    // override base class isOKToImport(), which rejects unsupported compressed transfer syntaxes

    protected boolean isOKToImport(String sopClassUID, String transferSyntaxUID) {
        return sopClassUID != null
                && (SOPClass.isImageStorage(sopClassUID) || (SOPClass.isNonImageStorage(sopClassUID) && !SOPClass.isDirectory(sopClassUID)))
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
