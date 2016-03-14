/**
 * Copyright (c) 2014 Washington University School of Medicine
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import com.pixelmed.dicom.DicomException;
import org.nrg.dcm.edit.AttributeException;
import org.nrg.dcm.edit.ScriptEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public abstract class SeriesZipper {
    private final Logger logger = LoggerFactory.getLogger(SeriesZipper.class);

    protected static String removeCompressionSuffix(final String path) {
        return path.replaceAll("\\.[gG][zZ]$", "");
    }

    public void buildSeriesZipFile(final File f, final FileCollection seriesFileCollection)
            throws IOException, AttributeException, ScriptEvaluationException {
        logger.debug("creating zip file {}", f);
        IOException ioexception = null;
        final FileOutputStream fos = new FileOutputStream(f);
        try {
            final ZipOutputStream zos = new ZipOutputStream(fos);
            try {
                logger.trace("adding {} files for series {}", seriesFileCollection.getFileCount(), seriesFileCollection);
                for (final File file : seriesFileCollection.getFiles()) {
                    processNextFile(file, zos);
                }
            } catch (DicomException e) {
                logger.trace("DicomException exception building zipfile", e);
                throw ioexception = new IOException(e);
            } catch (IOException e) {
                logger.trace("I/O exception building zipfile", e);
                throw ioexception = e;
            } finally {
                try {
                    zos.close();
                } catch (IOException e) {
                    if (null == ioexception) {
                        throw ioexception = e;
                    } else {
                        logger.error("unable to close series zip stream", e);
                        throw ioexception;
                    }
                }
            }
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                if (null == ioexception) {
                    throw ioexception = e;
                } else {
                    logger.error("unable to close series zip file", e);
                    throw ioexception;
                }
            }
        }
        logger.debug("zip file built");
    }
    
    public File buildSeriesZipFile(final FileCollection seriesFileCollection)
            throws IOException, AttributeException, ScriptEvaluationException {
        final File seriesZipFile = File.createTempFile("series", ".zip");
        try {
            buildSeriesZipFile(seriesZipFile, seriesFileCollection);
            return seriesZipFile;
        } catch (IOException e) {
            seriesZipFile.delete();
            throw e;
        } catch (AttributeException e) {
            seriesZipFile.delete();
            throw e;
        } catch (ScriptEvaluationException e) {
            seriesZipFile.delete();
            throw e;
        } catch (RuntimeException e) {
            seriesZipFile.delete();
            throw e;
        } catch (Error e) {
            seriesZipFile.delete();
            throw e;
        }
    }

    public abstract void processNextFile(final File nextFile, final ZipOutputStream zos) throws AttributeException, IOException, ScriptEvaluationException, DicomException;
}
