package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

public class PixelDataAnonymiseFilter {
    private String filterName;
    private List<PixelDataAnonymiseFilterRequiredTag> requiredTags;
    private List<Rectangle2D.Double> redactedShapes;

    public PixelDataAnonymiseFilter(final String filterName, final List<PixelDataAnonymiseFilterRequiredTag> requiredTags, final List<Rectangle2D.Double> redactedShapes) {
        this.filterName = filterName;
        this.requiredTags = requiredTags;
        this.redactedShapes = redactedShapes;
    }

    public Vector<Shape> getRedactedShapesAsShapeVector() {
        final Vector<Shape> shapes = new Vector<Shape>();
        for (Rectangle2D.Double redactedRectangle : redactedShapes) {
            shapes.add(redactedRectangle);
        }
        return shapes;
    }

    public String getFilterName() {
        return filterName;
    }

    public List<PixelDataAnonymiseFilterRequiredTag> getRequiredTags() {
        return requiredTags;
    }

    public List<Rectangle2D.Double> getRedactedShapes() {
        return redactedShapes;
    }

    public boolean matches(final AttributeList attributeList) throws DicomException {
        for (final PixelDataAnonymiseFilterRequiredTag requiredTag : requiredTags) {
            final AttributeTag attributeTag = new AttributeTag(requiredTag.getDicomGroup(), requiredTag.getDicomElement());
            if (!attributeList.containsKey(attributeTag)) {
                return false;
            }
            final Attribute attribute = attributeList.get(attributeTag);
            if (!requiredTag.matches(attribute)) {
                return false;
            }
        }
        return true;
    }
}
