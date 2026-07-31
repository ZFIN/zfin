// Hand-edited (NOT generated). Lives next to the generated types.ts but
// stays out of GenerateTypeScript.java's overwrite path.

/**
 * Server-set DTO keys that don't belong in form-edit scope: identity
 * columns, FK identifiers, sort order. The corresponding PATCH endpoints
 * carry these in the URL, not the body.
 *
 * Used at the type level by {@link FormFor} and at runtime by
 * {@link seedFromDto}; keeping both in sync avoids accidental drift
 * between "what types think isn't editable" and "what the seed strips".
 */
const SERVER_KEYS = ['id', 'sortOrder', 'mutationId', 'lineSubmissionId'] as const;

type ServerManagedKey = (typeof SERVER_KEYS)[number];

/**
 * Per-form data shape derived from a DTO. Strips server-managed keys
 * at the type level so any code that tries to read or write them in
 * form scope fails at compile time.
 *
 * Use as `type FormDataShape = FormFor<MutationDTO>` in each page
 * component.
 */
export type FormFor<T> = Omit<T, Extract<keyof T, ServerManagedKey>>;

/**
 * Build a form-data seed from a DTO by dropping the server-managed
 * keys. Field values pass through as-is — boxed nulls stay null,
 * arrays stay arrays. The renderers handle null gracefully via
 * `value ?? ''` at the input level, so no per-field defaulting is
 * needed here.
 */
export function seedFromDto<T extends object>(dto: T): FormFor<T> {
    const drop: Set<string> = new Set(SERVER_KEYS);
    const out: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(dto)) {
        if (drop.has(k)) {continue;}
        out[k] = v;
    }
    return out as FormFor<T>;
}

/**
 * Recursively diff two form-data trees, returning one
 * [jsonPointerPath, newValue] entry per changed leaf. The autosave loop
 * walks the result and emits one field-path PATCH per entry.
 *
 * <p>Arrays are treated as leaves: a changed array yields a single entry
 * for the whole array (compared by JSON.stringify), never per-element
 * paths. That's deliberate — the server's FIELDS dispatch takes a whole
 * array value for list fields (publications, reasons), and
 * server-managed object arrays are filtered out upstream via the
 * `managesOwnPersistence` skip-set before they ever reach here.
 *
 * <p>Plain objects recurse, prefixing the path. Leaf scalars compare by
 * {@code Object.is}. A null/undefined new value is normalized to null on
 * the wire.
 */
export function diffLeaves(
    prev: unknown,
    curr: unknown,
    basePath = '',
): Array<[string, unknown]> {
    const isPlainObject = (v: unknown): v is Record<string, unknown> =>
        typeof v === 'object' && v !== null && !Array.isArray(v);
    if (Array.isArray(prev) || Array.isArray(curr)) {
        if (JSON.stringify(prev ?? null) !== JSON.stringify(curr ?? null)) {
            return [[basePath || '/', curr ?? null]];
        }
        return [];
    }
    if (isPlainObject(prev) && isPlainObject(curr)) {
        // Union of keys, deduped without iterating a Set (keeps the helper
        // target-agnostic — the project's tsconfig targets es5).
        const keys = Object.keys(prev)
            .concat(Object.keys(curr))
            .filter((k, i, all) => all.indexOf(k) === i);
        const changes: Array<[string, unknown]> = [];
        for (const key of keys) {
            changes.push(...diffLeaves(prev[key], curr[key], `${basePath}/${key}`));
        }
        return changes;
    }
    if (!Object.is(prev, curr)) {
        return [[basePath || '/', curr ?? null]];
    }
    return [];
}
