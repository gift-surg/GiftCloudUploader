package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.display.EmptyProgress;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;

public class GiftCloudServer implements BackgroundUploader.BackgroundUploadOutcomeCallback {

    private final String giftCloudServerUrl;
    private final PendingUploadTaskList pendingUploadTaskList;
    private final MultiUploadReporter reporter;
    private final RestServerHelper restServerHelper;
    private final Container container;
    private final BackgroundUploader backgroundUploader;
    private final URI giftCloudUri;

    public GiftCloudServer(final String giftCloudServerUrl, final Container container, final GiftCloudProperties giftCloudProperties, final PendingUploadTaskList pendingUploadTaskList, final MultiUploadReporter reporter) throws MalformedURLException {
        this.giftCloudServerUrl = giftCloudServerUrl;
        this.pendingUploadTaskList = pendingUploadTaskList;
        this.reporter = reporter;
        this.container = container;

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

        final EmptyProgress emptyProgress = new EmptyProgress();

        final int numThreads = 1;

        backgroundUploader = new BackgroundUploader(new BackgroundCompletionServiceTaskList<Callable<Set<String>>>(numThreads), restServerHelper, emptyProgress, this, reporter);
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

    @Override
    public void notifySuccess(final FileCollection fileCollection) {
        pendingUploadTaskList.notifySuccess(fileCollection);
    }

    @Override
    public void notifyFailure(final FileCollection fileCollection) {
        pendingUploadTaskList.notifyFailure(fileCollection);
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

    public boolean uploadToStudy(List<FileCollection> fileCollections, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, ResultProgressHandle progress, Optional<String> windowName, Optional<JSObject> jsContext, MultiUploadReporter logger) {
        return restServerHelper.uploadToStudy(fileCollections, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, progress, windowName, jsContext, logger);
    }

    public boolean appendToStudy(List<FileCollection> fileCollections, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, ResultProgressHandle progress, Optional<String> windowName, Optional<JSObject> jsContext, MultiUploadReporter logger) {
        return restServerHelper.appendToStudy(fileCollections, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, progress, windowName, jsContext, logger);
    }

    public void createPseudonymIfNotExisting(final String projectName, final String subjectName, final String hashedPatientId) throws IOException {
        restServerHelper.createPseudonymIfNotExisting(projectName, subjectName, hashedPatientId);
    }

    public Optional<String> getSubjectPseudonym(final String projectName, final String hashedPatientId) throws IOException {
        return restServerHelper.getSubjectPseudonym(projectName, hashedPatientId);
    }
}
