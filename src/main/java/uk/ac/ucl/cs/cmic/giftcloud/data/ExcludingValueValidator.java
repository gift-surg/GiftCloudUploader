/*
 * ExcludingValueValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public final class ExcludingValueValidator implements ValueValidator {
    private final Logger logger = LoggerFactory.getLogger(ExcludingValueValidator.class);
	private final Set<?> excluding;
	private final String message;
	private final boolean isStrict;
	
	public ExcludingValueValidator(final Collection<?> excluding, final String message, final boolean isStrict) {
	    this.excluding = ImmutableSet.copyOf(excluding);
		this.message = message;
		this.isStrict = isStrict;
	}
	
	/* (non-Javadoc)
	 * @see ValueValidator#getMessage(java.lang.Object)
	 */
	public String getMessage(final Object value) {
	    logger.trace("checking for {} in {}", value, excluding);
		return excluding.contains(value) ? String.format(message, value) : null;
	}

	/* (non-Javadoc)
	 * @see ValueValidator#isValid(java.lang.Object)
	 */
	public boolean isValid(final Object value) {
		return !(isStrict && excluding.contains(value));
	}
	
	public boolean contains(final Object value) {
		return excluding.contains(value);
	}
}
