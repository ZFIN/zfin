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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the OntologyManager class.
 */
public class OntologyManagerTest extends AbstractOntologyTest{

    private static final Logger logger = Logger.getLogger(OntologyManagerTest.class);

    @Override
    protected Ontology[] getOntologiesToLoad() {
        Ontology[] ontologies = new Ontology[3];
        ontologies[0] =  Ontology.ANATOMY;
        ontologies[1] =  Ontology.QUALITY;
        ontologies[2] =  Ontology.STAGE;
        return ontologies ;
    }

    @Test
    public void testTermByID(){
        assertNotNull(ontologyManager.getTermByID(Ontology.STAGE,"ZDB-TERM-100331-2430")) ;
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-2430")) ;
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-1001")) ;
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-070117-73")) ;
    }

    @Test
    public void testTermByName(){
        assertNull(ontologyManager.getTermByName(Ontology.ANATOMY,"bad bad term")) ;
        assertNotNull(ontologyManager.getTermByName(Ontology.ANATOMY,"pelvic fin bud")) ;
        assertNotNull(ontologyManager.getTermByName(Ontology.ANATOMY,"Brachet's cleft")) ;
        assertNotNull(ontologyManager.getTermByName(Ontology.ANATOMY,"Cajal-Retzius cell")) ;
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
    }

    // only works for QUALITY
    @Test
    public void testBadSearches(){
        // can find decreased p
        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> matches = service.getMatchingTerms(Ontology.QUALITY,"decreased p") ;
        assertEquals(13,matches.size());

        // can not find decreased
        matches = service.getMatchingTerms(Ontology.QUALITY,"decreased") ;
        assertEquals(service.getMaximumNumberOfMatches(),matches.size());
    }


}
