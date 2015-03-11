/*
 * MutableSessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


public class MutableSessionVariable
extends AbstractSessionVariable implements DocumentListener {
	private final JTextField editor;
	private String value;
	private String message = null;

	public MutableSessionVariable(final String name, final String value) {
		super(name);
		this.value = value;
		this.editor = new JTextField(value);
		editor.getDocument().addDocumentListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	public final void changedUpdate(final DocumentEvent e) {
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getEditor()
	 */
	public final JTextField getEditor() { return editor; }

	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getValue()
	 */
	public final String getValue() { return value; }

	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getValueMessage()
	 */
	public final String getValueMessage() { return message; }

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	public final void insertUpdate(final DocumentEvent e) {
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see SessionVariable#isHidden()
	 */
	public final boolean isHidden() { return false; }
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#refresh()
	 */
	public final void refresh() {}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	public final void removeUpdate(final DocumentEvent e) {
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see SessionVariable#setValue(java.lang.String)
	 */
	public final String setValue(final String v) throws InvalidValueException {
		message = validate(v);
		final String old = value;
		final Document d = editor.getDocument();
		synchronized(this) {
			d.removeDocumentListener(this);
			editor.setText(value = v);
			d.addDocumentListener(this);
		}
		return old;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" ").append(getName()).append(" = ").append(value);
		return sb.toString();
	}

	private final void update() {
		final String v = editor.getText();
		try {
			message = validate(v);
			value = v;
			fireHasChanged();
		} catch (InvalidValueException e) {
			fireIsInvalid(v, e.getMessage());
		}
	}
}