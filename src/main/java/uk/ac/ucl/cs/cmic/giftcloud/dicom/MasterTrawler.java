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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever;
import uk.ac.ucl.cs.cmic.giftcloud.util.ArrayIterator;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class MasterTrawler implements Callable<List<Session>> {
    private final Logger logger = LoggerFactory.getLogger(MasterTrawler.class);
    // TODO: hard-coded? configurable?
    private final Trawler[] trawlers = {
            new DicomTrawler()
    };
    private final Collection<File> roots;
    private final EditProgressMonitor pm;
    private final SeriesImportFilterApplicatorRetriever filters;

    // TODO: add progress monitor
    public MasterTrawler(final EditProgressMonitor monitor, final Iterable<File> files, final SeriesImportFilterApplicatorRetriever filters) {
        this.pm = monitor;
        this.roots = Lists.newArrayList(files);
        this.filters = filters;
    }

    @SuppressWarnings("unchecked")
    public List<Session> call() {
        // TODO: a really clever implementation could multithread the trawlers
        // by using a lazy, blocking collection for remaining.  This is a cool
        // idea but may add lots of complexity for no real performance gain.
        final List<Session> sessions = Lists.newArrayList();
        final Iterator<File> filei = new FileWalkIterator(roots, pm);   // TODO: progress monitor
        final Iterator<Trawler> trawleri = new ArrayIterator<Trawler>(trawlers);
        final Collection<File> remaining = Sets.newLinkedHashSet();
        Trawler trawler = trawleri.next();
        if (trawler instanceof DicomTrawler) {
            ((DicomTrawler) trawler).setSeriesImportFilters(filters);
        }
        sessions.addAll(trawler.trawl(filei, remaining, pm));
        while (trawleri.hasNext()) {
            final Collection<File> files = Lists.newArrayList(remaining);
            logger.trace("trawling {}", files);
            remaining.clear();
            trawler = trawleri.next();
            if (trawler instanceof DicomTrawler) {
                ((DicomTrawler) trawler).setSeriesImportFilters(filters);
            }
            sessions.addAll(trawler.trawl(files.iterator(), remaining, pm));
            if (null != pm && pm.isCanceled()) {
                logger.debug("user canceled file search");
                sessions.clear();
                return sessions;
            }
        }
        return sessions;
    }
}
