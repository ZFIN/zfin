package org.zfin.ontology;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class MatchingTermServiceTest {

    private List<Term> sampleTerms;
    private List<TermAlias> sampleAliasTerms;
    private static final String MELANOBLAST = "melanoblast";
    private static final String MELANOCYTE = "melanocyte";
    private static final String MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL = "melanocyte stimulating hormone secreting cell";
    private static final String MELANOPHORE_STRIPE = "melanophore stripe";

    @Test
    public void termMatchStartMela() {
        String queryString = "mela";
        MatchingTermService service = new MatchingTermService(queryString);
        for (Term term : sampleTerms) {
            service.compareWithTerm(term);
        }
        for (TermAlias termAlias : sampleAliasTerms) {
            service.compareWithAlias(termAlias);
        }
        List<MatchingTerm> matchingTerms = service.getTermsMatchingStart();
        assertNotNull(matchingTerms);
        assertEquals(4, matchingTerms.size());
        assertEquals(MELANOBLAST, matchingTerms.get(0).getMatchingTermDisplay());
        assertEquals(MELANOCYTE, matchingTerms.get(1).getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, matchingTerms.get(2).getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, matchingTerms.get(3).getMatchingTermDisplay());

    }

    @Test
    public void termMatchStartMel() {
        String queryString = "mel";
        MatchingTermService service = new MatchingTermService(queryString);
        for (Term term : sampleTerms) {
            service.compareWithTerm(term);
        }
        for (TermAlias termAlias : sampleAliasTerms) {
            service.compareWithAlias(termAlias);
        }
        List<MatchingTerm> matchingTerms = service.getTermsMatchingStart();
        assertNotNull(matchingTerms);
        assertEquals(7, matchingTerms.size());
        assertEquals("MeL", matchingTerms.get(0).getMatchingTermDisplay());
        assertEquals(MELANOBLAST, matchingTerms.get(1).getMatchingTermDisplay());
        assertEquals(MELANOCYTE, matchingTerms.get(2).getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, matchingTerms.get(3).getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, matchingTerms.get(4).getMatchingTermDisplay());
        assertEquals("MeLm", matchingTerms.get(5).getMatchingTermDisplay());
        assertEquals("MeLr", matchingTerms.get(6).getMatchingTermDisplay());

        List<MatchingTerm> matchingAliases = service.getTermsMatchingContains();
        assertNotNull(matchingAliases);
        assertEquals(9, matchingAliases.size());
        assertEquals("afferent lamellar arteriole", matchingAliases.get(0).getMatchingTermDisplay());
        assertEquals("ameloblast", matchingAliases.get(1).getMatchingTermDisplay());
        assertEquals("dental organ [enamel organ]", matchingAliases.get(2).getMatchingTermDisplay());
        assertEquals("dorsal larval melanophore stripe", matchingAliases.get(3).getMatchingTermDisplay());
        assertEquals("enameloid", matchingAliases.get(4).getMatchingTermDisplay());
        assertEquals("gill lamella", matchingAliases.get(5).getMatchingTermDisplay());
        assertEquals("inner dental epithelium [inner enamel epithelium]", matchingAliases.get(6).getMatchingTermDisplay());
        assertEquals("ventral larval melanophore stripe", matchingAliases.get(7).getMatchingTermDisplay());
        assertEquals("yolk larval melanophore stripe", matchingAliases.get(8).getMatchingTermDisplay());
    }

    @Before
    public void createTermsList() {
        sampleTerms = new ArrayList<Term>(20);
        sampleTerms.add(createTermWithName("afferent lamellar arteriole"));
        sampleTerms.add(createTermWithName("ameloblast"));
        sampleTerms.add(createTermWithName("dorsal larval " + MELANOPHORE_STRIPE));
        sampleTerms.add(createTermWithName("enameloid"));
        sampleTerms.add(createTermWithName("gill lamella"));
        sampleTerms.add(createTermWithName("inner dental epithelium"));
        sampleTerms.add(createTermWithName("MeL"));
        sampleTerms.add(createTermWithName(MELANOBLAST));
        sampleTerms.add(createTermWithName(MELANOCYTE));
        sampleTerms.add(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL));
        sampleTerms.add(createTermWithName(MELANOPHORE_STRIPE));
        sampleTerms.add(createTermWithName("MeLm"));
        sampleTerms.add(createTermWithName("MeLr"));
        sampleTerms.add(createTermWithName("Mine and Yours"));
        sampleTerms.add(createTermWithName("ventral larval " + MELANOPHORE_STRIPE));
        sampleTerms.add(createTermWithName("yolk larval " + MELANOPHORE_STRIPE));

        sampleAliasTerms = new ArrayList<TermAlias>(2);
        sampleAliasTerms.add(createTermWithNameAndAlias("dental organ", "enamel organ"));
        sampleAliasTerms.add(createTermWithNameAndAlias("inner dental epithelium", "inner enamel epithelium"));
    }

    private Term createTermWithName(String name) {
        Term term = new GenericTerm();
        term.setTermName(name);
        return term;
    }

    private TermAlias createTermWithNameAndAlias(String name, String aliasName) {
        GenericTerm term = new GenericTerm();
        term.setTermName(name);

        TermAlias alias = new TermAlias();
        alias.setAlias(aliasName);
        alias.setTerm(term);
        return alias;
    }

}