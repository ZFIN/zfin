import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import {
    AutocompleteEndpoint,
    useAutocomplete,
    useAutocompleteResolve,
    useDebouncedValue,
} from '../../api/queries';
import { api } from '../../api/client';
import { AutocompleteItemDTO } from '../../api/types';
import { viewConfigFrom, leafOf, commentsEnabled } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';
import { ValueDisplay } from '../../components/ValueDisplay';

/**
 * Type-ahead input that resolves to a single ZDB-ID. Used for cross-entity
 * fields — alleles (features endpoint) and genes (markers endpoint,
 * GENEDOM).
 *
 * <p>The Control's {@code options.searchEndpoint} (one of
 * {@code 'markers' | 'features' | 'persons' | 'pis'}) picks which
 * autocomplete service to hit. The value stored back in the form is the
 * ZDB-ID.
 *
 * <p>Once a value is chosen it renders as a removable <b>chip</b> showing
 * the record symbol + ZDB-ID (inspired by email "To" fields), so it's
 * obvious the field is bound to a specific record rather than free text.
 * These fields are single-value: while a chip is present the text input is
 * replaced by the chip, so there's no way to add a second. Editing means
 * removing the chip (×) and picking/typing a new value.
 *
 * <p>Symbol resolution: a freshly-picked suggestion carries its label; for
 * an id that was stored earlier (form data only has the raw ZDB-ID) we
 * resolve the symbol via {@link useAutocompleteResolve} (the autocomplete
 * endpoint with {@code exactMatch=true}). A directly typed/pasted id is
 * validated against the same exact-match lookup on blur — accepted as a
 * chip if it resolves, otherwise rejected with an inline error.
 */
