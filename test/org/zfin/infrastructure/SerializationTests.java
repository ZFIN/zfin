package org.zfin.infrastructure;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.ontology.*;
import org.zfin.util.FileUtil;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class SerializationTests {

    private final Logger logger = Logger.getLogger(SerializationTests.class) ;

    private OntologyTokenizer tokenizer = new OntologyTokenizer() ;

    @Test
    public void memoryTest() throws Exception{
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("key1 key2","key4"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("key1 key2 key3","key4"),termMap);

        logger.debug("keys");
        for(String key: termMap.keySet()){
            logger.debug(key + "-" + termMap.get(key));
        }
        logger.debug("values");
        for(Term term:  termMap.getAllValues()){
            logger.debug(term) ;
        }
        File tempSerialFile = FileUtil.serializeObject(termMap, File.createTempFile("temp",".ser"));
        tempSerialFile.deleteOnExit();
        PatriciaTrieMultiMap<Term> deserializedMap = (PatriciaTrieMultiMap<Term>) FileUtil.deserializeOntologies(tempSerialFile) ;

        logger.debug("keys 2");
        assertTrue(CollectionUtils.isEqualCollection(termMap.keySet(),deserializedMap.keySet())) ;
        assertTrue(CollectionUtils.isEqualCollection(termMap.values(),deserializedMap.values())) ;
        for(String key: termMap.keySet()){
            logger.debug(key + "-" + deserializedMap.get(key));
        }
        logger.debug("values 2");
        for(Term term: deserializedMap.getAllValues()){
            logger.debug(term) ;
        }
    }

    private final static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generateRandomWord() {
        int length = (int) (Math.random() * 20f) + 12;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int character = (int) (Math.random() * 26);
            sb.append(alphabet.substring(character, character + 1));
        }
        return sb.toString();
    }

    public Collection<String> generateRandomWords(int number){
        Set<String> words = new HashSet<String>(number) ;
        for(int i = 0 ; i < number ; i++){
            words.add(generateRandomWord()) ;
        }


        return words ;
    }

    @Test
    public void serializationWorks() throws Exception{
        int numberOfTerms = 100 ;
        Collection<String> words = generateRandomWords(numberOfTerms) ;

        PatriciaTrieMultiMap<Term> patriciaTrieMultiMap = new PatriciaTrieMultiMap<Term>() ;

        for(String word: words){
            patriciaTrieMultiMap.put(word,createTermWithName(word)) ;
            patriciaTrieMultiMap.put(word,createTermWithName(word+" "+word)) ;
        }

        File temp3SerialFile = FileUtil.serializeObject(patriciaTrieMultiMap, File.createTempFile("temp",".ser"));
        logger.debug("patttrie termLookup file size: "+temp3SerialFile.length()  + " or "+ temp3SerialFile.length() / numberOfTerms ); ;
        temp3SerialFile.deleteOnExit();

        long startTime = System.currentTimeMillis() ;
        PatriciaTrieMultiMap<Term> retrievedTrie = (PatriciaTrieMultiMap<Term>) FileUtil.deserializeOntologies(temp3SerialFile) ;
        long loadTime = System.currentTimeMillis() ;
        retrievedTrie.rebuild();
        long finishTime = System.currentTimeMillis() ;

        logger.debug("load time: " + (loadTime - startTime) + " (ms) ");
        logger.debug("reconstruct time: " + (finishTime - loadTime) + " (ms) ");


        String firstWord = words.iterator().next() ;
        assertNotNull(firstWord);
        logger.debug("word["+firstWord+"]");


        Set<Term> termsFound = retrievedTrie.get(firstWord) ;
        assertNotNull(termsFound) ;
        assertTrue(termsFound.size()>0) ;

        logger.debug("word name: "+ termsFound.iterator().next());
        assertTrue("No terms found for word",termsFound.size()>0) ;
        SortedMap<String,Set<Term>> terms = retrievedTrie.getPrefixedBy(firstWord.substring(0,firstWord.length()-2)) ;
        assertNotNull(terms) ;
        assertTrue("No terms found for word ["+firstWord+"]",terms.size()>0) ;

    }


    /**
     * Here, I want to see how much space the TrieMap versus HashMap takes up serializing Terms
     * @throws Exception
     */
    @Test
    public void serializationPatriciaTreeTest() throws Exception{
        TrieMultiMap<Set<Term>> termMap = new TrieMultiMap<Set<Term>>() ;

        int numberOfTerms = 100 ;

        Collection<String> words = generateRandomWords(numberOfTerms) ;
        for(String word: words){
            termMap.put(word,createTermWithName(word)) ;
//            tokenizer.tokenizeTerm(createTermWithName(word),termMap);
        }

        File tempSerialFile = FileUtil.serializeObject(termMap, File.createTempFile("temp",".ser"));
        logger.debug("trie map file size: "+tempSerialFile.length()  + " or "+ tempSerialFile.length() / numberOfTerms ); ;
        tempSerialFile.deleteOnExit();


        Map<String,Set<Term>> map = new HashMap<String,Set<Term>>(numberOfTerms ) ;

        for(String word: words){
            map.put(word,termMap.get(word)) ;
        }

        File temp2SerialFile = FileUtil.serializeObject(map, File.createTempFile("temp",".ser"));
        logger.debug("map file size: "+temp2SerialFile.length()  + " or "+ temp2SerialFile.length() / numberOfTerms ); ;
        temp2SerialFile.deleteOnExit();

        PatriciaTrieMultiMap<Term> patriciaTrieMultiMap = new PatriciaTrieMultiMap<Term>() ;

        for(String word: words){
            patriciaTrieMultiMap.put(word,termMap.get(word)) ;
        }

        File temp3SerialFile = FileUtil.serializeObject(patriciaTrieMultiMap, File.createTempFile("temp",".ser"));
        logger.debug("patttrie termLookup file size: "+temp3SerialFile.length()  + " or "+ temp3SerialFile.length() / numberOfTerms ); ;
        temp3SerialFile.deleteOnExit();

    }

    /**
     * Here, I want to see how much space the TrieMap versus HashMap takes up serializing Terms
     * @throws Exception
     */
    @Test
    public void serializationSizeTest() throws Exception{
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;

        int numberOfTerms = 100 ;

        Collection<String> words = generateRandomWords(numberOfTerms) ;
        for(String word: words){
            tokenizer.tokenizeTerm(createTermWithName(word),termMap);
        }

        File tempSerialFile = FileUtil.serializeObject(termMap, File.createTempFile("temp",".ser"));
        logger.debug("trie map file size: "+tempSerialFile.length()  + " or "+ tempSerialFile.length() / numberOfTerms ); ;
        tempSerialFile.deleteOnExit();


        Map<String,Set<Term>> map = new HashMap<String,Set<Term>>(numberOfTerms ) ;

        for(String word: words){
            map.put(word,termMap.get(word)) ;
        }

        File temp2SerialFile = FileUtil.serializeObject(map, File.createTempFile("temp",".ser"));
        logger.debug("map file size: "+temp2SerialFile.length()  + " or "+ temp2SerialFile.length() / numberOfTerms ); ;
        temp2SerialFile.deleteOnExit();

    }

    /**
     * Here, I want to see how much space the TrieMap versus HashMap takes up serializing Strings
     * @throws Exception
     */
