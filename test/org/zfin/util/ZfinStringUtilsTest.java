package org.zfin.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtilsTest {

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
}