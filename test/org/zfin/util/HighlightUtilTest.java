package org.zfin.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * test HighlightUtil.
 */
public class HighlightUtilTest {


    @Test
    public void hightlightBegining() {
        String text = "anti-DLX3b";
        String highlightString = "anTi-";

        String hightlightString  = HighlightUtil.hightlightMatchHTML(text, highlightString, false);

        assertEquals("<b>anti-</b>DLX3b", hightlightString);
    }

    @Test
    public void hightlightMiddle() {
        String text = "anti-DLX3b";
        String highlightString = "dlX";

        String hightlightString  = HighlightUtil.hightlightMatchHTML(text, highlightString, false);

        assertEquals("anti-<b>DLX</b>3b", hightlightString);
    }

    @Test
    public void hightlightEnd() {
        String text = "anti-DLX3b";
        String highlightString = "dlX3b";

        String hightlightString  = HighlightUtil.hightlightMatchHTML(text, highlightString, false);

        assertEquals("anti-<b>DLX3b</b>", hightlightString);
    }

    @Test
    public void hightlightNoMatch() {
        String text = "anti-DLX3b";
        String highlightString = "dlX3g";

        String hightlightString  = HighlightUtil.hightlightMatchHTML(text, highlightString, false);

        assertEquals(text, hightlightString);
    }

}