//    @Test
    public void serializationStringSizeTest() throws Exception{
        TrieMultiMap<Set<String>> termMap = new TrieMultiMap<Set<String>>() ;

        int numberOfTerms = 1000 ;

        Collection<String> words = generateRandomWords(numberOfTerms) ;
        for(String word: words){
            termMap.put(word,word) ;
        }

        File tempSerialFile = FileUtil.serializeObject(termMap, File.createTempFile("temp",".ser"));
        logger.debug("trie map file size: "+tempSerialFile.length()  + " or "+ tempSerialFile.length() / numberOfTerms ); ;
        tempSerialFile.deleteOnExit();

        PatriciaTrieMultiMap<String> patriciaTrieMultiMap = new PatriciaTrieMultiMap<String>() ;
        for(String word: words){
            patriciaTrieMultiMap.put(word,termMap.get(word)) ;
        }

        File temp3SerialFile = FileUtil.serializeObject(patriciaTrieMultiMap, File.createTempFile("temp",".ser"));
        logger.debug("term lookup map file size: "+temp3SerialFile.length()  + " or "+ temp3SerialFile.length() / numberOfTerms ); ;
        temp3SerialFile.deleteOnExit();
        


        Map<String,Set<String>> map = new HashMap<String,Set<String>>(numberOfTerms ) ;

        for(String word: words){
            map.put(word,termMap.get(word)) ;
        }

        File temp2SerialFile = FileUtil.serializeObject(map, File.createTempFile("temp",".ser"));
        logger.debug("map file size: "+temp2SerialFile.length()  + " or "+ temp2SerialFile.length() / numberOfTerms ); ;
        temp2SerialFile.deleteOnExit();
    }

//    @Test
    public void trieSetSizeTest() throws Exception{

        Trie trie = new Trie() ;

        int numberOfTerms = 100 ;

        Collection<String> words = generateRandomWords(numberOfTerms) ;
        for(String word: words){
            trie.add(word) ;
        }

        File tempSerialFile = FileUtil.serializeObject(trie, File.createTempFile("temp",".ser"));
        logger.debug("trie map file size: "+tempSerialFile.length()  + " or "+ tempSerialFile.length() / numberOfTerms ); ;
        tempSerialFile.deleteOnExit();


        File temp2SerialFile = FileUtil.serializeObject(words, File.createTempFile("temp",".ser"));
        logger.debug("map file size: "+temp2SerialFile.length()  + " or "+ temp2SerialFile.length() / numberOfTerms ) ;
        temp2SerialFile.deleteOnExit();
    }

//    @Test
    public void trieMapSizeTest() throws Exception{

        int numberOfTerms = 100 ;

        TrieMap<String> trieMap = new TrieMap<String>() ;
        Map<String,String> map = new HashMap<String,String>(numberOfTerms ) ;

        Collection<String> words = generateRandomWords(numberOfTerms) ;

        for(String word: words){
            trieMap.put(word,word) ;
            map.put(word,word) ;
        }


        File tempSerialFile = FileUtil.serializeObject(trieMap, File.createTempFile("temp",".ser"));
        logger.debug("trie map file size: "+tempSerialFile.length()  + " or "+ tempSerialFile.length() / numberOfTerms ) ;
        tempSerialFile.deleteOnExit();


        File temp2SerialFile = FileUtil.serializeObject(map, File.createTempFile("temp",".ser"));
        logger.debug("map file size: "+temp2SerialFile.length()  + " or "+ temp2SerialFile.length() / numberOfTerms ) ;
        temp2SerialFile.deleteOnExit();
    }

    
    public static Term createTermWithNameAndAlias(String name, String aliasName) {
        GenericTerm term = new GenericTerm();
        term.setTermName(name);

        TermAlias alias = new TermAlias();
        alias.setAlias(aliasName);
        alias.setTerm(term);

        Set<TermAlias> aliases = new HashSet<TermAlias>() ;
        aliases.add(alias) ;

        term.setAliases(aliases);
//        term.setA
        return term ;
    }

    public static Term createTermWithName(String name) {
        Term term = new GenericTerm();
        term.setTermName(name);
        return term;
    }
}


