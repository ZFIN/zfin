import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import type { JsonFormsRendererRegistryEntry } from '@jsonforms/core';
import { fieldRenderers } from '../schemaForm/fieldRenderers';
import { VocabularyTermDTO } from '../api/types';

/**
 * Test harness for the schema-driven renderers.
 *
 * A renderer can't be mounted directly — `withJsonFormsControlProps` sources
 * its props from JsonForms context — so these tests drive a real `<JsonForms>`
 * with a small schema and uiSchema. That means the renderer is exercised the
 * way the app reaches it, through the same tester dispatch, rather than as a
 * plain component with hand-made props.
 */

/** Vocabulary rows the stubbed endpoint returns, keyed by vocabulary name. */
export type VocabularyFixtures = Record<string, VocabularyTermDTO[]>;

export const AMINO_ACIDS: VocabularyTermDTO[] = [
    { id: 'ZDB-TERM-1', label: 'Stop', abbreviation: null },
    { id: 'ZDB-TERM-2', label: 'Ala', abbreviation: 'A' },
    { id: 'ZDB-TERM-3', label: 'Val', abbreviation: 'V' },
];

export const PROTEIN_CONSEQUENCES: VocabularyTermDTO[] = [
    { id: 'ZDB-TERM-20', label: 'polypeptide truncation', abbreviation: null },
    { id: 'ZDB-TERM-21', label: 'amino acid substitution', abbreviation: null },
];

export const TRANSCRIPT_CONSEQUENCES: VocabularyTermDTO[] = [
    { id: 'ZDB-TERM-10', label: 'premature stop', abbreviation: null },
    { id: 'ZDB-TERM-11', label: 'missense', abbreviation: null },
    { id: 'ZDB-TERM-12', label: 'frameshift', abbreviation: null },
];

type StubOutcome = { kind: 'ok'; fixtures: VocabularyFixtures } | { kind: 'error' };

/**
 * Replace global fetch for the duration of a test.
 *
 * Only `/api/zirc/vocabulary/{name}` is understood; anything else rejects
 * loudly, so a renderer that starts calling an endpoint the test didn't
 * anticipate fails rather than hanging on a never-settling promise.
 */
function installFetchStub(outcome: StubOutcome): () => void {
    const original = globalThis.fetch;
    globalThis.fetch = (async (input: RequestInfo | URL) => {
        const url = String(input);
        const match = /\/api\/zirc\/vocabulary\/([\w]+)$/.exec(url);
        if (!match) {
            throw new Error(`unexpected fetch in test: ${url}`);
        }
        if (outcome.kind === 'error') {
            return new Response(JSON.stringify({ title: 'Boom', status: 500 }), {
                status: 500,
                headers: { 'Content-Type': 'application/problem+json' },
            });
        }
        return new Response(JSON.stringify(outcome.fixtures[match[1]] ?? []), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
        });
    }) as typeof globalThis.fetch;
    return () => { globalThis.fetch = original; };
}

export type HarnessOptions = {
    schema: object;
    uischema: object;
    data?: Record<string, unknown>;
    renderers?: JsonFormsRendererRegistryEntry[];
    config?: Record<string, unknown>;
    /** Vocabulary rows to serve; omit for the endpoint to fail. */
    vocabularies?: VocabularyFixtures;
};

export type Harness = RenderResult & {
    /** Form data after the most recent onChange, for asserting what was written. */
    latest: () => Record<string, unknown>;
    cleanupFetch: () => void;
};

export function renderForm(opts: HarnessOptions): Harness {
    const restore = installFetchStub(
        opts.vocabularies ? { kind: 'ok', fixtures: opts.vocabularies } : { kind: 'error' },
    );
    // retry:false so an error-path test fails fast instead of backing off, and
    // gcTime:0 so one test's vocabulary can't be served from cache to the next.
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false, gcTime: 0, staleTime: 0 } },
    });

    let current: Record<string, unknown> = opts.data ?? {};

    function Wrapper() {
        const [data, setData] = React.useState(current);
        return (
            <QueryClientProvider client={queryClient}>
                <table><tbody>
                    <JsonForms
                        schema={opts.schema}
                        uischema={opts.uischema}
                        data={data}
                        renderers={opts.renderers ?? fieldRenderers}
                        cells={[]}
                        config={opts.config ?? {}}
                        onChange={({ data: next }) => {
                            current = next as Record<string, unknown>;
                            setData(current);
                        }}
                    />
                </tbody></table>
            </QueryClientProvider>
        );
    }

    const result = render(<Wrapper/>);
    return Object.assign(result, {
        latest: () => current,
        cleanupFetch: restore,
    });
}

/** Minimal single-string-property schema, the shape most Controls need. */
export function stringSchema(...names: string[]) {
    return {
        type: 'object',
        properties: Object.fromEntries(names.map((n) => [n, { type: 'string', title: n }])),
    };
}
