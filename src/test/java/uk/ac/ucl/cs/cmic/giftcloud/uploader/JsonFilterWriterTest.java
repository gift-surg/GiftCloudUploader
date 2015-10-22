package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.io.Files;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class JsonFilterWriterTest {

    @Test
    public void readWriteTest() throws Exception {
        final File tempDir = Files.createTempDir();
        final GiftCloudReporter reporter = mock(GiftCloudReporter.class);

        final String filterName = "MyFilter";
        final List<PixelDataAnonymiseFilterRequiredTag> requiredTags = new ArrayList<PixelDataAnonymiseFilterRequiredTag>();
        requiredTags.add(new IntFilterTag(1, 2, 123));
        requiredTags.add(new StringFilterTag(5, 2, "MyString"));
        requiredTags.add(new IntFilterTag(1001, 2002, 12345));
        final List<Rectangle2D.Double> redactedShapes = new ArrayList<Rectangle2D.Double>();
        redactedShapes.add(new Rectangle2D.Double(1,2,3,4));
        redactedShapes.add(new Rectangle2D.Double(10.1,20.4,30.3,40.5));
        final PixelDataAnonymiseFilter filter = new PixelDataAnonymiseFilter(filterName, requiredTags, redactedShapes);

        final File jsonFile = new File(tempDir, "TestFile.json");
        PixelDataAnonymiserFilterJsonWriter.writeJsonfile(jsonFile, filter, reporter);

        final PixelDataAnonymiseFilter filterRead = PixelDataAnonymiserFilterJsonWriter.readJsonFile(jsonFile);
        junit.framework.Assert.assertEquals(filter.getFilterName(), filterRead.getFilterName());
        junit.framework.Assert.assertEquals(filter.getRedactedShapes(), filterRead.getRedactedShapes());
        junit.framework.Assert.assertEquals(filter.getRequiredTags(), filterRead.getRequiredTags());
        jsonFile.delete();
    }
}