import * as React from 'react';
import { QueryClientProvider, useQuery } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { queryClient } from '../queryClient';
import { api } from '../api/client';
import { MutationDTO } from '../api/types';
import { FormFor, seedFromDto } from '../api/formHelpers';
import { useMutationById } from '../api/queries';
import { SaveStatusBadge, SaveStatus } from '../components/SaveStatusBadge';
import { sectionRendererEntry } from '../schemaForm/renderers/SectionRenderer';
import { rowControlRendererEntry } from '../schemaForm/renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from '../schemaForm/renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from '../schemaForm/renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from '../schemaForm/renderers/YesNoRadioRenderer';
import { selectWithOtherRendererEntry } from '../schemaForm/renderers/SelectWithOtherRenderer';
import { publicationsListRendererEntry } from '../schemaForm/renderers/PublicationsListRenderer';
import { assaysListRendererEntry } from '../schemaForm/renderers/AssaysListRenderer';
import { genesListRendererEntry } from '../schemaForm/renderers/GenesListRenderer';
import { lesionsListRendererEntry } from '../schemaForm/renderers/LesionsListRenderer';
import { phenotypesListRendererEntry } from '../schemaForm/renderers/PhenotypesListRenderer';
import { autocompleteRendererEntry } from '../schemaForm/renderers/AutocompleteRenderer';

export type MutationEditProps = {
    // From data-mutation-id on the JSP mount.
    mutationId?: string;
    submissionId?: string;
};

export default function MutationEdit(props: MutationEditProps) {
    return (
        <QueryClientProvider client={queryClient}>
            <MutationEditInner {...props} />
        </QueryClientProvider>
    );
}

type FormDataShape = FormFor<MutationDTO>;

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

const AUTOSAVE_DEBOUNCE_MS = 800;

const renderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    yesNoRadioRendererEntry,
    selectWithOtherRendererEntry,
    publicationsListRendererEntry,
    assaysListRendererEntry,
    genesListRendererEntry,
    lesionsListRendererEntry,
    phenotypesListRendererEntry,
    autocompleteRendererEntry,
];


// Paths whose changes flow through dedicated endpoints (POST/DELETE), not
// the field-path PATCH. Without this filter, an Add/Delete on the assays
// list would emit a spurious PATCH /assays trying to write an array.
const EXTERNALLY_MANAGED_PATHS = new Set<string>(['/assays', '/genes', '/lesions', '/phenotypes']);

function diffLeaves(
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

function MutationEditInner({ mutationId, submissionId }: MutationEditProps) {
    const idNum = mutationId ? Number(mutationId) : null;

    const schemaQuery = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'mutation-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/mutations/form-schema'),
        staleTime: Infinity,
    });
    const mutationQuery = useMutationById(idNum);

    const mutation = mutationQuery.data;

    // formData starts as null and stays null until the seed effect has run.
    // Holding off on rendering JsonForms eliminates the window in which
    // formData (captured from useState's initial value) and lastSavedRef
    // (mutated by the seed effect) can disagree — that disagreement was the
    // root cause of spurious "clear field" PATCHes under slow page loads.
    // Documented in memory `zirc-schema-form-known-issues.md`.
    const [formData, setFormData] = React.useState<FormDataShape | null>(null);
    const lastSavedRef = React.useRef<FormDataShape | null>(null);

    React.useEffect(() => {
        if (!mutation || formData !== null) {return;}
        const seed = seedFromDto(mutation);
        setFormData(seed);
        lastSavedRef.current = seed;
    }, [mutation?.id, formData]);

    // Mirror server-managed assays list into the local form state after the
    // initial seed, so Add/Delete refetches surface in the renderer without
    // triggering the autosave diff. Same pattern as the mutations sync in
    // SchemaForm on the submission page.
    const assaysKey = JSON.stringify(mutation?.assays ?? []);
    React.useEffect(() => {
        if (!mutation || formData == null) {return;}
        const next = mutation.assays ?? [];
        setFormData((d) => (d == null ? d : { ...d, assays: next }));
        if (lastSavedRef.current != null) {
            lastSavedRef.current = { ...lastSavedRef.current, assays: next };
        }
    }, [assaysKey]);

    // Same mirror pattern for /genes.
    const genesKey = JSON.stringify(mutation?.genes ?? []);
    React.useEffect(() => {
        if (!mutation || formData == null) {return;}
        const next = mutation.genes ?? [];
        setFormData((d) => (d == null ? d : { ...d, genes: next }));
        if (lastSavedRef.current != null) {
            lastSavedRef.current = { ...lastSavedRef.current, genes: next };
        }
    }, [genesKey]);

    // Same mirror pattern for /lesions.
    const lesionsKey = JSON.stringify(mutation?.lesions ?? []);
    React.useEffect(() => {
        if (!mutation || formData == null) {return;}
        const next = mutation.lesions ?? [];
        setFormData((d) => (d == null ? d : { ...d, lesions: next }));
        if (lastSavedRef.current != null) {
            lastSavedRef.current = { ...lastSavedRef.current, lesions: next };
        }
    }, [lesionsKey]);

    // Same mirror pattern for /phenotypes.
    const phenotypesKey = JSON.stringify(mutation?.phenotypes ?? []);
    React.useEffect(() => {
        if (!mutation || formData == null) {return;}
        const next = mutation.phenotypes ?? [];
        setFormData((d) => (d == null ? d : { ...d, phenotypes: next }));
        if (lastSavedRef.current != null) {
            lastSavedRef.current = { ...lastSavedRef.current, phenotypes: next };
        }
    }, [phenotypesKey]);

    const [status, setStatus] = React.useState<SaveStatus>('idle');
    const [errorMessage, setErrorMessage] = React.useState<string | null>(null);

    const formDataKey = formData == null ? 'null' : JSON.stringify(formData);

    React.useEffect(() => {
        // No autosave until the seed has applied — guarantees that any diff
        // is between two real values, not "empty default vs seed".
        if (!idNum || formData == null || lastSavedRef.current == null) {return;}

        const handle = window.setTimeout(async () => {
            const changes = diffLeaves(lastSavedRef.current, formData)
                .filter(([path]) => !EXTERNALLY_MANAGED_PATHS.has(path));
            if (changes.length === 0) {return;}

            setStatus('saving');
            setErrorMessage(null);
            try {
                for (const [path, value] of changes) {
                    await api.patch<MutationDTO>(
                        `/mutations/${idNum}`,
                        { path, value },
                    );
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

    if (!idNum) {
        return <div className='alert alert-danger'>Missing mutation id.</div>;
    }
    if (schemaQuery.isLoading || mutationQuery.isLoading || formData == null) {
        return <p className='text-muted'>Loading…</p>;
    }
    if (schemaQuery.isError || mutationQuery.isError || !schemaQuery.data || !mutation) {
        return <div className='alert alert-danger'>Failed to load mutation form.</div>;
    }

    return (
        <div>
            <div className='d-flex justify-content-end mb-1'>
                <SaveStatusBadge status={status} message={errorMessage} />
            </div>
            <JsonForms
                schema={schemaQuery.data.schema}
                uischema={schemaQuery.data.uiSchema}
                data={formData}
                renderers={renderers}
                cells={[]}
                config={{ mutationId: idNum, submissionId }}
                onChange={({ data }) => setFormData(data as FormDataShape)}
            />
        </div>
    );
}
