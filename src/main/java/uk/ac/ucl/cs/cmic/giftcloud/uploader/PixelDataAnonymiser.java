/*

 Author: Tom Doel

 Some parts of this software were derived from DicomCleaner, Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.


 */

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.*;
import com.pixelmed.display.ImageEditUtilities;
import com.pixelmed.display.SourceImage;
import com.pixelmed.utils.CapabilitiesAvailable;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.RedactedFileWrapper;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;


/**
 * Removes burnt-in patient identifiable data from images
 */
public class PixelDataAnonymiser {

    private final List<PixelDataAnonymiseFilter> filters;
    private boolean burnInOverlays = false;
    private boolean useZeroBlackoutValue = false;
    private boolean usePixelPaddingBlackoutValue = true;
    private final String aeTitle;

    /**
     * Creates the PixelDataAnonymiser object. Parameters will be set at construction time
     *
     * @param giftCloudProperties Shared properties use to define anonymsation options
     */
    public PixelDataAnonymiser(final GiftCloudProperties giftCloudProperties) {
        burnInOverlays = giftCloudProperties.getBurnInOverlays();
        useZeroBlackoutValue = giftCloudProperties.getUseZeroBlackoutValue();
        usePixelPaddingBlackoutValue = giftCloudProperties.getUsePixelPaddingBlackoutValue();
        aeTitle = giftCloudProperties.getListenerAETitle();
        filters = readFilters();
    }


    /**
     * Creates a RedactedFileWrapper object and if necessary construct a new temporary file with pixel data redacted
     *
     * @param file the image file to be parsed and anonymised if necessary
     * @return a RedactedFileWrapper object which is used to return either a file suitable for uploading
     * @throws IOException if there was an error parsing or anonymising the file
     */
    public RedactedFileWrapper createRedactedFile(final File file) throws IOException {
        RedactedFileWrapper.FileRedactionStatus redactionStatus;
        Optional<File> redactedFile;

        if (anonymisationIsRequired()) {
            redactionStatus = RedactedFileWrapper.FileRedactionStatus.REDACTED;
            redactedFile = Optional.of(anonymisePixelData(file, getFilter()));

        } else {
            redactionStatus = RedactedFileWrapper.FileRedactionStatus.REDACTION_NOT_REQUIRED;
            redactedFile = Optional.empty();
        }


        return new RedactedFileWrapper(file, redactedFile, redactionStatus);
    }

    private File anonymisePixelData(final File inputFile, final PixelDataAnonymiseFilter filter) throws IOException {

        final File outputFile = File.createTempFile("pixel_data_anonymised", ".dcm");
        try {
            PixelDataAnonymiser.anonymisePixelDataUsingFilter(inputFile, outputFile, filter.getRedactedShapes(), burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, aeTitle);
        } catch (DicomException exception) {
            throw new IOException(exception.getCause());
        }
        return outputFile;
    }

    private List<PixelDataAnonymiseFilter> readFilters() {
        return new ArrayList<PixelDataAnonymiseFilter>(); // ToDo
    }

    private boolean anonymisationIsRequired() {
        return true; // ToDo
    }

    private PixelDataAnonymiseFilter getFilter() {
        return new PixelDataAnonymiseFilter(); // ToDo
    }

    private static void anonymisePixelDataUsingFilter(File inputFile, File outputFile, Vector shapes, boolean burnInOverlays, boolean usePixelPaddingBlackoutValue, boolean useZeroBlackoutValue, String ourAETitle) throws IOException, DicomException {
        AttributeList attributeList = readAttributeList(inputFile, true);
        if (attributeList == null) {
            throw new IOException("Could not read image");
        }

        String useSOPClassUID = Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.SOPClassUID);
        if (!SOPClass.isImageStorage(useSOPClassUID)) {
            throw new IOException("unsupported SOP Class " + useSOPClassUID);
        }

