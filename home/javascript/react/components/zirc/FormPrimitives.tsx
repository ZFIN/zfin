import React, {useEffect, useRef, useState} from 'react';

export type FieldType = 'text' | 'textarea' | 'bool' | 'select' | 'autocomplete';

export interface SelectOption {
    value: string;
    label: string;
}

export interface FieldDef<T extends string = string> {
    field: T;
    label: string;
    type: FieldType;
    /** React key for the rendered row. Defaults to `field`. Set this when
     *  two FieldDefs share the same `field` (e.g. autocomplete and free-text
     *  variants for the same column, gated by `visible`) so React doesn't
     *  warn about duplicate keys. */
    rowKey?: string;
    /** Optional id prefix for the input — useful when multiple components
     *  on the page would otherwise collide on `ls-field-name` etc. Defaults
     *  to "ls-field" for the line-submission form. */
    idPrefix?: string;
    /** HTML placeholder; only meaningful for `text` and `textarea`. */
    placeholder?: string;
    /** Required for `select` fields. Renders a "(select)" sentinel option
     *  before these so the field can start unselected. */
    options?: SelectOption[];
    /** Required for `autocomplete` fields. The component fetches matches
     *  from `${autocompleteUrl}?term=…` and expects a JSON array of
     *  `{label, value}` (extra fields ignored). */
    autocompleteUrl?: string;
    /** Optional visibility predicate. The row is rendered iff this returns
     *  true (or is absent). The predicate is given the current value map
     *  so conditional rendering can branch on sibling field values. */
    visible?: (values: Record<string, string>) => boolean;
    /** Optional URL for a small "(i)" link rendered next to the label.
     *  Use for technical fields that benefit from an external reference
     *  (HGVS nomenclature, allele-designation conventions, …). */
    infoHref?: string;
    /** Optional hint rendered below the input as muted help text. */
    helpText?: string;
}

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
export const Autocomplete = ({id, value, placeholder, fetchUrl, onChange, onCommit}: AutocompleteProps) => {
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
            fetch(`${fetchUrl}?term=${encodeURIComponent(term)}`)
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

/**
 * Render a value (string | boolean | null | undefined) into the string
 * representation a controlled `<input>` / `<textarea>` / radio expects.
 * Bools become `'true'` / `'false'`; null / undefined become `''`.
 */
export function valueToInputString(v: string | boolean | null | undefined): string {
    if (v === null || v === undefined) {
        return '';
    }
    if (typeof v === 'boolean') {
        return v ? 'true' : 'false';
    }
    return v;
}

interface FieldRowProps<T extends string> {
    def: FieldDef<T>;
    value: string;
    onChange: (next: string) => void;
    onCommit: (next: string) => void;
}

export const FieldRow = <T extends string>({def, value, onChange, onCommit}: FieldRowProps<T>) => {
    const prefix = def.idPrefix ?? 'ls-field';
    const inputId = `${prefix}-${def.field}`;
    const labelId = `${prefix}-label-${def.field}`;

    function renderInput() {
        if (def.type === 'textarea') {
            return (
                <textarea
                    id={inputId}
                    className='form-control'
                    rows={3}
                    placeholder={def.placeholder}
                    value={value}
                    onChange={e => onChange(e.target.value)}
                    onBlur={() => onCommit(value)}
                />
            );
        }
        if (def.type === 'autocomplete') {
            return (
                <Autocomplete
                    id={inputId}
                    value={value}
                    placeholder={def.placeholder}
                    fetchUrl={def.autocompleteUrl ?? ''}
                    onChange={onChange}
                    onCommit={onCommit}
                />
            );
        }
        if (def.type === 'select') {
            const options = def.options ?? [];
            return (
                <select
                    id={inputId}
                    className='form-control'
                    value={value}
                    onChange={e => { onChange(e.target.value); onCommit(e.target.value); }}
                >
                    <option value=''>(select)</option>
                    {options.map(opt => (
                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                    ))}
                </select>
            );
        }
        if (def.type === 'bool') {
            const groupName = `${prefix}-group-${def.field}`;
            // Two-state radio: Yes / No. Initial null shows nothing checked,
            // but once the user picks a value there's no way to unset it
            // (matches the YAML form spec — null is "not yet answered").
            const options: Array<[string, string]> = [['true', 'Yes'], ['false', 'No']];
            return (
                <div role='radiogroup' aria-labelledby={labelId}>
                    {options.map(([v, lbl]) => {
                        const radioId = `${inputId}-${v}`;
                        return (
                            <div className='form-check form-check-inline' key={v}>
                                <input
                                    type='radio'
                                    id={radioId}
                                    className='form-check-input'
                                    name={groupName}
                                    value={v}
                                    checked={value === v}
                                    onChange={() => { onChange(v); onCommit(v); }}
                                />
                                <label className='form-check-label' htmlFor={radioId}>{lbl}</label>
                            </div>
                        );
                    })}
                </div>
            );
        }
        return (
            <input
                id={inputId}
                type='text'
                className='form-control'
                placeholder={def.placeholder}
                value={value}
                onChange={e => onChange(e.target.value)}
                onBlur={() => onCommit(value)}
            />
        );
    }

    const labelContent = (
        <>
            {def.label}
            {def.infoHref && (
                <a
                    href={def.infoHref}
                    target='_blank'
                    rel='noopener noreferrer'
                    className='text-info small ml-1'
                    title={`More about ${def.label}`}
                    style={{textDecoration: 'none'}}
                >
                    (i)
                </a>
            )}
        </>
    );

    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                {def.type === 'bool'
                    ? labelContent
                    : <label htmlFor={inputId} className='mb-0'>{labelContent}</label>}
            </th>
            <td>
                {renderInput()}
                {def.helpText && <small className='form-text text-muted'>{def.helpText}</small>}
            </td>
        </tr>
    );
};

export interface SectionProps {
    id: string;
    title: string;
    children: React.ReactNode;
}

export const Section = ({id, title, children}: SectionProps) => (
    <section className='section' id={id} aria-labelledby={`${id}-heading`}>
        <h2 id={`${id}-heading`} className='heading'>{title}</h2>
        {children}
    </section>
);

export const FieldsTable = ({children}: {children: React.ReactNode}) => (
    <table className='table table-borderless'>
        <tbody>{children}</tbody>
    </table>
);
