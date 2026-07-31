import * as React from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { AutocompleteEndpoint, useAutocomplete, useDebouncedValue } from '../api/queries';

type Props = {
    submissionZdbID: string;
    /** Wire role used by the server endpoint URL — `add-submitter` or `add-pi`. */
    endpoint: 'add-submitter' | 'add-pi';
    /** Used for accessibility labels + the menu's empty-state text. */
    roleLabel: string;
    /**
     * Which autocomplete endpoint to query for suggestions. Defaults to
     * the unrestricted Person search ("persons"); use "pis" to restrict
     * to people holding a PI-level lab position.
     */
    autocompleteEndpoint?: AutocompleteEndpoint;
};

/**
 * Inline "+ Add" trigger that opens a small autocomplete popover for
 * picking a {@link org.zfin.profile.Person} and POSTing the selection
 * to {@code /action/zirc/line-submission/{zdb}/{endpoint}}. Used by
 * the {@code StatusOverviewBar} so curators can add a Submitter or PI
 * directly from the bar instead of switching to the legacy JSP detail
 * page. On success, invalidates the {@code ['zirc', 'lineSubmission']}
 * query so the bar's Submitter/PI cells refresh in place.
 */
export function AddPersonPicker({
    submissionZdbID, endpoint, roleLabel, autocompleteEndpoint = 'persons',
}: Props) {
    const [open, setOpen] = React.useState(false);
    const [term, setTerm] = React.useState('');
    const [error, setError] = React.useState<string | null>(null);
    const [busy, setBusy] = React.useState(false);
    const debouncedTerm = useDebouncedValue(term, 200);
    const suggestions = useAutocomplete(autocompleteEndpoint, debouncedTerm);
    const inputRef = React.useRef<HTMLInputElement | null>(null);
    const wrapperRef = React.useRef<HTMLSpanElement | null>(null);
    const qc = useQueryClient();

    // Close on outside-click / Escape.
    React.useEffect(() => {
        if (!open) {return;}
        const onDown = (e: MouseEvent) => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
                setOpen(false);
            }
        };
        const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') {setOpen(false);} };
        document.addEventListener('mousedown', onDown);
        document.addEventListener('keydown', onKey);
        return () => {
            document.removeEventListener('mousedown', onDown);
            document.removeEventListener('keydown', onKey);
        };
    }, [open]);

    // Focus the input when the popover opens.
    React.useEffect(() => {
        if (open && inputRef.current) {inputRef.current.focus();}
    }, [open]);

    const items = suggestions.data ?? [];

    const pick = async (personZdbID: string) => {
        setBusy(true);
        setError(null);
        try {
            const params = new URLSearchParams({ personZdbID });
            const res = await fetch(
                `/action/zirc/line-submission/${encodeURIComponent(submissionZdbID)}/${endpoint}`,
                { method: 'POST', body: params, headers: { 'Accept': 'application/json' } },
            );
            if (!res.ok) {
                const body = await res.text().catch(() => '');
                throw new Error(`HTTP ${res.status} ${res.statusText}: ${body.slice(0, 200)}`);
            }
            // Refresh the line submission so Submitter / PI cells re-render
            // with the new name.
            await qc.invalidateQueries({ queryKey: ['zirc', 'lineSubmission', submissionZdbID] });
            setTerm('');
            setOpen(false);
        } catch (e) {
            setError(`Add ${roleLabel} failed: ${(e as Error).message}`);
        } finally {
            setBusy(false);
        }
    };

    return (
        <span ref={wrapperRef} style={{ position: 'relative', display: 'inline-block', marginLeft: '0.25rem' }}>
            <button
                type='button'
                className='btn btn-link p-0 text-success'
                aria-label={`Add a ${roleLabel}`}
                title={`Add a ${roleLabel}`}
                onClick={() => setOpen((v) => !v)}
            >
                <i className='fas fa-plus-circle'/>
            </button>
            {open && (
                <div
                    role='dialog'
                    aria-label={`Add a ${roleLabel}`}
                    style={{
                        position: 'absolute',
                        top: '1.4rem',
                        left: 0,
                        zIndex: 1000,
                        background: '#fff',
                        border: '1px solid #ced4da',
                        borderRadius: '4px',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
                        padding: '0.5rem',
                        width: '20rem',
                    }}
                >
                    <input
                        ref={inputRef}
                        type='text'
                        className='form-control form-control-sm'
                        placeholder='Start typing a name…'
                        autoComplete='off'
                        value={term}
                        onChange={(e) => setTerm(e.target.value)}
                        disabled={busy}
                    />
                    {error && <div className='text-danger small mt-1'>{error}</div>}
                    {busy && <div className='text-muted small mt-1'>Adding…</div>}
                    {!busy && items.length > 0 && (
                        <ul className='list-group list-group-flush mt-1' style={{ maxHeight: 260, overflowY: 'auto' }}>
                            {items.map((it) => (
                                <li
                                    key={it.value}
                                    role='option'
                                    aria-selected={false}
                                    className='list-group-item list-group-item-action small py-1'
                                    style={{ cursor: 'pointer' }}
                                    onMouseDown={(e) => { e.preventDefault(); pick(it.value); }}
                                >
                                    {it.label}
                                </li>
                            ))}
                        </ul>
                    )}
                    {!busy && debouncedTerm.length > 0 && items.length === 0 && !suggestions.isLoading && (
                        <div className='text-muted small mt-1'>No matches.</div>
                    )}
                </div>
            )}
        </span>
    );
}
