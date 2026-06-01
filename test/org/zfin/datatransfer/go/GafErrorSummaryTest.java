package org.zfin.datatransfer.go;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the GAF error-message categorisation. categorize() is the single
 * source of truth used both by the error-summary text artifact and by the
 * report builder to match individual errors to their category bucket; drift
 * here would silently empty the "Matching errors" table.
 */
public class GafErrorSummaryTest {

    @Test
    public void ieaInferenceCategoryExtractsTypeFromBracket() {
        assertEquals("IEA inference field validation (UniRule)",
            GafErrorSummary.categorize("Annotations with IEA evidence must have inferred-from [UniRule]:"));
    }

    @Test
    public void obsoleteGoTermCategory() {
        assertEquals("Obsolete GO term referenced",
            GafErrorSummary.categorize("Go term in column 5 is obsolete:"));
    }

    @Test
    public void geneNotFoundCategory() {
        assertEquals("Gene not found for ID",
            GafErrorSummary.categorize("Unable to find genes associated with ID[A0A068FR00]:"));
    }

    @Test
    public void duplicateAnnotationCategory() {
        assertEquals("Duplicate annotation entry",
            GafErrorSummary.categorize("A duplicate entry is being added: ..."));
    }

    @Test
    public void doNotAnnotateSubsetCategory() {
        assertEquals("Term in \"Do Not Annotate\" subset",
            GafErrorSummary.categorize("Can not use term in a \"Do Not Annotate\" subset: ..."));
    }

    @Test
    public void proteinBindingCategory() {
        assertEquals("Protein binding (GO:0005515) requires IPI evidence",
            GafErrorSummary.categorize("Protein binding (GO:0005515) requires IPI evidence"));
    }

    @Test
    public void publicationNotFoundCategory() {
        assertEquals("Publication not found for PMID",
            GafErrorSummary.categorize("No pub found for pmid: 12345"));
    }

    @Test
    public void gorefCategoryNormalizesSpecificId() {
        assertEquals("Goref ID is not known or loaded",
            GafErrorSummary.categorize("Goref ID is not known or loaded[GO_REF:0000115]:"));
        assertEquals("Goref ID is not known or loaded",
            GafErrorSummary.categorize("Goref ID is not known or loaded[GO_REF:0000999]:"));
    }

    @Test
    public void roTermCategoryNormalizesSpecificTerm() {
        assertEquals("RO term does not exist",
            GafErrorSummary.categorize("RO term acts_upstream_of_or_within,_negative_effect does not exist"));
        assertEquals("RO term does not exist",
            GafErrorSummary.categorize("RO term something_else does not exist"));
    }

    @Test
    public void annotationInsertionFailedCategory() {
        assertEquals("Annotation insertion failed",
            GafErrorSummary.categorize("MarkerGoTermEvidence{zdbID='X', marker='y'}"));
    }

    @Test
    public void defaultBranchStripsTrailingColon() {
        assertEquals("Some other error",
            GafErrorSummary.categorize("Some other error:"));
    }

    @Test
    public void defaultBranchFallsThroughUnchanged() {
        assertEquals("Mystery error",
            GafErrorSummary.categorize("Mystery error"));
    }
}
