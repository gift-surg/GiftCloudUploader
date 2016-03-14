/**
 * Copyright (c) 2014 Washington University School of Medicine
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import com.google.common.io.ByteStreams;
import com.pixelmed.dicom.DicomException;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.*;
import org.dcm4che2.io.*;
import org.nrg.dcm.edit.AttributeException;
import org.nrg.dcm.edit.ScriptEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PixelDataAnonymiser;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DicomSeriesZipper extends SeriesZipper {
    private final Logger logger = LoggerFactory.getLogger(DicomSeriesZipper.class);

    private final PixelDataAnonymiser pixelDataAnonymiser;
    private final MetaDataAnonymiser metaDataAnonymiser;
    private final UploadParameters uploadParameters;
    private final StopTagInputHandler stopTagInputHandler;

    public DicomSeriesZipper(final DicomMetaDataAnonymiser metaDataAnonymiser, final PixelDataAnonymiser pixelDataAnonymiser, final UploadParameters uploadParameters) throws IOException {
        this.metaDataAnonymiser = metaDataAnonymiser;
        this.uploadParameters = uploadParameters;
        this.pixelDataAnonymiser = pixelDataAnonymiser;
        this.stopTagInputHandler = metaDataAnonymiser.makeStopTagInputHandler();
    }

    public void processNextFile(final File nextFile, final ZipOutputStream zos) throws AttributeException, IOException, ScriptEvaluationException, DicomException {
        final RedactedFileWrapper redactedFileWrapper = pixelDataAnonymiser.createRedactedFile(nextFile);
        try {
            addFileToZip(redactedFileWrapper.getFileToProcess(), zos, stopTagInputHandler);
        } finally {
            redactedFileWrapper.cleanup();
        }
    }

    private void addFileToZip(final File f, final ZipOutputStream zos, final DicomInputHandler handler)
    throws AttributeException,IOException,ScriptEvaluationException {
        final long remainder;
        IOException ioexception = null;
        final FileInputStream fin = new FileInputStream(f);
        final BufferedInputStream bis = new BufferedInputStream(f.getName().endsWith(".gz") ? new GZIPInputStream(fin) : fin);
        try {
            final DicomInputStream dis = new DicomInputStream(bis);
            if (dis.getAllocateLimit() < f.length()) {
                dis.setAllocateLimit((int)f.length());
            }
            try {
                if (null != handler) {
                    dis.setHandler(handler);
                }

                final DicomObject o = dis.readDicomObject();

                // Temporarily store patient details to confirm they have been modified by the anonymisation scripts
                final String originalPatientName = o.getString(Tag.PatientName);
                final String originalPatientId = o.getString(Tag.PatientID);
                final String originalPatientBirthDate = o.getString(Tag.PatientBirthDate);

                metaDataAnonymiser.anonymiseMetaData(f, uploadParameters, o);

                // Get the new patient details after anonymisation
                final String finalPatientName = o.getString(Tag.PatientName);
                final String finalPatientId = o.getString(Tag.PatientID);
                final String finalPatientBirthDate = o.getString(Tag.PatientBirthDate);

                // Check critical tags have been anonymised
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
