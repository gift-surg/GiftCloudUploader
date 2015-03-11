/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.SessionVariableValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import java.util.*;

import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.edit.Value;
import org.nrg.dcm.edit.Variable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;

public final class SessionVariableValue implements Value {
    private final Set<?> tags;
    private final Set<?> variables;
    private SessionVariable variable;

    /**
     * 
     */
    public SessionVariableValue(final SessionVariable source) {
        if (source instanceof DicomSessionVariable) {
            final DicomSessionVariable dsv = (DicomSessionVariable)source;
            tags = Collections.unmodifiableSet(dsv.getTags());
            variables = Collections.unmodifiableSet(dsv.getVariables());
        } else {
            tags = Collections.emptySet();
            variables = Collections.emptySet();
        }
        variable = source;
    }


    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#getTags()
     */
    @SuppressWarnings("unchecked")
    public SortedSet getTags() {
        return new TreeSet<Object>(tags);
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#getVariables()
     */
    @SuppressWarnings("unchecked")
    public Set getVariables() { return variables; }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#on(org.dcm4che2.data.DicomObject)
     */
    public String on(DicomObject o) {
        return variable.getValue();
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#on(java.util.Map)
     */
    public String on(Map m) {
        return variable.getValue();
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#replaceVariable(org.nrg.dcm.edit.Variable)
     */
    public void replace(final Variable _) {}
    
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append("[").append(variable).append("]");
        return sb.toString();
    }
}
