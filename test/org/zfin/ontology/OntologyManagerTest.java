package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

import java.util.Iterator;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the OntologyManager class.
 */
public class OntologyManagerTest extends AbstractOntologyTest{

    private static final Logger logger = Logger.getLogger(OntologyManagerTest.class);


    @Test
    public void testTermByID(){
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-1001")) ;
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-091209-1")) ;
    }

//    @Test
    public void serializeAllOntologies(){

        initHibernate();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
        ontologyManager.reLoadOntologies();
    }

    @Test
    public void getMatchingTerms() {
        String query = "mel";
        long startTime = System.currentTimeMillis();
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> qualityList = matcher.getMatchingTerms(Ontology.ANATOMY, query);
        long endTime = System.currentTimeMillis();
        long timeToSearch = endTime - startTime;
        logger.info("Search Duration: " + timeToSearch);
        //OntologyManager.serializeOntology();
        assertNotNull(qualityList);
        assertEquals(13, qualityList.size());

    }

    @Test
    public void getMatchingAnatomyTermsOnSynonym() throws Exception {
        // 'orbital cartilage' is a synonym for
        // 'taenia marginalis anterior'
        // 'taenia marginalis posterior'
        String query = "orbital cartilage";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> anatomyList = matcher.getMatchingTerms(Ontology.ANATOMY, query);
        assertNotNull(anatomyList);
        assertTrue(anatomyList.size() == 2);
    }


    @Test
    public void suggestionsShouldNotRepeat() throws Exception {
        String query = "retina";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> anatomyList = matcher.getMatchingTerms(Ontology.ANATOMY, query);
        Iterator<MatchingTerm> iter = anatomyList.iterator();
        assertEquals("retina",iter.next().getTerm().getTermName()) ;
        assertEquals("retinal bipolar neuron",iter.next().getTerm().getTermName()) ;
//        assertNotNull(anatomyList);
//        assertTrue(anatomyList.size() == 2);
    }

    // only works for QUALITY
    @Test
    public void testBadSearches(){
        // can find decreased p
        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> matches = service.getMatchingTerms(Ontology.QUALITY,"decreased p") ;
        assertEquals(8,matches.size());

        // can not find decreased
        matches = service.getMatchingTerms(Ontology.QUALITY,"decreased") ;
        assertEquals(service.getMaximumNumberOfMatches(),matches.size());
    }

    /**
     * Note that this test is not exactly fair as the patriciatrie and hashmap are using
     * their only a single term.
     */
    @Test
    public void testComposedOntology() {
        MatchingTermService service = new MatchingTermService() ;
        int wordLength = 5 ;
        int numWords = 100  ;
        int numIterations = 10  ;
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

//            logger.info( "Terms GO size: "+termsGO.size());
//            logger.info( "Terms GO_MF size: "+termsGO_MF.size());

            assertTrue(termsGO.size()>=termsGO_MF.size());
        }

        logger.info( "pattrie individual search : " + (patTrieSearchTime / (double) (numWords*numIterations)) + " (ms)");
        logger.info( " pattrie individual access: " + (patTrieAccessTime / (double) (numWords*numIterations))  + " (ms)");

        logger.info( "pattrie composed search : " + (patTrieComposedSearchTime/ (double) (numWords*numIterations)) + " (ms)");
        logger.info( " pattrie composed access: " + (patTrieComposedAccessTime/ (double) (numWords*numIterations))  + " (ms)");
    }



    @Test
    public void getMatchingTermsPerformance() {
//        String[] queries = {"pel","plac","retin","glutam"};
        long startTime , endTime, wordTimeToSearch ,totalTimeToSearch ;
        totalTimeToSearch = 0 ;
        int numIterations = 20 ;
        int numQueries = 20 ;
        for(int i = 0 ; i < numQueries ; i++){
            String query = getRandomWordFromSet(ontologyManager.getOntologyMap().get(ontology).keySet()) ;
//            for(String query : queries ){
            wordTimeToSearch = 0 ;
            for(int j = 0 ; j < numIterations ; j++){
                MatchingTermService matcher = new MatchingTermService();
                startTime = System.currentTimeMillis();
                Set<MatchingTerm> qualityList = matcher.getMatchingTerms(ontology, query);
                endTime = System.currentTimeMillis();
                wordTimeToSearch += endTime - startTime;
            }
            logger.info("Word Avg: " + query + " is: " + (float) wordTimeToSearch / (float) numIterations + "ms");
            totalTimeToSearch += wordTimeToSearch ;
        }

        logger.info("Search Avg: " + (float) totalTimeToSearch  / (float) (numIterations*numQueries) + "ms");
    }

}
