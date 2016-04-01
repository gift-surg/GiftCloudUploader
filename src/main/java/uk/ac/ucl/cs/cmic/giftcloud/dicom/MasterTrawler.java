/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.MasterTrawler
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.nrg.io.FileWalkIterator;
import org.nrg.util.EditProgressMonitor;
import uk.ac.ucl.cs.cmic.giftcloud.data.Study;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class MasterTrawler implements Callable<List<Study>> {
    private final Trawler[] trawlers = {
            new DicomTrawler()
    };
    private final Collection<File> roots;
    private final EditProgressMonitor pm;
    private final SeriesImportFilterApplicatorRetriever filters;
    private final List<GiftCloudUploaderError> errors = new ArrayList<GiftCloudUploaderError>();

    public MasterTrawler(final EditProgressMonitor monitor, final Iterable<File> files, final SeriesImportFilterApplicatorRetriever filters) {
        this.pm = monitor;
        this.roots = Lists.newArrayList(files);
        this.filters = filters;
    }

    public List<Study> call() {

        // The studies found so far
        final List<Study> studies = Lists.newArrayList();

        // Iterator to the next file to trawl
        Iterator<File> nextFileIterator = new FileWalkIterator(roots, pm);

        for (final Trawler trawler : trawlers) {

            // Trawler will return unprocessed files into the remaining set
            final Collection<File> remaining = Sets.newLinkedHashSet();

            // Call the trawler to find new studies
            studies.addAll(trawler.trawl(nextFileIterator, remaining, pm, filters));

            // Check for user cancellation
            if (null != pm && pm.isCanceled()) {
                studies.clear();
                return studies;
            }

            // Ensure errors are recorded
            errors.addAll(trawler.getErrorMessages());

            // Update the next file iterator to point to the remaining list
            nextFileIterator = Lists.newArrayList(remaining).iterator();
        }
        return studies;
    }

    public List<GiftCloudUploaderError> getErrorMessages() {
        return errors;
    }
}
