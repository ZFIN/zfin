import * as React from 'react';
import type { ViewConfig } from '../schemaForm/useViewConfig';
import { commentsEnabled } from '../schemaForm/useViewConfig';
import { FieldHistory } from './FieldHistory';
import { FieldComments } from './FieldComments';

type Props = {
    view: ViewConfig;
    fieldName: string;
    label: string;
    uischema: unknown;
};

/**
 * History + comments icons for one field, as the edit-mode row renderers
 * place them next to the field label (ZFIN-10543). The merged submission page
 * edits in place, so a curator needs the same review tools the read-only
 * detail page had beside each value. Both icons render nothing until the
 * record has a recId (e.g. /new before the first save).
 */
export function FieldAccessories({ view, fieldName, label, uischema }: Props) {
    return (
        <>
            <FieldHistory
                recId={view.recId}
                scope='field'
                fieldName={fieldName}
                label={label}
            />
            {commentsEnabled(uischema) && (
                <FieldComments
                    recId={view.recId}
                    scope='field'
                    fieldName={fieldName}
                    label={label}
                />
            )}
        </>
    );
}
