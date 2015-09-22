/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.DicomSessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;


import com.google.common.base.Strings;
import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.edit.MultipleInitializationException;
import org.nrg.dcm.edit.ScriptEvaluationException;
import org.nrg.dcm.edit.Value;
import org.nrg.dcm.edit.Variable;
import uk.ac.ucl.cs.cmic.giftcloud.data.AbstractSessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.ValueListener;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public abstract class DicomSessionVariable
extends AbstractSessionVariable implements SessionVariable,ValueListener {
    private final Variable v;
    private final DicomObject sample;
    private String message = null;

    public static DicomSessionVariable getSessionVariable(final Variable v, final DicomObject sample) {
        return new TextDicomVariable(v, sample);
    }

    DicomSessionVariable(final Variable v, final DicomObject sample) {
        super(v.getName());
        this.sample = sample;
        this.v = v;
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#getDescription()
     */
    public String getDescription() {
        final String description = v.getDescription();
        return Strings.isNullOrEmpty(description) ? v.getName() : description;
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#getExportField()
     */
    public String getExportField() {
        return v.getExportField();
    }

    /* (non-Javadoc)
     * @see SessionVariable#getValue()
     */
    public String getValue() {
        final String value = v.getValue();
        if (null == value) {
            final Value iv = v.getInitialValue();
            if (null == iv) {
                return null;
            } else {
                try {
                    return iv.on(sample);
                } catch (ScriptEvaluationException e) {
                    final StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append("An serious error was encountered while preparing the DICOM.\n");
                    errorMessage.append("Please contact your site administrator with the following error\n");
                    errorMessage.append("message before uploading this data, as it could contain incorrect data.\n\n");
                    errorMessage.append(e.getMessage());

                    //non-ideal, but somewhat protects against HTTP issues in GetURL
                    JOptionPane.showMessageDialog(null, errorMessage.toString(), "DICOM Script Error", JOptionPane.ERROR_MESSAGE);

                    return null;
                }
            }
        } else {
            return value;
        }
    }

    /**
     * Gets the underlying DicomEdit script variable.
     * @return script variable
     */
    public Variable getVariable() {
        return v;
    }

    protected void editTo(final String value) {
        try {
            message = validate(value);
            v.setValue(value);
            fireHasChanged();
        } catch (InvalidValueException e) {
            fireIsInvalid(v, e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * @see AbstractSessionVariable#setDescription(java.lang.String)
     */
    @Override
    public String setDescription(final String description) {
        // Set the description only if it's not set in the source variable.
        if (Strings.isNullOrEmpty(v.getDescription())) {
            return super.setDescription(description);
        } else {
            return v.getDescription();
        }
    }
    
    public void setIsHidden(final boolean isHidden) {
        v.setIsHidden(isHidden);
    }

    /* (non-Javadoc)
     * @see SessionVariable#setValue(java.lang.String)
     */
    public String setValue(final String value) throws InvalidValueException {
        message = validate(value);
        final String old = v.getValue();
            v.setValue(value);
        return old;
    }

    public void setInitialValue(final Value value) throws MultipleInitializationException {
        v.setInitialValue(value);
    }

    public boolean hasInitialValue() {
        return null != v.getInitialValue();
    }

    public Set<?> getTags() {
        final Value iv = v.getInitialValue();
        return null == iv ? Collections.emptySet() : iv.getTags();
    }

    public Set<?> getVariables() {
        final Value iv = v.getInitialValue();
        return null == iv ? Collections.emptySet() : iv.getVariables();
    }

    /*
     * (non-Javadoc)
     * @see ValueListener#hasChanged(SessionVariable)
     */
    public void hasChanged(final SessionVariable variable) {
        fireHasChanged();
    }

    /*
     * (non-Javadoc)
     * @see ValueListener#isInvalid(SessionVariable, java.lang.Object, java.lang.String)
     */
    public void isInvalid(SessionVariable variable, Object value, String message) {}

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append("/").append(v);
        sb.append(" ").append(getName()).append(" = ").append(getValue());
        sb.append(" [init ").append(v.getInitialValue()).append("]");
        return sb.toString();
    }
}
