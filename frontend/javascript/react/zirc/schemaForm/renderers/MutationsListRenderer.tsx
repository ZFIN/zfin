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
import { FieldHistory } from '../../components/FieldHistory';
import { MutationEdit } from '../../pages/MutationEdit';

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

/**
 * How a mutation's allele is labelled wherever it is named as a whole
 * (ZFIN-10488): the symbol with the ZDB-ALT ID in parentheses.
 *
 *   b700 (ZDB-ALT-040701-2)
 *
 * Note which field holds which, because the names invite the opposite
 * reading: alleleDesignation holds the ZDB-ALT ID once a curator has picked an
 * existing ZFIN feature, and alleleName is the abbreviation the server
 * resolved for it. Showing only the designation made the table and the card
 * header say less than the General section directly below them, which already
 * renders the symbol via displayLabels.
 *
 * Falls back to the bare designation when nothing was resolved -- a submission
 * whose allele is not yet a ZFIN record keeps a free-text symbol there, and
 * "b700 (b700)" would be worse than "b700".
 */
export function alleleLabel(m: MutationDTO): string | null {
    if (!m.alleleDesignation) {return null;}
    return m.alleleName ? `${m.alleleName} (${m.alleleDesignation})` : m.alleleDesignation;
}

/**
 * Renders the mutations section of the submission page.
 *
 * In edit mode each mutation is a collapsible card (ZFIN-10543): the header
 * shows its status, allele, protocol and type with Expand and Remove
 * actions, and expanding it mounts the full mutation editor in place, so
 * the whole submission is edited on one page. Add/Delete go through
 * dedicated REST endpoints (not the field-path PATCH), so SchemaForm filters
 * /mutations out of its leaf diff — changes here flow through the React
 * Query cache invalidation triggered by the mutations.
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

    // Ids of the mutation cards whose editor is open. A lone mutation starts
    // open; with several, only the one a #mutation-N hash points at does.
    const [expanded, setExpanded] = React.useState<Set<number>>(() => {
        const open = new Set<number>();
        if (mutations.length === 1) {open.add(mutations[0].id);}
        const pos = mutationPositionFromHash(window.location.hash);
        if (pos != null && mutations[pos - 1]) {open.add(mutations[pos - 1].id);}
        return open;
    });

    // Left-nav links (#mutation-2, #mutation-2-general) point into cards that
    // may be collapsed: open the card, then scroll to the target once it has
    // rendered. Runs for the hash the page loaded with and for each change.
    const mutationsRef = React.useRef(mutations);
    mutationsRef.current = mutations;
    React.useEffect(() => {
        if (view.readonly) {return undefined;}
        const openFromHash = () => {
            const hash = window.location.hash;
            const pos = mutationPositionFromHash(hash);
            const target = pos != null ? mutationsRef.current[pos - 1] : undefined;
            if (!target) {return;}
            setExpanded((prev) => (prev.has(target.id) ? prev : new Set(prev).add(target.id)));
            scrollToWhenPresent(hash.slice(1));
        };
        openFromHash();
        window.addEventListener('hashchange', openFromHash);
        return () => window.removeEventListener('hashchange', openFromHash);
    }, [view.readonly]);

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
                            {alleleLabel(m) && (
                                <span className='ml-2 text-muted'>
                                    — {alleleLabel(m)}
                                </span>
                            )}
                            <FieldHistory
                                recId={`ZIRC-MUT-${m.id}`}
                                scope='entity'
                                label={`Mutation ${i + 1}`}
                            />
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
                                    // Server-resolved abbreviation when the
                                    // curator picked an existing ZFIN feature
                                    // for alleleInZfin=true. AutocompleteRenderer
                                    // uses this to show the name instead of
                                    // the raw ZDB-ID and links to /{zdbID}.
                                    displayLabels: m.alleleName
                                        ? { alleleDesignation: m.alleleName }
                                        : {},
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
        // A new mutation opens expanded, ready to fill in.
        addMutation.mutate(submissionId, {
            onSuccess: (created) => setExpanded((prev) => new Set(prev).add(created.id)),
        });
    };

    const handleDelete = (mutationId: number) => {
        if (!submissionId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this mutation? This action cannot be undone.')) {return;}
        deleteMutation.mutate({ submissionId, mutationId });
    };

    const toggle = (id: number) => {
        setExpanded((prev) => {
            const next = new Set(prev);
            if (next.has(id)) {next.delete(id);} else {next.add(id);}
            return next;
        });
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
            {mutations.length > 1 && (
                <div className='d-flex justify-content-end mb-2'>
                    <button
                        type='button'
                        className='btn btn-sm btn-link'
                        onClick={() => setExpanded(new Set(mutations.map((m) => m.id)))}
                    >
                        Expand all
                    </button>
                    <button
                        type='button'
                        className='btn btn-sm btn-link'
                        onClick={() => setExpanded(new Set())}
                    >
                        Collapse all
                    </button>
                </div>
            )}
            {mutations.map((m, i) => {
                const isOpen = expanded.has(m.id);
                const summary = [m.mutagenesisProtocol, m.mutationType].filter(Boolean).join(' · ');
                return (
                    <div key={m.id} id={`mutation-${i + 1}`} className='card mb-3'>
                        <div className='card-header py-2 d-flex justify-content-between align-items-center'>
                            <span>
                                <button
                                    type='button'
                                    className='btn btn-link p-0 mr-2 text-body'
                                    onClick={() => toggle(m.id)}
                                    aria-expanded={isOpen}
                                    aria-controls={`mutation-${i + 1}-body`}
                                    aria-label={`${isOpen ? 'Collapse' : 'Expand'} mutation ${i + 1}`}
                                >
                                    <i className={`fas fa-fw fa-caret-${isOpen ? 'down' : 'right'}`}/>
                                </button>
                                <StatusBadge status={outerCfg.mutationOverallStatus?.[String(m.id)]}/>
                                <strong>Mutation {i + 1}</strong>
                                {alleleLabel(m) && (
                                    <span className='ml-2 text-muted'>— {alleleLabel(m)}</span>
                                )}
                                {summary && <span className='ml-2 small text-muted'>{summary}</span>}
                                <FieldHistory
                                    recId={`ZIRC-MUT-${m.id}`}
                                    scope='entity'
                                    label={`Mutation ${i + 1}`}
                                />
                            </span>
                            <span className='text-nowrap'>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-secondary mr-1'
                                    onClick={() => toggle(m.id)}
                                    aria-expanded={isOpen}
                                >
                                    {isOpen ? 'Collapse' : 'Expand'}
                                </button>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => handleDelete(m.id)}
                                    disabled={deleteMutation.isPending}
                                >
                                    Remove
                                </button>
                            </span>
                        </div>
                        {isOpen && (
                            <div className='card-body py-2' id={`mutation-${i + 1}-body`}>
                                <MutationEdit
                                    mutationId={m.id}
                                    submissionId={submissionId}
                                    position={i + 1}
                                />
                            </div>
                        )}
                    </div>
                );
            })}
            {addButton}
        </div>
    );
}

/**
 * The 1-based mutation position a location hash points into: #mutation-2 (the
 * card, from the left nav) or #mutation-2-general (one of its sections).
 */
export function mutationPositionFromHash(hash: string): number | null {
    const m = /^#mutation-(\d+)(?:-|$)/.exec(hash);
    return m ? Number(m[1]) : null;
}

// The card's sections render only after its editor has fetched the mutation,
// so a hash target inside a just-expanded card does not exist yet. Look for it
// for a few seconds and scroll once it appears.
function scrollToWhenPresent(id: string) {
    let tries = 0;
    const tick = () => {
        const el = document.getElementById(id);
        if (el) {
            el.scrollIntoView();
            return;
        }
        if (++tries < 50) {window.setTimeout(tick, 100);}
    };
    tick();
}

export const mutationsListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'mutationsList'))),
    renderer: withJsonFormsControlProps(MutationsListRenderer),
};
