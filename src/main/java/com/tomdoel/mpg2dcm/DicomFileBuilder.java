/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */


package com.tomdoel.mpg2dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Create a DICOM file from an MPEG stream, with DICOM tags provided via an Attributes list, or specified from MPEG metadata, or both
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
public class DicomFileBuilder {

    private static final String transferSyntax = UID.MPEG2;
    private static final String charset = "ISO_IR 100";
    private static final String imageType = "ORIGINAL\\PRIMARY";
    private static final String MANUFACTURER = "mpg2dcm";
    private byte[] buffer = new byte[8192];
    private final Attributes dicomAttributes;

    /**
     * Create a DicomFileBuilder using the default DICOM tags
     */
    public DicomFileBuilder() {
        this(new Attributes());
    }

    /**
     * Create a DicomFileBuilder using the specified DICOM tags
     * @param dicomAttributes
     */
    public DicomFileBuilder(final Attributes dicomAttributes) {
        this.dicomAttributes = dicomAttributes;
    }

    /**
     * Create a DICOM file from the provided MPEG input stream
     *
     * @param dicomOutputFile File object describing the DICOM file to be created
     * @param mpgInput a DataInputStream to the MPEG file
     * @throws IOException
     */
    public void writeDicomFile(final File dicomOutputFile, final DataInputStream mpgInput) throws IOException {

        // Ensure that essential tags are set to default values if not already present
        setDefaultAttributes();

        // Create the DICOM file
        new CloseableResource<Void, DicomOutputStream>() {
            @Override
            public Void run() throws IOException {
                resource = new DicomOutputStream(dicomOutputFile);

                // Write out the DICOM headers
                resource.writeDataset(dicomAttributes.createFileMetaInformation(transferSyntax), dicomAttributes);

                // Write out the DICOM pixel data
                resource.writeHeader(Tag.PixelData, VR.OB, -1);
                int r;
                while ((r = mpgInput.read(buffer)) > 0) {
                    resource.write(buffer, 0, r);
                }
                resource.writeHeader(Tag.SequenceDelimitationItem, null, 0);
                return null;
            }
        }.tryWithResource();
    }

    /** Sets DICOM tags from MPEG metaheader information
     * @param metaData
     */
    public void applyMpegMetaHeader(final MpegMetaData metaData) {
        if (metaData.getRows().isPresent()) {
            dicomAttributes.setInt(Tag.Rows, VR.US, metaData.getRows().get());
        }
        if (metaData.getColumns().isPresent()) {
            dicomAttributes.setInt(Tag.Columns, VR.US, metaData.getColumns().get());
        }
        if (metaData.getFrameRate().isPresent()) {
            dicomAttributes.setString(Tag.CineRate, VR.IS, metaData.getFrameRate().get().getDicomFrameRateString());
        }
        if (metaData.getFrameRate().isPresent()) {
            dicomAttributes.setString(Tag.FrameTime, VR.DS, metaData.getFrameRate().get().getDicomFrameTimeString());
            dicomAttributes.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        }
        if (metaData.getAspectRatio().isPresent()) {
            dicomAttributes.setString(Tag.PixelAspectRatio, VR.IS, metaData.getAspectRatio().get().getAspectRatioDicomString());
        }
    }

    private void setDefaultAttributes() {

        // These tags are always set
        final Date currentDate = new Date();
        dicomAttributes.setDate(Tag.InstanceCreationDate, VR.DA, currentDate);
        dicomAttributes.setDate(Tag.InstanceCreationTime, VR.TM, currentDate);
        dicomAttributes.setString(Tag.Manufacturer, VR.LO, MANUFACTURER);

        // These tags are added if they don't already exist
        setDefaultCS(Tag.ImageType, imageType);
        setDefaultCS(Tag.SpecificCharacterSet, charset);
        setDefaultUS(Tag.BitsAllocated, 8);
        setDefaultUS(Tag.BitsStored, 8);
        setDefaultUS(Tag.HighBit, 7);
        setDefaultUS(Tag.PixelRepresentation, 0);
        setDefaultCS(Tag.PhotometricInterpretation, "YBR_PARTIAL_420");
        setDefaultCS(Tag.LossyImageCompression, "01");
        setDefaultUS(Tag.PlanarConfiguration, 0);
        setDefaultUS(Tag.SamplesPerPixel, 3);

        // Endoscopy
        setDefaultCS(Tag.Modality, "ES");
        setDefaultUI(Tag.SOPClassUID, "1.2.840.10008.5.1.4.1.1.77.1.1");

        // Force creation of UIDs if they are not already present
        setDefaultUI(Tag.StudyInstanceUID, UIDUtils.createUID());
        setDefaultUI(Tag.SeriesInstanceUID, UIDUtils.createUID());
        setDefaultUI(Tag.SOPInstanceUID, UIDUtils.createUID());
    }

    private void setDefaultCS(final int tag, final String value) {
        if (!dicomAttributes.containsValue(tag)) {
            dicomAttributes.setString(tag, VR.CS, value);
        }
    }

    private void setDefaultUS(final int tag, final int value) {
        if (!dicomAttributes.containsValue(tag)) {
            dicomAttributes.setInt(tag, VR.US, value);
        }
    }

    private void setDefaultUI(final int tag, final String uid) {
        if (!dicomAttributes.containsValue(tag)) {
            dicomAttributes.setString(tag, VR.UI, uid);
        }
    }
}
