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

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Application for converting an MPEG-2 file to DICOM
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
public class Mpg2Dcm {
    public static void main(String[] args) {
        try {
            final Options helpOptions = new Options();
            helpOptions.addOption("h", false, "Print help for this application");

            final DefaultParser parser = new DefaultParser();
            final CommandLine commandLine = parser.parse(helpOptions, args);

            if (commandLine.hasOption('h')) {
                final String helpHeader = "Converts an mpeg2 video file to Dicom\n\n";
                String helpFooter = "\nPlease report issues at github.com/tomdoel/mpg2dcm";

                final HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Mpg2Dcm mpegfile dicomfile", helpHeader, helpOptions, helpFooter, true);

            } else {
                final List<String> remainingArgs = commandLine.getArgList();
                if (remainingArgs.size() < 2) {
                    throw new org.apache.commons.cli.ParseException("ERROR : Not enough arguments specified.");
                }

                final String mpegFileName = remainingArgs.get(0);
                final String dicomFileName = remainingArgs.get(1);
                final File mpegFile = new File(mpegFileName);
                final File dicomOutputFile = new File(dicomFileName);
                MpegFileConverter.convert(mpegFile, dicomOutputFile);
            }
        } catch (org.apache.commons.cli.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}