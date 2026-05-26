import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import type { JsonSchema } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { useQuery } from '@tanstack/react-query';
import { GeneDTO } from '../../api/types';
import { api } from '../../api/client';
import { useAddGene, useDeleteGene } from '../../api/queries';
import { GeneEdit } from '../../pages/GeneEdit';
import { viewConfigFrom } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldComments } from '../../components/FieldComments';
import { STATUS_COMPLETE, STATUS_MISSING } from '../deriveStatus';

type FormSchemaDTO = { schema: JsonSchema; uiSchema: unknown };

/**
 * Per-cell helper that defers to the shared MISSING/COMPLETE constants.
 * The gene table renders four cells per row, each needing a tiny badge —
 * inlining the empty check keeps the JSX readable.
 */
function deriveStatus(value: unknown, fieldName: string, requiredSet: Set<string>) {
    const empty = value == null || (typeof value === 'string' && value.trim() === '');
    return empty && requiredSet.has(fieldName) ? STATUS_MISSING : STATUS_COMPLETE;
}

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
    // Fetch the gene schema once (cached) so the required-set in the table's
    // per-cell badges stays in lockstep with ZircGeneFormSchema's declaration.
    const geneSchema = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', 'gene-form-schema'],
        queryFn: () => api.get<FormSchemaDTO>('/genes/form-schema'),
        staleTime: Infinity,
        enabled: view.readonly,
    });
    const requiredSet = React.useMemo<Set<string>>(() => {
        const r = (geneSchema.data?.schema as { required?: string[] } | undefined)?.required;
        return new Set(r ?? []);
    }, [geneSchema.data]);

    if (view.readonly) {
        if (genes.length === 0) {
            return <p className='text-muted small mb-0'>No genes.</p>;
        }
        // Compact table view: Gene-as-link + the three optional metadata
        // columns. Status badge in each cell is derived from the schema's
        // required-set + value emptiness so all four columns line up
        // regardless of which fields are required.
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
                                    <StatusBadge status={deriveStatus(zdb, 'mutatedGeneZdbID', requiredSet)}/>
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
                                    <StatusBadge status={deriveStatus(g.linkageGroup, 'linkageGroup', requiredSet)}/>
                                    {g.linkageGroup ?? <span className='text-muted'>&mdash;</span>}
                                    <FieldComments
                                        recId={geneRecId}
                                        scope='field'
                                        fieldName='linkageGroup'
                                        label='Linkage Group'
                                    />
                                </td>
                                <td className='pr-3'>
                                    <StatusBadge status={deriveStatus(g.genbankGenomicDna, 'genbankGenomicDna', requiredSet)}/>
                                    {g.genbankGenomicDna ?? <span className='text-muted'>&mdash;</span>}
                                    <FieldComments
                                        recId={geneRecId}
                                        scope='field'
                                        fieldName='genbankGenomicDna'
                                        label='GenBank Genomic DNA'
                                    />
                                </td>
                                <td className='pr-3'>
                                    <StatusBadge status={deriveStatus(g.genbankCdna, 'genbankCdna', requiredSet)}/>
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
