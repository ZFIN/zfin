package org.zfin.zirc.service;

import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.zirc.entity.LineSubmission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Pure unit tests for {@link LineSubmissionService}'s value coercion + field
 * dispatch. The DB-touching saveField path is exercised by
 * {@code ZircDashboardControllerTest} (or future integration tests); here we
 * cover the bits that don't need Hibernate.
 */
public class LineSubmissionServiceTest {

    @Test
    public void parseTriBool_nullAndBlank() {
        assertNull(LineSubmissionService.parseTriBool(null));
        assertNull(LineSubmissionService.parseTriBool(""));
        assertNull(LineSubmissionService.parseTriBool("   "));
    }

    @Test
    public void parseTriBool_truthy() {
        assertEquals(Boolean.TRUE, LineSubmissionService.parseTriBool("true"));
        assertEquals(Boolean.TRUE, LineSubmissionService.parseTriBool("TRUE"));
        assertEquals(Boolean.TRUE, LineSubmissionService.parseTriBool("yes"));
        assertEquals(Boolean.TRUE, LineSubmissionService.parseTriBool("YES"));
    }

    @Test
    public void parseTriBool_falsy() {
        assertEquals(Boolean.FALSE, LineSubmissionService.parseTriBool("false"));
        assertEquals(Boolean.FALSE, LineSubmissionService.parseTriBool("FALSE"));
        assertEquals(Boolean.FALSE, LineSubmissionService.parseTriBool("no"));
        assertEquals(Boolean.FALSE, LineSubmissionService.parseTriBool("NO"));
    }

    @Test
    public void parseTriBool_garbageReturnsNull() {
        assertNull(LineSubmissionService.parseTriBool("maybe"));
        assertNull(LineSubmissionService.parseTriBool("1"));
    }

    @Test
    public void applyField_setsScalarFields() {
        LineSubmission s = new LineSubmission();
        LineSubmissionService.applyField(s, "name", "Test Line");
        assertEquals("Test Line", s.getName());

        LineSubmissionService.applyField(s, "abbreviation", "tl");
        assertEquals("tl", s.getAbbreviation());

        LineSubmissionService.applyField(s, "additionalInfo", "more notes");
        assertEquals("more notes", s.getAdditionalInfo());
    }

    @Test
    public void applyField_coercesBooleanFields() {
        LineSubmission s = new LineSubmission();
        LineSubmissionService.applyField(s, "featuresLinked", "true");
        assertEquals(Boolean.TRUE, s.getFeaturesLinked());

        LineSubmissionService.applyField(s, "backgroundChangeable", "no");
        assertEquals(Boolean.FALSE, s.getBackgroundChangeable());

        // Empty/garbage clears boolean fields back to null.
        LineSubmissionService.applyField(s, "featuresLinked", null);
        assertNull(s.getFeaturesLinked());
    }

    @Test
    public void applyField_unknownFieldThrows() {
        LineSubmission s = new LineSubmission();
        try {
            LineSubmissionService.applyField(s, "bogusField", "whatever");
            fail("Expected ResponseStatusException for unknown field");
        } catch (ResponseStatusException expected) {
            // pass
        }
    }

    /*
     * The replace-all save methods (saveGenes / saveLesions /
     * saveGenotypingAssays / savePhenotypes) used to track "rows to
     * keep" by entity id. A mid-loop Hibernate auto-flush (triggered
     * by resolveMarker's HQL query) would assign an IDENTITY id to a
     * just-created row; that new id wasn't in the incomingIds set, so
     * the trailing removeIf would delete the row. Net result: new
     * rows on the first save with a gene reference would silently
     * disappear.
     *
     * The fix switched to a Set<EntityType> keep keyed on entity
     * reference identity, immune to mid-loop id assignment. The
     * pure-helper structure here doesn't exercise the diff (needs
     * Hibernate), so this comment is the regression marker until a
     * proper integration test gets written.
     */
}
