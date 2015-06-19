package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.AliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class for exporting a subject list to a Json file
 */
public class JsonWriter extends PatientListWriter {
    private final JSONObject mainObj;
    private final JSONArray projectList;

    private static final String PATIENT_LIST_FILENAME = "GiftCloudPatientList.json";
    private static final String BACKUP_PATIENT_LIST_FILENAME = "GiftCloudPatientList.backup.json";
    private static final String BACKUP_PATIENT_LIST_FILENAME_PREFIX = "BackupGiftCloudPatientList";
    private static final String BACKUP_PATIENT_LIST_FILENAME_SUFFIX = "json";
    private static final String PATIENT_NAME_STRING = "PatientName";
    private static final String PATIENT_ID_STRING = "PatientId";
    private static final String PATIENT_ALIAS_STRING = "GiftCloudAlias";
    private static final String PATIENT_PPID_STRING = "GiftCloudPpid";
    private static final String PROJECT_NAME_STRING = "ProjectName";
    private static final String ALIAS_LIST_STRING = "AliasList";
    private static final String PROJECT_LIST_STRING = "ProjectList";

    /**
     * Construct a JsonWriter object
     *
     * @param patientListFolder the folder to which the patient list will be stored
     * @param reporter for error and progress reporting
     */
    public JsonWriter(final File patientListFolder, final GiftCloudReporter reporter) {
        super(patientListFolder, reporter);
        mainObj = new JSONObject();
        projectList = new JSONArray();
        mainObj.put(PROJECT_LIST_STRING, projectList);
    }

    @Override
    protected void saveFile(final File file) throws IOException {
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(mainObj.toJSONString());
        fileWriter.flush();
        fileWriter.close();
    }

    @Override
    protected PatientListForProject createNewPatientList(final String projectName) {
        return new JsonPatientListForProject(projectList, projectName);
    }

    /**
     * Reads in a project list from a Json file
     *
     * @param patientListFolder the folder from which the patient list will be loaded
     * @param reporter for error and progress reporting
     * @return a map of project names to AliasMaps
     */
    public static Map<String, AliasMap> readProjectMap(final File patientListFolder, final GiftCloudReporter reporter) {
        final Map<String, AliasMap> projectMap = new HashMap<String, AliasMap>();
        JSONParser parser = new JSONParser();

        try {
            final File patientListFile = new File(patientListFolder, PATIENT_LIST_FILENAME);
            if (!patientListFile.exists()) {
                return projectMap;
            }
            final Object obj = parser.parse(new FileReader(patientListFile));

            final JSONObject jsonObject = (JSONObject)obj;
            final JSONArray projectList = (JSONArray)jsonObject.get(PROJECT_LIST_STRING);

            Iterator<JSONObject> iterator = projectList.iterator();
            while (iterator.hasNext()) {

                AliasMap aliasMap = new AliasMap();

                final JSONObject projectEntry = iterator.next();
                final String projectName = (String)projectEntry.get(PROJECT_NAME_STRING);
                final JSONArray aliasList = (JSONArray)projectEntry.get(ALIAS_LIST_STRING);

                Iterator<JSONObject> listIterator = aliasList.iterator();
                while (listIterator.hasNext()) {
                    final JSONObject aliasEntry = listIterator.next();
                    final String patientName = (String)aliasEntry.get(PATIENT_NAME_STRING);
                    final String patientId = (String)aliasEntry.get(PATIENT_ID_STRING);
                    final String patientAlias = (String)aliasEntry.get(PATIENT_ALIAS_STRING);
                    final String patientPpid = (String)aliasEntry.get(PATIENT_PPID_STRING);
                    aliasMap.addAlias(patientPpid, patientAlias, patientId, patientName);
                }

                projectMap.put(projectName, aliasMap);

            }


        } catch (FileNotFoundException e) {
            reporter.silentLogException(e, "Unable to load Json patient list file due to the following error:" + e.getLocalizedMessage() );
        } catch (IOException e) {
            reporter.silentLogException(e, "Unable to load Json patient list file due to the following error:" + e.getLocalizedMessage());
        } catch (ParseException e) {
            reporter.silentLogException(e, "Unable to load Json patient list file due to the following error:" + e.getLocalizedMessage());
        }
        return projectMap;
    }

    @Override
    protected String getPatientListFilename() {
        return PATIENT_LIST_FILENAME;
    }

    @Override
    protected String getBackupPatientListFilename() {
        return BACKUP_PATIENT_LIST_FILENAME;
    }

    @Override
    protected String getBackupPatientListFilenamePrefix() {
        return BACKUP_PATIENT_LIST_FILENAME_PREFIX;
    }

    @Override
    protected String getBackupPatientListFilenameSuffix() {
        return BACKUP_PATIENT_LIST_FILENAME_SUFFIX;
    }

    private static class JsonPatientListForProject extends PatientListForProject {

        private final JSONArray aliasListForProject;

        public JsonPatientListForProject(final JSONArray projectList, final String projectName) {
            final JSONObject projectEntry = new JSONObject();
            aliasListForProject = new JSONArray();

            projectEntry.put(PROJECT_NAME_STRING, projectName);
            projectEntry.put(ALIAS_LIST_STRING, aliasListForProject);
            projectList.add(projectEntry);
        }

        public void addEntry(final String hashedPatientId, final String alias, final String patientId, final String patientName) {
            final JSONObject record = new JSONObject();
            record.put(PATIENT_NAME_STRING, patientName);
            record.put(PATIENT_ID_STRING, patientId);
            record.put(PATIENT_ALIAS_STRING, alias);
            record.put(PATIENT_PPID_STRING, hashedPatientId);
            aliasListForProject.add(record);
        }
    }
}
