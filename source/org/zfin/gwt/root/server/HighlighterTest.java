package org.zfin.gwt.root.server;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class HighlighterTest {


    @Test
    public void highlightTest(){
        assertEquals("<strong>bob</strong>by jones",new Highlighter("bob").highlight("bobby jones"));
        assertEquals("<strong>bobby</strong> <strong>j</strong>ones",new Highlighter("bobby j").highlight("bobby jones"));
        assertEquals("<strong>Bobby</strong> <strong>J</strong>ones",new Highlighter("bobby j").highlight("Bobby Jones"));
        assertEquals("<strong>J</strong>ones <strong>Bobby</strong>",new Highlighter("bobby j").highlight("Jones Bobby"));
        assertEquals("<strong>Stron</strong>g <strong>Bob</strong>by",new Highlighter("stron bob").highlight("Strong Bobby"));
        assertEquals("<strong>Stron</strong>g <strong>Bob</strong>by",new Highlighter("bob stron").highlight("Strong Bobby"));
        assertEquals("<strong>Bobby</strong> A <strong>J</strong>ones",new Highlighter("bobby j").highlight("Bobby A Jones"));
    }

    @Test
    public void highlightEqualsIgnoreCaseFunction(){
        assertEquals("apples",new Highlighter("bananas").highlight("apples"));
        assertEquals("apples and grapefruit",new Highlighter("bananas").highlight("apples and grapefruit"));
    }

    @Test
    public void badStringMatcher(){
        assertTrue("abcd\\".contains("\\")) ;
        assertTrue("ab(cd)".contains("(")) ;
        assertTrue("ab(cd)".contains(")")) ;
        assertEquals("abcd asdf","ab(cd) asdf\\".replaceAll(Highlighter.illegalCharacters,""));
        assertEquals("\\(\\)\\\\","()\\".replaceAll(Highlighter.illegalCharacters,"\\\\$0"));
        assertEquals("Tg\\(flk\\)","Tg(flk)".replaceAll(Highlighter.illegalCharacters,"\\\\$0"));
        assertTrue(Pattern.compile("Tg\\(flk\\)").matcher("Tg(flk)").find());
        assertTrue(Pattern.compile("Tg\\(flk\\)").matcher("Tg(flk) insertion").find());
        assertTrue(Pattern.compile("Tgflk").matcher("Tgflk insertion").find());
    }

    @Test
    public void cleanBadStrings(){
        assertTrue(new Highlighter("Tg(flk)").contains("Tg(flk) insertion"));
        assertEquals("<strong>Tg(flk)</strong> insertion",new Highlighter("Tg(flk)").highlight("Tg(flk) insertion"));
        assertEquals("<strong>spinal</strong>chord\\",new Highlighter("spinal").highlight("spinalchord\\"));
        assertEquals("<strong>spinal</strong> <strong>cord</strong>\\",new Highlighter("spinal cord").highlight("spinal cord\\"));
        assertEquals("<strong>Tg(</strong>",new Highlighter("Tg(").highlight("Tg("));
    }
}
