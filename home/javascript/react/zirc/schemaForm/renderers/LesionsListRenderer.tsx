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
import { LesionSummaryDTO } from '../../api/types';
import { useAddLesion, useDeleteLesion } from '../../api/queries';
import { LesionEdit } from '../../pages/LesionEdit';

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
                    const label = l.lesionType || `Lesion #${l.sortOrder}`;
                    return (
                        <li key={l.id} className='border rounded p-2 mb-2'>
                            <div className='d-flex justify-content-between align-items-center'>
                                <button
                                    type='button'
                                    className='btn btn-link p-0 text-left'
                                    onClick={() => toggle(l.id)}
                                    aria-expanded={isOpen}
                                >
                                    <span className='mr-2'>{isOpen ? '▾' : '▸'}</span>
                                    <strong>{label}</strong>
                                </button>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => handleDelete(l.id)}
                                    disabled={deleteLesion.isPending}
                                >
                                    Delete
                                </button>
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
