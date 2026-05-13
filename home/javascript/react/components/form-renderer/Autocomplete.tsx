import React, {useEffect, useRef, useState} from 'react';

interface AutocompleteSuggestion {
    label: string;
    value: string;
}

interface AutocompleteProps {
    id: string;
    value: string;
    placeholder?: string;
    fetchUrl: string;
    onChange: (next: string) => void;
    onCommit: (next: string) => void;
}

/**
 * Minimal accessible typeahead. Fetches suggestions from
 * `${fetchUrl}?term=…` whenever the input value changes (debounced
 * ~150ms). Keyboard nav: ArrowDown/Up cycles the suggestion list,
 * Enter selects, Escape closes. Mouse: click a suggestion to select.
 * Selecting a suggestion sets the input value and calls onCommit; the
 * raw input value also commits on blur, so curators can free-type.
 */
const Autocomplete = ({id, value, placeholder, fetchUrl, onChange, onCommit}: AutocompleteProps) => {
    const [suggestions, setSuggestions] = useState<AutocompleteSuggestion[]>([]);
    const [open, setOpen] = useState<boolean>(false);
    const [activeIdx, setActiveIdx] = useState<number>(-1);
    const debounceRef = useRef<number | null>(null);
    const reqSeqRef = useRef<number>(0);

    useEffect(() => {
        if (debounceRef.current) {
            window.clearTimeout(debounceRef.current);
        }
        const term = value.trim();
        if (term.length < 1) {
            setSuggestions([]);
            return;
        }
        debounceRef.current = window.setTimeout(() => {
            const seq = ++reqSeqRef.current;
            // Use '&' when the caller already has query params in
            // fetchUrl (e.g. a typeGroup filter), '?' otherwise.
            const sep = fetchUrl.includes('?') ? '&' : '?';
            fetch(`${fetchUrl}${sep}term=${encodeURIComponent(term)}`)
                .then(r => r.ok ? r.json() : [])
                .then((data: AutocompleteSuggestion[]) => {
                    // Drop stale responses if a newer request was kicked off.
                    if (seq === reqSeqRef.current) {
                        setSuggestions(Array.isArray(data) ? data : []);
                        setActiveIdx(-1);
                    }
                })
                .catch(() => { /* swallow — suggestions are optional */ });
        }, 150);
        return () => {
            if (debounceRef.current) {
                window.clearTimeout(debounceRef.current);
            }
        };
    }, [value, fetchUrl]);

    function pick(s: AutocompleteSuggestion) {
        onChange(s.value);
        onCommit(s.value);
        setOpen(false);
        setActiveIdx(-1);
    }

    function handleKey(e: React.KeyboardEvent<HTMLInputElement>) {
        if (!open || suggestions.length === 0) {
            return;
        }
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActiveIdx(i => (i + 1) % suggestions.length);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActiveIdx(i => (i <= 0 ? suggestions.length - 1 : i - 1));
        } else if (e.key === 'Enter' && activeIdx >= 0) {
            e.preventDefault();
            pick(suggestions[activeIdx]);
        } else if (e.key === 'Escape') {
            setOpen(false);
            setActiveIdx(-1);
        }
    }

    return (
        <div style={{position: 'relative'}}>
            <input
                id={id}
                type='text'
                className='form-control'
                placeholder={placeholder}
                value={value}
                onChange={e => { onChange(e.target.value); setOpen(true); }}
                onFocus={() => setOpen(true)}
                onBlur={() => {
                    // Defer close so a click on a suggestion lands first.
                    window.setTimeout(() => setOpen(false), 150);
                    onCommit(value);
                }}
                onKeyDown={handleKey}
                aria-autocomplete='list'
                aria-expanded={open && suggestions.length > 0}
                autoComplete='off'
            />
            {open && suggestions.length > 0 && (
                <ul
                    className='list-group'
                    style={{
                        position: 'absolute',
                        top: '100%',
                        left: 0,
                        right: 0,
                        zIndex: 1050,
                        maxHeight: 240,
                        overflowY: 'auto',
                        marginTop: 2,
                    }}
                    role='listbox'
                >
                    {suggestions.map((s, i) => (
                        <li
                            key={s.value + i}
                            className={'list-group-item list-group-item-action py-1 small'
                                + (i === activeIdx ? ' active' : '')}
                            onMouseDown={e => { e.preventDefault(); pick(s); }}
                            role='option'
                            aria-selected={i === activeIdx}
                            style={{cursor: 'pointer'}}
                        >
                            {s.label}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default Autocomplete;