function AutocompleteRenderer({
    data,
    handleChange,
    path,
    label,
    required,
    errors,
    visible,
    uischema,
    config,
}: ControlProps) {
    if (visible === false) {return null;}

    const view = viewConfigFrom(config);
    const fieldNameView = leafOf(path);
    if (view.readonly) {
        // When the stored value is a ZDB-ID (autocomplete picked from a server
        // entity), render it as a hyperlink to the entity's detail page and
        // use the optional displayLabel override for the link text so the
        // curator sees the human-readable name instead of the raw ID.
        const valueAsString = typeof data === 'string' ? data : null;
        const isZdbId = valueAsString != null && valueAsString.startsWith('ZDB-');
        const displayLabel = view.displayLabels[fieldNameView];
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }}>
                    <StatusBadge status={view.fieldStatus[fieldNameView]}/>
                    {label}
                </th>
                <td>
                    {isZdbId
                        ? <a href={`/${valueAsString}`}>{displayLabel ?? valueAsString}</a>
                        : <ValueDisplay value={data}/>}
                    <FieldHistory
                        recId={view.recId}
                        scope='field'
                        fieldName={fieldNameView}
                        label={label ?? fieldNameView}
                    />
                    {commentsEnabled(uischema) && (
                        <FieldComments
                            recId={view.recId}
                            scope='field'
                            fieldName={fieldNameView}
                            label={label ?? fieldNameView}
                        />
                    )}
                </td>
            </tr>
        );
    }

    const opts = ((uischema as {
        options?: {
            searchEndpoint?: AutocompleteEndpoint;
            typeGroup?: string;
            placeholder?: string;
            helpText?: string;
        };
    } | undefined)?.options) ?? {};
    const endpoint: AutocompleteEndpoint = opts.searchEndpoint ?? 'markers';
    const typeGroup = opts.typeGroup;

    // The stored ZDB-ID. Non-empty ⇒ a chip is shown instead of the input.
    const committed = typeof data === 'string' && data.trim() ? data : '';

    const [term, setTerm] = React.useState('');
    const [open, setOpen] = React.useState(false);
    const [picked, setPicked] = React.useState<AutocompleteItemDTO | null>(null);
    const [error, setError] = React.useState<string | null>(null);
    const inputRef = React.useRef<HTMLInputElement>(null);
    // Set when a suggestion's mousedown fires so the input's blur handler
    // skips its validate-and-commit (the pick already committed).
    const pickingRef = React.useRef(false);

    const debouncedTerm = useDebouncedValue(term, 200);
    const suggestions = useAutocomplete(endpoint, debouncedTerm, typeGroup);
    // Resolve the symbol for an id that wasn't picked this session.
    const resolveQ = useAutocompleteResolve(
        endpoint,
        picked && picked.value === committed ? '' : committed,
        typeGroup,
    );

    const fieldName = path.split('.').pop() ?? path;
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;

    const symbolOf = (item: AutocompleteItemDTO): string => {
        const suffix = ` (${item.value})`;
        return item.label.endsWith(suffix)
            ? item.label.slice(0, -suffix.length)
            : item.label;
    };

    const handlePick = (item: AutocompleteItemDTO) => {
        setPicked(item);
        setError(null);
        setTerm('');
        setOpen(false);
        handleChange(path, item.value);
    };

    const handleRemove = () => {
        setPicked(null);
        setError(null);
        setTerm('');
        handleChange(path, undefined);
        // Return focus to the input once it re-mounts in place of the chip.
        window.setTimeout(() => inputRef.current?.focus(), 0);
    };

    // Validate a directly typed/pasted value against the exact-match lookup;
    // accept it as a chip only if it resolves to a real record.
    const validateAndCommit = async () => {
        const candidate = term.trim();
        if (!candidate) {return;}
        try {
            const params = new URLSearchParams({ term: candidate, exactMatch: 'true' });
            if (typeGroup) {params.set('typeGroup', typeGroup);}
            const rows = await api.get<AutocompleteItemDTO[]>(
                `/autocomplete/${endpoint}?${params.toString()}`,
            );
            const found = rows[0];
            if (found) {
                setPicked(found);
                setError(null);
                setTerm('');
                handleChange(path, found.value);
            } else {
                setError(`No matching record found for “${candidate}”.`);
            }
        } catch {
            setError('Could not validate entry. Please try again.');
        }
    };

    // ─── Chip: a value is bound. Single-value, so no input is shown. ─────────
    if (committed) {
        const chipItem: AutocompleteItemDTO =
            picked && picked.value === committed
                ? picked
                : resolveQ.data?.[0] ?? { label: committed, value: committed };
        const symbol = symbolOf(chipItem);
        const showId = symbol !== chipItem.value;
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    {label}{required ? ' *' : ''}
                </th>
                <td>
                    <span
                        className='d-inline-flex align-items-center border rounded-pill bg-light px-2 py-1'
                        style={{ maxWidth: '100%' }}
                        title='One record can be linked here. Use × to remove.'
                    >
                        <span className='font-weight-bold text-truncate'>{symbol}</span>
                        {showId && (
                            <span className='text-muted small ml-2 text-truncate'>
                                {chipItem.value}
                            </span>
                        )}
                        <button
                            type='button'
                            className='btn btn-link p-0 ml-2 text-muted'
                            style={{ fontSize: '1.1rem', lineHeight: 1, textDecoration: 'none' }}
                            aria-label={`Remove ${symbol}`}
                            title='Remove'
                            onClick={handleRemove}
                        >
                            <span aria-hidden='true'>&times;</span>
                        </button>
                    </span>
                    {opts.helpText && (
                        <small className='form-text text-muted'>{opts.helpText}</small>
                    )}
                </td>
            </tr>
        );
    }

    // ─── Empty: type-ahead input + suggestion dropdown. ─────────────────────
    const items = suggestions.data ?? [];

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                <label htmlFor={inputId} className='mb-0'>
                    {label}{required ? ' *' : ''}
                </label>
            </th>
            <td>
                <div style={{ position: 'relative' }}>
                    <input
                        ref={inputRef}
                        id={inputId}
                        type='text'
                        className={`form-control${error ? ' is-invalid' : ''}`}
                        value={term}
                        placeholder={opts.placeholder}
                        onChange={(e) => {
                            setTerm(e.target.value);
                            setOpen(true);
                            if (error) {setError(null);}
                        }}
                        onFocus={() => setOpen(term.trim().length > 0)}
                        onBlur={() => {
                            // Delay so an option's mousedown→click lands first;
                            // skip validation if a pick already committed.
                            window.setTimeout(() => {
                                setOpen(false);
                                if (pickingRef.current) {
                                    pickingRef.current = false;
                                    return;
                                }
                                void validateAndCommit();
                            }, 150);
                        }}
                        autoComplete='off'
                    />
                    {open && items.length > 0 && (
                        <ul
                            className='list-group position-absolute w-100'
                            style={{ zIndex: 1100, maxHeight: 240, overflowY: 'auto' }}
                            role='listbox'
                        >
                            {items.map((it) => (
                                <li
                                    key={it.value}
                                    role='option'
                                    aria-selected={false}
                                    className='list-group-item list-group-item-action small'
                                    style={{ cursor: 'pointer' }}
                                    onMouseDown={(e) => {
                                        // mousedown (not click) so the
                                        // suggestion fires before the input's
                                        // blur handler closes the menu.
                                        e.preventDefault();
                                        pickingRef.current = true;
                                        handlePick(it);
                                    }}
                                >
                                    {it.label}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                {error && <small className='text-danger'>{error}</small>}
                {!error && errors && <small className='text-danger'>{errors}</small>}
                {opts.helpText && (
                    <small className='form-text text-muted'>{opts.helpText}</small>
                )}
            </td>
        </tr>
    );
}

export const autocompleteRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'autocomplete'))),
    renderer: withJsonFormsControlProps(AutocompleteRenderer),
};
