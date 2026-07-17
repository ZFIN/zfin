import * as React from 'react';
import { useAssayById } from '../api/queries';
import { ZircEntityEditor } from '../schemaForm/ZircEntityEditor';

/**
 * Inline per-assay editor (M4.2), mounted inside an expanded card on the
 * AssaysListRenderer. A thin wrapper over {@link ZircEntityEditor} — the
 * assay-type field matrix, the attachments self-management, and the
 * parent-card refresh on assayType change are all driven by the uiSchema
 * flags from ZircAssayFormSchema, not this component.
 */

export type AssayEditProps = {
    assayId: number;
    mutationId?: number;
};

export function AssayEdit({ assayId, mutationId }: AssayEditProps) {
    const entityQuery = useAssayById(assayId);
    return (
        <ZircEntityEditor
            kind='assay'
            entityId={assayId}
            parentId={mutationId}
            entityQuery={entityQuery}
        />
    );
}
