package org.zfin.util;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtilsTest {

    public static final Logger logger = Logger.getLogger(ZfinStringUtils.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void truncateText() {
        String text = null;
        int maxLength = 4;
        String truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertThat(truncatedText, is(nullValue()));

        text = "harry der Grosse";
        maxLength = 12;
        truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertThat(truncatedText, is("harry der ..."));

        text = "harry der Grosse";
        maxLength = 8;
        truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertThat(truncatedText, is("harry ..."));

        text = "harry der Grosse";
        maxLength = 4;
        truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertThat(truncatedText, is("har..."));
    }

    @Test
    public void truncateTextMaxLengthZero() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("The maximum length ");
        String text = "harry der Grosse";
        int maxLength = 0;
        ZfinStringUtils.getTruncatedString(text, maxLength);
    }

    @Test
    public void truncateQueryStringFormatting() {
        String text = "page=1&antibodyCriteria.antibodyNameFilterType=contains&antibodyCriteria.name";
        String htmlIzed = ZfinStringUtils.getHtmlTableFromQueryString(text);
        assertThat(htmlIzed, is("<table><tr><td>page</td><td>1</td></tr><tr><td>antibodyCriteria.antibodyNameFilterType</td><td>contains</td></tr><tr><td>antibodyCriteria.name</td><td></td></tr></table>"));
    }

    @Test
    public void detectWhiteSpaces() {
        String text = " ??  9907- 00o ";
        List<Integer> whiteSpaces = ZfinStringUtils.detectWhiteSpaces(text);
        assertThat(whiteSpaces, is(notNullValue()));
        assertThat(whiteSpaces, hasSize(5));
        assertThat(whiteSpaces, contains(0, 3, 4, 10, 14));
    }

    @Test
    public void stripHighUnicodeTest() {
        //European characters in names should pass through
        String before = "Torres-Nuñez";
        String after = ZfinStringUtils.escapeHighUnicode(before);
        logger.debug("BEFORE: " + before);
        logger.debug(" AFTER: " + after);
        assertThat(after, is(before));

        //bullet & endash characters should not!
        before = "Am Klopferspitz 18 • D-82152 Martinsried, phone +49 89 8578 3263 • fax +49 89 8578 3240";
        after = ZfinStringUtils.escapeHighUnicode(before);
        logger.debug("BEFORE: " + before);
        logger.debug(" AFTER: " + after);
        assertThat(after, is(not(before)));

        before = Character.toString((char) 2012);
        after = ZfinStringUtils.escapeHighUnicode(before);
        logger.debug("BEFORE: " + before);
        logger.debug(" AFTER: " + after);
        assertThat(after, is("&#2012;"));
    }

    @Test
    public void isValidNucleotideSequenceShouldReturnTrueForValidSequence() {
        String input = "GCGCCATTGCTTTGCAAGAATTG";
        Boolean output = ZfinStringUtils.isValidNucleotideSequence(input);
        assertThat("isValidNucleotideSequence should return false for " + input,
                output, is(true));
    }

    @Test
    public void isValidNucleotideSequenceShouldReturnFalseForSequenceWithSpace() {
        String input = "GCGCCATTGCT TTGCAAGAATTG";
        Boolean output = ZfinStringUtils.isValidNucleotideSequence(input);
        assertThat("isValidNucleotideSequence should return true for " + input,
                output, is(false));
    }

    @Test
    public void isValidNucleotideSequenceShouldReturnFalseForSequenceWithQ() {
        String input = "GCGCCATTGCTQTGCAAGAATTG";
        Boolean output = ZfinStringUtils.isValidNucleotideSequence(input);
        assertThat("isValidNucleotideSequence should return true for " + input,
                output, is(false));
    }

}
