import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { JsonForms, withJsonFormsControlProps } from '@jsonforms/react';
import { useQuery } from '@tanstack/react-query';
import { AssaySummaryDTO } from '../../api/types';
import { api } from '../../api/client';
import { useAddAssay, useAssayById, useDeleteAssay } from '../../api/queries';
import { AssayEdit } from '../../pages/AssayEdit';
import { viewConfigFrom } from '../useViewConfig';
import { aggregateRenderers } from '../aggregateRenderers';
import { deriveFieldStatus, deriveSectionStatus } from '../deriveStatus';

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

function AssayDetailCard({
    summary, n, schema, uiSchema,
}: {
    summary: AssaySummaryDTO;
    n: number;
    schema: JsonSchema;
    uiSchema: UISchemaElement;
}) {
    const q = useAssayById(summary.id);
    return (
        <div className='card mb-2'>
            <div className='card-header py-1'>
                <strong>{summary.assayType || `Assay ${n}`}</strong>
            </div>
            <div className='card-body py-2'>
                {q.isLoading && <p className='text-muted small mb-0'>Loading…</p>}
                {q.isError && (
                    <div className='alert alert-warning mb-0'>
                        Failed to load assay {summary.id}.
                    </div>
                )}
                {q.data && (
                    <JsonForms
                        schema={schema}
                        uischema={uiSchema}
                        data={q.data}
                        renderers={aggregateRenderers}
                        cells={[]}
                        readonly
                        config={{
                            mode: 'view',
                            readonly: true,
                            assayId: summary.id,
                            recId: `ZIRC-GA-${summary.id}`,
                            fieldStatus: deriveFieldStatus(schema, q.data),
                            sectionStatus: deriveSectionStatus(schema, uiSchema, q.data),
                        }}
                        onChange={() => { /* read-only */ }}
                    />
                )}
            </div>
        </div>
    );
}

/**
 * Renders the per-mutation list of genotyping assays as a stack of cards.
 *
 * Each card collapses to a one-line summary (assay type + sort order) and
 * expands to mount {@link AssayEdit} inline. Expanding doesn't change the
 * URL — the assay editor talks directly to /api/zirc/assays/{id} with flat
 * paths, so it sidesteps the mutation form's path namespace. Add/Delete
 * still go through their dedicated endpoints, and MutationEdit's diff
 * filter skips /assays so those mutations don't fight autosave.
 *
 * Expansion state is local component state (a Set of expanded ids) so
 * collapse on parent re-render is avoided.
 *
 * The mutation id needed for Add/Delete + the AssayEdit's parent-summary
 * invalidate comes through JsonForms' `config` prop.
 */
function AssaysListRenderer({ data, schema, config }: ControlProps) {
    const assays = (data as AssaySummaryDTO[] | undefined) ?? [];
    const mutationId = (config as { mutationId?: number } | undefined)?.mutationId;
    const addAssay = useAddAssay();
    const deleteAssay = useDeleteAssay();
    const [expanded, setExpanded] = React.useState<Set<number>>(new Set());

    const view = viewConfigFrom(config);
    const assaySchema = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'assay-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/assays/form-schema'),
        staleTime: Infinity,
        enabled: view.readonly,
    });
    if (view.readonly) {
        if (assays.length === 0) {
            return <p className='text-muted small mb-0'>No genotyping assays.</p>;
        }
        if (assaySchema.isLoading) {
            return <p className='text-muted small'>Loading assay details…</p>;
        }
        if (assaySchema.isError || !assaySchema.data) {
            return <div className='alert alert-warning'>Failed to load assay form schema.</div>;
        }
        return (
            <div>
                {assays.map((a, i) => (
                    <AssayDetailCard
                        key={a.id}
                        summary={a}
                        n={i + 1}
                        schema={assaySchema.data.schema}
                        uiSchema={assaySchema.data.uiSchema}
                    />
                ))}
            </div>
        );
    }

    // Server-published MAX_ASSAYS_PER_MUTATION via JSON Schema maxItems.
    const maxItems = (schema as { maxItems?: number } | undefined)?.maxItems;
    const atCapacity = maxItems != null && assays.length >= maxItems;
    const capTitle = atCapacity
        ? `Maximum ${maxItems} genotyping assays per mutation.`
        : undefined;

    const toggle = (id: number) => {
        setExpanded((prev) => {
            const next = new Set(prev);
            if (next.has(id)) {next.delete(id);} else {next.add(id);}
            return next;
        });
    };

    const handleAdd = () => {
        if (!mutationId) {return;}
        addAssay.mutate(mutationId, {
            // Open the newly-created card so the user can fill it in right away.
            onSuccess: (mutation) => {
                const newest = (mutation.assays ?? []).reduce<AssaySummaryDTO | null>(
                    (best, a) => (best == null || a.id > best.id ? a : best),
                    null,
                );
                if (newest) {
                    setExpanded((prev) => new Set(prev).add(newest.id));
                }
            },
        });
    };

    const handleDelete = (assayId: number) => {
        if (!mutationId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this genotyping assay? This action cannot be undone.')) {return;}
        deleteAssay.mutate({ mutationId, assayId });
    };

    if (assays.length === 0) {
        return (
            <div>
                <p className='text-muted'>No genotyping assays recorded for this mutation.</p>
                <button
                    type='button'
                    className='btn btn-sm btn-outline-secondary'
                    onClick={handleAdd}
                    disabled={!mutationId || addAssay.isPending || atCapacity}
                    title={capTitle}
                >
                    + Add assay
                </button>
            </div>
        );
    }

    return (
        <div>
            <ul className='list-unstyled'>
                {assays.map((a) => {
                    const isOpen = expanded.has(a.id);
                    return (
                        <li key={a.id} className='border rounded p-2 mb-2'>
                            <div className='d-flex justify-content-between align-items-center'>
                                <button
                                    type='button'
                                    className='btn btn-link p-0 text-left'
                                    onClick={() => toggle(a.id)}
                                    aria-expanded={isOpen}
                                >
                                    <span className='mr-2'>{isOpen ? '▾' : '▸'}</span>
                                    <strong>{a.assayType || `Assay #${a.sortOrder}`}</strong>
                                </button>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => handleDelete(a.id)}
                                    disabled={deleteAssay.isPending}
                                >
                                    Delete
                                </button>
                            </div>
                            {isOpen && (
                                <AssayEdit assayId={a.id} mutationId={mutationId} />
                            )}
                        </li>
                    );
                })}
            </ul>
            <button
                type='button'
                className='btn btn-sm btn-outline-secondary'
                onClick={handleAdd}
                disabled={!mutationId || addAssay.isPending || atCapacity}
                title={capTitle}
            >
                + Add assay
            </button>
        </div>
    );
}

export const assaysListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'assaysList'))),
    renderer: withJsonFormsControlProps(AssaysListRenderer),
};
