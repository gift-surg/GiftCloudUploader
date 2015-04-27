/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import uk.ac.ucl.cs.cmic.giftcloud.Progress;

import javax.swing.JProgressBar;

/**
 * <p>A class that implements {@link java.lang.Runnable Runnable} so that it can be invoked by {@link java.awt.EventQueue#invokeLater(Runnable) EventQueue.invokeLater()}.</p>
 *
 * <p>This is needed, for example, to call from a worker thread, since the progress bar methods used MUST be invoked on the AWT Event Dispatch Thread.</p>
 *
 * <p>So, for example, instead of directly accessing the {@link javax.swing.JProgressBar JProgressBar} methods:</p>
 * <pre>
 * 	progressBar.setMaximum(maximum);
 * 	progressBar.setValue(value);
 * 	progressBar.repaint();
 * </pre>
 * <p>do the following instead:</p>
 * <pre>
 *  progressBarUpdater = new SafeProgressBarUpdaterThread(progressBar);
 *  ...
 * 	progressBarUpdater.setMaximum(maximum);
 * 	progressBarUpdater.setValue(value);
 * 	java.awt.EventQueue.invokeLater(progressBarUpdater);
 * </pre>
 */

public class SafeProgressBarUpdaterThread implements Runnable, Progress {
	
	private JProgressBar progressBar;
	private int value;
	private int maximum;
	private boolean stringPainted;
	private String progressText;
	
	public SafeProgressBarUpdaterThread(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.value = 0;
		this.maximum = 0;
		this.progressText = "";
	}
	
	public void run() {
		progressBar.setValue(value);
		progressBar.setMaximum(maximum);			// undesirable to keep setting the maximum this way, may cause flicker but saves having to have a separate class to do it since only one run method
		progressBar.setString(progressText);
		progressBar.setStringPainted(stringPainted);
		progressBar.repaint();
	}

    @Override
    public void startProgressBar(int maximum) {
		{
			if (java.awt.EventQueue.isDispatchThread()) {
				progressBar.setValue(0);
				progressBar.setMaximum(maximum);
				progressBar.setStringPainted(true);
				progressBar.repaint();
			}
			else {
				setCachedValue(0);
				setCachedMaximum(maximum);
				setCachedStringPainted(true);
				java.awt.EventQueue.invokeLater(this);
			}
		}
	}
	
	@Override
    public void startProgressBar() {
		startProgressBar(100);	// assume standard default of 100 if unknown, expecting that it will be updated when known
	}
	
	@Override
    public void updateProgressBar(int value) {
		{
			if (java.awt.EventQueue.isDispatchThread()) {
				progressBar.setValue(value);
				progressBar.setStringPainted(true);
				progressBar.repaint();
			}
			else {
				setCachedValue(value);
				setCachedStringPainted(true);
				java.awt.EventQueue.invokeLater(this);
			}
		}
	}
	
	@Override
    public void updateProgressBar(int value, int maximum) {
		{
			if (java.awt.EventQueue.isDispatchThread()) {
				progressBar.setValue(value);
				progressBar.setMaximum(maximum);
				progressBar.setStringPainted(true);
				progressBar.repaint();
			}
			else {
				setCachedValue(value);
				setCachedMaximum(maximum);
				setCachedStringPainted(true);
				java.awt.EventQueue.invokeLater(this);
			}
		}
	}
	
	@Override
    public void endProgressBar() {
		{
			if (java.awt.EventQueue.isDispatchThread()) {
				progressBar.setValue(0);
				progressBar.setMaximum(100);			// clears the progress bar
				progressBar.setStringPainted(false);	// do not want to display 0%
				progressBar.repaint();
			}
			else {
				setCachedValue(0);
				setCachedMaximum(100);				// clears the progress bar
				setCachedStringPainted(false);		// do not want to display 0%
				java.awt.EventQueue.invokeLater(this);
			}
		}
	}

	@Override
	public void updateStatusText(String progressText) {
		if (java.awt.EventQueue.isDispatchThread()) {
			progressBar.setString(progressText);
			progressBar.setStringPainted(true);
			progressBar.repaint();
		}
		else {
			setCachedString(progressText);
			setCachedStringPainted(true);
			java.awt.EventQueue.invokeLater(this);
		}
	}

	@Override
	public boolean isCancelled() {
		return false;
	}


	// static convenience methods ...

	public static void startProgressBar(SafeProgressBarUpdaterThread progressBarUpdater, int maximum) {
		if (progressBarUpdater != null) {
			progressBarUpdater.startProgressBar(maximum);
		}
	}
	
	public static void startProgressBar(SafeProgressBarUpdaterThread progressBarUpdater) {
		if (progressBarUpdater != null) {
			progressBarUpdater.startProgressBar();
		}
	}
	
	public static void updateProgressBar(SafeProgressBarUpdaterThread progressBarUpdater, int value) {
		if (progressBarUpdater != null) {
			progressBarUpdater.updateProgressBar(value);
		}
	}
	
	public static void updateProgressBar(SafeProgressBarUpdaterThread progressBarUpdater, int value, int maximum) {
		if (progressBarUpdater != null) {
			progressBarUpdater.updateProgressBar(value,maximum);
		}
	}
	
	public static void endProgressBar(SafeProgressBarUpdaterThread progressBarUpdater) {
		if (progressBarUpdater != null) {
			progressBarUpdater.endProgressBar();
		}
	}

    // Set cached values for later invocation on EDT

    private void setCachedValue(int value) {
        this.value = value;
    }

    private void setCachedMaximum(int maximum) {
        this.maximum = maximum;
    }

	private void setCachedString(String s) {
		progressText = s;
	}

    private void setCachedStringPainted(boolean b) {
        stringPainted = b;
    }

    private void runOnEdt() {
        java.awt.EventQueue.invokeLater(this);
    }

}

