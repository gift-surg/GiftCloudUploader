/*

 Author: Tom Doel

 Some parts of this software were derived from DicomCleaner, Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.


 */

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.*;
import com.pixelmed.display.ImageEditUtilities;
import com.pixelmed.display.SourceImage;
import com.pixelmed.utils.CapabilitiesAvailable;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Vector;


public class PixelDataAnonymiser {

    public static void anonymisePixelData(File inputFile, File outputFile, Vector<Rectangle2D.Double> shapes, boolean burnInOverlays, boolean usePixelPaddingBlackoutValue, boolean useZeroBlackoutValue, String ourAETitle) throws IOException, DicomException {
        AttributeList attributeList = readAttributeList(inputFile, true);
        if (attributeList == null) {
            throw new DicomException("Could not read image");
        }

        String useSOPClassUID = Attribute.getSingleStringValueOrEmptyString(attributeList, TagFromName.SOPClassUID);
        if (!SOPClass.isImageStorage(useSOPClassUID)) {
            throw new DicomException("unsupported SOP Class " + useSOPClassUID);
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