        String outputTransferSyntaxUID = null;
        if ((shapes != null && shapes.size() > 0) || burnInOverlays) {
            String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.TransferSyntaxUID);

            if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline) && !burnInOverlays && CapabilitiesAvailable.haveJPEGBaselineSelectiveBlockRedaction()) {
                // For a JPEG file we black out the image blocks

                outputTransferSyntaxUID = TransferSyntax.JPEGBaseline;

                // Perform a blackout of the JPEG blocks - this writes out to a temporary file
                File redactedJPEGFile = File.createTempFile("BlackoutJpegFile", ".dcm");
                try {
                    ImageEditUtilities.blackoutJPEGBlocks(inputFile, redactedJPEGFile, shapes);
                } catch (Exception e) {
                    throw new DicomException("JPEG blackout failed: " + e.getLocalizedMessage());
                }

                // Now read in the new attributes in from the temporary file
                attributeList = readAttributeList(redactedJPEGFile, true);

            } else {
                // For other files we black out the image data

                outputTransferSyntaxUID = TransferSyntax.ExplicitVRLittleEndian;

                SourceImage sImg = new SourceImage(attributeList);
                if (sImg == null) {
                    throw new DicomException("Could not read image");
                }

                ImageEditUtilities.blackout(sImg, attributeList, shapes, burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, 0);
                try {
                    sImg.close();
                } catch (Throwable throwable) {
                }

                attributeList.correctDecompressedImagePixelModule();
                attributeList.insertLossyImageCompressionHistoryIfDecompressed();
            }
        }

        addDeidentificationMethod(burnInOverlays, attributeList);

        // Set BurnedInAnnotation attribute to NO
        attributeList.remove(TagFromName.BurnedInAnnotation);
        Attribute a = new CodeStringAttribute(TagFromName.BurnedInAnnotation);
        a.addValue("NO");
        attributeList.put(a);

        // Update header attributes
        attributeList.removeGroupLengthAttributes();
        attributeList.removeMetaInformationHeaderAttributes();
        attributeList.remove(TagFromName.DataSetTrailingPadding);
        FileMetaInformation.addFileMetaInformation(attributeList, outputTransferSyntaxUID, ourAETitle);

        // Write out the new file
        attributeList.write(outputFile, outputTransferSyntaxUID, true/*useMeta*/, true/*useBufferedStream*/);
    }

    private static AttributeList readAttributeList(File currentFile, boolean decompressPixelData) throws IOException, DicomException {
        DicomInputStream i = new DicomInputStream(currentFile);
        AttributeList attributeList = new AttributeList();
        if (!decompressPixelData) {
            attributeList.setDecompressPixelData(decompressPixelData);
        }
        attributeList.read(i);
        i.close();
        return attributeList;
    }

    private static void addDeidentificationMethod(boolean burnInOverlays, AttributeList list) throws DicomException {
        {
            Attribute aDeidentificationMethod = list.get(TagFromName.DeidentificationMethod);
            if (aDeidentificationMethod == null) {
                aDeidentificationMethod = new LongStringAttribute(TagFromName.DeidentificationMethod);
                list.put(aDeidentificationMethod);
            }
            if (burnInOverlays) {
                aDeidentificationMethod.addValue("Overlays burned in then blacked out");
            }
            aDeidentificationMethod.addValue("Burned in text blacked out");
        }
        {
            SequenceAttribute aDeidentificationMethodCodeSequence = (SequenceAttribute) (list.get(TagFromName.DeidentificationMethodCodeSequence));
            if (aDeidentificationMethodCodeSequence == null) {
                aDeidentificationMethodCodeSequence = new SequenceAttribute(TagFromName.DeidentificationMethodCodeSequence);
                list.put(aDeidentificationMethodCodeSequence);
            }
            aDeidentificationMethodCodeSequence.addItem(new CodedSequenceItem("113101", "DCM", "Clean Pixel Data Option").getAttributeList());
        }
    }
}

