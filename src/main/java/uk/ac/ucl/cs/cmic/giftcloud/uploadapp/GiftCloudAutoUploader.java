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

public class GiftCloudAutoUploader {

    private BackgroundUploader backgroundUploader;
    private final GiftCloudReporter reporter;

    private final String autoSubjectNamePrefix = "AutoUploadSubject";
    private final long autoSubjectNameStartNumber = 0;

    private final String autoSessionNamePrefix = "AutoUploadSession";
    private long autoSessionNameStartNumber = 0;

    private final String autoScanNamePrefix = "AutoUploadScan";
    private long autoScanNameStartNumber = 0;

    private final NameGenerator<GiftCloudLabel.SubjectLabel> subjectNameGenerator = new NameGenerator<GiftCloudLabel.SubjectLabel>(autoSubjectNamePrefix, autoSubjectNameStartNumber, GiftCloudLabel.SubjectLabel.getFactory());
    private final NameGenerator<GiftCloudLabel.ExperimentLabel> experimentLabelNameGenerator = new NameGenerator<GiftCloudLabel.ExperimentLabel>(autoSessionNamePrefix, autoSessionNameStartNumber, GiftCloudLabel.ExperimentLabel.getFactory());
    private final NameGenerator<GiftCloudLabel.ScanLabel> scanNameGenerator = new NameGenerator<GiftCloudLabel.ScanLabel>(autoScanNamePrefix, autoScanNameStartNumber, GiftCloudLabel.ScanLabel.getFactory());

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

    private GiftCloudLabel.ExperimentLabel getSessionName(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectName, final String studyInstanceUid, final Map<String, String> serverSessionMap, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ExperimentLabel> existingExperimentLabel = subjectAliasStore.getExperimentLabel(server, projectName, subjectName, studyInstanceUid);
        if (existingExperimentLabel.isPresent()) {
            return existingExperimentLabel.get();
        } else {
            final GiftCloudLabel.ExperimentLabel newExperimentLabel = experimentLabelNameGenerator.getNewName(serverSessionMap.keySet());
            subjectAliasStore.addExperimentAlias(server, projectName, subjectName, newExperimentLabel, studyInstanceUid, xnatModalityParams);
            return newExperimentLabel;
        }
    }

    private GiftCloudLabel.ScanLabel getScanName(final GiftCloudServer server, final String projectName, final GiftCloudLabel.SubjectLabel subjectName, final GiftCloudLabel.ExperimentLabel experimentName, final String seriesInstanceUid, final Map<String, String> serverScanMap, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ScanLabel> existingScanLabel = subjectAliasStore.getScanLabel(server, projectName, subjectName, experimentName, seriesInstanceUid);
        if (existingScanLabel.isPresent()) {
            return existingScanLabel.get();
        } else {
            final GiftCloudLabel.ScanLabel newScanLabel = scanNameGenerator.getNewName(serverScanMap.keySet());
            subjectAliasStore.addScanAlias(server, projectName, subjectName, experimentName, newScanLabel, seriesInstanceUid, xnatModalityParams);
            return newScanLabel;
        }
    }

    /**
     * Threadsafe class to generate unique names
     */
    private class NameGenerator<T extends GiftCloudLabel> {
        private long nameNumber;
        private final GiftCloudLabel.LabelFactory<T> labelFactory;
        private final String prefix;

        /** Creates a new NameGenerator which will create names starting with the given prefix, and incrementing a suffix number starting at startNumber
         * @param prefix the string prefix for each generated name
         * @param startNumber the number used for the suffix of the first name, which will be incremented after each name generation
         */
        NameGenerator(final String prefix, final long startNumber, final GiftCloudLabel.LabelFactory<T> labelFactory) {
            this.prefix = prefix;
            this.nameNumber = startNumber;
            this.labelFactory = labelFactory;
        }

        /** Returns a unique name that is not part of the given list of known names
         * @param knownNames a list of known names. The returned name will not be one of these
         * @return a new name
         */
        private T getNewName(final Set<String> knownNames) {
            String candidateName;

            do {
                candidateName = getNextName();

            } while (knownNames.contains(candidateName));

            return labelFactory.create(candidateName);
        }

        /** Returns a name that has not been returned before by this object
         * @return a new name
         */
        String getNextName() {
            long nextNameNumber = getNextNameNumber();
            return prefix + Long.toString(nextNameNumber);
        }

        private synchronized long getNextNameNumber() {
            return nameNumber++;
        }
    }
}
