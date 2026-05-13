import React, {useEffect, useState} from 'react';

export type SaveStatus = 'saving' | 'saved' | 'error';

export interface SaveEvent {
    status: SaveStatus;
    label: string;
    message?: string;
    /** Increments on every emit so equal payloads still re-trigger the effect. */
    seq: number;
}

interface SaveToastProps {
    event: SaveEvent | null;
}

const FADE_MS = 2000;

const SaveToast = ({event}: SaveToastProps) => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        if (!event) {
            return;
        }
        setVisible(true);
        if (event.status === 'saved') {
            const timer = setTimeout(() => setVisible(false), FADE_MS);
            return () => clearTimeout(timer);
        }
        return undefined;
    }, [event]);

    if (!event || !visible) {
        return null;
    }

    const variant = event.status === 'error' ? 'danger'
        : event.status === 'saving' ? 'secondary'
            : 'success';
    // Field label is intentionally not surfaced — the toast just confirms
    // that *something* saved. The label is still kept on SaveEvent for
    // telemetry / debugging.
    const text = event.status === 'saving' ? 'Saving…'
        : event.status === 'saved' ? 'Saved'
            : `Error saving: ${event.message ?? ''}`;

    const style: React.CSSProperties = {
        position: 'fixed',
        bottom: 16,
        right: 16,
        zIndex: 1100,
        minWidth: 220,
        maxWidth: 360,
        boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
    };

    return (
        <div className={`alert alert-${variant} mb-0`} style={style} role='status'>
            {event.status === 'error' && (
                <button
                    type='button'
                    className='close'
                    aria-label='Dismiss'
                    style={{marginLeft: 8}}
                    onClick={() => setVisible(false)}
                >
                    <span aria-hidden='true'>&times;</span>
                </button>
            )}
            {text}
        </div>
    );
};

export default SaveToast;
