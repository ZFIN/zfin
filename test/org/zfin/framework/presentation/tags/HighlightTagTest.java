package org.zfin.framework.presentation.tags;

import org.junit.Test;

import javax.servlet.jsp.tagext.TagSupport;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HighlightTagTest extends TagSupport {

    @Test
    public void testSimpleHighlight() {
        HighlightTag tag = new HighlightTag();
        tag.setHighlightEntity("Der Wald ist frei von Reif- und Eisgehaenge");
        tag.setHighlightString("ei");
        assertEquals("Der Wald ist fr<b>ei</b> von Reif- und Eisgehaenge", tag.getHighlightedString());
    }
}
