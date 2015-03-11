/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.TextDicomVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.edit.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TextDicomVariable
extends DicomSessionVariable implements DocumentListener {
	private final Logger logger = LoggerFactory.getLogger(TextDicomVariable.class);
	private final JTextField text;
	
	TextDicomVariable(final Variable v, final DicomObject sample) {
		super(v, sample);
		text = new JTextField(v.getValue());
		text.getDocument().addDocumentListener(this);
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
	 * @see SessionVariable#getEditor()
	 */
	public JTextField getEditor() { return text; }
		
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
	 * @see uk.ac.ucl.cs.cmic.giftcloud.dicom.DicomSessionVariable#setValue(java.lang.String)
	 */
	public String setValue(final String value) throws InvalidValueException {
		final String old = super.setValue(value);
		setDisplayValue(value);
		return old;
	}
}