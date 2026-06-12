import * as React from 'react';
import { useQueryClient, UseQueryResult } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import { FormFor } from '../api/formHelpers';
import { useAutosavedSchemaForm } from './useAutosavedSchemaForm';
import { SaveStatusBadge } from '../components/SaveStatusBadge';
import { AGGREGATES, AggregateKind } from './aggregateRegistry';
import { sectionRendererEntry } from './renderers/SectionRenderer';
import { rowControlRendererEntry } from './renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from './renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from './renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from './renderers/YesNoRadioRenderer';
import { checkboxRendererEntry } from './renderers/CheckboxRenderer';
import { selectWithOtherRendererEntry } from './renderers/SelectWithOtherRenderer';
import { singleSelectRendererEntry } from './renderers/SingleSelectRenderer';
import { publicationsListRendererEntry } from './renderers/PublicationsListRenderer';
import { autocompleteRendererEntry } from './renderers/AutocompleteRenderer';
import { attachmentsRendererEntry } from './renderers/AttachmentsRenderer';
import { phenotypeTimingRendererEntry } from './renderers/PhenotypeTimingRenderer';

/**
 * One schema-driven editor for any per-mutation aggregate (assay, gene,
 * lesion, phenotype). Replaces the four near-identical per-aggregate
 * editor components.
 *
 * <p>It registers the union of every renderer the inline forms use;
 * each aggregate's uiSchema picks the ones it needs. The autosave
 * behavior (skip-set, mirror-sync, parent-refresh) comes from the
 * uiSchema's per-Control flags via {@link useAutosavedSchemaForm}. The
 * one non-schema fact — endpoints, cache keys, the parent relationship —
 * lives in {@link AGGREGATES}.
 *
 * <p>Callers pass the already-fetched entity query (each aggregate has
 * its own typed React Query hook, which can't be called through a
 * variable without tripping the rules-of-hooks lint), the kind, the id,
 * and the parent id.
 */

// Union of renderers across the four inline forms. JsonForms only fires
// the ones a given uiSchema's Controls reference.
const renderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    yesNoRadioRendererEntry,
    checkboxRendererEntry,
    selectWithOtherRendererEntry,
    singleSelectRendererEntry,
    publicationsListRendererEntry,
    autocompleteRendererEntry,
    attachmentsRendererEntry,
    phenotypeTimingRendererEntry,
];

export type ZircEntityEditorProps<TDto extends object> = {
    kind: AggregateKind;
    entityId: number;
    /** Parent mutation id — used for the parent-refresh invalidation and
     *  exposed to renderers via config. Optional (e.g. not yet linked). */
    parentId?: number;
    /** The aggregate's GET-by-id query, called by the thin wrapper. */
    entityQuery: UseQueryResult<TDto>;
};

export function ZircEntityEditor<TDto extends object>({
    kind,
    entityId,
    parentId,
    entityQuery,
}: ZircEntityEditorProps<TDto>) {
    const cfg = AGGREGATES[kind];
    const qc = useQueryClient();

    const onRefreshParent = React.useCallback(() => {
        qc.invalidateQueries({ queryKey: cfg.selfCacheKey(entityId) });
        if (parentId != null) {
            qc.invalidateQueries({ queryKey: cfg.parentCacheKey(parentId) });
        }
    }, [qc, cfg, entityId, parentId]);

    const { formData, setFormData, status, errorMessage, schemaQuery } =
        useAutosavedSchemaForm<TDto>({
            entity: entityQuery.data,
            entityId,
            schemaQueryKey: cfg.schemaQueryKey,
            schemaEndpoint: cfg.schemaEndpoint,
            patchEndpointFor: cfg.patchEndpointFor,
            onRefreshParent,
        });

    const config: Record<string, unknown> = {
        [cfg.idConfigKey]: entityId,
        [cfg.parentConfigKey]: parentId,
    };

    if (schemaQuery.isLoading || entityQuery.isLoading || formData == null) {
        return <p className='text-muted'>Loading {kind}…</p>;
    }
    if (schemaQuery.isError || entityQuery.isError || !schemaQuery.data || !entityQuery.data) {
        return <div className='alert alert-danger'>Failed to load {kind} form.</div>;
    }

    return (
        <div className='mt-2'>
            <div className='d-flex justify-content-end mb-1'>
                <SaveStatusBadge status={status} message={errorMessage} />
            </div>
            <JsonForms
                schema={schemaQuery.data.schema}
                uischema={schemaQuery.data.uiSchema}
                data={formData}
                renderers={renderers}
                cells={[]}
                config={config}
                onChange={({ data }) => setFormData(data as FormFor<TDto>)}
            />
        </div>
    );
}
