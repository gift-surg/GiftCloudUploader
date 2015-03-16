package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel;
import com.pixelmed.dicom.*;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.network.*;
import com.pixelmed.utils.CapabilitiesAvailable;

import java.io.*;
import java.util.*;

public class DicomNode extends Observable {

    private StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher;
    private String ourCalledAETitle;		// set when reading network properties; used not just in StorageSCP, but also when creating exported meta information headers
    private GiftCloudPropertiesFromBridge giftCloudProperties;
    private File savedImagesFolder;
    private NetworkApplicationInformation networkApplicationInformation;
    private DatabaseInformationModel srcDatabase;
    protected Map<String,Date> earliestDatesIndexedBySourceFilePath = new HashMap<String,Date>();


    public DicomNode(final GiftCloudPropertiesFromBridge giftCloudProperties, final String databaseRootTitle) throws DicomException {
        this.giftCloudProperties = giftCloudProperties;
        savedImagesFolder = new File(System.getProperty("java.io.tmpdir"));

        {
            NetworkApplicationInformationFederated federatedNetworkApplicationInformation = new NetworkApplicationInformationFederated();
            federatedNetworkApplicationInformation.startupAllKnownSourcesAndRegister(giftCloudProperties);
            networkApplicationInformation = federatedNetworkApplicationInformation;
        }

        // Start database for the "source" instances. This will not persist when the application is closed, so only
        // in-memory databases are used, and instances live in the temporary filesystem.
        srcDatabase = new PatientStudySeriesConcatenationInstanceModel("mem:src",null, databaseRootTitle);

        // ShutdownHook will run regardless of whether Command-Q (on Mac) or window closed ...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (networkApplicationInformation != null && networkApplicationInformation instanceof NetworkApplicationInformationFederated) {
                    ((NetworkApplicationInformationFederated)networkApplicationInformation).removeAllSources();
                }
            }
        });

    }

    /**
     * <p>Start DICOM storage listener for populating source database.</p>
     *
     * @throws	com.pixelmed.dicom.DicomException
     */
    public void activateStorageSCP() throws DicomNodeStartException {
        try {
            // Start up DICOM association listener in background for receiving images and responding to echoes ...
            if (giftCloudProperties.areNetworkPropertiesValid()) {
                {
                    int port = giftCloudProperties.getListeningPort();
                    ourCalledAETitle = giftCloudProperties.getCalledAETitle();
                    ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Starting up DICOM association listener on port " + port + " AET " + ourCalledAETitle));
                    int storageSCPDebugLevel = giftCloudProperties.getStorageSCPDebugLevel();
                    int queryDebugLevel = giftCloudProperties.getQueryDebugLevel();
                    storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(port, ourCalledAETitle, savedImagesFolder, StoredFilePathStrategy.BYSOPINSTANCEUIDINSINGLEFOLDER, new OurReceivedObjectHandler(),
                            srcDatabase == null ? null : srcDatabase.getQueryResponseGeneratorFactory(queryDebugLevel),
                            srcDatabase == null ? null : srcDatabase.getRetrieveResponseGeneratorFactory(queryDebugLevel),
                            networkApplicationInformation,
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

    public class DicomNodeStartException extends Exception {
        DicomNodeStartException(final String message, final Exception cause) {
            super(message, cause);
        }
    }


            String getOurCalledAETitle() {
        return ourCalledAETitle;
    }

    public void shutdownStorageSCP() {
        if (storageSOPClassSCPDispatcher != null) {
            storageSOPClassSCPDispatcher.shutdown();
        }
    }

    public DatabaseInformationModel getSrcDatabase() {
        return srcDatabase;
    }

    public String getCalledAETitle(final String ae) {
        return networkApplicationInformation.getApplicationEntityTitleFromLocalName(ae);
    }

    public PresentationAddress getPresentationAddress(String calledAETitle) {
        return networkApplicationInformation.getApplicationEntityMap().getPresentationAddress(calledAETitle);
    }

    public String getLocalNameFromApplicationEntityTitle(String calledAET) {
        return networkApplicationInformation.getLocalNameFromApplicationEntityTitle(calledAET);
    }

    public String getApplicationEntityTitleFromLocalName(String remoteAEForQuery) {
        return networkApplicationInformation.getApplicationEntityTitleFromLocalName(remoteAEForQuery);
    }

    public String getQueryModel(String queryCalledAETitle) {
        return networkApplicationInformation.getApplicationEntityMap().getQueryModel(queryCalledAETitle);
    }

    public boolean isNetworkApplicationInformationValid() {
        return (networkApplicationInformation != null);
    }

    public Set getListOfLocalNamesOfApplicationEntities() {
        return networkApplicationInformation.getListOfLocalNamesOfApplicationEntities();
    }

    public NetworkApplicationInformation getNetworkApplicationInformation() {
        return networkApplicationInformation;
    }

    public void importFileIntoDatabase(String dicomFileName,String fileReferenceType) throws FileNotFoundException, IOException, DicomException {

        System.out.println(">>>> DICOM FILE IMPORTING: " + dicomFileName);
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
                String localName = networkApplicationInformation.getLocalNameFromApplicationEntityTitle(callingAETitle);
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Received "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax));
//                giftCloudUploaderPanel.logger.sendLn("Received "+dicomFileName+" from "+localName+" ("+callingAETitle+")");
                try {
                    importFileIntoDatabase(dicomFileName, DatabaseInformationModel.FILE_COPIED);
//                    giftCloudUploaderPanel.srcDatabasePanel.removeAll();
//                    new GiftCloudUploaderPanel.OurSourceDatabaseTreeBrowser(srcDatabase, giftCloudUploaderPanel.srcDatabasePanel);
//                    giftCloudUploaderPanel.srcDatabasePanel.validate();
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
