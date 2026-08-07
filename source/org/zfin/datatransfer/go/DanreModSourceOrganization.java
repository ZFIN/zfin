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
 *       ZFIN-created rows ("skip own annotations") and deferred them to the Noctua load.</li>
 *   <li>everything else → {@link OrganizationEnum#GOA} — the legacy GOA load owned all
 *       non-ZFIN sources (UniProt, InterPro, GO_Central phylo, RHEA, IntAct, …).</li>
 * </ul>
 *
 * <p><b>PROVISIONAL, pending ZFIN-10025 open decisions</b> (see workbench plan §7):</p>
 * <ul>
 *   <li>{@code GO_Central} (phylo IBA, GO_REF:0000033) currently falls through to GOA;
 *       it could instead get a dedicated org to preserve FP-Inference identity in reports.</li>
 *   <li>{@code GOC} (GO_REF:0000108) is net-new content the legacy GOA load rejected; it
 *       currently falls through to GOA pending the adopt/reject decision.</li>
 * </ul>
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

    /** Owner for any source not explicitly mapped above (the legacy GOA bucket). */
    public static final OrganizationEnum DEFAULT = OrganizationEnum.GOA;

    /** Resolve a GPAD {@code assigned_by} value to the owning organization. */
    public static OrganizationEnum resolve(String assignedBy) {
        if (assignedBy == null) {
            return DEFAULT;
        }
        return BY_ASSIGNED_BY.getOrDefault(assignedBy.trim(), DEFAULT);
    }

    /**
     * Every organization a row in this file can be assigned to. Used to drive the per-source
     * removal loop: removal is computed and applied once per org so each only prunes its own
     * outdated annotations.
     */
    public static Set<OrganizationEnum> allTargetOrganizations() {
        Set<OrganizationEnum> orgs = new HashSet<>(BY_ASSIGNED_BY.values());
        orgs.add(DEFAULT);
        return orgs;
    }
}
