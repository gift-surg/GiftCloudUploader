package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.data.*;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.MasterTrawler;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.EditProgressMonitorWrapper;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Class for uploading files and automatically assigning them to projects, subjects, experiments and scans
 */
public class GiftCloudAutoUploader {

    private final BackgroundUploader backgroundUploader;
    private final GiftCloudReporter reporter;
    private final NameGenerator.SubjectNameGenerator subjectNameGenerator;
    private final SubjectAliasStore subjectAliasStore;

    /**
     * This class is used to automatically and asynchronously group and upload multiple files to a GIFT-Cloud server
     *
     * @para serverFactory
     * @param reporter
     */
    public GiftCloudAutoUploader(final BackgroundUploader backgroundUploader, final GiftCloudProperties properties, final GiftCloudReporter reporter) {
        this.subjectNameGenerator = new NameGenerator.SubjectNameGenerator(properties.getSubjectPrefix());
        this.backgroundUploader = backgroundUploader;
        this.reporter = reporter;
        subjectAliasStore = new SubjectAliasStore(new PatientListStore(properties, reporter), reporter);
    }

    public boolean uploadToGiftCloud(final GiftCloudServer server, final Vector<String> paths, final String projectName) throws IOException {
        return uploadOrAppend(server, paths, projectName, false);
    }

    public boolean appendToGiftCloud(final GiftCloudServer server, final Vector<String> paths, final String projectName) throws IOException {
        return uploadOrAppend(server, paths, projectName, true);
    }

    /**
     * Force saving of the patient list
     */
    public void exportPatientList() {
        subjectAliasStore.exportPatientList();
    }

    private boolean uploadOrAppend(final GiftCloudServer server, final Vector<String> paths, final String projectName, final boolean append) throws IOException {

        SeriesImportFilterApplicatorRetriever filter;
        try {
            if (StringUtils.isEmpty(projectName)) {
                final Optional<String> emptyProject = Optional.empty();
                filter = new SeriesImportFilterApplicatorRetriever(server, emptyProject);
            } else {
                filter = new SeriesImportFilterApplicatorRetriever(server, Optional.of(projectName));
            }
        } catch (Exception exception) {
            throw new IOException("Error encountered retrieving series import filters", exception);
        }

        final Vector<File> fileList = new Vector<File>();
        for (final String path : paths) {
            fileList.add(new File(path));
        }

        final EditProgressMonitorWrapper progressWrapper = new EditProgressMonitorWrapper(reporter);
        final MasterTrawler trawler = new MasterTrawler(progressWrapper, fileList, filter);
        final List<Session> sessions = trawler.call();

        final Project project = server.getProject(projectName);

        final Iterable<ScriptApplicator> projectApplicators = project.getDicomScriptApplicators();

        for (final Session session : sessions) {

            addSessionToUploadList(server, project, projectApplicators, projectName, session);
        }


        List<GiftCloudUploaderError> errors = trawler.getErrorMessages();

        // If any files failed to upload, we log all of them and throw an exception for the first one
        if (errors.size() > 0) {
            for (final GiftCloudUploaderError error : errors) {
                reporter.warn("Failed to upload file: " + error.getUserVisibleMessage());
            }
            throw new GiftCloudException(errors.get(0));
        }

        return true;
    }

