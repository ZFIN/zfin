import * as React from 'react';
import { useGeneById } from '../api/queries';
import { ZircEntityEditor } from '../schemaForm/ZircEntityEditor';

/**
 * Inline per-gene editor (M6.1), mounted inside an expanded card on the
 * GenesListRenderer. A thin wrapper over {@link ZircEntityEditor} — the
 * marker autocomplete and the parent-card refresh on mutatedGeneZdbID
 * change are driven by the uiSchema flags from ZircGeneFormSchema.
 */

export type GeneEditProps = {
    geneId: number;
    mutationId?: number;
};

export function GeneEdit({ geneId, mutationId }: GeneEditProps) {
    const entityQuery = useGeneById(geneId);
    return (
        <ZircEntityEditor
            kind='gene'
            entityId={geneId}
            parentId={mutationId}
            entityQuery={entityQuery}
        />
    );
}
