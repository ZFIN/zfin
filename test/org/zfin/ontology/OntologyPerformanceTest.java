package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.infrastructure.TrieMultiMap;
import org.zfin.util.AlphanumComparator;
import org.zfin.util.NumberAwareStringComparator;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 */
public class OntologyPerformanceTest extends AbstractOntologyTest{

    private final static Logger logger = Logger.getLogger(OntologyPerformanceTest.class) ;
    private final Pattern numberPattern = Pattern.compile("\\p{Digit}");

    @Override
    protected Ontology[] getOntologiesToLoad() {
        return Ontology.getSerializableOntologies();
    }

    @Test
    public void serializationTiming() throws Exception{
        initHibernate();
        ontologyManager.reLoadOntologies();
    }

    /**
     */
    @Test
    public void ontologyPerfomanceTest() {
        PatriciaTrieMultiMap<Term> patriciaTrie = ontologyManager.getOntologyMap().get(Ontology.QUALITY) ;
        TrieMultiMap<Set<Term>> trieMultiMap = new TrieMultiMap<Set<Term>>() ;
        HashMap<String,Set<Term>> hashMap = new HashMap<String,Set<Term>>();
        for(String key : patriciaTrie.keySet()){
            trieMultiMap.put(key,patriciaTrie.get(key)) ;
            hashMap.put(key,patriciaTrie.get(key)) ;
        }

        logger.debug("pattrie length: " + patriciaTrie.size());
        logger.debug("triemultimap length: " + trieMultiMap.size());
        logger.debug("hashmap length: " + hashMap.size());

        double trieSearchTime = 0f;
        double trieAccessTime = 0f;

        double patTrieSearchTime = 0f;
        double patTrieAccessTime = 0f;
        double hashSearchTime = 0f ;
        double hashAccessTime = 0f ;


        double startTime, finishTime ;

        String[] results = null;
        List<String> hits = new ArrayList<String>();
        int wordLength = 3 ;
        int numWords = 100 ;
        int numIterations = 100 ;
        int hitCount ;
        for (int i = 0; i < numWords ; i++) {
            // prepare word
            String testWord = getRandomWordFromSet(trieMultiMap.keySet()) ;
            if(testWord.length()<wordLength){
                testWord = testWord.substring(0, wordLength).toLowerCase();
            }



            // triemap test
            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                results = trieMultiMap.suggest(testWord);
            }
            hitCount= results.length ;
            finishTime = System.currentTimeMillis();
            trieSearchTime += (finishTime - startTime) ;

            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                for (String result : results) {
                    assertNotNull(trieMultiMap.get(result));
                }
            }
            finishTime = System.currentTimeMillis();
            trieAccessTime += ( finishTime - startTime  )  / (double) results.length ;

