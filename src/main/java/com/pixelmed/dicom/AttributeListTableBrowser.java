/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.HashSet;

/**
 * <p>The {@link com.pixelmed.dicom.AttributeListTableBrowser AttributeListTableBrowser} class implements a Swing graphical user interface
 * to browse the contents of an {@link com.pixelmed.dicom.AttributeListTableModel AttributeListTableModel}.</p>
 *
 * @author	dclunie
 */
public class AttributeListTableBrowser extends JTable {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/AttributeListTableBrowser.java,v 1.12 2014/09/09 20:34:09 dclunie Exp $";

	/**
	 * <p>Build and display a graphical user interface view of a table model.</p>
	 *
	 * @param	model	the instance of the table model
	 */
	public AttributeListTableBrowser(AttributeListTableModel model) {
		super();
		setModel(model);
		setColumnWidths();
	}
	
	/**
	 * <p>Build and display a graphical user interface view of a table of attributes.</p>
	 *
	 * <p>Implicitly builds a model from the attribute list.</p>
	 *
	 * @param	list	an attribute list
	 */
	public AttributeListTableBrowser(AttributeList list) {
		super();
		setModel(new AttributeListTableModel(list));
		setColumnWidths();
	}
	
	/**
	 * <p>Build and display a graphical user interface view of a table of attributes.</p>
	 *
	 * <p>Implicitly builds a model from the attribute list.</p>
	 *
	 * @param	list	an attribute list
	 * @param	includeList	the list of attributes to include
	 * @param	excludeList	the list of attributes to exclude
	 */
	public AttributeListTableBrowser(AttributeList list,HashSet<AttributeTag> includeList,HashSet<AttributeTag> excludeList) {
		super();
		setModel(new AttributeListTableModel(list,includeList,excludeList));
		setColumnWidths();
	}
	
	/**
	 * <p>Called after setting the model to make sure that the cells (columns)
	 * are rendered with an appropriate width, with fudge factors to handle
	 * different platforms.</p>
	 */
	public void setColumnWidths() {			// See "http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#custom"
							// and "http://java.sun.com/docs/books/tutorial/uiswing/components/example-1dot4/TableRenderDemo.java"
		int n = getModel().getColumnCount();
		for (int j=0; j<n; ++j) {
			TableColumn column = getColumnModel().getColumn(j);
			TableCellRenderer headerRenderer = column.getHeaderRenderer();
			if (headerRenderer == null) headerRenderer = getTableHeader().getDefaultRenderer();	// the new 1.3 way
			Component columnComponent = headerRenderer.getTableCellRendererComponent(this,column.getHeaderValue(),false,false,-1,j);
			Component   cellComponent = getDefaultRenderer(getColumnClass(j)).getTableCellRendererComponent(this,getModel().getValueAt(0,j),false,false,0,j);
			int wantWidth = Math.max(
				columnComponent.getPreferredSize().width+10,	// fudge factor ... seems to always be too small otherwise (on x86 Linux)
				cellComponent.getPreferredSize().width+10	// fudge factor ... seems to always be too small otherwise (on Mac OS X)
			);
			column.setPreferredWidth(wantWidth);
		}
	}

}



