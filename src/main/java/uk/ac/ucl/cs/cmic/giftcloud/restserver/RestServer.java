package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public interface RestServer {
    void tryAuthentication() throws IOException;

    Vector<String> getListOfProjects() throws IOException;

    Map<String, String> getListOfSubjects(String projectName) throws IOException, JSONException;

    Map<String, String> getListOfSessions(String projectName) throws IOException, JSONException;

    Map<String, String> getListOfScans(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException, JSONException;

    Map<String, String> getListOfPseudonyms(String projectName) throws IOException, JSONException;

    Map<String, String> getListOfResources(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, GiftCloudLabel.ExperimentLabel experimentLabel, GiftCloudLabel.ScanLabel scanLabel) throws IOException, JSONException;

    Optional<GiftCloudLabel.SubjectLabel> getSubjectLabel(String projectName, String ppid) throws IOException;

    Collection<String> getScriptStatus(String projectName) throws IOException;

    Collection<String> getScripts(String projectName) throws IOException;

    Optional<String> getSiteWideAnonScript() throws IOException;

    Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException, JSONException;

    Optional<Map<String, String>> getProjectSeriesImportFilter(String projectName) throws IOException, JSONException;

    String getPreArcCode(String projectName) throws Exception;

    Set<String> uploadZipFile(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final File temporaryFile) throws Exception;

    void createSubjectAliasIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedPatientId) throws IOException;

    void appendZipFileToExistingScan(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final XnatModalityParams xnatModalityParams, final File temporaryFile) throws Exception;

    void resetCancellation();

    Optional<GiftCloudLabel.ScanLabel> getScanLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid) throws IOException;

    Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid) throws IOException;

    void createExperimentAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedStudyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException;

    void createScanAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final String hashedSeriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException;
}
