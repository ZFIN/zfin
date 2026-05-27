import * as React from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { api } from '../api/client';
import { LesionDTO } from '../api/types';
import { FormFor, seedFromDto, diffLeaves } from '../api/formHelpers';
import { lesionKey, mutationKey, useLesionById } from '../api/queries';
import { SaveStatusBadge, SaveStatus } from '../components/SaveStatusBadge';
import { sectionRendererEntry } from '../schemaForm/renderers/SectionRenderer';
import { rowControlRendererEntry } from '../schemaForm/renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from '../schemaForm/renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from '../schemaForm/renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from '../schemaForm/renderers/YesNoRadioRenderer';
import { selectWithOtherRendererEntry } from '../schemaForm/renderers/SelectWithOtherRenderer';

/**
 * Inline per-lesion editor (M7.1). Mounted inside an expanded
 * LesionsListRenderer card. Same shape as {@link AssayEdit}: schema
 * fetch + null-gated formData + autosave diff. The {@code lesionType}
 * dropdown drives conditional reveal of field clusters via uiSchema
 * rules on the server.
 */

export type LesionEditProps = {
    lesionId: number;
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
    selectWithOtherRendererEntry,
];

type FormDataShape = FormFor<LesionDTO>;

export function LesionEdit({ lesionId, mutationId }: LesionEditProps) {
    const qc = useQueryClient();
    const schemaQuery = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'lesion-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/lesions/form-schema'),
        staleTime: Infinity,
    });
    const lesionQuery = useLesionById(lesionId);
    const lesion = lesionQuery.data;

    const [formData, setFormData] = React.useState<FormDataShape | null>(null);
    const lastSavedRef = React.useRef<FormDataShape | null>(null);

    React.useEffect(() => {
        if (!lesion || formData !== null) {return;}
        const seed = seedFromDto(lesion);
        setFormData(seed);
        lastSavedRef.current = seed;
    }, [lesion?.id, formData]);

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
                let typeChanged = false;
                for (const [path, value] of changes) {
                    if (path === '/lesionType') {typeChanged = true;}
                    await api.patch<LesionDTO>(`/lesions/${lesionId}`, { path, value });
                }
                lastSavedRef.current = formData;
                setStatus('saved');
                if (typeChanged) {
                    qc.invalidateQueries({ queryKey: lesionKey(lesionId) });
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

    if (schemaQuery.isLoading || lesionQuery.isLoading || formData == null) {
        return <p className='text-muted'>Loading lesion…</p>;
    }
    if (schemaQuery.isError || lesionQuery.isError || !schemaQuery.data || !lesion) {
        return <div className='alert alert-danger'>Failed to load lesion form.</div>;
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
                config={{ lesionId, mutationId }}
                onChange={({ data }) => setFormData(data as FormDataShape)}
            />
        </div>
    );
}
