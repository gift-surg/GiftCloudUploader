/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Some parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

 ============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.io.Files;
import com.pixelmed.dicom.*;
import com.pixelmed.display.ImageEditUtilities;
import com.pixelmed.display.SourceImage;
import com.pixelmed.utils.CapabilitiesAvailable;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.RedactedFileWrapper;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.OneWayHash;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.Vector;


/**
 * Removes burnt-in patient identifiable data from images
 */
public class DicomPixelDataAnonymiser {

    private boolean burnInOverlays = false;
    private boolean useZeroBlackoutValue = false;
    private boolean usePixelPaddingBlackoutValue = true;
    private final String aeTitle;
    private final GiftCloudReporter reporter;
    private final PixelDataAnonymiserFilterCache filters;

    /**
     * Creates the DicomPixelDataAnonymiser object. Parameters will be set at construction time
     *
     * @param giftCloudProperties Shared properties use to define anonymsation options
     * @param reporter
     */
    public DicomPixelDataAnonymiser(final PixelDataAnonymiserFilterCache filters, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) {
        this.reporter = reporter;
        burnInOverlays = giftCloudProperties.getBurnInOverlays();
        useZeroBlackoutValue = giftCloudProperties.getUseZeroBlackoutValue();
        usePixelPaddingBlackoutValue = giftCloudProperties.getUsePixelPaddingBlackoutValue();
        aeTitle = giftCloudProperties.getListenerAETitle();
        this.filters = filters;
    }

    /**
     * Creates a RedactedFileWrapper object and if necessary construct a new temporary file with pixel data redacted
     *
     * @param file the image file to be parsed and anonymised if necessary
     * @param dataAlreadyAnonymised true if teh data has already been anonymised so no further anonymisation is required
     * @return a RedactedFileWrapper object which is used to return either a file suitable for uploading
     * @throws IOException if there was an error parsing or anonymising the file
     */
    public RedactedFileWrapper createRedactedFile(final File file, boolean dataAlreadyAnonymised) throws IOException, DicomException {
        RedactedFileWrapper.FileRedactionStatus redactionStatus;
        Optional<File> redactedFile;
        AttributeList attributeList = readAttributeList(file, true);
        if (attributeList == null) {
            throw new IOException("Could not read image");
        }

        if (!dataAlreadyAnonymised && anonymisationIsRequired(attributeList)) {
            final Optional<PixelDataAnonymiseFilter> filter = getFilter(attributeList);
            if (filter.isPresent()) {
                redactionStatus = RedactedFileWrapper.FileRedactionStatus.REDACTED;
                redactedFile = Optional.of(anonymisePixelData(file, filter.get(), attributeList.get(TagFromName.SOPInstanceUID).getStringValues()[0]));
            } else {
                redactionStatus = RedactedFileWrapper.FileRedactionStatus.NO_APPROPRIATE_FILTER_FOUND;
                redactedFile= Optional.empty();
            }

        } else {
            redactionStatus = RedactedFileWrapper.FileRedactionStatus.REDACTION_NOT_REQUIRED;
            redactedFile = Optional.empty();
        }


        return new RedactedFileWrapper(file, redactedFile, redactionStatus);
    }

    private File anonymisePixelData(final File inputFile, final PixelDataAnonymiseFilter filter, final String filePrefix) throws IOException {

        final String safePrefix = StringUtils.isNotBlank(filePrefix) ? filePrefix : OneWayHash.hashUid(inputFile.getName());
        final File outputFile = new File(Files.createTempDir(), safePrefix + ".dcm");
        try {
            DicomPixelDataAnonymiser.anonymisePixelDataUsingFilter(inputFile, outputFile, filter.getRedactedShapesAsShapeVector(), burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, aeTitle);
        } catch (DicomException exception) {
            throw new IOException(exception.getCause());
        }
        return outputFile;
    }

    private boolean anonymisationIsRequired(AttributeList attributeList) {
        final String burntInAnnotations = Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.BurnedInAnnotation);

