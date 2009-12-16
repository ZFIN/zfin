package org.zfin.util;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Test class for checking regular expressions.
 */
public class RegularExpressionTest {

    @Test
    public void matchAndRetrieveMatchedString() {
        Pattern regExpPattern = Pattern.compile("\\$\\{\\w*\\}");
        Matcher matcher = regExpPattern.matcher("${XB}-Otto");
        assertTrue(matcher.find());
        String match = matcher.group();
        String replacement = matcher.replaceAll("Karl");

        assertEquals("Karl-Otto", replacement);

    }

    @Test
    public void matchAndRetrieveMatchedStringWithUnderscore() {
        Pattern regExpPattern = Pattern.compile("\\$\\{\\w*\\}");
        Matcher matcher = regExpPattern.matcher("${Community_WIKI}-Otto");
        assertTrue(matcher.find());
        String match = matcher.group();
        String replacement = matcher.replaceAll("Karl");

        assertEquals("Karl-Otto", replacement);

    }
}
