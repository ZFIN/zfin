import * as React from 'react';

export type SaveStatus = 'idle' | 'saving' | 'saved' | 'error';

/**
 * Position-fixed toast for the autosave status indicator. Replaces the
 * inline text we shipped initially; the API the caller sees is unchanged
 * (status + optional message), but the visible affordance is a small
 * floating badge in the bottom-right of the viewport that auto-dismisses
 * the "Saved" state after ~2s. Errors persist until status changes.
 *
 * One small caveat: there is no animation library here, so the fade is
 * a plain CSS transition on opacity, driven by a local boolean.
 */
export function SaveStatusBadge({
    status,
    message,
}: {
    status: SaveStatus;
    message: string | null;
}) {
    // visible drives the fade-out: when status flips back to idle (or saved
    // times out), we still want a couple hundred ms of fade before unmounting.
    const [visible, setVisible] = React.useState(false);
    const [mounted, setMounted] = React.useState(false);

    React.useEffect(() => {
        if (status === 'idle') {
            setVisible(false);
            // Wait for the fade-out, then unmount.
            const t = window.setTimeout(() => setMounted(false), 300);
            return () => window.clearTimeout(t);
        }
        setMounted(true);
        setVisible(true);

        if (status === 'saved') {
            // Auto-dismiss after 2s on success.
            const t = window.setTimeout(() => setVisible(false), 2000);
            const t2 = window.setTimeout(() => setMounted(false), 2300);
            return () => {
                window.clearTimeout(t);
                window.clearTimeout(t2);
            };
        }
        return undefined;
    }, [status]);

    if (!mounted) {return null;}

    const text = (
        status === 'saving' ? 'Saving…'
            : status === 'saved' ? 'Saved'
                : status === 'error' ? `Error${message ? `: ${message}` : ''}`
                    : ''
    );

    const bgClass = (
        status === 'saving' ? 'bg-secondary'
            : status === 'saved' ? 'bg-success'
                : status === 'error' ? 'bg-danger'
                    : 'bg-secondary'
    );

    const style: React.CSSProperties = {
        position: 'fixed',
        bottom: 16,
        right: 16,
        zIndex: 2000,
        padding: '0.4rem 0.75rem',
        borderRadius: 4,
        color: '#fff',
        boxShadow: '0 2px 6px rgba(0,0,0,0.18)',
        opacity: visible ? 1 : 0,
        transition: 'opacity 250ms ease',
        pointerEvents: 'none',
    };

    return (
        <div
            role={status === 'error' ? 'alert' : 'status'}
            aria-live={status === 'error' ? 'assertive' : 'polite'}
            className={`small ${bgClass}`}
            style={style}
            title={status === 'error' && message ? message : undefined}
        >
            {text}
        </div>
    );
}
