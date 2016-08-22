/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.JTreeWithAdditionalKeyStrokeActions;

import javax.swing.*;

/**
 * <p>The {@link com.pixelmed.dicom.StructuredReportTreeBrowser StructuredReportTreeBrowser} class implements a Swing graphical user interface
 * to browse the contents of a {@link com.pixelmed.dicom.StructuredReport StructuredReport}.</p>
 *
 * <p>A main() method is provided for testing and as a utility that reads a DICOM SR file and displays it as a tree of content items.</p>
 *
 * @see com.pixelmed.dicom.StructuredReportBrowser
 * @see com.pixelmed.dicom.AttributeTreeBrowser
 *
 * @author	dclunie
 */
public class StructuredReportTreeBrowser {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/StructuredReportTreeBrowser.java,v 1.2 2014/09/09 20:34:09 dclunie Exp $";

	private JTree tree;
	private StructuredReport treeModel;

	/**
	 * <p>Build and display a graphical user interface view of a tree of attributes.</p>
	 *
	 * <p>Implicitly builds a tree from the SR attribute list.</p>
	 *
	 * @param	list				the list whose attributes to browse
	 * @param	treeBrowserScrollPane		the scrolling pane in which the tree view of the attributes will be rendered
	 * @throws	DicomException
	 */
	public StructuredReportTreeBrowser(AttributeList list,JScrollPane treeBrowserScrollPane) throws DicomException {
		treeModel=new StructuredReport(list);
		tree=new JTreeWithAdditionalKeyStrokeActions(treeModel);
		treeBrowserScrollPane.setViewportView(tree);
	}

}






