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

import org.nrg.ProgressListener;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;

public class ResultProgressListener implements ProgressListener {
	private final Progress handle;
	private int progress, end;
	
	public ResultProgressListener(final Progress handle, final int progress, final int end) {
		this.handle = handle;
		handle.updateProgressBar(this.progress = progress, this.end = end);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.ProgressListener#incrementProgress(int)
	 */
	public void incrementProgress(final int increment) {
		handle.updateProgressBar(progress += increment, end);
	}

	/* (non-Javadoc)
	 * @see org.nrg.ProgressListener#incrementTaskSize(int)
	 */
	public void incrementTaskSize(int increment) {
		handle.updateProgressBar(progress, end += increment);
	}

	/* (non-Javadoc)
	 * @see org.nrg.ProgressListener#setMessage(java.lang.String)
	 */
	public void setMessage(final String message) {
		handle.updateStatusText(message);
		handle.updateProgressBar(progress, end);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s (%d/%d)", super.toString(), progress, end);
	}
}
