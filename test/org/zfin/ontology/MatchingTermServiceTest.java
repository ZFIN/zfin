package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.PatriciaTrieMultiMap;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchingTermServiceTest {

    private final Logger logger = Logger.getLogger(MatchingTermServiceTest.class);

    private static final String MELANOBLAST = "melanoblast";
    private static final String MELANOCYTE = "melanocyte";
    private static final String MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL = "melanocyte stimulating hormone secreting cell";
    private static final String MELANOPHORE_STRIPE = "melanophore stripe";
    MatchingTermService service = new MatchingTermService();
    OntologyTokenizer tokenizer = new OntologyTokenizer();

    @Test
    public void testSplitTrim() {
        String s = "  aaaa    ggggg  ";
        String[] strings = s.trim().split("\\s+");
        assertEquals(2, strings.length);
        assertEquals("aaaa", strings[0]);
        assertEquals("ggggg", strings[1]);
    }

    private void dumpKeys(PatriciaTrieMultiMap<TermDTO> termMap) {
        for (String key : termMap.keySet()) {
            logger.debug(key);
        }

    }

    // for hit "A B C", should find (in this order) "A B C" (first), "A B" (second), "A","B", "C", "A C" (any order), "B C" (last)
    @Test
    public void testFindWithin() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName("AAA BBB CCC"), termMap);
        dumpKeys(termMap);
        assertEquals(1, service.getMatchingTerms("aaa bbb ccc", termMap).size());
        assertEquals(1, service.getMatchingTerms("A", termMap).size());
        assertEquals(1, service.getMatchingTerms("B", termMap).size());
        assertEquals(1, service.getMatchingTerms("C", termMap).size());
        assertEquals(1, service.getMatchingTerms("a b", termMap).size());
        assertEquals(1, service.getMatchingTerms("b c", termMap).size());
        assertEquals(1, service.getMatchingTerms("ccc bbb", termMap).size());
        assertEquals(1, service.getMatchingTerms("aaa ccc", termMap).size());
        assertEquals(1, service.getMatchingTerms("ccc aaa", termMap).size());
        assertEquals(1, service.getMatchingTerms("bbb aaa", termMap).size());
    }

    // test the order
    @Test
    public void testFindWithinOrder() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName("AAA BBB CCC"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("BBB AAA CCC"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("CCC AAA BBB"), termMap);
        dumpKeys(termMap);
        Set<MatchingTerm> matchingTerms;
        Iterator<MatchingTerm> iterator;

        matchingTerms = service.getMatchingTerms("a b c", termMap);
        iterator = matchingTerms.iterator();
        assertEquals(3, matchingTerms.size());
        assertEquals("AAA BBB CCC", iterator.next().getTerm().getName());
        assertEquals("BBB AAA CCC", iterator.next().getTerm().getName());
        assertEquals("CCC AAA BBB", iterator.next().getTerm().getName());

        matchingTerms = service.getMatchingTerms("b a c", termMap);
        iterator = matchingTerms.iterator();
        assertEquals(3, matchingTerms.size());
        assertEquals("AAA BBB CCC", iterator.next().getTerm().getName());
        assertEquals("BBB AAA CCC", iterator.next().getTerm().getName());
        assertEquals("CCC AAA BBB", iterator.next().getTerm().getName());
        matchingTerms = service.getMatchingTerms("c a b", termMap);


    }

    // for hit "A B C", should find (in this order) "A B C" (first), "A B" (second), "A","B", "C", "A C" (any order), "B C" (last)
    @Test
    public void testFindWithinRealWords() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL), termMap);
        assertEquals(1, service.getMatchingTerms("melan stim hor", termMap).size());
        assertEquals(1, service.getMatchingTerms("melan", termMap).size());
        assertEquals(1, service.getMatchingTerms("stim", termMap).size());
        assertEquals(1, service.getMatchingTerms("hor", termMap).size());
        assertEquals(1, service.getMatchingTerms("melan stim", termMap).size());
        assertEquals(1, service.getMatchingTerms("stim horm", termMap).size());
        assertEquals(1, service.getMatchingTerms("melan horm", termMap).size());
    }


    // for hits "ABC-123",
    @Test
    public void testTokenizeEntireWord() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName("A'BCD-123-DEF GHI-456"), termMap);
        assertEquals(1, termMap.getAllValues().size());
        assertEquals(8, termMap.size());
        assertEquals(0, service.getMatchingTerms("abc", termMap).size());
        assertEquals(1, service.getMatchingTerms("bcd", termMap).size());
        assertEquals(1, service.getMatchingTerms("ghi", termMap).size());
        assertEquals(1, service.getMatchingTerms("456", termMap).size());
        assertEquals(1, service.getMatchingTerms("ghi-456", termMap).size());
        assertEquals(1, service.getMatchingTerms("a'bcd", termMap).size());
        assertEquals(1, service.getMatchingTerms("a'bcd-123-def ghi-456", termMap).size());
        assertEquals(1, service.getMatchingTerms("A'BCD-123-DEF GHI-456", termMap).size());
        assertEquals(1, service.getMatchingTerms("A'BCD-123-DEF 456", termMap).size());
    }

    @Test
    public void termMatchStartMela() {

        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName(MELANOBLAST), termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE), termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL), termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOPHORE_STRIPE), termMap);


        String queryString = "mela";
        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(queryString, termMap);
        assertNotNull(matchingTerms);
        assertEquals(4, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals(MELANOBLAST, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("stim", termMap);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("s", termMap);
        assertEquals(2, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("cell", termMap);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
    }


    @Test
    public void termMatchStartMel() {
        PatriciaTrieMultiMap<TermDTO> termMapLarge = new PatriciaTrieMultiMap<TermDTO>();
        for (TermDTO term : createTermsList()) {
            termMapLarge.put(term.getName().toLowerCase(), term);
        }

        String queryString = "mel";
        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(queryString, termMapLarge);
        assertNotNull(matchingTerms);
        assertEquals(7, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals("MeL", iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOBLAST, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());
        assertEquals("MeLm", iterator.next().getMatchingTermDisplay());
        assertEquals("MeLr", iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void matchingObsoletes() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createObsoleteTermWithName(MELANOBLAST), termMap);
        tokenizer.tokenizeTerm(createObsoleteTermWithName(MELANOCYTE), termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL), termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOPHORE_STRIPE), termMap);

        String queryString = "mela";
        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(queryString, termMap);
        assertNotNull(matchingTerms);
        assertEquals(4, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOBLAST + MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE + MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void matchingAliases() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithNameAndAlias(MELANOBLAST, "dental"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias(MELANOCYTE, "melangostar"), termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL), termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOPHORE_STRIPE), termMap);


        // don't really work with this one, then?
        termMap.put("dental", createTermWithNameAndAlias(MELANOBLAST, "dental"));
        termMap.put("melangostar", createTermWithNameAndAlias(MELANOCYTE, "melangostar"));

        // there are only 4 values of type GenericTerm so this is correct
        Collection<TermDTO> termDTOs = termMap.getAllValues();
        for (TermDTO termDTO : termDTOs) {
            logger.debug(termDTO.getName());
        }
        // only add values, not aliases
        assertEquals(4, termMap.getAllValues().size());

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms("mel", termMap);
        assertNotNull(matchingTerms);
        assertEquals(4, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals(MELANOBLAST, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());


        matchingTerms = service.getMatchingTerms("dent", termMap);
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        MatchingTerm matchingTerm = matchingTerms.iterator().next();
        assertEquals(MELANOBLAST + " [dental]", matchingTerm.getMatchingTermDisplay());
    }

    @Test
    public void testDatDuplication() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("retina", "retinas"), termMap);
        assertEquals(2, termMap.keySet().size());
        assertEquals(1, termMap.getAllValues().size());

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms("retina", termMap);
        assertEquals(1, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals("retina", iterator.next().getMatchingTermDisplay());
    }

    /**
     * Ordering factors:
     * - direct hit
     * - is alias
     * - is obsolete
     * <p/>
     * Within these the next set is:
     * - exact match
     * - match on first word
     * - match on later index
     * <p/>
     * Within each one of these, just alphabetize using the AlphaNumeric Comparator
     */
    @Test
    public void testOrderingOfAliasVersusObsoleteVsDirect() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName("dog cat"), termMap);
        tokenizer.tokenizeTerm(createObsoleteTermWithName("dog cat bird"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("cat", "dog cat"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("cat", "pigeon cat"), termMap);
        assertEquals(7, termMap.keySet().size());
        assertEquals(3, termMap.getAllValues().size());

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms("cat", termMap);
        assertEquals(3, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals("cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird" + MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("pig", termMap);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("cat [pigeon cat]", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("dog", termMap);
        assertEquals(3, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("cat [dog cat]", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird" + MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("ca", termMap);
        assertEquals(3, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird" + MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("bir", termMap);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("dog cat bird" + MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void testOrderingForFirstVersusLast() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName("dog cat"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat dog bird"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("bird dog cat"), termMap);
        assertEquals(6, termMap.keySet().size());
        assertEquals(3, termMap.getAllValues().size());

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms("cat", termMap);
        assertEquals(3, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals("cat dog bird", iterator.next().getMatchingTermDisplay());
        assertEquals("bird dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("dog", termMap);
        assertEquals(3, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("bird dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("cat dog bird", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("bir", termMap);
        assertEquals(2, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("bird dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("cat dog bird", iterator.next().getMatchingTermDisplay());
    }

    // tests direct hit
    @Test
    public void testDirectHitOrdering() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName("dog cat"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("dog cat bird"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat"), termMap);

        assertEquals(5, termMap.keySet().size());

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms("cat", termMap);
        assertNotNull(matchingTerms);
        assertEquals(3, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals("cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("dog cat", termMap);
        assertNotNull(matchingTerms);
        assertEquals(2, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird", iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void containsAllContains() {
        String[] strings = {"abcd", "defg"};
        assertTrue(service.containsAllTokens("abcd defg", strings));
        assertFalse(service.containsAllTokens("abcd", strings));
        assertFalse(service.containsAllTokens("defg", strings));
        assertTrue(service.containsAllTokens("abcd defg", new String[]{"ab", "de"}));
        assertFalse(service.containsAllTokens("cat", new String[]{"dog", "cat"}));
        assertFalse(service.containsAllTokens("cat d", new String[]{"dog", "cat"}));
    }

    @Test
    public void sortNumbers() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithName("cat 1"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 2"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 10"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 20"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 21"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 100"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 201"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 110"), termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat 0"), termMap);

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms("ca", termMap);
        assertEquals(9, matchingTerms.size());
        Iterator<MatchingTerm> matchingTermIterator = matchingTerms.iterator();
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 0");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 1");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 2");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 10");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 20");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 21");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 100");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 110");
        assertEquals(matchingTermIterator.next().getTerm().getName(), "cat 201");

    }

    @Test
    public void createTerm() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat", "cat1"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat", "cat2"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat", "cat31"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat", "cat32"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat", "cat4"), termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat", "cat5 doggy"), termMap);

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms("ca", termMap);
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator();
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("dogg", termMap);
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator();
        assertEquals("dog cat [cat5 doggy]", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms("cat3", termMap);
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator();
        // this could be cat31 or cat32
        String displayTerm = iterator.next().getMatchingTermDisplay();
        assertTrue("dog cat [cat32]".equals(displayTerm) || "dog cat [cat31]".equals(displayTerm));
    }


    @Test
    public void multipleValuePerKey() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        assertEquals(0, termMap.size());
        tokenizer.tokenizeTerm(createTermWithName("AAA BBB CCC"), termMap);
        assertEquals(4, termMap.size());
        assertEquals(1, termMap.get("aaa").size());
        tokenizer.tokenizeTerm(createTermWithName("BBB AAA CCC"), termMap);
        assertEquals(5, termMap.size());
        assertEquals(2, termMap.get("aaa").size());

    }

    @Test
    public void bestAliasMatch() {
        PatriciaTrieMultiMap<TermDTO> termMap = new PatriciaTrieMultiMap<TermDTO>();
        assertEquals(0, termMap.size());
        TermDTO term = createTermWithName("mitotic cell cycle DNA replication checkpoint");
        Set<String> aliases = new HashSet<String>(2);
        aliases.add("S-M DNA replication checkpoint");
        aliases.add("S-M checkpoint");
        term.setAliases(aliases);
        tokenizer.tokenizeTerm(term, termMap);
        assertEquals(10, termMap.size());
        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> matches = service.getMatchingTerms("s-m checkpoint", termMap);
        assertEquals(1, matches.size());
        assertEquals("S-M checkpoint", matches.iterator().next().getAlias());
    }


    //    @Before
    public static List<TermDTO> createTermsList() {
        List<TermDTO> sampleTerms = new ArrayList<TermDTO>(20);
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

        return sampleTerms;

    }

    public static TermDTO createObsoleteTermWithName(String name) {
        TermDTO term = createTermWithName(name);
        term.setObsolete(true);
        return term;
    }

    public static TermDTO createTermWithName(String name) {
        TermDTO term = new TermDTO();
        term.setName(name);
        return term;
    }

    public static TermDTO createTermWithNameAndAlias(String name, String aliasName) {
        TermDTO term = new TermDTO();
        term.setName(name);

//        String alias = alalias = new TermDTO();
//        alias.setName(aliasName);
////        alias.setTerm(term);

        Set<String> aliases = new HashSet<String>();
        aliases.add(aliasName);

        term.setAliases(aliases);
//        term.setA
        return term;
    }


}
