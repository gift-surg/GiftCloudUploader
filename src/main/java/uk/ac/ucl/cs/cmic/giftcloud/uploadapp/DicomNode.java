package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel;
import com.pixelmed.dicom.*;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.*;
import com.pixelmed.utils.CapabilitiesAvailable;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploaderError;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.*;
import java.util.*;

public class DicomNode extends Observable {

    private StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher;
    private GiftCloudPropertiesFromApplication giftCloudProperties;
    private GiftCloudReporter reporter;
    private DatabaseInformationModel srcDatabase;
    protected Map<String,Date> earliestDatesIndexedBySourceFilePath = new HashMap<String,Date>();
    private final GiftCloudUploader giftCloudUploader;


    public DicomNode(final GiftCloudPropertiesFromApplication giftCloudProperties, final String databaseRootTitle, final GiftCloudUploader giftCloudUploader, final GiftCloudReporter reporter) throws DicomException {
        this.giftCloudProperties = giftCloudProperties;
        this.giftCloudUploader = giftCloudUploader;
        this.reporter = reporter;

        // Start database for the "source" instances.
        srcDatabase = new PatientStudySeriesConcatenationInstanceModel("mem:src", null, databaseRootTitle);

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
    public void activateStorageSCP() throws DicomNodeStartException, DicomNetworkException {
        try {
            final File savedImagesFolder = giftCloudProperties.getUploadFolder(reporter);

            // Start up DICOM association listener in background for receiving images and responding to echoes ...
            if (giftCloudProperties.areNetworkPropertiesValid()) {
                {
                    int port = giftCloudProperties.getListeningPort();
                    if (port < 0) {
                        throw new GiftCloudException(GiftCloudUploaderError.EMPTY_LISTENER_PORT, "Could not start the Dicom storage SCP service because the port was not set");
                    }
                    final Optional<String> ourCalledAETitle = giftCloudProperties.getListenerCalledAETitle();
                    if (!ourCalledAETitle.isPresent()) {
                        throw new GiftCloudException(GiftCloudUploaderError.EMPTY_LISTENER_AE_TITLE, "Could not start the Dicom storage SCP service because the AE title was not set");
                    }

                    ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Starting up DICOM association listener on port " + port  + " AET " + ourCalledAETitle));
                    int storageSCPDebugLevel = giftCloudProperties.getStorageSCPDebugLevel();
                    int queryDebugLevel = giftCloudProperties.getQueryDebugLevel();
                    storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(getAe(), port, ourCalledAETitle.get(), savedImagesFolder, StoredFilePathStrategy.BYSOPINSTANCEUIDINSINGLEFOLDER, new OurReceivedObjectHandler(),
                            srcDatabase == null ? null : srcDatabase.getQueryResponseGeneratorFactory(queryDebugLevel),
                            srcDatabase == null ? null : srcDatabase.getRetrieveResponseGeneratorFactory(queryDebugLevel),
                            new OurPresentationContextSelectionPolicy(),
                            false/*secureTransport*/,
                            storageSCPDebugLevel);
                    new Thread(storageSOPClassSCPDispatcher).start();
                }
            }
        } catch (IOException e) {
            throw new DicomNodeStartException("Could not start the Dicom storage SCP service due to the following error: " + e.getLocalizedMessage(), e);
        }
    }


    /**
     * Creates an ApplicationEntity from the current properties
     * @return
     * @throws DicomNetworkException
     */
    public ApplicationEntity getAe() throws GiftCloudException {
        final Optional<String> aeTitle = giftCloudProperties.getPacsAeTitle();
        final Optional<String> hostname = giftCloudProperties.getPacsHostName();
        final int port = giftCloudProperties.getPacsPort();
        final Optional<String> queryModel = giftCloudProperties.getPacsQueryModel();
        final Optional<String> primaryDeviceType = giftCloudProperties.getPacsPrimaryDeviceType();

        if (!aeTitle.isPresent() || StringUtils.isBlank(aeTitle.get())) {
            throw new GiftCloudException(GiftCloudUploaderError.NETWORK_PROPERTIES_INVALID, "The PACS AE title has not been set.");
        }
        if (!hostname.isPresent() || StringUtils.isBlank(hostname.get())) {
            throw new GiftCloudException(GiftCloudUploaderError.NETWORK_PROPERTIES_INVALID, "The PACS host name has not been set.");
        }

        final PresentationAddress presentationAddress = new PresentationAddress(hostname.get(), port);
        return new ApplicationEntity(aeTitle.get(), presentationAddress, queryModel.orElse(null), primaryDeviceType.orElse(null));
    }

    public class DicomNodeStartException extends Exception {
        DicomNodeStartException(final String message, final Exception cause) {
            super(message, cause);
        }
    }

    public void shutdownStorageSCP() {
        if (storageSOPClassSCPDispatcher != null) {
            storageSOPClassSCPDispatcher.shutdown();
        }
    }

    public void shutdownStorageSCPAndWait(final long maximumThreadCompletionWaitTime) {
        if (storageSOPClassSCPDispatcher != null) {
            storageSOPClassSCPDispatcher.shutdownAndWait(maximumThreadCompletionWaitTime);
        }
    }

    public DatabaseInformationModel getSrcDatabase() {
        return srcDatabase;
    }

    public void importFileIntoDatabase(String dicomFileName,String fileReferenceType) throws FileNotFoundException, IOException, DicomException {
        ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Importing: "+dicomFileName));
        FileInputStream fis = new FileInputStream(dicomFileName);
        DicomInputStream i = new DicomInputStream(new BufferedInputStream(fis));
        AttributeList list = new AttributeList();
        list.read(i, TagFromName.PixelData);
        i.close();
        fis.close();
        srcDatabase.insertObject(list,dicomFileName,fileReferenceType);
        if (earliestDatesIndexedBySourceFilePath != null) {
            Date earliestInObject = ClinicalTrialsAttributes.findEarliestDateTime(list);
            if (earliestInObject != null) {
                earliestDatesIndexedBySourceFilePath.put(dicomFileName,earliestInObject);
            }
        }

        // Send a notification that the database has changed
        setChanged();
        notifyObservers(dicomFileName);
    }

    public void removeFileFromEasliestDatesIndex(String fileName) {
        if (earliestDatesIndexedBySourceFilePath != null) {
            earliestDatesIndexedBySourceFilePath.remove(fileName);
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
                    importFileIntoDatabase(dicomFileName, DatabaseInformationModel.FILE_COPIED);
                    giftCloudUploader.addFileInstance(dicomFileName);
//                    giftCloudUploaderPanel.srcDatabasePanel.removeAll();
//                    new GiftCloudUploaderPanel.OurSourceDatabaseTreeBrowser(srcDatabase, giftCloudUploaderPanel.srcDatabasePanel);
//                    giftCloudUploaderPanel.srcDatabasePanel.validate();



                    // ToDo: Remove this line so the file doesn't get deleted on exit
                    new File(dicomFileName).deleteOnExit();
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
