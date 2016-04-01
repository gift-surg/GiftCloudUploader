package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.StoredFilePathStrategy;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.*;
import com.pixelmed.utils.CapabilitiesAvailable;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.DicomFileImportRecord;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PendingUploadTask;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class DicomListener {

    private StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher;
    private GiftCloudUploader uploader;
    private GiftCloudPropertiesFromApplication giftCloudProperties;
    private UploaderStatusModel uploaderStatusModel;
    private GiftCloudReporter reporter;


    public DicomListener(final GiftCloudUploader uploader, final GiftCloudPropertiesFromApplication giftCloudProperties, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporter reporter) throws DicomException {
        this.uploader = uploader;
        this.giftCloudProperties = giftCloudProperties;
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;

        // ShutdownHook will run regardless of whether Command-Q (on Mac) or window closed ...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdownStorageSCPAndWait(giftCloudProperties.getShutdownTimeoutMs());
            }
        });

    }

    /**
     * <p>Start DICOM storage listener for populating source database.</p>
     *
     * @throws	com.pixelmed.dicom.DicomException
     */
    public void activateStorageSCP() throws IOException {
        uploaderStatusModel.setImportingStatusMessage("Starting up Dicom listening service");
        final File savedImagesFolder = giftCloudProperties.getUploadFolder(reporter);

        // Start up DICOM association listener in background for receiving images and responding to echoes ...
        {
            int port = giftCloudProperties.getListeningPort();
            if (port < 0) {

                uploaderStatusModel.setImportingStatusMessage("Cannot listen to PACS because the port is not set");
                throw new GiftCloudException(GiftCloudUploaderError.EMPTY_LISTENER_PORT, "Could not start the Dicom storage SCP service because the port was not set");
            }
            final String ourAETitle = giftCloudProperties.getListenerAETitle();

            ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Starting up DICOM association listener on port " + port  + " AET " + ourAETitle));
            storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(port, ourAETitle, savedImagesFolder, StoredFilePathStrategy.BYSOPINSTANCEUIDINSINGLEFOLDER, new OurReceivedObjectHandler(),
                    new OurPresentationContextSelectionPolicy(),
                    false/*secureTransport*/
            );
            storageSOPClassSCPDispatcher.startup();
        }
    }


    public class DicomNodeStartException extends Exception {
        DicomNodeStartException(final String message, final Exception cause) {
            super(message, cause);
        }
    }

    public void shutdownStorageSCP() {
        if (storageSOPClassSCPDispatcher != null) {
            uploaderStatusModel.setImportingStatusMessage("Shutting down Dicom listening service");
            storageSOPClassSCPDispatcher.shutdown();
        }
    }

    public void shutdownStorageSCPAndWait(final long maximumThreadCompletionWaitTime) {
        if (storageSOPClassSCPDispatcher != null) {
            uploaderStatusModel.setImportingStatusMessage("Shutting down Dicom listening service");
            storageSOPClassSCPDispatcher.shutdownAndWait(maximumThreadCompletionWaitTime);
        }
    }

    class OurPresentationContextSelectionPolicy extends UnencapsulatedExplicitStoreFindMoveGetPresentationContextSelectionPolicy {
        OurPresentationContextSelectionPolicy() {
            super();
            transferSyntaxSelectionPolicy = new OurTransferSyntaxSelectionPolicy();
        }
    }


    protected class OurReceivedObjectHandler extends ReceivedObjectHandler {
        public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle)
                throws DicomNetworkException, DicomException, IOException {
            if (dicomFileName != null) {
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Received "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax));
                try {
                    uploader.importFiles(new DicomFileImportRecord(dicomFileName, PendingUploadTask.DeleteAfterUpload.DELETE_AFTER_UPLOAD));
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    // we will (grudgingly) accept JPEGBaseline, since we know the JRE can natively decode it without JIIO extensions present,
    // so will work by decompressing during attribute list read for cleaning

    class OurTransferSyntaxSelectionPolicy extends TransferSyntaxSelectionPolicy {
        public LinkedList applyTransferSyntaxSelectionPolicy(LinkedList presentationContexts,int associationNumber,int debugLevel) {
            boolean canUseBzip = CapabilitiesAvailable.haveBzip2Support();
            ListIterator pcsi = presentationContexts.listIterator();
            while (pcsi.hasNext()) {
                PresentationContext pc = (PresentationContext)(pcsi.next());
                boolean foundExplicitVRLittleEndian = false;
                boolean foundImplicitVRLittleEndian = false;
                boolean foundExplicitVRBigEndian = false;
                boolean foundDeflated = false;
                boolean foundBzipped = false;
                boolean foundJPEGBaseline = false;
                boolean foundJPEGLossless = false;
                boolean foundJPEGLosslessSV1 = false;
                boolean foundJPEG2000 = false;
                boolean foundJPEG2000Lossless = false;
                boolean foundJPEGLSLossless = false;
                boolean foundJPEGLSNearLossless = false;
                String lastRecognized = null;
                List tsuids = pc.getTransferSyntaxUIDs();
                ListIterator tsuidsi = tsuids.listIterator();
                while (tsuidsi.hasNext()) {
                    String transferSyntaxUID=(String)(tsuidsi.next());
                    if (transferSyntaxUID != null) {
                        if      (transferSyntaxUID.equals(TransferSyntax.ImplicitVRLittleEndian)) foundImplicitVRLittleEndian = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.ExplicitVRLittleEndian)) foundExplicitVRLittleEndian = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.ExplicitVRBigEndian)) foundExplicitVRBigEndian = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.DeflatedExplicitVRLittleEndian)) foundDeflated = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.PixelMedBzip2ExplicitVRLittleEndian)) foundBzipped = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline)) foundJPEGBaseline = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.JPEGLossless)) foundJPEGLossless = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.JPEGLosslessSV1)) foundJPEGLosslessSV1 = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.JPEG2000)) foundJPEG2000 = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.JPEG2000Lossless)) foundJPEG2000Lossless = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.JPEGLS)) foundJPEGLSLossless = true;
                        else if (transferSyntaxUID.equals(TransferSyntax.JPEGNLS)) foundJPEGLSNearLossless = true;
                        else if (new TransferSyntax(transferSyntaxUID).isRecognized()) lastRecognized = transferSyntaxUID;
                    }
                }
                // discard old list and make a new one ...
                pc.newTransferSyntaxUIDs();
                // Policy is prefer bzip then deflate compressed then explicit (little then big) then implicit,
                // then supported image compression transfer syntaxes in the following order and ignore anything else
                // unless the acceptAnyTransferSyntaxCheckBox is selected, in which case the last recognized transfer syntax in the offered list will be used
                // with the intent of having the sender decompress the image compression transfer syntaxes if it provided multiple choices.
                // must only support ONE in response
                if (foundBzipped && canUseBzip) {
                    pc.addTransferSyntaxUID(TransferSyntax.PixelMedBzip2ExplicitVRLittleEndian);
                }
                else if (foundDeflated) {
                    pc.addTransferSyntaxUID(TransferSyntax.DeflatedExplicitVRLittleEndian);
                }
                else if (foundExplicitVRLittleEndian) {
                    pc.addTransferSyntaxUID(TransferSyntax.ExplicitVRLittleEndian);
                }
                else if (foundExplicitVRBigEndian) {
                    pc.addTransferSyntaxUID(TransferSyntax.ExplicitVRBigEndian);
                }
                else if (foundImplicitVRLittleEndian) {
                    pc.addTransferSyntaxUID(TransferSyntax.ImplicitVRLittleEndian);
                }
                else if (foundJPEGBaseline) {
                    pc.addTransferSyntaxUID(TransferSyntax.JPEGBaseline);
                }
                else if (foundJPEGLossless && CapabilitiesAvailable.haveJPEGLosslessCodec()) {
                    pc.addTransferSyntaxUID(TransferSyntax.JPEGLossless);
                }
                else if (foundJPEGLosslessSV1 && CapabilitiesAvailable.haveJPEGLosslessCodec()) {
                    pc.addTransferSyntaxUID(TransferSyntax.JPEGLosslessSV1);
                }
                else if (foundJPEG2000 && CapabilitiesAvailable.haveJPEG2000Part1Codec()) {
                    pc.addTransferSyntaxUID(TransferSyntax.JPEG2000);
                }
                else if (foundJPEG2000Lossless && CapabilitiesAvailable.haveJPEG2000Part1Codec()) {
                    pc.addTransferSyntaxUID(TransferSyntax.JPEG2000Lossless);
                }
                else if (foundJPEGLSLossless && CapabilitiesAvailable.haveJPEGLSCodec()) {
                    pc.addTransferSyntaxUID(TransferSyntax.JPEGLS);
                }
                else if (foundJPEGLSNearLossless && CapabilitiesAvailable.haveJPEGLSCodec()) {
                    pc.addTransferSyntaxUID(TransferSyntax.JPEGNLS);
                }
                else if (giftCloudProperties.acceptAnyTransferSyntax() && lastRecognized != null) {
                    pc.addTransferSyntaxUID(lastRecognized);
                }
                else {
                    pc.setResultReason((byte)4);				// transfer syntaxes not supported (provider rejection)
                }
            }
//System.err.println("GiftCloudUploaderPanel.OurTransferSyntaxSelectionPolicy.applyTransferSyntaxSelectionPolicy(): accepted "+presentationContexts);
            return presentationContexts;
        }
    }


}
