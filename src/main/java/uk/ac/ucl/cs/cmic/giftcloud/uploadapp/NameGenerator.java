package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Threadsafe class to generate unique names
 */
class NameGenerator<T extends GiftCloudLabel> {
    private long nameNumber;
    private final GiftCloudLabel.LabelFactory<T> labelFactory;
    private String prefix;

    /** Creates a new NameGenerator which will create names starting with the given prefix, and incrementing a suffix number starting at startNumber
     * @param prefix the string prefix for each generated name
     * @param startNumber the number used for the suffix of the first name, which will be incremented after each name generation
     */
    NameGenerator(final String prefix, final long startNumber, final GiftCloudLabel.LabelFactory<T> labelFactory) {
        this.prefix = prefix;
        this.nameNumber = startNumber;
        this.labelFactory = labelFactory;
    }

    /** Change the name prefix. Also resets the number if the prefix has has changed
     * @param prefix the string prefix for each generated name
     * @param startNumber the number used for the suffix of the first name, which will be incremented after each name generation
     */
    void updateNamePrefix(final String prefix, final long startNumber) {
        if (!prefix.equals(this.prefix)) {
            this.prefix = prefix;
            this.nameNumber = startNumber;
        }
    }

    /** Returns a unique name that is not part of the given list of known names
     * @param knownNames a list of known names. The returned name will not be one of these
     * @return a new name
     */
    T getNewName(final Set<String> knownNames) {
        String candidateName;

        do {
            candidateName = getNextName();

        } while (knownNames.contains(candidateName));

        return labelFactory.create(candidateName);
    }

    /** Returns a name that has not been returned before by this object
     * @return a new name
     */
    private String getNextName() {
        long nextNameNumber = getNextNameNumber();
        return prefix + Long.toString(nextNameNumber);
    }

    private synchronized long getNextNameNumber() {
        return nameNumber++;
    }

    /**
     * A class for generating subject labels.
     */
    static class SubjectNameGenerator extends NameGenerator<GiftCloudLabel.SubjectLabel>{
        private static String defaultAutoSubjectNamePrefix = "AutoUploadSubject";
        private static final long autoSubjectNameStartNumber = 1;
        private Map<GiftCloudLabel.SubjectLabel, ExperimentNameGenerator> experimentLabelGeneratorMap = new HashMap<GiftCloudLabel.SubjectLabel, ExperimentNameGenerator>();

        /** Creates a new NameGenerator which will create names starting with the given prefix, and incrementing a suffix number starting at startNumber
         */
        SubjectNameGenerator(final Optional<String> prefix) {
            super(prefix.orElse(defaultAutoSubjectNamePrefix), autoSubjectNameStartNumber, GiftCloudLabel.SubjectLabel.getFactory());
        }

        void updateSubjectNamePrefix(final Optional<String> prefix) {
            updateNamePrefix(prefix.orElse(defaultAutoSubjectNamePrefix), autoSubjectNameStartNumber);
        }


        /**
         * returns the experiment name generator for a particular subject
         *
         * @param subjectLabel the GIFT-Cloud subject label
         * @return the name generator for generating new experiment names for this subject
         */
        ExperimentNameGenerator getExperimentNameGenerator(final GiftCloudLabel.SubjectLabel subjectLabel) {
            if (!experimentLabelGeneratorMap.containsKey(subjectLabel)) {
                experimentLabelGeneratorMap.put(subjectLabel, new ExperimentNameGenerator());
            }
            return experimentLabelGeneratorMap.get(subjectLabel);
        }
    }

    /**
     * A class for generating experiment labels. There will be a separate generator for each subject
     */
    static class ExperimentNameGenerator extends NameGenerator<GiftCloudLabel.ExperimentLabel>{

        private static String autoExperimentNamePrefix = "Study";
        private static long autoExperimentNameStartNumber = 1;
        private Map<GiftCloudLabel.ExperimentLabel, ScanNameGenerator> scanLabelGeneratorMap = new HashMap<GiftCloudLabel.ExperimentLabel, ScanNameGenerator>();

        /** Creates a new ExperimentNameGenerator which will create GIFT-Cloud experiment labels
         */
        ExperimentNameGenerator() {
            super(autoExperimentNamePrefix, autoExperimentNameStartNumber, GiftCloudLabel.ExperimentLabel.getFactory());
        }

        /**
         * returns the scan name generator for a particular experiment
         *
         * @param experimentLabel the GIFT-Cloud experiment label
         * @return the name generator for generating new scan names for this experiment
         */
        ScanNameGenerator getScanNameGenerator(final GiftCloudLabel.ExperimentLabel experimentLabel) {
            if (!scanLabelGeneratorMap.containsKey(experimentLabel)) {
                scanLabelGeneratorMap.put(experimentLabel, new ScanNameGenerator());
            }
            return scanLabelGeneratorMap.get(experimentLabel);
        }
    }

    /**
     * A class for generating scan labels. There will be a separate generator for each experiment
     */
    static class ScanNameGenerator extends NameGenerator<GiftCloudLabel.ScanLabel>{

        private static String autoScanNamePrefix = "Series";
        private static long autoScanNameStartNumber = 1;

        /** Creates a new ScanNameGenerator which will create GIFT-Cloud scan labels
         */
        ScanNameGenerator() {
            super(autoScanNamePrefix, autoScanNameStartNumber, GiftCloudLabel.ScanLabel.getFactory());
        }
    }
}
