package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.properties.ZfinProperties;

import java.io.File;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the OntologyManager class.
 */
public class OntologyManagerTest {

    private OntologyManager ontologyManager = null;
    private static final Logger LOG = RootLogger.getLogger(OntologyManagerTest.class);

    @Before
    public void setUp() throws Exception {
        // load the ontology manager with test serialized file
        File ontologyFile = OntologyManagerUtil.getSerializedFile(Ontology.ANATOMY);
        if (!ontologyFile.exists())
            LOG.error("Serialized file does not exist:  " + ontologyFile.getAbsolutePath());
        ontologyManager = OntologyManager.getInstanceFromFile(ontologyFile);
    }

    @Test
    public void getMatchingTerms() {
        String query = "mel";
        long startTime = System.currentTimeMillis();
        MatchingTermService matcher = new MatchingTermService(query);
        List<MatchingTerm> qualityList = matcher.getMatchingTerms(Ontology.ANATOMY, query);
        long endTime = System.currentTimeMillis();
        long timeToSearch = endTime - startTime;
        LOG.info("Search Duration: " + timeToSearch);
        //OntologyManager.serializeOntology();
        assertNotNull(qualityList);
        assertEquals(21, qualityList.size());

    }

    @Test
    public void getMatchingAnatomyTermsOnSynonym() throws Exception {
        // 'orbital cartilage' is a synonym for
        // 'taenia marginalis anterior'
        // 'taenia marginalis posterior'
        String query = "orbital cartilage";
        MatchingTermService matcher = new MatchingTermService(query); 
        List<MatchingTerm> anatomyList = matcher.getMatchingTerms(Ontology.ANATOMY, query);
        assertNotNull(anatomyList);
        assertTrue(anatomyList.size() == 2);

    }

    @Test
    public void getMatchingQualityTermsOnSynonym() throws Exception {
        // 'small' is a synonym for 'decreased size'
        String query = "small";
        MatchingTermService matcher = new MatchingTermService(query);
        List<MatchingTerm> anatomyList = matcher.getMatchingTerms(Ontology.QUALITY, query);
        assertNotNull(anatomyList);
        assertTrue(anatomyList.size() > 0);
    }


}
