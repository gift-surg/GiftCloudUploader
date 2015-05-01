/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import org.json.JSONException;
import org.netbeans.spi.wizard.WizardPage;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public final class SelectProjectPage extends WizardPage implements ListSelectionListener {
	private static final long serialVersionUID = 1L;

	public static final String PRODUCT_NAME = "project";
	private static final String STEP_DESCRIPTION = "Select project";
	private static final String LONG_DESCRIPTION = "Select the project to which you will upload a session";

	private final JList list;
	private final Dimension preferredComponentSize;
	private final UploadSelector uploadSelector;
	private final GiftCloudReporter reporter;

	public static String getDescription() {
		return STEP_DESCRIPTION;
	}

	public SelectProjectPage(final Dimension preferredComponentSize, final UploadSelector uploadSelector, final GiftCloudReporter reporter)
	throws IOException,JSONException {
		super(); // TODO: remove this?

		this.uploadSelector = uploadSelector;
		this.reporter = reporter;

		// We could just load the JList with Project objects.  Instead, we load it with
		// labels and create the Projects dynamically upon selection, because the Project
		// constructor spins off threads to retrieve the contained subjects and sessions.
		// This is smart if we're creating one Project at a time, but troublesome when
		// creating lots of Projects.
		reporter.trace("initializing SelectProjectPage; querying XNAT for project labels");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		list = new JList<Object>(uploadSelector.getListOfProjects());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);

		this.preferredComponentSize = preferredComponentSize;

		setOpaque(true);
		setLongDescription(LONG_DESCRIPTION);
        setCursor(Cursor.getDefaultCursor());

		reporter.trace("SelectProjectPage ready with projects " + Arrays.toString(list.getComponents()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.netbeans.spi.wizard.WizardPage#recycle()
	 */
	public void recycle() {
		removeAll();
	}

	/*
	 * (non-Javadoc)
	 * @see org.netbeans.spi.wizard.WizardPage#renderingPage()
	 */
	public void renderingPage() {
		reporter.trace("rendering SelectProjectPage");
		final JScrollPane pane = new JScrollPane(list);
		if (null != preferredComponentSize) {
			pane.setPreferredSize(preferredComponentSize);
		}
		add(pane);
	}

	/*
	 * (non-Javadoc)
	 * @see org.netbeans.spi.wizard.WizardPage#validateContents(java.awt.Component, java.lang.Object)
	 */
	protected String validateContents(final Component component, final Object o) {
		if (1 != list.getSelectedIndices().length) {
			return "";
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(final ListSelectionEvent e) {

		// TODO: do we need to actually wait for getValueIsAdjusting or will it repeat?

		if (!e.getValueIsAdjusting()) {	// wait until selection is complete

			uploadSelector.replaceProject((String) list.getSelectedValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#toString()
	 */
	public String toString() {
		if (uploadSelector.isProjectSet()) {
			return uploadSelector.getProject().toString();
		} else {
			return "";
		}
	}
}
