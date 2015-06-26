package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.AliasUidMap;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Stores a mapping of hashed patient IDs and patient aliases to patient records
 */
public class PatientAliasMap {
    private final AliasUidMap<PatientAliasRecord> aliasRecordMap = new AliasUidMap<PatientAliasRecord>();

    /**
     * Determine if a record already exists for this hashed patient ID
     *
     * @param hashedPatientId the unique map key, ie a hashed patient ID
     * @return true if an entry already exists for the subject
     */
    public boolean containsPpid(final String hashedPatientId) {
        return aliasRecordMap.containsUid(hashedPatientId);
    }

    /**
     * Returns the GIFT-Cloud alias for the specified hashed patient ID
     *
     * @param hashedPatientId the unique map key specified by a hashed patient ID
     * @return an Optional of the alias for this hashed patient ID, or an empty optional if no record exists for this hashed patient ID
     */
    public Optional<String> getAlias(final String hashedPatientId) {
        if (aliasRecordMap.containsUid(hashedPatientId)) {
            return Optional.of(aliasRecordMap.getValueForUid(hashedPatientId).getPatientAlias());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Adds a new record for a new subject
     *
     * @param hashedPatientId the pseudonymised patient ID (PPID), which is a one-way hash of the patient ID
     * @param alias the GIFT-Cloud alias which will be visible on the server
     * @param patientId the real patient ID, which will only be stored locally
     * @param patientName the real patient name, which will only be stored locally
     */
    public void addAlias(final String hashedPatientId, final String alias, final String patientId, final String patientName) {
        aliasRecordMap.put(alias, hashedPatientId, new PatientAliasRecord(hashedPatientId, alias, patientId, patientName));
    }

    /**
     * Returns the map of record maps
     *
     * @return the map of alias records
     */
    public Map<String, PatientAliasRecord> getMap() {
        return aliasRecordMap.getUidMap();
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

    /**
     * Gets the GIFT-CLoud alias for an experiment
     *
     * @param subjectAlias the GIFT-Cloud alias for the subject
     * @param hashedStudyInstanceUid a one-way hash of the study instance UID
     * @return an Optional, set to the experiment alias if it exists
     */
    public Optional<String> getExperimentAlias(final String subjectAlias, final String hashedStudyInstanceUid) {
        if (aliasRecordMap.containsAlias(subjectAlias)) {
            return Optional.empty();
        } else {
            return aliasRecordMap.getValueForAlias(subjectAlias).getExperimentAlias(hashedStudyInstanceUid);
        }
    }

    /**
     * Gets the GIFT-Cloud alias for a scan
     *
     * @param subjectAlias the GIFT-Cloud alias for the subject
     * @param experimentAlias the GIFT-Cloud alias for the experiment
     * @param hashedSeriesInstanceUid a one-way hash of the series instance UID
     * @return an Optional, set to the scan alias if it exists
     */
    public Optional<String> getScanAlias(final String subjectAlias, final String experimentAlias, final String hashedSeriesInstanceUid) {
        if (aliasRecordMap.containsAlias(subjectAlias)) {
            return Optional.empty();
        } else {
            return aliasRecordMap.getValueForAlias(subjectAlias).getScanAlias(experimentAlias, hashedSeriesInstanceUid);
        }
    }

    /**
     * Adds a new GIFT-Cloud alias for an experiment
     *
     * @param subjectAlias the GIFT-Cloud alias for the subject
     * @param hashedStudyInstanceUid a one-way hash of the study instance UID
     * @param experimentAlias the GIFT-Cloud alias to add to this experiment
     * @throws IOException if the subject does not exist
     */
    public void addExperimentAlias(final String subjectAlias, final String hashedStudyInstanceUid, final String experimentAlias) throws IOException {
        if (aliasRecordMap.containsAlias(subjectAlias)) {
            throw new IOException("The subject alias was not found");
        }
        aliasRecordMap.getValueForAlias(subjectAlias).addExperimentAlias(hashedStudyInstanceUid, experimentAlias);
    }

    /**
     * Adds a GIFT-Cloud alias for a scan
     *
     * @param subjectAlias the GIFT-Cloud alias for the subject
     * @param experimentAlias the GIFT-Cloud alias for the experiment
     * @param hashedSeriesInstanceUid a one-way hash of the series instance UID
     * @param scanAlias the GIFT-Cloud alias to add to this scan
     * @throws IOException if the subject or experiment do not exist
     */
    public void addScanAlias(final String subjectAlias, final String experimentAlias, final String hashedSeriesInstanceUid, final String scanAlias) throws IOException {
        if (aliasRecordMap.containsAlias(subjectAlias)) {
            throw new IOException("The subject alias was not found");
        }
        aliasRecordMap.getValueForAlias(subjectAlias).addScanAlias(experimentAlias, hashedSeriesInstanceUid, scanAlias);
    }

    /**
     * Stores details of subjects which have been uploaded, for local storage on the uploading client
     */
    public static class PatientAliasRecord {
        private final String ppid;
        private final String alias;
        private final String patientId;
        private final String patientName;
        private final AliasUidMap<ExperimentAliasRecord> experimentAliasRecordMap = new AliasUidMap<ExperimentAliasRecord>();;

        /**
         * Creates a new subject record for local storage
         *
         * @param ppid the pseudonymised patient ID (PPID), which is a one-way hash of the patient ID
         * @param alias the GIFT-Cloud alias which will be visible on the server
         * @param patientId the real patient ID, which will only be stored locally
         * @param patientName the real patient name, which will only be stored locally
         */
        public PatientAliasRecord(final String ppid, final String alias, final String patientId, final String patientName) {
            this.ppid = ppid;
            this.alias = alias;
            this.patientId = patientId;
            this.patientName = patientName;
        }

        /**
         * @return the GIFT-Cloud alias
         */
        public String getPatientAlias() {
            return alias;
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
         * @return true if the other object is an PatientAliasMap referring to the same patient alias
         */
        @Override public boolean equals(Object otherOb) {
            if (this == otherOb) return true;

            if (!(otherOb instanceof PatientAliasRecord)) return false;

            final PatientAliasRecord other = (PatientAliasRecord)otherOb;

            return this.ppid.equals(other.ppid) && this.alias.equals(other.alias) && this.patientId.equals(other.patientId) && this.patientName.equals(other.patientName);
        }

        /**
         * Returns the GIFT-Cloud experiment label alias for a particualar scan
         *
         * @param hashedStudyInstanceUid pseudonymised study UID (PSID) for this scan
         * @return the experiment label
         */
        public Optional<String> getExperimentAlias(final String hashedStudyInstanceUid) {
            if (!experimentAliasRecordMap.containsUid(hashedStudyInstanceUid)) {
                return Optional.empty();
            }
            return Optional.of(experimentAliasRecordMap.getValueForUid(hashedStudyInstanceUid).getExperimentAlias());
        }

        /**
         * Returns the GIFT-Cloud scan label for a particualar scan
         *
         * @param experimentAlias GIFT-Cloud label for the experiment containing this can
         * @param hashedSeriesInstanceUid pseudonymised series UID (PSERID) for this scan
         * @return the scan label
         */
        public Optional<String> getScanAlias(final String experimentAlias, final String hashedSeriesInstanceUid) {
            if (!experimentAliasRecordMap.containsAlias(experimentAlias)) {
                return Optional.empty();
            }
            return experimentAliasRecordMap.getValueForAlias(experimentAlias).getScanAlias(hashedSeriesInstanceUid);
        }

        /**
         * Adds a new GIFT-Cloud alias for a particular experiment
         * @param hashedStudyInstanceUid  pseudonymised study UID (PSID) for this scan
         * @param experimentAlias the GIFT-Cloud experiment label
         */
        public void addExperimentAlias(final String hashedStudyInstanceUid, final String experimentAlias) {
            experimentAliasRecordMap.put(experimentAlias, hashedStudyInstanceUid, new ExperimentAliasRecord(hashedStudyInstanceUid, experimentAlias));
        }

        /**
         * Adds a new GIFT-Cloud label for a particular scan
         * @param hashedSeriesInstanceUid  pseudonymised series UID (PSERID) for this scan
         * @param scanAlias the GIFT-Cloud scan label
         */
        public void addScanAlias(final String experimentAlias, final String hashedSeriesInstanceUid, final String scanAlias) throws IOException {
            if (!experimentAliasRecordMap.containsAlias(experimentAlias)) {
                throw new IOException("experiment alias not found");
            }
            experimentAliasRecordMap.getValueForAlias(experimentAlias).addScanAlias(hashedSeriesInstanceUid, scanAlias);;
        }

        /**
         * Stores details of experiments (studies) which have been uploaded, for memory cache storage on the uploading client
         */
        public static class ExperimentAliasRecord {
            private final String psid;
            private final String experimentAlias;
            private final AliasUidMap<ScanAliasRecord> scanAliasRecordMap = new AliasUidMap<ScanAliasRecord>();

            /**
             * Creates a new subject record for local storage
             *
             * @param psid the pseudonymised study ID (PSID), which is a one-way hash of the patient ID
             * @param experimentAlias the GIFT-Cloud experiment alias which will be visible on the server
             */
            public ExperimentAliasRecord(final String psid, final String experimentAlias) {
                this.psid = psid;
                this.experimentAlias = experimentAlias;
            }

            /**
             * Returns the GIFT-Cloud label for this experiment
             *
             * @return the GIFT-Cloud label
             */
            public String getExperimentAlias() {
                return experimentAlias;
            }

            /**
             * @return the pseudonymised study ID (PSID), which is a one-way hash of the study instance UID
             */
            public String getPsid() {
                return psid;
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

                return this.psid.equals(other.psid) && this.experimentAlias.equals(other.experimentAlias);
            }

            /**
             * Returns the GIFT-Cloud scan label corresponding to a pseudonymised series UID (PSERID)
             *
             * @param hashedSeriesInstanceUid pseudonymised series UID (PSERID) for this scan
             * @return the scan label
             */
            public Optional<String> getScanAlias(final String hashedSeriesInstanceUid) {
                if (!scanAliasRecordMap.containsAlias(hashedSeriesInstanceUid)) {
                    return Optional.empty();
                } else {
                    return Optional.of(scanAliasRecordMap.getValueForUid(hashedSeriesInstanceUid).getScanAlias());
                }
            }

            /**
             * Adds a new GIFT-Cloud label for a particular scan
             * @param hashedSeriesInstanceUid  pseudonymised series UID (PSERID) for this scan
             * @param scanAlias the GIFT-Cloud scan label
             */
            public void addScanAlias(final String hashedSeriesInstanceUid, final String scanAlias) {
                scanAliasRecordMap.put(scanAlias, hashedSeriesInstanceUid, new ScanAliasRecord(hashedSeriesInstanceUid, scanAlias));
            }

            /**
             * Stores details of subjects which have been uploaded, for local storage on the uploading client
             */
            public static class ScanAliasRecord {
                private final String pserid;
                private final String scanAlias;

                /**
                 * Creates a new scan record for memory storage
                 *
                 * @param pserid    the pseudonymised series ID (PSERID), which is a one-way hash of the patient ID
                 * @param scanAlias the GIFT-Cloud experiment alias which will be visible on the server
                 */
                public ScanAliasRecord(final String pserid, final String scanAlias) {
                    this.pserid = pserid;
                    this.scanAlias = scanAlias;
                }

                /**
                 * @return the GIFT-Cloud scan alias
                 */
                public String getScanAlias() {
                    return scanAlias;
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

                    return this.pserid.equals(other.pserid) && this.scanAlias.equals(other.scanAlias);
                }
            }
        }
    }
}
