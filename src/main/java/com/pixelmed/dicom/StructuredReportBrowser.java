/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.JTreeWithAdditionalKeyStrokeActions;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>The {@link com.pixelmed.dicom.StructuredReportBrowser StructuredReportBrowser} class implements a Swing graphical user interface
 * to browse the contents of a {@link com.pixelmed.dicom.StructuredReport StructuredReport}.</p>
 *
 * @see com.pixelmed.dicom.StructuredReportTreeBrowser
 * @see com.pixelmed.dicom.AttributeTreeBrowser
 *
 * @author	dclunie
 */
public class StructuredReportBrowser extends JFrame {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/StructuredReportBrowser.java,v 1.11 2014/09/09 20:34:09 dclunie Exp $";

	private JTree tree;
	private StructuredReport treeModel;

	/**
	 * <p>Build and display a graphical user interface view of a tree representing a structured reports.</p>
	 *
	 * <p>Implicitly builds a tree from the attribute list.</p>
	 *
	 * @param	list				the list of attributes in which the structured report is encoded
	 * @throws	DicomException
	 */
	public StructuredReportBrowser(AttributeList list) throws DicomException {
		this(list,"SR Tree");
	}

	/**
	 * <p>Build and display a graphical user interface view of a tree representing a structured reports.</p>
	 *
	 * <p>Implicitly builds a tree from the attribute list.</p>
	 *
	 * @param	list				the list of attributes in which the structured report is encoded
	 * @param	title
	 * @throws	DicomException
	 */
	public StructuredReportBrowser(AttributeList list,String title) throws DicomException {
		super(title);

		setSize(400,800);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				//System.exit(0);
			}
		});

		treeModel=new StructuredReport(list);
		tree=new JTreeWithAdditionalKeyStrokeActions(treeModel);

		tree.addTreeSelectionListener(buildTreeSelectionListener());

		JScrollPane scrollPane = new JScrollPane(tree);
		getContentPane().add(scrollPane,BorderLayout.CENTER);
	}

	private TreeSelectionListener buildTreeSelectionListener() {
		return new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				TreePath tp = tse.getNewLeadSelectionPath();
				if (tp != null) {
//System.err.println("Selected: "+tp.getLastPathComponent());
//System.err.println("Selected: "+tp);
					Object rootComponent = tp.getPathComponent(0);				// root is need to find by reference rather than by value nodes
					Object lastPathComponent = tp.getLastPathComponent();
					if (rootComponent instanceof ContentItem && lastPathComponent instanceof ContentItem) {
						Vector instances=StructuredReport.findAllContainedSOPInstances((ContentItem)rootComponent,(ContentItem)lastPathComponent);
						if (instances != null) doSomethingWithSelectedSOPInstances(instances);
					}
				}
			}
		};
	}

	// 

	/**
	 * <p>Do something when the user selects a node of the tree.</p>
	 *
	 * <p>Override this method in derived classes to do something useful.</p>
	 *
	 * @param	instances
	 */
	protected void doSomethingWithSelectedSOPInstances(Vector instances) {
		Iterator i = instances.iterator();
		while (i.hasNext()) {
			System.err.println((SpatialCoordinateAndImageReference)i.next());
		}
	}

}





