import * as React from 'react';
import { useQuery } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { api } from '../api/client';
import { PhenotypeDTO } from '../api/types';
import { FormFor, seedFromDto } from '../api/formHelpers';
import { usePhenotypeById } from '../api/queries';
import { SaveStatusBadge, SaveStatus } from '../components/SaveStatusBadge';
import { sectionRendererEntry } from '../schemaForm/renderers/SectionRenderer';
import { rowControlRendererEntry } from '../schemaForm/renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from '../schemaForm/renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from '../schemaForm/renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from '../schemaForm/renderers/YesNoRadioRenderer';
import { publicationsListRendererEntry } from '../schemaForm/renderers/PublicationsListRenderer';
import { phenotypeTimingRendererEntry } from '../schemaForm/renderers/PhenotypeTimingRenderer';

/**
 * Inline per-phenotype editor (M8.1). Mirrors {@link LesionEdit}:
 * schema fetch + null-gated formData + autosave diff. No type matrix
 * — all fields are visible for every phenotype.
 */

export type PhenotypeEditProps = {
    phenotypeId: number;
    mutationId?: number;
};

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

const AUTOSAVE_DEBOUNCE_MS = 800;

const renderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    yesNoRadioRendererEntry,
    publicationsListRendererEntry,
    phenotypeTimingRendererEntry,
];

type FormDataShape = FormFor<PhenotypeDTO>;

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

export function PhenotypeEdit({ phenotypeId, mutationId }: PhenotypeEditProps) {
    const schemaQuery = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'phenotype-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/phenotypes/form-schema'),
        staleTime: Infinity,
    });
    const phenotypeQuery = usePhenotypeById(phenotypeId);
    const phenotype = phenotypeQuery.data;

    const [formData, setFormData] = React.useState<FormDataShape | null>(null);
    const lastSavedRef = React.useRef<FormDataShape | null>(null);

    React.useEffect(() => {
        if (!phenotype || formData !== null) {return;}
        const seed = seedFromDto(phenotype);
        setFormData(seed);
        lastSavedRef.current = seed;
    }, [phenotype?.id, formData]);

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
                for (const [path, value] of changes) {
                    await api.patch<PhenotypeDTO>(`/phenotypes/${phenotypeId}`, { path, value });
                }
                lastSavedRef.current = formData;
                setStatus('saved');
            } catch (e: unknown) {
                setStatus('error');
                setErrorMessage(e instanceof Error ? e.message : 'Save failed');
            }
        }, AUTOSAVE_DEBOUNCE_MS);
        return () => window.clearTimeout(handle);
    }, [formDataKey]);

    if (schemaQuery.isLoading || phenotypeQuery.isLoading || formData == null) {
        return <p className='text-muted'>Loading phenotype…</p>;
    }
    if (schemaQuery.isError || phenotypeQuery.isError || !schemaQuery.data || !phenotype) {
        return <div className='alert alert-danger'>Failed to load phenotype form.</div>;
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
                config={{ phenotypeId, mutationId }}
                onChange={({ data }) => setFormData(data as FormDataShape)}
            />
        </div>
    );
}
