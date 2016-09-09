package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.SubsetDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.ontology.Ontology.*;


/**
 * Test the OntologyManager class.
 */
@SuppressWarnings({"FeatureEnvy"})
public class OntologyManagerTest extends AbstractOntologyTest {

    private static final Logger logger = Logger.getLogger(OntologyManagerTest.class);
    protected static final String QUALITY_TERM_DECREASED_AGE = "PATO:0001765";
    protected static final String QUALITY_TERM_DECREASED_RATE = "PATO:0000911";

    @Override
    protected Ontology[] getOntologiesToLoad() {
        return new Ontology[]{
                ANATOMY,
                QUALITY,
                STAGE,
                SPATIAL,
                QUALITY_PROCESSES,
                QUALITY_QUALITIES};
    }


    @Test
    public void testTermByID() {
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-2430", OntologyDTO.STAGE));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-2430"));
        assertNotNull(ontologyManager.getTermByID("ZDB-TERM-100331-1001"));
        assertNull(ontologyManager.getTermByID("ZDB-TERM-070117-73"));
    }

    @Test
    public void dontTokenizeSmallWords() {
        Set<TermDTO> terms;
        terms = ontologyManager.getOntologyMap().get(OntologyDTO.QUALITY).get("disease");
        assertTrue(terms.size() > 5);
        terms = ontologyManager.getOntologyMap().get(OntologyDTO.QUALITY).get("a");
        if (terms != null) {
            for (TermDTO term : terms) {
                assertTrue(term.getName().startsWith("a"));
            }
        }

    }


    @Test
    public void testTermByName() {
        assertNotNull(ontologyManager.getTermByName("B cell", ANATOMY));
        assertNull(ontologyManager.getTermByName("bad bad term", ANATOMY));
        assertNotNull(ontologyManager.getTermByName("pelvic fin bud", ANATOMY));
        assertNotNull(ontologyManager.getTermByName("Brachet's cleft", ANATOMY));
        assertNotNull(ontologyManager.getTermByName("Cajal-Retzius cell", ANATOMY));
        assertNotNull(ontologyManager.getTermByName("nucleus of the medial longitudinal fasciculus medulla oblongata", ANATOMY));
        assertNotNull(ontologyManager.getTermByName("dorsal region", SPATIAL));
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

    @Test
    public void getOntologyByTermByOboID() {
        Ontology ontology = ontologyManager.getOntologyForTerm("ZFA:0009091");
        assertNotNull(ontology);
    }

    @Test
    public void testAliases() {
        TermDTO term = ontologyManager.getTermByName("B cell", ANATOMY);
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
        List<MatchingTerm> qualityList = matcher.getMatchingTerms(query, ANATOMY);
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
        List<MatchingTerm> qualityList = matcher.getMatchingTerms(query, ANATOMY);
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
        List<MatchingTerm> anatomyList = matcher.getMatchingTerms(query, ANATOMY);
        assertNotNull(anatomyList);
        assertTrue(anatomyList.size() == 2);
    }


    @Test
    public void suggestionsShouldNotRepeat() throws Exception {
        String query = "retina";
        MatchingTermService matcher = new MatchingTermService();
        List<MatchingTerm> anatomyList = matcher.getMatchingTerms(query, ANATOMY);
        Iterator<MatchingTerm> iter = anatomyList.iterator();
        assertEquals("retina", iter.next().getTerm().getName());
        assertEquals("retinal bipolar neuron", iter.next().getTerm().getName());
    }

    // only works for QUALITY

    @Test
    public void testBadSearches() {
        // can find decreased p
        MatchingTermService service = new MatchingTermService();
        List<MatchingTerm> matches = service.getMatchingTerms("decreased p", QUALITY);
        assertTrue(matches.size() > 10);

        // can not find decreased
        matches = service.getMatchingTerms("decreased", QUALITY);
        assertEquals(service.getMaximumNumberOfMatches(), matches.size());
    }

    @Test
    public void testObsoleteTerm() {
        String termName = "stomach";
        String termID = "ZDB-TERM-100331-416";
        TermDTO term = ontologyManager.getTermByName(termName, ANATOMY);
        assertNull(term);
        term = ontologyManager.getTermByID(termID, OntologyDTO.ANATOMY);
        assertNotNull(term);
    }

    @Test
    public void testRelationalTerm() {
        // fused with
        String termID = "ZDB-TERM-070117-643";
        TermDTO term = ontologyManager.getTermByID(termID, OntologyDTO.QUALITY);
        assertNotNull(term);
        assertTrue(term.isPartOfSubset(SubsetDTO.RELATIONAL_SLIM));
    }

    /**
     * The term 'normal' is suppressed in the process and object slim, We do not
     * want those terms to be suggested in the auto-complete.
     */
    @Test
    public void noNormalTermInQualityOntology() {
        // 'normal' term should be excluded in the process and object slim of PATO
        TermDTO term = ontologyManager.getTermByID(OntologyManager.QUALITY_TERM_NORMAL, OntologyDTO.QUALITY_PROCESSES);
        assertNull(term);
        term = ontologyManager.getTermByID(OntologyManager.QUALITY_TERM_NORMAL, OntologyDTO.QUALITY_QUALITIES);
        assertNull(term);
        term = ontologyManager.getTermByID(OntologyManager.QUALITY_QUALITIES_ROOT, OntologyDTO.QUALITY_QUALITIES);
        assertNotNull(term);
        term = ontologyManager.getTermByID(OntologyManager.QUALITY_PROCESSES_ROOT, OntologyDTO.QUALITY_PROCESSES);
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
        List<MatchingTerm> qualities = matcher.getMatchingTerms(query, QUALITY);
        assertNotNull(qualities);
        assertEquals(14, qualities.size());

        int count = 0;
        for (MatchingTerm matchingTerm : qualities) {
            count += (matchingTerm.getTerm().isObsolete() ? 1 : 0);
        }
        assertEquals(4, count);

    }

    @Test
    public void getMatchingAnatomyTerms() {
        String query = "mel";
        MatchingTermService matcher = new MatchingTermService();
        List<MatchingTerm> anatomyList = matcher.getMatchingTerms(query, ANATOMY);
        assertNotNull(anatomyList);
        assertEquals(13, anatomyList.size());
    }

    @Test
    public void shouldGetExactTerm() {
        String query = "epithelium";
        TermDTO termDTO = ontologyManager.getTermByID(query);
        assertEquals(query, termDTO.getName());
    }

    @Test
    public void allAnatomyTerms() {
        List<TermDTO> set = ontologyManager.getAllTerms(ANATOMY);
        assertNotNull(set);
    }


}
