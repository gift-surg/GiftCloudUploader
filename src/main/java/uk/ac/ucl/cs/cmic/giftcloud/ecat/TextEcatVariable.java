/*
 * uk.ac.ucl.cs.cmic.giftcloud.ecat.TextEcatVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import org.nrg.ecat.edit.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;

public class TextEcatVariable extends EcatSessionVariable implements DocumentListener {
	private final Logger logger = LoggerFactory.getLogger(TextEcatVariable.class);
	private final JTextField text;
	
	/**
	 * @param variable
	 */
	public TextEcatVariable(final Variable variable) {
		super(variable);
		final Object v = variable.getValue();
		text = new JTextField(null == v ? null : v.toString());
		text.getDocument().addDocumentListener(this);
	}

	/* (non-Javadoc)
	 * @see SessionVariable#getEditor()
	 */
	public Component getEditor() { return text; }

	/* (non-Javadoc)
	 * @see SessionVariable#refresh()
	 */
	public void refresh() {
		setDisplayValue(getValue());
	}

	private String setDisplayValue(final String value) {
		final String old = text.getText();
		final Document d = text.getDocument();
		synchronized(this) {
			d.removeDocumentListener(this);
			logger.trace("Setting text field for " + this.getName() + " = " + value);
			text.setText(value);
			d.addDocumentListener(this);
		}
		return old;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	public void changedUpdate(final DocumentEvent e) {
		editTo(text.getText());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	public void insertUpdate(final DocumentEvent e) {
		editTo(text.getText());
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	public void removeUpdate(final DocumentEvent e) {
		editTo(text.getText());
	}

}
