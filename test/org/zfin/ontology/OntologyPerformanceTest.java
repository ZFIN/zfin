package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 */
public class OntologyPerformanceTest {

    private OntologyManager ontologyManager = null;
    private Logger logger = Logger.getLogger(OntologyPerformanceTest.class) ;

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() throws Exception{
        // load the ontology manager with test serialized file
        File ontologyFile = new File("test/serialized-ontologies.ser");
        logger.info(ontologyFile);
        if (!ontologyFile.exists())
            logger.error("Serialized file does not exist:  " + ontologyFile.getAbsolutePath());
        ontologyManager = OntologyManager.getInstanceFromFile(ontologyFile);
    }

    @Test
    public void serializationTiming() throws Exception{
        Ontology ontology = Ontology.ANATOMY ;
        OntologyManager ontologyManagerInstance = OntologyManager.getInstance(ontology) ;

        File serializationFile = getSerializedFile(ontology);
        ontologyManagerInstance.serializeOntology(serializationFile);

        ontologyManagerInstance= OntologyManager.getInstanceFromFile(serializationFile);
    }

//    @Test
    public void getMatchingTerms() {
        String[] queries = {"pel","plac","retin","glutam"};
        long startTime , endTime, wordTimeToSearch ,totalTimeToSearch ;
        totalTimeToSearch = 0 ;
        int numSearches = 20 ;
        for(String query : queries ){
            wordTimeToSearch = 0 ;
            for(int i = 0 ; i < numSearches ; i++){
                MatchingTermService matcher = new MatchingTermService(query);
                startTime = System.currentTimeMillis();
                List<MatchingTerm> qualityList = matcher.getMatchingTerms(Ontology.GO, query);
                endTime = System.currentTimeMillis();
                wordTimeToSearch += endTime - startTime;
            }
            logger.info("Word Avg: " + query + " is: " + (float) wordTimeToSearch / (float) numSearches + "ms");
            totalTimeToSearch += wordTimeToSearch ;
        }

        logger.info("Search Avg: " + (float) totalTimeToSearch  / (float) (numSearches*queries.length) + "ms");
    }

    protected static File getSerializedFile(Ontology anatomy) {
        String serializedFileName = anatomy.getOntologyName();
        serializedFileName += "-ontology.ser";
        return new File("test", serializedFileName);
    }
}
