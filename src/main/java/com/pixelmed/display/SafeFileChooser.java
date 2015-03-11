/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import com.pixelmed.utils.ThreadUtilities;

import java.awt.Component;
import java.awt.HeadlessException;

import java.io.File;

import javax.swing.JFileChooser;

public class SafeFileChooser {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/SafeFileChooser.java,v 1.4 2013/02/21 00:06:44 dclunie Exp $";
	
	JFileChooser chooser;
	
	public SafeFileChooser() {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		chooser = new JFileChooser();
	}
	
	public SafeFileChooser(String currentDirectoryPath) {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		chooser = new JFileChooser(currentDirectoryPath);
	}
	
	public void setFileSelectionMode(int mode) throws IllegalArgumentException {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		chooser.setFileSelectionMode(mode);
	}
	
	public int showOpenDialog(Component parent) throws HeadlessException {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		return chooser.showOpenDialog(parent);
	}
	
	public File getCurrentDirectory() {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		return chooser.getCurrentDirectory();
	}
	
	public File getSelectedFile() {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		return chooser.getSelectedFile();
	}
	
	public int showSaveDialog(Component parent) throws HeadlessException {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		return chooser.showSaveDialog(parent);
	}
	
	public void setDialogTitle(String dialogTitle) throws HeadlessException {
		ThreadUtilities.checkIsEventDispatchThreadElseException();
		chooser.setDialogTitle(dialogTitle);
	}
	
	public static class SafeFileChooserThread implements Runnable {
		private int fileSelectionMode;
		private String initialDirectoryPath;
		private String dialogTitle;
		private String selectedFileName;
		private String currentDirectoryPath;
		
		public SafeFileChooserThread() {
			this.fileSelectionMode = JFileChooser.FILES_ONLY;
		}
		
		public SafeFileChooserThread(int fileSelectionMode) {
			this.fileSelectionMode = fileSelectionMode;
		}
		
		public SafeFileChooserThread(String initialDirectoryPath) {
			this.fileSelectionMode = JFileChooser.FILES_ONLY;
			this.initialDirectoryPath = initialDirectoryPath;
		}
		
		public SafeFileChooserThread(int fileSelectionMode,String initialDirectoryPath) {
			this.fileSelectionMode = fileSelectionMode;
			this.initialDirectoryPath = initialDirectoryPath;
		}
		
		public SafeFileChooserThread(String initialDirectoryPath,String dialogTitle) {
			this.fileSelectionMode = JFileChooser.FILES_ONLY;
			this.initialDirectoryPath = initialDirectoryPath;
			this.dialogTitle = dialogTitle;
		}
		
		public SafeFileChooserThread(int fileSelectionMode,String initialDirectoryPath,String dialogTitle) {
			this.fileSelectionMode = fileSelectionMode;
			this.initialDirectoryPath = initialDirectoryPath;
			this.dialogTitle = dialogTitle;
		}
		
		public void run() {
			SafeFileChooser chooser = initialDirectoryPath == null ? new SafeFileChooser() : new SafeFileChooser(initialDirectoryPath);
			if (dialogTitle != null) {
				chooser.setDialogTitle(dialogTitle);
			}
			chooser.setFileSelectionMode(fileSelectionMode);
			selectedFileName = null;
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				selectedFileName=chooser.getSelectedFile().getAbsolutePath();
				currentDirectoryPath=chooser.getCurrentDirectory().getAbsolutePath();
			}
		}
		
		public String getSelectedFileName() {
			return selectedFileName;
		}
		
		public String getCurrentDirectoryPath() {
			return currentDirectoryPath;
		}
	}
	
}