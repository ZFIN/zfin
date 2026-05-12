// Tiny constructor helpers for the form schema. Each just injects the
// discriminating `kind` field so schemas read like a config object instead
// of a list of JSX-ish entries. Use them in your schema file:
//
//   const MY_SCHEMA: FormNode[] = [
//     section('overview', 'Overview', [
//       field({path: 'name', label: 'Name', type: 'text'}),
//       field({path: 'age',  label: 'Age',  type: 'int', suffix: 'yrs'}),
//     ]),
//   ];

import {
    ArrayNode,
    CustomNode,
    FieldNode,
    FormNode,
    SectionNode,
    Visible,
} from './types';

export const section = (
    id: string,
    title: string,
    children: FormNode[],
    extra?: Partial<Omit<SectionNode, 'kind' | 'id' | 'title' | 'children'>>,
): SectionNode => ({kind: 'section', id, title, children, ...extra});

export const field = (props: Omit<FieldNode, 'kind'>): FieldNode =>
    ({kind: 'field', ...props});

export const array = (props: Omit<ArrayNode, 'kind'>): ArrayNode =>
    ({kind: 'array', ...props});

export const custom = (props: Omit<CustomNode, 'kind'>): CustomNode =>
    ({kind: 'custom', ...props});

/**
 * Adapter for the typeMatrices pattern: given a `{type: [fieldKey, ...]}`
 * record, returns a visibility predicate that shows a field only when the
 * row's discriminator field maps to a list containing this field's key.
 *
 *   field({
 *     path: 'lesionSizeBp',
 *     ...,
 *     visible: showWhenType(LESION_TYPE_TO_FIELDS, 'lesionType', 'lesionSizeBp'),
 *   })
 *
 * The same matrix can be consumed elsewhere (server-side validation,
 * summarize callbacks, type-picker modal); the schema doesn't own the
 * rules, it just applies them per-field.
 */
export function showWhenType<K extends string, F extends string>(
    matrix: Record<K, F[]>,
    discriminatorPath: string,
    fieldKey: F,
): Visible {
    return (_dto, row) => {
        if (!row || typeof row !== 'object') {
            return false;
        }
        const key = (row as Record<string, unknown>)[discriminatorPath] as K | null | undefined;
        if (key == null) {
            return false;
        }
        const fields = matrix[key];
        return Array.isArray(fields) && fields.includes(fieldKey);
    };
}

/** Compose multiple visibility predicates with AND semantics. */
export function allVisible(...preds: Visible[]): Visible {
    return (dto, row) => preds.every(p => p(dto, row));
}

/** Compose multiple visibility predicates with OR semantics. */
export function anyVisible(...preds: Visible[]): Visible {
    return (dto, row) => preds.some(p => p(dto, row));
}
