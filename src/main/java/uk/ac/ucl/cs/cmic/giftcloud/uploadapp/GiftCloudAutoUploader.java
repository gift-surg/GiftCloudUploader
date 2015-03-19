package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.fasterxml.uuid.Generators;
import com.google.common.collect.Lists;
import com.pixelmed.display.EmptyProgress;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.MasterTrawler;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerXnat;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SeriesImportFilterApplicatorRetriever;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.SwingProgressMonitor;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.SwingUploadFailureHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class GiftCloudAutoUploader {

    private GiftCloudReporter reporter;
    private RestServerHelper restServerHelper;
    private Container container;

    private final String giftCloudServerUrl;

    // Create a map of subjects and sessions we have already uploaded
    final Map<String, String> subjectsAlreadyUploaded = new HashMap<String, String>();
    final Map<String, String> sessionsAlreadyUploaded = new HashMap<String, String>();
    final Map<String, String> scansAlreadyUploaded = new HashMap<String, String>();




    final String temporarySubjectNamePrefix = "AutoUploadSubject";
    long temporarySubjectNameNum = 0;

    final String temporarySessionNamePrefix = "AutoUploadSession";
    long temporarySessionNameNum = 0;

    final String temporaryScanNamePrefix = "AutoUploadScan";
    long temporaryScanNameNum = 0;

    public GiftCloudAutoUploader(final Container container, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) throws IOException {
        this.reporter = reporter;
        this.container = container;
        giftCloudServerUrl = giftCloudProperties.getGiftCloudUrl().get();

        if (StringUtils.isBlank(giftCloudServerUrl)) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        final RestServerXnat restServerXnat = new RestServerXnat(giftCloudProperties, giftCloudServerUrl, reporter);
        restServerHelper = new RestServerHelper(restServerXnat, reporter);
    }

    public void tryAuthentication() {
        try {
            restServerHelper.tryAuthentication();
        } catch (CancellationException e) {

        } catch (Exception e) {
            JOptionPane.showMessageDialog(container, "Could not log into GIFT-Cloud due to the following error: " + e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
            // ToDo: log error here
        }

    }

    public Vector<Object> getListOfProjects() throws IOException {
        return restServerHelper.getListOfProjects();
    }

    public String getUrl() {
        return giftCloudServerUrl;
    }

    public boolean uploadToGiftCloud(Vector<String> paths, final String projectName) throws IOException {


        SeriesImportFilterApplicatorRetriever filter;
        try {
            if (StringUtils.isEmpty(projectName)) {
                final Optional<String> emptyProject = Optional.empty();
                filter = new SeriesImportFilterApplicatorRetriever(restServerHelper, emptyProject);
            } else {
                filter = new SeriesImportFilterApplicatorRetriever(restServerHelper, Optional.of(projectName));
            }
        } catch (Exception exception) {
            throw new IOException("Error encountered retrieving series import filters", exception);
        }

        final SwingProgressMonitor progress = new SwingProgressMonitor(container, "Finding data files", "searching", 0, paths.size());

        final EmptyProgress emptyProgress = new EmptyProgress();

        final Vector<File> fileList = new Vector<File>();
        for (final String path : paths) {
            fileList.add(new File(path));
        }


        final List<Session> sessions = new MasterTrawler(progress, fileList, filter).call();

        final ExecutorService executorService = Executors.newCachedThreadPool();

        // Get a list of subjects from the server
        Map<String, String> subjectMapFromServer;
        Map<String, String> sessionMapFromServer;
        try {
            subjectMapFromServer = restServerHelper.getListOfSubjects(projectName);
            sessionMapFromServer = restServerHelper.getListOfSessions(projectName);

        } catch (IOException e) {
            throw new IOException("Uploading could not be performed. The subject and session map could not be obtained due to the following error: " + e.getMessage(), e);
        }

        boolean uploadSuccess = true;

        for (final Session session : sessions) {

            final String patientId = session.getPatientId();
            final String studyUid = session.getStudyUid();
            final String seriesUid = session.getSeriesUid();

            final String subjectName = getSubjectName(patientId, subjectMapFromServer);
            final String sessionName = getSessionName(studyUid, sessionMapFromServer);
            final String scanName = getScanName(seriesUid, sessionMapFromServer);

            final GiftCloudSessionParameters sessionParameters = new GiftCloudSessionParameters();
            sessionParameters.setAdminEmail("null@null.com");
            sessionParameters.setSessionLabel(sessionName);
            sessionParameters.setProtocol("");
            sessionParameters.setVisit("");
            sessionParameters.setScan(scanName);
            sessionParameters.setBaseUrl(new URL(giftCloudServerUrl));
            sessionParameters.setNumberOfThreads(1);
            sessionParameters.setUsedFixedSize(true);

            final Project project = new Project(projectName, restServerHelper);

            final LinkedList<SessionVariable> sessionVariables = Lists.newLinkedList(session.getVariables(project, session));
            sessionParameters.setSessionVariables(sessionVariables);


            final Optional<String> windowTitle = Optional.empty();
            final Optional<JSObject> jsContext = Optional.empty();

            Future<Boolean> upload = executorService.submit(new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        Boolean returnValue = session.uploadTo(projectName, subjectName, restServerHelper, sessionParameters, project, emptyProgress, windowTitle, jsContext, new SwingUploadFailureHandler(), reporter);
                        return returnValue;
                    } catch (CancellationException exception) {
                        // Cancellation is the only type of exception for which we don't attempt to upload any more files
                        throw exception;
                    } catch (Exception e) {
                        // ToDo: We should pass back the exception and catch it lower down
                        return false;
                    }

                }
            });

            try {
                uploadSuccess = uploadSuccess && upload.get();
            } catch (InterruptedException e) {
                final Throwable cause = e.getCause();
                throw new IOException("Uploading was interrupted due to the following error: " + cause.getMessage(), cause);
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                throw new IOException("Uploading failed due to the following error: " + cause.getMessage(), cause);
            }
        }

        return true;
    }




    public boolean appendToGiftCloud(Vector paths, final String projectName) throws IOException {


        SeriesImportFilterApplicatorRetriever filter;
        try {
            if (StringUtils.isEmpty(projectName)) {
                final Optional<String> emptyProject = Optional.empty();
                filter = new SeriesImportFilterApplicatorRetriever(restServerHelper, emptyProject);
            } else {
                filter = new SeriesImportFilterApplicatorRetriever(restServerHelper, Optional.of(projectName));
            }
        } catch (Exception exception) {
            throw new IOException("Error encountered retrieving series import filters", exception);
        }

        final SwingProgressMonitor progress = new SwingProgressMonitor(container, "Finding data files", "searching", 0, paths.size());

        final EmptyProgress emptyProgress = new EmptyProgress();

        final Vector<File> fileList = new Vector<File>();
        for (final Object path : paths) {
            fileList.add(new File((String) path));
        }


        final List<Session> sessions = new MasterTrawler(progress, fileList, filter).call();

        final ExecutorService executorService = Executors.newCachedThreadPool();

        // Get a list of subjects from the server
        Map<String, String> subjectMapFromServer;
        Map<String, String> sessionMapFromServer;
        try {
            subjectMapFromServer = restServerHelper.getListOfSubjects(projectName);
            sessionMapFromServer = restServerHelper.getListOfSessions(projectName);

        } catch (IOException e) {
            throw new IOException("Uploading could not be performed. The subject and session map could not be obtained due to the following error: " + e.getMessage(), e);
        }

        boolean uploadSuccess = true;

        for (final Session session : sessions) {

            final String patientId = session.getPatientId();
            final String studyUid = session.getStudyUid();
            final String seriesUid = session.getSeriesUid();

            final String subjectName = getSubjectName(patientId, subjectMapFromServer);
            final String sessionName = getSessionName(studyUid, sessionMapFromServer);
            final String scanName = getScanName(seriesUid, sessionMapFromServer);

            final GiftCloudSessionParameters sessionParameters = new GiftCloudSessionParameters();
            sessionParameters.setAdminEmail("null@null.com");
            sessionParameters.setSessionLabel(sessionName);
            sessionParameters.setProtocol("");
            sessionParameters.setVisit("");
            sessionParameters.setScan(scanName);
            sessionParameters.setBaseUrl(new URL(giftCloudServerUrl));
            sessionParameters.setNumberOfThreads(1);
            sessionParameters.setUsedFixedSize(true);

            final Project project = new Project(projectName, restServerHelper);

            final LinkedList<SessionVariable> sessionVariables = Lists.newLinkedList(session.getVariables(project, session));
            sessionParameters.setSessionVariables(sessionVariables);


            final Optional<String> windowTitle = Optional.empty();
            final Optional<JSObject> jsContext = Optional.empty();

            Future<Boolean> upload = executorService.submit(new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        Boolean returnValue = session.appendTo(projectName, subjectName, restServerHelper, sessionParameters, project, emptyProgress, windowTitle, jsContext, new SwingUploadFailureHandler(), reporter);
                        return returnValue;
                    } catch (CancellationException exception) {
                        // Cancellation is the only type of exception for which we don't attempt to upload any more files
                        throw exception;
                    } catch (Exception e) {
                        // ToDo: We should pass back the exception and catch it lower down
                        return false;
                    }

                }
            });

            try {
                uploadSuccess = uploadSuccess && upload.get();
            } catch (InterruptedException e) {
                final Throwable cause = e.getCause();
                throw new IOException("Uploading was interrupted due to the following error: " + cause.getMessage(), cause);
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                throw new IOException("Uploading failed due to the following error: " + cause.getMessage(), cause);
            }
        }

        return true;
    }

    public void resetCancellation() {
        restServerHelper.resetCancellation();
    }

    private String getSubjectName(final String patientId, final Map<String, String> serverSubjectMap) {

        if (StringUtils.isNotBlank(patientId) && subjectsAlreadyUploaded.containsKey(patientId)) {
            return subjectsAlreadyUploaded.get(patientId);
        }

        if (StringUtils.isNotBlank(patientId)) {
            return hashUid(patientId);
        }

        String candidateName;

        do {
            temporarySubjectNameNum++;
            candidateName = temporarySubjectNamePrefix + Long.toString(temporarySubjectNameNum);

        } while (serverSubjectMap.containsKey(candidateName) || subjectsAlreadyUploaded.containsKey(candidateName));

        subjectsAlreadyUploaded.put(patientId, candidateName);
        return candidateName;
    }


    private String getSessionName(final String studyUid, final Map<String, String> serverSessionMap) {

        // We can't upload more than one set of files to the same session using session.uploadTo, so we force a new session to be created

        if (StringUtils.isNotBlank(studyUid) && sessionsAlreadyUploaded.containsKey(studyUid)) {
            return sessionsAlreadyUploaded.get(studyUid);
        }

        if (StringUtils.isNotBlank(studyUid)) {
            return hashUid(studyUid);
        }

        String candidateSessionName;

        do {
            temporarySessionNameNum++;
            candidateSessionName = temporarySessionNamePrefix + Long.toString(temporarySessionNameNum);

        } while (serverSessionMap.containsKey(candidateSessionName) || sessionsAlreadyUploaded.containsKey(candidateSessionName));

        sessionsAlreadyUploaded.put(studyUid, candidateSessionName);
        return candidateSessionName;
    }

    private String getScanName(final String seriesUid, final Map<String, String> serverScanMap) {

        // We can't upload more than one set of files to the same scan using session.uploadTo, so we force a new session to be created

        if (StringUtils.isNotBlank(seriesUid) && scansAlreadyUploaded.containsKey(seriesUid)) {
            return scansAlreadyUploaded.get(seriesUid);
        }

        if (StringUtils.isNotBlank(seriesUid)) {
            return hashUid(seriesUid);
        }

        String candidateScanName;

        do {
            temporaryScanNameNum++;
            candidateScanName = temporaryScanNamePrefix + Long.toString(temporaryScanNameNum);

        } while (serverScanMap.containsKey(candidateScanName) || scansAlreadyUploaded.containsKey(candidateScanName));

        scansAlreadyUploaded.put(seriesUid, candidateScanName);
        return candidateScanName;
    }


    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated as per Rec. ITU-T X.667 | ISO/IEC 9834-8.
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    public static final String UUID_ROOT = "2.25";



    private String hashUid(final String inputUid) {
        if (StringUtils.isBlank(inputUid)) {
            return null;
        } else {
            String hashedUid = toUID(toUUID(inputUid));
            return hashedUid.replace('.', '_');
        }
    }

    /* (non-Javadoc)
 * @see org.dcm4che2.util.UIDUtils#doCreateUID(java.util.String)
 * @param uuid
 * @return UID derived from the provided UUID
 */
    private static String toUID(final UUID uuid) {
        final byte[] b17 = new byte[17];
        fill(b17, 1, uuid.getMostSignificantBits());
        fill(b17, 9, uuid.getLeastSignificantBits());
        return new StringBuilder(64).append(UUID_ROOT).append('.')
                .append(new BigInteger(b17)).toString();
    }

    /* (non-Javadoc)
     * @see org.dcm4che2.util.UIDUtils#fill(byte[], int, long)
     */
    private static void fill(byte[] bb, int off, long val) {
        for (int i = off, shift = 56; shift >= 0; i++, shift -= 8)
            bb[i] = (byte) (val >>> shift);
    }

    /**
     * Generates a Version 5 UUID from the provided string
     * @param s source string
     * @return Version 5 UUID
     */
    private static UUID toUUID(final String s) {
        return Generators.nameBasedGenerator().generate(s.getBytes());
    }
}
