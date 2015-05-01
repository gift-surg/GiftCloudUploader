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

    <ApplicatorT> ApplicatorT getApplicator(String projectName, ScriptApplicatorFactory<ApplicatorT> factory) throws Exception;

    String uploadSubject(String projectName, InputStream xmlStream) throws Exception;

    UploadResult uploadToEcat(FileCollection fileCollection, String projectLabel, String subjectLabel, SessionParameters sessionParameters, UploadFailureHandler failureHandler, TimeZone timeZone, GiftCloudReporter logger);

    UploadResult closeSession(String uri, SessionParameters sessionParameters, Map<FileCollection, Throwable> failures, Optional<TimeZone> timeZone);

    Set<String> uploadZipFile(String projectLabel, String subjectLabel, SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception;

    void createPseudonymIfNotExisting(String projectLabel, String subjectLabel, String pseudonym) throws IOException;

    Set<String> appendZipFileToExistingScan(String projectLabel, String subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception;

    void uploadEcat(String projectLabel, String subjectLabel, SessionParameters sessionParameters, String timestamp, String timeZoneId, File file, int fileNumber) throws Exception;

    void resetCancellation();
}
