/*
 * uk.ac.ucl.cs.cmic.giftcloud.util.ResultProgressMonitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.util.EditProgressMonitor;

public class ResultProgressMonitor implements EditProgressMonitor {
	private final ResultProgressHandle progress;
	private int min = 0, max = 1, current = 0;
	private String note = null;
	private boolean isCanceled = false;
	
	/**
	 * 
	 */
	public ResultProgressMonitor(final ResultProgressHandle progress) {
		this.progress = progress;
	}

	public void cancel() { isCanceled = true; }
	
	/* (non-Javadoc)
	 * @see org.nrg.util.EditProgressMonitor#close()
	 */
	public void close() {
		// noop; surrounding code must call finish or failed
	}

	/* (non-Javadoc)
	 * @see org.nrg.util.EditProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() { return isCanceled; }

	/* (non-Javadoc)
	 * @see org.nrg.util.EditProgressMonitor#setMaximum(int)
	 */
	public void setMaximum(final int max) {
		this.max = max;
	}

	/* (non-Javadoc)
	 * @see org.nrg.util.EditProgressMonitor#setMinimum(int)
	 */
	public void setMinimum(final int min) {
		this.min = min;
	}

	/* (non-Javadoc)
	 * @see org.nrg.util.EditProgressMonitor#setNote(java.lang.String)
	 */
	public void setNote(final String note) {
		this.note = note;
		progress.setProgress(note, current - min, max - min);
	}

	/* (non-Javadoc)
	 * @see org.nrg.util.EditProgressMonitor#setProgress(int)
	 */
	public void setProgress(final int current) {
		this.current = current;
		progress.setProgress(note, current - min, max - min);
	}
}
