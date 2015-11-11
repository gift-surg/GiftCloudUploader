package com.pixelmed.display;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.FileUtilities;

import java.io.File;
import java.io.IOException;

public class BlackoutCurrentImage {

    private AttributeList list;
    private SourceImage sImg;

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
        list = readAttributeList(currentFile, true);
        String useSOPClassUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.SOPClassUID);
        if (SOPClass.isImageStorage(useSOPClassUID)) {
            sImg = new SourceImage(list);
        } else {
            throw new DicomException("unsupported SOP Class " + useSOPClassUID);
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
