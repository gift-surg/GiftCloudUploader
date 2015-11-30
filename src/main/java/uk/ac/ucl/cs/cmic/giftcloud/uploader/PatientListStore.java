package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.PatientAliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.util.Map;

/**
 * A class used to save and load the patient list from local storage
 */
public class PatientListStore {
    private GiftCloudProperties giftCloudProperties;
    private final GiftCloudReporter reporter;
    private final File localCacheFolder;

    /**
     * Construct a PatientListStore
     *
     * @param reporter for error and progress reporting
     */
    public PatientListStore(final GiftCloudProperties giftCloudProperties, final GiftCloudReporter reporter) {
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;

        // The local cache folder is used to store the patient list that will be reloaded on startup
        final Optional<String> localCacheFolderOptional = giftCloudProperties.getPatientListLocalCacheFolder();
        localCacheFolder = localCacheFolderOptional.isPresent() ? new File(localCacheFolderOptional.get()) : GiftCloudUtils.createOrGetGiftCloudFolder(Optional.of(reporter));
    }

    /**
     * Save the project list to JSON and Excel on local storage
     *
     * @param projectMap map of project names to alias maps to be saved
     */
    public void save(final Map<String, PatientAliasMap> projectMap) {

        try {
            final JsonWriter jsonWriter = new JsonWriter(localCacheFolder, reporter);
            jsonWriter.writeProjectMap(projectMap);
            jsonWriter.save(true);
        } catch (Throwable t) {
            reporter.silentLogException(t, "Failed to save the cache of the project list due to the following error:" + t.getLocalizedMessage());
        }

        try {
            // The export folder is used to store the patient list exported to an excel spreadsheet
            final Optional<String> exportFolderOptional = giftCloudProperties.getPatientListExportFolder();
            final Optional<char[]> patientListPassword = giftCloudProperties.getPatientListPassword();
            if (exportFolderOptional.isPresent()) {
                final File exportFolder = new File(exportFolderOptional.get());

                final ExcelWriter excelWriter = new ExcelWriter(exportFolder, patientListPassword, reporter);
                excelWriter.writeProjectMap(projectMap);
                excelWriter.save(true);
            }
        } catch (Throwable t) {
            reporter.silentLogException(t, "Failed to export the project list spreadsheet due to the following error:" + t.getLocalizedMessage());
        }
    }

    /**
     * Loads the project list from a JSON file on local storage
     *
     * @return the reconstructed map of project names to alias maps
     */
    public Map<String, PatientAliasMap> load() {
        return JsonWriter.readProjectMap(localCacheFolder, reporter);
    }
}
