package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.base.Objects;
import uk.ac.ucl.cs.cmic.giftcloud.util.LabelUidMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;
import java.util.Map;

/**
 * Stores a mapping of hashed patient IDs and patient labels to patient records
 */
public class PatientAliasMap {
    private final LabelUidMap<GiftCloudLabel.SubjectLabel, SubjectAliasRecord> subjectMap = new LabelUidMap<GiftCloudLabel.SubjectLabel, SubjectAliasRecord>();

    /**
     * Determine if a record already exists for this hashed patient ID
     *
     * @param hashedPatientId the unique map key, ie a hashed patient ID
     * @return true if an entry already exists for the subject
     */
    public boolean containsPpid(final String hashedPatientId) {
        return subjectMap.containsUid(hashedPatientId);
    }

    /**
     * Returns the GIFT-Cloud subject label for the specified hashed patient ID
     *
     * @param hashedPatientId the unique map key specified by a hashed patient ID
     * @return an Optional of the subject label for this hashed patient ID, or an empty optional if no record exists for this hashed patient ID
     */
    public Optional<GiftCloudLabel.SubjectLabel> getSubjectLabel(final String hashedPatientId) {
        if (subjectMap.containsUid(hashedPatientId)) {
            return Optional.of(subjectMap.getValueForUid(hashedPatientId).getSubjectLabel());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Adds a new record for a new subject
     *
     * @param hashedPatientId the pseudonymised patient ID (PPID), which is a one-way hash of the patient ID
     * @param subjectLabel the GIFT-Cloud subject label which will be visible on the server
     * @param patientId the real patient ID, which will only be stored locally
     * @param patientName the real patient name, which will only be stored locally
     */
    public void addSubjectAlias(final String hashedPatientId, final GiftCloudLabel.SubjectLabel subjectLabel, final String patientId, final String patientName) {
        subjectMap.put(subjectLabel, hashedPatientId, new SubjectAliasRecord(hashedPatientId, subjectLabel, patientId, patientName));
    }

    /**
     * Returns the map of record maps
     *
     * @return the map of alias records
     */
    public Map<String, SubjectAliasRecord> getMap() {
        return subjectMap.getUidMap();
    }

    /**
     * Compares another object to this PatientAliasMap
     *
     * @param otherOb the other object
     * @return true if the other object is an PatientAliasMap for the same subject
     */
    @Override public boolean equals(Object otherOb) {
        if (this == otherOb) return true;

        if (!(otherOb instanceof PatientAliasMap)) return false;

        final PatientAliasMap other = (PatientAliasMap)otherOb;

        return other.getMap().equals(this.getMap());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getMap());
    }

    /**
     * Gets the GIFT-Cloud label for an experiment
     *
     * @param subjectLabel the GIFT-Cloud label for the subject
     * @param hashedStudyInstanceUid a one-way hash of the study instance UID
     * @return an Optional, set to the GIFT-Cloud experiment label if the hashed ID exists
     */
    public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid) {
        if (!subjectMap.containsLabel(subjectLabel)) {
            return Optional.empty();
        } else {
            return subjectMap.getValueForLabel(subjectLabel).getExperimentLabel(hashedStudyInstanceUid);
        }
    }

