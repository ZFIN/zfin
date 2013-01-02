package org.zfin.util;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.gwt.root.util.StringUtils;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtilsTest {

    public static Logger logger = Logger.getLogger(ZfinStringUtils.class);

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
        maxLength = 0;
        try {
            truncatedText = ZfinStringUtils.getTruncatedString(text, maxLength);
        } catch (Exception e) {
            assertTrue(e.getMessage().startsWith("The maximum length "));
        }

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

}