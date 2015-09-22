/*
 * SessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

public interface SessionVariable {
    void addListener(ValueListener listener);

    String getDescription();
    String getExportField();

    String getName();
    String getValue();

    String setDescription(String description);
    SessionVariable fixValue();
    SessionVariable fixValue(String value) throws InvalidValueException;
    String setValue(String value) throws InvalidValueException;
    String validate(String value) throws InvalidValueException;

    class InvalidValueException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidValueException(final String message) {
            super(message);
        }
    }
}
