package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang.StringUtils;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

public class GiftCloudServer {

    private final String giftCloudServerUrlString;
    private final RestServer restServer;
    private final URI giftCloudUri;
    private final ProjectCache projectCache;

    public GiftCloudServer(final RestServerFactory restServerFactory, final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) throws MalformedURLException {
        this.giftCloudServerUrlString = giftCloudServerUrlString;

        if (StringUtils.isBlank(giftCloudServerUrlString)) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        try {
            giftCloudUri = new URI(giftCloudServerUrlString);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudServerUrlString + " is not a valid URL.");
        }

        restServer = restServerFactory.create(giftCloudServerUrlString, giftCloudProperties, reporter);
        projectCache = new ProjectCache(restServer);
    }

    public void tryAuthentication() throws IOException {
        restServer.tryAuthentication();
    }

    public Vector<String> getListOfProjects() throws IOException {
        return restServer.getListOfProjects();
    }

    public Project getProject(final String projectName) {
        return projectCache.getProject(projectName);
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

    public String getGiftCloudServerUrl() {
        return giftCloudServerUrlString;
    }

    public Map<String,String> getListOfSubjects(final String projectName) throws IOException {
        return restServer.getListOfSubjects(projectName);
    }

    public Map<String, String> getListOfSessions(final String projectName) throws IOException {
        return restServer.getListOfSessions(projectName);
    }

    public Map<String, String> getListOfScans(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException {
        return restServer.getListOfScans(projectName, subjectLabel, experimentLabel);
    }

    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException {
        return restServer.getSitewideSeriesImportFilter();
    }

    public Optional<Map<String, String>> getProjectSeriesImportFilter(final String projectName) throws IOException {
        return restServer.getProjectSeriesImportFilter(projectName);
    }

    public Set<String> uploadZipFile(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {
        return restServer.uploadZipFile(projectLabel, subjectLabel, sessionParameters, useFixedSizeStreaming, fileCollection, applicators);
    }

    public void appendZipFileToExistingScan(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {
        restServer.appendZipFileToExistingScan(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSizeStreaming, fileCollection, applicators);
    }

    public void createSubjectAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectName, final String hashedPatientId) throws IOException {
        restServer.createSubjectAliasIfNotExisting(projectName, subjectName, hashedPatientId);
    }

    public Optional<GiftCloudLabel.SubjectLabel> getSubjectLabel(final String projectName, final String hashedPatientId) throws IOException {
        return restServer.getSubjectLabel(projectName, hashedPatientId);
    }

    public void createExperimentAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentAlias, final String hashedStudyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        restServer.createExperimentAliasIfNotExisting(projectName, subjectLabel, experimentAlias, hashedStudyInstanceUid, xnatModalityParams);
    }

    public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid) throws IOException {
        return restServer.getExperimentLabel(projectName, subjectLabel, hashedStudyInstanceUid);
    }

    public void createScanAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentAlias, final GiftCloudLabel.ScanLabel scanLabel, final String hashedSeriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        restServer.createScanAliasIfNotExisting(projectName, subjectLabel, experimentAlias, scanLabel, hashedSeriesInstanceUid, xnatModalityParams);
    }

    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentAlias, final String hashedSeriesInstanceUid) throws IOException {
        return restServer.getScanLabel(projectName, subjectLabel, experimentAlias, hashedSeriesInstanceUid);
    }

}
