/*
 * AbstractSessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public abstract class AbstractSessionVariable implements SessionVariable {
    private final Logger logger = LoggerFactory.getLogger(AbstractSessionVariable.class);
    private final String name, exportField;
    private final Set<ValueListener> listeners = Sets.newLinkedHashSet();
    private final Set<ValueValidator> validators = Sets.newLinkedHashSet();
    private final Set<SessionVariable> shadows = Sets.newLinkedHashSet();
    private String description = null;

    protected AbstractSessionVariable(final String name, final String exportField) {
        this.name = name;
        this.exportField = exportField;
    }

    protected AbstractSessionVariable(final String name) {
        this(name, null);
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#addListener(ValueListener)
     */
    @Override
    public final void addListener(final ValueListener listener) {
        if (!listeners.contains(listener)) {
        listeners.add(listener);
    }
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#getDescription()
     */
    @Override
    public String getDescription() {
        return Strings.isNullOrEmpty(description) ? name : description;
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#getName()
     */
    @Override
    public final String getName() { return name; }

    /*
     * (non-Javadoc)
     * @see SessionVariable#setDescription(java.lang.String)
     */
    @Override
    public String setDescription(final String description) {
        final String old = this.description;
        this.description = description;
        return old;
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#fixValue()
     */
    @Override
    public SessionVariable fixValue() {
        return this;
    }
    
    /*
     * (non-Javadoc)
     * @see SessionVariable#setEditable(boolean)
     */
    @Override
    public SessionVariable fixValue(final String value) throws InvalidValueException {
        setValue(value);
        return fixValue();
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#validate(java.lang.String)
     */
    @Override
    public final String validate(final String value) throws InvalidValueException {
        StringBuilder sb = null;
        for (final ValueValidator validator : validators) {
            if (validator.isValid(value)) {
                final String message = validator.getMessage(value);
                if (null != message) {
                    if (null == sb) {
                        sb = new StringBuilder(message);
                    } else {
                        sb.append("; ").append(message);
                    }
                }
            } else {
                throw new InvalidValueException(validator.getMessage(value));
            }
        }
        return null == sb ? null : sb.toString();
    }

    /**
     * Notify all listeners that the value of this variable has changed.
     */
    protected final void fireHasChanged() {
        final String value = getValue();
        logger.trace("{} value has changed to {}", this, value);
        for (final ValueListener listener : listeners) {
            logger.trace("{} notifying listener {} of value change", this, listener);
            listener.hasChanged(this);
        }
        for (final SessionVariable shadow : shadows) {
            try {
                shadow.setValue(value);
            } catch (InvalidValueException e) {
                logger.error(this + "unable to set shadow value", e);
            }
        }
    }

    /**
     * Notify all listeners that the user attempted to set this variable
     * to an invalid value
     * @param value the attempted invalid value
     * @param message explanation of what went wrong
     */
    protected final void fireIsInvalid(final Object value, final String message) {
        logger.trace("attempted to set {} to invalid value {}", this, value);
        for (final ValueListener listener : listeners) {
            logger.trace("{} notifying listener {} of invalid value", this, listener);
            listener.isInvalid(this, value, message);
        }
    }
    }
