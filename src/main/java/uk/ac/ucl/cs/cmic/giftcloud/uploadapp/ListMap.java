package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains an ordered map with efficient indexing
 */
public class ListMap<K, V> {
    private final Map<K, V> map = new HashMap<K, V >();
    private final Map<K, Integer> mapToIndex = new HashMap<K, Integer>();
    private final List<K> keyList = new ArrayList<K>();

    /**
     * Adds a new key-value pair
     * @param key a unique key that must not already exist
     * @param value the value for this key
     * @return the index of the newly created pair
     */
    public synchronized int put(final K key, final V value) {
        if (map.containsKey(key)) {
            throw new java.lang.IllegalArgumentException("Key already present");
        }
        map.put(key, value);
        keyList.add(key);
        int newIndex = keyList.size() - 1;
        mapToIndex.put(key, newIndex);
        return newIndex;
    }

    /**
     * Removes a key-value pair
     * @param key the key of the pair to remove
     */
    public synchronized void remove(final K key) {
        if (!map.containsKey(key)) {
            throw new java.lang.IllegalArgumentException("Key does not exist");
        }
        map.remove(key);
        keyList.remove(key);
        mapToIndex.clear();
        for (int index = 0; index < keyList.size(); index++) {
            mapToIndex.put(keyList.get(index), index);
        }
    }

    /**
     * Returns the value using an index lookup. This index could change if items are removed from the ListMap
     * @param index the index of the item to fetch
     * @return the value for the given index
     */
    public synchronized V getFromIndex(final int index) {
        if (index >= keyList.size()) {
            throw new java.lang.IllegalArgumentException("Index does not exist");
        }
        return map.get(keyList.get(index));
    }

    /**
     * Returns the value using a key lookup
     * @param key of the item to fetch
     * @return the value for the given key
     */
    public synchronized V getFromKey(final K key) {
        if (!map.containsKey(key)) {
            throw new java.lang.IllegalArgumentException("Key does not exist");
        }
        return map.get(key);
    }

    /**
     * @return the number of key-value pairs
     */
    public synchronized int size() {
        return map.size();
    }

    /**
     * @param key the key for the key-value pair
     * @return true if a key-value pair exists for the given key
     */
    public synchronized boolean containsKey(final K key) {
        return map.containsKey(key);
    }

    /**
     * @param key the key for the key-value pair
     * @return the array index of the key-value pair
     */
    public synchronized int getIndex(final K key) {
        if (!map.containsKey(key)) {
            throw new java.lang.IllegalArgumentException("Key does not exist");
        }
        return mapToIndex.get(key);
    }
}
