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
import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.SessionReviewPanel;
import org.nrg.util.EditProgressMonitor;
import uk.ac.ucl.cs.cmic.giftcloud.util.MapRegistry;
import uk.ac.ucl.cs.cmic.giftcloud.util.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public final class DicomTrawler implements Trawler {

    public static final int MAX_TAG = Collections.max(new ArrayList<Integer>() {{
        add(Tag.AcquisitionNumber);
        add(Tag.SOPClassUID);
        add(Tag.SeriesDescription);
    }});

    public static final int APP_MAX_TAG = Collections.max(new ArrayList<Integer>() {{
        add(MAX_TAG);
        add(Series.MAX_TAG);
        add(SessionReviewPanel.MAX_TAG);
        add(Study.MAX_TAG);
        add(CallableUploader.MAX_TAG);
    }});

    private SeriesImportFilterApplicatorRetriever _filters;
    private final Logger logger = LoggerFactory.getLogger(DicomTrawler.class);

    /* (non-Javadoc)
         * @see uk.ac.ucl.cs.cmic.giftcloud.dicom.Trawler#trawl(java.util.Iterator)
         */
	public Collection<Session> trawl(final Iterator<File> files, EditProgressMonitor pm) {
		return trawl(files, null, pm);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ucl.cs.cmic.giftcloud.dicom.Trawler#trawl(java.util.Iterator, java.util.Collection)
	 */
	public Collection<Session> trawl(final Iterator<File> files, final Collection<File> remaining, EditProgressMonitor pm) {
		final Registry<Study> studies = new MapRegistry<Study>();
		while (files.hasNext()) {
			if (null != pm && pm.isCanceled()) {
				return new ArrayList<Session>();
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
				assert null != o.getString(Tag.SOPClassUID);
                final String modality = o.getString(Tag.Modality);
                if (!modalityIsSupported(modality)) {
                    logger.debug("This modality is not supported", "");

                } else {

                    if (_filters != null) {
                        logger.debug("Found series import filters, testing series for inclusion/exclusion.");
                        final String description = o.getString(Tag.SeriesDescription);
                        logger.debug("Found series description: {}", description);
                        if (_filters.checkSeries(description)) {
                            logger.debug("Series description {} matched series import filter restrictions, including in session", description);
                            final Study study = studies.get(new Study(o));
                            study.getSeries(o, f);
                        } else {
                            logger.debug("Series description {} did not match series import filter restrictions, excluding from session", description);
                        }
                    } else {
                        logger.debug("Series import filters not found, including series in session");
                        final Study study = studies.get(new Study(o));
                        study.getSeries(o, f);
                    }
                }


            }
		}
		
		return new ArrayList<Session>(studies.getAll());
	}

    private boolean modalityIsSupported(final String modality) {
        if (StringUtils.isBlank(modality)) {
            return false;
        } else if (modality.equals("MR")) {
            return true;
        } else if (modality.equals("CT")) {
            return true;
        } else if (modality.equals("US")) {
            // Currently we do not support US upload until we can anonymise the patient data burnt into the images
            return false;
        } else {
            return false;
        }
    }

    public void setSeriesImportFilters(final SeriesImportFilterApplicatorRetriever filters) {
        _filters = filters;
    }
}
