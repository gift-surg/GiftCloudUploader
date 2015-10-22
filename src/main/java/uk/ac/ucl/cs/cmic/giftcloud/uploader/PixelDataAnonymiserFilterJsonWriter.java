package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for exporting a pixel data anonymisation filter to a Json file
 */
public class PixelDataAnonymiserFilterJsonWriter {

    // A description of the file
    private static final String APPLICATION_LABEL = "FileType";
    private static final String APPLICATION_VALUE = "GIFT-Cloud Anonymisation Filter";

    // Version number for the filter file format
    private static final String VERSION_LABEL = "Version";
    private static final String VERSION_VALUE = "1.0";

    // The minimum version of the filter file format that will be able to parse this code
    private static final String MINIMUM_VERSION_LABEL = "MinimumVersion";
    private static final String MININUM_VERSION_VALUE = "1.0";

    private static final String FILTER_NAME_LABEL = "FilterName";
    private static final String REQUIRED_TAGS_LABEL = "RequiredTags";
    private static final String REDACTED_SHAPES_LABEL = "RedactedShapes";

    private static final String TAG_DICOMGROUP = "DicomGroup";
    private static final String TAG_DICOMELEMENT = "DicomElement";
    private static final String TAG_VALUETYPE = "ValueType";
    private static final String TAG_VALUE = "Value";

    private static final String SHAPE_X = "X";
    private static final String SHAPE_Y = "Y";
    private static final String SHAPE_WIDTH = "Width";
    private static final String SHAPE_HEIGHT = "Height";


    public static PixelDataAnonymiseFilter readJsonFile(final File filterFile) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        final Object obj = parser.parse(new FileReader(filterFile));

        final JSONObject jsonObject = (JSONObject) obj;
        final String applicationName = (String) jsonObject.get(APPLICATION_LABEL);
        if (!applicationName.equals(APPLICATION_VALUE)) {
            throw new IOException("Not a GIFT-Cloud anonymisation filter");
        }
        final String versionName = (String) jsonObject.get(VERSION_LABEL);
        final String minimumVersionName = (String) jsonObject.get(MINIMUM_VERSION_LABEL);
        // ToDo: check version numbers

        final String filterName = (String) jsonObject.get(FILTER_NAME_LABEL);
        final JSONArray requiredTagsJson = (JSONArray) jsonObject.get(REQUIRED_TAGS_LABEL);
        final JSONArray redactedShapesJson = (JSONArray) jsonObject.get(REDACTED_SHAPES_LABEL);

        final List<PixelDataAnonymiseFilterRequiredTag> requiredTags = parseTagArray(requiredTagsJson);
        final List<Rectangle2D.Double> redactedShapes = parseRedactedShapes(redactedShapesJson);


