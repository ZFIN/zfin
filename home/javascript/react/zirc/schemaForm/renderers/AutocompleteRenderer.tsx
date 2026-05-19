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
    useDebouncedValue,
} from '../../api/queries';

/**
 * Type-ahead text input that resolves to a ZDB-ID. Used for cross-entity
 * fields on M5 (linked features), M6 (genes → markers), and M7 (lesions
 * referencing features).
 *
 * <p>The Control's {@code options.searchEndpoint} (one of
 * {@code 'markers' | 'features' | 'persons'}) picks which autocomplete
 * service to hit. The value stored back in the form is the ZDB-ID; the
 * label is shown in the input. The user can either pick a suggestion
 * from the dropdown or paste a ZDB-ID directly.
 *
 * <p>Three corner cases worth flagging:
 *
 * <ol>
 *   <li><b>Initial load with a value but no label cache.</b> When the
 *     form data already carries a ZDB-ID (existing record), we don't
 *     know the human label without a second request. For now we just
 *     show the ZDB-ID in the input. A label-by-id endpoint could
 *     replace this later.</li>
 *   <li><b>Clicking outside the dropdown.</b> Closing the suggestion
 *     list on blur would close it before an option's mousedown fires.
 *     We delay close via {@code setTimeout(close, 100)} so the option's
 *     click handler runs first.</li>
 *   <li><b>Empty term.</b> Suggests nothing rather than firing a
 *     wildcard query — the server caps results at 20 anyway, but the
 *     UX is better when an unfocused field shows nothing.</li>
 * </ol>
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
}: ControlProps) {
    if (visible === false) {return null;}

    const opts = ((uischema as { options?: { searchEndpoint?: AutocompleteEndpoint } } | undefined)?.options) ?? {};
    const endpoint: AutocompleteEndpoint = opts.searchEndpoint ?? 'markers';

    const value = (data as string | undefined) ?? '';
    const [term, setTerm] = React.useState(value);
    const [open, setOpen] = React.useState(false);
    const debouncedTerm = useDebouncedValue(term, 200);
    const suggestions = useAutocomplete(endpoint, debouncedTerm);

    // Sync the local input state when the form's data prop changes
    // out-of-band (e.g. autosave snapshot reset after a server response).
    React.useEffect(() => { setTerm(value); }, [value]);

    const fieldName = path.split('.').pop() ?? path;
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;

    const handlePick = (item: { label: string; value: string }) => {
        setTerm(item.label);
        handleChange(path, item.value);
        setOpen(false);
    };

    const handleBlur = () => {
        // Delay close so an option's mousedown→click can land first.
        window.setTimeout(() => setOpen(false), 150);
    };

    const items = suggestions.data ?? [];

    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                <label htmlFor={inputId} className='mb-0'>
                    {label}{required ? ' *' : ''}
                </label>
            </th>
            <td>
                <div style={{ position: 'relative' }}>
                    <input
                        id={inputId}
                        type='text'
                        className='form-control'
                        value={term}
                        onChange={(e) => {
                            setTerm(e.target.value);
                            setOpen(true);
                            // Don't commit on every keystroke — a free-text
                            // typo would clear a previously-picked ZDB-ID
                            // immediately. The picker commits via handlePick;
                            // a manual paste of a ZDB-ID commits on blur.
                        }}
                        onFocus={() => setOpen(term.trim().length > 0)}
                        onBlur={() => {
                            // Commit free-text on blur in case the user pasted
                            // a ZDB-ID directly without using the dropdown.
                            if (term !== value) {handleChange(path, term);}
                            handleBlur();
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
                                    aria-selected={value === it.value}
                                    className='list-group-item list-group-item-action small'
                                    style={{ cursor: 'pointer' }}
                                    onMouseDown={(e) => {
                                        // mousedown (not click) so the
                                        // suggestion fires before the input's
                                        // blur handler closes the menu.
                                        e.preventDefault();
                                        handlePick(it);
                                    }}
                                >
                                    {it.label}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                {errors && (
                    <small className='text-danger'>{errors}</small>
                )}
            </td>
        </tr>
    );
}

export const autocompleteRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'autocomplete'))),
    renderer: withJsonFormsControlProps(AutocompleteRenderer),
};
