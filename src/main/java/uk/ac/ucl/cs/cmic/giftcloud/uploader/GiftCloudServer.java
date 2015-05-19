package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang.StringUtils;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class GiftCloudServer {

    private final String giftCloudServerUrlString;
    private final GiftCloudReporter reporter;
    private final RestServer restServer;
    private final URI giftCloudUri;

    public GiftCloudServer(final RestServerFactory restServerFactory, final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) throws MalformedURLException {
        this.giftCloudServerUrlString = giftCloudServerUrlString;
        this.reporter = reporter;

        if (StringUtils.isBlank(giftCloudServerUrlString)) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        try {
            giftCloudUri = new URI(giftCloudServerUrlString);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudServerUrlString + " is not a valid URL.");
        }

        restServer = restServerFactory.create(giftCloudServerUrlString, giftCloudProperties, reporter);
    }

    public void tryAuthentication() throws IOException {
        restServer.tryAuthentication();
    }

    public Vector<Object> getListOfProjects() throws IOException {
        return restServer.getListOfProjects();
    }

    public void resetCancellation() {
        restServer.resetCancellation();
    }

    public boolean matchesServer(final String giftCloudUrl) throws MalformedURLException {
        try {
            final URI uri = new URI(giftCloudUrl);
            return (uri.equals(giftCloudUri));
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudUrl + " is not a valid URL.");
        }
    }

    public RestServer getRestServer() {
        return restServer;
    }

    public String getGiftCloudServerUrl() {
        return giftCloudServerUrlString;
    }

    public Map<String,String> getListOfSubjects(final String projectName) throws IOException {
        return restServer.getListOfSubjects(projectName);
    }

    public Map<String, String> getListOfSessions(final String projectName) throws IOException {
        return restServer.getListOfSessions(projectName);
    }

    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException {
        return restServer.getSitewideSeriesImportFilter();
    }

    public Optional<Map<String, String>> getProjectSeriesImportFilter(final String projectName) throws IOException {
        return restServer.getProjectSeriesImportFilter(projectName);
    }

    /**
     * Legacy method for uploading using the applet wizard
     *
     * @param fileCollections
     * @param xnatModalityParams
     * @param applicators
     * @param projectLabel
     * @param subjectLabel
     * @param sessionParameters
     * @param logger
     * @return
     */
    public UploadResult uploadToStudy(List<FileCollection> fileCollections, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, GiftCloudReporter logger) {
        final int nThreads = sessionParameters.getNumberOfThreads();
        final BackgroundCompletionServiceTaskList uploaderTaskList = new BackgroundCompletionServiceTaskList<Set<String>, FileCollection>(nThreads);
        MultiZipSeriesUploader uploader = new MultiZipSeriesUploader(uploaderTaskList, false, fileCollections, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, logger, this);
        final CallableUploader.CallableUploaderFactory callableUploaderFactory = ZipSeriesUploaderFactorySelector.getZipSeriesUploaderFactory(true);
        final UploadStatisticsReporter stats = new UploadStatisticsReporter(reporter);
        for (final FileCollection s : fileCollections) {
            stats.addToSend(s.getSize());
            uploader.addFile(this, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, callableUploaderFactory, stats, s);
        }

        final Optional<String> failureMessage = uploader.run(logger);
        if (failureMessage.isPresent()) {
            return new UploadResultsFailure(failureMessage.get());
        }

        Set<String> uris = uploader.getUris();

        if (1 != uris.size()) {
            logger.error("Server reports unexpected sessionLabel count:" + uris.size() + " : " + uris);
            logger.updateStatusText("<p>The XNAT server reported receiving an unexpected number of sessions: (" + uris.size() + ")</p>" + "<p>Please contact the system manager for help.</p>");
            return new UploadResultsFailure("<p>The XNAT server reported receiving an unexpected number of sessions: (" + uris.size() + ")</p>" + "<p>Please contact the system manager for help.</p>");
        }

        final String uri = uris.iterator().next();
        final Optional<TimeZone> timeZone = Optional.empty();
        return restServer.closeSession(uri, sessionParameters, uploader.getFailures(), timeZone);
    }

    public Set<String> uploadZipFile(String projectLabel, String subjectLabel, SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception {
        return restServer.uploadZipFile(projectLabel, subjectLabel, sessionParameters, useFixedSizeStreaming, fileCollection, applicators, progress);
    }

    public void appendZipFileToExistingScan(String projectLabel, String subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception {
        restServer.appendZipFileToExistingScan(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSizeStreaming, fileCollection, applicators, progress);
    }

    public void createPseudonymIfNotExisting(final String projectName, final String subjectName, final String hashedPatientId) throws IOException {
        restServer.createPseudonymIfNotExisting(projectName, subjectName, hashedPatientId);
    }

    public Optional<String> getSubjectPseudonym(final String projectName, final String hashedPatientId) throws IOException {
        return restServer.getSubjectPseudonym(projectName, hashedPatientId);
    }
}
