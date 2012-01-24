package org.zfin.framework;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class ZfinSimpleTokenizerTest {

    @Test
    public void tokenizePureAlphaCharacters() {
        // no stop words
        String text = "pure text with alpha characters only";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 6, tokenizer.getNumberOfTokens());
        assertEquals("First token", "pure", tokenizer.next());
        assertEquals("Second token", "text", tokenizer.next());
        assertEquals("Third token", "with", tokenizer.next());
        assertEquals("Fourth token", "alpha", tokenizer.next());
        assertEquals("Fifth token", "characters", tokenizer.next());
        assertEquals("Sixth token", "only", tokenizer.next());
    }

    @Test
    public void tokenizeHonorNoStopWords() {
        // no stop words
        String text = "This is a bee";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 4, tokenizer.getNumberOfTokens());
        assertEquals("First token", "This", tokenizer.next());
        assertEquals("Second token", "is", tokenizer.next());
        assertEquals("Third token", "a", tokenizer.next());
        assertEquals("Fourth token", "bee", tokenizer.next());
    }

    @Test
    public void tokenizePureNumeralCharacters() {
        String text = "1234 2345 5567";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 3, tokenizer.getNumberOfTokens());
        assertEquals("First token", "1234", tokenizer.next());
        assertEquals("Second token", "2345", tokenizer.next());
        assertEquals("Third token", "5567", tokenizer.next());
        try {
            tokenizer.next();
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void tokenizeAlphaNumericalCharacters() {
        // no stop words
        String text = "pure text with 123 44";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 5, tokenizer.getNumberOfTokens());
        assertEquals("First token", "pure", tokenizer.next());
        assertEquals("Second token", "text", tokenizer.next());
        assertEquals("Third token", "with", tokenizer.next());
        assertEquals("Fourth token", "123", tokenizer.next());
        assertEquals("Fifth token", "44", tokenizer.next());
    }

    @Test
    public void tokenizeAlphaNumericalCharactersMixed() {
        // no stop words
        String text = "pure text12 with23a ";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 3, tokenizer.getNumberOfTokens());
        assertEquals("First token", "pure", tokenizer.next());
        assertEquals("Second token", "text12", tokenizer.next());
        assertEquals("Third token", "with23a", tokenizer.next());
    }

    @Test
    public void tokenizeAlphaNumPlusDot() {
        // no stop words
        String text = "pure text12.a with23a ";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 4, tokenizer.getNumberOfTokens());
        assertEquals("First token", "pure", tokenizer.next());
        assertEquals("Second token", "text12", tokenizer.next());
        assertEquals("Second token", "a", tokenizer.next());
        assertEquals("Third token", "with23a", tokenizer.next());
    }

    @Test
    public void tokenizeAlphaNumPlusDotParentheses() {
        // no stop words
        String text = "Tg(pou4f3) ";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 2, tokenizer.getNumberOfTokens());
        assertEquals("First token", "Tg", tokenizer.next());
        assertEquals("Second token", "pou4f3", tokenizer.next());
    }

    @Test
    public void tokenizeAlphaNumPlusDotParenthesesColon() {
        // no stop words
        String text = "Tg(pou4f3:gap43-GFP)s356t ";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 5, tokenizer.getNumberOfTokens());
        assertEquals("First token", "Tg", tokenizer.next());
        assertEquals("Second token", "pou4f3", tokenizer.next());
        assertEquals("Third token", "gap43", tokenizer.next());
        assertEquals("Foutth token", "GFP", tokenizer.next());
        assertEquals("Fifth token", "s356t", tokenizer.next());
    }

    @Test
    public void tokenizeAlphaNumPlusDotParenthesesColonStartingNumber() {
        // no stop words
        String text = "Tg(-2.4pou4f3:gap43-GFP)s356t ";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 6, tokenizer.getNumberOfTokens());
        assertEquals("First token", "Tg", tokenizer.next());
        assertEquals("Second token", "2", tokenizer.next());
        assertEquals("Third token", "4pou4f3", tokenizer.next());
        assertEquals("Fourth token", "gap43", tokenizer.next());
        assertEquals("Fifth token", "GFP", tokenizer.next());
        assertEquals("Sixth token", "s356t", tokenizer.next());
    }

    @Test
    public void tokenizeColon() {
        // no stop words
        String text = "zgc:113262";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 2, tokenizer.getNumberOfTokens());
        assertEquals("First token", "zgc", tokenizer.next());
        assertEquals("Second token", "113262", tokenizer.next());
    }

    @Test
    public void tokenizeComplexString() {
        // no stop words
        String text = "GATA-6-TP miR-145";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 5, tokenizer.getNumberOfTokens());
        assertEquals("First token", "GATA", tokenizer.next());
        assertEquals("Second token", "6", tokenizer.next());
    }

    @Test
    public void tokenizeWithPuncutation() {
        // no stop words
        String text = "etID42255.12";
        ZfinSimpleTokenizer tokenizer = new ZfinSimpleTokenizer(text);
        assertEquals("Number of tokens", 2, tokenizer.getNumberOfTokens());
        assertEquals("First token", "etID42255", tokenizer.next());
        assertEquals("Second token", "12", tokenizer.next());
    }

}
