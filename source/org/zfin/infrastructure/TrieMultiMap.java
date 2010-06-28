package org.zfin.infrastructure;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;

import java.io.Serializable;

/**
 * From here:
 * http://whiteboxcomputing.com/java/prefix_tree/
 * Applied AbstractMap<V> to Trie class, adding a <Node>
 */
public class TrieMultiMap<V extends Collection>
//        extends AbstractMap<String, V>
        implements Serializable
        , MultiMap // unable to define methods as String even though paramaterized as such
{
    private int maxDepth; // Not exact, but bounding for the maximum
    private boolean ignoreCase = true ;

//    private transient Set<Entry<String,V>> entrySet = null;
//    private transient KeySet<String> navigableKeySet = null;
    /**
     * The root
     */
    private Node<V> root;
    /**
     * the number of entries in a tree
     */
    private int size;
    /**
     * The number of structural modifications to the tree.
     */
    private transient int modCount = 0;

    /**
     * The delimiter used in this word to tell where words end. Without a proper delimiter either A.
     * a lookup for 'win' would return false if the list also contained 'windows', or B. a lookup
     * for 'mag' would return true if the only word in the list was 'magnolia'
     * <p/>
     * The delimiter should never occur in a word added to the trie.
     */
    public final static char DELIMITER = '\u0001';

    /**
     * Creates a new case sensitive Trie
     */
    public TrieMultiMap() {
        this(true);
    }

    /**
     * Creates a new Trie.
     *
     * @param ignoreCase Set this to true to make the trie ignore case (warning: this slows down
     *                   performance considerably!)
     */
    public TrieMultiMap(boolean ignoreCase) {
        root = new Node<V>('r');
        size = 0;
        this.ignoreCase = ignoreCase;
    }


    @Override
    public Object put(Object key, Object value) {
        if(value instanceof Collection){
            put(key.toString(),(V) value) ;
        }
        else{
            V values = get(key.toString()) ;
            if(values!=null){
                values.add(value) ;
            }
            else{
                Set set = new HashSet();
                set.add(value) ;
                put(key.toString(),set) ;
            }
        }
        return value ;
    }

    /**
     * Adds a word to the list.
     *
     * @param word  The word to add.
     * @param value to add.
     * @return True if the word wasn't in the list yet
     */
    public V put(String word, V value) {
        if (word.length() == 0)
            throw new IllegalArgumentException("Word can't be empty");
        String w = (ignoreCase) ? word.toLowerCase() : word;
        if (put(root, w + DELIMITER, 0, value)) {
            size++;
            int n = word.length();
            if (n > maxDepth) maxDepth = n;
            return value;
        }
        return null;
    }

    /*
     * Does the real work of adding a word to the trie
     */

    private boolean put(Node<V> root, String word, int offset, V value) {
        if (offset == word.length()) return false;
        int c = word.charAt(offset);

        // Search for node to add to
        Node<V> last = null, next = root.firstChild;
        while (next != null) {
            if (next.value < c) {
                // Not found yet, continue searching
                last = next;
                next = next.nextSibling;
            } else if (next.value == c) {
                // Match found, add remaining word to this node
                return put(next, word, offset + 1, value);
            }
            // Because of the ordering of the list getting here means we won't
            // find a match
            else break;
        }

        // No match found, create a new node and insert
        Node<V> node = new Node<V>(c, value);
        if (last == null) {
            // Insert node at the beginning of the list (Works for next == null
            // too)
            root.firstChild = node;
            node.nextSibling = next;
        } else {
            // Insert between last and next
            last.nextSibling = node;
            node.nextSibling = next;
        }

        // Add remaining letters
        for (int i = offset + 1; i < word.length(); i++) {
            node.firstChild = new Node<V>(word.charAt(i), value);
            node = node.firstChild;
        }
        return true;
    }


    /**
     * Returns the value removed.
     * @param key Key that it must contain.
     * @param value Value to remove
     * @return Value removed by specified key.
     */
    @Override
    public Object remove(Object key, Object value) {
        V values = get(key) ;
        if(values!=null){
            values.remove(value) ;
        }

        return null ;
    }

    @Override
    public V remove(Object key) {
        V value = get(key) ;
        if(value!=null){
            if(remove(key.toString())){
                return value ;
            }
            else{
                return null ;
            }
        }
        return null ;
    }

    /**
     * Removes a word from the list.
     *
     * @param key The word to remove.
     * @return True if the word was found and removed.
     *
     * // this method roughly shadows remove(Object), which returns the value removd.
     */
    public boolean remove(String key) {
        if (key.length() == 0)
            throw new IllegalArgumentException("Word can't be empty");
        String w = (ignoreCase) ? key.toLowerCase() : key;
        if (remove(root, w + DELIMITER, 0, null, null, null)) {
            size--;
            return true;
        }
        return false;
    }

    /*
     * Removes a word from the list: searches for the word while retaining information about the
     * last time it encountered a tree branch, and which branch of the tree it followed...
     */

    private boolean remove(Node<V> root, String word, int offset, Node<V> branch, Node<V> branchLast, Node<V> branchNext) {
        if (offset == word.length()) {
            // Word found, delete entry at last branch
            if (branch == null) {
                // No branches found in the tree, only one word!
                this.root.firstChild = null;
            } else {
                if (branchLast == null) branch.firstChild = branchNext;
                else branchLast.nextSibling = branchNext;
            }
            return true;
        }

        // Search for word
        int c = word.charAt(offset);
        Node<V> last = null, next = root.firstChild;
        while (next != null) {
            if (next.value < c) {
                last = next;
                next = next.nextSibling;
            } else if (next.value == c) {
                // Test if this node had more than one child
                if (last != null || next.nextSibling != null) {
                    branch = root;
                    branchLast = last;
                    branchNext = next.nextSibling;
                }
                return remove(next, word, offset + 1, branch, branchLast,
                        branchNext);
            } else return false;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return get(key.toString()) ;
    }

    public V get(String key) {
        if (key == null || false == isEntry(key.toString())) {
            return null;
        }
        Node<V> node = getEntry(key.toString());
        return node.getPayload();
    }


    /**
     * Searches for a word in the list.
     *
     * @param word The word to search for.
     * @return True if the word was found.
     */
    public boolean isEntry(String word) {
        if (word.length() == 0)
            throw new IllegalArgumentException("Word can't be empty");
        String w = (ignoreCase) ? word.toLowerCase() : word;
        return isEntry(root, w + DELIMITER, 0);
    }

    /*
     * Does the real work of determining if a word is in the list
     */

    private boolean isEntry(Node<V> root, String word, int offset) {
        if (offset == word.length()) return true;
        int c = word.charAt(offset);

        // Search for node to add to
        Node<V> next = root.firstChild;
        while (next != null) {
            if (next.value < c) next = next.nextSibling;
            else if (next.value == c) return isEntry(next, word, offset + 1);
            else return false;
        }
        return false;
    }

    /**
     * Searches for a word in the list.
     *
     * @param word The word to search for.
     * @return True if the word was found.
     */
    public Node<V> getEntry(String word) {
        if (word.length() == 0)
            throw new IllegalArgumentException("Word can't be empty");
        String w = (ignoreCase) ? word.toLowerCase() : word;
        return getEntry(root, w + DELIMITER, 0);
    }

    /*
    * Does the real work of determining if a word is in the list
    */

    private Node<V> getEntry(Node<V> root, String word, int offset) {
        if (offset == word.length()) return root;
        int c = word.charAt(offset);

        // Search for node to add to
        Node<V> next = root.firstChild;
        while (next != null) {
            if (next.value < c) next = next.nextSibling;
            else if (next.value == c) return getEntry(next, word, offset + 1);
            else return null;
        }
        return null;
    }

    /**
     * Returns all words in the list. If (ignoreCase=true) all words are returned in lower case. For
     * large lists this will be a fairly slow operation.
     *
     * @return A String array of all words in the list
     */
    public Set<String> keySet() {
        Set<String> words = new TreeSet<String>();
        char[] chars = new char[maxDepth];
        keySet(root, words, chars, 0);
        return words;
    }

    public Set<String> getKeys(){
        return keySet();
    }

    // convenience accessor
    public Set getAllValues(){
        return (Set) values();
    }

    public Collection values(){
        Set<V> values = new HashSet<V>(size);
//        char[] chars = new char[maxDepth];
        values(root, values, null, 0);

        // converting so that we don't return a collection, but the object, albeit unmapped
        Set<Object> returnValues = new TreeSet<Object>() ;
        for(V value : values){
            returnValues.addAll(value) ;
        }
        return returnValues;
    }


    /*
     * Adds any words found in this branch to the array
     */
    private void keySet(Node<V> root, Set<String> words, char[] chars, int pointer) {
        Node<V> n = root.firstChild;
        while (n != null) {
            if (n.firstChild == null) {
                words.add(new String(chars, 0, pointer));
            } else {
                chars[pointer] = (char) n.value;
                keySet(n, words, chars, pointer + 1);
            }
            n = n.nextSibling;
        }
    }

    /*
    * Adds any words found in this branch to the array
    */
    private void values(Node<V> root, Set<V> values, V value, int pointer) {
        Node<V> n = root.firstChild;
        while (n != null) {
            if (n.firstChild == null) {
                values.add(value);
            } else {
                V thisValue =  n.payload;
                values(n, values, thisValue, pointer + 1);
            }
            n = n.nextSibling;
        }
    }

    /**
     * Returns the size of this list;
     */
    @Override
    public int size() {
        return size;
    }

    // convenience method for accessors
    public int getSize(){
        return size() ;
    }



    /**
     * Returns all words in this list starting with the given prefix
     *
     * @param prefix The prefix to search for.
     * @return All words in this list starting with the given prefix, or if no such words are found,
     *         an array containing only the suggested prefix.
     */
    public String[] suggest(String prefix) {
        return suggest(root, prefix, 0);
    }

    /*
     * Recursive function for finding all words starting with the given prefix
     */

    private String[] suggest(Node<V> root, String word, int offset) {
        // this is the end condition
        if (offset == word.length()) {
            Set<String> words = new HashSet<String>(size);
            char[] chars = new char[maxDepth];
            for (int i = 0; i < offset; i++){
                chars[i] = word.charAt(i);
            }
            keySet(root, words, chars, offset);
            return words.toArray(new String[words.size()]);
        }
        int c = word.charAt(offset);

        // Search for node to add to
        Node<V> next = root.firstChild;
        while (next != null) {
            if (next.value < c) next = next.nextSibling;
            else if (next.value == c) return suggest(next, word, offset + 1);
            else break;
        }
        // I think that this should be, otherwise it will return itself even if its not there:
//        return new String[] { };
        return new String[]{word};
    }

    /**
     * Searches a string for words present in the trie and replaces them with stars (asterixes).
     *
     * @param s The string to censor
     */
    public String censor(String s) {
        if (size == 0) return s;
        String z = s.toLowerCase();
        int n = z.length();
        StringBuilder buffer = new StringBuilder(n);
        int match;
        char star = '*';
        for (int i = 0; i < n;) {
            match = longestMatch(root, z, i, 0, 0);
            if (match > 0) {
                for (int j = 0; j < match; j++) {
                    buffer.append(star);
                    i++;
                }
            } else {
                buffer.append(s.charAt(i++));
            }
        }
        return buffer.toString();
    }

    /*
     * Finds the longest matching word in the trie that starts at the given offset...
     */

    private int longestMatch(Node<V> root, String word, int offset, int depth, int maxFound) {
        // Uses delimiter = first in the list!
        Node<V> next = root.firstChild;
        if (next.value == DELIMITER) maxFound = depth;
        if (offset == word.length()) return maxFound;
        int c = word.charAt(offset);

        while (next != null) {
            if (next.value < c) next = next.nextSibling;
            else if (next.value == c) return longestMatch(next, word,
                    offset + 1, depth + 1, maxFound);
            else return maxFound;
        }
        return maxFound;
    }

    public boolean isEmpty() {
        return size==0 ;
    }

    public void putAll(TrieMultiMap<V> map) {
        for(String key: map.keySet()){
            put(key.toString(),map.get(key)) ;
        }
    }

    @Override
    public void putAll(Map map) {
        for(Object key: map.keySet()){
            put(key.toString(),map.get(key)) ;
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(key.toString()) ;
    }

    public boolean containsKey(String key1) {
        return get(key1)!=null ;
    }


    // bad O(N^2)
    @Override
    public boolean containsValue(Object value) {
        Collection values = values() ;
        for(Object o : values){
            if(o.equals(value)){
                return true ;
            }
        }
        return false ;
    }

    // doe this create memory leaks?
    public void clear() {
        root = new Node<V>('r');
        size = 0;
    }



//    @Override

    /**
     * TODO: make the entrySet implementation work properly
     * @deprecated This does not work like a Map entrySet, as it does not support functions on the map.
     * @return
     */
    public Set<Map.Entry<String,V>> entrySet() {
        Set<Map.Entry<String,V>> entries = new HashSet<Map.Entry<String,V>>() ;
        for(String key: keySet()){
            entries.add(new Entry<V>(key,get(key))) ;
        }
        return entries;
    }

    static final class Entry<V> implements Map.Entry<String,V>{

        String key;
        V value ;

        Entry(String key, V value){
            this.key = key ;
            this.value = value ;
        }

        @Override
        public String getKey() {
            return key ;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public V getValue() {
            return value ;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public V setValue(V value) {
            return this.value = value ;
        }
    }

    /*
     * Represents a node in the trie. Because a node's children are stored in a linked list this
     * data structure takes the odd structure of node with a firstChild and a nextSibling.
     */

    private class Node<V> implements Serializable {
        public int value;
        public V payload;
        public Node<V> firstChild;
        public Node<V> nextSibling;

        public Node(int value) {
            this.value = value;
            firstChild = null;
            nextSibling = null;
        }

        public Node(int value, V payload) {
            this.value = value;
            firstChild = null;
            nextSibling = null;
            this.payload = payload;
        }

        public V getPayload() {
            return payload;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrieMultiMap)) return false;

        TrieMultiMap trieMap = (TrieMultiMap) o;

        if (ignoreCase != trieMap.ignoreCase) return false;
        if (maxDepth != trieMap.maxDepth) return false;
        if (size != trieMap.size) return false;
        if (root != null ? !root.equals(trieMap.root) : trieMap.root != null) return false;

        if(!CollectionUtils.isEqualCollection(entrySet(),trieMap.entrySet())) return false ;

        return true;
    }

    @Override
    public int hashCode() {
        int result = maxDepth;
        result = 31 * result + (ignoreCase ? 1 : 0);
        result = 31 * result + (root != null ? root.hashCode() : 0);
        result = 31 * result + size;

        for(Map.Entry<String,V> entry : entrySet()){
            result = 31 * result + entry.getKey().hashCode() ;
            result = 31 * result + entry.getValue().hashCode();
        }

        return result;
    }
}

