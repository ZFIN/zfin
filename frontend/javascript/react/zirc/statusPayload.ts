import type { FieldStatus } from './components/StatusBadge';

/** Entity id → field name → status, as ZircDashboardController serializes it. */
export type PerEntityStatus = Record<string, Record<string, FieldStatus>>;

/**
 * Server-computed status for a whole line submission, from
 * {@code GET /action/zirc/line-submission/{zdbID}/status}.
 *
 * <p>Lives here rather than in LineSubmissionDetail because the mutation edit
 * page needs the same payload: its assay / gene / lesion / phenotype list
 * renderers read the per-entity maps out of the JsonForms config, and before
 * ZFIN-10407 nothing on that page supplied them, so every badge there
 * rendered blank.
 */
export type StatusPayload = {
    fieldStatus: Record<string, FieldStatus>;
    sectionStatus: Record<string, FieldStatus>;
    // Top-level submission field name → its containing section label. Used
    // by ChangeHistoryPanel to label/scope audit entries.
    fieldSectionMap: Record<string, string>;
    mutationFieldStatus: PerEntityStatus;
    mutationSectionStatus: PerEntityStatus;
    mutationOverallStatus: Record<string, FieldStatus>;
    geneFieldStatus: PerEntityStatus;
    geneSectionStatus: PerEntityStatus;
    lesionFieldStatus: PerEntityStatus;
    lesionSectionStatus: PerEntityStatus;
    assayFieldStatus: PerEntityStatus;
    assaySectionStatus: PerEntityStatus;
    phenotypeFieldStatus: PerEntityStatus;
    phenotypeSectionStatus: PerEntityStatus;
};

/**
 * Fetch the status payload for a submission.
 *
 * The endpoint sits under /action/zirc, not the /action/api/zirc client base,
 * so it is fetched directly rather than through api.get().
 */
export function fetchStatusPayload(submissionId: string): Promise<StatusPayload> {
    return fetch(`/action/zirc/line-submission/${encodeURIComponent(submissionId)}/status`, {
        headers: { Accept: 'application/json' },
    }).then((r) => (r.ok ? r.json() : Promise.reject(new Error(`HTTP ${r.status}`))));
}

/**
 * The per-entity slices the list renderers look for in the JsonForms config
 * (AssaysListRenderer reads assayFieldStatus, LesionsListRenderer reads
 * lesionFieldStatus, and so on). Returns empty maps when the payload has not
 * arrived yet, which is what those renderers already fall back to.
 */
export function perEntityStatusConfig(payload: StatusPayload | null) {
    return {
        mutationFieldStatus:   payload?.mutationFieldStatus   ?? {},
        mutationSectionStatus: payload?.mutationSectionStatus ?? {},
        mutationOverallStatus: payload?.mutationOverallStatus ?? {},
        geneFieldStatus:       payload?.geneFieldStatus       ?? {},
        geneSectionStatus:     payload?.geneSectionStatus     ?? {},
        lesionFieldStatus:     payload?.lesionFieldStatus     ?? {},
        lesionSectionStatus:   payload?.lesionSectionStatus   ?? {},
        assayFieldStatus:      payload?.assayFieldStatus      ?? {},
        assaySectionStatus:    payload?.assaySectionStatus    ?? {},
        phenotypeFieldStatus:  payload?.phenotypeFieldStatus  ?? {},
        phenotypeSectionStatus: payload?.phenotypeSectionStatus ?? {},
    };
}
