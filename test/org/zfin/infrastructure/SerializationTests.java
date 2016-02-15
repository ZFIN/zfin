package org.zfin.infrastructure;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.ontology.OntologyTokenizer;
import org.zfin.ontology.Term;
import org.zfin.util.FileUtil;

import java.io.File;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
/**
 */
public class SerializationTests {

    private final Logger logger = Logger.getLogger(SerializationTests.class);

    private OntologyTokenizer tokenizer = new OntologyTokenizer();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void serializeTerm() throws Exception {
        TermDTO termDTO = createTermWithName("key1 key2");
        File tempSerialFile = FileUtil.serializeObject(termDTO, tempFolder.newFile());
        TermDTO desieralizedTermDTO = (TermDTO) FileUtil.deserializeOntologies(tempSerialFile);
        assertThat(termDTO, equalTo(desieralizedTermDTO));
    }

    @Test
    public void memoryTest() throws Exception {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<>();
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("key1 key2", "key4"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("key1 key2 key3", "key4"), termMap);

        File tempSerialFile = FileUtil.serializeObject(termMap, tempFolder.newFile());
        PatriciaTrieMultiMap<TermDTO> deserializedMap = (PatriciaTrieMultiMap<TermDTO>) FileUtil.deserializeOntologies(tempSerialFile);

        assertThat(deserializedMap.keySet(), everyItem(isIn(termMap.keySet())));
        assertThat(deserializedMap.values(), everyItem(isIn(termMap.values())));
    }

    @Test
    public void serializationWorks() throws Exception {
        int numberOfTerms = 100;
        Collection<String> words = generateRandomWords(numberOfTerms);

        PatriciaTrieMultiMap<TermDTO> patriciaTrieMultiMap = new PatriciaTrieMultiMap<>();

        for (String word : words) {
            patriciaTrieMultiMap.put(word, createTermWithName(word));
            patriciaTrieMultiMap.put(word, createTermWithName(word + " " + word));
        }

        File tempSerialFile = FileUtil.serializeObject(patriciaTrieMultiMap, tempFolder.newFile());
        logger.debug("patttrie termLookup file size: " + tempSerialFile.length() + " or " + tempSerialFile.length() / numberOfTerms);

        long startTime = System.currentTimeMillis();
        PatriciaTrieMultiMap<Term> retrievedTrie = (PatriciaTrieMultiMap<Term>) FileUtil.deserializeOntologies(tempSerialFile) ;
        long loadTime = System.currentTimeMillis();
        retrievedTrie.rebuild();
        long finishTime = System.currentTimeMillis();
        logger.debug("load time: " + (loadTime - startTime) + " (ms) ");
        logger.debug("reconstruct time: " + (finishTime - loadTime) + " (ms) ");

        String firstWord = words.iterator().next();
        assertThat(firstWord, notNullValue());
        logger.debug("word[" + firstWord + "]");

        Set<Term> termsFound = retrievedTrie.get(firstWord) ;
        assertThat(termsFound, notNullValue());
        assertThat("No terms found for word", termsFound, hasSize(greaterThan(0)));

        logger.debug("word name: " + termsFound.iterator().next());

        SortedMap<String,Set<Term>> terms = retrievedTrie.getPrefixedBy(firstWord.substring(0, firstWord.length() - 2));
        assertThat(terms, notNullValue()) ;
        assertThat("No terms found for word [" + firstWord + "]", terms.size(), greaterThan(0));
    }


    /**
     * Here, I want to see how much space the TrieMap versus HashMap takes up serializing Terms
     * @throws Exception
     */
    @Test
    public void serializationPatriciaTreeTest() throws Exception {
        TrieMultiMap<Set<Term>> termMap = new TrieMultiMap<>();
        int numberOfTerms = 100;
        Collection<String> words = generateRandomWords(numberOfTerms);
        for (String word : words) {
            termMap.put(word, createTermWithName(word));
        }
        File tempSerialFile = FileUtil.serializeObject(termMap, tempFolder.newFile());
        logger.debug("trie map file size: " + tempSerialFile.length() + " or " + tempSerialFile.length() / numberOfTerms);

        Map<String,Set<Term>> map = new HashMap<>(numberOfTerms);
        for (String word : words) {
            map.put(word, termMap.get(word));
        }
        File temp2SerialFile = FileUtil.serializeObject(map, tempFolder.newFile());
        logger.debug("map file size: " + temp2SerialFile.length() + " or " + temp2SerialFile.length() / numberOfTerms);

        PatriciaTrieMultiMap<Term> patriciaTrieMultiMap = new PatriciaTrieMultiMap<>();
        for (String word : words) {
            patriciaTrieMultiMap.put(word, termMap.get(word));
        }
        File temp3SerialFile = FileUtil.serializeObject(patriciaTrieMultiMap, tempFolder.newFile());
        logger.debug("patttrie termLookup file size: " + temp3SerialFile.length() + " or " + temp3SerialFile.length() / numberOfTerms);

    }

