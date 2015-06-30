package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.MasterTrawler;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.BackgroundUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientListStore;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.ZipSeriesUploaderFactorySelector;
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
    private final NameGenerator.SubjectNameGenerator subjectNameGenerator = new NameGenerator.SubjectNameGenerator();
    private final SubjectAliasStore subjectAliasStore;

    /**
     * This class is used to automatically and asynchronously group and upload multiple files to a GIFT-Cloud server
     *
     * @para serverFactory
     * @param reporter
     */
    public GiftCloudAutoUploader(final BackgroundUploader backgroundUploader, final GiftCloudProperties properties, final GiftCloudReporter reporter) {
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
        final List<Session> sessions = new MasterTrawler(progressWrapper, fileList, filter).call();

        // Get a list of subjects from the server
        Map<String, String> subjectMapFromServer;
        Map<String, String> sessionMapFromServer;
        try {
            subjectMapFromServer = server.getListOfSubjects(projectName);
            sessionMapFromServer = server.getListOfSessions(projectName);

        } catch (IOException e) {
            throw new IOException("Uploading could not be performed. The subject and session maps could not be obtained due to the following error: " + e.getMessage(), e);
        }

        final Project project = server.getProject(projectName);

        final Iterable<ScriptApplicator> projectApplicators = project.getDicomScriptApplicators();

        for (final Session session : sessions) {

            addSessionToUploadList(server, project, projectApplicators, projectName, subjectMapFromServer, sessionMapFromServer, session);
        }

        return true;
    }

    private void addSessionToUploadList(final GiftCloudServer server, final Project project, final Iterable<ScriptApplicator> projectApplicators, final String projectName, final Map<String, String> subjectMapFromServer, Map<String, String> sessionMapFromServer, final Session session) throws IOException {
        final String patientId = session.getPatientId();
        final String patientName = session.getPatientName();
        final String studyInstanceUid = session.getStudyUid();
        final String seriesUid = session.getSeriesUid();

        final XnatModalityParams xnatModalityParams = session.getXnatModalityParams();

        final GiftCloudLabel.SubjectLabel subjectLabel = getSubjectName(server, projectName, subjectMapFromServer, patientId, patientName);
        final GiftCloudLabel.ExperimentLabel experimentLabel = getSessionName(server, projectName, subjectLabel, studyInstanceUid, sessionMapFromServer, xnatModalityParams);
        final Map<String, String> scanMapFromServer = server.getListOfScans(projectName, subjectLabel, experimentLabel);
        final GiftCloudLabel.ScanLabel scanName = getScanName(server, projectName, subjectLabel, experimentLabel, seriesUid, scanMapFromServer, xnatModalityParams);

        final GiftCloudSessionParameters sessionParameters = new GiftCloudSessionParameters();
        sessionParameters.setAdminEmail("null@null.com");
        sessionParameters.setExperimentLabel(experimentLabel);
        sessionParameters.setProtocol("");
        sessionParameters.setVisit("");
        sessionParameters.setScanLabel(scanName);
        sessionParameters.setBaseUrl(new URL(server.getGiftCloudServerUrl()));
        sessionParameters.setNumberOfThreads(1);
        sessionParameters.setUsedFixedSize(true);

        final LinkedList<SessionVariable> sessionVariables = Lists.newLinkedList(session.getVariables(project, session));
        sessionParameters.setSessionVariables(sessionVariables);

        final List<FileCollection> fileCollections = session.getFiles();

        if (fileCollections.isEmpty()) {
            reporter.updateStatusText("No files were selected for upload");
            throw new IOException("No files were selected for upload");
        }


        final CallableUploader.CallableUploaderFactory callableUploaderFactory = ZipSeriesUploaderFactorySelector.getZipSeriesUploaderFactory(true);

        backgroundUploader.addFiles(server, fileCollections, xnatModalityParams, projectApplicators, projectName, subjectLabel, sessionParameters, callableUploaderFactory);
    }

    private synchronized GiftCloudLabel.SubjectLabel getSubjectName(final GiftCloudServer server, final String projectName, final Map<String, String> subjectMapFromServer, final String patientId, final String patientName) throws IOException {
        final Optional<GiftCloudLabel.SubjectLabel> existingSubjectLabel = subjectAliasStore.getSubjectAlias(server, projectName, patientId, patientName);
        if (existingSubjectLabel.isPresent()) {
            return existingSubjectLabel.get();
        } else {
            final GiftCloudLabel.SubjectLabel newSubjectLabel = subjectNameGenerator.getNewName(subjectMapFromServer.keySet());
            subjectAliasStore.addSubjectAlias(server, projectName, patientId, newSubjectLabel, patientName);
            return newSubjectLabel;
        }
    }

    private GiftCloudLabel.ExperimentLabel getSessionName(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String studyInstanceUid, final Map<String, String> serverSessionMap, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ExperimentLabel> existingExperimentLabel = subjectAliasStore.getExperimentLabel(server, projectName, subjectLabel, studyInstanceUid);
        if (existingExperimentLabel.isPresent()) {
            return existingExperimentLabel.get();
        } else {
            final GiftCloudLabel.ExperimentLabel newExperimentLabel = subjectNameGenerator.getExperimentNameGenerator(subjectLabel).getNewName(serverSessionMap.keySet());
            subjectAliasStore.addExperimentAlias(server, projectName, subjectLabel, newExperimentLabel, studyInstanceUid, xnatModalityParams);
            return newExperimentLabel;
        }
    }

    private GiftCloudLabel.ScanLabel getScanName(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String seriesInstanceUid, final Map<String, String> serverScanMap, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ScanLabel> existingScanLabel = subjectAliasStore.getScanLabel(server, projectName, subjectLabel, experimentLabel, seriesInstanceUid);
        if (existingScanLabel.isPresent()) {
            return existingScanLabel.get();
        } else {
            final GiftCloudLabel.ScanLabel newScanLabel = subjectNameGenerator.getExperimentNameGenerator(subjectLabel).getScanNameGenerator(experimentLabel).getNewName(serverScanMap.keySet());
            subjectAliasStore.addScanAlias(server, projectName, subjectLabel, experimentLabel, newScanLabel, seriesInstanceUid, xnatModalityParams);
            return newScanLabel;
        }
    }


}
