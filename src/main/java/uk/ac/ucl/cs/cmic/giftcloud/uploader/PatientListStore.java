package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.AliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.Map;

public class PatientListStore {
    private GiftCloudReporter reporter;

    public PatientListStore(final GiftCloudReporter reporter) {
        this.reporter = reporter;
    }

    public void update(final Map<String, AliasMap> projectMap) {
        final JsonWriter jsonWriter = new JsonWriter(reporter);
        jsonWriter.writeProjectMap(projectMap);
        jsonWriter.writeJsonFile();

        final ExcelWriter excelWriter = new ExcelWriter(reporter);
        excelWriter.writeProjectMap(projectMap);
        excelWriter.writeExcelFile();
    }

    public Map<String, AliasMap> load() {
        return JsonWriter.readProjectMap(reporter);
    }
}
