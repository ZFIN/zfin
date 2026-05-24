import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { MutationDTO } from '../../api/types';
import { useAddMutation, useDeleteMutation } from '../../api/queries';
import { viewConfigFrom } from '../useViewConfig';

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

    if (view.readonly) {
        // The full mutation card layout (nested per-mutation SchemaForm with
        // mutation-aggregate field/section status) is a follow-up. Stub the
        // section so the rest of the detail page renders cleanly.
        if (mutations.length === 0) {
            return <p className='text-muted'>No mutations recorded for this submission.</p>;
        }
        return (
            <ul className='list-unstyled'>
                {mutations.map((m, i) => (
                    <li key={m.id} className='mb-1'>
                        <strong>Mutation {i + 1}:</strong>{' '}
                        {m.alleleDesignation ?? <span className='text-muted'>(no allele designation)</span>}
                    </li>
                ))}
                <li className='small text-muted mt-2'>
                    Detailed per-mutation read-only view not yet ported.
                </li>
            </ul>
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

    if (mutations.length === 0) {
        return (
            <div>
                <p className='text-muted'>No mutations recorded for this submission.</p>
                <button
                    type='button'
                    className='btn btn-sm btn-outline-secondary'
                    onClick={handleAdd}
                    disabled={!submissionId || addMutation.isPending || atCapacity}
                    title={capTitle}
                >
                    + Add mutation
                </button>
            </div>
        );
    }

    return (
        <div>
            <ul className='list-unstyled'>
                {mutations.map((m) => (
                    <li key={m.id} className='border rounded p-2 mb-2 d-flex justify-content-between align-items-center'>
                        <div>
                            <strong>{m.alleleDesignation || `Mutation #${m.sortOrder}`}</strong>
                            {m.mutationType && (
                                <span className='text-muted small ml-2'>{m.mutationType}</span>
                            )}
                        </div>
                        <div>
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
                                Delete
                            </button>
                        </div>
                    </li>
                ))}
            </ul>
            <button
                type='button'
                className='btn btn-sm btn-outline-secondary'
                onClick={handleAdd}
                disabled={!submissionId || addMutation.isPending || atCapacity}
                title={capTitle}
            >
                + Add mutation
            </button>
        </div>
    );
}

export const mutationsListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'mutationsList'))),
    renderer: withJsonFormsControlProps(MutationsListRenderer),
};
