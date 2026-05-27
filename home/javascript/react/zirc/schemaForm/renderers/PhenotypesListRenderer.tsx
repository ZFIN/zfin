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
import { PhenotypeSummaryDTO } from '../../api/types';
import { api } from '../../api/client';
import { useAddPhenotype, useDeletePhenotype, usePhenotypeById } from '../../api/queries';
import { PhenotypeEdit } from '../../pages/PhenotypeEdit';
import { viewConfigFrom } from '../useViewConfig';
import { aggregateRenderers } from '../aggregateRenderers';

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

function PhenotypeDetailCard({
    summary, n, schema, uiSchema, fieldStatus, sectionStatus,
}: {
    summary: PhenotypeSummaryDTO;
    n: number;
    schema: JsonSchema;
    uiSchema: UISchemaElement;
    fieldStatus: Record<string, unknown>;
    sectionStatus: Record<string, unknown>;
}) {
    const q = usePhenotypeById(summary.id);
    return (
        <div className='card mb-2'>
            <div className='card-header py-1'>
                <strong>{summary.description || `Phenotype ${n}`}</strong>
            </div>
            <div className='card-body py-2'>
                {q.isLoading && <p className='text-muted small mb-0'>Loading…</p>}
                {q.isError && (
                    <div className='alert alert-warning mb-0'>
                        Failed to load phenotype {summary.id}.
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
                            phenotypeId: summary.id,
                            recId: `ZIRC-PHEN-${summary.id}`,
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
 * Per-mutation phenotypes list with inline-expand cards. Same shape as
 * the lesion/gene/assay list renderers; collapsed card header shows
 * the description snippet, expanded card mounts {@link PhenotypeEdit}.
 */
function PhenotypesListRenderer({ data, schema, config }: ControlProps) {
    const phenotypes = (data as PhenotypeSummaryDTO[] | undefined) ?? [];
    const mutationId = (config as { mutationId?: number } | undefined)?.mutationId;
    const addPhenotype = useAddPhenotype();
    const deletePhenotype = useDeletePhenotype();
    const [expanded, setExpanded] = React.useState<Set<number>>(new Set());

    const view = viewConfigFrom(config);
    const phenotypeSchema = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'phenotype-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/phenotypes/form-schema'),
        staleTime: Infinity,
        enabled: view.readonly,
    });
    if (view.readonly) {
        if (phenotypes.length === 0) {
            return <p className='text-muted small mb-0'>No phenotypes.</p>;
        }
        if (phenotypeSchema.isLoading) {
            return <p className='text-muted small'>Loading phenotype details…</p>;
        }
        if (phenotypeSchema.isError || !phenotypeSchema.data) {
            return <div className='alert alert-warning'>Failed to load phenotype form schema.</div>;
        }
        const outerCfg = (config ?? {}) as {
            phenotypeFieldStatus?: Record<string, Record<string, unknown>>;
            phenotypeSectionStatus?: Record<string, Record<string, unknown>>;
        };
        return (
            <div>
                {phenotypes.map((p, i) => (
                    <PhenotypeDetailCard
                        key={p.id}
                        summary={p}
                        n={i + 1}
                        schema={phenotypeSchema.data.schema}
                        uiSchema={phenotypeSchema.data.uiSchema}
                        fieldStatus={outerCfg.phenotypeFieldStatus?.[String(p.id)] ?? {}}
                        sectionStatus={outerCfg.phenotypeSectionStatus?.[String(p.id)] ?? {}}
                    />
                ))}
            </div>
        );
    }

    const maxItems = (schema as { maxItems?: number } | undefined)?.maxItems;
    const atCapacity = maxItems != null && phenotypes.length >= maxItems;
    const capTitle = atCapacity
        ? `Maximum ${maxItems} phenotypes per mutation.`
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
        addPhenotype.mutate(mutationId, {
            onSuccess: (mutation) => {
                const newest = (mutation.phenotypes ?? []).reduce<PhenotypeSummaryDTO | null>(
                    (best, p) => (best == null || p.id > best.id ? p : best),
                    null,
                );
                if (newest) {
                    setExpanded((prev) => new Set(prev).add(newest.id));
                }
            },
        });
    };

    const handleDelete = (phenotypeId: number) => {
        if (!mutationId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this phenotype? This action cannot be undone.')) {return;}
        deletePhenotype.mutate({ mutationId, phenotypeId });
    };

    if (phenotypes.length === 0) {
        return (
            <div>
                <p className='text-muted'>No phenotypes recorded for this mutation.</p>
                <button
                    type='button'
                    className='btn btn-sm btn-outline-secondary'
                    onClick={handleAdd}
                    disabled={!mutationId || addPhenotype.isPending || atCapacity}
                    title={capTitle}
                >
                    + Add phenotype
                </button>
            </div>
        );
    }

    return (
        <div>
            <ul className='list-unstyled'>
                {phenotypes.map((p) => {
                    const isOpen = expanded.has(p.id);
                    const snippet = (p.description ?? '').trim();
                    const label = snippet.length === 0
                        ? `Phenotype #${p.sortOrder}`
                        : snippet.length > 80 ? snippet.slice(0, 80) + '…' : snippet;
                    return (
                        <li key={p.id} className='border rounded p-2 mb-2'>
                            <div className='d-flex justify-content-between align-items-center'>
                                <button
                                    type='button'
                                    className='btn btn-link p-0 text-left'
                                    onClick={() => toggle(p.id)}
                                    aria-expanded={isOpen}
                                >
                                    <span className='mr-2'>{isOpen ? '▾' : '▸'}</span>
                                    <strong>{label}</strong>
                                </button>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => handleDelete(p.id)}
                                    disabled={deletePhenotype.isPending}
                                >
                                    Delete
                                </button>
                            </div>
                            {isOpen && (
                                <PhenotypeEdit phenotypeId={p.id} mutationId={mutationId} />
                            )}
                        </li>
                    );
                })}
            </ul>
            <button
                type='button'
                className='btn btn-sm btn-outline-secondary'
                onClick={handleAdd}
                disabled={!mutationId || addPhenotype.isPending || atCapacity}
                title={capTitle}
            >
                + Add phenotype
            </button>
        </div>
    );
}

export const phenotypesListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'phenotypesList'))),
    renderer: withJsonFormsControlProps(PhenotypesListRenderer),
};