    /**
     * Gets the GIFT-Cloud label for a scan
     *
     * @param subjectLabel the GIFT-Cloud label for the subject
     * @param experimentLabel the GIFT-Cloud label for the experiment
     * @param hashedSeriesInstanceUid a one-way hash of the series instance UID
     * @return an Optional, set to the scan label if the hashed ID exists
     */
    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid) {
        if (!subjectMap.containsLabel(subjectLabel)) {
            return Optional.empty();
        } else {
            return subjectMap.getValueForLabel(subjectLabel).getScanLabel(experimentLabel, hashedSeriesInstanceUid);
        }
    }

    /**
     * Adds a new GIFT-Cloud label for an experiment
     *
     * @param subjectLabel the GIFT-Cloud label for the subject
     * @param hashedStudyInstanceUid a one-way hash of the study instance UID
     * @param experimentLabel the GIFT-Cloud label to add to this experiment
     * @throws IOException if the subject does not exist
     */
    public void addExperimentAlias(final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid, final GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException {
        if (!subjectMap.containsLabel(subjectLabel)) {
            throw new IOException("The subject alias was not found");
        }
        subjectMap.getValueForLabel(subjectLabel).addExperimentLabel(hashedStudyInstanceUid, experimentLabel);
    }

    /**
     * Adds a GIFT-Cloud alias for a scan
     *
     * @param subjectLabel the GIFT-Cloud alias for the subject
     * @param experimentLabel the GIFT-Cloud alias for the experiment
     * @param hashedSeriesInstanceUid a one-way hash of the series instance UID
     * @param scanLabel the GIFT-Cloud alias to add to this scan
     * @throws IOException if the subject or experiment do not exist
     */
    public void addScanAlias(final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid, final GiftCloudLabel.ScanLabel scanLabel) throws IOException {
        if (!subjectMap.containsLabel(subjectLabel)) {
            throw new IOException("The subject label was not found");
        }
        subjectMap.getValueForLabel(subjectLabel).addScanLabel(experimentLabel, hashedSeriesInstanceUid, scanLabel);
    }

    /**
     * Stores details of subjects which have been uploaded, for local storage on the uploading client
     */
    public static class SubjectAliasRecord {
        private final String ppid;
        private final GiftCloudLabel.SubjectLabel subjectLabel;
        private final String patientId;
        private final String patientName;
        private final LabelUidMap<GiftCloudLabel.ExperimentLabel, ExperimentAliasRecord> experimentAliasRecordMap = new LabelUidMap<GiftCloudLabel.ExperimentLabel, ExperimentAliasRecord>();;

        /**
         * Creates a new subject record for local storage
         *
         * @param ppid the pseudonymised patient ID (PPID), which is a one-way hash of the patient ID
         * @param subjectLabel the GIFT-Cloud subject label which will be visible on the server
         * @param patientId the real patient ID, which will only be stored locally
         * @param patientName the real patient name, which will only be stored locally
         */
        public SubjectAliasRecord(final String ppid, final GiftCloudLabel.SubjectLabel subjectLabel, final String patientId, final String patientName) {
            this.ppid = ppid;
            this.subjectLabel = subjectLabel;
            this.patientId = patientId;
            this.patientName = patientName;
        }

        /**
         * @return the GIFT-Cloud subject label
         */
        public GiftCloudLabel.SubjectLabel getSubjectLabel() {
            return subjectLabel;
        }

        /**
         * @return the pseudonymised patient ID (PPID), which is a one-way hash of the patient ID
         */
        public String getPpid() {
            return ppid;
        }

        /**
         * @return the real patient ID, which will only be stored locally
         */
        public String getPatientId() {
            return patientId;
        }

        /**
         * @return the real patient name, which will only be stored locally
         */
        public String getPatientName() {
            return patientName;
        }

        /**
         * Determines if another object refers to the same subject alias
         *
         * @param otherOb the object to compare to this one
         * @return true if the other object is an SubjectAliasRecord referring to the same subject alias
         */
        @Override public boolean equals(Object otherOb) {
            if (this == otherOb) return true;

            if (!(otherOb instanceof SubjectAliasRecord)) return false;

            final SubjectAliasRecord other = (SubjectAliasRecord)otherOb;

            return this.ppid.equals(other.ppid) && this.subjectLabel.equals(other.subjectLabel) && this.patientId.equals(other.patientId) && this.patientName.equals(other.patientName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(ppid, subjectLabel, patientId, patientName);
        }

        /**
         * Returns the GIFT-Cloud experiment label for a particualar scan
         *
         * @param hashedStudyInstanceUid pseudonymised study UID (PSID) for this scan
         * @return the experiment label
         */
        public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final String hashedStudyInstanceUid) {
            if (!experimentAliasRecordMap.containsUid(hashedStudyInstanceUid)) {
                return Optional.empty();
            }
            return Optional.of(experimentAliasRecordMap.getValueForUid(hashedStudyInstanceUid).getExperimentLabel());
        }

        /**
         * Returns the GIFT-Cloud scan label for a particular scan
         *
         * @param experimentLabel GIFT-Cloud label for the experiment containing this scan
         * @param hashedSeriesInstanceUid pseudonymised series UID (PSERID) for this scan
         * @return the scan label
         */
        public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid) {
            if (!experimentAliasRecordMap.containsLabel(experimentLabel)) {
                return Optional.empty();
            }
            return experimentAliasRecordMap.getValueForLabel(experimentLabel).getScanLabel(hashedSeriesInstanceUid);
        }

        /**
         * Adds a new GIFT-Cloud label for a particular experiment
         * @param hashedStudyInstanceUid  pseudonymised study UID (PSID) for this scan
         * @param experimentLabel the GIFT-Cloud experiment label
         */
        public void addExperimentLabel(final String hashedStudyInstanceUid, final GiftCloudLabel.ExperimentLabel experimentLabel) {
            experimentAliasRecordMap.put(experimentLabel, hashedStudyInstanceUid, new ExperimentAliasRecord(hashedStudyInstanceUid, experimentLabel));
        }

        /**
         * Adds a new GIFT-Cloud label for a particular scan
         * @param hashedSeriesInstanceUid  pseudonymised series UID (PSERID) for this scan
         * @param scanLabel the GIFT-Cloud scan label
         */
        public void addScanLabel(final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid, final GiftCloudLabel.ScanLabel scanLabel) throws IOException {
            if (!experimentAliasRecordMap.containsLabel(experimentLabel)) {
                throw new IOException("Experiment label not found");
            }
            experimentAliasRecordMap.getValueForLabel(experimentLabel).addScanAlias(hashedSeriesInstanceUid, scanLabel);;
        }

        /**
         * Stores details of experiments (studies) which have been uploaded, for memory cache storage on the uploading client
         */
        public static class ExperimentAliasRecord {
            private final GiftCloudLabel.ExperimentLabel experimentLabel;
            private final String anonymisedUid;
            private final LabelUidMap<GiftCloudLabel.ScanLabel, ScanAliasRecord> scanAliasRecordMap = new LabelUidMap<GiftCloudLabel.ScanLabel, ScanAliasRecord>();

            /**
             * Creates a new subject record for local storage
             *
             * @param anonymisedUid the pseudonymised study ID (PSTUID), which is a one-way hash of the study instance UID
             * @param experimentLabel the GIFT-Cloud experiment alias which will be visible on the server
             */
            public ExperimentAliasRecord(final String anonymisedUid, final GiftCloudLabel.ExperimentLabel experimentLabel) {
                this.anonymisedUid = anonymisedUid;
                this.experimentLabel = experimentLabel;
            }

            /**
             * Returns the GIFT-Cloud label for this experiment
             *
             * @return the GIFT-Cloud label
             */
            public GiftCloudLabel.ExperimentLabel getExperimentLabel() {
                return experimentLabel;
            }

            /**
             * @return the pseudonymised study UID (PSTUID), which is a one-way hash of the study instance UID
             */
            public String getExperimentAnonymisedUid() {
                return anonymisedUid;
            }

            /**
             * Determines if another object refers to the same experiment alias
             *
             * @param otherOb the object to compare to this one
             * @return true if the other object is an ExperimentAliasRecord referring to the same experiment alias
             */
            @Override public boolean equals(Object otherOb) {
                if (this == otherOb) return true;

                if (!(otherOb instanceof ExperimentAliasRecord)) return false;

                final ExperimentAliasRecord other = (ExperimentAliasRecord)otherOb;

                return this.anonymisedUid.equals(other.anonymisedUid) && this.experimentLabel.equals(other.experimentLabel);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(anonymisedUid, experimentLabel);
            }


            /**
             * Returns the GIFT-Cloud scan label corresponding to a pseudonymised scan UID
             *
             * @param hashedSeriesInstanceUid pseudonymised scan UID
             * @return an Optional which contains the scan label if the UID was found, or empty if it was not found
             */
            public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final String hashedSeriesInstanceUid) {
                if (!scanAliasRecordMap.containsUid(hashedSeriesInstanceUid)) {
                    return Optional.empty();
                } else {
                    return Optional.of(scanAliasRecordMap.getValueForUid(hashedSeriesInstanceUid).getScanLabel());
                }
            }

            /**
             * Adds a new GIFT-Cloud label for a particular scan
             * @param hashedSeriesInstanceUid  pseudonymised series UID (PSERID) for this scan
             * @param scanLabel the GIFT-Cloud scan label
             */
            public void addScanAlias(final String hashedSeriesInstanceUid, final GiftCloudLabel.ScanLabel scanLabel) {
                scanAliasRecordMap.put(scanLabel, hashedSeriesInstanceUid, new ScanAliasRecord(hashedSeriesInstanceUid, scanLabel));
            }

            /**
             * Stores details of scans which have been uploaded.
             */
            public static class ScanAliasRecord {
                private final String pserid;
                private final GiftCloudLabel.ScanLabel scanLabel;

                /**
                 * Creates a new scan record for memory storage
                 *
                 * @param pserid    the pseudonymised series ID (PSERID), which is a one-way hash of the patient ID
                 * @param scanLabel the GIFT-Cloud scan label which will be visible on the server
                 */
                public ScanAliasRecord(final String pserid, final GiftCloudLabel.ScanLabel scanLabel) {
                    this.pserid = pserid;
                    this.scanLabel = scanLabel;
                }

                /**
                 * @return the GIFT-Cloud scan label
                 */
                public GiftCloudLabel.ScanLabel getScanLabel() {
                    return scanLabel;
                }

                /**
                 * @return the pseudonymised series UID (PSERID), which is a one-way hash of the series instance uid
                 */
                public String getPserid() {
                    return pserid;
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

                    if (!(otherOb instanceof ScanAliasRecord)) return false;

                    final ScanAliasRecord other = (ScanAliasRecord) otherOb;

                    return this.pserid.equals(other.pserid) && this.scanLabel.equals(other.scanLabel);
                }

                @Override
                public int hashCode() {
                    return Objects.hashCode(pserid, scanLabel);
                }
            }
        }
    }
}
