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

    private final String giftCloudServerUrl;
    private final GiftCloudReporter reporter;
    private final RestServerHelper restServerHelper;
    private final URI giftCloudUri;

    public GiftCloudServer(final String giftCloudServerUrl, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) throws MalformedURLException {
        this.giftCloudServerUrl = giftCloudServerUrl;
        this.reporter = reporter;

        if (StringUtils.isBlank(giftCloudServerUrl)) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        try {
            giftCloudUri = new URI(giftCloudServerUrl);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudServerUrl + " is not a valid URL.");
        }

        final RestServer restServer = new RestServer(giftCloudProperties, giftCloudServerUrl, reporter);
        restServerHelper = new RestServerHelper(restServer, reporter);


    }

    public void tryAuthentication() throws IOException {
        restServerHelper.tryAuthentication();
    }

    public Vector<Object> getListOfProjects() throws IOException {
        return restServerHelper.getListOfProjects();
    }

    public void resetCancellation() {
        restServerHelper.resetCancellation();
    }

    public boolean matchesServer(final String giftCloudUrl) throws MalformedURLException {
        try {
            final URI uri = new URI(giftCloudUrl);
            return (uri.equals(giftCloudUri));
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudUrl + " is not a valid URL.");
        }
    }

    public RestServerHelper getRestServerHelper() {
        return restServerHelper;
    }

    public String getGiftCloudServerUrl() {
        return giftCloudServerUrl;
    }

    public Map<String,String> getListOfSubjects(final String projectName) throws IOException {
        return restServerHelper.getListOfSubjects(projectName);
    }

    public Map<String, String> getListOfSessions(final String projectName) throws IOException {
        return restServerHelper.getListOfSessions(projectName);
    }

    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException {
        return restServerHelper.getSitewideSeriesImportFilter();
    }

    public Optional<Map<String, String>> getProjectSeriesImportFilter(final String projectName) throws IOException {
        return restServerHelper.getProjectSeriesImportFilter(projectName);
    }

    public UploadResult uploadToStudy(final boolean append, List<FileCollection> fileCollections, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, GiftCloudReporter logger) {
        MultiZipSeriesUploader uploader = new MultiZipSeriesUploader(append, fileCollections, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, logger, this);

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
        return restServerHelper.closeSession(uri, sessionParameters, uploader.getFailures(), timeZone);
    }

    public void createPseudonymIfNotExisting(final String projectName, final String subjectName, final String hashedPatientId) throws IOException {
        restServerHelper.createPseudonymIfNotExisting(projectName, subjectName, hashedPatientId);
    }

    public Optional<String> getSubjectPseudonym(final String projectName, final String hashedPatientId) throws IOException {
        return restServerHelper.getSubjectPseudonym(projectName, hashedPatientId);
    }
}
