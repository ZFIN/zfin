import * as React from 'react';
import { useLesionById } from '../api/queries';
import { ZircEntityEditor } from '../schemaForm/ZircEntityEditor';

/**
 * Inline per-lesion editor (M7.1), mounted inside an expanded card on the
 * LesionsListRenderer. A thin wrapper over {@link ZircEntityEditor} — the
 * lesion-type field matrix and the parent-card refresh on lesionType
 * change are driven by the uiSchema flags from ZircLesionFormSchema.
 */

export type LesionEditProps = {
    lesionId: number;
    mutationId?: number;
};

export function LesionEdit({ lesionId, mutationId }: LesionEditProps) {
    const entityQuery = useLesionById(lesionId);
    return (
        <ZircEntityEditor
            kind='lesion'
            entityId={lesionId}
            parentId={mutationId}
            entityQuery={entityQuery}
        />
    );
}
