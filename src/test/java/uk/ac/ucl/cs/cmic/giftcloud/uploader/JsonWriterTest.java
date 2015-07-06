package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.PatientAliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class JsonWriterTest {

    @Test
    public void readWriteTest() throws Exception {
        final File tempDir = Files.createTempDir();
        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);
        final JsonWriter jsonWriter = new JsonWriter(tempDir, reporter);

        final Map<String, PatientAliasMap> projectMap = new HashMap<String, PatientAliasMap>();

        final PatientAliasMap patientAliasMap1 = new PatientAliasMap();
        patientAliasMap1.addSubjectAlias("hash1", GiftCloudLabel.SubjectLabel.getFactory().create("alias1"), "pid1", "name1");
        patientAliasMap1.addSubjectAlias("hash2", GiftCloudLabel.SubjectLabel.getFactory().create("alias2"), "pid2", "name2");
        final PatientAliasMap patientAliasMap2 = new PatientAliasMap();
        patientAliasMap2.addSubjectAlias("hash3", GiftCloudLabel.SubjectLabel.getFactory().create("alias3"), "pid3", "name3");
        final PatientAliasMap patientAliasMap3 = new PatientAliasMap();
        patientAliasMap3.addSubjectAlias("hash1", GiftCloudLabel.SubjectLabel.getFactory().create("alias1"), "pid1", "name1");
        patientAliasMap3.addSubjectAlias("hash3", GiftCloudLabel.SubjectLabel.getFactory().create("alias3"), "pid3", "name3");

        projectMap.put("Subject1", patientAliasMap1);
        projectMap.put("Subject2", patientAliasMap2);
        projectMap.put("Subject3", patientAliasMap3);

        jsonWriter.writeProjectMap(projectMap);
        jsonWriter.save(false);

        final Map<String, PatientAliasMap> loadedProjectMap = JsonWriter.readProjectMap(tempDir, reporter);

        Assert.assertEquals(loadedProjectMap, projectMap);
        new File(tempDir, "GiftCloudPatientList.json").delete();
        tempDir.delete();
    }


}