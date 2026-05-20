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
