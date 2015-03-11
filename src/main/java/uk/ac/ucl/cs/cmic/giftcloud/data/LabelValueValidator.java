/*
 * LabelValueValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class LabelValueValidator implements ValueValidator {
	private static final LabelValueValidator instance = new LabelValueValidator();

	private final Logger logger = LoggerFactory.getLogger(LabelValueValidator.class);

	private LabelValueValidator() {}

	public static LabelValueValidator getInstance() { return instance; }

	/* (non-Javadoc)
	 * @see ValueValidator#getMessage(java.lang.Object)
	 */
	public String getMessage(final Object value) {
		if (isValid(value)) {
			return null;
		} else if (null == value || "".equals(value)) {
			return "Empty label not allowed.";
		} else {
			return "Only numbers, letters, or _ allowed in label.";
		}
	}

	/* (non-Javadoc)
	 * @see ValueValidator#isValid(java.lang.Object)
	 */
	public boolean isValid(final Object value) {
		logger.trace("Checking {} for validity", value);
        if (value == null) {
            return false;
        }
        final String contents = value.toString();
		return StringUtils.isNotBlank(contents) && LABEL_VALIDATOR.matcher(contents).matches();
	}

    private static final Pattern LABEL_VALIDATOR = Pattern.compile("^[A-z0-9_-]{1,64}$");
}
