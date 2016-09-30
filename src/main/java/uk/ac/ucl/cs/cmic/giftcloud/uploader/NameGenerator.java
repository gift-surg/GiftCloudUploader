/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Threadsafe class to generate unique names
 */
class NameGenerator<T extends GiftCloudLabel> {
    private long nameNumber;
    private final String numZerosString;
    private final GiftCloudLabel.LabelFactory<T> labelFactory;
    private String prefix;

    /** Creates a new NameGenerator which will create names starting with the given prefix, and incrementing a suffix number starting at startNumber
     * @param prefix the string prefix for each generated name
     * @param startNumber the number used for the suffix of the first name, which will be incremented after each name generation
     */
    NameGenerator(final String prefix, final long startNumber, final String numZerosString, final GiftCloudLabel.LabelFactory<T> labelFactory) {
        this.prefix = prefix;
        this.nameNumber = startNumber;
        this.numZerosString = numZerosString;
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
        Pair<String, String> candidateNames;

        // Repeat until we get a name which doesn't already exist, either in its legacy or new format
        do {
            candidateNames = getNextName();

        } while (knownNames.contains(candidateNames.getLeft()) || knownNames.contains(candidateNames.getRight()));

        return labelFactory.create(candidateNames.getLeft());
    }

    /** Returns a name that has not been returned before by this object
     * @return a {@link Pair} containing two versions of the new name. The first should be used. The second is used to prevent duplicates arising from the use of different naming schemes
     */
    private Pair<String, String> getNextName() {
        long nextNameNumber = getNextNameNumber();
        final String new_value = prefix + String.format("%0" + numZerosString + "d", nextNameNumber);
        final String legacy_value = prefix + Long.toString(nextNameNumber);
        return new ImmutablePair<String, String>(new_value, legacy_value);
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
            super(prefix.orElse(defaultAutoSubjectNamePrefix), autoSubjectNameStartNumber, "5", GiftCloudLabel.SubjectLabel.getFactory());
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
                experimentLabelGeneratorMap.put(subjectLabel, new ExperimentNameGenerator(subjectLabel.getStringLabel() + "-Study"));
            }
            return experimentLabelGeneratorMap.get(subjectLabel);
        }
    }

    /**
     * A class for generating experiment labels. There will be a separate generator for each subject
     */
    static class ExperimentNameGenerator extends NameGenerator<GiftCloudLabel.ExperimentLabel>{

        private static long autoExperimentNameStartNumber = 1;
        private Map<GiftCloudLabel.ExperimentLabel, ScanNameGenerator> scanLabelGeneratorMap = new HashMap<GiftCloudLabel.ExperimentLabel, ScanNameGenerator>();

        /** Creates a new ExperimentNameGenerator which will create GIFT-Cloud experiment labels
         */
        ExperimentNameGenerator(final String autoExperimentNamePrefix) {
            super(autoExperimentNamePrefix, autoExperimentNameStartNumber, "1", GiftCloudLabel.ExperimentLabel.getFactory());
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
            super(autoScanNamePrefix, autoScanNameStartNumber, "1", GiftCloudLabel.ScanLabel.getFactory());
        }
    }
}
