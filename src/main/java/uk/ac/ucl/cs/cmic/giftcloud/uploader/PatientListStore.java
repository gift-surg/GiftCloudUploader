package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.AliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.Map;

/**
 * A class used to save and load the patient list from local storage
 */
public class PatientListStore {
    private final GiftCloudReporter reporter;

    /**
     * Construct a PatientListStore
     *
     * @param reporter for error and progress reporting
     */
    public PatientListStore(final GiftCloudReporter reporter) {
        this.reporter = reporter;
    }

    /**
     * Save the project list to JSON and Excel on local storage
     *
     * @param projectMap map of project names to alias maps to be saved
     */
    public void save(final Map<String, AliasMap> projectMap) {
        final JsonWriter jsonWriter = new JsonWriter(reporter);
        jsonWriter.writeProjectMap(projectMap);
        jsonWriter.writeJsonFile();

        final ExcelWriter excelWriter = new ExcelWriter(reporter);
        excelWriter.writeProjectMap(projectMap);
        excelWriter.writeExcelFile();
    }

    /**
     * Loads the project list from a JSON file on local storage
     *
     * @return the reconstructed map of project names to alias maps
     */
    public Map<String, AliasMap> load() {
        return JsonWriter.readProjectMap(reporter);
    }
}
