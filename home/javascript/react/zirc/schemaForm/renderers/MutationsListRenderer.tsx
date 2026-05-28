import * as React from 'react';
import { useQuery } from '@tanstack/react-query';
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
import { MutationDTO } from '../../api/types';
import { api } from '../../api/client';
import { useAddMutation, useDeleteMutation } from '../../api/queries';
import { viewConfigFrom } from '../useViewConfig';
import { aggregateRenderers } from '../aggregateRenderers';
import { FieldStatus, StatusBadge } from '../../components/StatusBadge';

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

/**
 * Renders the read-only summary of mutations on the submission page.
 *
 * Each row is a card with the mutation's summary fields + Edit and Delete
 * actions. Add/Delete go through dedicated REST endpoints (not the field-
 * path PATCH), so SchemaForm filters /mutations out of its leaf diff —
 * changes here flow through the React Query cache invalidation triggered
 * by the mutations.
 *
 * The submission id needed for the endpoints comes through JsonForms'
 * `config` prop, which SchemaForm threads through.
 */
function MutationsListRenderer({ data, schema, config }: ControlProps) {
    const mutations = (data as MutationDTO[] | undefined) ?? [];
    const submissionId = (config as { submissionId?: string } | undefined)?.submissionId;
    const addMutation = useAddMutation();
    const deleteMutation = useDeleteMutation();
    const view = viewConfigFrom(config);
    // Per-aggregate status maps come through the outer SchemaForm's config,
    // each keyed by entity id (as string — JSON object keys are always
    // strings). We pass them through unchanged into the inner mutation
    // config so the sibling list renderers (genes, lesions, assays,
    // phenotypes) inside the nested JsonForms can look up their own
    // entity's map.
    const outerCfg = (config ?? {}) as {
        mutationFieldStatus?: Record<string, Record<string, unknown>>;
        mutationSectionStatus?: Record<string, Record<string, unknown>>;
        mutationOverallStatus?: Record<string, FieldStatus>;
        geneFieldStatus?: Record<string, Record<string, unknown>>;
        geneSectionStatus?: Record<string, Record<string, unknown>>;
        lesionFieldStatus?: Record<string, Record<string, unknown>>;
        lesionSectionStatus?: Record<string, Record<string, unknown>>;
        assayFieldStatus?: Record<string, Record<string, unknown>>;
        assaySectionStatus?: Record<string, Record<string, unknown>>;
        phenotypeFieldStatus?: Record<string, Record<string, unknown>>;
        phenotypeSectionStatus?: Record<string, Record<string, unknown>>;
    };

    // Mutation form-schema is shared across all rows; fetch once, cache forever.
    // The hook must be invoked unconditionally to satisfy the rules of hooks,
    // even though we only use the data in the readonly branch.
    const mutationSchema = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'mutation-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/mutations/form-schema'),
        staleTime: Infinity,
    });

    if (view.readonly) {
        if (mutations.length === 0) {
            return <p className='text-muted'>No mutations recorded for this submission.</p>;
        }
        if (mutationSchema.isLoading) {
            return <p className='text-muted'>Loading mutation details…</p>;
        }
        if (mutationSchema.isError || !mutationSchema.data) {
            return (
                <div className='alert alert-warning'>
                    Failed to load mutation form schema.
                </div>
            );
        }
        return (
            <div>
                {mutations.map((m, i) => (
                    <div
                        key={m.id}
                        id={`mutation-${i + 1}`}
                        className='card mb-3'
                    >
                        <div className='card-header py-2'>
                            <StatusBadge status={outerCfg.mutationOverallStatus?.[String(m.id)]}/>
                            <strong>Mutation {i + 1}</strong>
                            {m.alleleDesignation && (
                                <span className='ml-2 text-muted'>
                                    — {m.alleleDesignation}
                                </span>
                            )}
                        </div>
                        <div className='card-body py-2'>
                            <JsonForms
                                schema={mutationSchema.data.schema}
                                uischema={mutationSchema.data.uiSchema}
                                data={m}
                                renderers={aggregateRenderers}
                                cells={[]}
                                readonly
                                config={{
                                    mode: 'view',
                                    readonly: true,
                                    mutationId: m.id,
                                    idPrefix: `mutation-${i + 1}`,
                                    fieldStatus:
                                        outerCfg.mutationFieldStatus?.[String(m.id)] ?? {},
                                    sectionStatus:
                                        outerCfg.mutationSectionStatus?.[String(m.id)] ?? {},
                                    recId: `ZIRC-MUT-${m.id}`,
                                    // Pass child maps unchanged; the inner
                                    // list renderers index them by their own
                                    // entity id.
                                    geneFieldStatus:        outerCfg.geneFieldStatus        ?? {},
                                    geneSectionStatus:      outerCfg.geneSectionStatus      ?? {},
                                    lesionFieldStatus:      outerCfg.lesionFieldStatus      ?? {},
                                    lesionSectionStatus:    outerCfg.lesionSectionStatus    ?? {},
                                    assayFieldStatus:       outerCfg.assayFieldStatus       ?? {},
                                    assaySectionStatus:     outerCfg.assaySectionStatus     ?? {},
                                    phenotypeFieldStatus:   outerCfg.phenotypeFieldStatus   ?? {},
                                    phenotypeSectionStatus: outerCfg.phenotypeSectionStatus ?? {},
                                }}
                                onChange={() => { /* read-only */ }}
                            />
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    // JSON Schema maxItems on the array — server publishes the curator-spec
    // cap (MAX_MUTATIONS_PER_SUBMISSION); we honor it client-side so the
    // "+ Add" button is disabled once the curator hits the limit.
    const maxItems = (schema as { maxItems?: number } | undefined)?.maxItems;
    const atCapacity = maxItems != null && mutations.length >= maxItems;
    const capTitle = atCapacity
        ? `Maximum ${maxItems} mutations per submission.`
        : undefined;

    const handleAdd = () => {
        if (!submissionId) {return;}
        addMutation.mutate(submissionId);
    };

    const handleDelete = (mutationId: number) => {
        if (!submissionId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this mutation? This action cannot be undone.')) {return;}
        deleteMutation.mutate({ submissionId, mutationId });
    };

    const addButton = (
        <button
            type='button'
            className='btn btn-sm btn-outline-secondary'
            onClick={handleAdd}
            disabled={!submissionId || addMutation.isPending || atCapacity}
            title={capTitle}
        >
            + Add mutation
        </button>
    );

    if (mutations.length === 0) {
        return (
            <div>
                <p className='text-muted'>No mutations recorded for this submission.</p>
                {addButton}
            </div>
        );
    }

    return (
        <div>
            <table className='table table-sm'>
                <thead>
                    <tr>
                        <th style={{ width: '3rem' }}>#</th>
                        <th>Allele Designation</th>
                        <th>Mutagenesis Protocol</th>
                        <th>Mutation Type</th>
                        <th>Discoverer</th>
                        <th className='text-right' style={{ width: '8rem' }}>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {mutations.map((m, i) => (
                        <tr key={m.id}>
                            <td>{i + 1}</td>
                            <td>{m.alleleDesignation || <span className='text-muted'>—</span>}</td>
                            <td>{m.mutagenesisProtocol || <span className='text-muted'>—</span>}</td>
                            <td>{m.mutationType || <span className='text-muted'>—</span>}</td>
                            <td>{m.mutationDiscoverer || <span className='text-muted'>—</span>}</td>
                            <td className='text-right text-nowrap'>
                                <a
                                    className='btn btn-sm btn-outline-secondary mr-1'
                                    href={`/action/zirc/mutation/${m.id}/edit`}
                                >
                                    Edit
                                </a>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => handleDelete(m.id)}
                                    disabled={deleteMutation.isPending}
                                >
                                    Remove
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            {addButton}
        </div>
    );
}

export const mutationsListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'mutationsList'))),
    renderer: withJsonFormsControlProps(MutationsListRenderer),
};
