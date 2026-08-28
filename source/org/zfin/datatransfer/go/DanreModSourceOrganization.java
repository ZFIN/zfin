package org.zfin.datatransfer.go;

import org.zfin.datatransfer.go.GafOrganization.OrganizationEnum;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ZFIN-10025 Phase 1 — maps a row in the unified DANRE-mod GPAD file to the ZFIN
 * {@link GafOrganization} that should OWN the resulting annotation.
 *
 * <p>The file is a merged superset of many sources, identified per row by the GPAD
 * {@code assigned_by} column (carried on {@link GafEntry#getCreatedBy()}). Ownership scopes
 * add / update / <b>remove</b> per source, so the unified load can never mass-delete another
 * source's annotations — each resolved org only removes the DB rows attributed to it
 * ({@code getEvidencesForGafOrganization}).</p>
 *
 * <p>The mapping deliberately <b>reproduces legacy ownership</b> so the first real
 * (non-report-only) load is a near no-op diff rather than a churn of mass add+remove:</p>
 * <ul>
 *   <li>{@code ZFIN} → {@link OrganizationEnum#NOCTUA} — the legacy GAF/GOA path rejected
 *       ZFIN-created rows ("skip own annotations") and deferred them to the Noctua load.
 *       Curation ownership wins over everything below.</li>
 *   <li>{@code GO_REF:0000033} → {@link OrganizationEnum#PAINT} — phylogenetically inferred
 *       annotations (IBA), whoever asserted them. See below.</li>
 *   <li>everything else → {@link OrganizationEnum#GOA} — the legacy GOA load owned all
 *       non-ZFIN sources (UniProt, InterPro, RHEA, IntAct, …).</li>
 * </ul>
 *
 * <p><b>Why phylo gets its own org (ZFIN-10025 open decision 9, settled).</b> The legacy
 * FP-Inference load owned {@code zfin-prediction.gaf} under the {@code FP Inferences} org, and
 * those rows sit on {@code ZDB-PUB-110330-1} — the <i>same</i> publication as the unified file's
 * {@code GO_REF:0000033} rows. They are the same kind of annotation, differing only in
 * {@code assigned_by} ({@code GOC} vs {@code GO_Central}) because they arrived in different
 * files. Left in GOA, phylo would be indistinguishable from UniProt/InterPro IEA in every
 * per-org report, and retiring the FP-Inference job would strand its rows in an org no load
 * owns — never refreshed, never pruned. {@code PAINT} already existed in the schema (pk 4) and
 * was empty; it is GO's Phylogenetic Annotation and INference Tool, i.e. exactly the producer of
 * {@code GO_REF:0000033}.</p>
 *
 * <p>Keyed on the <b>reference</b>, not {@code assigned_by}, because the reference is what makes
 * an annotation phylogenetic. {@code assigned_by=GO_Central} is <i>nearly</i> a proxy — 62,197 of
 * 62,221 such rows are on the PAINT reference — but the 24 that are not would be mis-homed into
 * PAINT's removal scope, and any phylo row asserted by someone other than GO_Central would be
 * missed.</p>
 *
 * <p><b>PROVISIONAL, pending ZFIN-10025 open decisions:</b> {@code GOC}
 * ({@code GO_REF:0000108}) is net-new content the legacy GOA load rejected; it currently falls
 * through to GOA pending the adopt/reject decision.</p>
 *
 * Kept data-driven (a map, not hard-coded {@code if}s) so a source can be re-homed without
 * touching control flow. Any source not explicitly listed resolves to {@link #DEFAULT}.
 */
public final class DanreModSourceOrganization {

    private DanreModSourceOrganization() {
    }

    /** Sources whose ownership differs from the default GOA bucket. */
    private static final Map<String, OrganizationEnum> BY_ASSIGNED_BY = Map.of(
        "ZFIN", OrganizationEnum.NOCTUA
    );

    /** References whose ownership differs from the default, regardless of who asserted them. */
    private static final Map<String, OrganizationEnum> BY_REFERENCE = Map.of(
        "GO_REF:0000033", OrganizationEnum.PAINT
    );

    /** Owner for any source not explicitly mapped above (the legacy GOA bucket). */
    public static final OrganizationEnum DEFAULT = OrganizationEnum.GOA;

    /**
     * Resolve a row to its owning organization.
     *
     * @param assignedBy GPAD col 10, from {@link GafEntry#getCreatedBy()}
     * @param reference  GPAD col 6, from {@link GafEntry#getPubmedId()} — a {@code GO_REF:*} or
     *                   {@code PMID:*}. May be null.
     */
    public static OrganizationEnum resolve(String assignedBy, String reference) {
        // assigned_by is checked first: ZFIN curation stays Noctua-owned whatever it cites.
        if (assignedBy != null) {
            OrganizationEnum bySource = BY_ASSIGNED_BY.get(assignedBy.trim());
            if (bySource != null) {
                return bySource;
            }
        }
        if (reference != null) {
            OrganizationEnum byReference = BY_REFERENCE.get(reference.trim());
            if (byReference != null) {
                return byReference;
            }
        }
        return DEFAULT;
    }

    /**
     * Every organization a row in this file can be assigned to. Used to drive the per-source
     * removal loop: removal is computed and applied once per org so each only prunes its own
     * outdated annotations.
     */
    public static Set<OrganizationEnum> allTargetOrganizations() {
        Set<OrganizationEnum> orgs = new HashSet<>(BY_ASSIGNED_BY.values());
        orgs.addAll(BY_REFERENCE.values());
        orgs.add(DEFAULT);
        return orgs;
    }
}
