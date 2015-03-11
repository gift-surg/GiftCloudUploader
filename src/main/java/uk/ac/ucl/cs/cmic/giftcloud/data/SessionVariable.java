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

import java.awt.Component;

public interface SessionVariable {
    void addListener(ValueListener listener);
    void addValidator(ValueValidator validator);
    String getDescription();
    String getExportField();
    Component getEditor();
    String getName();
    String getValue();
    String getValueMessage();
    boolean isMutable();
    boolean isHidden();
    void refresh();
    void removeListener(ValueListener listener);
    String setDescription(String description);
    SessionVariable fixValue();
    SessionVariable fixValue(String value) throws InvalidValueException;
    String setValue(String value) throws InvalidValueException;
    String validate(String value) throws InvalidValueException;

    public class InvalidValueException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidValueException(final String message) {
            super(message);
        }

        public InvalidValueException(final SessionVariable variable, final String value) {
            this(buildMessage(variable, value));
        }

        public InvalidValueException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public InvalidValueException(final SessionVariable variable, final String value,
                final Throwable cause) {
            this(buildMessage(variable, value), cause);
        }

        private static String buildMessage(final SessionVariable variable,
                final String value) {
            final StringBuilder sb = new StringBuilder("Invalid value for ");
            sb.append(variable.getName()).append(": ");
            sb.append(value);
            return sb.toString();
        }
    }
}
