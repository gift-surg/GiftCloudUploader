package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.data.UploadFailureHandler;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MockRestServer implements RestServer {

    public MockRestServer(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final ConnectionFactory connectionFactory, final GiftCloudReporter reporter) {
    }

    @Override
    public void tryAuthentication() throws IOException {

    }

    @Override
    public Vector<Object> getListOfProjects() throws IOException {
        return null;
    }

    @Override
    public Map<String, String> getListOfSubjects(String projectName) throws IOException, JSONException {
        return null;
    }

    @Override
    public Map<String, String> getListOfSessions(String projectName) throws IOException, JSONException {
        return null;
    }

    @Override
    public Map<String, String> getListOfScans(String projectName, String subjectName, String sessionName) throws IOException, JSONException {
        return null;
    }

    @Override
    public Map<String, String> getListOfPseudonyms(String projectName) throws IOException, JSONException {
        return null;
    }

    @Override
    public Map<String, String> getListOfResources(String projectName, String subjectName, String sessionName, String scanName) throws IOException, JSONException {
        return null;
    }

    @Override
    public Optional<String> getSubjectPseudonym(String projectName, String ppid) throws IOException {
        return null;
    }

    @Override
    public Collection<?> getScriptStatus(String projectName) throws IOException {
        return null;
    }

    @Override
    public Collection<?> getScripts(String projectName) throws IOException {
        return null;
    }

    @Override
    public Optional<String> getSiteWideAnonScript() throws IOException {
        return null;
    }

    @Override
    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException, JSONException {
        return null;
    }

    @Override
    public Optional<Map<String, String>> getProjectSeriesImportFilter(String projectName) throws IOException, JSONException {
        return null;
    }

    @Override
    public String getPreArcCode(String projectName) throws Exception {
        return null;
    }

    @Override
    public Set<String> getProjectTracers(String projectName) throws Exception {
        return null;
    }

    @Override
    public Set<String> getSiteTracers() throws Exception {
        return null;
    }

    @Override
    public <ApplicatorT> ApplicatorT getApplicator(String projectName, ScriptApplicatorFactory<ApplicatorT> factory) throws Exception {
        return null;
    }

    @Override
    public String uploadSubject(String projectName, InputStream xmlStream) throws Exception {
        return null;
    }

    @Override
    public UploadResult uploadToEcat(FileCollection fileCollection, String projectLabel, String subjectLabel, SessionParameters sessionParameters, UploadFailureHandler failureHandler, TimeZone timeZone, GiftCloudReporter logger) {
        return null;
    }

    @Override
    public UploadResult closeSession(String uri, SessionParameters sessionParameters, Map<FileCollection, Throwable> failures, Optional<TimeZone> timeZone) {
        return null;
    }

    @Override
    public Set<String> uploadZipFile(String projectLabel, String subjectLabel, SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception {
        return null;
    }

    @Override
    public void createPseudonymIfNotExisting(String projectLabel, String subjectLabel, String pseudonym) throws IOException {

    }

    @Override
    public Set<String> appendZipFileToExistingScan(String projectLabel, String subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception {
        return null;
    }

    @Override
    public void uploadEcat(String projectLabel, String subjectLabel, SessionParameters sessionParameters, String timestamp, String timeZoneId, File file, int fileNumber) throws Exception {

    }

    @Override
    public void resetCancellation() {

    }
}
