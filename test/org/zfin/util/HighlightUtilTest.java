package org.zfin.util;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * test HighlightUtil.
 */
public class HighlightUtilTest {


    @Test
    public void highlightBegining() {
        String text = "anti-DLX3b";
        String highlightString = "anTi-";
        String hightlightString  = HighlightUtil.highlightMatchHTML(text, highlightString, false);

        assertEquals("<b>anti-</b>DLX3b", hightlightString);
    }

    @Test
    public void highlightMiddle() {
        String text = "anti-DLX3b";
        String highlightString = "dlX";
        String hightlightString  = HighlightUtil.highlightMatchHTML(text, highlightString, false);

        assertEquals("anti-<b>DLX</b>3b", hightlightString);
    }

    @Test
    public void highlightEnd() {
        String text = "anti-DLX3b";
        String highlightString = "dlX3b";
        highlightString  = HighlightUtil.highlightMatchHTML(text, highlightString, false);

        assertEquals("anti-<b>DLX3b</b>", highlightString);
    }

    @Test
    public void highlightNoMatch() {
        String text = "anti-DLX3b";
        String highlightString = "dlX3g";
        highlightString  = HighlightUtil.highlightMatchHTML(text, highlightString, false);

        assertEquals(text, highlightString);
    }

}