import * as React from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { AuditEntry } from './FieldHistory';
import { HistoryFocusContext } from '../historyFocusContext';
import {
    formatValue,
    formatWhen,
    humanizeFieldPath,
    sectionForEntry,
} from '../utils/auditFormat';

type Props = {
    submissionId: string | null | undefined;
    // Top-level submission field name → section label, derived server-side
    // from the form's uiSchema Group structure. Used to bucket submission-
    // scope audit entries when the user clicks a top-level section's
    // history icon.
    fieldSectionMap?: Record<string, string>;
};

/**
 * Right-hand "Change History" panel on the line-submission detail page.
 * Fetches every audit row in the submission tree (the submission itself
 * plus every mutation / gene / lesion / assay / phenotype underneath it)
 * via {@code GET /api/zirc/audit?recId=<sub>&scope=submission} and renders
 * a newest-first stream so curators can see at a glance what changed
 * across the whole submission without opening every field's popover.
 *
 * Rendering is intentionally compact — one entry per audit row, with the
 * field path, actor, timestamp, and an "old → new" value diff. The legacy
 * {@code FieldHistory} popover stays in place for per-field drill-in;
 * this panel is the cross-cutting summary view that complements it.
 */
export function ChangeHistoryPanel({ submissionId, fieldSectionMap }: Props) {
    const enabled = !!submissionId;
    const { focus, setFocus } = React.useContext(HistoryFocusContext);

    // Reset the panel's internal scroll position whenever the focused
    // scope changes — otherwise a user who scrolled deep into one field's
    // history sees the next click's entries from that same scroll offset,
    // which looks like the new list was appended below the old one.
    const bodyRef = React.useRef<HTMLDivElement | null>(null);
    React.useEffect(() => {
        if (bodyRef.current) {bodyRef.current.scrollTop = 0;}
    }, [focus?.recId, focus?.scope, focus?.fieldName, focus?.sectionName]);

    // Submission-wide query: only loaded when a top-level section is the
    // explicit focus (we filter its result client-side). Not loaded
    // otherwise — the panel is empty by default and only shows entries
    // after the user clicks a history icon.
    const wantsSubmissionWide = !!focus
        && focus.scope === 'section'
        && focus.recId === submissionId;
    const submissionQuery = useQuery<AuditEntry[]>({
        queryKey: ['zirc', 'audit', 'submission', submissionId ?? ''],
        enabled: enabled && wantsSubmissionWide,
        queryFn: () => api.get(`/audit?recId=${encodeURIComponent(submissionId!)}&scope=submission`),
    });
    // Focused query: powers the per-field / per-section view when the user
    // clicks a history icon on a field. Bypasses the submission-wide cache
    // so a deep aggregate's history isn't tied to the submission's
    // staleness window.
    const focusQuery = useQuery<AuditEntry[]>({
        queryKey: ['zirc', 'audit', focus?.recId ?? '', focus?.scope ?? '', focus?.fieldName ?? '', focus?.sectionName ?? ''],
        enabled: !!focus,
        queryFn: () => {
            const p = new URLSearchParams({ recId: focus!.recId, scope: focus!.scope });
            if (focus!.scope === 'field')   {p.set('fieldName',   focus!.fieldName   ?? '');}
            if (focus!.scope === 'section') {p.set('sectionName', focus!.sectionName ?? '');}
            return api.get<AuditEntry[]>(`/audit?${p.toString()}`);
        },
    });

    if (!enabled) {return null;}

    // Three focus modes; scroll-spy is intentionally not used to drive
    // either the title or the entry list — the panel stays empty until
    // the user explicitly clicks a history icon to set focus.
    //   • Field focus → fetch that field's rows directly.
    //   • Top-level section focus (icon click on Overview / Mutations /
    //     Linked Features / Background / Additional Info — recId === the
    //     submission's ZDB-ID) → reuse the submission-wide cache and
    //     filter by section name so the panel content actually changes
    //     between sections (the /audit?scope=section endpoint ignores
    //     sectionName and returns every submission-scope row, which is
    //     why clicking different section icons used to look identical).
    //   • Section focus on a nested aggregate (mutation/gene/lesion/…) →
    //     fall through to the focused fetch; that entity's rows are
    //     scoped narrowly enough on the server.
    const fsMap = fieldSectionMap ?? {};
    const focused = !!focus;
    const isTopLevelSectionFocus =
        focused && focus!.scope === 'section' && focus!.recId === submissionId;

    const query = isTopLevelSectionFocus ? submissionQuery : focusQuery;
    const allEntries = focused ? (query.data ?? []) : [];
    const visibleEntries = isTopLevelSectionFocus
        ? allEntries.filter((e) => sectionForEntry(e, fsMap) === focus!.label)
        : allEntries;
    // Prefix the focused label so curators see at a glance whether the
    // history is for a section, a single field, or a whole entity card.
    // The name itself is rendered bold; the "Section:" / "Field:" prefix
    // stays in the muted weight to keep the visual hierarchy.
    const focusPrefix = focused
        ? (focus!.scope === 'section' ? 'Section: '
            : focus!.scope === 'field' ? 'Field: '
                : '')
        : '';
    const focusLabel = focused ? (
        <>{focusPrefix}<strong className='text-primary'>{focus!.label}</strong></>
    ) : null;

    // Pull the card down to the clicked icon's vertical level. `position:
    // sticky; top: 0.5rem` alone doesn't move the panel — sticky only
    // *constrains* the element on scroll; the natural starting position
    // is still the top of the right column. We push the panel down by
    // adjusting its margin-top so its natural top aligns with the trigger
    // at click time, then sticky keeps it visible as the user scrolls.
    const cardRef = React.useRef<HTMLDivElement | null>(null);
    const [marginTopPx, setMarginTopPx] = React.useState(0);
    React.useEffect(() => {
        if (focus?.anchorY == null || !cardRef.current) {
            setMarginTopPx(0);
            return;
        }
        // Measure the panel's current viewport-top with whatever margin
        // is already applied, then shift by the delta needed to reach
        // the clicked icon's viewport-y. This way the math stays correct
        // when the user clicks a second icon after a previous focus.
        const currentTop = cardRef.current.getBoundingClientRect().top;
        const delta = focus.anchorY - currentTop;
        setMarginTopPx((prev) => Math.max(0, prev + delta));
    }, [focus?.anchorY]);

    return (
        <div
            ref={cardRef}
            className='card'
            style={{ position: 'sticky', top: '0.5rem', marginTop: `${marginTopPx}px` }}
        >
            <div className='card-header py-2'>
                <div className='d-flex justify-content-between align-items-center'>
                    <span>
                        <i className='fas fa-history mr-2 text-primary' aria-hidden='true'/>
                        <strong>Change History</strong>
                        {focused && query.data ? (
                            <span className='text-muted small ml-2'>({visibleEntries.length})</span>
                        ) : null}
                    </span>
                    {focused && (
                        <button
                            type='button'
                            className='btn btn-sm btn-link p-0 text-muted'
                            aria-label='Clear history focus'
                            title='Clear history focus'
                            onClick={() => setFocus(null)}
                        >
                            <span aria-hidden='true'>&times;</span>
                        </button>
                    )}
                </div>
                {focusLabel && (
                    <div className='small text-muted ml-4'>{focusLabel}</div>
                )}
            </div>
            <div ref={bodyRef} className='card-body p-2' style={{ maxHeight: '40rem', overflowY: 'auto' }}>
                {!focused && (
                    <div className='text-muted small'>
                        Click a history icon next to any field or section to view its change history here.
                    </div>
                )}
                {focused && query.isLoading && <div className='text-muted small'>Loading…</div>}
                {focused && query.isError && (
                    <div className='text-danger small'>
                        Failed to load history: {(query.error as Error)?.message ?? 'unknown error'}
                    </div>
                )}
                {focused && query.data && visibleEntries.length === 0 && (
                    <div className='text-muted small'>
                        No changes recorded in {focus!.label} yet.
                    </div>
                )}
                {focused && query.data && visibleEntries.length > 0 && (
                    <ul
                        className='mb-0'
                        style={{
                            listStyle: 'none',
                            borderLeft: '2px solid #dee2e6',
                            margin: '0 0 0 0.5rem',
                            padding: '0.25rem 0 0 1.25rem',
                            position: 'relative',
                        }}
                    >
                        {/* Time-direction arrow at the top of the rail; newest
                            entries are listed first, so the caret points up. */}
                        <i
                            className='fas fa-caret-up'
                            aria-hidden='true'
                            title='Newest first'
                            style={{
                                position: 'absolute',
                                left: '-0.5rem',
                                top: '-0.7rem',
                                color: '#6c757d',
                                fontSize: '1rem',
                                lineHeight: 1,
                            }}
                        />
                        {visibleEntries.map((e) => (
                            <li
                                key={e.id}
                                style={{
                                    position: 'relative',
                                    paddingBottom: '0.85rem',
                                }}
                            >
                                {/* Bullet straddling the parent's left border. */}
                                <span
                                    aria-hidden='true'
                                    style={{
                                        position: 'absolute',
                                        left: '-1.625rem',
                                        top: '0.35rem',
                                        width: '0.625rem',
                                        height: '0.625rem',
                                        borderRadius: '50%',
                                        background: '#6c757d',
                                        border: '2px solid #fff',
                                        boxShadow: '0 0 0 1px #dee2e6',
                                    }}
                                />
                                <div className='small text-muted'>
                                    <strong>{formatWhen(e.whenUpdated)}</strong>
                                    <span className='mx-1'>·</span>
                                    {e.actorName}
                                </div>
                                {/* When the panel is scoped to a single field, the
                                    field name already appears in the header. Skip
                                    the per-entry path line to avoid repetition. */}
                                {focus!.scope !== 'field' && (
                                    <div className='small'>
                                        {humanizeFieldPath(e.path) ?? e.action}
                                    </div>
                                )}
                                {(e.oldValue !== null || e.newValue !== null) && (
                                    <div className='small'>
                                        <span className='text-muted'>{formatValue(e.oldValue) ?? <em>∅</em>}</span>
                                        <span className='mx-1'>→</span>
                                        <span>{formatValue(e.newValue) ?? <em>∅</em>}</span>
                                    </div>
                                )}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
}
