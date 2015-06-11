package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.AliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.*;
import java.util.*;

public class JsonWriter {
    private final HSSFWorkbook workbook;
    private final Map<String, ProjectSheet> sheets = new HashMap<String, ProjectSheet>();
    private final File patientListFolder;
    private GiftCloudReporter reporter;
    private static final String PATIENT_LIST_FILENAME = "GiftCloudPatientList.json";

    private static final String PATIENT_NAME_STRING = "PatientName";
    private static final String PATIENT_ID_STRING = "PatientId";
    private static final String PATIENT_ALIAS_STRING = "GiftCloudAlias";
    private static final String PATIENT_PPID_STRING = "GiftCloudPpid";

    private static final String PROJECT_NAME_STRING = "ProjectName";
    private static final String ALIAS_LIST_STRING = "AliasList";
    private static final String PROJECT_LIST_STRING = "ProjectList";

    private final Set<String> knownPatientIds = new HashSet<String>();

    private final JSONObject mainObj;


    public JsonWriter(final GiftCloudReporter reporter) {
        this.patientListFolder = MultiUploaderUtils.createOrGetPatientListFolder(reporter);
        this.reporter = reporter;
        workbook = new HSSFWorkbook();
        mainObj = new JSONObject();
    }

    private void addEntry(final String projectName, final String hashedPatientId, final String alias, final String patientId, final String patientName) {
        if (!knownPatientIds.contains(patientId)) {
            knownPatientIds.add(patientId);

            final ProjectSheet sheet = getSheet(projectName);
            final Row row = sheet.createRow();
            int cellNum = 0;
            final Cell patientNameCell = row.createCell(cellNum++);
            final Cell patientIdCell = row.createCell(cellNum++);
            final Cell patientAliasCell = row.createCell(cellNum++);
            final Cell patientPpidCell = row.createCell(cellNum++);
            patientNameCell.setCellValue(patientName);
            patientIdCell.setCellValue(patientId);
            patientAliasCell.setCellValue(alias);
            patientPpidCell.setCellValue(hashedPatientId);
        }
    }

    public void writeJsonFile() {

        try {
            FileWriter file = new FileWriter(new File(patientListFolder, PATIENT_LIST_FILENAME));
            file.write(mainObj.toJSONString());
            file.flush();
            file.close();

        } catch (FileNotFoundException e) {
            reporter.silentLogException(e, "Failed to write Excel file of patient information due to the following error: " + e.getLocalizedMessage());
        } catch (IOException e) {
            reporter.silentLogException(e, "Failed to write Excel file of patient information due to the following error: " + e.getLocalizedMessage());
        }

    }

    public void writeProjectMap(final Map<String, AliasMap> projectMap) {
        JSONArray projectList = new JSONArray();

        for (Map.Entry<String, AliasMap> entry : projectMap.entrySet()) {
            JSONObject projectEntry = new JSONObject();
            JSONArray aliasListForProject = new JSONArray();

            final String projectName = entry.getKey();
            Map<String, AliasMap.AliasRecord> aliasMap = entry.getValue().getMap();

            for (AliasMap.AliasRecord aliasRecord : aliasMap.values()) {
                final JSONObject record = new JSONObject();
                record.put(PATIENT_NAME_STRING, aliasRecord.getPatientName());
                record.put(PATIENT_ID_STRING, aliasRecord.getPatientId());
                record.put(PATIENT_ALIAS_STRING, aliasRecord.getAlias());
                record.put(PATIENT_PPID_STRING, aliasRecord.getPpid());
                aliasListForProject.add(record);
            }

            projectEntry.put(PROJECT_NAME_STRING, projectName);
            projectEntry.put(ALIAS_LIST_STRING, aliasListForProject);
            projectList.add(projectEntry);
        }
        mainObj.put(PROJECT_LIST_STRING, projectList);
    }

    public static Map<String, AliasMap> readProjectMap(GiftCloudReporter reporter) {
        final Map<String, AliasMap> projectMap = new HashMap<String, AliasMap>();
        JSONParser parser = new JSONParser();

        try {
            final File patientListFolder = MultiUploaderUtils.createOrGetPatientListFolder(reporter);
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


    private ProjectSheet getSheet(final String projectName) {
        if (!sheets.containsKey(projectName)) {
            sheets.put(projectName, new ProjectSheet(workbook, projectName));
            addEntry(projectName, PATIENT_PPID_STRING, PATIENT_ALIAS_STRING, PATIENT_ID_STRING, PATIENT_NAME_STRING);
        }
        return sheets.get(projectName);
    }

    public class ProjectSheet {
        private final HSSFSheet sheet;
        private int rowNum = 0;

        public ProjectSheet(final HSSFWorkbook workbook, final String projectName) {
            sheet = workbook.createSheet(projectName);
        }

        public Row createRow() {
            return sheet.createRow(rowNum++);
        }
    }
}
