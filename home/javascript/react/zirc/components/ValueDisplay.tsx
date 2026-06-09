import * as React from 'react';
import type { JsonSchema } from '@jsonforms/core';

type OneOfEntry = { const?: unknown; title?: string };

type Props = {
    value: unknown;
    schema?: JsonSchema;
};

/**
 * Read-only formatter for a single field value. Knows how to render:
 *   - null / undefined / empty string  → em dash
 *   - boolean                          → "Yes" / "No"
 *   - string with enum titles in schema.oneOf → resolved title
 *   - string[]                         → comma-joined (with enum-title lookup)
 *   - any other primitive              → String(value)
 *
 * Object values fall through to JSON.stringify; the row-style renderers only
 * call this on leaf scopes so that case shouldn't arise in normal use.
 */
export function ValueDisplay({ value, schema }: Props) {
    const out = renderValue(value, schema);
    if (out == null || out === '') {
        return <span className='text-muted'>&mdash;</span>;
    }
    return <>{out}</>;
}

function renderValue(value: unknown, schema?: JsonSchema): React.ReactNode {
    if (value === null || value === undefined || value === '') {return null;}
    if (typeof value === 'boolean') {return value ? 'Yes' : 'No';}
    if (Array.isArray(value)) {
        if (value.length === 0) {return null;}
        const itemSchema = (schema && (schema as { items?: JsonSchema }).items) ?? undefined;
        const labels = value.map((v) => labelFor(v, itemSchema) ?? String(v));
        return labels.join(', ');
    }
    if (typeof value === 'string') {
        const resolved = labelFor(value, schema);
        return resolved ?? value;
    }
    if (typeof value === 'number') {return String(value);}
    return JSON.stringify(value);
}

function labelFor(value: unknown, schema?: JsonSchema): string | null {
    if (!schema || typeof value !== 'string') {return null;}
    const oneOf = (schema as { oneOf?: OneOfEntry[] }).oneOf;
    if (!oneOf) {return null;}
    const match = oneOf.find((e) => e.const === value);
    return match?.title ?? null;
}
