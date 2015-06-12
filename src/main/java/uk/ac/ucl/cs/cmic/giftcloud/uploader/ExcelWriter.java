package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.AliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class for exporting a patient map to an Excel spreadsheet
 */
public class ExcelWriter {
    private final HSSFWorkbook workbook;
    private final Map<String, ProjectSheet> sheets = new HashMap<String, ProjectSheet>();
    private final File patientListFolder;
    private GiftCloudReporter reporter;
    private static final String PATIENT_LIST_FILENAME = "PatientList.xls";

    private static final String PATIENT_NAME_STRING = "Patient Name";
    private static final String PATIENT_ID_STRING = "Patient ID";
    private static final String PATIENT_ALIAS_STRING = "GIFT-Cloud Alias";
    private static final String PATIENT_PPID_STRING = "GIFT-Cloud pseudonymised patient ID";

    private final Set<String> knownPatientIds = new HashSet<String>();


    public ExcelWriter(final GiftCloudReporter reporter) {
        this.patientListFolder = MultiUploaderUtils.createOrGetPatientListFolder(reporter);
        this.reporter = reporter;
        workbook = new HSSFWorkbook();
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

    /**
     * Writes out the Excel file from the current subject information
     */
    public void writeExcelFile() {

        try {
            FileOutputStream out = new FileOutputStream(new File(patientListFolder, PATIENT_LIST_FILENAME));
            workbook.write(out);
            out.close();

        } catch (FileNotFoundException e) {
            reporter.silentLogException(e, "Failed to write Excel file of patient information due to the following error: " + e.getLocalizedMessage());
        } catch (IOException e) {
            reporter.silentLogException(e, "Failed to write Excel file of patient information due to the following error: " + e.getLocalizedMessage());
        }

    }

    /**
     * Adds subject information from a project map
     *
     * @param projectMap a map of project name to AliasMap containing subject information
     */
    public void writeProjectMap(Map<String, AliasMap> projectMap) {
        for (Map.Entry<String, AliasMap> entry : projectMap.entrySet()) {
            final String projectName = entry.getKey();
            Map<String, AliasMap.AliasRecord> aliasMap = entry.getValue().getMap();
            for (AliasMap.AliasRecord aliasRecord : aliasMap.values()) {
                addEntry(projectName, aliasRecord.getPpid(), aliasRecord.getAlias(), aliasRecord.getPatientId(), aliasRecord.getPatientName());
            }
        }
    }

    private ProjectSheet getSheet(final String projectName) {
        if (!sheets.containsKey(projectName)) {
            sheets.put(projectName, new ProjectSheet(workbook, projectName));
            addEntry(projectName, PATIENT_PPID_STRING, PATIENT_ALIAS_STRING, PATIENT_ID_STRING, PATIENT_NAME_STRING);
        }
        return sheets.get(projectName);
    }

    private static class ProjectSheet {
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
