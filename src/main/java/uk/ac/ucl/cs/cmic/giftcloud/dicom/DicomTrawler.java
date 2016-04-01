/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.DicomTrawler
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.StopTagInputHandler;
import org.nrg.dcm.DicomUtils;
import org.nrg.util.EditProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.data.Study;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.ZipSeriesUploader;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;
import uk.ac.ucl.cs.cmic.giftcloud.util.MapRegistry;
import uk.ac.ucl.cs.cmic.giftcloud.util.Registry;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class DicomTrawler implements Trawler {

    public static final int MAX_TAG = Collections.max(new ArrayList<Integer>() {{
        add(Tag.AcquisitionNumber);
        add(Tag.SOPClassUID);
        add(Tag.SeriesDescription);
    }});

    public static final int APP_MAX_TAG = Collections.max(new ArrayList<Integer>() {{
        add(MAX_TAG);
        add(Series.MAX_TAG);
        add(DicomTrawler.getSeriesMaxTags());
        add(DicomStudy.MAX_TAG);
        add(ZipSeriesUploader.MAX_TAG);
    }});

    private static int getSeriesMaxTags() {
        return Collections.max(new ArrayList<Integer>() {
            {
                add(Tag.SeriesDescription);
                add(Tag.SeriesNumber);
            }});
    }

    private final Logger logger = LoggerFactory.getLogger(DicomTrawler.class);

    private final List<GiftCloudUploaderError> errors = new ArrayList<GiftCloudUploaderError>();

	/* (non-Javadoc)
	 * @see uk.ac.ucl.cs.cmic.giftcloud.dicom.Trawler#trawl(java.util.Iterator, java.util.Collection)
	 */
	public Collection<Study> trawl(final Iterator<File> files, final Collection<File> remaining, EditProgressMonitor pm, final SeriesImportFilterApplicatorRetriever filters) {
		final Registry<DicomStudy> studies = new MapRegistry<DicomStudy>();
		while (files.hasNext()) {
			if (null != pm && pm.isCanceled()) {
				return new ArrayList<Study>();
			}
			final File f = files.next();
			if (f.isFile()) {
				final DicomObject o;
				try {
					o = DicomUtils.read(f, new StopTagInputHandler(APP_MAX_TAG + 1)); // We don't need anything higher than this tag.
				} catch (IOException e) {
					remaining.add(f);
					continue;
				} catch (Exception e) {
					remaining.add(f);
					continue;
				}
                if (StringUtils.isBlank(o.getString(Tag.SOPClassUID))) {
                    errors.add(GiftCloudUploaderError.SOP_CLASS_UID_NOT_FOUND);
                    logger.debug("Invalid DICOM file: SOPClassUID is not specified in file " + f.getAbsolutePath(), "");
                    continue;
                }
                if (StringUtils.isBlank(o.getString(Tag.PatientID))) {
                    errors.add(GiftCloudUploaderError.PATIENT_ID_NOT_FOUND);
                    logger.debug("The Patient ID is not specified in file " + f.getAbsolutePath(), "");
                    remaining.add(f);
                    continue;
                }
				assert null != o.getString(Tag.SOPClassUID);
                final String modality = o.getString(Tag.Modality);
                if (!modalityIsSupported(modality)) {
                    errors.add(GiftCloudUploaderError.MODALITY_UNSUPPORTED);
                    remaining.add(f);
                    logger.debug("Modality " + modality + "is not supported for file " + f.getAbsolutePath(), "");

                } else {

                    if (filters != null) {
                        logger.debug("Found series import filters, testing series for inclusion/exclusion.");
                        final String description = o.getString(Tag.SeriesDescription);
                        logger.debug("Found series description: {}", description);
                        if (filters.checkSeries(description)) {
                            logger.debug("Series description {} matched series import filter restrictions, including in session", description);
                            final DicomStudy dicomStudy = studies.get(new DicomStudy(o));
                            dicomStudy.addFileAndGetSeries(o, f);
                        } else {
                            logger.debug("Series description {} did not match series import filter restrictions, excluding from session", description);
                        }
                    } else {
                        logger.debug("Series import filters not found, including series in session");
                        final DicomStudy dicomStudy = studies.get(new DicomStudy(o));
                        dicomStudy.addFileAndGetSeries(o, f);
                    }
                }


            }
		}
		
		return new ArrayList<Study>(studies.getAll());
	}

    public final List<GiftCloudUploaderError> getErrorMessages() {
        return errors;
    }

    private boolean modalityIsSupported(final String modality) {
        if (StringUtils.isBlank(modality)) {
            return false;
        } else if (modality.equals("MR")) {
            return true;
        } else if (modality.equals("ES")) {
            return true;
        } else if (modality.equals("CT")) {
            return true;
        } else if (modality.equals("US")) {
            return true;
        } else {
            return false;
        }
    }
}
