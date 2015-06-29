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
     * Used to generate a new GiftCloudLabel of a particular type. For generic classes to be able to create an instance of their parameterised type, we need to pass in a factory class

     * @param <T> the type of GiftCloudLabel
     */
    public interface LabelFactory<T> { // } extends GiftCloudLabel> { // ToDo T should extend GiftCloudLabel - can do this once SubjectLabel is a GiftCloudLabel
        T create(final String label);
    }

    /**
     * Experiment label
     */
    public static class ExperimentLabel extends GiftCloudLabel {
        private static final LabelFactory<ExperimentLabel> labelFactorySingleton = new LabelFactory<ExperimentLabel>() {
            @Override
            public ExperimentLabel create(String label) {
                return new ExperimentLabel(label);
            }
        };

        /**
         * Creates a new GIFT-Cloud experiment label
         * @param label string representation of the label
         */
        protected ExperimentLabel(final String label) {
            super(label);
        }

        /**
         * Static method to return the ExperimentLabel factory
         *
         * @return a LabelFactory for creating ExperimentLabels
         */
        public static LabelFactory<ExperimentLabel> getFactory() {
            return labelFactorySingleton;
        }
    }

    /**
     * Scan label
     */
    public static class ScanLabel extends GiftCloudLabel {
        private static final LabelFactory<ScanLabel> labelFactorySingleton = new LabelFactory<ScanLabel>() {
            @Override
            public ScanLabel create(String label) {
                return new ScanLabel(label);
            }
        };

        /**
         * Creates a new GIFT-Cloud scan label
         * @param label string representation of the label
         */
        protected ScanLabel(final String label) {
            super(label);
        }

        /**
         * Static method to return the ScanLabel factory
         *
         * @return a LabelFactory for creating ScanLabels
         */
        public static LabelFactory<ScanLabel> getFactory() {
            return labelFactorySingleton;
        }
    }
}