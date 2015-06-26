package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing a map which can be indexed by two different keys, a "label" and a "uid"
 * @param <V> the value type of the map
 */
public class LabelUidMap<K, V> {
    private final Map<K, V> labelMap = new HashMap<K, V>();
    private final Map<String, V> uidMap = new HashMap<String, V>();

    /**
     * Returns the value for this label key
     * @param label
     * @return
     */
    public V getValueForLabel(final K label) {
        return labelMap.get(label);
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
     * @param label the label key
     * @param uid the uid key
     * @param value the value
     */
    public void put(final K label, final String uid, final V value) {
        labelMap.put(label, value);
        uidMap.put(uid, value);
    }

    /**
     * Determines if the map contains a value with the given label key
     * @param label
     * @return true if the given label key exists
     */
    public boolean containsLabel(final K label) {
        return labelMap.containsKey(label);
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
     * @return the map of label keys to values
     */
    public Map<K, V> getLabelMap() {
        return labelMap;
    }

    /**
     * @return the map of uid keys to values
     */
    public Map<String, V> getUidMap() {
        return uidMap;
    }
}