package uk.ac.ucl.cs.cmic.giftcloud.restserver;

/**
 * Base class for GIFT-Cloud label types.
 * The aim is to use type safety to ensure different types of labels do not get mixed in the code
 */
public abstract class GiftCloudLabel {
    protected final String label;

    /**
     * Create a new GiftCloudLabel using the given String as the underlying label
     * @param label
     */
    protected GiftCloudLabel(final String label) {
        this.label = label;
    }

    /**
     * Returns the underlying String label
     *
     * @return the String label
     */
    public String getStringLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Used to generate a new GiftCloudLabel in generic classes
     * @param <T> the type of GiftCloudLabel
     */
    public interface LabelFactory<T extends GiftCloudLabel> {
        T create(final String label);
    }

    /**
     * Scan label
     */
    public static class ScanLabel extends GiftCloudLabel {
        protected ScanLabel(final String label) {
            super(label);
        }

        /**
         * Static method to create a new factory for ScanLabels
         *
         * @return a LabelFactory for creating ScanLabels
         */
        public static LabelFactory<ScanLabel> getFactory() {
            return new LabelFactory<ScanLabel>() {
                @Override
                public ScanLabel create(String label) {
                    return new ScanLabel(label);
                }
            };
        }
    }
}