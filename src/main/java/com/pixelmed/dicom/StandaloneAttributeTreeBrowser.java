/* Copyright (c) 2001-2006, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * <p>The {@link com.pixelmed.dicom.StandaloneAttributeTreeBrowser StandaloneAttributeTreeBrowser} class implements a Swing graphical user interface
 * to browse the contents of an {@link com.pixelmed.dicom.AttributeTree AttributeTree} using an {@link com.pixelmed.dicom.AttributeTreeBrowser AttributeTreeBrowser}.</p>
 *
 * @see	com.pixelmed.dicom.AttributeTree
 *
 * @author	dclunie
 */
public class StandaloneAttributeTreeBrowser extends JFrame {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/StandaloneAttributeTreeBrowser.java,v 1.2 2014/09/09 20:34:09 dclunie Exp $";

	/**
	 * <p>Build and display a graphical user interface view of a tree representing a DICOM attribute list.</p>
	 *
	 * @param	list				the list of attributes in which the structured report is encoded
	 * @throws	DicomException
	 */
	public StandaloneAttributeTreeBrowser(AttributeList list) throws DicomException {
		super("StandaloneAttributeTreeBrowser");
		setSize(400,800);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				//System.exit(0);
			}
		});
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane,BorderLayout.CENTER);
		AttributeTreeBrowser browser = new AttributeTreeBrowser(list,scrollPane);
	}

}





