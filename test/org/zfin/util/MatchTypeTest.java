package org.zfin.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MatchTypeTest {

    private MatchingService service = new MatchingService();

    @Test
    public void exactMatch() {
        String query = "SingleWord";
        String matchingString = "singleword";
        MatchType type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.EXACT, type);
    }

    @Test
    public void exactStartsWith() {
        String query = "SingleWord";
        String matchingString = "singlewords";
        MatchType type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.STARTS_WITH, type);
    }

    @Test
    public void exactContains() {
        String query = "word";
        String matchingString = "SingleWords";
        MatchType type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.CONTAINS, type);
    }

    @Test
    public void exactExactWord() {
        String query = "word";
        String matchingString = "SingleWords";
        MatchingService service = new MatchingService(MatchType.EXACT, MatchType.EXACT_WORD, MatchType.STARTS_WITH, MatchType.CONTAINS);
        MatchType type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.CONTAINS, type);

        query = "two word";
        matchingString = "There is a word in two sentences.";
        service = new MatchingService(MatchType.EXACT, MatchType.EXACT_WORD, MatchType.STARTS_WITH, MatchType.CONTAINS);
        type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.EXACT_WORD, type);

        query = "two words";
        matchingString = "There is a word in two sentences.";
        service = new MatchingService(MatchType.EXACT, MatchType.EXACT_WORD, MatchType.STARTS_WITH, MatchType.CONTAINS);
        type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.NO_MATCH, type);
    }

    @Test
    public void startsWithWord() {
        String query = "mir1";
        String matchingString = "GATA-6-TP miR-145";
        MatchingService service = new MatchingService(MatchType.EXACT, MatchType.EXACT_WORD, MatchType.STARTS_WITH, MatchType.CONTAINS);
        MatchType type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.NO_MATCH, type);

        service = new MatchingService(MatchType.STARTS_WITH_WORDS);
        type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.NO_MATCH, type);

    }

    @Test
    public void matchOnWord() {
        String query = "mpz";
        String matchingString = "Tg(Hsa.MPZ_c.126-1026T>A:EGFP)";
        MatchingService service = new MatchingService(MatchType.EXACT, MatchType.EXACT_WORD, MatchType.STARTS_WITH);
        MatchType type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.NO_MATCH, type);

        service = new MatchingService(MatchType.STARTS_WITH_WORDS);
        type = service.checkMatch(query, matchingString);
        assertEquals(MatchType.STARTS_WITH_WORDS, type);

    }
}