package org.zfin.gwt.root.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
