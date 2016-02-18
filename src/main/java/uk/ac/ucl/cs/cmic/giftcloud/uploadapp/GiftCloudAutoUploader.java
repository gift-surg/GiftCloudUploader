package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.MasterTrawler;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.BackgroundUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientListStore;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.ZipSeriesUploaderFactorySelector;
import uk.ac.ucl.cs.cmic.giftcloud.util.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class for uploading files and automatically assigning them to projects, subjects, experiments and scans
 */
public class GiftCloudAutoUploader {

    private final BackgroundUploader backgroundUploader;
    private final GiftCloudReporter reporter;
    private final NameGenerator.SubjectNameGenerator subjectNameGenerator;
    private final SubjectAliasStore subjectAliasStore;
    private final GiftCloudProperties properties;

    /**
     * This class is used to automatically and asynchronously group and upload multiple files to a GIFT-Cloud server
     *
     * @para serverFactory
     * @param reporter
     */
    public GiftCloudAutoUploader(final BackgroundUploader backgroundUploader, final GiftCloudProperties properties, final GiftCloudReporter reporter) {
        this.properties = properties;
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

        final Vector<File> fileList = new Vector<File>();
        for (final String path : paths) {
            fileList.add(new File(path));
        }

        final Project project = server.getProject(projectName);
        final SeriesImportFilterApplicatorRetriever seriesImportFilter = project.getSeriesImportFilter(server);

        final EditProgressMonitorWrapper progressWrapper = new EditProgressMonitorWrapper(reporter);
        final MasterTrawler trawler = new MasterTrawler(progressWrapper, fileList, seriesImportFilter);
        final List<Session> sessions = trawler.call();

        for (final Session session : sessions) {

            addSessionToUploadList(server, project, projectName, session);
        }


        List<GiftCloudUploaderError> errors = trawler.getErrorMessages();

        // If any files failed to upload, we log all of them and throw an exception for the first one
        if (errors.size() > 0) {
            final Set<String> uniqueErrors = new HashSet<String>();
            for (final GiftCloudUploaderError error : errors) {
                uniqueErrors.add(error.getUserVisibleMessage());
            }
            final String prefixMessage = errors.size() == 1 ? "1 error" : String.valueOf(errors.size()) + " errors";
            final StringBuilder builder = new StringBuilder();
            builder.append("<html>" + prefixMessage + " occurred during upload:");
            for (final String errorText : uniqueErrors) {
                builder.append("<br>" + errorText);
            }
            builder.append("</html");

            // We would ideally like to display a message to the user if uploading has been initiated via importing, but not in a background context. Not possible with current architecture so suppress the dialog and let the status bar show the error
//            reporter.showMessageToUser(builder.toString());
            throw new GiftCloudException(errors.get(0));
        }

        return true;
    }

    private void addSessionToUploadList(final GiftCloudServer server, final Project project, final String projectName, final Session session) throws IOException {
        final String patientId = session.getPatientId();
        final String patientName = session.getPatientName();
        final String studyInstanceUid = session.getStudyUid();
        final String seriesUid = session.getSeriesUid();

        final XnatModalityParams xnatModalityParams = session.getXnatModalityParams();

        final GiftCloudLabel.SubjectLabel subjectLabel = getSubjectName(server, projectName, patientId, patientName);
        final GiftCloudLabel.ExperimentLabel experimentLabel = getSessionName(server, projectName, subjectLabel, studyInstanceUid, xnatModalityParams);
        final GiftCloudLabel.ScanLabel scanName = getScanName(server, projectName, subjectLabel, experimentLabel, seriesUid, xnatModalityParams);


        project.getDicomMetaDataAnonymiser().fixSessionVariableValues(projectName, subjectLabel, experimentLabel, session.getSampleObject());

        final List<FileCollection> fileCollections = session.getFiles();

        if (fileCollections.isEmpty()) {
            throw new IOException("No files were selected for upload");
        }

        final CallableUploader.CallableUploaderFactory callableUploaderFactory = ZipSeriesUploaderFactorySelector.getZipSeriesUploaderFactory(true);

        // Iterate through each set of files
        for (final FileCollection fileCollection : fileCollections) {
            final UploadParameters uploadParameters = new UploadParameters();
            uploadParameters.setProjectName(projectName);
            uploadParameters.setSubjectLabel(subjectLabel);
            uploadParameters.setExperimentLabel(experimentLabel);
            uploadParameters.setScanLabel(scanName);
            uploadParameters.setFileCollection(fileCollection);
            uploadParameters.setXnatModalityParams(xnatModalityParams);

            final CallableUploader uploader = callableUploaderFactory.create(uploadParameters, server, project.getDicomMetaDataAnonymiser());
            backgroundUploader.addUploader(uploader);
        }
    }

    private synchronized GiftCloudLabel.SubjectLabel getSubjectName(final GiftCloudServer server, final String projectName, final String patientId, final String patientName) throws IOException {
        final Optional<GiftCloudLabel.SubjectLabel> existingSubjectLabel = subjectAliasStore.getSubjectAlias(server, projectName, patientId, patientName);
        if (existingSubjectLabel.isPresent()) {
            return existingSubjectLabel.get();
        } else {
            // Get a list of known subject labels from the server
            Map<String, String> subjectMapFromServer = server.getListOfSubjects(projectName);

            // Generate a new subject label
            subjectNameGenerator.updateSubjectNamePrefix(properties.getSubjectPrefix());
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