            // patTrie  test
            startTime = System.currentTimeMillis();
            SortedMap<String,Set<Term>> resultMap ;
            for(int j = 0 ; j < numIterations ; j++){
                resultMap = patriciaTrie.getPrefixedBy(testWord);
                if(hitCount!=resultMap.size()){
                    logger.debug("TEST WORD: " + testWord);
                    // results out
                    logger.debug("TRIE OUTPUT");
                    for(String s : results){
                        logger.debug(s);
                    }

                    logger.debug("PAT TRIE OUTPUT");
                    for(String key : resultMap.keySet()){
                        logger.debug(key);
                    }
                }
                assertEquals(hitCount,resultMap.size());
            }
            finishTime = System.currentTimeMillis();
            patTrieSearchTime += (finishTime - startTime) ;

            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                for (String result : results) {
                    assertNotNull(patriciaTrie.get(result));
                }
            }
            finishTime = System.currentTimeMillis();
            patTrieAccessTime += ( finishTime - startTime  )  / (double) results.length ;

            // hashmap test
            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                hits.clear();
                for (String s : hashMap.keySet()) {
                    if (s.equals(testWord)) {
                        hits.add(s);
                    } else if (s.startsWith(testWord)) {
                        hits.add(s);
                    }
                }
            }
            finishTime = System.currentTimeMillis();
            hashSearchTime += (finishTime - startTime) ;

            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                for (String result : results) {
                    assertNotNull(hashMap.get(result));
                }
            }
            finishTime = System.currentTimeMillis();
            hashAccessTime += ( finishTime - startTime  )  / (double) results.length ;

            logger.debug("Hits for word: " + testWord + " "+ hits.size());

            assertEquals("both hit lengths should be the same for word: "+testWord , hits.size(),results.length);
        }

        logger.info("\nSearch time trieTime " + (trieSearchTime / (double) (numWords*numIterations))  + "(ms) " +
                " vs hash: " + (hashSearchTime  / (double) (numWords*numIterations)) + " (ms)" +
                " vs pattrie : " + (patTrieSearchTime / (double) (numWords*numIterations)) + " (ms)");
        logger.info("\nAccess time trieTime " + (trieAccessTime / (double) (numWords*numIterations))  + "(ms) " +
                " vs hash: " + (hashAccessTime  / (double) (numWords*numIterations))  + " (ms)" +
                " vs pattrie: " + (patTrieAccessTime / (double) (numWords*numIterations))  + " (ms)");
    }



    @Test
    public void dataStructurePerformance() {
        PatriciaTrieMultiMap<Term> patriciaMap = new PatriciaTrieMultiMap<Term>();
        TrieMultiMap<Set<Term>> trieMap = new TrieMultiMap<Set<Term>>();
        HashMap<String, GenericTerm> hashTest = new HashMap<String, GenericTerm>();
        int length = 1000;
        for (int i = 0; i < length; i++) {
            String word = generateRandomWord();
            GenericTerm genericTerm = new GenericTerm();
            genericTerm.setTermName(word);
            trieMap.put(word, genericTerm);
            hashTest.put(word, genericTerm);
            patriciaMap.put(word, genericTerm);
        }

        System.out.println("total length: " + trieMap.keySet().size());

        double totalPatTrieTime = 0f;
        double totalTrieTime = 0f;
        double totalHashSetTime = 0f;

        String[] results = null;
        List<String> hits = null;
        for (int i = 0; i < 100; i++) {
            String testWord = getRandomWordFromSet(trieMap.keySet()) ;

            testWord = testWord.substring(0, 2);

            // trie test
            double startTime = System.currentTimeMillis();
            results = trieMap.suggest(testWord);
            for (String result : results) {
                hashTest.get(result).getTermName();
            }
            double finishTime = System.currentTimeMillis();
            totalTrieTime += finishTime - startTime;

            // hash test
            hits = new ArrayList<String>();
            startTime = System.currentTimeMillis();
            for (String s : hashTest.keySet()) {
                if (s.equals(testWord)) {
                    hits.add(s);
                } else if (s.startsWith(testWord)) {
                    hits.add(s);
                }
            }

            for (String result : hits) {
                hashTest.get(result).getTermName();
            }
            finishTime = System.currentTimeMillis();

            totalHashSetTime += finishTime - startTime;

            if (hits.size() != results.length) {
                System.out.println("results don't match");
                return;
            }
            assert (hits.size() == results.length);
            hits.clear();

            // pat trie test
            startTime = System.currentTimeMillis();
            Set<String> hitKeys = patriciaMap.getPrefixedBy(testWord).keySet();

            for (String result : hitKeys) {
                patriciaMap.get(result).iterator().next().getTermName();
            }
            finishTime = System.currentTimeMillis();
            totalPatTrieTime += finishTime - startTime;
        }

        System.out.println("\ntrieTime " + totalTrieTime + "(ms) vs hashTime: " + totalHashSetTime + " (ms)");
        System.out.println("\ntrieTime " + totalTrieTime + "(ms) vs patTrieTie: " + totalPatTrieTime+ " (ms)");
    }

    @Test
    public void tokenizationTest(){
        OntologyTokenizer tokenizer = new OntologyTokenizer() ;
        Set<String> strings = new HashSet<String>() ;
        int numberToGenerate = 100000 ;
        for(int i = 0 ; i < numberToGenerate ; i++){
            strings.add(generateRandomWordWithSpaces()) ;
        }
        long startTime = System.currentTimeMillis() ;
        for(String s : strings){
            tokenizer.tokenizeStrings(s) ;
        }
        long finishTime = System.currentTimeMillis() ;
        logger.debug("time: "+((finishTime - startTime)/ (float) numberToGenerate) + " (ms) per term");
    }

    /**
     * Note that this test is not exactly fair as the patriciatrie and hashmap are using
     * their only a single term.
     */
    @Test
    public void testComposedOntology() {
        MatchingTermService service = new MatchingTermService() ;
        int wordLength = 5 ;
        int numWords = 10  ;
        int numIterations = 3  ;
        long startTime, finishTime;
        long patTrieSearchTime = 0 , patTrieAccessTime = 0 ;
        long patTrieComposedSearchTime = 0 , patTrieComposedAccessTime = 0 ;
        Set<MatchingTerm> termsGO_MF = null;
        Set<MatchingTerm> termsGO = null;
        for (int i = 0; i < numWords ; i++) {
            // prepare word
            String testWord = getRandomWordFromSet(ontologyManager.getOntologyMap().get(Ontology.GO_MF).keySet()) ;
            if(testWord.length()>wordLength){
                testWord = testWord.substring(0, wordLength).toLowerCase();
            }

            // individusal
            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                termsGO_MF = service.getMatchingTerms(Ontology.GO_MF,testWord) ;
            }
            finishTime = System.currentTimeMillis();
            patTrieSearchTime += (finishTime - startTime) ;

            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                for (MatchingTerm term: termsGO_MF) {
                    assertNotNull(ontologyManager.getTermByID(term.getTerm().getID()));
                }
            }
            finishTime = System.currentTimeMillis();
            patTrieAccessTime += ( finishTime - startTime  )  / (double) termsGO_MF.size();

            // composed
            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                termsGO = service.getMatchingTerms(Ontology.GO,testWord) ;
            }
            finishTime = System.currentTimeMillis();
            patTrieComposedSearchTime += (finishTime - startTime) ;

            startTime = System.currentTimeMillis();
            for(int j = 0 ; j < numIterations ; j++){
                for (MatchingTerm term: termsGO) {
                    assertNotNull(ontologyManager.getTermByID(term.getTerm().getID()));
                }
            }
            finishTime = System.currentTimeMillis();
            patTrieComposedAccessTime += ( finishTime - startTime  )  / (double) termsGO.size();

            logger.debug( "Terms GO size: "+termsGO.size());
            logger.debug( "Terms GO_MF size: "+termsGO_MF.size());

            assertTrue(termsGO.size()>=termsGO_MF.size());
        }

        logger.info( "pattrie individual search : " + (patTrieSearchTime / (double) (numWords*numIterations)) + " (ms)");
        logger.info( " pattrie individual access: " + (patTrieAccessTime / (double) (numWords*numIterations))  + " (ms)");

        logger.info( "pattrie composed search : " + (patTrieComposedSearchTime/ (double) (numWords*numIterations)) + " (ms)");
        logger.info( " pattrie composed access: " + (patTrieComposedAccessTime/ (double) (numWords*numIterations))  + " (ms)");
    }