        return new PixelDataAnonymiseFilter(filterName, requiredTags, redactedShapes);
    }

    private static List<Rectangle2D.Double> parseRedactedShapes(final JSONArray redactedShapesJson) {
        final List<Rectangle2D.Double> redactedShapes = new ArrayList<Rectangle2D.Double>();
        final Iterator<JSONObject> iterator = redactedShapesJson.iterator();
        while (iterator.hasNext()) {
            final JSONObject shape = iterator.next();
            redactedShapes.add(parseShape(shape));
        }
        return redactedShapes;
    }

    private static Rectangle2D.Double parseShape(final JSONObject shape) {
        final double x = ((Double)shape.get(SHAPE_X));
        final double y = ((Double)shape.get(SHAPE_Y)).doubleValue();
        final double width = ((Double)shape.get(SHAPE_WIDTH)).doubleValue();
        final double height = ((Double)shape.get(SHAPE_HEIGHT)).doubleValue();
        return new Rectangle2D.Double(x, y, width, height);
    }

    private static List<PixelDataAnonymiseFilterRequiredTag> parseTagArray(final JSONArray requiredTagsJson) throws IOException {
        final List<PixelDataAnonymiseFilterRequiredTag> requiredTags = new ArrayList<PixelDataAnonymiseFilterRequiredTag>();
        final Iterator<JSONObject> iterator = requiredTagsJson.iterator();
        while (iterator.hasNext()) {
            final JSONObject tagEntry = iterator.next();
            requiredTags.add(parseFilterTag(tagEntry));
        }
        return requiredTags;
    }

    private static PixelDataAnonymiseFilterRequiredTag parseFilterTag(final JSONObject tagEntry) throws IOException {
        final String valueType = (String)tagEntry.get(TAG_VALUETYPE);
        final int dicomGroup = ((Long)tagEntry.get(TAG_DICOMGROUP)).intValue();
        final int dicomElement = ((Long)tagEntry.get(TAG_DICOMELEMENT)).intValue();
        if (valueType.equals("Integer")) {
            final int value = ((Long)tagEntry.get(TAG_VALUE)).intValue();
            return new IntFilterTag(dicomGroup, dicomElement, value);
        } else if (valueType.equals("String")) {
            final String value = (String)tagEntry.get(TAG_VALUE);
            return new StringFilterTag(dicomGroup, dicomElement, value);
        } else {
            throw new IOException("Unexpected value type");
        }
    }

    public static void writeJsonfile(final File filterFileName, final PixelDataAnonymiseFilter filter, final GiftCloudReporter reporter) throws IOException {
        final JSONObject mainObj = new JSONObject();

        // Add some metadata for the file type
        mainObj.put(APPLICATION_LABEL, APPLICATION_VALUE);
        mainObj.put(VERSION_LABEL, VERSION_VALUE);
        mainObj.put(MINIMUM_VERSION_LABEL, MININUM_VERSION_VALUE);

        mainObj.put(FILTER_NAME_LABEL, filter.getFilterName());
        mainObj.put(REQUIRED_TAGS_LABEL, createJsonArrayFromRequiredTags(filter.getRequiredTags()));
        mainObj.put(REDACTED_SHAPES_LABEL, createJsonArrayFromRedacedShapes(filter.getRedactedShapes()));

        saveFile(filterFileName, mainObj);
    }

    private static JSONArray createJsonArrayFromRedacedShapes(List<Rectangle2D.Double> redactedShapes) {
        final JSONArray requiredTagsArray = new JSONArray();
        for (final Rectangle2D.Double shape : redactedShapes) {
            requiredTagsArray.add(createJsonShapeObject(shape));
        }
        return requiredTagsArray;    }

    private static Object createJsonShapeObject(final Rectangle2D.Double shape) {
        final JSONObject shapeObject = new JSONObject();
        shapeObject.put(SHAPE_X, shape.getX());
        shapeObject.put(SHAPE_Y, shape.getY());
        shapeObject.put(SHAPE_WIDTH, shape.getWidth());
        shapeObject.put(SHAPE_HEIGHT, shape.getHeight());

        return shapeObject;
    }

    private static JSONArray createJsonArrayFromRequiredTags(List<PixelDataAnonymiseFilterRequiredTag> requiredTags) {
        final JSONArray requiredTagsArray = new JSONArray();
        for (final PixelDataAnonymiseFilterRequiredTag tag : requiredTags) {
            requiredTagsArray.add(createJsonTagObject(tag));
        }
        return requiredTagsArray;
    }

    private static JSONObject createJsonTagObject(final PixelDataAnonymiseFilterRequiredTag tag) {
        final JSONObject tagObject = new JSONObject();
        tagObject.put(TAG_DICOMGROUP, tag.getDicomGroup());
        tagObject.put(TAG_DICOMELEMENT, tag.getDicomElement());
        tagObject.put(TAG_VALUETYPE, tag.getValueType());
        tagObject.put(TAG_VALUE, tag.getValue());

        return tagObject;
    }

    protected static void saveFile(final File file, final JSONObject mainObj) throws IOException {
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(mainObj.toJSONString());
        fileWriter.flush();
        fileWriter.close();
    }
}
