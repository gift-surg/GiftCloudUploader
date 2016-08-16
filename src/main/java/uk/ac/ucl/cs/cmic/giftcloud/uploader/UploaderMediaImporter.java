package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.utils.CapabilitiesAvailable;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

public class UploaderMediaImporter extends MediaImporter {
    boolean acceptAnyTransferSyntax;
    private GiftCloudUploader giftCloudUploader;
    private boolean importAsReference;

    public UploaderMediaImporter(boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final GiftCloudReporter reporter) {
        super(reporter);
        this.acceptAnyTransferSyntax = acceptAnyTransferSyntax;
        this.giftCloudUploader = giftCloudUploader;
        this.importAsReference = importAsReference;
    }

    protected void doSomethingWithDicomFileOnMedia(String mediaFileName, AttributeList list) {
        try {
            // Choose whether the imported files should be deleted after uploading; only do this if we have created the files and want them to be removed
            final PendingUploadTask.DeleteAfterUpload deleteAfterUpload = importAsReference ? PendingUploadTask.DeleteAfterUpload.DO_NOT_DELETE_AFTER_UPLOAD : PendingUploadTask.DeleteAfterUpload.DELETE_AFTER_UPLOAD;
            giftCloudUploader.importFiles(new DicomFileImportRecord(mediaFileName, GiftCloudUtils.getDateAsAString(), deleteAfterUpload, Optional.of(list)));

        } catch (Exception e) {
            reporter.silentLogException(e, "Error during file import");
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
