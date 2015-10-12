/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */

package com.tomdoel.mpg2dcm;

import org.dcm4che3.data.Attributes;
import uk.ac.ucl.cs.cmic.giftcloud.util.CloseableResource;

import java.io.*;


/**
 * Converts MPEG2 files into DICOM
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
public class MpegFileConverter{

    /**
     * Convert an MPEG2 file to DICOM. Tags will be derived from the MPEG header or set to sensible default values
     *
     * @param mpegFile the MPEG2 file to convert
     * @param dicomOutputFile the DICOM file that will be produced
     * @throws IOException if the MPEG file could not be opened or the DICOM file could not be written
     */
    public static void convert(final File mpegFile, final File dicomOutputFile) throws IOException {
        convertWithAttributes(mpegFile, dicomOutputFile, new Attributes());
    }

    /**
     * Convert an MPEG2 file to DICOM. Tags will be derived from the MPEG header. Any tags specified in the dicomAttributes parameter will also be set. Certain important tags will be set to sensible default values if they have not been in
     * included in dicomAttributes
     *
     * @param mpegFile the MPEG2 file to convert
     * @param dicomOutputFile the DICOM file that will be produced
     * @param dicomAttributes contains DICOM tags which should be applied to the output DICOM file. These will override default values.
     * @throws IOException if the MPEG file could not be opened or the DICOM file could not be written
     */
    public static void convertWithAttributes(final File mpegFile, final File dicomOutputFile, final Attributes dicomAttributes) throws IOException {
        new CloseableResource<Void, BufferedInputStream>() {
            @Override
            public Void run() throws IOException {
                resource = new BufferedInputStream(new FileInputStream(mpegFile));
                // Mark the stream because the file will be parsed initially to find the metadata
                resource.mark(0);
                final DataInputStream dataInputStream = new DataInputStream(resource);
                final DicomFileBuilder dicomFileBuilder = new DicomFileBuilder(dicomAttributes);

                // Set Dicom attributes based on the metadata from the mpeg file
                dicomFileBuilder.applyMpegMetaHeader(MpegMetaData.getMetaDataFromMpegStream(dataInputStream));

                // Reset the stream to start from the beginning of the file
                resource.reset();

                // Write the
                dicomFileBuilder.writeDicomFile(dicomOutputFile, dataInputStream);

                return null;
            }
        }.tryWithResource();
    }
}
