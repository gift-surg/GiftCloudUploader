package com.pixelmed.display;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.CapabilitiesAvailable;
import com.pixelmed.utils.FileUtilities;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class BlackoutCurrentImage {

    private AttributeList list;
    private SourceImage sImg;
    private File redactedJPEGFile;
    private BlackoutImage blackoutImage;

    public SourceImage getSourceImage() {
        return sImg;
    }

    public void loadDicomFileOrDirectory(String currentFileName) throws IOException, DicomException {
        File currentFile = FileUtilities.getFileFromNameInsensitiveToCaseIfNecessary(currentFileName);
        loadDicomFileOrDirectory(currentFile);
    }

    public AttributeList getDicomAttributes() {
        return list;
    }

    /**
     * <p>Load the named DICOM file and display it in the image panel.</p>
     *
     * @param    currentFile
     */
    protected void loadDicomFileOrDirectory(File currentFile) throws IOException, DicomException {
        blackoutImage = new BlackoutImage(currentFile);
        list = readAttributeList(currentFile, true);
        String useSOPClassUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.SOPClassUID);
        if (SOPClass.isImageStorage(useSOPClassUID)) {
            sImg = new SourceImage(list);
        } else {
            throw new DicomException("unsupported SOP Class " + useSOPClassUID);
        }
    }


    public void apply(Vector<Rectangle2D.Double> shapes, boolean burnInOverlays, boolean usePixelPaddingBlackoutValue, boolean useZeroBlackoutValue) throws Exception {
        if (sImg != null && list != null) {
            if ((shapes != null && shapes.size() > 0) || burnInOverlays) {
                String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.TransferSyntaxUID);
                if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline) && !burnInOverlays && CapabilitiesAvailable.haveJPEGBaselineSelectiveBlockRedaction()) {
                    if (redactedJPEGFile != null) {
                        redactedJPEGFile.delete();
                    }
                    redactedJPEGFile = File.createTempFile("RedactedImage", ".dcm");
                    ImageEditUtilities.blackoutJPEGBlocks(new File(blackoutImage.getCurrentFileName()), redactedJPEGFile, shapes);
                    // Need to re-read the file because we need to decompress the redacted JPEG to use to display it again
                    list = readAttributeList(redactedJPEGFile, true);
                    // do NOT delete redactedJPEGFile, since will reuse it when "saving", and also file may need to hang around for display of cached pixel data
                } else {
                    ImageEditUtilities.blackout(sImg, list, shapes, burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, 0);
                }
                sImg = new SourceImage(list);    // remake SourceImage, in case blackout() changed the AttributeList (e.g., removed overlays)
            } else {
            }
        }

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

}
