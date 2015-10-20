package uk.ac.ucl.cs.cmic.giftcloud.uploader;

/**
 * A PixelDataAnonymiseFilterRequiredTag representing an integer value
 */
public class IntFilterTag extends PixelDataAnonymiseFilterRequiredTag<Integer> {
    public IntFilterTag(final int dicomGroup, final int dicomElement, final Integer value) {
        super("Integer", dicomGroup, dicomElement, value);
    }
}