    /**
     * Here, I want to see how much space the TrieMap versus HashMap takes up serializing Terms
     * @throws Exception
     */
    @Test
    public void serializationSizeTest() throws Exception {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<>();
        int numberOfTerms = 100;
        Collection<String> words = generateRandomWords(numberOfTerms);
        for (String word: words){
            tokenizer.tokenizeTerm(createTermWithName(word), termMap);
        }

        File tempSerialFile = FileUtil.serializeObject(termMap, tempFolder.newFile());
        logger.debug("trie map file size: " + tempSerialFile.length() + " or " + tempSerialFile.length() / numberOfTerms);

        Map<String,Set<TermDTO>> map = new HashMap<>(numberOfTerms);
        for (String word : words) {
            map.put(word, termMap.get(word));
        }
        File temp2SerialFile = FileUtil.serializeObject(map, tempFolder.newFile());
        logger.debug("map file size: " + temp2SerialFile.length() + " or " + temp2SerialFile.length() / numberOfTerms);
    }

    /**
     * Here, I want to see how much space the TrieMap versus HashMap takes up serializing Strings
     * @throws Exception
     */
    @Test
    public void serializationStringSizeTest() throws Exception {
        TrieMultiMap<Set<String>> termMap = new TrieMultiMap<>();
        int numberOfTerms = 1000;
        Collection<String> words = generateRandomWords(numberOfTerms);
        for (String word : words) {
            termMap.put(word, word);
        }

        File tempSerialFile = FileUtil.serializeObject(termMap, tempFolder.newFile());
        logger.debug("trie map file size: " + tempSerialFile.length() + " or " + tempSerialFile.length() / numberOfTerms);

        PatriciaTrieMultiMap<String> patriciaTrieMultiMap = new PatriciaTrieMultiMap<>();
        for (String word : words) {
            patriciaTrieMultiMap.put(word, termMap.get(word));
        }

        File temp3SerialFile = FileUtil.serializeObject(patriciaTrieMultiMap, tempFolder.newFile());
        logger.debug("term lookup map file size: " + temp3SerialFile.length() + " or " + temp3SerialFile.length() / numberOfTerms);

        Map<String, Set<String>> map = new HashMap<>(numberOfTerms);
        for (String word : words) {
            map.put(word, termMap.get(word));
        }
        File temp2SerialFile = FileUtil.serializeObject(map, tempFolder.newFile());
        logger.debug("map file size: " + temp2SerialFile.length() + " or " + temp2SerialFile.length() / numberOfTerms);
    }

    @Test
    public void trieSetSizeTest() throws Exception {
        Trie trie = new Trie();
        int numberOfTerms = 100;
        Collection<String> words = generateRandomWords(numberOfTerms);
        for (String word : words) {
            trie.add(word);
        }

        File tempSerialFile = FileUtil.serializeObject(trie, tempFolder.newFile());
        logger.debug("trie map file size: " + tempSerialFile.length()  + " or " + tempSerialFile.length() / numberOfTerms);

        File temp2SerialFile = FileUtil.serializeObject(words, tempFolder.newFile());
        logger.debug("map file size: " + temp2SerialFile.length() + " or " + temp2SerialFile.length() / numberOfTerms);
    }

    @Test
    public void trieMapSizeTest() throws Exception{
        int numberOfTerms = 100;
        TrieMap<String> trieMap = new TrieMap<>();
        Map<String, String> map = new HashMap<>(numberOfTerms);

        Collection<String> words = generateRandomWords(numberOfTerms);
        for (String word: words) {
            trieMap.put(word, word);
            map.put(word, word);
        }

        File tempSerialFile = FileUtil.serializeObject(trieMap, tempFolder.newFile());
        logger.debug("trie map file size: " + tempSerialFile.length() + " or " + tempSerialFile.length() / numberOfTerms);

        File temp2SerialFile = FileUtil.serializeObject(map, tempFolder.newFile());
        logger.debug("map file size: " + temp2SerialFile.length() + " or " + temp2SerialFile.length() / numberOfTerms);
    }

    private final static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String generateRandomWord() {
        int length = (int) (Math.random() * 20f) + 12;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int character = (int) (Math.random() * 26);
            sb.append(alphabet.substring(character, character + 1));
        }
        return sb.toString();
    }

    private Collection<String> generateRandomWords(int number){
        Set<String> words = new HashSet<>(number) ;
        for(int i = 0 ; i < number ; i++){
            words.add(generateRandomWord()) ;
        }
        return words ;
    }
    
    private static TermDTO createTermWithNameAndAlias(String name, String aliasName) {
        TermDTO term = new TermDTO();
        term.setName(name);

        Set<String> aliases = new HashSet<String>() ;
        aliases.add(aliasName) ;

        term.setAliases(aliases);
        return term ;
    }

    private static TermDTO createTermWithName(String name) {
        TermDTO term = new TermDTO();
        term.setName(name);
        return term;
    }
}


