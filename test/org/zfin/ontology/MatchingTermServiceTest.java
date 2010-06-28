package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.infrastructure.PatriciaTrieMultiMap;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchingTermServiceTest {

    private final Logger logger = Logger.getLogger(MatchingTermServiceTest.class) ;

    private static final String MELANOBLAST = "melanoblast";
    private static final String MELANOCYTE = "melanocyte";
    private static final String MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL = "melanocyte stimulating hormone secreting cell";
    private static final String MELANOPHORE_STRIPE = "melanophore stripe";
    MatchingTermService service = new MatchingTermService();
    OntologyTokenizer tokenizer = new OntologyTokenizer() ;

    @Test
    public void testSplitTrim(){
        String s = "  aaaa    ggggg  " ;
        String[] strings = s.trim().split("\\s+") ;
        assertEquals(2,strings.length);
        assertEquals("aaaa",strings[0]);
        assertEquals("ggggg",strings[1]);
    }

    private void dumpKeys(PatriciaTrieMultiMap<Term> termMap){
        for(String key: termMap.keySet()){
            logger.debug(key);
        }
    }

    // for hit "A B C", should find (in this order) "A B C" (first), "A B" (second), "A","B", "C", "A C" (any order), "B C" (last)
    @Test
    public void testFindWithin(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName("AAA BBB CCC"),termMap);
        dumpKeys(termMap);
        assertEquals(1,service.getMatchingTerms(termMap,"aaa bbb ccc").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"A").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"B").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"C").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"a b").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"b c").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"ccc bbb").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"aaa ccc").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"ccc aaa").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"bbb aaa").size()) ;
    }

    // test the order
    @Test
    public void testFindWithinOrder(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName("AAA BBB CCC"),termMap);
        tokenizer.tokenizeTerm(createTermWithName("BBB AAA CCC"),termMap);
        tokenizer.tokenizeTerm(createTermWithName("CCC AAA BBB"),termMap);
        dumpKeys(termMap);
        Set<MatchingTerm> matchingTerms ;
        Iterator<MatchingTerm> iterator ;
        
        matchingTerms = service.getMatchingTerms(termMap,"a b c") ;
        iterator = matchingTerms.iterator() ;
        assertEquals(3,matchingTerms.size()) ;
        assertEquals("AAA BBB CCC",iterator.next().getTerm().getTermName()) ;
        assertEquals("BBB AAA CCC",iterator.next().getTerm().getTermName()) ;
        assertEquals("CCC AAA BBB",iterator.next().getTerm().getTermName()) ;
        
        matchingTerms = service.getMatchingTerms(termMap,"b a c") ;
        iterator = matchingTerms.iterator() ;
        assertEquals(3,matchingTerms.size()) ;
        assertEquals("AAA BBB CCC",iterator.next().getTerm().getTermName()) ;
        assertEquals("BBB AAA CCC",iterator.next().getTerm().getTermName()) ;
        assertEquals("CCC AAA BBB",iterator.next().getTerm().getTermName()) ;
        matchingTerms = service.getMatchingTerms(termMap,"c a b") ;


    }

    // for hit "A B C", should find (in this order) "A B C" (first), "A B" (second), "A","B", "C", "A C" (any order), "B C" (last)
    @Test
    public void testFindWithinRealWords(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL),termMap);
        assertEquals(1,service.getMatchingTerms(termMap,"melan stim hor").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"melan").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"stim").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"hor").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"melan stim").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"stim horm").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"melan horm").size()) ;
    }



    // for hits "ABC-123",
    @Test
    public void testTokenizeEntireWord(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName("A'BCD-123-DEF GHI-456"),termMap);
        assertEquals(1,termMap.getAllValues().size()) ;
        assertEquals(8,termMap.size()) ;
        assertEquals(0,service.getMatchingTerms(termMap,"abc").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"bcd").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"ghi").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"456").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"ghi-456").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"a'bcd").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"a'bcd-123-def ghi-456").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"A'BCD-123-DEF GHI-456").size()) ;
        assertEquals(1,service.getMatchingTerms(termMap,"A'BCD-123-DEF 456").size()) ;
    }

    @Test
    public void termMatchStartMela() {

        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName(MELANOBLAST),termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE),termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL),termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOPHORE_STRIPE),termMap);


        String queryString = "mela";
        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,queryString) ;
        assertNotNull(matchingTerms);
        assertEquals(4, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals(MELANOBLAST, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"stim") ;
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"s") ;
        assertEquals(2, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"cell") ;
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
    }


    @Test
    public void termMatchStartMel() {
        PatriciaTrieMultiMap<Term> termMapLarge = new PatriciaTrieMultiMap<Term>() ;
        for(Term term: createTermsList()){
            termMapLarge.put(term.getTermName().toLowerCase(),term) ;
        }

        String queryString = "mel";
        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMapLarge,queryString) ;
        assertNotNull(matchingTerms);
        assertEquals(7, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals("MeL", iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOBLAST, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());
        assertEquals("MeLm", iterator.next().getMatchingTermDisplay());
        assertEquals("MeLr", iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void matchingObsoletes(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createObsoleteTermWithName(MELANOBLAST),termMap);
        tokenizer.tokenizeTerm(createObsoleteTermWithName(MELANOCYTE),termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL),termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOPHORE_STRIPE),termMap);

        String queryString = "mela";
        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,queryString) ;
        assertNotNull(matchingTerms);
        assertEquals(4, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOBLAST +MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE+MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void matchingAliases(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithNameAndAlias(MELANOBLAST,"dental"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias(MELANOCYTE,"melangostar"),termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL),termMap);
        tokenizer.tokenizeTerm(createTermWithName(MELANOPHORE_STRIPE),termMap);


        // don't really work with this one, then?
        termMap.put("dental",createTermWithNameAndAlias(MELANOBLAST,"dental")) ;
        termMap.put("melangostar",createTermWithNameAndAlias(MELANOCYTE,"melangostar")) ;

        // there are only 4 values of type GenericTerm so this is correct
        for(Object o : termMap.getAllValues()){
            Term t = (Term) o ;
            logger.debug(t.getTermName());
        }
        assertEquals(4,termMap.getAllValues().size());

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,"mel") ;
        assertNotNull(matchingTerms);
        assertEquals(4, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals(MELANOBLAST, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOCYTE_STIMULATING_HORMONE_SECRETING_CELL, iterator.next().getMatchingTermDisplay());
        assertEquals(MELANOPHORE_STRIPE, iterator.next().getMatchingTermDisplay());


        matchingTerms = service.getMatchingTerms(termMap,"dent") ;
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        MatchingTerm matchingTerm = matchingTerms.iterator().next() ;
        assertEquals(MELANOBLAST +" [dental]", matchingTerm.getMatchingTermDisplay());
    }

    @Test
    public void testDatDuplication(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("retina","retinas"),termMap);
        assertEquals(2,termMap.keySet().size()) ;
        assertEquals(1,termMap.getAllValues().size()) ;

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,"retina") ;
        assertEquals(1, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals("retina", iterator.next().getMatchingTermDisplay());
    }

    /**
     * Ordering factors:
     * - direct hit
     * - is alias
     * - is obsolete
     *
     * Within these the next set is:
     * - exact match
     * - match on first word
     * - match on later index
     *
     * Within each one of these, just alphabetize using the AlphaNumeric Comparator
     */
    @Test
    public void testOrderingOfAliasVersusObsoleteVsDirect(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName("dog cat"),termMap);
        tokenizer.tokenizeTerm(createObsoleteTermWithName("dog cat bird"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("cat","dog cat"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("cat","pigeon cat"),termMap);
        assertEquals(7,termMap.keySet().size()) ;
        assertEquals(3,termMap.getAllValues().size()) ;

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,"cat") ;
        assertEquals(3, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals("cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird" +MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"pig") ;
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("cat [pigeon cat]", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"dog") ;
        assertEquals(3, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("cat [dog cat]", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird" +MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"ca") ;
        assertEquals(3, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird" +MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"bir") ;
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("dog cat bird" +MatchingTerm.OBSOLETE_SUFFIX, iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void testOrderingForFirstVersusLast(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName("dog cat"),termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat dog bird"),termMap);
        tokenizer.tokenizeTerm(createTermWithName("bird dog cat"),termMap);
        assertEquals(6,termMap.keySet().size()) ;
        assertEquals(3,termMap.getAllValues().size()) ;

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,"cat") ;
        assertEquals(3, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals("cat dog bird", iterator.next().getMatchingTermDisplay());
        assertEquals("bird dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"dog") ;
        assertEquals(3, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("bird dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("cat dog bird", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"bir") ;
        assertEquals(2, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("bird dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("cat dog bird", iterator.next().getMatchingTermDisplay());
    }

    // tests direct hit
    @Test
    public void testDirectHitOrdering(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithName("dog cat"),termMap);
        tokenizer.tokenizeTerm(createTermWithName("dog cat bird"),termMap);
        tokenizer.tokenizeTerm(createTermWithName("cat"),termMap);

        assertEquals(5,termMap.keySet().size()) ;

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,"cat") ;
        assertNotNull(matchingTerms);
        assertEquals(3, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals("cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"dog cat") ;
        assertNotNull(matchingTerms);
        assertEquals(2, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());
        assertEquals("dog cat bird", iterator.next().getMatchingTermDisplay());
    }

    @Test
    public void containsAllContains(){
        String[] strings = {"abcd","defg"} ;
        assertTrue(service.containsAllTokens("abcd defg",strings)) ;
        assertFalse(service.containsAllTokens("abcd",strings)) ;
        assertFalse(service.containsAllTokens("defg",strings)) ;
        assertTrue(service.containsAllTokens("abcd defg",new String[]{"ab","de"})) ;
        assertFalse(service.containsAllTokens("cat",new String[]{"dog","cat"})); ;
        assertFalse(service.containsAllTokens("cat d",new String[]{"dog","cat"})); ;
    }

    @Test
    public void createTerm(){
        PatriciaTrieMultiMap<Term> termMap = new PatriciaTrieMultiMap<Term>() ;
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat","cat1"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat","cat2"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat","cat31"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat","cat32"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat","cat4"),termMap);
        tokenizer.tokenizeTerm(createTermWithNameAndAlias("dog cat","cat5 doggy"),termMap);

        Set<MatchingTerm> matchingTerms = service.getMatchingTerms(termMap,"ca") ;
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        Iterator<MatchingTerm> iterator = matchingTerms.iterator() ;
        assertEquals("dog cat", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"dogg") ;
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        assertEquals("dog cat [cat5 doggy]", iterator.next().getMatchingTermDisplay());

        matchingTerms = service.getMatchingTerms(termMap,"cat3") ;
        assertNotNull(matchingTerms);
        assertEquals(1, matchingTerms.size());
        iterator = matchingTerms.iterator() ;
        // this could be cat31 or cat32
        String displayTerm = iterator.next().getMatchingTermDisplay();
        assertTrue("dog cat [cat32]".equals(displayTerm)  || "dog cat [cat31]".equals(displayTerm));
    }



//    @Before
    public static List<Term> createTermsList() {
        List<Term> sampleTerms = new ArrayList<Term>(20);
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

    public static Term createObsoleteTermWithName(String name){
        Term term = createTermWithName(name);
        term.setObsolete(true);
        return term ;
    }

    public static Term createTermWithName(String name) {
        Term term = new GenericTerm();
        term.setTermName(name);
        return term;
    }

    public static Term createTermWithNameAndAlias(String name, String aliasName) {
        GenericTerm term = new GenericTerm();
        term.setTermName(name);

        TermAlias alias = new TermAlias();
        alias.setAlias(aliasName);
        alias.setTerm(term);

        Set<TermAlias> aliases = new HashSet<TermAlias>() ;
        aliases.add(alias) ;

        term.setAliases(aliases);
//        term.setA
        return term ;
    }



}