        // If the BurntInAnnotation tag is set, this tells us whether or not PID is contained in the pixel data
        if (burntInAnnotations.equals("NO")) {
            return false;
        } else if (burntInAnnotations.equals("YES")) {
            return true;
        }

        // Otherwise we will check for standard ultrasound images which contain JPEG or similar formats
        final String sopClassUID = Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.SOPClassUID);

        if (sopClassUID.equals(SOPClass.UltrasoundImageStorage) || sopClassUID.equals(SOPClass.UltrasoundImageStorageRetired) || (sopClassUID.equals(SOPClass.UltrasoundMultiframeImageStorage) || sopClassUID.equals(SOPClass.UltrasoundMultiframeImageStorageRetired))) {
            return true;
        }

        return false;
    }

    private Optional<PixelDataAnonymiseFilter> getFilter(AttributeList attributeList) {
        for (final PixelDataAnonymiseFilter filter : filters.getFilters()) {
            try {
                if (filter.matches(attributeList)) {
                    return Optional.of(filter);
                }
            } catch (DicomException e) {
                reporter.silentLogException(e, "Problem when comparing filter tag values");
            }
        }
        return Optional.empty();
    }

    private static void anonymisePixelDataUsingFilter(File inputFile, File outputFile, Vector shapes, boolean burnInOverlays, boolean usePixelPaddingBlackoutValue, boolean useZeroBlackoutValue, String ourAETitle) throws IOException, DicomException {
        final AttributeList headers = readHeaders(inputFile);
        if (headers == null) {
            throw new IOException("Could not read image");
        }

        String useSOPClassUID = Attribute.getSingleStringValueOrEmptyString(headers, TagFromName.SOPClassUID);
        if (!SOPClass.isImageStorage(useSOPClassUID)) {
            throw new IOException("unsupported SOP Class " + useSOPClassUID);
        }

        String outputTransferSyntaxUID = TransferSyntax.ExplicitVRLittleEndian;

        AttributeList finalAttributeList = null;

        if ((shapes != null && shapes.size() > 0) || burnInOverlays) {
            String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(headers, TagFromName.TransferSyntaxUID);

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
                finalAttributeList = readAttributeList(redactedJPEGFile, false);

            } else {
                // For other files we black out the image data

                outputTransferSyntaxUID = TransferSyntax.ExplicitVRLittleEndian;

                finalAttributeList = readAttributeList(inputFile, true);
                SourceImage sImg = new SourceImage(finalAttributeList);
                if (sImg == null) {
                    throw new DicomException("Could not read image");
                }

                ImageEditUtilities.blackout(sImg, finalAttributeList, shapes, burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, 0);
                try {
                    sImg.close();
                } catch (Throwable throwable) {
                }

                finalAttributeList.correctDecompressedImagePixelModule();
                finalAttributeList.insertLossyImageCompressionHistoryIfDecompressed();
            }
        }

        addDeidentificationMethod(burnInOverlays, finalAttributeList);

        // Set BurnedInAnnotation attribute to NO
        finalAttributeList.remove(TagFromName.BurnedInAnnotation);
        Attribute a = new CodeStringAttribute(TagFromName.BurnedInAnnotation);
        a.addValue("NO");
        finalAttributeList.put(a);

        // Update header attributes
        finalAttributeList.removeGroupLengthAttributes();
        finalAttributeList.removeMetaInformationHeaderAttributes();
        finalAttributeList.remove(TagFromName.DataSetTrailingPadding);
        FileMetaInformation.addFileMetaInformation(finalAttributeList, outputTransferSyntaxUID, ourAETitle);

        // Write out the new file
        finalAttributeList.write(outputFile, outputTransferSyntaxUID, true/*useMeta*/, true/*useBufferedStream*/);
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

    private static AttributeList readHeaders(final File currentFile) throws IOException, DicomException {
        DicomInputStream i = new DicomInputStream(currentFile);
        AttributeList attributeList = new AttributeList();
        attributeList.read(i, TagFromName.PixelData);
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
                aDeidentificationMethod.addValue("GIFT-Cloud overlays burned in then blacked out");
            }
            aDeidentificationMethod.addValue("GIFT-Cloud Burned in text blacked out");
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

