package org.zfin.datatransfer.ncbi;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.zfin.datatransfer.ncbi.GenomeLocationDrift.Category;
import static org.zfin.datatransfer.ncbi.GenomeLocationDrift.categorize;

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
        assertEquals(Category.ORPHANED, categorize(null, false));
        assertEquals(Category.ORPHANED, categorize("", false));
        // string_agg over an empty set can also surface as whitespace depending on the driver
        assertEquals(Category.ORPHANED, categorize("   ", false));
    }

    @Test
    public void accessionMappingToOneOtherGeneIsRemapped() {
        assertEquals(Category.REMAPPED, categorize("ZDB-GENE-070820-22", false));
    }

    @Test
    public void accessionMappingToSeveralGenesIsAmbiguous() {
        // string_agg joins with a comma, so a comma is the marker for N:1
        assertEquals(Category.AMBIGUOUS,
                categorize("ZDB-GENE-070820-22,ZDB-GENE-141216-85", false));
        assertEquals(Category.AMBIGUOUS,
                categorize("ZDB-GENE-A,ZDB-GENE-B,ZDB-GENE-C", false));
    }

    @Test
    public void ambiguousIsNeverTreatedAsRemapped() {
        // Guards the ordering in the reconcile loop: an N:1 accession must not be silently
        // re-pointed at whichever gene happened to sort first.
        Category category = categorize("ZDB-GENE-A,ZDB-GENE-B", false);
        assertFalse(category == Category.REMAPPED);
    }

    // ---------------- the two branch points together ----------------

    @Test
    public void aRemapThatWouldCollideIsADuplicate() {
        // The drift query predicts the collision, so no write has to be attempted to find out.
        assertEquals(Category.REMAPPED_DUPLICATE, categorize("ZDB-GENE-A", true));
        assertEquals(Category.REMAPPED, categorize("ZDB-GENE-A", false));
    }

    @Test
    public void collisionPredictionOnlyMattersForARemap() {
        // Orphaned and ambiguous rows are decided from db_link alone; a stray true must not
        // divert them.
        assertEquals(Category.ORPHANED, categorize(null, true));
        assertEquals(Category.AMBIGUOUS, categorize("ZDB-GENE-A,ZDB-GENE-B", true));
    }
}
