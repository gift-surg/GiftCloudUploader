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

    Map<String, String> getListOfScans(String projectName, String subjectName, String sessionName) throws IOException, JSONException;

    Map<String, String> getListOfPseudonyms(String projectName) throws IOException, JSONException;

    Map<String, String> getListOfResources(String projectName, String subjectName, String sessionName, String scanName) throws IOException, JSONException;

    Optional<String> getSubjectPseudonym(String projectName, String ppid) throws IOException;

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

    Set<String> uploadZipFile(String projectLabel, String subjectLabel, SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception;

    void createPseudonymIfNotExisting(String projectLabel, String subjectLabel, String pseudonym) throws IOException;

    void appendZipFileToExistingScan(String projectLabel, String subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception;

    void resetCancellation();

    Optional<String> getScanPseudonym(final String projectName, final String subjectAlias, final String experimentAlias, final String hashedSeriesInstanceUid) throws IOException;

    Optional<String> getExperimentPseudonym(final String projectName, final String subjectAlias, final String hashedStudyInstanceUid) throws IOException;

    void createExperimentPseudonymIfNotExisting(final String projectName, final String subjectAlias, final String experimentAlias, final String hashedStudyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException;

    void createScanPseudonymIfNotExisting(final String projectName, final String subjectAlias, final String experimentAlias, final String scanAlias, final String hashedSeriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException;
}
