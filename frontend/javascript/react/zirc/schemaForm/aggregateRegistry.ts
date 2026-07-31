import type { QueryKey } from '@tanstack/react-query';
import { assayKey, geneKey, lesionKey, phenotypeKey, mutationKey } from '../api/queries';

/**
 * Per-aggregate configuration the ZircEntityEditor can't get from the
 * schema: endpoint conventions, React Query cache keys, and the
 * parent-card relationship. This is the one piece of the editor that
 * isn't schema-driven — see the "flagged for future iteration" note in
 * reference/zirc-entity-editor-design.md (a server aggregate-metadata
 * endpoint could eventually generate this).
 */

export type AggregateKind = 'assay' | 'gene' | 'lesion' | 'phenotype';

export type AggregateConfig = {
    /** React Query key suffix for the form-schema fetch. */
    schemaQueryKey: string;
    /** Endpoint serving { schema, uiSchema }. */
    schemaEndpoint: string;
    /** Field-path PATCH endpoint for one row. */
    patchEndpointFor: (id: number) => string;
    /** Cache key for this aggregate's own GET-by-id query. */
    selfCacheKey: (id: number) => QueryKey;
    /** Cache key for the parent whose collapsed card this row feeds. */
    parentCacheKey: (id: number) => QueryKey;
    /** config key under which the entity id is exposed to renderers
     *  (e.g. AttachmentsRenderer reads config.assayId). */
    idConfigKey: string;
    /** config key under which the parent id is exposed to renderers. */
    parentConfigKey: string;
};

export const AGGREGATES: Record<AggregateKind, AggregateConfig> = {
    assay: {
        schemaQueryKey: 'assay-form-schema',
        schemaEndpoint: '/assays/form-schema',
        patchEndpointFor: (id) => `/assays/${id}`,
        selfCacheKey: assayKey,
        parentCacheKey: mutationKey,
        idConfigKey: 'assayId',
        parentConfigKey: 'mutationId',
    },
    gene: {
        schemaQueryKey: 'gene-form-schema',
        schemaEndpoint: '/genes/form-schema',
        patchEndpointFor: (id) => `/genes/${id}`,
        selfCacheKey: geneKey,
        parentCacheKey: mutationKey,
        idConfigKey: 'geneId',
        parentConfigKey: 'mutationId',
    },
    lesion: {
        schemaQueryKey: 'lesion-form-schema',
        schemaEndpoint: '/lesions/form-schema',
        patchEndpointFor: (id) => `/lesions/${id}`,
        selfCacheKey: lesionKey,
        parentCacheKey: mutationKey,
        idConfigKey: 'lesionId',
        parentConfigKey: 'mutationId',
    },
    phenotype: {
        schemaQueryKey: 'phenotype-form-schema',
        schemaEndpoint: '/phenotypes/form-schema',
        patchEndpointFor: (id) => `/phenotypes/${id}`,
        selfCacheKey: phenotypeKey,
        parentCacheKey: mutationKey,
        idConfigKey: 'phenotypeId',
        parentConfigKey: 'mutationId',
    },
};
