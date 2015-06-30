package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public interface RestServer {
    void tryAuthentication() throws IOException;

    Vector<Object> getListOfProjects() throws IOException;

    Map<String, String> getListOfSubjects(String projectName) throws IOException, JSONException;

    Map<String, String> getListOfSessions(String projectName) throws IOException, JSONException;

    Map<String, String> getListOfScans(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException, JSONException;

    Map<String, String> getListOfPseudonyms(String projectName) throws IOException, JSONException;

    Map<String, String> getListOfResources(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, GiftCloudLabel.ExperimentLabel experimentLabel, GiftCloudLabel.ScanLabel scanLabel) throws IOException, JSONException;

    Optional<GiftCloudLabel.SubjectLabel> getSubjectLabel(String projectName, String ppid) throws IOException;

    Collection<?> getScriptStatus(String projectName) throws IOException;

    Collection<?> getScripts(String projectName) throws IOException;

    Optional<String> getSiteWideAnonScript() throws IOException;

    Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException, JSONException;

    Optional<Map<String, String>> getProjectSeriesImportFilter(String projectName) throws IOException, JSONException;

    String getPreArcCode(String projectName) throws Exception;

    Set<String> getProjectTracers(String projectName) throws Exception;

    Set<String> getSiteTracers() throws Exception;

    String uploadSubject(String projectName, InputStream xmlStream) throws Exception;

    UploadResult closeSession(String uri, SessionParameters sessionParameters, Map<FileCollection, Throwable> failures, Optional<TimeZone> timeZone);

    Set<String> uploadZipFile(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception;

    void createSubjectAliasIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedPatientId) throws IOException;

    void appendZipFileToExistingScan(String projectLabel, GiftCloudLabel.SubjectLabel subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception;

    void resetCancellation();

    Optional<GiftCloudLabel.ScanLabel> getScanLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid) throws IOException;

    Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid) throws IOException;

    void createExperimentAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedStudyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException;

    void createScanAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final String hashedSeriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException;
}
