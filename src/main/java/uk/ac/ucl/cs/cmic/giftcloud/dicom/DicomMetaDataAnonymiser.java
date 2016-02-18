package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.edit.ScriptApplicator;
import org.nrg.dcm.edit.Variable;
import uk.ac.ucl.cs.cmic.giftcloud.data.AssignedSessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariableNames;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Handles anonymisation of DICOM tags using XNAT anonymisation scripts
 */
public class DicomMetaDataAnonymiser {
    private final Future<Iterable<ScriptApplicator>> dicomScriptApplicator;
    private GiftCloudReporter reporter;

    public DicomMetaDataAnonymiser(Future<Iterable<ScriptApplicator>> dicomScriptApplicator, final GiftCloudReporter reporter) {
        this.dicomScriptApplicator = dicomScriptApplicator;
        this.reporter = reporter;
    }

    public Iterable<org.nrg.dcm.edit.ScriptApplicator> getDicomScriptApplicators() throws IOException {
        try {
            return dicomScriptApplicator.get();
        } catch (InterruptedException e) {
            throw new IOException("Unable to retrieve Dicom scripts", e.getCause());
        } catch (ExecutionException e) {
            throw new IOException("Unable to retrieve Dicom scripts", e.getCause());
        }
    }

    /** Set the predefined variables for project, subject and session, so that these can be used in the DICOM anonymisation scripts
     * @param projectName
     * @param subjectLabel
     * @param experimentLabel
     * @param sampleObject
     */
    public void fixSessionVariableValues(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, GiftCloudLabel.ExperimentLabel experimentLabel, final DicomObject sampleObject) {
        final Map<String, SessionVariable> predefs = Maps.newLinkedHashMap();
        predefs.put(SessionVariableNames.PROJECT, new AssignedSessionVariable(SessionVariableNames.PROJECT, projectName));
        predefs.put(SessionVariableNames.SUBJECT, new AssignedSessionVariable(SessionVariableNames.SUBJECT, subjectLabel.getStringLabel()));
        predefs.put(SessionVariableNames.SESSION_LABEL, new AssignedSessionVariable(SessionVariableNames.SESSION_LABEL, experimentLabel.getStringLabel()));
        for (final SessionVariable sessionVariable : getVariables(sampleObject)) {
            final String name = sessionVariable.getName();
            if (predefs.containsKey(name)) {
                final SessionVariable predef = predefs.get(name);
                try {
                    sessionVariable.fixValue(predef.getValue());
                } catch (SessionVariable.InvalidValueException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /*
 * (non-Javadoc)
 * @see Session#getVariables()
 */
    private List<SessionVariable> getVariables(final DicomObject sampleObject) {
        final LinkedHashSet<Variable> dvs = Sets.newLinkedHashSet();
        try {
            // This replaces variables in later scripts with similarly-name variables from
            // earlier scripts. Therefore scripts whose variables should take precedence
            // must appear earlier in the list.
            final Iterable<ScriptApplicator> applicators = getDicomScriptApplicators();
            for (final ScriptApplicator a : applicators) {
                for (final Variable v : dvs) {
                    a.unify(v);
                }
                dvs.addAll(a.getVariables().values());
            }
        } catch (Throwable t) { // ToDo: remove this catch, because we want the operation to fail if there is no script
            reporter.silentLogException(t, "Unable to load anonymisation script");
            return Collections.emptyList();
        }
        final List<SessionVariable> vs = Lists.newArrayList();
        for (final Variable dv : dvs) {
            vs.add(DicomSessionVariable.getSessionVariable(dv, sampleObject));
        }
        return vs;
    }
}
