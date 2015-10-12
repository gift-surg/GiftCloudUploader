/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */

package com.tomdoel.mpg2dcm;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Application for creating a DICOM file from an XML file describing an endoscopy data structure with MPEG-2 videos
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
public class Xml2Dicom {

    public static void main(String[] args) {
        try {
            final Options helpOptions = new Options();
            helpOptions.addOption("h", false, "Print help for this application");

            final DefaultParser parser = new DefaultParser();
            final CommandLine commandLine = parser.parse(helpOptions, args);

            if (commandLine.hasOption('h')) {
                final String helpHeader = "Converts an endoscopic xml and video files to Dicom\n\n";
                String helpFooter = "\nPlease report issues at github.com/tomdoel/mpg2dcm";

                final HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Xml2Dcm xml-file dicom-output-path", helpHeader, helpOptions, helpFooter, true);

            } else {
                final List<String> remainingArgs = commandLine.getArgList();
                if (remainingArgs.size() < 2) {
                    throw new org.apache.commons.cli.ParseException("ERROR : Not enough arguments specified.");
                }

                final String xmlInputFileName = remainingArgs.get(0);
                final String dicomOutputPath = remainingArgs.get(1);
                EndoscopicXmlToDicomConverter.convert(new File(xmlInputFileName), dicomOutputPath);
            }
        } catch (org.apache.commons.cli.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}