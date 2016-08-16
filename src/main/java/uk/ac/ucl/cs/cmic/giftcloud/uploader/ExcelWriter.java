package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.ac.ucl.cs.cmic.giftcloud.util.LoggingReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.*;
import java.security.GeneralSecurityException;

/**
 * Class for exporting a patient map to an Excel spreadsheet
 */
public class ExcelWriter extends PatientListWriter {
    private final XSSFWorkbook workbook;

    private static final String PATIENT_LIST_FILENAME = "GiftCloudPatientList.xlsx";
    private static final String BACKUP_PATIENT_LIST_FILENAME = "GiftCloudPatientList.backup.xlsx";
    private static final String BACKUP_PATIENT_LIST_FILENAME_PREFIX = "BackupGiftCloudPatientList";
    private static final String BACKUP_PATIENT_LIST_FILENAME_SUFFIX = "xlsx";
    private static final String PATIENT_NAME_STRING = "Patient Name";
    private static final String PATIENT_ID_STRING = "Patient ID";
    private static final String PATIENT_ALIAS_STRING = "GIFT-Cloud Alias";
    private static final String PATIENT_PPID_STRING = "GIFT-Cloud pseudonymised patient ID";
    private final Optional<char[]> spreadsheetPassword;


    /**
     * Constructs an ExcelWriter
     *
     * @param patientListFolder the folder to which the excel file will be exported
     * @param reporter for error reporting
     */
    public ExcelWriter(final File patientListFolder, final Optional<char[]> spreadsheetPassword, final LoggingReporter reporter) {
        super(patientListFolder, reporter);
        this.spreadsheetPassword = spreadsheetPassword;
        workbook = new XSSFWorkbook();
    }

    @Override
    protected void saveFile(final File file) throws IOException {
        if (spreadsheetPassword.isPresent() && StringUtils.isNotBlank(new String(spreadsheetPassword.get()))) {
            try {
                // Write the unencrypted spreadsheet to an in-memory stream
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                baos.close();

                // Create objects for encryption
                POIFSFileSystem fs = new POIFSFileSystem();
                EncryptionInfo info = new EncryptionInfo(fs, EncryptionMode.agile);
                Encryptor enc = info.getEncryptor();

                // Set the password
                enc.confirmPassword(new String(spreadsheetPassword.get()));

                // Open the in-memory spreadsheet
                InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
                OPCPackage opc = OPCPackage.open(inputStream);
                OutputStream os = enc.getDataStream(fs);
                opc.save(os);
                opc.close();
                inputStream.close();

                // Write the encrypted Excel spreadsheet
                FileOutputStream fos = new FileOutputStream(file);
                fs.writeFilesystem(fos);
                fos.close();
            } catch (InvalidFormatException e) {
                throw new IOException("Unable to save the patient list file due to the following InvalidFormatException when reading the excel file:" + e.getLocalizedMessage(), e);
            } catch (GeneralSecurityException e) {
                throw new IOException("Unable to save the patient list file due to the following GeneralSecurityException when reading the excel file:" + e.getLocalizedMessage(), e);
            }
        } else {
            // Write the unencrypted spreadsheet to an in-memory stream
            final FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
        }
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
        private final XSSFSheet sheet;
        private int rowNum = 0;

        public ExcelPatientListForProject(final XSSFWorkbook workbook, final String projectName) {
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
