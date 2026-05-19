import * as React from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { api } from '../api/client';
import { AssayFileDTO, AssayDTO } from '../api/types';
import { assayKey, mutationKey, useAssayById } from '../api/queries';
import { SaveStatusBadge, SaveStatus } from '../components/SaveStatusBadge';
import { sectionRendererEntry } from '../schemaForm/renderers/SectionRenderer';
import { rowControlRendererEntry } from '../schemaForm/renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from '../schemaForm/renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from '../schemaForm/renderers/TextareaRowRenderer';
import { selectWithOtherRendererEntry } from '../schemaForm/renderers/SelectWithOtherRenderer';
import { publicationsListRendererEntry } from '../schemaForm/renderers/PublicationsListRenderer';
import { attachmentsRendererEntry } from '../schemaForm/renderers/AttachmentsRenderer';

export type AssayEditProps = {
    assayId: number;
    // Parent mutation id is only used to invalidate the mutation query when
    // assayType changes so the parent card summary reflects the new label.
    mutationId?: number;
};

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

const AUTOSAVE_DEBOUNCE_MS = 800;

const renderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    selectWithOtherRendererEntry,
    publicationsListRendererEntry,
    attachmentsRendererEntry,
];

type FormDataShape = Omit<AssayDTO, 'id' | 'mutationId' | 'sortOrder'>;

// Server-managed: uploads/deletes go through dedicated endpoints, not the
// field-path PATCH. AssayEdit must skip /attachments in its leaf diff.
const EXTERNALLY_MANAGED_PATHS = new Set<string>(['/attachments']);

function initialDataFromAssay(a: AssayDTO): FormDataShape {
    return {
        assayType: a.assayType ?? '',
        additionalInfo: a.additionalInfo ?? '',
        forwardPrimer: a.forwardPrimer ?? '',
        reversePrimer: a.reversePrimer ?? '',
        expectedWtPcr: a.expectedWtPcr ?? '',
        expectedMutPcr: a.expectedMutPcr ?? '',
        sequencingPrimer: a.sequencingPrimer ?? '',
        dcapsMismatchPrimer: a.dcapsMismatchPrimer ?? '',
        wtSpecificPrimer: a.wtSpecificPrimer ?? '',
        mutSpecificPrimer: a.mutSpecificPrimer ?? '',
        commonPrimer: a.commonPrimer ?? '',
        kaspGenomicSequence: a.kaspGenomicSequence ?? '',
        restrictionEnzymeName: a.restrictionEnzymeName ?? '',
        restrictionEnzymeCatalog: a.restrictionEnzymeCatalog ?? '',
        enzymeCleaves: a.enzymeCleaves ?? [],
        expectedWtDigest: a.expectedWtDigest ?? '',
        expectedMutDigest: a.expectedMutDigest ?? '',
        sslpMarkerName: a.sslpMarkerName ?? '',
        sslpDistance: a.sslpDistance ?? '',
        sslpGenomicLocation: a.sslpGenomicLocation ?? '',
        sslpInducedBackground: a.sslpInducedBackground ?? '',
        sslpOutcrossedBackground: a.sslpOutcrossedBackground ?? '',
        sslpInducedPcr: a.sslpInducedPcr ?? '',
        sslpOutcrossedPcr: a.sslpOutcrossedPcr ?? '',
        attachments: a.attachments ?? [],
    };
}

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

/**
 * Schema-driven per-assay editor (M4.2). Mounted inline inside an expanded
 * card on the AssaysListRenderer; reuses the same SchemaForm / MutationEdit
 * autosave pattern with the null-gated seed effect (see
 * memory: zirc-schema-form-known-issues).
 *
 * The {@code assayType} dropdown drives conditional reveal of field
 * clusters via uiSchema rules — the server's matrix lives in
 * {@link org.zfin.zirc.api.ZircAssayFormSchema}, not the client.
 */
export function AssayEdit({ assayId, mutationId }: AssayEditProps) {
    const qc = useQueryClient();

    const schemaQuery = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'assay-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/assays/form-schema'),
        staleTime: Infinity,
    });
    const assayQuery = useAssayById(assayId);
    const assay = assayQuery.data;

    const [formData, setFormData] = React.useState<FormDataShape | null>(null);
    const lastSavedRef = React.useRef<FormDataShape | null>(null);

    React.useEffect(() => {
        if (!assay || formData !== null) {return;}
        const seed = initialDataFromAssay(assay);
        setFormData(seed);
        lastSavedRef.current = seed;
    }, [assay?.id, formData]);

    // Mirror server-managed attachments after each refetch (upload/delete)
    // so the renderer sees the new list. Same idiom as the mutations sync
    // in SchemaForm and the assays sync in MutationEdit.
    const attachmentsKey = JSON.stringify(assay?.attachments ?? []);
    React.useEffect(() => {
        if (!assay || formData == null) {return;}
        const next: AssayFileDTO[] = assay.attachments ?? [];
        setFormData((d) => (d == null ? d : { ...d, attachments: next }));
        if (lastSavedRef.current != null) {
            lastSavedRef.current = { ...lastSavedRef.current, attachments: next };
        }
    }, [attachmentsKey]);

    const [status, setStatus] = React.useState<SaveStatus>('idle');
    const [errorMessage, setErrorMessage] = React.useState<string | null>(null);

    const formDataKey = formData == null ? 'null' : JSON.stringify(formData);

    React.useEffect(() => {
        if (formData == null || lastSavedRef.current == null) {return;}

        const handle = window.setTimeout(async () => {
            const changes = diffLeaves(lastSavedRef.current, formData)
                .filter(([path]) => !EXTERNALLY_MANAGED_PATHS.has(path));
            if (changes.length === 0) {return;}

            setStatus('saving');
            setErrorMessage(null);
            try {
                let assayTypeChanged = false;
                for (const [path, value] of changes) {
                    if (path === '/assayType') {assayTypeChanged = true;}
                    await api.patch<AssayDTO>(
                        `/assays/${assayId}`,
                        { path, value },
                    );
                }
                lastSavedRef.current = formData;
                setStatus('saved');
                // Refresh the parent card summary if the type label changed.
                if (assayTypeChanged) {
                    qc.invalidateQueries({ queryKey: assayKey(assayId) });
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

    if (schemaQuery.isLoading || assayQuery.isLoading || formData == null) {
        return <p className='text-muted'>Loading assay…</p>;
    }
    if (schemaQuery.isError || assayQuery.isError || !schemaQuery.data || !assay) {
        return <div className='alert alert-danger'>Failed to load assay form.</div>;
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
                config={{ assayId }}
                onChange={({ data }) => setFormData(data as FormDataShape)}
            />
        </div>
    );
}
