package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MockRestServer implements RestServer {

    private final ProjectMap projectMap = new ProjectMap();
    private final Map<String, String> experimentMap = new HashMap<String, String>();
    private final Map<String, GiftCloudLabel.ScanLabel> scanMap = new HashMap<String, GiftCloudLabel.ScanLabel>();

    public MockRestServer(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final ConnectionFactory connectionFactory, final GiftCloudReporter reporter) {
    }

    public void addTestProject(final String projectName) {
        projectMap.add(projectName);
    }

    @Override
    public void tryAuthentication() throws IOException {

    }

    @Override
    public Vector<Object> getListOfProjects() throws IOException {
        final Set<String> projectList = projectMap.getProjectList();
        final Vector<Object> projectVector = new Vector<Object>();
        for (final String projectLabel : projectList) {
            projectVector.add(projectLabel);
        }
        return projectVector;
    }

    @Override
    public Map<String, String> getListOfSubjects(String projectName) throws IOException, JSONException {
        if (projectMap.projectExists(projectName)) {
            final Map<String, String> subjectMap = new HashMap<String, String>();
            final Set<String> subjectList = projectMap.get(projectName).getListOfSubjects();
            for (final String subject : subjectList) {
                subjectMap.put(subject, subject);
            }
            return subjectMap;

        } else {
            throw new GiftCloudHttpException(404, "Project not found", "", "");
        }
    }

    @Override
    public Map<String, String> getListOfSessions(String projectName) throws IOException, JSONException {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> getListOfScans(String projectName, String subjectName, String sessionName) throws IOException, JSONException {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> getListOfPseudonyms(String projectName) throws IOException, JSONException {
        return projectMap.get(projectName).getPseudonyms();
    }

    @Override
    public Map<String, String> getListOfResources(String projectName, String subjectName, String sessionName, String scanName) throws IOException, JSONException {
        return new HashMap<String, String>();
    }

    @Override
    public Optional<String> getSubjectLabel(String projectName, String ppid) throws IOException {
        return projectMap.projectExists(projectName) ? projectMap.get(projectName).getPseudonym(ppid) : Optional.<String>empty();
    }

    @Override
    public Collection<?> getScriptStatus(String projectName) throws IOException {
        return new ArrayList<Object>();
    }

    @Override
    public Collection<?> getScripts(String projectName) throws IOException {
        return new ArrayList<Object>();
    }

    @Override
    public Optional<String> getSiteWideAnonScript() throws IOException {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException, JSONException {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, String>> getProjectSeriesImportFilter(String projectName) throws IOException, JSONException {
        return Optional.empty();
    }

    @Override
    public String getPreArcCode(String projectName) throws Exception {
        return "";
    }

    @Override
    public Set<String> getProjectTracers(String projectName) throws Exception {
        return new HashSet<String>();
    }

    @Override
    public Set<String> getSiteTracers() throws Exception {
        return new HashSet<String>();
    }

    @Override
    public String uploadSubject(String projectName, InputStream xmlStream) throws Exception {
        return UUID.randomUUID().toString();
    }

    @Override
    public UploadResult closeSession(String uri, SessionParameters sessionParameters, Map<FileCollection, Throwable> failures, Optional<TimeZone> timeZone) {
        return new UploadResult(true);
    }

    @Override
    public Set<String> uploadZipFile(String projectLabel, String subjectLabel, SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {
        final Set<String> uids = new HashSet<String>();
        uids.add(UUID.randomUUID().toString());
        return uids;
    }

    @Override
    public void createSubjectAliasIfNotExisting(final String projectLabel, final String subjectLabel, final String pseudonym) throws IOException {
        final ProjectMap.ProjectRecord projectRecord = projectMap.get(projectLabel);

        final ProjectMap.ProjectRecord.SubjectRecord subjectRecord = projectRecord.get(subjectLabel);
        projectRecord.addPseudonym(subjectLabel, pseudonym);
    }

    @Override
    public void appendZipFileToExistingScan(String projectLabel, String subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {
        System.out.println("Appending file for " + fileCollection);
        Thread.sleep(10);
    }

    @Override
    public void resetCancellation() {

    }

    @Override
    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(String projectName, String subjectAlias, String experimentAlias, String hashedSeriesInstanceUid) throws IOException {
        if (!scanMap.containsKey(hashedSeriesInstanceUid)) {
            return Optional.empty();
        } else {
            return Optional.of(scanMap.get(hashedSeriesInstanceUid));
        }
    }

    @Override
    public Optional<String> getExperimentLabel(String projectName, String subjectAlias, String hashedStudyInstanceUid) throws IOException {
        if (!experimentMap.containsKey(hashedStudyInstanceUid)) {
            return Optional.empty();
        } else {
            return Optional.of(experimentMap.get(hashedStudyInstanceUid));
        }
    }

    @Override
    public void createExperimentAliasIfNotExisting(String projectName, String subjectAlias, String experimentAlias, String hashedStudyInstanceUid, XnatModalityParams xnatModalityParams) throws IOException {
        experimentMap.put(hashedStudyInstanceUid, experimentAlias);
    }

    @Override
    public void createScanAliasIfNotExisting(String projectName, String subjectAlias, String experimentAlias, GiftCloudLabel.ScanLabel scanAlias, String hashedSeriesInstanceUid, XnatModalityParams xnatModalityParams) throws IOException {
        scanMap.put(hashedSeriesInstanceUid, scanAlias);
    }

    class ProjectMap {

        private final Map<String, ProjectRecord> projectMap = new HashMap<String, ProjectRecord>();

        final Set<String> getProjectList() {
            return projectMap.keySet();
        }

        public ProjectRecord get(final String projectLabel) {
            return projectMap.get(projectLabel);
        }

        public void add(final String projectName) {
            projectMap.put(projectName, new ProjectRecord(projectName));
        }

        public boolean projectExists(final String projectLabel) {
            return projectMap.containsKey(projectLabel);
        }

        class ProjectRecord {
            private String projectLabel;
            private final Map<String, SubjectRecord> subjectMap = new HashMap<String, SubjectRecord>();
            private final Map<String, String> pseudonyms = new HashMap<String, String>();

            ProjectRecord(final String projectLabel) {
                this.projectLabel = projectLabel;
            }

            public SubjectRecord get(final String subjectLabel) {
                return subjectMap.get(subjectLabel);
            }

            public Map<String,String> getPseudonyms() {
                return pseudonyms;
            }

            public void addPseudonym(final String subjectLabel, final String pseudonym) {
                pseudonyms.put(pseudonym, subjectLabel);
            }

            public Optional<String> getPseudonym(final String ppid) {
                if (pseudonyms.containsKey(ppid)) {
                    return Optional.of(pseudonyms.get(ppid));
                } else {
                    return Optional.empty();
                }
            }

            public Set<String> getListOfSubjects() {
                return subjectMap.keySet();
            }

            class SubjectRecord {
                private String subjectLabel;

                SubjectRecord(final String subjectLabel) {
                    this.subjectLabel = subjectLabel;
                }

            }

        }

    }


}
