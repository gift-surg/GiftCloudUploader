package uk.ac.ucl.cs.cmic.giftcloud.workers;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.utils.CapabilitiesAvailable;
import com.pixelmed.utils.MessageLogger;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.DicomNode;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporter;

public class ImportWorker implements Runnable {
    private DicomNode dicomNode;
    private GiftCloudReporter reporter;
    private MediaImporter importer;
    private String pathName;
    private Progress progress;

    public ImportWorker(final DicomNode dicomNode, String pathName, final Progress progress, final boolean acceptAnyTransferSyntax, final GiftCloudReporter reporter) {
        this.dicomNode = dicomNode;
        this.reporter = reporter;
        this.progress = progress;
        importer = new OurMediaImporter(reporter, acceptAnyTransferSyntax);
        this.pathName=pathName;
    }

    public void run() {
        reporter.setWaitCursor();
        reporter.sendLn("Import starting");
        reporter.startProgressBar();

        try {
            importer.importDicomFiles(pathName, progress);
        } catch (Exception e) {
            reporter.updateProgress("Importing failed: " + e);
            e.printStackTrace(System.err);
        }
//			srcDatabasePanel.removeAll();
//			try {
//				new OurSourceDatabaseTreeBrowser(srcDatabase,srcDatabasePanel);
//			} catch (Exception e) {
//				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Refresh source database browser failed: "+e));
//				e.printStackTrace(System.err);
//			}
//			srcDatabasePanel.validate();

        reporter.endProgress();
        reporter.updateProgress("Done importing");
        // importer sends its own completion message to log, so do not need another one
        reporter.restoreCursor();
    }


    protected class OurMediaImporter extends MediaImporter {
        boolean acceptAnyTransferSyntax;

        public OurMediaImporter(MessageLogger logger, boolean acceptAnyTransferSyntax) {
            super(logger);
            this.acceptAnyTransferSyntax = acceptAnyTransferSyntax;
        }

        protected void doSomethingWithDicomFileOnMedia(String mediaFileName) {
            try {
                logger.sendLn("Importing DICOM file: "+mediaFileName);
                dicomNode.importFileIntoDatabase(mediaFileName, DatabaseInformationModel.FILE_REFERENCED);
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
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
