package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.Iterator;
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
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-2430", OntologyDTO.STAGE));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-2430"));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-1001"));
        assertNull(ontologyManager.getTermByID("ZDB-TERM-070117-73"));
    }

//    @Test
//    public void testGetSorted() {
//        // forebrain has an alias
//        TermDTO t = ontologyManager.getTermByID("ZDB-TERM-100331-102", OntologyDTO.ANATOMY);
//        assertNotNull(t);
//        List<AliasDTO> synonyms = DTOConversionService.sortSynonyms(t);
//        Assert.assertNotNull(synonyms);
//        Assert.assertTrue(synonyms.size()>0);
//    }

    @Test
    public void dontTokenizeSmallWords() {
        Set<TermDTO> terms;
        terms = ontologyManager.getOntologyMap().get(OntologyDTO.QUALITY).get("disease");
        assertTrue(terms.size() > 5);
        terms = ontologyManager.getOntologyMap().get(OntologyDTO.QUALITY).get("(for");
        assertNull(terms);
        terms = ontologyManager.getOntologyMap().get(OntologyDTO.QUALITY).get("a");
        if (terms != null) {
            for (TermDTO term : terms) {
                assertTrue(term.getName().startsWith("a"));
            }
        } else {
            assertNull(terms);
        }

    }


    @Test
    public void testTermByName() {
        assertNotNull(ontologyManager.getTermByName("B cell", Ontology.ANATOMY));
        assertNull(ontologyManager.getTermByName("bad bad term", Ontology.ANATOMY));
        assertNotNull(ontologyManager.getTermByName("pelvic fin bud", Ontology.ANATOMY));
        assertNotNull(ontologyManager.getTermByName("Brachet's cleft", Ontology.ANATOMY));
        assertNotNull(ontologyManager.getTermByName("Cajal-Retzius cell", Ontology.ANATOMY));
        assertNotNull(ontologyManager.getTermByName("nucleus of the medial longitudinal fasciculus medulla oblongata", Ontology.ANATOMY));
        assertNotNull(ontologyManager.getTermByName("dorsal region", Ontology.SPATIAL));
    }

    @Test
    public void testTermByOboID() {
        // retrieve term by obo ID
        // melanocyte
        assertNotNull(ontologyManager.getTermByID("ZFA:0009091", OntologyDTO.ANATOMY));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-2149", OntologyDTO.ANATOMY));
        assertNotNull(ontologyManager.getTermByID("melanocyte", OntologyDTO.ANATOMY));
        assertNotNull(ontologyManager.getTermByName("melanocyte", Ontology.ANATOMY));
    }

//    @Test
//    public void testRelatedTerms() {
//        TermDTO term = ontologyManager.getTermByName("B cell", OntologyDTO.ANATOMY);
//        List<TermRelationship> relatedTerms = term.getRelatedTerms();
//        assertEquals(7, relatedTerms.size());
//        assertEquals(3, term.getChildrenTerms().size());
//        Term childTerm = term.getChildrenTerms().get(0);
//        assertEquals("mature B cell", childTerm.getName());
//    }

    @Test
    public void testAliases() {
        TermDTO term = ontologyManager.getTermByName("B cell", Ontology.ANATOMY);
        assertTrue(term.isAliasesExist());
//        List<AliasDTO> relatedTerms = term.getAliases();
        Set<String> relatedTerms = term.getAliases();
        assertEquals(3, relatedTerms.size());
    }


    @Test
    public void getMatchingTerms() {
        String query = "mel";
        long startTime = System.currentTimeMillis();
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> qualityList = matcher.getMatchingTerms(query, Ontology.ANATOMY);
        long endTime = System.currentTimeMillis();
        long timeToSearch = endTime - startTime;
        logger.info("Search Duration: " + timeToSearch);
        assertNotNull(qualityList);
        assertEquals(13, qualityList.size());

    }

    @Test
    public void getMatchingShortTerms() {
        String query = "nucleus of a";
        long startTime = System.currentTimeMillis();
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> qualityList = matcher.getMatchingTerms(query, Ontology.ANATOMY);
        long endTime = System.currentTimeMillis();
        long timeToSearch = endTime - startTime;
        logger.info("Search Duration: " + timeToSearch);
        assertNotNull(qualityList);
        assertTrue(qualityList.size() > 0);

    }

    @Test
    public void getMatchingAnatomyTermsOnSynonym() throws Exception {
        // 'orbital cartilage' is a synonym for
        // 'taenia marginalis anterior'
        // 'taenia marginalis posterior'
        String query = "orbital cartilage";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> anatomyList = matcher.getMatchingTerms(query, Ontology.ANATOMY);
        assertNotNull(anatomyList);
        assertTrue(anatomyList.size() == 2);
    }


    @Test
    public void suggestionsShouldNotRepeat() throws Exception {
        String query = "retina";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> anatomyList = matcher.getMatchingTerms(query, Ontology.ANATOMY);
        Iterator<MatchingTerm> iter = anatomyList.iterator();
        assertEquals("retina", iter.next().getTerm().getName());
        assertEquals("retinal bipolar neuron", iter.next().getTerm().getName());
    }

    // only works for QUALITY

    @Test
    public void testBadSearches() {
        // can find decreased p
        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> matches = service.getMatchingTerms("decreased p", Ontology.QUALITY);
        assertEquals(15, matches.size());

        // can not find decreased
        matches = service.getMatchingTerms("decreased", Ontology.QUALITY);
        assertEquals(service.getMaximumNumberOfMatches(), matches.size());
    }

//    @Test
//    public void getAllRelationshipsPerOntology() {
//        Set<String> relationships = OntologyService.getDistinctRelationships(OntologyDTO.ANATOMY);
//        assertNotNull(relationships);
//    }

    @Test
    public void testObsoleteTerm() {
        String termName = "stomach";
        String termID = "ZDB-TERM-100331-416";
        TermDTO term = ontologyManager.getTermByName(termName, Ontology.ANATOMY);
        assertNull(term);
        term = ontologyManager.getTermByID(termID, OntologyDTO.ANATOMY);
        assertNotNull(term);
    }


    @Test
    public void loadAllTermsFromFiles() throws Exception {
        OntologyManager manager = OntologyManager.getInstanceFromFile(OntologyDTO.QUALITY);
        assertNotNull(manager);
    }


    @Test
    public void getMatchingQualityTerms() {
        String query = "red";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> qualities = matcher.getMatchingTerms(query, Ontology.QUALITY);
        assertNotNull(qualities);
        assertEquals(14, qualities.size());

        int count = 0;
        for (MatchingTerm matchingTerm : qualities) {
            count += (matchingTerm.getTerm().isObsolete() ? 1 : 0);
        }
        assertEquals(4, count);

    }

    //@Test

    public void getMatchingAnatomyTerms() {
        String query = "mel";
        MatchingTermService matcher = new MatchingTermService();
        Set<MatchingTerm> anatomyList = matcher.getMatchingTerms(query, Ontology.ANATOMY);
        assertNotNull(anatomyList);
        assertEquals(21, anatomyList.size());
    }


}
