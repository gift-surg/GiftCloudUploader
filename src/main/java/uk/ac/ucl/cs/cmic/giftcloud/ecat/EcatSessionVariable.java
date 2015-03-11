/*
 * uk.ac.ucl.cs.cmic.giftcloud.ecat.EcatSessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import java.util.Collections;
import java.util.Set;

import org.nrg.ecat.edit.MultipleInitializationException;
import org.nrg.ecat.edit.Value;
import org.nrg.ecat.edit.Variable;
import uk.ac.ucl.cs.cmic.giftcloud.data.AbstractSessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EcatSessionVariable
extends AbstractSessionVariable implements SessionVariable {
	private final Logger logger = LoggerFactory.getLogger(EcatSessionVariable.class);
	private final Variable variable;
	private String message = null;
	
	/**
	 * @param name
	 */
	public EcatSessionVariable(final Variable variable) {
		super(variable.getName());
		this.variable = variable;
	}
	
	public static SessionVariable getSessionVariable(final Variable v) {
		return new TextEcatVariable(v);
	}
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getExportField()
	 */
	public String getExportField() {
	    return variable.getExportField();
	}

	/* (non-Javadoc)
	 * @see SessionVariable#getValue()
	 */
	public String getValue() {
		logger.trace("getting value for " + this);
		final Object value = variable.getValue();
		if (null == value) {
			final Value iv = variable.getInitialValue();
			logger.trace("no value set; evaluating initial value " + iv);
			if (null == iv) {
				return null;
			} else {
				try {
					final Object ivo = iv.on(Collections.emptyMap());
					logger.trace("initial value = " + ivo);
					return null == ivo ? null : ivo.toString();
				} catch (Throwable t) {
					logger.warn("unable to evaluate initial value " + iv, t);
					return null;
				}
			}
		} else {
			return value.toString();
		}
	}

	/* (non-Javadoc)
	 * @see SessionVariable#getValueMessage()
	 */
	public String getValueMessage() {
		return message;
	}

	public boolean hasInitialValue() {
	    return null != variable.getInitialValue();
	}
	
	/* (non-Javadoc)
	 * @see SessionVariable#isHidden()
	 */
	public boolean isHidden() {
		return variable.isHidden();
	}

	/* (non-Javadoc)
	 * @see SessionVariable#setValue(java.lang.String)
	 */
	public String setValue(String value) throws InvalidValueException {
		final Object old = variable.getValue();
		variable.setValue(value);
		return null == old ? null : old.toString();
	}

	public void setInitialValue(final Value value) throws MultipleInitializationException {
		variable.setInitialValue(value);
	}
	
	public void setIsHidden(final boolean isHidden) {
		variable.setIsHidden(isHidden);
	}
	
	
	public Set<?> getVariables() {
		final Value iv = variable.getInitialValue();
		return null == iv ? Collections.emptySet() : iv.getVariables();
	}
	
	protected void editTo(final String value) {
		try {
			message = validate(value);
			variable.setValue(value);
			fireHasChanged();
		} catch (InvalidValueException e) {
			fireIsInvalid(variable, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + " (" + variable + ")";
	}
}
