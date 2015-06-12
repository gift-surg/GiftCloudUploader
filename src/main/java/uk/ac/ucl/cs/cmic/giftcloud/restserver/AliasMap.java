package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores a mapping of hashed patient IDs to patient records
 */
public class AliasMap {
    private final Map<String, AliasRecord> aliasRecordMap = new HashMap<String, AliasRecord>();

    /**
     * Determine if a record already exists for this hashed patient ID
     *
     * @param hashedPatientId the unique map key, ie a hashed patient ID
     * @return true if an entry already exists for the subject
     */
    public boolean containsKey(final String hashedPatientId) {
        return aliasRecordMap.containsKey(hashedPatientId);
    }

    /**
     * Returns the GIFT-Cloud alias for the specified hashed patient ID
     *
     * @param hashedPatientId the unique map key specified by a hashed patient ID
     * @return an Optional of the alias for this hashed patient ID, or an empty optional if no record exists for this hashed patient ID
     */
    public Optional<String> getAlias(final String hashedPatientId) {
        if (aliasRecordMap.containsKey(hashedPatientId)) {
            return Optional.of(aliasRecordMap.get(hashedPatientId).getAlias());
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
        aliasRecordMap.put(hashedPatientId, new AliasRecord(hashedPatientId, alias, patientId, patientName));
    }

    /**
     * Returns the map of record maps
     *
     * @return the map of alias records
     */
    public Map<String, AliasRecord> getMap() {
        return aliasRecordMap;
    }

    /**
     * Compares another object to this AliasMap
     *
     * @param otherOb the other object
     * @return true if the other object is an AliasMap for the same subject
     */
    @Override public boolean equals(Object otherOb) {
        if (this == otherOb) return true;

        if (!(otherOb instanceof AliasMap)) return false;

        final AliasMap other = (AliasMap)otherOb;

        return other.getMap().equals(this.getMap());
    }

    /**
     * Stores details of subjects which have been uploaded, for local storage on the uploading client
     */
    public static class AliasRecord {
        private final String ppid;
        private final String alias;
        private final String patientId;
        private final String patientName;

        /**
         * Creates a new subject record for local storage
         *
         * @param ppid the pseudonymised patient ID (PPID), which is a one-way hash of the patient ID
         * @param alias the GIFT-Cloud alias which will be visible on the server
         * @param patientId the real patient ID, which will only be stored locally
         * @param patientName the real patient name, which will only be stored locally
         */
        public AliasRecord(final String ppid, final String alias, final String patientId, final String patientName) {
            this.ppid = ppid;
            this.alias = alias;
            this.patientId = patientId;
            this.patientName = patientName;
        }

        /**
         * @return the GIFT-Cloud alias
         */
        public String getAlias() {
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
         * @return true if the other object is an AliasMap referring to the same patient alias
         */
        @Override public boolean equals(Object otherOb) {
            if (this == otherOb) return true;

            if (!(otherOb instanceof AliasRecord)) return false;

            final AliasRecord other = (AliasRecord)otherOb;

            return this.ppid.equals(other.ppid) && this.alias.equals(other.alias) && this.patientId.equals(other.patientId) && this.patientName.equals(other.patientName);
        }

    }

}
