package org.zfin.datatransfer.ncbi;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.DriftCategory;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.categorizeDrift;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.describeConflict;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.isDuplicateOfTargetRow;

/**
 * Decision logic behind {@code reconcileNcbiGenomeLocations()} (ZFIN-10461).
 *
 * <p>The reconciliation reads rows of sequence_feature_chromosome_location_generated whose
 * (gene, NCBI Gene ID) pairing no longer agrees with db_link, and has to choose between
 * deleting, re-pointing, and leaving alone for a curator. Getting that choice wrong either
 * strands wrong coordinates on a gene page or destroys the only coordinates a gene has, so
 * the two branch points are pulled out as pure functions and pinned here.
 */
public class NCBIGenomeLocationReconcileTest {

    // ---------------- categorizeDrift ----------------

    @Test
    public void accessionWithNoRemainingGeneLinkIsOrphaned() {
        // The subquery returns null when no db_link row survives for the accession.
        assertEquals(DriftCategory.ORPHANED, categorizeDrift(null));
        assertEquals(DriftCategory.ORPHANED, categorizeDrift(""));
        // string_agg over an empty set can also surface as whitespace depending on the driver
        assertEquals(DriftCategory.ORPHANED, categorizeDrift("   "));
    }

    @Test
    public void accessionMappingToOneOtherGeneIsRemapped() {
        assertEquals(DriftCategory.REMAPPED, categorizeDrift("ZDB-GENE-070820-22"));
    }

    @Test
    public void accessionMappingToSeveralGenesIsAmbiguous() {
        // string_agg joins with a comma, so a comma is the marker for N:1
        assertEquals(DriftCategory.AMBIGUOUS,
                categorizeDrift("ZDB-GENE-070820-22,ZDB-GENE-141216-85"));
        assertEquals(DriftCategory.AMBIGUOUS,
                categorizeDrift("ZDB-GENE-A,ZDB-GENE-B,ZDB-GENE-C"));
    }

    @Test
    public void ambiguousIsNeverTreatedAsRemapped() {
        // Guards the ordering in the reconcile loop: an N:1 accession must not be silently
        // re-pointed at whichever gene happened to sort first.
        DriftCategory category = categorizeDrift("ZDB-GENE-A,ZDB-GENE-B");
        assertFalse(category == DriftCategory.REMAPPED);
    }

    // ---------------- isDuplicateOfTargetRow ----------------

    @Test
    public void targetHoldingTheSameLocationIsADuplicate() {
        // Every collision observed in real data was this case: the GFF3 load had already
        // created the correct row on the new gene and left this one behind on the old one.
        assertTrue(isDuplicateOfTargetRow("3:31406831-31442430",
                Collections.singletonList("3:31406831-31442430")));
    }

    @Test
    public void targetHoldingDifferentCoordinatesIsNotADuplicate() {
        assertFalse(isDuplicateOfTargetRow("3:31406831-31442430",
                Collections.singletonList("3:31406831-31442999")));
        // same span, different chromosome
        assertFalse(isDuplicateOfTargetRow("3:31406831-31442430",
                Collections.singletonList("7:31406831-31442430")));
    }

    @Test
    public void duplicateIsFoundAmongSeveralTargetLocations() {
        assertTrue(isDuplicateOfTargetRow("11:12280170-12288888",
                Arrays.asList("4:100-200", "11:12280170-12288888", "9:5-6")));
    }

    @Test
    public void targetWithNoLocationsIsNotADuplicate() {
        // A collision with nothing to collide against means something other than a
        // duplicate went wrong; it must stay a reported conflict, not a deletion.
        assertFalse(isDuplicateOfTargetRow("3:1-2", Collections.emptyList()));
        assertFalse(isDuplicateOfTargetRow("3:1-2", null));
    }

    // ---------------- describeConflict ----------------

    @Test
    public void conflictNamesTheTargetsDifferingLocations() {
        String message = describeConflict(
                Arrays.asList("7:100-200", "7:300-400"), "ZDB-GENE-070820-22");
        assertTrue(message.contains("ZDB-GENE-070820-22"));
        assertTrue(message.contains("7:100-200"));
        assertTrue(message.contains("7:300-400"));
        assertTrue(message.contains("different location"));
    }

    @Test
    public void conflictWithNoTargetRowSaysSo() {
        String message = describeConflict(Collections.emptyList(), "ZDB-GENE-070820-22");
        assertTrue(message.contains("ZDB-GENE-070820-22"));
        assertTrue(message.contains("no location for this accession"));
    }

    // ---------------- the two branch points together ----------------

    /**
     * Walks the same order of decisions the reconcile loop makes, so a future reordering
     * that changed an outcome would fail here rather than in production.
     */
    private static String outcomeOf(String currentGenes, String staleLocation,
                                    List<String> targetLocations, boolean repointSucceeds) {
        switch (categorizeDrift(currentGenes)) {
            case ORPHANED:
                return "delete-orphan";
            case AMBIGUOUS:
                return "report-ambiguous";
            default:
                if (repointSucceeds) {
                    return "repoint";
                }
                return isDuplicateOfTargetRow(staleLocation, targetLocations)
                        ? "delete-duplicate" : "report-conflict";
        }
    }

    @Test
    public void allFiveOutcomesAreReachable() {
        assertEquals("delete-orphan",
                outcomeOf(null, "3:1-2", Collections.emptyList(), false));
        assertEquals("report-ambiguous",
                outcomeOf("ZDB-GENE-A,ZDB-GENE-B", "3:1-2", Collections.emptyList(), false));
        assertEquals("repoint",
                outcomeOf("ZDB-GENE-A", "3:1-2", Collections.emptyList(), true));
        assertEquals("delete-duplicate",
                outcomeOf("ZDB-GENE-A", "3:1-2", Collections.singletonList("3:1-2"), false));
        assertEquals("report-conflict",
                outcomeOf("ZDB-GENE-A", "3:1-2", Collections.singletonList("3:9-9"), false));
    }

    @Test
    public void orphanIsDeletedEvenWhenAConflictingRowWouldExist() {
        // ORPHANED is decided before any write is attempted, so a coincidentally matching
        // location elsewhere must not divert it into the duplicate branch.
        assertEquals("delete-orphan",
                outcomeOf(null, "3:1-2", Collections.singletonList("3:1-2"), false));
    }

    @Test
    public void successfulRepointNeverConsultsTheTargetLocations() {
        // The reconcile loop attempts the update first and only classifies on failure. If
        // that order inverted, a gene whose target already held the same coordinates would
        // be deleted instead of re-pointed.
        assertEquals("repoint",
                outcomeOf("ZDB-GENE-A", "3:1-2", Collections.singletonList("3:1-2"), true));
    }
}
