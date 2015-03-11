/*
 * ResultProgressListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.ProgressListener;

public class ResultProgressListener implements ProgressListener {
	private final ResultProgressHandle handle;
	private int progress, end;
	
	public ResultProgressListener(final ResultProgressHandle handle, final int progress, final int end) {
		this.handle = handle;
		handle.setProgress(this.progress = progress, this.end = end);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.ProgressListener#incrementProgress(int)
	 */
	public void incrementProgress(final int increment) {
		handle.setProgress(progress += increment, end);
	}

	/* (non-Javadoc)
	 * @see org.nrg.ProgressListener#incrementTaskSize(int)
	 */
	public void incrementTaskSize(int increment) {
		handle.setProgress(progress, end += increment);
	}

	/* (non-Javadoc)
	 * @see org.nrg.ProgressListener#setMessage(java.lang.String)
	 */
	public void setMessage(final String message) {
		handle.setProgress(message, progress, end);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s (%d/%d)", super.toString(), progress, end);
	}
}
