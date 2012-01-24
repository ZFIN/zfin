package org.zfin.util;

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

    //@Test
    public void highlightSpecialCharacter() {
        String text = "mp:zf637-3-001691";
        String highlightString = "300";
        highlightString  = HighlightUtil.highlightSmartMatchHTML(text, highlightString, false, true);
        assertEquals("mp:zf637-<b>3-00</b>1691", highlightString);
        highlightString  = HighlightUtil.highlightSmartMatchHTML(text, "mpz", false, true);
        assertEquals("<b>mp:z</b>f637-3-001691", highlightString);
        highlightString  = HighlightUtil.highlightSmartMatchHTML(text, "pzf", false, true);
        assertEquals("m<b>p:zf</b>637-3-001691", highlightString);
        highlightString  = HighlightUtil.highlightSmartMatchHTML(text, "pzf6373", false, true);
        assertEquals("m<b>p:zf637-3</b>-001691", highlightString);
        highlightString  = HighlightUtil.highlightSmartMatchHTML(text, "pzf63730", false, true);
        assertEquals("m<b>p:zf637-3-0</b>01691", highlightString);
    }

    //@Test
    public void highlightSpecialCharacterInQueryString() {
        String text = "B-ACTZF";
        String highlightString = "b-actzf";
        highlightString  = HighlightUtil.highlightSmartMatchHTML(text, highlightString, false, true);
        assertEquals("<b>B-ACTZF</b>", highlightString);
    }

}