/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.Trawler
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import org.nrg.util.EditProgressMonitor;
import uk.ac.ucl.cs.cmic.giftcloud.data.Study;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface Trawler {
	/**
	 * Identifies imaging sessions in the given group of files.  Files that are part of
	 * a session are removed from the underlying collection (so the Iterator must be one
	 * that implements remove().
	 * @param files Iterator over files to be examined
	 * @return any Sessions that could be identified
	 */
	Collection<Study> trawl(final Iterator<File> files, final Collection<File> remaining, EditProgressMonitor pm);

	List<GiftCloudUploaderError> getErrorMessages();
}
