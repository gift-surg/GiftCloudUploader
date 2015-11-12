package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.DicomException;

/**
 * A PixelDataAnonymiseFilterRequiredTag representing an integer value
 */
public class StringFilterTag extends PixelDataAnonymiseFilterRequiredTag<String> {
    public StringFilterTag(final int dicomGroup, final int dicomElement, final String value) {
        super("String", dicomGroup, dicomElement, value);
    }

    public StringFilterTag(final Attribute attribute) throws DicomException {
        this(attribute.getGroup(), attribute.getElement(), attribute.getStringValues()[0]);
    }

    @Override
    public boolean matches(final Attribute attribute) throws DicomException {
        final String[] stringValues = attribute.getStringValues();
        return stringValues.length == 1 && stringValues[0].equals(getValue());
    }
}