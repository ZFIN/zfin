package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the OntologyManager class.
 */
@SuppressWarnings({"FeatureEnvy"})
public class OntologyManagerTest extends AbstractOntologyTest {

    private static final Logger logger = Logger.getLogger(OntologyManagerTest.class);

    @Before
    public void setup() {
    }

    @Override
    protected Ontology[] getOntologiesToLoad() {
        Ontology[] ontologies = new Ontology[4];
        ontologies[0] = Ontology.ANATOMY;
        ontologies[1] = Ontology.QUALITY;
        ontologies[2] = Ontology.STAGE;
        ontologies[3] = Ontology.SPATIAL;
        return ontologies;
    }

    @Test
    public void testTermByID() {
        assertNotNull(ontologyManager.getTermByID(Ontology.STAGE, "ZDB-TERM-100331-2430"));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-2430"));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-1001"));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-070117-73"));
    }

    @Test
    public void testTermByName() {
        ontologyManager.getTermByName(Ontology.ANATOMY, "B cell");
        assertNull(ontologyManager.getTermByName(Ontology.ANATOMY, "bad bad term"));
        assertNotNull(ontologyManager.getTermByName(Ontology.ANATOMY, "pelvic fin bud"));
        assertNotNull(ontologyManager.getTermByName(Ontology.ANATOMY, "Brachet's cleft"));
        assertNotNull(ontologyManager.getTermByName(Ontology.ANATOMY, "Cajal-Retzius cell"));
        assertNotNull(ontologyManager.getTermByName(Ontology.SPATIAL, "dorsal region"));
    }

    @Test
    public void testTermByOboID() {
        // retrieve term by obo ID
        // melanocyte
        assertNotNull(ontologyManager.getTermByID(Ontology.ANATOMY, "ZFA:0009091"));
    }

    @Test
    public void testRelatedTerms() {
        Term term = ontologyManager.getTermByName(Ontology.ANATOMY, "B cell");
        List<TermRelationship> relatedTerms = term.getRelatedTerms();
        assertEquals(7, relatedTerms.size());
        assertEquals(3, term.getChildrenTerms().size());
        Term childTerm = term.getChildrenTerms().get(0);
        assertEquals("mature B cell", childTerm.getTermName());
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
        assertEquals("retina", iter.next().getTerm().getTermName());
        assertEquals("retinal bipolar neuron", iter.next().getTerm().getTermName());
    }

    // only works for QUALITY

    @Test
    public void testBadSearches() {
        // can find decreased p
        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> matches = service.getMatchingTerms(Ontology.QUALITY, "decreased p");
        assertEquals(13, matches.size());

        // can not find decreased
        matches = service.getMatchingTerms(Ontology.QUALITY, "decreased");
        assertEquals(service.getMaximumNumberOfMatches(), matches.size());
    }


}
