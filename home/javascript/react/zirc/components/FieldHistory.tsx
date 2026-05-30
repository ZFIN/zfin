import * as React from 'react';
import { createPortal } from 'react-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

export type AuditEntry = {
    id: number;
    whenUpdated: string | null;
    actor: string;
    actorName: string;
    action: string;
    path: string | null;
    oldValue: string | null;
    newValue: string | null;
};

type Props = {
    recId: string | null | undefined;
    scope: 'field' | 'section';
    fieldName?: string | null;
    sectionName?: string | null;
    label: string;
};

const queryKey = (
    recId: string,
    scope: 'field' | 'section',
    fieldName: string | null,
    sectionName: string | null,
) => ['zirc', 'audit', recId, scope, fieldName ?? '', sectionName ?? ''] as const;

/**
 * Field-history popover. Symmetric with {@link FieldComments}: opens on
 * click, lazy-fetches {@code GET /api/zirc/audit?recId&scope&fieldName|
 * sectionName}, lists prior values. recId follows the same convention
 * used elsewhere (submission ZDB-ID or {@code ZIRC-<KIND>-<id>}); nested
 * aggregates get history for free without bootstrap-payload plumbing.
 *
 * <p>Hides itself entirely when the lookup returns zero rows — there's
 * no historical chatter to surface for that (recId, field).
 */
export function FieldHistory({ recId, scope, fieldName, sectionName, label }: Props) {
    const [open, setOpen] = React.useState(false);
    const [anchorRect, setAnchorRect] = React.useState<DOMRect | null>(null);
    const triggerRef = React.useRef<HTMLButtonElement | null>(null);

    const enabled = !!recId && (scope === 'field' ? !!fieldName : !!sectionName);

    // We always want the count up front to gate the trigger visibility;
    // enable the query regardless of open state.
    const list = useQuery<AuditEntry[]>({
        queryKey: enabled
            ? queryKey(recId!, scope, fieldName ?? null, sectionName ?? null)
            : ['zirc', 'audit', '__disabled__'],
        queryFn: () => {
            const p = new URLSearchParams({ recId: recId!, scope });
            if (scope === 'field') {p.set('fieldName', fieldName!);}
            else {p.set('sectionName', sectionName!);}
            return api.get<AuditEntry[]>(`/audit?${p.toString()}`);
        },
        enabled,
        staleTime: 30_000,
    });

    const updates = list.data ?? [];
    const popupKey = `${recId ?? ''}-${scope}-${fieldName ?? ''}${sectionName ?? ''}`;

    // Hooks must run unconditionally — the "no history → render null" branch
    // moved below all hooks to satisfy rules of hooks. Effects are gated on
    // `open` internally and no-op when the popup is closed.
    React.useEffect(() => {
        if (!open) {return;}
        const update = () => {
            if (triggerRef.current) {
                setAnchorRect(triggerRef.current.getBoundingClientRect());
            }
        };
        window.addEventListener('scroll', update, true);
        window.addEventListener('resize', update);
        return () => {
            window.removeEventListener('scroll', update, true);
            window.removeEventListener('resize', update);
        };
    }, [open]);

    React.useEffect(() => {
        if (!open) {return;}
        const onKey = (e: KeyboardEvent) => {
            if (e.key === 'Escape') {setOpen(false);}
        };
        const onDocClick = (e: MouseEvent) => {
            const target = e.target as Node;
            if (triggerRef.current?.contains(target)) {return;}
            const popup = document.getElementById(`fh-popup-${popupKey}`);
            if (popup?.contains(target)) {return;}
            setOpen(false);
        };
        document.addEventListener('keydown', onKey);
        document.addEventListener('mousedown', onDocClick);
        return () => {
            document.removeEventListener('keydown', onKey);
            document.removeEventListener('mousedown', onDocClick);
        };
    }, [open, popupKey]);

    if (!enabled || updates.length === 0) {return null;}

    const handleToggle = () => {
        if (!open && triggerRef.current) {
            setAnchorRect(triggerRef.current.getBoundingClientRect());
        }
        setOpen((v) => !v);
    };

    const popup = open && anchorRect ? (
        <div
            id={`fh-popup-${popupKey}`}
            role='dialog'
            aria-label={`${label} change history`}
            className='card shadow'
            style={{
                position: 'fixed',
                top: Math.min(anchorRect.bottom + 4, window.innerHeight - 240),
                left: Math.max(8, Math.min(
                    anchorRect.right - 480,
                    window.innerWidth - 488)),
                zIndex: 2000,
                minWidth: 480,
                maxWidth: 720,
                maxHeight: '70vh',
                overflowY: 'auto',
            }}
        >
            <div className='card-header py-1 d-flex justify-content-between align-items-center'>
                <strong>{label} — Change History</strong>
                <button
                    type='button'
                    className='close'
                    aria-label='Close'
                    onClick={() => setOpen(false)}
                >
                    <span aria-hidden='true'>&times;</span>
                </button>
            </div>
            <table className='table table-sm table-striped mb-0'>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Person</th>
                        <th>Old Value</th>
                        <th>New Value</th>
                    </tr>
                </thead>
                <tbody>
                    {updates.map((u) => (
                        <tr key={u.id}>
                            <td>{u.whenUpdated ?? ''}</td>
                            <td>{personLabel(u)}</td>
                            <td>{u.oldValue ?? <span className='text-muted'>&mdash;</span>}</td>
                            <td>{u.newValue ?? <span className='text-muted'>&mdash;</span>}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    ) : null;

    return (
        <span className='field-history-wrapper ml-2'>
            <button
                ref={triggerRef}
                type='button'
                className='btn btn-link p-0 text-muted field-history-trigger'
                aria-label={`View change history for ${label}`}
                title={`View change history for ${label}`}
                onClick={handleToggle}
            >
                <i className='fas fa-history'/>
            </button>
            {popup && createPortal(popup, document.body)}
        </span>
    );
}

function personLabel(u: AuditEntry): React.ReactNode {
    if (u.actor) {
        return (
            <a href={`/action/profile/person/view/${u.actor}`}>
                {u.actorName}
            </a>
        );
    }
    return <span className='text-muted'>&mdash;</span>;
}
