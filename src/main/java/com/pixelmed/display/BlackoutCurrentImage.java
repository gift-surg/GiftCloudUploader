package com.pixelmed.display;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.CapabilitiesAvailable;
import com.pixelmed.utils.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class BlackoutCurrentImage {

    private AttributeList list;
    private SourceImage sImg;
    private boolean changesWereMade;
    private boolean usedjpegblockredaction;
    private String currentFileName;
    private File redactedJPEGFile;

    public BlackoutCurrentImage() {
    }

    public SourceImage getSourceImage() {
        return sImg;
    }

    public void save(String currentFileName, boolean burnInOverlays, String ourAETitle, int burnedinflag) throws IOException, DicomException {
        boolean success = true;
        try {
            sImg.close();        // in case memory-mapped pixel data open; would inhibit Windows rename or copy/reopen otherwise
            sImg = null;
            System.gc();                    // cannot guarantee that buffers will be released, causing problems on Windows, but try ... http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154 :(
            System.runFinalization();
            System.gc();
        } catch (Throwable t) {
            // Save failed - unable to close image - not saving modifications
            success = false;
        }
        File currentFile = new File(currentFileName);
        File newFile = new File(currentFileName + ".new");
        if (success) {
            String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.TransferSyntaxUID);
            try {
                String outputTransferSyntaxUID = null;
                if (usedjpegblockredaction && redactedJPEGFile != null) {
                    // do not repeat the redaction, reuse redactedJPEGFile, without decompressing the pixels, so that we can update the technique stuff in the list
                    DicomInputStream i = new DicomInputStream(redactedJPEGFile);
                    list = new AttributeList();
                    list.setDecompressPixelData(false);
                    list.read(i);
                    i.close();
                    outputTransferSyntaxUID = TransferSyntax.JPEGBaseline;
                } else {
                    outputTransferSyntaxUID = TransferSyntax.ExplicitVRLittleEndian;
                    list.correctDecompressedImagePixelModule();
                    list.insertLossyImageCompressionHistoryIfDecompressed();
                }
                if (burnedinflag != BurnedInAnnotationFlagAction.LEAVE_ALONE) {
                    list.remove(TagFromName.BurnedInAnnotation);
                    if (burnedinflag == BurnedInAnnotationFlagAction.ADD_AS_NO_IF_SAVED
                            || (burnedinflag == BurnedInAnnotationFlagAction.ADD_AS_NO_IF_CHANGED && changesWereMade)) {
                        Attribute a = new CodeStringAttribute(TagFromName.BurnedInAnnotation);
                        a.addValue("NO");
                        list.put(a);
                    }
                }
                if (changesWereMade) {
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
                list.removeGroupLengthAttributes();
                list.removeMetaInformationHeaderAttributes();
                list.remove(TagFromName.DataSetTrailingPadding);

                FileMetaInformation.addFileMetaInformation(list, outputTransferSyntaxUID, ourAETitle);
                list.write(newFile, outputTransferSyntaxUID, true/*useMeta*/, true/*useBufferedStream*/);

                list = null;
                try {
                    currentFile.delete();
                    FileUtilities.renameElseCopyTo(newFile, currentFile);
                } catch (IOException e) {
                    // Unable to rename or copy - save failed - not saving modifications
                    success = false;
                }

                if (redactedJPEGFile != null) {
                    redactedJPEGFile.delete();
                    redactedJPEGFile = null;
                }
                usedjpegblockredaction = false;

                changesWereMade = false;
                // "Save of "+currentFileName+" succeeded"
            } catch (DicomException e) {
                // Save failed
            } catch (IOException e) {
                // Save failed
            }
        }
        loadDicomFileOrDirectory(currentFile);
    }

    public void loadDicomFileOrDirectory(String currentFileName) throws IOException, DicomException {
        File currentFile = FileUtilities.getFileFromNameInsensitiveToCaseIfNecessary(currentFileName);
        loadDicomFileOrDirectory(currentFile);
    }

    /**
     * <p>Load the named DICOM file and display it in the image panel.</p>
     *
     * @param    currentFile
     */
    protected void loadDicomFileOrDirectory(File currentFile) throws IOException, DicomException {
        changesWereMade = false;
        currentFileName = currentFile.getAbsolutePath();        // set to what we actually used, used for later save
        DicomInputStream i = new DicomInputStream(currentFile);
        list = new AttributeList();
        list.read(i);
        i.close();
        String useSOPClassUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.SOPClassUID);
        if (SOPClass.isImageStorage(useSOPClassUID)) {
            sImg = new SourceImage(list);
        } else {
            throw new DicomException("unsupported SOP Class " + useSOPClassUID);
        }
    }

    public void apply(Vector shapes, boolean burnInOverlays, boolean usePixelPaddingBlackoutValue, boolean useZeroBlackoutValue) throws Exception {
        if (sImg != null && list != null) {
            if ((shapes != null && shapes.size() > 0) || burnInOverlays) {
                changesWereMade = true;
                String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.TransferSyntaxUID);
                if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline) && !burnInOverlays && CapabilitiesAvailable.haveJPEGBaselineSelectiveBlockRedaction()) {
                    usedjpegblockredaction = true;
                    if (redactedJPEGFile != null) {
                        redactedJPEGFile.delete();
                    }
                    redactedJPEGFile = File.createTempFile("DicomImageBlackout", ".dcm");
                    ImageEditUtilities.blackoutJPEGBlocks(new File(currentFileName), redactedJPEGFile, shapes);
                    // Need to re-read the file because we need to decompress the redacted JPEG to use to display it again
                    DicomInputStream i = new DicomInputStream(redactedJPEGFile);
                    list = new AttributeList();
                    list.read(i);
                    i.close();
                    // do NOT delete redactedJPEGFile, since will reuse it when "saving", and also file may need to hang around for display of cached pixel data
                } else {
                    usedjpegblockredaction = false;
                    ImageEditUtilities.blackout(sImg, list, shapes, burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, 0);
                }
                sImg = new SourceImage(list);    // remake SourceImage, in case blackout() changed the AttributeList (e.g., removed overlays)
            } else {
            }
        }

    }

    public boolean UnsavedChanges() {
        return changesWereMade;
    }

    public int getNumberOfImages() {
        return Attribute.getSingleIntegerValueOrDefault(list, TagFromName.NumberOfFrames, 1);
    }

    /**
     * <p>A class of values for the Burned in Annotation action argument of the DicomImageBlackout constructor.</p>
     */
    public abstract class BurnedInAnnotationFlagAction {
        private BurnedInAnnotationFlagAction() {
        }

        /**
         * <p>Leave any existing Burned in Annotation attribute value alone.</p>
         */
        public static final int LEAVE_ALONE = 1;
        /**
         * <p>Always remove the Burned in Annotation attribute when the file is saved, without replacing it.</p>
         */
        public static final int ALWAYS_REMOVE = 2;
        /**
         * <p>Always remove the Burned in Annotation attribute when the file is saved, only replacing it and using a value of NO when regions have been blacked out.</p>
         */
        public static final int ADD_AS_NO_IF_CHANGED = 3;
        /**
         * <p>Always remove the Burned in Annotation attribute when the file is saved, always replacing it with a value of NO,
         * regardless of whether when regions have been blacked out, such as when visual inspection confirms that there is no
         * burned in annotation.</p>
         */
        public static final int ADD_AS_NO_IF_SAVED = 4;
    }


}
