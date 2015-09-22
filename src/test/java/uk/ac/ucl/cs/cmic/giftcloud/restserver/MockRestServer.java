package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.json.JSONException;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.IOException;
import java.util.*;

public class MockRestServer implements RestServer {

    private final ProjectMap projectMap = new ProjectMap();
    private final Map<String, GiftCloudLabel.ExperimentLabel> experimentMap = new HashMap<String, GiftCloudLabel.ExperimentLabel>();
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
    public Vector<String> getListOfProjects() throws IOException {
        final Set<String> projectList = projectMap.getProjectList();
        final Vector<String> projectVector = new Vector<String>();
        for (final String projectLabel : projectList) {
            projectVector.add(projectLabel);
        }
        return projectVector;
    }

    @Override
    public Map<String, String> getListOfSubjects(String projectName) throws IOException, JSONException {
        if (projectMap.projectExists(projectName)) {
            final Map<String, String> subjectMap = new HashMap<String, String>();
            final Set<GiftCloudLabel.SubjectLabel> subjectList = projectMap.get(projectName).getListOfSubjects();
            for (final GiftCloudLabel.SubjectLabel subject : subjectList) {
                subjectMap.put(subject.getStringLabel(), subject.getStringLabel());
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
    public Map<String, String> getListOfScans(String projectName, GiftCloudLabel.SubjectLabel subjectName, GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException, JSONException {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> getListOfPseudonyms(String projectName) throws IOException, JSONException {
        final Map<String,GiftCloudLabel.SubjectLabel> subjectMap = projectMap.get(projectName).getPseudonyms();
        final Map<String, String> listOfPseudonyms = new HashMap<String, String>();
        for (final Map.Entry<String,GiftCloudLabel.SubjectLabel> entry : subjectMap.entrySet()) {
            listOfPseudonyms.put(entry.getKey(), entry.getValue().getStringLabel());
        }
        return listOfPseudonyms;
    }

    @Override
    public Map<String, String> getListOfResources(String projectName, GiftCloudLabel.SubjectLabel subjectName, GiftCloudLabel.ExperimentLabel experimentLabel, GiftCloudLabel.ScanLabel scanLabel) throws IOException, JSONException {
        return new HashMap<String, String>();
    }

    @Override
    public Optional<GiftCloudLabel.SubjectLabel> getSubjectLabel(String projectName, String ppid) throws IOException {
        return projectMap.projectExists(projectName) ? projectMap.get(projectName).getPseudonym(ppid) : Optional.<GiftCloudLabel.SubjectLabel>empty();
    }

    @Override
    public Collection<String> getScriptStatus(String projectName) throws IOException {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getScripts(String projectName) throws IOException {
        return new ArrayList<String>();
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
    public Set<String> uploadZipFile(String projectLabel, GiftCloudLabel.SubjectLabel subjectLabel, SessionParameters sessionParameters, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {
        final Set<String> uids = new HashSet<String>();
        uids.add(UUID.randomUUID().toString());
        return uids;
    }

    @Override
    public void createSubjectAliasIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedPatientId) throws IOException {
        final ProjectMap.ProjectRecord projectRecord = projectMap.get(projectLabel);

        final ProjectMap.ProjectRecord.SubjectRecord subjectRecord = projectRecord.get(subjectLabel);
        projectRecord.addPseudonym(subjectLabel, hashedPatientId);
    }

    @Override
    public void appendZipFileToExistingScan(String projectLabel, GiftCloudLabel.SubjectLabel subjectLabel, SessionParameters sessionParameters, XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {
        System.out.println("Appending file for " + fileCollection);
        Thread.sleep(10);
    }

    @Override
    public void resetCancellation() {

    }

    @Override
    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, GiftCloudLabel.ExperimentLabel experimentLabel, String hashedSeriesInstanceUid) throws IOException {
        if (!scanMap.containsKey(hashedSeriesInstanceUid)) {
            return Optional.empty();
        } else {
            return Optional.of(scanMap.get(hashedSeriesInstanceUid));
        }
    }

    @Override
    public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, String hashedStudyInstanceUid) throws IOException {
        if (!experimentMap.containsKey(hashedStudyInstanceUid)) {
            return Optional.empty();
        } else {
            return Optional.of(experimentMap.get(hashedStudyInstanceUid));
        }
    }

    @Override
    public void createExperimentAliasIfNotExisting(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, GiftCloudLabel.ExperimentLabel experimentLabel, String hashedStudyInstanceUid, XnatModalityParams xnatModalityParams) throws IOException {
        experimentMap.put(hashedStudyInstanceUid, experimentLabel);
    }

    @Override
    public void createScanAliasIfNotExisting(String projectName, GiftCloudLabel.SubjectLabel subjectLabel, GiftCloudLabel.ExperimentLabel experimentLabel, GiftCloudLabel.ScanLabel scanAlias, String hashedSeriesInstanceUid, XnatModalityParams xnatModalityParams) throws IOException {
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
            private final Map<GiftCloudLabel.SubjectLabel, SubjectRecord> subjectMap = new HashMap<GiftCloudLabel.SubjectLabel, SubjectRecord>();
            private final Map<String, GiftCloudLabel.SubjectLabel> pseudonyms = new HashMap<String, GiftCloudLabel.SubjectLabel>();

            ProjectRecord(final String projectLabel) {
                this.projectLabel = projectLabel;
            }

            public SubjectRecord get(final GiftCloudLabel.SubjectLabel subjectLabel) {
                return subjectMap.get(subjectLabel);
            }

            public Map<String,GiftCloudLabel.SubjectLabel> getPseudonyms() {
                return pseudonyms;
            }

            public void addPseudonym(final GiftCloudLabel.SubjectLabel subjectLabel, final String pseudonym) {
                pseudonyms.put(pseudonym, subjectLabel);
            }

            public Optional<GiftCloudLabel.SubjectLabel> getPseudonym(final String ppid) {
                if (pseudonyms.containsKey(ppid)) {
                    return Optional.of(pseudonyms.get(ppid));
                } else {
                    return Optional.empty();
                }
            }

            public Set<GiftCloudLabel.SubjectLabel> getListOfSubjects() {
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