//    @Test
    public void getMatchingTermsPerformance() {
        long startTime , endTime, wordTimeToSearch ,totalTimeToSearch ;
        totalTimeToSearch = 0 ;
        int numIterations = 20 ;
        int numQueries = 20 ;
        for(int i = 0 ; i < numQueries ; i++){
            String query = getRandomWordFromSet(ontologyManager.getOntologyMap().get(Ontology.GO).keySet()) ;
//            for(String query : queries ){
            wordTimeToSearch = 0 ;
            for(int j = 0 ; j < numIterations ; j++){
                MatchingTermService matcher = new MatchingTermService();
                startTime = System.currentTimeMillis();
                Set<MatchingTerm> qualityList = matcher.getMatchingTerms(Ontology.GO, query);
                endTime = System.currentTimeMillis();
                wordTimeToSearch += endTime - startTime;
            }
            logger.info("Word Avg: " + query + " is: " + (float) wordTimeToSearch / (float) numIterations + "ms");
            totalTimeToSearch += wordTimeToSearch ;
        }

        logger.info("Search Avg: " + (float) totalTimeToSearch  / (float) (numIterations*numQueries) + "ms");
    }

    public List<String> getRandomWordSets(Ontology ontology, int number,int minWordLength,int maxWordLength){
        List<String> queries1 = new ArrayList<String>() ;
        String word ;
        do{
            word =  getRandomWordFromSet(ontologyManager.getOntologyMap().get(ontology).keySet());
//            if( numberPattern.matcher(word).find() && word.length()>minWordLength && word.length()<maxWordLength){
            if( word.length()>minWordLength && word.length()<maxWordLength){
                queries1.add(word) ;
            }
        }
        while(  queries1.size()<number ) ;
        return queries1 ;
    }

    @Test
    public void compareAlphaNumericComparators(){
        long startTime , endTime ;
        int numQueries = 100 ; // this should be a relatively standard list to sort
        int numIterations = 10 ;
        long numawareTime = 0 , alphanumTime = 0 ;
        int minWordLength = 0 ;
        int maxWordLength = 200 ;

        float totalWordLength = 0 ;
        float totalWordsThatHaveNumbers = 0 ;
        NumberAwareStringComparator numberAwareStringComparator = new NumberAwareStringComparator();
        AlphanumComparator<String> alphanumComparator = new AlphanumComparator<String>();

        for(int i = 0 ; i < numIterations ; i++){

            List<String> queries1 = getRandomWordSets(Ontology.GO_BP, numQueries,minWordLength,maxWordLength) ;
            List<String> queries2 = new ArrayList<String>(queries1) ;

            float wordLength = getAverageWordLength(queries1) ;
            totalWordLength += wordLength ;
            logger.debug("\nword length: "+ wordLength);

            int wordsThatHaveNumbers= getWordsThatHaveNumbers(queries1) ;
            totalWordsThatHaveNumbers += wordsThatHaveNumbers ;
            logger.debug("\nwords that have nubmers: "+ wordsThatHaveNumbers);

            startTime = System.currentTimeMillis();
            for(String query1 : queries1){
                for(String query2 : queries2){
                    numberAwareStringComparator.compare(query1,query2) ;
                }
            }
            endTime = System.currentTimeMillis();
            numawareTime += endTime - startTime ;
            logger.debug("numawaretime time " + (float) (endTime - startTime)+ " ms");

            startTime = System.currentTimeMillis();
            for(String query1 : queries1){
                for(String query2 : queries2){
                    alphanumComparator.compare(query1,query2) ;
                }
            }
            endTime = System.currentTimeMillis();
            alphanumTime += endTime - startTime ;
            logger.debug("alphanum time " + (float) (endTime - startTime)   + " ms" );

        }

        logger.info("\navg word length" + totalWordLength / (float) numIterations + " chars");
        logger.info("\nwords that have numbers: "+ ( totalWordsThatHaveNumbers / ((float) numIterations * (float) numQueries))  );
        logger.info("numawaretime time " + (float) numawareTime + " ms");
        logger.info("alphanum time " + (float) alphanumTime   + " ms");

    }

    private int getWordsThatHaveNumbers(List<String> queries1) {
        int counter = 0 ;
        for(String query: queries1){
            if(numberPattern.matcher(query).find()){
                ++counter ;
            }
        }
        return counter ;
    }

    private float getAverageWordLength(List<String> queries1) {
        int sum = 0 ;
        for(String query : queries1){
            sum += query.length();
        }

        return (float ) sum / (float) queries1.size();
    }
}
