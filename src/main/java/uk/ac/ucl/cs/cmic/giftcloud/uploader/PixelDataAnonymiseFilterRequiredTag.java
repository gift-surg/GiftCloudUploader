package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.base.Objects;

/**
 * Stores a DICOM tag and value for a filter. An image may use the filter if the tag is present and equal to the specified value
 * @param <T> the Java data type of the tag
 */
public class PixelDataAnonymiseFilterRequiredTag<T> {
    private final int dicomGroup;
    private final int dicomElement;
    private final T value;
    private final String valueType;

    public PixelDataAnonymiseFilterRequiredTag(final String valueType, final int dicomGroup, final int dicomElement, final T value) {
        this.valueType = valueType;
        this.dicomGroup = dicomGroup;
        this.dicomElement = dicomElement;
        this.value = value;
    }

    public int getDicomGroup() {
        return dicomGroup;
    }

    public int getDicomElement() {
        return dicomElement;
    }

    public T getValue() {
        return value;
    }

    /**
     * Returns a String describing this data type - this is to help with Json deserialisation
     * @return
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * Determines if another object refers to the same scan alias
     *
     * @param otherOb the object to compare to this one
     * @return true if the other object is an ScanAliasRecord referring to the same scan alias
     */
    @Override
    public boolean equals(Object otherOb) {
        if (this == otherOb) return true;

        if (!(otherOb instanceof PixelDataAnonymiseFilterRequiredTag)) return false;

        final PixelDataAnonymiseFilterRequiredTag<T> other = (PixelDataAnonymiseFilterRequiredTag<T>) otherOb;

        return this.dicomGroup == other.dicomGroup && this.dicomElement == other.dicomElement && this.value.equals(other.value) && this.valueType.equals(other.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dicomGroup, dicomElement, value, valueType);
    }
}

