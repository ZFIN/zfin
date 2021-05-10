package org.zfin.infrastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * From here:
 * http://whiteboxcomputing.com/java/prefix_tree/
 */
public class Trie implements Serializable {
    private Node root;
    private int size;
    private int maxDepth; // Not exact, but bounding for the maximum
    private boolean ignoreCase;

    /**
     * The delimiter used in this word to tell where words end. Without a proper delimiter either A.
     * a lookup for 'win' would return false if the list also contained 'windows', or B. a lookup
     * for 'mag' would return true if the only word in the list was 'magnolia'
     * <p/>
     * The delimiter should never occur in a word added to the trie.
     */
    public final transient static char DELIMITER = '\u0001';

    /**
     * Creates a new case sensitive Trie
     */
    public Trie() {
        this(false);
    }

    /**
     * Creates a new Trie.
     *
     * @param ignoreCase Set this to true to make the trie ignore case (warning: this slows down
     *                   performance considerably!)
     */
    public Trie(boolean ignoreCase) {
        root = new Node('r');
        size = 0;
        this.ignoreCase = ignoreCase;
    }

    /**
     * Adds a word to the list.
     *
     * @param word The word to add.
     * @return True if the word wasn't in the list yet
     */
    public boolean add(String word) {
        if (word.length() == 0)
            throw new IllegalArgumentException("Word can't be empty");
        String w = (ignoreCase) ? word.toLowerCase() : word;
        if (add(root, w + DELIMITER, 0)) {
            size++;
            int n = word.length();
            if (n > maxDepth) maxDepth = n;
            return true;
        }
        return false;
    }

    /*
     * Does the real work of adding a word to the trie
     */

    private boolean add(Node root, String word, int offset) {
        if (offset == word.length()) return false;
        int c = word.charAt(offset);

        // Search for node to add to
        Node last = null, next = root.firstChild;
        while (next != null) {
            if (next.value < c) {
                // Not found yet, continue searching
                last = next;
                next = next.nextSibling;
            } else if (next.value == c) {
                // Match found, add remaining word to this node
                return add(next, word, offset + 1);
            }
            // Because of the ordering of the list getting here means we won't
            // find a match
            else break;
        }

        // No match found, create a new node and insert
        Node node = new Node(c);
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
            node.firstChild = new Node(word.charAt(i));
            node = node.firstChild;
        }
        return true;
      }

    /**
     * Removes a word from the list.
     *
     * @param word The word to remove.
     * @return True if the word was found and removed.
     */
    public boolean remove(String word) {
        if (word.length() == 0)
            throw new IllegalArgumentException("Word can't be empty");
        String w = (ignoreCase) ? word.toLowerCase() : word;
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

    private boolean remove(Node root, String word, int offset, Node branch, Node branchLast, Node branchNext) {
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
        Node last = null, next = root.firstChild;
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

    private boolean isEntry(Node root, String word, int offset) {
        if (offset == word.length()) return true;
        int c = word.charAt(offset);

        // Search for node to add to
        Node next = root.firstChild;
        while (next != null) {
            if (next.value < c) next = next.nextSibling;
            else if (next.value == c) return isEntry(next, word, offset + 1);
            else return false;
        }
        return false;
    }

    /**
     * Returns all words in the list. If (ignoreCase=true) all words are returned in lower case. For
     * large lists this will be a fairly slow operation.
     *
     * @return A String array of all words in the list
     */
    public String[] getAll() {
        ArrayList<String> words = new ArrayList<String>(size);
        char[] chars = new char[maxDepth];
        getAll(root, words, chars, 0);
        return words.toArray(new String[size]);
    }

    /*
     * Adds any words found in this branch to the array
     */

    private void getAll(Node root, ArrayList<String> words, char[] chars, int pointer) {
        Node n = root.firstChild;
        while (n != null) {
            if (n.firstChild == null) {
                words.add(new String(chars, 0, pointer));
            } else {
                chars[pointer] = (char) n.value;
                getAll(n, words, chars, pointer + 1);
            }
            n = n.nextSibling;
        }
    }

    /**
     * Returns the size of this list;
     */
    public int size() {
        return size;
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

    private String[] suggest(Node root, String word, int offset) {
        // this is the end condition
        if (offset == word.length()) {
            ArrayList<String> words = new ArrayList<String>(size);
            char[] chars = new char[maxDepth];
            for (int i = 0; i < offset; i++)
                chars[i] = word.charAt(i);
            getAll(root, words, chars, offset);
            return words.toArray(new String[words.size()]);
        }
        int c = word.charAt(offset);

        // Search for node to add to
        Node next = root.firstChild;
        while (next != null) {
            if (next.value < c) next = next.nextSibling;
            else if (next.value == c) return suggest(next, word, offset + 1);
            else break;
        }
        // I thikn that this should be, otherwise it will return itself even if its not there:
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

    private int longestMatch(Node root, String word, int offset, int depth, int maxFound) {
        // Uses delimiter = first in the list!
        Node next = root.firstChild;
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

    /*
     * Represents a node in the trie. Because a node's children are stored in a linked list this
     * data structure takes the odd structure of node with a firstChild and a nextSibling.
     */

    private class Node implements Serializable{
        public int value;
        public Node firstChild;
        public Node nextSibling;

        public Node(int value) {
            this.value = value;
            firstChild = null;
            nextSibling = null;
        }
    }

    transient final static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateRandomWord() {
        int length = (int) (Math.random() * 20f) + 12;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int character = (int) (Math.random() * 26);
            sb.append(alphabet.substring(character, character + 1));
        }
        return sb.toString();
    }


    public static void main(String args[]) {
        Trie trie = new Trie();
        HashSet<String> hashTest = new HashSet<String>();
        int length = 100000;
        for (int i = 0; i < length; i++) {
            String word = Trie.generateRandomWord();
            trie.add(word);
            hashTest.add(word);
        }

        System.out.println("total length: " + trie.getAll().length);

        double totalTrieTime = 0f;
        double totalHashSetTime = 0f;

        for (int i = 0; i < 100; i++) {
            String testWord = trie.getAll()[(int) (Math.random() * length)];
            testWord = testWord.substring(0, 5);
//            System.out.println("testWord: " + testWord) ;
            double startTime = System.currentTimeMillis();
            String[] results = trie.suggest(testWord);
            double finishTime = System.currentTimeMillis();
//            System.out.println("Trie time (ms): " + String.valueOf(finishTime-startTime)) ;
            totalTrieTime += finishTime - startTime;
//            System.out.println("# of results: "+ results.length) ;
//        for(String s : results){
//            System.out.println(s) ;
//        }

            List<String> hits = new ArrayList<String>();
            startTime = System.currentTimeMillis();
            for (String s : hashTest) {
                if (s.equals(testWord)) {
                    hits.add(s);
                } else if (s.startsWith(testWord)) {
                    hits.add(s);
                }
            }

            finishTime = System.currentTimeMillis();
//            System.out.println("Hash time (ms): " + String.valueOf(finishTime-startTime)) ;
            totalHashSetTime += finishTime - startTime;
//            System.out.println("hits: "+ hits.size()) ;
            if (hits.size() != results.length) {
                System.out.println("results don't match");
                return;
            }
            assert (hits.size() == results.length);
        }

        System.out.println("\ntrieTime " + totalTrieTime + "(ms) vs hashTime: " + totalHashSetTime + " (ms)");
    }

}
