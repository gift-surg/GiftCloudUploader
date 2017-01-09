/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.LoggingReporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for classes that export a patient map to a file
 */
public abstract class PatientListWriter {
    private final File patientListFolder;
    protected final LoggingReporter reporter;
    private final Map<String, PatientListForProject> sheets = new HashMap<String, PatientListForProject>();

    /**
     * Constructs a PatientListWriter
     *
     * @param patientListFolder the folder to which the excel file will be exported
     * @param reporter for error reporting
     */
    public PatientListWriter(final File patientListFolder, final LoggingReporter reporter) {
        this.patientListFolder = patientListFolder;
        this.reporter = reporter;
        if (!GiftCloudUtils.createDirectoryIfNotExisting(patientListFolder)) {
            reporter.silentWarning("Could not create the patient list export folder:" + patientListFolder.toString());
        }
    }

    /**
     * Writes out the file from the current subject information
     */
    public final void save(final boolean createBackup) {

        try {
            // Ensure the directory exists. We perform this here in case the folder was not accessible when the uploader started
            if (!GiftCloudUtils.createDirectoryIfNotExisting(patientListFolder)) {
                reporter.silentWarning("Could not create the patient list export folder:" + patientListFolder.toString());
                return;
            }

            File file = new File(patientListFolder, getPatientListFilename());

            // If the file already exists, rename it so it is a preserved as a backup
            if (file.exists()) {
                final File backupFile = new File(patientListFolder, getBackupPatientListFilename());
                file.renameTo(backupFile);
                file = new File(patientListFolder, getPatientListFilename());
            }

            // Save the current file
            saveFile(file);

            if (createBackup) {
                // Create an additional backup (at most one per hour)
                GiftCloudUtils.createTimeStampedBackup(file, patientListFolder, getBackupPatientListFilenamePrefix(), getBackupPatientListFilenameSuffix());
            }

        } catch (FileNotFoundException e) {
            reporter.silentLogException(e, "Failed to write the file containing patient information due to the following error: " + e.getLocalizedMessage());
        } catch (IOException e) {
            reporter.silentLogException(e, "Failed to write the file containing patient information due to the following error: " + e.getLocalizedMessage());
        }

    }


    /**
     * Adds subject information from a project map
     *
     * @param projectMap a map of project name to PatientAliasMap containing subject information
     */
    public void writeProjectMap(final Map<String, PatientAliasMap> projectMap) {
        for (Map.Entry<String, PatientAliasMap> entry : projectMap.entrySet()) {
            final String projectName = entry.getKey();
            final Map<String, PatientAliasMap.SubjectAliasRecord> aliasMap = entry.getValue().getMap();

            for (PatientAliasMap.SubjectAliasRecord subjectAliasRecord : aliasMap.values()) {
                addEntry(projectName, subjectAliasRecord.getPpid(), subjectAliasRecord.getSubjectLabel(), subjectAliasRecord.getPatientId(), subjectAliasRecord.getPatientName());
            }
        }
    }

    /**
     * Adds a record for a single subject
     *
     * @param projectName the name of the GIFT-Cloud project
     * @param hashedPatientId the pseuodimysed patient ID (PPID) for this subject
     * @param subjectLabel the GIFT-Cloud label for this subject
     * @param patientId the original patient ID for this subject
     * @param patientName the original patient name fot this subject
     */
    protected void addEntry(final String projectName, final String hashedPatientId, final GiftCloudLabel.SubjectLabel subjectLabel, final String patientId, final String patientName) {
        final PatientListForProject sheet = getOrCreatePatientList(projectName);
        sheet.addEntry(hashedPatientId, subjectLabel.getStringLabel(), patientId, patientName);
    }

    /**
     * Creates a new list of subject IDs for a particular GIFT-Cloud project
     *
     * @param projectName the name of the GIFT-Cloud project
     * @return a new PatientListForProject object for this project
     */
    protected abstract PatientListForProject createNewPatientList(final String projectName);

    /**
     * Saves the current list of patients in the file format supported by the implementation
     *
     * @param file a File object representing the file to be saved
     * @throws IOException if an error occurs during the save
     */
    protected abstract void saveFile(final File file) throws IOException;

    /**
]     * @return the filename to be used for the patient list file
     */
    protected abstract String getPatientListFilename();

    /**
     * @return the filename to be used for the previous patient list file
     */
    protected abstract String getBackupPatientListFilename();

    /**
     * @return the prefix of the filename to be used for automatic backup files
     */
    protected abstract String getBackupPatientListFilenamePrefix();

    /**
     * @return the suffix of the filename to be used for automatic backup files
     */
    protected abstract String getBackupPatientListFilenameSuffix();

    private PatientListForProject getOrCreatePatientList(final String projectName) {
        if (!sheets.containsKey(projectName)) {
            sheets.put(projectName, createNewPatientList(projectName));

        }
        return sheets.get(projectName);
    }

    /**
     * An abstract representation of a list of patients for a particular GIFT-Cloud project. This should be implemented in a manner appropriate for however the list is be stored
     */
    protected static abstract class PatientListForProject {
        public abstract void addEntry(final String hashedPatientId, final String subjectLabel, final String patientId, final String patientName);
    }
}
