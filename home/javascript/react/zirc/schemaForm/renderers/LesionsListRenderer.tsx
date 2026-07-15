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
import { LesionSummaryDTO } from '../../api/types';
import { api } from '../../api/client';
import { useAddLesion, useDeleteLesion, useLesionById } from '../../api/queries';
import { LesionEdit } from '../../pages/LesionEdit';
import { viewConfigFrom } from '../useViewConfig';
import { aggregateRenderers } from '../aggregateRenderers';

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

/** "point_mutation" -> "Point Mutation"; for collapsed-card summaries. */
function humanize(snake: string): string {
    return snake
        .replace(/_/g, ' ')
        .replace(/\b\w/g, (c) => c.toUpperCase());
}

/**
 * Read-only card for one lesion. Fetches the full {@link LesionDTO} so
 * the nested JsonForms can render every field the schema declares (the
 * parent mutation only carries summaries). React Query coalesces and
 * caches per id, so re-renders / sibling renderers don't refetch.
 */
function LesionDetailCard({
    summary, n, schema, uiSchema, fieldStatus, sectionStatus,
}: {
    summary: LesionSummaryDTO;
    n: number;
    schema: JsonSchema;
    uiSchema: UISchemaElement;
    fieldStatus: Record<string, unknown>;
    sectionStatus: Record<string, unknown>;
}) {
    const q = useLesionById(summary.id);
    return (
        <div className='card mb-2'>
            <div className='card-header py-1'>
                <strong>{summary.lesionType ? humanize(summary.lesionType) : `Lesion ${n}`}</strong>
            </div>
            <div className='card-body py-2'>
                {q.isLoading && <p className='text-muted small mb-0'>Loading…</p>}
                {q.isError && (
                    <div className='alert alert-warning mb-0'>
                        Failed to load lesion {summary.id}.
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
                            lesionId: summary.id,
                            recId: `ZIRC-LESION-${summary.id}`,
                            fieldStatus,
                            sectionStatus,
                        }}
                        onChange={() => { /* read-only */ }}
                    />
                )}
            </div>
        </div>
    );
}

/**
 * Per-mutation lesions list with inline-expand cards. Same shape as
 * AssaysListRenderer and GenesListRenderer — collapsed card header
 * shows the lesion type; expanded card mounts {@link LesionEdit}.
 *
 * <p>{@code mutationId} arrives via JsonForms' {@code config} prop,
 * threaded by MutationEdit. {@code schema.maxItems} drives the
 * "+ Add lesion" disabled-at-capacity state.
 */
function LesionsListRenderer({ data, schema, config }: ControlProps) {
    const lesions = (data as LesionSummaryDTO[] | undefined) ?? [];
    const mutationId = (config as { mutationId?: number } | undefined)?.mutationId;
    const addLesion = useAddLesion();
    const deleteLesion = useDeleteLesion();
    const [expanded, setExpanded] = React.useState<Set<number>>(new Set());

    const view = viewConfigFrom(config);
    const lesionSchema = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'lesion-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/lesions/form-schema'),
        staleTime: Infinity,
        enabled: view.readonly,
    });
    if (view.readonly) {
        if (lesions.length === 0) {
            return <p className='text-muted small mb-0'>No lesions.</p>;
        }
        if (lesionSchema.isLoading) {
            return <p className='text-muted small'>Loading lesion details…</p>;
        }
        if (lesionSchema.isError || !lesionSchema.data) {
            return <div className='alert alert-warning'>Failed to load lesion form schema.</div>;
        }
        const outerCfg = (config ?? {}) as {
            lesionFieldStatus?: Record<string, Record<string, unknown>>;
            lesionSectionStatus?: Record<string, Record<string, unknown>>;
        };
        return (
            <div>
                {lesions.map((l, i) => (
                    <LesionDetailCard
                        key={l.id}
                        summary={l}
                        n={i + 1}
                        schema={lesionSchema.data.schema}
                        uiSchema={lesionSchema.data.uiSchema}
                        fieldStatus={outerCfg.lesionFieldStatus?.[String(l.id)] ?? {}}
                        sectionStatus={outerCfg.lesionSectionStatus?.[String(l.id)] ?? {}}
                    />
                ))}
            </div>
        );
    }

    const maxItems = (schema as { maxItems?: number } | undefined)?.maxItems;
    const atCapacity = maxItems != null && lesions.length >= maxItems;
    const capTitle = atCapacity
        ? `Maximum ${maxItems} lesions per mutation.`
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
        addLesion.mutate(mutationId, {
            onSuccess: (mutation) => {
                const newest = (mutation.lesions ?? []).reduce<LesionSummaryDTO | null>(
                    (best, l) => (best == null || l.id > best.id ? l : best),
                    null,
                );
                if (newest) {
                    setExpanded((prev) => new Set(prev).add(newest.id));
                }
            },
        });
    };

    const handleDelete = (lesionId: number) => {
        if (!mutationId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this lesion? This action cannot be undone.')) {return;}
        deleteLesion.mutate({ mutationId, lesionId });
    };

    if (lesions.length === 0) {
        return (
            <div>
                <p className='text-muted'>No lesions recorded for this mutation.</p>
                <button
                    type='button'
                    className='btn btn-sm btn-outline-secondary'
                    onClick={handleAdd}
                    disabled={!mutationId || addLesion.isPending || atCapacity}
                    title={capTitle}
                >
                    + Add lesion
                </button>
            </div>
        );
    }

    return (
        <div>
            <ul className='list-unstyled'>
                {lesions.map((l) => {
                    const isOpen = expanded.has(l.id);
                    const typeDisplay = l.lesionType ? humanize(l.lesionType) : null;
                    return (
                        <li key={l.id} className='border rounded p-2 mb-2'>
                            <div className='d-flex justify-content-between align-items-center'>
                                <span>
                                    <strong>Lesion {l.sortOrder ?? ''}</strong>
                                    {typeDisplay && (
                                        <span className='ml-2 text-muted'>{typeDisplay}</span>
                                    )}
                                </span>
                                <div>
                                    <button
                                        type='button'
                                        className='btn btn-sm btn-outline-secondary mr-1'
                                        onClick={() => toggle(l.id)}
                                        aria-expanded={isOpen}
                                    >
                                        {isOpen ? 'Done' : 'Edit'}
                                    </button>
                                    <button
                                        type='button'
                                        className='btn btn-sm btn-outline-danger'
                                        onClick={() => handleDelete(l.id)}
                                        disabled={deleteLesion.isPending}
                                    >
                                        Remove
                                    </button>
                                </div>
                            </div>
                            {isOpen && (
                                <LesionEdit lesionId={l.id} mutationId={mutationId} />
                            )}
                        </li>
                    );
                })}
            </ul>
            <button
                type='button'
                className='btn btn-sm btn-outline-secondary'
                onClick={handleAdd}
                disabled={!mutationId || addLesion.isPending || atCapacity}
                title={capTitle}
            >
                + Add lesion
            </button>
        </div>
    );
}

export const lesionsListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'lesionsList'))),
    renderer: withJsonFormsControlProps(LesionsListRenderer),
};
