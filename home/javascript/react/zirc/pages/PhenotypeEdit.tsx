import * as React from 'react';
import { usePhenotypeById } from '../api/queries';
import { ZircEntityEditor } from '../schemaForm/ZircEntityEditor';

/**
 * Inline per-phenotype editor (M8.1), mounted inside an expanded card on
 * the PhenotypesListRenderer. A thin wrapper over {@link ZircEntityEditor}
 * — the hpf/dpf timing widget and the parent-card refresh on description
 * change are driven by the uiSchema flags from ZircPhenotypeFormSchema.
 */

export type PhenotypeEditProps = {
    phenotypeId: number;
    mutationId?: number;
};

export function PhenotypeEdit({ phenotypeId, mutationId }: PhenotypeEditProps) {
    const entityQuery = usePhenotypeById(phenotypeId);
    return (
        <ZircEntityEditor
            kind='phenotype'
            entityId={phenotypeId}
            parentId={mutationId}
            entityQuery={entityQuery}
        />
    );
}
