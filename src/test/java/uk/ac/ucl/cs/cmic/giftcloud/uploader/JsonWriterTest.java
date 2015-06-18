package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.AliasMap;
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

        final Map<String, AliasMap> projectMap = new HashMap<String, AliasMap>();

        final AliasMap aliasMap1 = new AliasMap();
        aliasMap1.addAlias("hash1", "alias1", "pid1", "name1");
        aliasMap1.addAlias("hash2", "alias2", "pid2", "name2");
        final AliasMap aliasMap2 = new AliasMap();
        aliasMap2.addAlias("hash3", "alias3", "pid3", "name3");
        final AliasMap aliasMap3 = new AliasMap();
        aliasMap3.addAlias("hash1", "alias1", "pid1", "name1");
        aliasMap3.addAlias("hash3", "alias3", "pid3", "name3");

        projectMap.put("Subject1", aliasMap1);
        projectMap.put("Subject2", aliasMap2);
        projectMap.put("Subject3", aliasMap3);

        jsonWriter.writeProjectMap(projectMap);
        jsonWriter.save(false);

        final Map<String, AliasMap> loadedProjectMap = JsonWriter.readProjectMap(tempDir, reporter);

        Assert.assertEquals(loadedProjectMap, projectMap);
        new File(tempDir, "GiftCloudPatientList.json").delete();
        tempDir.delete();
    }


}