package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.StopTagInputHandler;
import org.nrg.dcm.edit.AttributeException;
import org.nrg.dcm.edit.ScriptApplicator;
import org.nrg.dcm.edit.ScriptEvaluationException;
import org.nrg.dcm.edit.Variable;
import uk.ac.ucl.cs.cmic.giftcloud.data.AssignedSessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariableNames;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.DicomProjectAnonymisationScripts;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Handles anonymisation of DICOM tags using XNAT anonymisation scripts
 */
public class DicomMetaDataAnonymiser {
    private final DicomProjectAnonymisationScripts dicomProjectAnonymisationScripts;
    private GiftCloudReporter reporter;

    public DicomMetaDataAnonymiser(final DicomProjectAnonymisationScripts dicomProjectAnonymisationScripts, final GiftCloudReporter reporter) {
        this.dicomProjectAnonymisationScripts = dicomProjectAnonymisationScripts;
        this.reporter = reporter;
    }

    /** Returns a dcm4chee StopTagInputHandler suitable for parsing a DICOM file sufficiently to implement and verify anonymisation
     * @return
     * @throws IOException
     */
    public StopTagInputHandler makeStopTagInputHandler() throws IOException {

        // The minimum stop tag is set to PatientBirthDate, because we will check the patient name, id and birth date tags to ensure anonymisation has occurred.
        long top = Tag.PatientBirthDate;

        // We go through the script applicators and check if any requires a higher tag number
        for (final ScriptApplicator a : dicomProjectAnonymisationScripts.getDicomScriptApplicators()) {
            final long atop = 0xffffffffL & a.getTopTag();
            if (atop > top) {
                if (0xffffffffL == atop) {  // this means no stop tag
                    return null;
                } else {
                    top = atop;
                }
            }
        }
        return new StopTagInputHandler((int)(top+1));
    }

    /** Applys anonymisation of DICOM tags based on the given list of ScriptApplicators
     *
     * @param outputDicomFile the output file which will contain the anonymised DICOM object
     * @param uploadParameters contains parameters that will be used to modify sesion variables if specified in the ScriptApplicators
     * @param originalDicomObject the original DICOM file containing non-anonymised data
     * @throws AttributeException
     * @throws ScriptEvaluationException
     */
    public void anonymiseMetaData(final File outputDicomFile, final UploadParameters uploadParameters, final DicomObject originalDicomObject) throws AttributeException, ScriptEvaluationException, IOException {

        final Iterable<org.nrg.dcm.edit.ScriptApplicator> applicators = dicomProjectAnonymisationScripts.getDicomScriptApplicators();
        fixSessionVariableValues(uploadParameters.getProjectName(), uploadParameters.getSubjectLabel(), uploadParameters.getExperimentLabel(), originalDicomObject, applicators);

        for (final ScriptApplicator a : applicators) {
            a.apply(outputDicomFile, originalDicomObject);
        }
    }

    /** Set the predefined variables for project, subject and session, so that these can be used in the DICOM anonymisation scripts
     */
    private void fixSessionVariableValues(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, GiftCloudLabel.ExperimentLabel experimentLabel, final DicomObject sampleObject, final Iterable<ScriptApplicator> applicators) {
        final Map<String, SessionVariable> predefs = Maps.newLinkedHashMap();
        predefs.put(SessionVariableNames.PROJECT, new AssignedSessionVariable(SessionVariableNames.PROJECT, projectName));
        predefs.put(SessionVariableNames.SUBJECT, new AssignedSessionVariable(SessionVariableNames.SUBJECT, subjectLabel.getStringLabel()));
        predefs.put(SessionVariableNames.SESSION_LABEL, new AssignedSessionVariable(SessionVariableNames.SESSION_LABEL, experimentLabel.getStringLabel()));
        for (final SessionVariable sessionVariable : getVariables(sampleObject, applicators)) {
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


    private List<SessionVariable> getVariables(final DicomObject sampleObject, final Iterable<ScriptApplicator> applicators) {
        final LinkedHashSet<Variable> dvs = Sets.newLinkedHashSet();
        try {
            // This replaces variables in later scripts with similarly-name variables from
            // earlier scripts. Therefore scripts whose variables should take precedence
            // must appear earlier in the list.
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
