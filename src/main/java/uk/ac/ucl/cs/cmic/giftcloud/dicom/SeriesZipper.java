/**
 * Copyright (c) 2014 Washington University School of Medicine
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.*;
import org.dcm4che2.io.*;
import org.nrg.dcm.edit.AttributeException;
import org.nrg.dcm.edit.ScriptApplicator;
import org.nrg.dcm.edit.ScriptEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploaderError;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class SeriesZipper {
    private final Logger logger = LoggerFactory.getLogger(SeriesZipper.class);
    
    private final Iterable<ScriptApplicator> applicators;
    private final StopTagInputHandler stopTagInputHandler;
    
    public SeriesZipper(final Iterable<ScriptApplicator> scriptApplicators) {
        this.applicators = ImmutableList.copyOf(scriptApplicators);
        this.stopTagInputHandler = makeStopTagInputHandler(this.applicators);
    }

    private static String removeCompressionSuffix(final String path) {
        return path.replaceAll("\\.[gG][zZ]$", "");
    }
    
    private static StopTagInputHandler makeStopTagInputHandler(final Iterable<ScriptApplicator> scriptApplicators) {
        long top = Tag.SOPInstanceUID;
        for (final ScriptApplicator a : scriptApplicators) {
            final long atop = 0xffffffffL & a.getTopTag();
            if (atop > top) {
                if (0xffffffffL == atop) {  // this means no stop tag
                    return null;
                } else {
                    top = atop;
                }
            }
        }
        return new StopTagInputHandler((int)(top+1));
    }
    
    public StopTagInputHandler getStopTagInputHandler() {
        return stopTagInputHandler;
    }
    
    public void buildSeriesZipFile(final File f, final FileCollection series)
            throws IOException, AttributeException, ScriptEvaluationException {
        logger.debug("creating zip file {}", f);
        IOException ioexception = null;
        final FileOutputStream fos = new FileOutputStream(f);
        try {
            final ZipOutputStream zos = new ZipOutputStream(fos);
            try {
                logger.trace("adding {} files for series {}", series.getFileCount(), series);
                for (final File file : series.getFiles()) {
                    addFileToZip(file, zos, getStopTagInputHandler());
                }
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
    
    public File buildSeriesZipFile(final FileCollection series)
            throws IOException, AttributeException, ScriptEvaluationException {
        final File zipf = File.createTempFile("series", ".zip");
        try {
            buildSeriesZipFile(zipf, series);
            return zipf;
        } catch (IOException e) {
            zipf.delete();
            throw e;
        } catch (AttributeException e) {
            zipf.delete();
            throw e;
        } catch (ScriptEvaluationException e) {
            zipf.delete();
            throw e;
        } catch (RuntimeException e) {
            zipf.delete();
            throw e;
        } catch (Error e) {
            zipf.delete();
            throw e;
        }
    }

    public void addFileToZip(final File f, final ZipOutputStream zos, final DicomInputHandler handler)
    throws AttributeException,IOException,ScriptEvaluationException {
        final long remainder;
        IOException ioexception = null;
        final FileInputStream fin = new FileInputStream(f);
        final BufferedInputStream bis = new BufferedInputStream(f.getName().endsWith(".gz") ? new GZIPInputStream(fin) : fin);
        try {
            final DicomInputStream dis = new DicomInputStream(bis);
            try {
                if (null != handler) {
                    dis.setHandler(handler);
                }

                final DicomObject o = dis.readDicomObject();

                // Temporarily store patient details to confirm they have been modified by the anonymisation scripts
                final String originalPatientName = o.getString(Tag.PatientName);
                final String originalPatientId = o.getString(Tag.PatientID);
                final String originalPatientBirthDate = o.getString(Tag.PatientBirthDate);

                for (final ScriptApplicator a : applicators) {
                    a.apply(f, o);
                }

                // Get the new patient details after anonymisation
                final String finalPatientName = o.getString(Tag.PatientName);
                final String finalPatientId = o.getString(Tag.PatientID);
                final String finalPatientBirthDate = o.getString(Tag.PatientBirthDate);

                // Check critial tags have been anonymised
                if (StringUtils.isNotBlank(finalPatientName) && finalPatientName.equals(originalPatientName)) {
                    throw new GiftCloudException(GiftCloudUploaderError.ANONYMISATION_UNACCEPTABLE);
                }
                if (StringUtils.isNotBlank(finalPatientId) && finalPatientId.equals(originalPatientId)) {
                    throw new GiftCloudException(GiftCloudUploaderError.ANONYMISATION_UNACCEPTABLE);
                }
                if (StringUtils.isNotBlank(finalPatientBirthDate) && finalPatientBirthDate.equals(originalPatientBirthDate)) {
                    throw new GiftCloudException(GiftCloudUploaderError.ANONYMISATION_UNACCEPTABLE);
                }

                final String tsuid = o.getString(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
                final TransferSyntax tsOriginal = TransferSyntax.valueOf(tsuid);
                if (tsOriginal.deflated() && null != handler) {
                    // Can't do a simple binary copy with deflated transfer syntax.
                    // Use no stop handler because we have to deserialize and serialize the entire object.
                    try {
                        dis.close();
                    } catch (IOException ignore) {}
                    addFileToZip(f, zos, null);
                    return;
                }
                final TransferSyntax ts = tsOriginal.deflated() ? TransferSyntax.ExplicitVRLittleEndian : tsOriginal;
                
                zos.putNextEntry(new ZipEntry(removeCompressionSuffix(f.getName())));
                @SuppressWarnings("resource")
                final DicomOutputStream dos = new DicomOutputStream(zos);
                dos.setAutoFinish(false);          // Don't let DicomOutputStream finish the ZipOutputStream.
                final DicomObject fmi = new BasicDicomObject();
                fmi.initFileMetaInformation(o.getString(Tag.SOPClassUID), o.getString(Tag.SOPInstanceUID), ts.uid());
                dos.writeFileMetaInformation(fmi);
                dos.writeDataset(o, ts);
                if (null != handler) {
                    bis.reset();
                    remainder = ByteStreams.copy(bis, zos);
                } else {
                    remainder = 0;
                }
            } catch (IOException e) {
                throw ioexception = e;
            } finally {
                try {
                    dis.close();
                } catch (IOException e) {
                    throw ioexception = (null == ioexception) ? e : ioexception;
                }
            }
        } catch (DicomCodingException e) {
            logger.debug("error reading " + f, e);
            return;
        } catch (IOException e) {
            throw ioexception = (null == ioexception) ? e : ioexception;
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                if (null == ioexception) {
                    ioexception = e;
                } else {
                    logger.error("error closing stream buffer", e);
                }
            }
            try {
                fin.close();
            } catch (IOException e) {
                if (null == ioexception) {
                    ioexception = e;
                } else {
                    logger.error("error closing input DICOM file", e);
                }
            }
            if (null != ioexception) {
                throw ioexception;
            }
        }

        zos.closeEntry();
        logger.trace("added {}, {} bytes streamed", f, remainder);
    }
}
