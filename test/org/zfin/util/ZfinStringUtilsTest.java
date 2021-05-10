package org.zfin.util;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zfin.marker.Marker;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtilsTest {

    public static final Logger logger = LogManager.getLogger(ZfinStringUtils.class);

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

    @Test
    public void isValidNucleotideSequenceShouldReturnFalseForSequenceWithR() {
        String input = "GCGCCATTGCTTGCARAGAATTG";
        Boolean output = ZfinStringUtils.isValidNucleotideSequence(input);
        assertThat("isValidNucleotideSequence should return false for " + input,
                output, is(false));
    }

    @Test
    public void isValidNucleotideSequenceShouldReturnTrueForTALENSequenceWithR() {
        String input = "GCGCCATTGCTTGCARAGAATTG";
        Boolean output = ZfinStringUtils.isValidNucleotideSequence(input, Marker.Type.TALEN);
        assertThat("isValidNucleotideSequence should return true for " + input,
                output, is(true));
    }

    @Test
    public void removeHtmlTagsShouldRemoveTagsFromAroundGeneName() {
        String input = "regenerating fin <i>lamb1a</i> expression decreased amount, abnormal";
        String expected = "regenerating fin lamb1a expression decreased amount, abnormal";
        assertThat(ZfinStringUtils.removeHtmlTags(input), is(expected));
    }

}
