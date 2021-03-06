package org.zfin.framework.presentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Convenience class that transforms text entry fields.
 */
public class UIFieldTransformerTest {


    @Test
    public void nullTextEntry(){
        String value = null;

        String transformedString = UIFieldTransformer.transformTextEntryFieldValue(value);
        assertTrue(transformedString == null);
    }

    @Test
    public void whiteSpaceAppendedTextEntry(){
        String value = "werner    ";

        String transformedString = UIFieldTransformer.transformTextEntryFieldValue(value);
        assertEquals("werner", transformedString);
    }

    @Test
    public void whiteSpacePrefixedTextEntry(){
        String value = "   werner";

        String transformedString = UIFieldTransformer.transformTextEntryFieldValue(value);
        assertEquals("werner", transformedString);
    }
}