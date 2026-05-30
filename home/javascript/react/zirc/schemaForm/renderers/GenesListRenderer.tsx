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
import { viewConfigFrom } from '../useViewConfig';
import { FieldStatus, StatusBadge } from '../../components/StatusBadge';
import { FieldComments } from '../../components/FieldComments';

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

    const view = viewConfigFrom(config);
    // Server-shipped per-gene field status, keyed by gene id (as string —
    // JSON keys are always strings). Threaded from the bootstrap payload
    // through SchemaForm -> MutationsListRenderer -> this renderer's
    // config. Each cell looks up its (geneId, fieldName) value.
    const outerCfg = (config ?? {}) as {
        geneFieldStatus?: Record<string, Record<string, FieldStatus>>;
    };
    const statusFor = (geneId: number, fieldName: string): FieldStatus | undefined =>
        outerCfg.geneFieldStatus?.[String(geneId)]?.[fieldName];

    if (view.readonly) {
        if (genes.length === 0) {
            return <p className='text-muted small mb-0'>No genes.</p>;
        }
        // Compact table view: Gene-as-link + the three optional metadata
        // columns. Status badge in each cell is shipped per-gene by the
        // server (LineSubmissionDetail bootstrap payload) so all four
        // columns line up regardless of which fields are required.
        return (
            <table className='table table-sm table-borderless mb-0' style={{ width: 'auto' }}>
                <thead>
                    <tr>
                        <th className='pr-3'>Gene</th>
                        <th className='pr-3'>Linkage Group</th>
                        <th className='pr-3'>GenBank Genomic DNA</th>
                        <th className='pr-3'>GenBank cDNA</th>
                    </tr>
                </thead>
                <tbody>
                    {genes.map((g, i) => {
                        const name = g.mutatedGeneAbbreviation
                            || g.mutatedGeneZdbID
                            || `Gene ${i + 1}`;
                        const zdb = g.mutatedGeneZdbID;
                        const geneRecId = `ZIRC-GENE-${g.id}`;
                        return (
                            <tr key={g.id}>
                                <td className='pr-3'>
                                    <StatusBadge status={statusFor(g.id, 'mutatedGeneZdbID')}/>
                                    {zdb
                                        ? (
                                            <a href={`/action/marker/view/${zdb}`}>
                                                {name}
                                            </a>
                                        )
                                        : <span className='text-muted'>{name}</span>}
                                    <FieldComments
                                        recId={geneRecId}
                                        scope='field'
                                        fieldName='mutatedGeneZdbID'
                                        label='Gene'
                                    />
                                </td>
                                <td className='pr-3'>
                                    <StatusBadge status={statusFor(g.id, 'linkageGroup')}/>
                                    {g.linkageGroup ?? <span className='text-muted'>&mdash;</span>}
                                    <FieldComments
                                        recId={geneRecId}
                                        scope='field'
                                        fieldName='linkageGroup'
                                        label='Linkage Group'
                                    />
                                </td>
                                <td className='pr-3'>
                                    <StatusBadge status={statusFor(g.id, 'genbankGenomicDna')}/>
                                    {g.genbankGenomicDna ?? <span className='text-muted'>&mdash;</span>}
                                    <FieldComments
                                        recId={geneRecId}
                                        scope='field'
                                        fieldName='genbankGenomicDna'
                                        label='GenBank Genomic DNA'
                                    />
                                </td>
                                <td className='pr-3'>
                                    <StatusBadge status={statusFor(g.id, 'genbankCdna')}/>
                                    {g.genbankCdna ?? <span className='text-muted'>&mdash;</span>}
                                    <FieldComments
                                        recId={geneRecId}
                                        scope='field'
                                        fieldName='genbankCdna'
                                        label='GenBank cDNA'
                                    />
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        );
    }

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
                    const summary = g.mutatedGeneAbbreviation || g.mutatedGeneZdbID;
                    return (
                        <li key={g.id} className='border rounded p-2 mb-2'>
                            <div className='d-flex justify-content-between align-items-center'>
                                <span>
                                    <strong>Gene {g.sortOrder ?? ''}</strong>
                                    {summary && (
                                        <span className='ml-2 text-muted'>{summary}</span>
                                    )}
                                </span>
                                <div>
                                    <button
                                        type='button'
                                        className='btn btn-sm btn-outline-secondary mr-1'
                                        onClick={() => toggle(g.id)}
                                        aria-expanded={isOpen}
                                    >
                                        {isOpen ? 'Done' : 'Edit'}
                                    </button>
                                    <button
                                        type='button'
                                        className='btn btn-sm btn-outline-danger'
                                        onClick={() => handleDelete(g.id)}
                                        disabled={deleteGene.isPending}
                                    >
                                        Remove
                                    </button>
                                </div>
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
