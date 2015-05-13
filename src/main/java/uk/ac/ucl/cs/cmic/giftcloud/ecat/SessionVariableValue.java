/*
 * uk.ac.ucl.cs.cmic.giftcloud.ecat.SessionVariableValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import org.nrg.ecat.edit.Value;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class SessionVariableValue implements Value {
    private final Set<?> variables;
    private final SessionVariable v;

    public SessionVariableValue(final SessionVariable source) {
        this.v = source;
        variables = Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see org.nrg.ecat.edit.Value#getVariables()
     */
    public Set<?> getVariables() { return variables; }

    /* (non-Javadoc)
     * @see org.nrg.ecat.edit.Value#on(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public Object on(final Map m) {
        return v.getValue();
    }
}
