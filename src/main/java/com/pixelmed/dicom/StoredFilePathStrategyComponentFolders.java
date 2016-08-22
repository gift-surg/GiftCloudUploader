/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.util.StringTokenizer;

import java.io.File;

/**
 *
 * <p>Store files in a hierarchy of folders using successive numeric components
 * of the SOP Instance UID as the folder name and the SOP Instance UID as the filename within the most deeply nested folder.</p>
 *
 * <p>This is not a good strategy, since there may still be too many files within folders, since UID roots have significant commonality.</p>
 *
 * @author	dclunie, jimirrer
 */
		
public class StoredFilePathStrategyComponentFolders extends StoredFilePathStrategy {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/StoredFilePathStrategyComponentFolders.java,v 1.2 2013/02/01 13:53:20 dclunie Exp $";

	public StoredFilePathStrategyComponentFolders() {}

	public String makeStoredFilePath(String sopInstanceUID) {
		// do this in reverse order, to maximize spread across sub-folders for the same manufacturer UID root
		String suffix = null;
		StringBuffer buf = new StringBuffer();
		StringTokenizer st = new StringTokenizer(sopInstanceUID,".");
		while (st.hasMoreTokens()) {
			if (suffix != null) {
				buf.insert(0,suffix);
			}
			buf.insert(0,st.nextToken());
			suffix = File.separator;
		}
		buf.append(File.separator);
		buf.append(sopInstanceUID);				// append the entire uid as the file name (avoids conflict between files and folders that have the same root as a file)
		return buf.toString();
	}

	public String toString() {
		return "BYSOPINSTANCEUIDCOMPONENTFOLDERS";
	}

}

