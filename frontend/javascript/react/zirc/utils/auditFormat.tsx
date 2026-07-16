import * as React from 'react';
import type { AuditEntry } from '../components/FieldHistory';

/**
 * Shared formatting + classification helpers for ZIRC audit-trail rows.
 * Extracted out of ChangeHistoryPanel so future history surfaces (and the
 * legacy popover, if it ever comes back) can reuse the exact same display
 * conventions without copy-pasting.
 */

/**
 * The entity kinds that are *children* of a Mutation in the line-submission
 * graph. Mirrors the server's parsing in {@code ZircAuditQueryService} —
 * keep the two in sync (Java side hardcodes the same kinds in
 * {@code containerChildKindFor} and {@code childIdsForParent}). All
 * client-side bucketing that asks "is this entry under the Mutations
 * section?" should reference this set rather than re-listing kinds.
 */
export const CHILD_KINDS: ReadonlySet<string> =
    new Set(['mutation', 'gene', 'lesion', 'assay', 'phenotype']);

/**
 * Bucket an audit entry into one of the form's top-level sections.
 * Child-entity rows (mutation / gene / lesion / assay / phenotype) all
 * fall under "Mutations" because the form embeds them there; submission-
 * scope rows are looked up via the server-shipped fieldSectionMap (leaf
 * field name → section label). Returns null when no section matches.
 */
export function sectionForEntry(
    e: AuditEntry,
    fieldSectionMap: Record<string, string>,
): string | null {
    if (e.entityKind && CHILD_KINDS.has(e.entityKind)) {return 'Mutations';}
    if (!e.path) {return null;}
    const segments = e.path.split('/').filter((s) => s.length > 0);
    if (segments.length === 0) {return null;}
    const leaf = segments[segments.length - 1];
    return fieldSectionMap[leaf] ?? fieldSectionMap[segments[0]] ?? null;
}

/**
 * Server emits {@code whenUpdated} as ISO-8601 with the host's timezone
 * offset. Render relative to the viewer's local day so a curator sees
 * "Today, 06:12 AM" / "Yesterday, 4:15 PM" / "Apr 12, 2026, 9:30 AM".
 * Falls back to a placeholder when the timestamp is missing or
 * unparseable.
 */
export function formatWhen(iso: string | null | undefined): React.ReactNode {
    if (!iso) {return <em>unknown time</em>;}
    const when = new Date(iso);
    if (isNaN(when.getTime())) {return iso;}
    const now = new Date();
    const sameDay = (a: Date, b: Date) =>
        a.getFullYear() === b.getFullYear()
        && a.getMonth() === b.getMonth()
        && a.getDate() === b.getDate();
    const yesterday = new Date(now);
    yesterday.setDate(now.getDate() - 1);
    const time = when.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
    if (sameDay(when, now))       {return `Today, ${time}`;}
    if (sameDay(when, yesterday)) {return `Yesterday, ${time}`;}
    return when.toLocaleString([], {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: 'numeric', minute: '2-digit',
    });
}

/**
 * Convert a JSON Pointer ("/maternalBackground", "/acceptance/reasons")
 * into a human label by humanizing every non-empty segment and joining
 * them with " · ". Empty/null paths return null so the caller can fall
 * back to the action verb. camelCase splits on internal capitals; the
 * result is then title-cased so it reads naturally in the right-hand
 * history panel.
 */
export function humanizeFieldPath(path: string | null | undefined): string | null {
    if (!path) {return null;}
    const segments = path.split('/').filter((s) => s.length > 0);
    if (segments.length === 0) {return null;}
    return segments.map(humanizeSegment).join(' · ');
}

function humanizeSegment(s: string): string {
    return s
        .replace(/([a-z])([A-Z])/g, '$1 $2')
        .replace(/^./, (c) => c.toUpperCase());
}

/**
 * Audit values are stored as JSONB-stringified scalars/objects. Unwrap a
 * JSON-encoded string to its plain form for display; anything more
 * complex (objects, arrays) falls back to the raw text so curators still
 * see something rather than a parse error.
 */
export function formatValue(v: string | null): React.ReactNode {
    if (v === null || v === undefined) {return null;}
    const trimmed = v.trim();
    if (trimmed === '' || trimmed === 'null') {return null;}
    try {
        const parsed = JSON.parse(trimmed);
        if (parsed === null) {return null;}
        if (typeof parsed === 'string') {return parsed.length === 0 ? null : parsed;}
        if (typeof parsed === 'number' || typeof parsed === 'boolean') {return String(parsed);}
        return <code>{trimmed}</code>;
    } catch {
        return trimmed;
    }
}
