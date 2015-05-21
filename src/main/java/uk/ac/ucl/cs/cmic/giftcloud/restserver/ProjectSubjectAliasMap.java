package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.util.HashMap;
import java.util.Map;

public class ProjectSubjectAliasMap {
    private final Map<String, AliasMap> projectMap = new HashMap<String, AliasMap>();

    public AliasMap getAliasMapForProject(final String projectName) {
        if (!projectMap.containsKey(projectName)) {
            projectMap.put(projectName, new AliasMap());
        }
        return projectMap.get(projectName);
    }

    public class AliasMap {
        private final Map<String, AliasRecord> aliasRecordMap = new HashMap<String, AliasRecord>();

        public boolean containsKey(final String hashedPatientId) {
            return aliasRecordMap.containsKey(hashedPatientId);
        }

        public String getAlias(final String hashedPatientId) {
            AliasRecord aliasRecord = aliasRecordMap.get(hashedPatientId);
            return aliasRecord.getAlias();
        }

        public void addAlias(final String hashedPatientId, final String alias, final String patientId) {
            aliasRecordMap.put(hashedPatientId, new AliasRecord(hashedPatientId, alias, patientId));
        }

        public class AliasRecord {
            private final String ppid;
            private final String alias;
            private final String patientId;

            public AliasRecord(final String ppid, final String alias, final String patientId) {
                this.ppid = ppid;
                this.alias = alias;
                this.patientId = patientId;
            }

            public String getAlias() {
                return alias;
            }
        }
    }
}
