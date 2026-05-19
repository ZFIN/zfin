import * as React from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { api } from '../api/client';
import { GeneDTO } from '../api/types';
import { geneKey, mutationKey, useGeneById } from '../api/queries';
import { SaveStatusBadge, SaveStatus } from '../components/SaveStatusBadge';
import { sectionRendererEntry } from '../schemaForm/renderers/SectionRenderer';
import { rowControlRendererEntry } from '../schemaForm/renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from '../schemaForm/renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from '../schemaForm/renderers/TextareaRowRenderer';
import { autocompleteRendererEntry } from '../schemaForm/renderers/AutocompleteRenderer';

/**
 * Inline per-gene editor (M6.1). Mounts inside an expanded
 * GenesListRenderer card on the mutation edit page. Mirrors
 * {@link AssayEdit} structure: schema fetch + null-gated formData
 * + autosave diff against lastSavedRef.
 *
 * <p>Only four fields, but the mutatedGeneZdbID field is interesting —
 * it uses the {@code autocomplete} widget against
 * {@code /api/zirc/autocomplete/markers}, which is the first place we
 * exercise the M5.2 renderer in production.
 */

export type GeneEditProps = {
    geneId: number;
    mutationId?: number;
};

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

const AUTOSAVE_DEBOUNCE_MS = 800;

const renderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    autocompleteRendererEntry,
];

type FormDataShape = {
    mutatedGeneZdbID: string | null;
    linkageGroup: string | null;
    genbankGenomicDna: string | null;
    genbankCdna: string | null;
};

function initialDataFromGene(g: GeneDTO): FormDataShape {
    return {
        mutatedGeneZdbID:  g.mutatedGeneZdbID ?? '',
        linkageGroup:      g.linkageGroup ?? '',
        genbankGenomicDna: g.genbankGenomicDna ?? '',
        genbankCdna:       g.genbankCdna ?? '',
    };
}

function diffLeaves(prev: unknown, curr: unknown, basePath = ''): Array<[string, unknown]> {
    const isPlainObject = (v: unknown): v is Record<string, unknown> =>
        typeof v === 'object' && v !== null && !Array.isArray(v);
    if (Array.isArray(prev) || Array.isArray(curr)) {
        if (JSON.stringify(prev ?? null) !== JSON.stringify(curr ?? null)) {
            return [[basePath || '/', curr ?? null]];
        }
        return [];
    }
    if (isPlainObject(prev) && isPlainObject(curr)) {
        const keys = new Set([...Object.keys(prev), ...Object.keys(curr)]);
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

export function GeneEdit({ geneId, mutationId }: GeneEditProps) {
    const qc = useQueryClient();
    const schemaQuery = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'gene-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/genes/form-schema'),
        staleTime: Infinity,
    });
    const geneQuery = useGeneById(geneId);
    const gene = geneQuery.data;

    const [formData, setFormData] = React.useState<FormDataShape | null>(null);
    const lastSavedRef = React.useRef<FormDataShape | null>(null);

    React.useEffect(() => {
        if (!gene || formData !== null) {return;}
        const seed = initialDataFromGene(gene);
        setFormData(seed);
        lastSavedRef.current = seed;
    }, [gene?.id, formData]);

    const [status, setStatus] = React.useState<SaveStatus>('idle');
    const [errorMessage, setErrorMessage] = React.useState<string | null>(null);
    const formDataKey = formData == null ? 'null' : JSON.stringify(formData);

    React.useEffect(() => {
        if (formData == null || lastSavedRef.current == null) {return;}
        const handle = window.setTimeout(async () => {
            const changes = diffLeaves(lastSavedRef.current, formData);
            if (changes.length === 0) {return;}
            setStatus('saving');
            setErrorMessage(null);
            try {
                let geneChanged = false;
                for (const [path, value] of changes) {
                    if (path === '/mutatedGeneZdbID') {geneChanged = true;}
                    await api.patch<GeneDTO>(`/genes/${geneId}`, { path, value });
                }
                lastSavedRef.current = formData;
                setStatus('saved');
                if (geneChanged) {
                    qc.invalidateQueries({ queryKey: geneKey(geneId) });
                    if (mutationId) {
                        qc.invalidateQueries({ queryKey: mutationKey(mutationId) });
                    }
                }
            } catch (e: unknown) {
                setStatus('error');
                setErrorMessage(e instanceof Error ? e.message : 'Save failed');
            }
        }, AUTOSAVE_DEBOUNCE_MS);
        return () => window.clearTimeout(handle);
    }, [formDataKey]);

    if (schemaQuery.isLoading || geneQuery.isLoading || formData == null) {
        return <p className='text-muted'>Loading gene…</p>;
    }
    if (schemaQuery.isError || geneQuery.isError || !schemaQuery.data || !gene) {
        return <div className='alert alert-danger'>Failed to load gene form.</div>;
    }

    return (
        <div className='mt-2'>
            <div className='d-flex justify-content-end mb-1'>
                <SaveStatusBadge status={status} message={errorMessage} />
            </div>
            <JsonForms
                schema={schemaQuery.data.schema}
                uischema={schemaQuery.data.uiSchema}
                data={formData}
                renderers={renderers}
                cells={[]}
                config={{ geneId, mutationId }}
                onChange={({ data }) => setFormData(data as FormDataShape)}
            />
        </div>
    );
}
