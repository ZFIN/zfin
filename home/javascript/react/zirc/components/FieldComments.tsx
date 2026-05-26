import * as React from 'react';
import { createPortal } from 'react-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

export type FieldCommentDTO = {
    id: number;
    recId: string;
    scope: 'field' | 'section';
    fieldName: string | null;
    sectionName: string | null;
    authorZdbId: string;
    authorName: string;
    comment: string;
    createdAt: string | null;
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
) => ['zirc', 'comments', recId, scope, fieldName ?? '', sectionName ?? ''] as const;

/**
 * Comment icon + portaled popover. Mirrors {@link FieldHistory}'s placement
 * and viewport handling so both popups behave the same way next to the nav.
 *
 * <p>Click the icon to open: fetches comments for this (recId, scope,
 * fieldName|sectionName) tuple, lists them, and offers a textarea + Save to
 * append. The badge shows the current count when > 0 so curators see at a
 * glance which fields have prior discussion.
 */
export function FieldComments({ recId, scope, fieldName, sectionName, label }: Props) {
    const [open, setOpen] = React.useState(false);
    const [anchorRect, setAnchorRect] = React.useState<DOMRect | null>(null);
    const [draft, setDraft] = React.useState('');
    const [errMsg, setErrMsg] = React.useState<string | null>(null);
    const triggerRef = React.useRef<HTMLButtonElement | null>(null);
    const qc = useQueryClient();

    const enabled = !!recId && (scope === 'field' ? !!fieldName : !!sectionName);

    const list = useQuery<FieldCommentDTO[]>({
        queryKey: enabled
            ? queryKey(recId!, scope, fieldName ?? null, sectionName ?? null)
            : ['zirc', 'comments', '__disabled__'],
        queryFn: () => {
            const p = new URLSearchParams({ recId: recId!, scope });
            if (scope === 'field') {p.set('fieldName', fieldName!);}
            else {p.set('sectionName', sectionName!);}
            return api.get<FieldCommentDTO[]>(`/comments?${p.toString()}`);
        },
        enabled: enabled && open,
        staleTime: 30_000,
    });

    const post = useMutation({
        mutationFn: (body: string) => api.post<FieldCommentDTO>('/comments', {
            recId, scope, fieldName, sectionName, comment: body,
        }),
        onSuccess: (created) => {
            setDraft('');
            setErrMsg(null);
            qc.setQueryData<FieldCommentDTO[]>(
                queryKey(recId!, scope, fieldName ?? null, sectionName ?? null),
                (prev) => [...(prev ?? []), created],
            );
        },
        onError: (e) => setErrMsg(e instanceof Error ? e.message : 'Failed to post'),
    });

    const popupKey = `${recId ?? ''}-${scope}-${fieldName ?? ''}${sectionName ?? ''}`;

    // Hooks must run unconditionally — the `!enabled` early return moved
    // below all hooks to satisfy rules of hooks. Effects are gated on
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
            const popup = document.getElementById(`fc-popup-${popupKey}`);
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

    if (!enabled) {return null;}

    const handleToggle = () => {
        if (!open && triggerRef.current) {
            setAnchorRect(triggerRef.current.getBoundingClientRect());
        }
        setOpen((v) => !v);
    };

    const count = list.data?.length ?? 0;

    const popup = open && anchorRect ? (
        <div
            id={`fc-popup-${popupKey}`}
            role='dialog'
            aria-label={`${label} comments`}
            className='card shadow'
            style={{
                position: 'fixed',
                top: Math.min(anchorRect.bottom + 4, window.innerHeight - 320),
                left: Math.max(8, Math.min(
                    anchorRect.right - 460,
                    window.innerWidth - 468)),
                zIndex: 2000,
                width: 460,
                maxHeight: '70vh',
                overflowY: 'auto',
            }}
        >
            <div className='card-header py-1 d-flex justify-content-between align-items-center'>
                <strong>{label} — Comments</strong>
                <button
                    type='button'
                    className='close'
                    aria-label='Close'
                    onClick={() => setOpen(false)}
                >
                    <span aria-hidden='true'>&times;</span>
                </button>
            </div>
            <div className='card-body py-2'>
                {list.isLoading && <p className='text-muted small mb-2'>Loading…</p>}
                {list.isError && (
                    <div className='alert alert-warning py-1 mb-2 small'>
                        Failed to load comments.
                    </div>
                )}
                {list.data && list.data.length === 0 && (
                    <p className='text-muted small mb-2'>No comments yet.</p>
                )}
                {list.data && list.data.length > 0 && (
                    <ul className='list-unstyled mb-3'>
                        {list.data.map((c) => (
                            <li key={c.id} className='mb-2'>
                                <div className='small text-muted'>
                                    <a href={`/action/profile/person/view/${c.authorZdbId}`}>
                                        {c.authorName}
                                    </a>
                                    {c.createdAt && <> &middot; {c.createdAt}</>}
                                </div>
                                <div style={{ whiteSpace: 'pre-wrap' }}>{c.comment}</div>
                            </li>
                        ))}
                    </ul>
                )}
                <textarea
                    className='form-control form-control-sm mb-2'
                    rows={3}
                    placeholder='Add a comment…'
                    value={draft}
                    onChange={(e) => setDraft(e.target.value)}
                />
                {errMsg && <div className='alert alert-danger py-1 mb-2 small'>{errMsg}</div>}
                <div className='d-flex justify-content-end'>
                    <button
                        type='button'
                        className='btn btn-sm btn-primary'
                        disabled={draft.trim().length === 0 || post.isPending}
                        onClick={() => post.mutate(draft.trim())}
                    >
                        {post.isPending ? 'Saving…' : 'Post'}
                    </button>
                </div>
            </div>
        </div>
    ) : null;

    return (
        <span className='field-comments-wrapper ml-2'>
            <button
                ref={triggerRef}
                type='button'
                className='btn btn-link p-0 text-muted'
                aria-label={`Comments on ${label}`}
                title={`Comments on ${label}`}
                onClick={handleToggle}
            >
                <i className='far fa-comment'/>
                {count > 0 && (
                    <span className='badge badge-secondary ml-1' style={{ fontSize: '0.7em' }}>
                        {count}
                    </span>
                )}
            </button>
            {popup && createPortal(popup, document.body)}
        </span>
    );
}
