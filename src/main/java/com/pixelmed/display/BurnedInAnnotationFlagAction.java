package com.pixelmed.display;

/**
 * <p>A class of values for the Burned in Annotation action argument of the DicomImageBlackout constructor.</p>
 */
public abstract class BurnedInAnnotationFlagAction {
    private BurnedInAnnotationFlagAction() {
    }

    /**
     * <p>Leave any existing Burned in Annotation attribute value alone.</p>
     */
    public static final int LEAVE_ALONE = 1;
    /**
     * <p>Always remove the Burned in Annotation attribute when the file is saved, without replacing it.</p>
     */
    public static final int ALWAYS_REMOVE = 2;
    /**
     * <p>Always remove the Burned in Annotation attribute when the file is saved, only replacing it and using a value of NO when regions have been blacked out.</p>
     */
    public static final int ADD_AS_NO_IF_CHANGED = 3;
    /**
     * <p>Always remove the Burned in Annotation attribute when the file is saved, always replacing it with a value of NO,
     * regardless of whether when regions have been blacked out, such as when visual inspection confirms that there is no
     * burned in annotation.</p>
     */
    public static final int ADD_AS_NO_IF_SAVED = 4;
}
