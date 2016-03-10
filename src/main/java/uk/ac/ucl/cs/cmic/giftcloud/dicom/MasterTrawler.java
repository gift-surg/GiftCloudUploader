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
import uk.ac.ucl.cs.cmic.giftcloud.util.ArrayIterator;

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

    // TODO: add progress monitor
    public MasterTrawler(final EditProgressMonitor monitor, final Iterable<File> files, final SeriesImportFilterApplicatorRetriever filters) {
        this.pm = monitor;
        this.roots = Lists.newArrayList(files);
        this.filters = filters;
    }

    @SuppressWarnings("unchecked")
    public List<Study> call() {
        final List<Study> studies = Lists.newArrayList();
        final Iterator<File> filei = new FileWalkIterator(roots, pm);   // TODO: progress monitor
        final Iterator<Trawler> trawleri = new ArrayIterator<Trawler>(trawlers);
        final Collection<File> remaining = Sets.newLinkedHashSet();
        Trawler trawler = trawleri.next();
        if (trawler instanceof DicomTrawler) {
            ((DicomTrawler) trawler).setSeriesImportFilters(filters);
        }
        studies.addAll(trawler.trawl(filei, remaining, pm));
        errors.addAll(trawler.getErrorMessages());

        while (trawleri.hasNext()) {
            final Collection<File> files = Lists.newArrayList(remaining);
            remaining.clear();
            trawler = trawleri.next();
            if (trawler instanceof DicomTrawler) {
                ((DicomTrawler) trawler).setSeriesImportFilters(filters);
            }
            studies.addAll(trawler.trawl(files.iterator(), remaining, pm));
            if (null != pm && pm.isCanceled()) {
                studies.clear();
                return studies;
            }

            errors.addAll(trawler.getErrorMessages());
        }
        return studies;
    }

    public List<GiftCloudUploaderError> getErrorMessages() {
        return errors;
    }
}
