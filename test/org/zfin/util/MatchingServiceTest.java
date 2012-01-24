package org.zfin.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchingServiceTest {

    @Test
    public void testWordMatch() {
        String queryString = "mir";
        String matchingText = "GATA-6-TP miR-145";
        assertTrue(MatchType.EXACT_WORD.isMatch(matchingText, queryString));

        matchingText = "Tg(mir124-5:GFP)wi124";
        assertFalse(MatchType.EXACT_WORD.isMatch(matchingText, queryString));
        assertTrue(MatchType.STARTS_WITH_WORDS.isMatch(matchingText, queryString));
    }

    @Test
    public void testWordMatchWithColon() {
        String queryString = "mpz";
        String matchingText = "mp:zf637-3-001691";
        assertFalse(MatchType.EXACT_WORD.isMatch(matchingText, queryString));
        assertFalse(MatchType.EXACT.isMatch(matchingText, queryString));
        assertFalse(MatchType.STARTS_WITH.isMatch(matchingText, queryString));
        assertFalse(MatchType.STARTS_WITH_WORDS.isMatch(matchingText, queryString));
    }

    @Test
    public void testMultipleWordsInQuery() {
        String queryString = "sonic hedgehog a";
        String matchingText = "sonic blue and pink hedgehog a and another gene name";
        assertTrue(MatchType.EXACT_WORD.isMatch(matchingText, queryString));
    }

    @Test
    public void testMultipleWordsInQuerySingleMatch() {
        String queryString = "oep gfp";
        String matchingText = "oep";
        assertFalse(MatchType.EXACT_WORD.isMatch(matchingText, queryString));
        assertFalse(MatchType.STARTS_WITH_WORDS.isMatch(matchingText, queryString));
    }

    @Test
    public void testColonMatch() {
        String matchingText = "Tg(-2.7shha:GFP) ";
        String queryString = "shha:GFP";
        assertFalse(MatchType.EXACT_WORD.isMatch(matchingText, queryString));
        assertFalse(MatchType.STARTS_WITH_WORDS.isMatch(matchingText, queryString));
        assertTrue(MatchType.CONTAINS.isMatch(matchingText, queryString));
    }
}
