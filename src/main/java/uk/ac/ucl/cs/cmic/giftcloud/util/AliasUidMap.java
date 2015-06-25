package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing a map which can be indexed by two different keys, a "uid" and "alias"
 * @param <V> the value type of the map
 */
public class AliasUidMap<V> {
    private final Map<String, V> uidMap = new HashMap<String, V>();
    private final Map<String, V> aliasMap = new HashMap<String, V>();

    /**
     * Returns the value for this alias key
     * @param alias
     * @return
     */
    public V getValueForAlias(final String alias) {
        return aliasMap.get(alias);
    }

    /**
     * Returns the value for this uid key
     * @param uid
     * @return
     */
    public V getValueForUid(final String uid) {
        return uidMap.get(uid);
    }

    /**
     * Adds a new value
     *
     * @param alias the alias key
     * @param uid the uid key
     * @param value the value
     */
    public void put(final String alias, final String uid, final V value) {
        aliasMap.put(alias, value);
        uidMap.put(uid, value);
    }

    /**
     * Determines if the map contains a value with the given uid key
     * @param uid
     * @return true if the given uid key exists
     */
    public boolean containsUid(final String uid) {
        return uidMap.containsKey(uid);
    }

    /**
     * Determines if the map contains a value with the given alias key
     * @param alias
     * @return true if the given alias key exists
     */
    public boolean containsAlias(final String alias) {
        return aliasMap.containsKey(alias);
    }

    /**
     * @return the map of uid keys to values
     */
    public Map<String, V> getUidMap() {
        return uidMap;
    }

    /**
     * @return the map of alias keys to values
     */
    public Map<String, V> getAliasMap() {
        return aliasMap;
    }
}