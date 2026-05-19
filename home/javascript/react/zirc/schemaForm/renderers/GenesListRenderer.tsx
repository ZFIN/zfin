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
import { GeneDTO } from '../../api/types';
import { useAddGene, useDeleteGene } from '../../api/queries';
import { GeneEdit } from '../../pages/GeneEdit';

/**
 * Per-mutation genes list with inline-expand cards. Same shape as
 * AssaysListRenderer — collapsed card header shows the marker
 * abbreviation; expanded card mounts {@link GeneEdit}.
 *
 * <p>{@code mutationId} arrives via JsonForms' {@code config} prop,
 * threaded by MutationEdit. {@code schema.maxItems} drives the
 * "+ Add gene" disabled-at-capacity state.
 */
function GenesListRenderer({ data, schema, config }: ControlProps) {
    const genes = (data as GeneDTO[] | undefined) ?? [];
    const mutationId = (config as { mutationId?: number } | undefined)?.mutationId;
    const addGene = useAddGene();
    const deleteGene = useDeleteGene();
    const [expanded, setExpanded] = React.useState<Set<number>>(new Set());

    const maxItems = (schema as { maxItems?: number } | undefined)?.maxItems;
    const atCapacity = maxItems != null && genes.length >= maxItems;
    const capTitle = atCapacity
        ? `Maximum ${maxItems} genes per mutation.`
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
        addGene.mutate(mutationId, {
            onSuccess: (mutation) => {
                const newest = (mutation.genes ?? []).reduce<GeneDTO | null>(
                    (best, g) => (best == null || g.id > best.id ? g : best),
                    null,
                );
                if (newest) {
                    setExpanded((prev) => new Set(prev).add(newest.id));
                }
            },
        });
    };

    const handleDelete = (geneId: number) => {
        if (!mutationId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this gene? This action cannot be undone.')) {return;}
        deleteGene.mutate({ mutationId, geneId });
    };

    if (genes.length === 0) {
        return (
            <div>
                <p className='text-muted'>No genes recorded for this mutation.</p>
                <button
                    type='button'
                    className='btn btn-sm btn-outline-secondary'
                    onClick={handleAdd}
                    disabled={!mutationId || addGene.isPending || atCapacity}
                    title={capTitle}
                >
                    + Add gene
                </button>
            </div>
        );
    }

    return (
        <div>
            <ul className='list-unstyled'>
                {genes.map((g) => {
                    const isOpen = expanded.has(g.id);
                    const label = g.mutatedGeneAbbreviation
                        || g.mutatedGeneZdbID
                        || `Gene #${g.sortOrder}`;
                    return (
                        <li key={g.id} className='border rounded p-2 mb-2'>
                            <div className='d-flex justify-content-between align-items-center'>
                                <button
                                    type='button'
                                    className='btn btn-link p-0 text-left'
                                    onClick={() => toggle(g.id)}
                                    aria-expanded={isOpen}
                                >
                                    <span className='mr-2'>{isOpen ? '▾' : '▸'}</span>
                                    <strong>{label}</strong>
                                </button>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => handleDelete(g.id)}
                                    disabled={deleteGene.isPending}
                                >
                                    Delete
                                </button>
                            </div>
                            {isOpen && (
                                <GeneEdit geneId={g.id} mutationId={mutationId} />
                            )}
                        </li>
                    );
                })}
            </ul>
            <button
                type='button'
                className='btn btn-sm btn-outline-secondary'
                onClick={handleAdd}
                disabled={!mutationId || addGene.isPending || atCapacity}
                title={capTitle}
            >
                + Add gene
            </button>
        </div>
    );
}

export const genesListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'genesList'))),
    renderer: withJsonFormsControlProps(GenesListRenderer),
};
