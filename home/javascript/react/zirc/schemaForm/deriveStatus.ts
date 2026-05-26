import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import type { FieldStatus } from '../components/StatusBadge';

/**
 * Hardcoded TS mirrors of the server's {@code FieldStatus} enum. Used for
 * client-side derivation in places where the bootstrap payload doesn't
 * carry a per-record status map (Gene table, Lesion / Assay / Phenotype
 * detail cards). Server-derived status takes precedence when present —
 * this is the fallback for "schema is single source of truth" cases
 * where the only signal is value-emptiness + schema.required.
 */
export const STATUS_MISSING: FieldStatus = {
    abbreviation: 'M', displayName: 'Missing', cssClass: 'badge-danger',
};
export const STATUS_COMPLETE: FieldStatus = {
    abbreviation: 'C', displayName: 'Complete', cssClass: 'badge-success',
};

function isEmptyValue(v: unknown): boolean {
    if (v == null) {return true;}
    if (typeof v === 'string') {return v.trim() === '';}
    if (Array.isArray(v)) {return v.length === 0;}
    return false;
}

/**
 * Build a per-leaf {@code fieldStatus} map from a flat (or single-level
 * nested) schema's {@code required} array + a record's data. Suitable
 * for the per-aggregate detail cards whose schemas are flat (no
 * sub-object properties); use {@link deriveStatusForSchema} if a
 * nested-object schema needs to walk the tree.
 */
export function deriveFieldStatus(
    schema: JsonSchema | undefined,
    data: unknown,
): Record<string, FieldStatus> {
    const required = new Set(
        ((schema as { required?: string[] } | undefined)?.required) ?? [],
    );
    const properties = ((schema as { properties?: Record<string, JsonSchema> } | undefined)?.properties) ?? {};
    const obj = (data ?? {}) as Record<string, unknown>;
    const out: Record<string, FieldStatus> = {};
    for (const key of Object.keys(properties)) {
        out[key] = isEmptyValue(obj[key]) && required.has(key)
            ? STATUS_MISSING
            : STATUS_COMPLETE;
    }
    return out;
}

/**
 * Rank used by {@link worseOf} — lower = more severe. Mirrors the server's
 * {@code FieldStatus.worse} ordinal-based merge (MISSING < IN_PROGRESS <
 * COMPLETE < APPROVED).
 */
const RANK: Record<string, number> = { M: 0, IP: 1, C: 2, A: 3 };

function worseOf(a: FieldStatus, b: FieldStatus): FieldStatus {
    return (RANK[a.abbreviation] ?? 9) <= (RANK[b.abbreviation] ?? 9) ? a : b;
}

/**
 * Per-Group section status, derived from the uiSchema's Group structure +
 * the field-status map. Mirrors the server's per-section rollup: for each
 * Group, take the worst-of statuses across the leaf fields it contains
 * (recursively, including conditionally-revealed sub-groups). The
 * resulting map is keyed by the Group's {@code label} — what
 * {@code SectionRenderer} looks up via {@code config.sectionStatus[label]}.
 *
 * <p>Groups without a {@code label} (rare) are skipped. Groups with no
 * leaf-field Controls roll up to COMPLETE.
 */
export function deriveSectionStatus(
    schema: JsonSchema | undefined,
    uiSchema: UISchemaElement | undefined,
    data: unknown,
): Record<string, FieldStatus> {
    if (!uiSchema) {return {};}
    const fieldStatus = deriveFieldStatus(schema, data);
    const out: Record<string, FieldStatus> = {};

    const walk = (node: UISchemaElement | undefined): void => {
        if (!node) {return;}
        const n = node as {
            type?: string;
            label?: string;
            elements?: UISchemaElement[];
        };
        if (n.type === 'Group' && n.label) {
            let worst: FieldStatus = STATUS_COMPLETE;
            for (const f of collectControlFields(node)) {
                const s = fieldStatus[f];
                if (s) {worst = worseOf(worst, s);}
            }
            out[n.label] = worst;
        }
        (n.elements ?? []).forEach(walk);
    };
    walk(uiSchema);
    return out;
}

function collectControlFields(node: UISchemaElement): string[] {
    const out: string[] = [];
    const walk = (n: UISchemaElement | undefined): void => {
        if (!n) {return;}
        const x = n as { type?: string; scope?: string; elements?: UISchemaElement[] };
        if (x.type === 'Control' && x.scope) {
            const leaf = x.scope.split('/').pop();
            if (leaf) {out.push(leaf);}
        }
        (x.elements ?? []).forEach(walk);
    };
    walk(node);
    return out;
}
