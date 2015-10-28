package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.DicomException;

/**
 * A PixelDataAnonymiseFilterRequiredTag representing an integer value
 */
public class IntFilterTag extends PixelDataAnonymiseFilterRequiredTag<Integer> {
    public IntFilterTag(final int dicomGroup, final int dicomElement, final Integer value) {
        super("Integer", dicomGroup, dicomElement, value);
    }

    public IntFilterTag(final Attribute attribute) throws DicomException {
        this(attribute.getGroup(), attribute.getElement(), attribute.getIntegerValues()[0]);
    }

    @Override
    public boolean matches(final Attribute attribute) throws DicomException {
        final int[] integerValues = attribute.getIntegerValues();
        return integerValues.length == 1 && integerValues[0] == getValue();
    }
}
