package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.google.common.collect.Lists;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadResult;
import uk.ac.ucl.cs.cmic.giftcloud.util.ProgressHandleWrapper;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.MasterTrawler;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SubjectAliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.SwingUploadFailureHandler;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.util.EditProgressMonitorWrapper;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.OneWayHash;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class GiftCloudAutoUploader {

    private final MultiUploadReporter reporter;

    // Create a map of subjects and sessions we have already uploaded
    private final Map<String, String> sessionsAlreadyUploaded = new HashMap<String, String>();
    private final Map<String, String> scansAlreadyUploaded = new HashMap<String, String>();

    private final String autoSubjectNamePrefix = "AutoUploadSubject";
    private final long autoSubjectNameStartNumber = 0;

    private final String autoSessionNamePrefix = "AutoUploadSession";
    private long autoSessionNameStartNumber = 0;

    private final String autoScanNamePrefix = "AutoUploadScan";
    private long autoScanNameStartNumber = 0;

    private final NameGenerator subjectNameGenerator = new NameGenerator(autoSubjectNamePrefix, autoSubjectNameStartNumber);
    private final NameGenerator sessionNameGenerator = new NameGenerator(autoSessionNamePrefix, autoSessionNameStartNumber);
    private final NameGenerator scanNameGenerator = new NameGenerator(autoScanNamePrefix, autoScanNameStartNumber);

    private final SubjectAliasMap subjectAliasMap;
    private GiftCloudServerFactory serverFactory;


    /**
     * This class is used to automatically and asynchronously group and upload multiple files to a GIFT-Cloud server
     *
     * @para serverFactory
     * @param reporter
     */
    public GiftCloudAutoUploader(final GiftCloudServerFactory serverFactory, final MultiUploadReporter reporter) {
        this.serverFactory = serverFactory;
        this.reporter = reporter;
        subjectAliasMap = new SubjectAliasMap();
    }

    public boolean uploadToGiftCloud(final GiftCloudServer server, final Vector<String> paths, final String projectName) throws IOException {
        return uploadOrAppend(server, paths, projectName, false);
    }

    public boolean appendToGiftCloud(final GiftCloudServer server, final Vector<String> paths, final String projectName) throws IOException {
        return uploadOrAppend(server, paths, projectName, true);
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



        final ProgressHandleWrapper progressHandleWrapper = new ProgressHandleWrapper(reporter);

        final Vector<File> fileList = new Vector<File>();
        for (final String path : paths) {
            fileList.add(new File(path));
        }

        final EditProgressMonitorWrapper progressWrapper = new EditProgressMonitorWrapper(reporter);
        final List<Session> sessions = new MasterTrawler(progressWrapper, fileList, filter).call();

        final ExecutorService executorService = Executors.newCachedThreadPool();

        // Get a list of subjects from the server
        Map<String, String> subjectMapFromServer;
        Map<String, String> sessionMapFromServer;
        try {
            subjectMapFromServer = server.getListOfSubjects(projectName);
            sessionMapFromServer = server.getListOfSessions(projectName);

        } catch (IOException e) {
            throw new IOException("Uploading could not be performed. The subject and session map could not be obtained due to the following error: " + e.getMessage(), e);
        }


        for (final Session session : sessions) {

            UploadResult uploadSuccess = addSessionToUploadList(server, projectName, append, executorService, subjectMapFromServer, sessionMapFromServer, session);
        }

        return true;
    }

    private UploadResult addSessionToUploadList(final GiftCloudServer server, final String projectName, final boolean append, ExecutorService executorService, Map<String, String> subjectMapFromServer, Map<String, String> sessionMapFromServer, final Session session) throws IOException {
        final String patientId = session.getPatientId();
        final String studyUid = session.getStudyUid();
        final String seriesUid = session.getSeriesUid();

        final String subjectName = getSubjectName(server, projectName, subjectMapFromServer, patientId);
        final String sessionName = getSessionName(server, studyUid, sessionMapFromServer);
        final String scanName = getScanName(seriesUid, sessionMapFromServer);

        final GiftCloudSessionParameters sessionParameters = new GiftCloudSessionParameters();
        sessionParameters.setAdminEmail("null@null.com");
        sessionParameters.setSessionLabel(sessionName);
        sessionParameters.setProtocol("");
        sessionParameters.setVisit("");
        sessionParameters.setScan(scanName);
        sessionParameters.setBaseUrl(new URL(server.getGiftCloudServerUrl()));
        sessionParameters.setNumberOfThreads(1);
        sessionParameters.setUsedFixedSize(true);

        final Project project = new Project(projectName, server.getRestServerHelper());

        final LinkedList<SessionVariable> sessionVariables = Lists.newLinkedList(session.getVariables(project, session));
        sessionParameters.setSessionVariables(sessionVariables);


        final Optional<String> windowTitle = Optional.empty();
        final Optional<JSObject> jsContext = Optional.empty();

        final String finalSubjectName = subjectName;

        Future<UploadResult> upload = executorService.submit(new Callable<UploadResult>() {
            public UploadResult call() throws IOException {
                try {
                    UploadResult returnValue;
                    if (append) {
                        returnValue = session.appendTo(projectName, finalSubjectName, server, sessionParameters, project, new SwingUploadFailureHandler(), reporter);
                    } else {
                        returnValue = session.uploadTo(projectName, finalSubjectName, server, sessionParameters, project, new SwingUploadFailureHandler(), reporter);
                    }
                    return returnValue;
                } catch (CancellationException exception) {
                    // Cancellation is the only type of exception for which we don't attempt to upload any more files
                    throw exception;
                }
                // We let other exceptions fall through and cause an ExecutionException
            }
        });

        try {
            return upload.get();
        } catch (InterruptedException e) {
            final Throwable cause = e.getCause();
            throw new IOException("Uploading was interrupted due to the following error: " + cause.getMessage(), cause);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            throw new IOException("Uploading failed due to the following error: " + cause.getMessage(), cause);
        }
    }

    private synchronized String getSubjectName(final GiftCloudServer server, final String projectName, final Map<String, String> subjectMapFromServer, final String patientId) throws IOException {
        final Optional<String> subjectAlias = subjectAliasMap.getSubjectAlias(server, projectName, patientId);
        if (subjectAlias.isPresent()) {
            return subjectAlias.get();
        } else {
            final String subjectName = subjectNameGenerator.getNewName(subjectMapFromServer.keySet());
            subjectAliasMap.addSubjectAlias(server, projectName, patientId, subjectName);
            return subjectName;
        }
    }

    private String getSessionName(final GiftCloudServer server, final String studyUid, final Map<String, String> serverSessionMap) {

        if (StringUtils.isNotBlank(studyUid) && sessionsAlreadyUploaded.containsKey(studyUid)) {
            return sessionsAlreadyUploaded.get(studyUid);
        }

        if (StringUtils.isNotBlank(studyUid)) {
            final String sessionName = OneWayHash.hashUid(studyUid);
            sessionsAlreadyUploaded.put(studyUid, sessionName);
            return sessionName;
        }

        final String sessionName = sessionNameGenerator.getNewName(serverSessionMap.keySet());
        if (StringUtils.isNotBlank(studyUid)) {
            sessionsAlreadyUploaded.put(studyUid, sessionName);
        }
        return sessionName;
    }

    private String getScanName(final String seriesUid, final Map<String, String> serverScanMap) {

        if (StringUtils.isNotBlank(seriesUid) && scansAlreadyUploaded.containsKey(seriesUid)) {
            return scansAlreadyUploaded.get(seriesUid);
        }

        if (StringUtils.isNotBlank(seriesUid)) {
            final String scanName = OneWayHash.hashUid(seriesUid);
            scansAlreadyUploaded.put(seriesUid, scanName);
            return scanName;
        }

        final String scanName = scanNameGenerator.getNewName(serverScanMap.keySet());
        if (StringUtils.isNotBlank(seriesUid)) {
            sessionsAlreadyUploaded.put(seriesUid, scanName);
        }
        return scanName;
    }

    /**
     * Threadsafe class to generate unique names
     */
    private class NameGenerator {
        private long nameNumber;
        private final String prefix;

        /** Creates a new NameGenerator which will create names starting with the given prefix, and incrementing a suffix number starting at startNumber
         * @param prefix the string prefix for each generated name
         * @param startNumber the number used for the suffix of the first name, which will be incremented after each name generation
         */
        NameGenerator(final String prefix, final long startNumber) {
            this.prefix = prefix;
            this.nameNumber = startNumber;
        }

        /** Returns a unique name that is not part of the given list of known names
         * @param knownNames a list of known names. The returned name will not be one of these
         * @return a new name
         */
        private String getNewName(final Set<String> knownNames) {
            String candidateName;

            do {
                candidateName = getNextName();

            } while (knownNames.contains(candidateName));

            return candidateName;
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
