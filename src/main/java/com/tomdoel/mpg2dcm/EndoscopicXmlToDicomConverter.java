/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */


package com.tomdoel.mpg2dcm;

import org.apache.commons.io.FilenameUtils;
import org.dcm4che3.data.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Creates a DICOM file from an XML file describing an endoscopy data structure with MPEG-2 videos
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
public class EndoscopicXmlToDicomConverter {

    public static void convert(final File xmlInputFile, final String dicomOutputPath) throws IOException, SAXException, ParserConfigurationException, ParseException {
        // Parse the XML file
        final EndoscopicFileProcessor converter = new EndoscopicFileProcessor(xmlInputFile);

        // Generate DICOM tags from the XML file - these will be shared across all files
        final Attributes dicomAttributes = converter.getDicomAttributes();

        // Iterate over all video files
        for (final File videoFile : converter.getVideoFileNames()) {

            // Create a DICOM file in the output directory
            final File dicomOutputFile = new File(dicomOutputPath, FilenameUtils.getBaseName(videoFile.getName()) + ".dcm");

            // Add the shared DICOM tags and write to a DICOM file
            MpegFileConverter.convertWithAttributes(videoFile, dicomOutputFile, dicomAttributes);
        }
    }
}
