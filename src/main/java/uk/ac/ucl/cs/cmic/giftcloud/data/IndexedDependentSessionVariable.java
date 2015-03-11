/*
 * IndexedDependentSessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;

public class IndexedDependentSessionVariable extends AbstractSessionVariable implements SessionVariable,DocumentListener {
	/**
	 * @param name Name of the session variable.
     * @param dependency The session variable on which this indexed session variable is dependent. This is used for updating the value of the control.
     * @param format A string format for composing the variable value.
	 */
	public IndexedDependentSessionVariable(final String name,
			final SessionVariable dependency,
			final String format) {
		this(name, dependency, format, new ValueValidator() {
            @Override
            public String getMessage(Object value) {
                return null;
            }

            @Override
            public boolean isValid(Object value) {
                return true;
            }
        });
    }

	/**
	 * @param name Name of the session variable.
     * @param dependency The session variable on which this indexed session variable is dependent. This is used for updating the value of the control.
     * @param format A string format for composing the variable value.
     * @param validator The object that validates and verifies the control value.
	 */
	public IndexedDependentSessionVariable(final String name,
			final SessionVariable dependency,
			final String format,
			final ValueValidator validator) {
		super(name);
		this.format = format;
		this.validator = validator;
        setDependency(dependency);
		text = new JTextField(evaluate());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
    @Override
	public void changedUpdate(final DocumentEvent e) { edit(); }
			
	/* (non-Javadoc)
	 * @see SessionVariable#getEditor()
	 */
    @Override
	public Component getEditor() { return text; }

	/* (non-Javadoc)
	 * @see SessionVariable#getValue()
	 */
    @Override
	public synchronized String getValue() {
		return text.getText();
	}

	/* (non-Javadoc)
	 * @see SessionVariable#getValueMessage()
	 */
    @Override
	public String getValueMessage() { return message; }

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
    @Override
	public void insertUpdate(final DocumentEvent e) { edit(); }
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#isHidden()
	 */
    @Override
	public boolean isHidden() { return false; }
	
	/* (non-Javadoc)
	 * @see SessionVariable#refresh()
	 */
    @Override
	public void refresh() {
		try {
			if (!edited) {
				setValue(evaluate());
			} else {
				validate(text.getText());
			}
			fireHasChanged();
		} catch (InvalidValueException e) {
			logger.trace("ignoring change to " + dependency.getName(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
    @Override
	public void removeUpdate(final DocumentEvent e) { edit(); }
	
	/* (non-Javadoc)
	 * @see SessionVariable#setValue(java.lang.String)
	 */
    @Override
	public synchronized String setValue(final String value) throws InvalidValueException {
		message = validate(value);
		final String old = text.getText();
		final Document d = text.getDocument();
		d.removeDocumentListener(this);
		text.setText(value);
		d.addDocumentListener(this);
		return old;
	}

    public SessionVariable getDependency() {
        return dependency;
    }

    public void setDependency(SessionVariable dependency) {
		this.dependency = dependency;
	}

	private void edit() {
		edited = true;
		try {
			validate(text.getText());
			fireHasChanged();
		} catch (InvalidValueException e) {
			fireIsInvalid(text.getText(), e.getMessage());
		}
	}
	
    private String evaluate() {
		int i = 1;
		String name;
		do {
            String value = null;
            if (getDependency() != null) {
                value = getDependency().getValue();
            }
            name = String.format(format, value, i++);
		} while (!validator.isValid(name));
		return name;
	}
	
    private final Logger logger = LoggerFactory.getLogger(IndexedDependentSessionVariable.class);
    private final String format;
    private SessionVariable dependency;
    private final ValueValidator validator;
    private final JTextField text;
    private String message = null;
    private boolean edited = false;
	}
