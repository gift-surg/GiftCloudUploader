/*
 * uk.ac.ucl.cs.cmic.giftcloud.ecat.EcatTrawler
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.nrg.ecat.MatrixDataFile;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.Trawler;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import org.nrg.util.EditProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class EcatTrawler implements Trawler {
    private final Logger logger = LoggerFactory.getLogger(EcatTrawler.class);

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
        final ListMultimap<String,MatrixDataFile> filesets = ArrayListMultimap.create();
        while (files.hasNext()) {
        	if (null != pm && pm.isCanceled()) {
				return new ArrayList<Session>();
			}
            final File f = files.next();
            logger.trace("checking {}", f);
            try {
                final MatrixDataFile mdf = new MatrixDataFile(f);
                filesets.put(mdf.getPatientID(), mdf);
            } catch (IOException e) {
                logger.debug(f + " is not an ECAT file", e);
                if (null != remaining) {
                    remaining.add(f);
                }
            }
        }

        logger.trace("found ECAT sessions: {}", filesets);
        final ArrayList<Session> sessions = Lists.newArrayList();
        for (final String label : Sets.newTreeSet(filesets.keySet())) { // add in sorted order
            sessions.add(new EcatSession(filesets.get(label)));
        }
        return sessions;
    }
}
