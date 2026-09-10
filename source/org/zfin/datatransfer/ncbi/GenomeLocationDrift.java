package org.zfin.datatransfer.ncbi;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The decision model for a genome location whose gene attribution has gone stale.
 *
 * <p>A row in {@code sequence_feature_chromosome_location_generated} sourced from
 * {@code NCBILoader} pairs an NCBI Gene ID with a ZFIN gene. When {@code db_link} no longer
 * carries that pairing the row has drifted, and what should happen to it depends only on
 * where the accession points now. That decision is pure - it needs no database - so it lives
 * here, away from the load that applies it, and is unit tested directly.
 *
 * @see NCBIDirectPort#reconcileNcbiGenomeLocations()
 */
public final class GenomeLocationDrift {

    private GenomeLocationDrift() {
    }

    /**
     * What the accession's current gene mapping says should happen to a drifted row.
     */
    public enum Category {
        /** The accession has no NCBI Gene db_link at all; the location belongs to nothing. */
        ORPHANED,
        /** The accession maps to exactly one gene, and it is not the one on the row. */
        REMAPPED,
        /**
         * As REMAPPED, but the gene it now belongs to already holds this very location, so
         * moving the row there would collide. The row duplicates one the GFF3 load already
         * created and can be dropped without losing coordinates.
         */
        REMAPPED_DUPLICATE,
        /** The accession maps to several genes; there is no single correct answer. */
        AMBIGUOUS
    }

    /**
     * One decision, as the report renders it.
     *
     * <p>Also used for links dropped elsewhere in the load - the shape is the same, and those
     * have no coordinates to name, hence the four-argument form.
     */
    public record ReportRow(String geneZdbId, String accession, String outcome,
                            String nowMapsTo, String location) {

        /** For a decision that is not about a genome location, so has no coordinates. */
        public ReportRow(String geneZdbId, String accession, String outcome, String nowMapsTo) {
            this(geneZdbId, accession, outcome, nowMapsTo, null);
        }

        public Map<String, Object> toMap() {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Gene ZDB ID", geneZdbId);
            row.put("NCBI Gene ID", accession);
            row.put("Outcome", outcome);
            row.put("Accession now maps to", nowMapsTo);
            row.put("Location", location == null ? "" : location);
            return row;
        }
    }

    /**
     * Decide from the accession's current mapping alone, before touching the database.
     *
     * @param currentGenes comma-separated ZFIN gene IDs the accession now maps to,
     *                     blank or null when it maps to none
     * @param wouldCollide whether the gene it now maps to already holds this exact location,
     *                     which the drift query works out up front so no write has to be
     *                     attempted to find out
     */
    public static Category categorize(String currentGenes, boolean wouldCollide) {
        if (StringUtils.isBlank(currentGenes)) {
            return Category.ORPHANED;
        }
        if (currentGenes.contains(",")) {
            return Category.AMBIGUOUS;
        }
        return wouldCollide ? Category.REMAPPED_DUPLICATE : Category.REMAPPED;
    }

}
