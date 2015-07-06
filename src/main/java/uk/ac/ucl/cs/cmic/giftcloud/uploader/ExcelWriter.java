package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class for exporting a patient map to an Excel spreadsheet
 */
public class ExcelWriter extends PatientListWriter {
    private final HSSFWorkbook workbook;

    private static final String PATIENT_LIST_FILENAME = "GiftCloudPatientList.xls";
    private static final String BACKUP_PATIENT_LIST_FILENAME = "GiftCloudPatientList.backup.xls";
    private static final String BACKUP_PATIENT_LIST_FILENAME_PREFIX = "BackupGiftCloudPatientList";
    private static final String BACKUP_PATIENT_LIST_FILENAME_SUFFIX = "xls";
    private static final String PATIENT_NAME_STRING = "Patient Name";
    private static final String PATIENT_ID_STRING = "Patient ID";
    private static final String PATIENT_ALIAS_STRING = "GIFT-Cloud Alias";
    private static final String PATIENT_PPID_STRING = "GIFT-Cloud pseudonymised patient ID";


    /**
     * Constructs an ExcelWriter
     *
     * @param patientListFolder the folder to which the excel file will be exported
     * @param reporter for error reporting
     */
    public ExcelWriter(final File patientListFolder, final GiftCloudReporter reporter) {
        super(patientListFolder, reporter);
        workbook = new HSSFWorkbook();
    }

    @Override
    protected void saveFile(final File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
    }

    @Override
    protected PatientListForProject createNewPatientList(final String projectName) {
        return new ExcelPatientListForProject(workbook, projectName);
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

    private static class ExcelPatientListForProject extends PatientListForProject {
        private final HSSFSheet sheet;
        private int rowNum = 0;

        public ExcelPatientListForProject(final HSSFWorkbook workbook, final String projectName) {
            sheet = workbook.createSheet(projectName);
            addEntry(PATIENT_PPID_STRING, PATIENT_ALIAS_STRING, PATIENT_ID_STRING, PATIENT_NAME_STRING);
        }

        public void addEntry(final String hashedPatientId, final String subjectLabel, final String patientId, final String patientName) {
            final Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            final Cell patientNameCell = row.createCell(cellNum++);
            final Cell patientIdCell = row.createCell(cellNum++);
            final Cell patientAliasCell = row.createCell(cellNum++);
            final Cell patientPpidCell = row.createCell(cellNum++);
            patientNameCell.setCellValue(patientName);
            patientIdCell.setCellValue(patientId);
            patientAliasCell.setCellValue(subjectLabel);
            patientPpidCell.setCellValue(hashedPatientId);
        }
    }
}
