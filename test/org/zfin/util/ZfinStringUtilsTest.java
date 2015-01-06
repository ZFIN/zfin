package org.zfin.util;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtilsTest {

    public static final Logger logger = Logger.getLogger(ZfinStringUtils.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void splitStringMethod() {
        String name = "retina,brain";
        String[] array = name.split(",");
        assertEquals(2, array.length);

        name = "retina@@brain";
        array = name.split("@@");
        assertEquals(2, array.length);

        name = "retina|brain";
        array = name.split("\\|");
        assertEquals(2, array.length);
    }

    @Test
    public void truncateText() {
        String text = null;
        int maxLength = 4;
        String truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertNull(truncatedText);

        text = "harry der Grosse";
        maxLength = 12;
        truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertEquals("harry der ...", truncatedText);

        text = "harry der Grosse";
        maxLength = 8;
        truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertEquals("harry ...", truncatedText);

        text = "harry der Grosse";
        maxLength = 4;
        truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        assertEquals("har...", truncatedText);
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
        assertEquals("<table><tr><td>page</td><td>1</td></tr><tr><td>antibodyCriteria.antibodyNameFilterType</td><td>contains</td></tr><tr><td>antibodyCriteria.name</td><td></td></tr></table>", htmlIzed);
    }

    @Test
    public void detectWhiteSpaces() {
        String text = " ??  9907- 00o ";
        List<Integer> whiteSpaces = ZfinStringUtils.detectWhiteSpaces(text);
        assertNotNull(whiteSpaces);
        assertEquals(5, whiteSpaces.size());
        assertTrue(whiteSpaces.contains(0));
        assertTrue(whiteSpaces.contains(3));
        assertTrue(whiteSpaces.contains(4));
        assertTrue(whiteSpaces.contains(10));
        assertTrue(whiteSpaces.contains(14));
    }

    @Test
    public void interningString() {
        List<TermDTO> list = new ArrayList<TermDTO>(100000);
        for (int index = 0; index < 100000; index++) {
            TermDTO term = new TermDTO();
            term.setRelationshipType("is_a");
            list.add(term);
        }
        assertTrue(list.size() > 1000);
    }

    @Test
    public void interningStringSimple() {
        List<String> list = new ArrayList<String>(100000);
        for (int index = 0; index < 100000; index++) {
            list.add(new String("is_a"));
        }
        assertTrue(list.size() > 1000);
        // two strings are equal by equals()
        assertTrue(list.get(0).equals(list.get(1)));

        // the two strings are not equal reference
        assertFalse(list.get(0) == list.get(1));
    }

    @Test
    public void interningStringSimplePool() {
        List<String> list = new ArrayList<String>(100000);
        for (int index = 0; index < 100000; index++) {
            list.add((new String("is_a")).intern());
        }
        assertTrue(list.size() > 1000);
        // two strings are equal by equals()
        assertTrue(list.get(0).equals(list.get(1)));

        // the two strings are not equal reference
        assertTrue(list.get(0) == list.get(1));
    }


    @Test
    public void stripHighUnicodeTest() {

        //European characters in names should pass through
        String before = "Torres-Nuñez";
        String after = ZfinStringUtils.escapeHighUnicode(before);
        logger.debug("BEFORE: " + before);
        logger.debug(" AFTER: " + after);
        assertEquals(before, after);


        //bullet & endash characters should not!
        before = "Am Klopferspitz 18 • D-82152 Martinsried, phone +49 89 8578 3263 • fax +49 89 8578 3240";
        after = ZfinStringUtils.escapeHighUnicode(before);
        logger.debug("BEFORE: " + before);
        logger.debug(" AFTER: " + after);

        assertTrue(!StringUtils.equals(before,after));


        before = Character.toString((char)2012);
        after = ZfinStringUtils.escapeHighUnicode(before);
        logger.debug("BEFORE: " + before);
        logger.debug(" AFTER: " + after);

        assertTrue(StringUtils.equals(after,"&#2012;"));

    }

    @Test
    public void testRegExp(){

        String pattern = "/ZDB-GENE-\\p{ASCII}*";
        String url = "http://eselsohr.zfin.org/ZDB-GENE-030616-10";
        Pattern regExpPattern = Pattern.compile(pattern);
        Matcher matcher = regExpPattern.matcher(url);
        assertTrue(matcher.find());

        pattern = "/ZDB-(GENE|ATB|BAC)-\\p{ASCII}*";
        url = "http://eselsohr.zfin.org/ZDB-ATB-030616-10";
        regExpPattern = Pattern.compile(pattern);
        matcher = regExpPattern.matcher(url);
        assertTrue(matcher.find());
    }

}