    private void addSessionToUploadList(final GiftCloudServer server, final Project project, final Iterable<ScriptApplicator> projectApplicators, final String projectName, final Session session) throws IOException {
        final String patientId = session.getPatientId();
        final String patientName = session.getPatientName();
        final String studyInstanceUid = session.getStudyUid();
        final String seriesUid = session.getSeriesUid();

        final XnatModalityParams xnatModalityParams = session.getXnatModalityParams();

        final GiftCloudLabel.SubjectLabel subjectLabel = getSubjectName(server, projectName, patientId, patientName);
        final GiftCloudLabel.ExperimentLabel experimentLabel = getSessionName(server, projectName, subjectLabel, studyInstanceUid, xnatModalityParams);
        final GiftCloudLabel.ScanLabel scanName = getScanName(server, projectName, subjectLabel, experimentLabel, seriesUid, xnatModalityParams);

        final GiftCloudSessionParameters sessionParameters = new GiftCloudSessionParameters();
        sessionParameters.setAdminEmail("null@null.com");
        sessionParameters.setExperimentLabel(experimentLabel);
        sessionParameters.setProtocol("");
        sessionParameters.setVisit("");
        sessionParameters.setScanLabel(scanName);
        sessionParameters.setBaseUrl(new URL(server.getGiftCloudServerUrl()));
        sessionParameters.setNumberOfThreads(1);
        sessionParameters.setUsedFixedSize(true);

        // Set the predefined variables for project, subject and session, so that these can be used in the DICOM anonymisation scripts
        final Map<String, SessionVariable> predefs = Maps.newLinkedHashMap();
        predefs.put(SessionVariableNames.PROJECT, new AssignedSessionVariable(SessionVariableNames.PROJECT, projectName));
        predefs.put(SessionVariableNames.SUBJECT, new AssignedSessionVariable(SessionVariableNames.SUBJECT, subjectLabel.getStringLabel()));
        predefs.put(SessionVariableNames.SESSION_LABEL, new AssignedSessionVariable(SessionVariableNames.SESSION_LABEL, experimentLabel.getStringLabel()));
        for (final SessionVariable sessionVariable : session.getVariables(project, session)) {
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

        final LinkedList<SessionVariable> sessionVariables = Lists.newLinkedList(session.getVariables(project, session));
        sessionParameters.setSessionVariables(sessionVariables);

        final List<FileCollection> fileCollections = session.getFiles();

        if (fileCollections.isEmpty()) {
            throw new IOException("No files were selected for upload");
        }


        final CallableUploader.CallableUploaderFactory callableUploaderFactory = ZipSeriesUploaderFactorySelector.getZipSeriesUploaderFactory(true);

        backgroundUploader.addFiles(server, fileCollections, xnatModalityParams, projectApplicators, projectName, subjectLabel, sessionParameters, callableUploaderFactory);
    }

    private synchronized GiftCloudLabel.SubjectLabel getSubjectName(final GiftCloudServer server, final String projectName, final String patientId, final String patientName) throws IOException {
        final Optional<GiftCloudLabel.SubjectLabel> existingSubjectLabel = subjectAliasStore.getSubjectAlias(server, projectName, patientId, patientName);
        if (existingSubjectLabel.isPresent()) {
            return existingSubjectLabel.get();
        } else {
            // Get a list of known subject labels from the server
            Map<String, String> subjectMapFromServer = server.getListOfSubjects(projectName);

            // Generate a new subject label
            final GiftCloudLabel.SubjectLabel newSubjectLabel = subjectNameGenerator.getNewName(subjectMapFromServer.keySet());

            // Add the label and its uid alias
            subjectAliasStore.addSubjectAlias(server, projectName, patientId, newSubjectLabel, patientName);
            return newSubjectLabel;
        }
    }

    private GiftCloudLabel.ExperimentLabel getSessionName(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String studyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ExperimentLabel> existingExperimentLabel = subjectAliasStore.getExperimentLabel(server, projectName, subjectLabel, studyInstanceUid);
        if (existingExperimentLabel.isPresent()) {
            return existingExperimentLabel.get();
        } else {
            // Get a list of known experiment labels from the server for this project
            final Map<String, String> experimentMapFromServer = server.getListOfSessions(projectName);

            // Generate a new experiment label
            final GiftCloudLabel.ExperimentLabel newExperimentLabel = subjectNameGenerator.getExperimentNameGenerator(subjectLabel).getNewName(experimentMapFromServer.keySet());

            // Add the label and its uid alias
            subjectAliasStore.addExperimentAlias(server, projectName, subjectLabel, newExperimentLabel, studyInstanceUid, xnatModalityParams);
            return newExperimentLabel;
        }
    }

    private GiftCloudLabel.ScanLabel getScanName(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String seriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ScanLabel> existingScanLabel = subjectAliasStore.getScanLabel(server, projectName, subjectLabel, experimentLabel, seriesInstanceUid);
        if (existingScanLabel.isPresent()) {
            return existingScanLabel.get();
        } else {
            // Get a list of known scan labels from the server for this project, subject and experiment
            final Map<String, String> scanMapFromServer = server.getListOfScans(projectName, subjectLabel, experimentLabel);

            // Generate a new scan label
            final GiftCloudLabel.ScanLabel newScanLabel = subjectNameGenerator.getExperimentNameGenerator(subjectLabel).getScanNameGenerator(experimentLabel).getNewName(scanMapFromServer.keySet());

            // Add the label and its uid alias
            subjectAliasStore.addScanAlias(server, projectName, subjectLabel, experimentLabel, newScanLabel, seriesInstanceUid, xnatModalityParams);
            return newScanLabel;
        }
    }


}
