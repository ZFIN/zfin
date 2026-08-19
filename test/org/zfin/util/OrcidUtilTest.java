package org.zfin.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrcidUtilTest {

    @Test
    public void normalizesCanonicalForm() {
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("0000-0002-1825-0097"));
    }

    @Test
    public void normalizesFormsPeopleActuallySubmit() {
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("0000000218250097"));
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("https://orcid.org/0000-0002-1825-0097"));
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("http://www.orcid.org/0000-0002-1825-0097"));
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("orcid.org/0000-0002-1825-0097"));
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("  0000-0002-1825-0097 "));
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("0000 0002 1825 0097"));
    }

    @Test
    public void keepsTheCheckDigitAndUppercasesIt() {
        assertEquals("0000-0002-1694-233X", OrcidUtil.normalize("0000-0002-1694-233x"));
    }

    /**
     * Word processors and email clients substitute typographic dashes.
     */
    @Test
    public void toleratesNonAsciiDashes() {
        assertEquals("0000-0002-1825-0097", OrcidUtil.normalize("0000‑0002‑1825‑0097"));
    }

    @Test
    public void rejectsValuesThatAreNotOrcids() {
        assertEquals("", OrcidUtil.normalize("rPlgedlRSUqMgwEAx"));
        assertEquals("", OrcidUtil.normalize("0000-0002-1825"));
        assertEquals("", OrcidUtil.normalize("0000-0002-1825-00977"));
        assertEquals("", OrcidUtil.normalize("X000-0002-1825-0097"));
        assertEquals("", OrcidUtil.normalize(""));
        assertEquals("", OrcidUtil.normalize(null));
    }

    @Test
    public void validityFollowsNormalization() {
        assertTrue(OrcidUtil.isValid("0000000218250097"));
        assertFalse(OrcidUtil.isValid("not-an-orcid"));
        assertFalse(OrcidUtil.isValid(null));
    }

    @Test
    public void recognizesBlanksAndPlaceholders() {
        for (String value : new String[]{null, "", "   ", "N/A", "n/a", "none", "None", "-", "?", "unknown"}) {
            assertTrue("expected placeholder: " + value, OrcidUtil.isBlankOrPlaceholder(value));
        }
        assertFalse(OrcidUtil.isBlankOrPlaceholder("0000-0002-1825-0097"));
        assertFalse(OrcidUtil.isBlankOrPlaceholder("rPlgedlRSUqMgwEAx"));
    }

    @Test
    public void recognizesCanonicalForm() {
        assertTrue(OrcidUtil.isCanonical("0000-0002-1825-0097"));
        assertTrue(OrcidUtil.isCanonical("0000-0002-1694-233X"));
        assertFalse(OrcidUtil.isCanonical("0000000218250097"));
        assertFalse(OrcidUtil.isCanonical(null));
    }
}
