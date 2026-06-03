import * as React from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { HistoryFocusContext } from '../historyFocusContext';

export type AuditEntry = {
    id: number;
    whenUpdated: string | null;
    actor: string;
    actorName: string;
    action: string;
    entityKind: string | null;
    path: string | null;
    oldValue: string | null;
    newValue: string | null;
};

type Props = {
    recId: string | null | undefined;
    scope: 'field' | 'section' | 'entity';
    fieldName?: string | null;
    sectionName?: string | null;
    label: string;
};

const queryKey = (
    recId: string,
    scope: 'field' | 'section' | 'entity',
    fieldName: string | null,
    sectionName: string | null,
) => ['zirc', 'audit', recId, scope, fieldName ?? '', sectionName ?? ''] as const;

/**
 * Field-history trigger. Renders a small clock icon next to a field /
 * section when there's at least one prior change recorded for it; clicking
 * the icon dispatches a {@link HistoryFocus} into the page-level
 * {@link HistoryFocusContext} so the right-hand ChangeHistoryPanel re-titles
 * and re-scopes to that exact (recId, fieldName | sectionName).
 *
 * <p>We still run the lookup query here — at low cost, gated by react-query
 * caching — so the icon stays hidden when nothing's changed. The actual
 * history rendering lives entirely in the right panel.
 */
export function FieldHistory({ recId, scope, fieldName, sectionName, label }: Props) {
    const { setFocus } = React.useContext(HistoryFocusContext);
    const enabled = !!recId
        && (scope === 'field'   ? !!fieldName
            :   scope === 'section' ? !!sectionName
                :                          true);

    const list = useQuery<AuditEntry[]>({
        queryKey: enabled
            ? queryKey(recId!, scope, fieldName ?? null, sectionName ?? null)
            : ['zirc', 'audit', '__disabled__'],
        queryFn: () => {
            const p = new URLSearchParams({ recId: recId!, scope });
            if (scope === 'field')   {p.set('fieldName',   fieldName!);}
            if (scope === 'section') {p.set('sectionName', sectionName!);}
            return api.get<AuditEntry[]>(`/audit?${p.toString()}`);
        },
        enabled,
        staleTime: 30_000,
    });

    if (!enabled || (list.data ?? []).length === 0) {return null;}

    return (
        <span className='field-history-wrapper ml-2'>
            <button
                type='button'
                className='btn btn-link p-0 text-muted field-history-trigger'
                aria-label={`Show change history for ${label} in the panel`}
                title={`Show change history for ${label} in the panel`}
                onClick={() => setFocus({
                    recId: recId!,
                    scope,
                    fieldName: fieldName ?? null,
                    sectionName: sectionName ?? null,
                    label,
                })}
            >
                <i className='fas fa-history'/>
            </button>
        </span>
    );
}
