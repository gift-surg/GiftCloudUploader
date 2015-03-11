/*
 * uk.ac.ucl.cs.cmic.giftcloud.ecat.FormatSessionDateFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.nrg.ecat.edit.ScriptEvaluationException;
import org.nrg.ecat.edit.ScriptFunction;
import org.nrg.ecat.edit.Value;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;

public final class FormatSessionDateFunction implements ScriptFunction {
    private static final String name="formatSessionDate";
    private final Callable<Session> sessionSource;

    public FormatSessionDateFunction(final Callable<Session> sessionSource) {
        this.sessionSource = sessionSource;
    }

    /* (non-Javadoc)
     * @see org.nrg.ecat.edit.ScriptFunction#apply(java.util.List)
     */
    public Value apply(final List args)
    throws ScriptEvaluationException {
        try {
            return new DelayedSessionDateValue((Value)args.get(0));
        } catch (Exception e) {
            throw new ScriptEvaluationException("unable to extract argument for function " + name, e);
        }
    }

    private final class DelayedSessionDateValue implements Value {
        private final Value fv;

        DelayedSessionDateValue(final Value format) {
            this.fv = format;
        }

        public Set getVariables() {
            return fv.getVariables();
        }

        @SuppressWarnings("unchecked")
        public String on(Map m) throws ScriptEvaluationException {
            final String format = (String)fv.on(m);
            final Session session;
            try {
                session = sessionSource.call();
            } catch (Exception e) {
                throw new ScriptEvaluationException("unable to retrieve session", e);
            }
            if (null == session) {
                return null;
            }
            final Date d = session.getDateTime();
            return null == d ? null : new SimpleDateFormat(format).format(d);
        }
    }
}
