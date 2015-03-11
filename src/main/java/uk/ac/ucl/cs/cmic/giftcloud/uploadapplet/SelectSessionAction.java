/*
 * SelectSessionAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import uk.ac.ucl.cs.cmic.giftcloud.data.Session;

public final class SelectSessionAction extends AbstractAction implements Action {
	private static final long serialVersionUID = 1L;
	private final SessionSelectionListener listener;
	private final Session session;
	
	/**
	 * 
	 */
	public SelectSessionAction(final SessionSelectionListener listener, final Session session) {
		this.listener = listener;
		this.session = session;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		listener.setSelectedSession(session);
	}
	
	public static interface SessionSelectionListener {
		void setSelectedSession(Session session);
	}

}
