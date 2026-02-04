package org.zfin.infrastructure;

import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.*;
import java.util.*;

/**
 * PatriciaTrie-based multimap using Apache Commons Collections 4.
 * Uses composition instead of inheritance to avoid JDK 21 SequencedMap compatibility issues.
 */
public class PatriciaTrieMultiMap<V> implements Map<String, Set<V>>, Serializable {

    private static final long serialVersionUID = 8774050446900697694L;

    private transient PatriciaTrie<Set<V>> trie;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(new HashMap<>(trie));
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Map<String, Set<V>> map = (Map<String, Set<V>>) in.readObject();
        this.trie = new PatriciaTrie<>(map);
    }

    public PatriciaTrieMultiMap() {
        this.trie = new PatriciaTrie<>();
    }

    public PatriciaTrieMultiMap(Map<String, Set<V>> map) {
        this.trie = new PatriciaTrie<>(map);
    }

    /**
     * There is an issue reserializing, but rebuilding seems to work, so that is what we will do.
     */
    public void rebuild() {
        Map<String, Set<V>> map = new HashMap<String, Set<V>>(trie.size());
        for (String key : trie.keySet()) {
            map.put(key, trie.get(key));
        }
        trie.clear();
        trie.putAll(map);
    }

    public V put(String s, V t) {
        Set<V> values = trie.get(s);
        if (values != null) {
            values.add(t);
        } else {
            Set<V> set = new HashSet<>();
            set.add(t);
            trie.put(s, set);
        }
        return t;
    }

    @Override
    public Set<V> get(Object key) {
        return trie.get(key);
    }

    @Override
    public Set<V> put(String key, Set<V> value) {
        return trie.put(key, value);
    }

    @Override
    public Set<V> remove(Object key) {
        return trie.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Set<V>> map) {
        trie.putAll(map);
    }

    @Override
    public void clear() {
        trie.clear();
    }

    @Override
    public int size() {
        return trie.size();
    }

    @Override
    public boolean isEmpty() {
        return trie.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return trie.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return trie.containsValue(value);
    }

    @Override
    public Set<String> keySet() {
        return trie.keySet();
    }

    @Override
    public Collection<Set<V>> values() {
        return trie.values();
    }

    @Override
    public Set<Entry<String, Set<V>>> entrySet() {
        return trie.entrySet();
    }

    /**
     * Returns a view of this map of entries with keys starting with the given prefix.
     * @param prefix The prefix to search for
     * @return A SortedMap of entries with keys starting with the prefix
     */
    public SortedMap<String, Set<V>> getPrefixedBy(String prefix) {
        return trie.prefixMap(prefix);
    }

    /**
     * @param prefix Query string.
     * @return The set of values that the prefix hit.
     */
    public Set<V> getSuggestedValues(String prefix) {
        Set<V> terms = new HashSet<V>();

        for (Set<V> termSet : trie.prefixMap(prefix).values()) {
            for (V term : termSet) {
                terms.add(term);
            }
        }

        return terms;
    }

    /**
     * Returns the flattened unique values in the map.
     *
     * @return
     */
    public Set<V> getAllValues() {
        Set<V> valueSet = new HashSet<V>();
        for (Set<V> values : trie.values()) {
            valueSet.addAll(values);
        }
        return valueSet;
    }

    /**
     * @param v
     * @return
     */
    public boolean containsValueInAllValues(V v) {
        for (V value : getAllValues()) {
            if (v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public List<V> getListOfValues() {
        List<V> valueSet = new ArrayList<V>();
        for (Set<V> values : trie.values())
            valueSet.addAll(values);
        return valueSet;
    }
}