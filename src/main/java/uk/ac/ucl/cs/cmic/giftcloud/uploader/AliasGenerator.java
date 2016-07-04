package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;
import java.util.*;

/**
 * Class for assigning pseudonames
 */
public class AliasGenerator {

    private final NameGenerator.SubjectNameGenerator subjectNameGenerator;
    private final SubjectAliasStore subjectAliasStore;
    private final GiftCloudProperties properties;

    public AliasGenerator(final GiftCloudProperties properties, final GiftCloudReporter reporter) {
        this.properties = properties;
        subjectNameGenerator = new NameGenerator.SubjectNameGenerator(properties.getSubjectPrefix());
        subjectAliasStore = new SubjectAliasStore(new PatientListStore(properties, reporter), reporter);
    }

    /**
     * Force saving of the patient list
     */
    public void exportPatientList() {
        subjectAliasStore.exportPatientList();
    }

    synchronized GiftCloudLabel.SubjectLabel getSubjectName(boolean requireHashing, final GiftCloudServer server, final String projectName, final String patientId, final String patientName) throws IOException {
        final Optional<GiftCloudLabel.SubjectLabel> existingSubjectLabel = subjectAliasStore.getSubjectAlias(requireHashing, server, projectName, patientId, patientName);
        if (existingSubjectLabel.isPresent()) {
            return existingSubjectLabel.get();
        } else {
            // Get a list of known subject labels from the server
            Map<String, String> subjectMapFromServer = server.getListOfSubjects(projectName);

            // Generate a new subject label
            subjectNameGenerator.updateSubjectNamePrefix(properties.getSubjectPrefix());
            final GiftCloudLabel.SubjectLabel newSubjectLabel = subjectNameGenerator.getNewName(subjectMapFromServer.keySet());

            // Add the label and its uid alias
            subjectAliasStore.addSubjectAlias(requireHashing, server, projectName, patientId, newSubjectLabel, patientName);
            return newSubjectLabel;
        }
    }

    GiftCloudLabel.ExperimentLabel getSessionName(boolean requireHashing, final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String studyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ExperimentLabel> existingExperimentLabel = subjectAliasStore.getExperimentLabel(requireHashing, server, projectName, subjectLabel, studyInstanceUid);
        if (existingExperimentLabel.isPresent()) {
            return existingExperimentLabel.get();
        } else {
            // Get a list of known experiment labels from the server for this project
            final Map<String, String> experimentMapFromServer = server.getListOfSessions(projectName);

            // Generate a new experiment label
            final GiftCloudLabel.ExperimentLabel newExperimentLabel = subjectNameGenerator.getExperimentNameGenerator(subjectLabel).getNewName(experimentMapFromServer.keySet());

            // Add the label and its uid alias
            subjectAliasStore.addExperimentAlias(requireHashing, server, projectName, subjectLabel, newExperimentLabel, studyInstanceUid, xnatModalityParams);
            return newExperimentLabel;
        }
    }

    GiftCloudLabel.ScanLabel getScanName(boolean requireHashing, final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String seriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ScanLabel> existingScanLabel = subjectAliasStore.getScanLabel(requireHashing, server, projectName, subjectLabel, experimentLabel, seriesInstanceUid);
        if (existingScanLabel.isPresent()) {
            return existingScanLabel.get();
        } else {
            // Get a list of known scan labels from the server for this project, subject and experiment
            final Map<String, String> scanMapFromServer = server.getListOfScans(projectName, subjectLabel, experimentLabel);

            // Generate a new scan label
            final GiftCloudLabel.ScanLabel newScanLabel = subjectNameGenerator.getExperimentNameGenerator(subjectLabel).getScanNameGenerator(experimentLabel).getNewName(scanMapFromServer.keySet());

            // Add the label and its uid alias
            subjectAliasStore.addScanAlias(requireHashing, server, projectName, subjectLabel, experimentLabel, newScanLabel, seriesInstanceUid, xnatModalityParams);
            return newScanLabel;
        }
    }
}
